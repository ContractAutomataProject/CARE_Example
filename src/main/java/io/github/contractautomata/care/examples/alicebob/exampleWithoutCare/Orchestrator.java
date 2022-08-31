package io.github.contractautomata.care.examples.alicebob.exampleWithoutCare;

import io.github.contractautomata.care.examples.alicebob.exampleWithoutCare.utilities.TypedCALabelExample;
import io.github.contractautomata.care.examples.alicebob.exampleWithoutCare.utilities.AutoCloseableListExample;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.operations.CompositionFunction;
import io.github.contractautomata.catlib.operations.OrchestrationSynthesisOperator;
import io.github.contractautomata.catlib.requirements.StrongAgreement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Orchestrator implements Runnable {

    public final static String stop_msg = "ORC_STOP";
    public final static String ack_msg= "ACK";
    public final static String choice_msg = "ORC_CHOICE";
    public final static String stop_choice = "CHOICE_STOP";
    public final static String check_msg = "ORC_CHECK";

    private final List<Integer> ports;
    private final List<String> addresses;
    private final Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> contract;
    private final Predicate<CALabel> pred;
    private State<String> currentState;


    public Orchestrator(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req,
                                 Predicate<CALabel> pred,
                                 List<Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, TypedCALabelExample>>> contracts,
                                 List<String> hosts, List<Integer> port) throws  ClassNotFoundException, IOException {
        super();

        if (hosts.size()!=port.size())
            throw new IllegalArgumentException();


        this.pred=pred;
        this.addresses = hosts;
        this.ports = port;
        checkCompatibility();

        CompositionFunction<String,State<String>,TypedCALabelExample,ModalTransition<String,Action,State<String>,TypedCALabelExample>,
                Automaton<String,Action,State<String>,ModalTransition<String,Action,State<String>,TypedCALabelExample>>> cf =
                new CompositionFunction<>
                        (contracts, TypedCALabelExample::match,State::new,ModalTransition::new,TypedCALabelExample::new,Automaton::new,
                                t->pred.negate().test(t.getLabel()));

        //conver to CALabel to use the synthesis
        Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> comp
                = new Automaton<>(cf.apply(Integer.MAX_VALUE)
                .getTransition().parallelStream()
                .map(t->new ModalTransition<>(t.getSource(),(CALabel)t.getLabel(),t.getTarget(),t.getModality()))
                .collect(Collectors.toSet()));

        contract = new OrchestrationSynthesisOperator<>(pred,req)
                .apply(comp);

    }

    public Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> getContract() {
        return contract;
    }

    public boolean isEmptyOrchestration() {
        return contract==null;
    }

    public State<String> getCurrentState() {
        return currentState;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    @Override
    public void run() {
        if (this.isEmptyOrchestration())
            throw new IllegalArgumentException("Empty orchestration");

        System.out.println("The orchestration is : " + contract.toString());

        this.currentState = contract.getStates().parallelStream()
                .filter(State::isInitial)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);

        try (AutoCloseableListExample<Socket> sockets = new AutoCloseableListExample<>();
             AutoCloseableListExample<ObjectOutputStream> oout = new AutoCloseableListExample<>();
             AutoCloseableListExample<ObjectInputStream> oin = new AutoCloseableListExample<>())
        {
            for (int i=0;i<ports.size();i++) {
                Socket s = new Socket(InetAddress.getByName(addresses.get(i)), ports.get(i));
                sockets.add(s);
                oout.add(new ObjectOutputStream(s.getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(s.getInputStream()));
            }

            while(true)
            {
                System.out.println("Orchestrator, current state is "+currentState.toString());

                //get forward state of the state
                List<ModalTransition<String,Action,State<String>,CALabel>> fs = new ArrayList<>(contract.getForwardStar(currentState));

                //check absence of deadlocks
                if (fs.isEmpty()&&!currentState.isFinalState())
                    throw new RuntimeException("Deadlocked Orchestration!");


                //the choice on the transition to fire or to terminate is made beforehand
                final String choice;
                if (fs.size()>1 || (currentState.isFinalState() && fs.size()==1))
                {
                    System.out.println("Orchestrator sending choice message");
                    for (ObjectOutputStream o : oout) {
                        o.writeObject(this.choice_msg);
                        o.flush();
                    }
                    choice = choice(oout,oin);
                }
                else if (fs.size()==1)
                    choice = fs.get(0).getLabel().getAction().getLabel();
                else
                    choice="";//for initialization

                //check final state
                if (currentState.isFinalState() && (fs.isEmpty()||choice.equals(stop_choice)))
                {
                    System.out.println("Orchestrator sending termination message");
                    for (ObjectOutputStream o : oout) {
                        o.flush();
                        o.writeObject(stop_msg);
                    }
                    return;
                }

                //select a transition to fire
                ModalTransition<String,Action,State<String>,CALabel> t = fs.stream()
                        .filter(tr->tr.getLabel().getAction().getLabel().equals(choice))
                        .findAny()
                        .orElseThrow(RuntimeException::new);


                if (t.getLabel().isRequest())
                    throw new RuntimeException("The orchestration has unmatched requests!");

                if (t.getLabel().isOffer()&&(pred instanceof StrongAgreement))
                    throw new RuntimeException("The orchestration has unmatched offers!");

                System.out.println("Orchestrator, selected transition is "+t);

                this.doAction(t, oout, oin);

                currentState = t.getTarget();
            }

        }catch (Exception e) {
            RuntimeException re = new RuntimeException();
            re.addSuppressed(e);
            throw new RuntimeException();
        }
    }

    public String choice(AutoCloseableListExample<ObjectOutputStream> oout, AutoCloseableListExample<ObjectInputStream> oin)
            throws IOException, ClassNotFoundException {

        //computing services that can choose (those involved in one next transition)
        Set<Integer> toInvoke = this.getContract()
                .getForwardStar(this.getCurrentState()).stream()
                .flatMap(t->t.getLabel().isOffer()? Stream.of(t.getLabel().getOfferer())
                        :Stream.of(t.getLabel().getOfferer(),t.getLabel().getRequester()))
                .distinct().collect(Collectors.toSet());

        //asking either to choose or skip to the services
        for (int i=0;i<oout.size();i++){
            ObjectOutputStream oos = oout.get(i);
            oos.writeObject(toInvoke.contains(i)?this.choice_msg:null);
            oout.get(i).flush();
        }

        //computing and sending the possible choices
        String[] toChoose = this.getContract()
                .getForwardStar(this.getCurrentState()).stream()
                .map(t->t.getLabel().getAction().getLabel())
                .toArray(String[]::new);
        for (Integer i : toInvoke) {
            oout.get(i).writeObject(toChoose);
            oout.get(i).flush();
        }

        //receiving the choice of each service
        List<String> choices = new ArrayList<>();
        for (int i=0;i<oin.size();i++)
            choices.add((String) oin.get(i).readObject());

        return	choices.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max((x,y)->x.getValue().intValue()-y.getValue().intValue()).orElseThrow(RuntimeException::new).getKey();

    }

    public String getChoiceType() {
        return "Majoritarian";
    }


    private void checkCompatibility() throws IOException, ClassNotFoundException {
        for (int i=0;i<ports.size();i++) {
            try (Socket s = new Socket(InetAddress.getByName(addresses.get(i)), ports.get(i));
                 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(s.getInputStream()))
            {
                oos.writeObject(check_msg);
                oos.writeObject(this.getChoiceType());
                oos.writeObject(this.getActionType());

                String msg = (String) ois.readObject();
                if (!msg.equals(ack_msg)) {
                    throw new IllegalArgumentException("Uncompatible type with service at "+s+" "+msg);
                }

            }

        }
    }

    public void doAction(ModalTransition<String, Action, State<String>, ? extends CALabel> t, AutoCloseableListExample<ObjectOutputStream> oout, AutoCloseableListExample<ObjectInputStream> oin) throws IOException, ClassNotFoundException {

        if (t.getLabel().isMatch())
        {
            // match: firstly interact with the requester
            oout.get(t.getLabel().getRequester()).writeObject(t.getLabel().getAction().getLabel());
            oout.get(t.getLabel().getRequester()).flush();
            oout.get(t.getLabel().getRequester()).writeObject(null);
            oout.get(t.getLabel().getRequester()).flush();

            Object rep_req = oin.get(t.getLabel().getRequester()).readObject();

            //forwarding the received requester payload to the offerer
            oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getAction().getLabel());
            oout.get(t.getLabel().getOfferer()).flush();
            oout.get(t.getLabel().getOfferer()).writeObject(rep_req);
            oout.get(t.getLabel().getOfferer()).flush();

            Object rep_off = oin.get(t.getLabel().getOfferer()).readObject();

            //forwarding the received  offerer payload to the requester
            oout.get(t.getLabel().getRequester()).writeObject(rep_off);
            oout.get(t.getLabel().getRequester()).flush();
        }
        else
        if (t.getLabel().isOffer()){
            //only invokes the offerer and then continue
            oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getAction().getLabel());
            oout.get(t.getLabel().getOfferer()).flush();
            oout.get(t.getLabel().getOfferer()).writeObject(null);
            oout.get(t.getLabel().getOfferer()).flush();
            oin.get(t.getLabel().getOfferer()).readObject();
        }
        else throw new IllegalArgumentException("The transition is not an offer nor a request");
    }

    public String getActionType() {
        return "Centralised";
    }


}
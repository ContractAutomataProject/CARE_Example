package io.github.contractautomata.care.examples.compositionService.withoutCARE;

import io.github.contractautomata.care.examples.compositionService.withoutCARE.utilities.TypedCALabelExample;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.converters.AutDataConverter;

import javax.net.ServerSocketFactory;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class ServiceImplementation implements Runnable {
    private final static String dir =
            Paths.get(System.getProperty("user.dir")).getParent()+ File.separator
                    +"CARE_Example"+File.separator+"resources"+File.separator+ "compositionService" +File.separator;

    private final Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, TypedCALabelExample>> contract;
    private final int port;
    private final Object service;
    private final int timeout=600000;//10 minutes

    private final Random generator;

    public ServiceImplementation() throws IOException {
        super();

        generator = new Random();
        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);
        this.port = 8082;
        this.service=new Service();

        Method[] arrm = service.getClass().getMethods();

        //change the labels to typed labels
        this.contract = new Automaton<>
                (conv.importMSCA(dir+ "service.data").getTransition().stream()
                        .map(t->{ Method met = Arrays.stream(arrm)
                                .filter(m->m.getName().equals(t.getLabel().getAction().getLabel()))
                                .findFirst().orElseThrow(IllegalArgumentException::new);
                            TypedCALabelExample tcl = new TypedCALabelExample(t.getLabel(),met.getParameterTypes()[0],met.getReturnType());
                            return new ModalTransition<>(t.getSource(),tcl,t.getTarget(),t.getModality());})
                        .collect(Collectors.toSet()));
    }

    public int getPort() {
        return port;
    }

    public Object getService() {
        return service;
    }

    public Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,TypedCALabelExample>> getContract() {
        return contract;
    }

    @Override
    public void run() {
        try (ServerSocket servsock =  ServerSocketFactory.getDefault().createServerSocket(port))
        {
            while (true) {
                new Thread() {
                    private final Socket socket;
                    private io.github.contractautomata.catlib.automaton.state.State<String> currentState;
                    {
                        socket = servsock.accept();
                        socket.setSoTimeout(timeout);
                        currentState = contract.getStates().parallelStream()
                                .filter(io.github.contractautomata.catlib.automaton.state.State::isInitial)
                                .findAny()
                                .orElseThrow(IllegalArgumentException::new);
                    }

                    @Override
                    public void run() {
                        try    (ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
                                ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream()))
                        {
                            oout.flush();
                            System.out.println("Connection with service started host " + socket.getLocalAddress().toString() + ", port "+socket.getLocalPort());
                            while (true) {
                                //receive message from orchestrator
                                String action = (String) oin.readObject();

                                System.out.println("Service on host " + socket.getLocalAddress().toString() + ", port "+socket.getLocalPort()+": received message "+action);

                                if (action.equals(OrchestratorCompositionExample.check_msg)) {
                                    check(oin, oout);
                                    break;
                                }
                                if (action.equals(OrchestratorCompositionExample.stop_msg))
                                {
                                    if (currentState.isFinalState())
                                        break;
                                    else
                                        throw new RuntimeException("Not in a final state!");
                                }

                                if (action.startsWith(OrchestratorCompositionExample.choice_msg))
                                {
                                    choice(currentState,oout,oin);
                                    continue;
                                }

                                //find a transition to fire
                                ModalTransition<String,Action, io.github.contractautomata.catlib.automaton.state.State<String>,TypedCALabelExample> t =
                                        contract.getForwardStar(currentState)
                                                .stream()
                                                .filter(tr->tr.getLabel().getAction().getLabel().equals(action))
                                                .findAny()
                                                .orElseThrow(UnsupportedOperationException::new);

                                try {
                                    Method[] arrm = service.getClass().getMethods();
                                    for (Method m1 : arrm)
                                    {
                                        if (m1.getName().equals(action)){
                                            invokeMethod(m1, oin, oout, t);
                                        }
                                    }
                                } catch(Exception e) {
                                    ContractViolationExceptionExample re = new ContractViolationExceptionExample(socket.getRemoteSocketAddress());
                                    re.addSuppressed(e);
                                    throw re;
                                }

                                //update state
                                currentState=t.getTarget();
                            }

                        } catch (SocketTimeoutException e) {
                            ContractViolationExceptionExample re = new ContractViolationExceptionExample(socket.getRemoteSocketAddress());
                            re.addSuppressed(e);
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            RuntimeException re = new RuntimeException();
                            re.addSuppressed(e);
                            throw new RuntimeException(e);
                        }

                        System.out.println("Session terminated at host " + socket.getLocalAddress().toString()
                                + ", port "+socket.getLocalPort());
                    }
                }.start();
            }
        } catch (IOException e2) {
            RuntimeException re = new RuntimeException();
            re.addSuppressed(e2);
            throw new RuntimeException(e2);
        }
    }

    public void choice(State<String> currentState, ObjectOutputStream oout, ObjectInputStream oin) throws IOException, ClassNotFoundException {

        //receive message from orchestrator on whether to choose or skip
        String action = (String) oin.readObject();

        System.out.println("received "+action);

        if (action==null) //skip
            return;

        //receiving the possible choices
        String[] toChoose = (String[]) oin.readObject();

        String select =select(currentState, toChoose);

        //sending the selected choice
        oout.writeObject(select);
        oout.flush();
    }


    public String select(State<String> currentState, String[] toChoose) {
        return "";
    }

    public String getChoiceType() {
        return "Majoritarian";
    }

    private void check(ObjectInputStream oin, ObjectOutputStream oout) throws ClassNotFoundException, IOException {
        String orcChoiceType = (String) oin.readObject();
        String orcActType = (String) oin.readObject();

        if (orcChoiceType.equals(this.getChoiceType())&&orcActType.equals(this.getActionType()))
            oout.writeObject(OrchestratorCompositionExample.ack_msg);
        else {
            String msg = (orcChoiceType.equals(this.getChoiceType())?"":"uncompatible choice ") +
                    (orcActType.equals(this.getActionType())?"":" uncompatible action");
            oout.writeObject(msg);
        }
    }

    public void invokeMethod(Method m1, ObjectInputStream oin, ObjectOutputStream oout,
                             ModalTransition<String, Action, State<String>, TypedCALabelExample> t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {
        Class<?> c=m1.getParameterTypes()[0];
        Object req=m1.invoke(this.getService(),c.cast(oin.readObject()));
        oout.writeObject(req);
        oout.flush();

        if (t.getLabel().isRequest()) {
            //if the action is a request, the payload from the offerer will be received
            m1.invoke(this.getService(),c.cast(oin.readObject()));
        }
    }

    public String getActionType() {
        return "Centralised";
    }
}

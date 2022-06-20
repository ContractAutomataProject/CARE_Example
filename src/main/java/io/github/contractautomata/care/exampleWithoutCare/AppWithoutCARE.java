package io.github.contractautomata.care.exampleWithoutCare;

import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.BasicState;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import io.github.contractautomata.catlib.requirements.Agreement;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AppWithoutCARE {
    private final static String dir =
            Paths.get(System.getProperty("user.dir")).getParent()+ File.separator
                    +"CARE_Example"+File.separator+"resources"+File.separator;

    public static void main(String[] args) throws Exception {

        // the designer of the application creates the  requirement
        //substitute with = createNewRequirement(); to change the application behaviour
        Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req = createOldRequirement();
        System.out.println("Requirement : \n" + req);


        //the services providers publish their contracts and services, in this example everything is local
        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);
        AliceImplementation alice = new AliceImplementation();
        BobImplementation bob = new BobImplementation();

        new Thread(alice).start();
        new Thread(bob).start();


        // when the hosts and ports running the threads alice and bob are discovered,
        // the runnable orchestration can be built
        Orchestrator ror = new Orchestrator(req,new Agreement(),
                Arrays.asList(alice.getContract(),bob.getContract()),
                Arrays.asList(null,null),
                Arrays.asList(alice.getPort(),bob.getPort()));

        //trying to execute the orchestration
        if (ror.isEmptyOrchestration())
            System.out.println("No orchestration found");
        else
            new Thread(ror).start();

    }

    /**
     * just an example of how it is possible to hard-code an automaton rather than loading it from a file.
     *
     * @return the requirement
     */
    @SuppressWarnings("unused")
    private static  Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>>  createNewRequirement() {
        State<String> s0 = new State<>(List.of(new BasicState<>("0",true,false)));
        State<String> s1 = new State<>(List.of(new BasicState<>("1",false,false)));
        State<String> s2 = new State<>(List.of(new BasicState<>("2",false,true)));
        ModalTransition<String, Action, State<String>, Label<Action>> t1 =
                new ModalTransition<>(s0, new Label<>(List.of(new Action("euro"))), s1, ModalTransition.Modality.PERMITTED);
        ModalTransition<String, Action, State<String>, Label<Action>> t2 =
                new ModalTransition<>(s1, new Label<>(List.of(new Action("coffee"))), s2, ModalTransition.Modality.PERMITTED);

        return new Automaton<>(Set.of(t1,t2));
    }

    /**
     * just an example of how it is possible to hard-code an automaton rather than loading it from a file.
     *
     * @return the requirement
     */
    @SuppressWarnings("unused")
    private static  Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>>  createOldRequirement() {
        State<String> s0 = new State<>(List.of(new BasicState<>("0",true,false)));
        State<String> s1 = new State<>(List.of(new BasicState<>("1",false,false)));
        State<String> s2 = new State<>(List.of(new BasicState<>("3",false,false)));
        State<String> s3 = new State<>(List.of(new BasicState<>("2",false,true)));

        ModalTransition<String, Action, State<String>, Label<Action>> t1 =
                new ModalTransition<>(s0,
                        new Label<>(List.of(new Action("euro"))),
                        s1, ModalTransition.Modality.PERMITTED);

        ModalTransition<String, Action, State<String>, Label<Action>> t2 =
                new ModalTransition<>(s1,
                        new Label<>(List.of(new Action("coffee"))),
                        s3, ModalTransition.Modality.PERMITTED);


        ModalTransition<String, Action, State<String>, Label<Action>> t3 =
                new ModalTransition<>(s0,
                        new Label<>(List.of(new Action("dollar"))),
                        s2, ModalTransition.Modality.PERMITTED);

        ModalTransition<String, Action, State<String>, Label<Action>> t4 =
                new ModalTransition<>(s2,
                        new Label<>(List.of(new Action("tea"))),
                        s3, ModalTransition.Modality.PERMITTED);

        return new Automaton<>(Set.of(t1,t2,t3,t4));
    }

}

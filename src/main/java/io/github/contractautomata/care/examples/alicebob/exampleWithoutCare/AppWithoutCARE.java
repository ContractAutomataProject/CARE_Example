package io.github.contractautomata.care.examples.alicebob.exampleWithoutCare;

import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.BasicState;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.requirements.Agreement;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AppWithoutCARE {

    public static void main(String[] args) throws Exception {

        Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req = createOldRequirement();
        System.out.println("Requirement : \n" + req);

        AliceImplementation alice = new AliceImplementation();
        BobImplementation bob = new BobImplementation();

        new Thread(alice).start();
        new Thread(bob).start();

        Orchestrator orc = new Orchestrator(req,new Agreement(),Arrays.asList(alice.getContract(),bob.getContract()),Arrays.asList(null,null),Arrays.asList(alice.getPort(),bob.getPort()));

        if (orc.isEmptyOrchestration())
            System.out.println("No orchestration found");
        else
            new Thread(orc).start();

    }

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

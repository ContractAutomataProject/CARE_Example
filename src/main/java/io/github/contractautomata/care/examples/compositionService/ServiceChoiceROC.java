package io.github.contractautomata.care.examples.compositionService;

import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

import java.io.IOException;

public class ServiceChoiceROC extends MajoritarianChoiceRunnableOrchestratedContract {
    public ServiceChoiceROC(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> contract, int port, Object service, OrchestratedAction act) throws IOException {
        super(contract, port, service, act);
    }

    @Override
    public String select(State<String> currentState, String[] toChoose) {
        return "";
    }
}

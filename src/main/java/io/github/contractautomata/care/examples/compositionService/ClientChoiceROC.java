package io.github.contractautomata.care.examples.compositionService;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

import java.io.IOException;

public class ClientChoiceROC extends MajoritarianChoiceRunnableOrchestratedContract {
    public ClientChoiceROC(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> contract, int port, Client service, OrchestratedAction act) throws IOException {
        super(contract, port, service, act);
    }

    @Override
    public String select(State<String> currentState, String[] toChoose) {
        Client client;
        if (this.getService() instanceof Client){
            client = (Client) this.getService();
        } else throw new RuntimeException();

        if (currentState.getState().get(0).getState().equals("Computing")) {
            if (client.getBound() <= 0)
                return "quit";
            System.out.println("Do you want to update the composition? (yes/no)");
            if (client.getScanner().nextLine().contains("y")) {
                return "update";
            } else {
                return "quit";
            }
        }
        else if (currentState.getState().get(0).getState().equals("Init")) {
            System.out.println("Do you want to compute a new composition? (yes/no)");
            if (client.getScanner().nextLine().contains("y")) {
                return "create";
            } else {
                client.getScanner().close();
                return RunnableOrchestration.stop_choice;
            }
        } else return "";
    }
}

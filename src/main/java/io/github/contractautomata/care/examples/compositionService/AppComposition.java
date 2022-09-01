package io.github.contractautomata.care.examples.compositionService;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratorAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestration;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import io.github.contractautomata.catlib.requirements.Agreement;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Example of execution of the client and composition service example
 */
public class AppComposition {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);

        //instantiating the services
        RunnableOrchestratedContract client = new ClientChoiceROC(loadAutomaton("client.data"),
                8080, new Client(), new CentralisedOrchestratedAction());
        RunnableOrchestratedContract service = new ServiceChoiceROC(loadAutomaton( "service.data"),
                8082, new Service(),  new CentralisedOrchestratedAction());

        //running the services
        new Thread(client,"Client").start();
        new Thread(service,"Server").start();

        //instantiating the orchestration
        RunnableOrchestration ror = new MajoritarianChoiceRunnableOrchestration(null,new Agreement(),
                Arrays.asList(client.getContract(),service.getContract()),
                Arrays.asList(null,null),
                Arrays.asList(client.getPort(),service.getPort()),
                new CentralisedOrchestratorAction());

        //executing the orchestration
        if (ror.isEmptyOrchestration())
            System.out.println("No orchestration found");
        else
            new Thread(ror,"Orc").start();

    }

    public static Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> loadAutomaton(String filename) throws IOException {
        InputStream in = AppComposition.class.getClassLoader().getResourceAsStream(filename);
        File f = new File(filename);
        FileUtils.copyInputStreamToFile(in, f);
        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);
        Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> aut = conv.importMSCA(filename);
        f.delete();
        return aut;
    }
}

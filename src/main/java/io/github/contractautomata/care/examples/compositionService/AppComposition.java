package io.github.contractautomata.care.examples.compositionService;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratorAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestration;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import io.github.contractautomata.catlib.requirements.Agreement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class AppComposition {
    private final static String dir =
            Paths.get(System.getProperty("user.dir")).getParent()+ File.separator
                    +"CARE_Example"+File.separator+"resources"+File.separator + "compositionService"+File.separator;
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);
        RunnableOrchestratedContract client = new ClientChoiceROC(conv.importMSCA(dir+ "client.data"),
                8080, new Client(), new CentralisedOrchestratedAction());
        RunnableOrchestratedContract service = new ServiceChoiceROC(conv.importMSCA(dir+ "service.data"),
                8082, new Service(),  new CentralisedOrchestratedAction());

        new Thread(client,"Client").start();
        new Thread(service,"Server").start();

        RunnableOrchestration ror = new MajoritarianChoiceRunnableOrchestration(null,new Agreement(),
                Arrays.asList(client.getContract(),service.getContract()),
                Arrays.asList(null,null),
                Arrays.asList(client.getPort(),service.getPort()),
                new CentralisedOrchestratorAction());

        //trying to execute the orchestration
        if (ror.isEmptyOrchestration())
            System.out.println("No orchestration found");
        else
            new Thread(ror,"Orc").start();

    }
}

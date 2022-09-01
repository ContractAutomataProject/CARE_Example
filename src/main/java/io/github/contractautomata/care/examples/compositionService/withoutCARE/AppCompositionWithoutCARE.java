package io.github.contractautomata.care.examples.compositionService.withoutCARE;

import io.github.contractautomata.catlib.requirements.Agreement;

import java.util.Arrays;

public class AppCompositionWithoutCARE {

    public static void main(String[] args) throws Exception {


        ClientImplementation client = new ClientImplementation();
        ServiceImplementation service = new ServiceImplementation();

        new Thread(client).start();
        new Thread(service).start();

        OrchestratorCompositionExample orc = new OrchestratorCompositionExample(null,new Agreement(),Arrays.asList(client.getContract(),service.getContract()),Arrays.asList(null,null),Arrays.asList(client.getPort(),service.getPort()));

        if (orc.isEmptyOrchestration())
            System.out.println("No orchestration found");
        else
            new Thread(orc).start();

    }

}

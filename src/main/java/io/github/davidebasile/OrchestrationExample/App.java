package io.github.davidebasile.OrchestrationExample;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import contractAutomata.automaton.Automaton;
import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.label.Label;
import contractAutomata.automaton.state.BasicState;
import contractAutomata.automaton.transition.Transition;
import contractAutomata.converters.DataConverter;
import contractAutomata.requirements.Agreement;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.Uniform.UniformChoiceRunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.Uniform.UniformChoiceRunnableOrchestration;
import io.github.davidebasile.OrchestrationExample.principals.Alice;
import io.github.davidebasile.OrchestrationExample.principals.Bob;

public class App {
	private final static String dir = System.getProperty("user.dir")+File.separator
			+"resources"+File.separator;

	public static void main(String[] args) throws IOException {
		Automaton<String, BasicState,Transition<String, BasicState,Label>> req = createOldRequirement();

		System.out.println("Requirement : \n" + req.toString());

		MSCA ca = new DataConverter().importMSCA(dir+"Alice.data");
		RunnableOrchestratedContract alice = new UniformChoiceRunnableOrchestratedContract(ca,8080,new Alice());
		new Thread(alice).start();
		
		MSCA cb = new DataConverter().importMSCA(dir+"Bob.data");
		RunnableOrchestratedContract bob = new UniformChoiceRunnableOrchestratedContract(cb,8081,new Bob());
		new Thread(bob).start();

		// assume the remote hosts and ports running the threads alice and bob are discovered, 
		// locally the orchestration has only their contracts ca and cb, the actual code of the services 
		// is running remotely.
		
		new Thread(new UniformChoiceRunnableOrchestration(req,
				new Agreement(),
				Arrays.asList(ca,cb),
				Arrays.asList(null,null),//local host
				Arrays.asList(alice.getPort(),bob.getPort())))
		.start();
	}

	/**
	 * just an example of how it is possible to hard-code an automaton rather than loading it from a file.
	 * 
	 * @return the requirement
	 */

	@SuppressWarnings("unused")
	private static  Automaton<String, BasicState,Transition<String, BasicState,Label>>  createNewRequirement() {
		BasicState s0 = new BasicState("0",true,false);
		BasicState s1 = new BasicState("1",false,false);
		BasicState s2 = new BasicState("2",false,true);
		Transition<String, BasicState,Label> t1 = new Transition<>(s0, new Label("euro"), s1);
		Transition<String, BasicState,Label> t2 = new Transition<>(s1, new Label("coffee"), s2);
		return new Automaton<>(Set.of(t1,t2));
	}

	/**
	 * just an example of how it is possible to hard-code an automaton rather than loading it from a file.
	 * 
	 * @return the requirement
	 */
	private static  Automaton<String, BasicState,Transition<String, BasicState,Label>>  createOldRequirement() {
		BasicState s0 = new BasicState("0",true,false);
		BasicState s1 = new BasicState("1",false,false);
		BasicState s2 = new BasicState("3",false,false);
		BasicState s3 = new BasicState("2",false,true);
		Transition<String, BasicState,Label> t1 = new Transition<>(s0, new Label("euro"), s1);
		Transition<String, BasicState,Label> t2 = new Transition<>(s1, new Label("coffee"), s3);
		Transition<String, BasicState,Label> t3 = new Transition<>(s0, new Label("dollar"), s2);
		Transition<String, BasicState,Label> t4 = new Transition<>(s2, new Label("tea"), s3);
		return new Automaton<>(Set.of(t1,t2,t3,t4));
	}

}

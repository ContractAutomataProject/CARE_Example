package io.github.contractautomata.care.examples.compositionService.withoutCARE;

import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import io.github.contractautomata.catlib.operations.MSCACompositionFunction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the composition service contract automaton
 *
 * Rank: 1
 * Initial state: [Init]
 * Final states: [[Init]]
 * Transitions:
 * ([Init],[!create],[Computing])
 * ([Computing],[!update],[Computing])
 * ([Computing],[!quit],[Init])
 *
 */
public class Service {
    private MSCACompositionFunction<String> cf;
    private Automaton<String,Action,State<String>,ModalTransition<String,Action,State<String>,CALabel>> aut;

    /**
     * create offer
     * @param pl  the payload received by the requester
     * @return  the composed automaton
     * @throws IOException
     */
    public String create(Payload pl) throws IOException {
        AutDataConverter<CALabel> conv = new AutDataConverter<>(CALabel::new);
        List<Automaton<String,Action,State<String>,ModalTransition<String,Action,State<String>,CALabel>>> laut = new ArrayList<>();
        for (int i=0;i<pl.getAutomata().size();i++){
            Files.write(Paths.get(i+".data"), pl.getAutomata().get(i).getBytes());
            laut.add(conv.importMSCA(i+".data"));
            Files.delete(Paths.get(i+".data"));
        }

        this.cf = new MSCACompositionFunction<>(laut, pl.isClosedAgreement() ? t -> t.getLabel().isRequest() : null);
        int bound = pl.getBound() > 0 ? pl.getBound() : Integer.MAX_VALUE;
        try {
            aut = this.cf.apply(bound);
        }catch(Exception ex){}
        if (aut!=null)
            return aut.toString();
        else
            return "";
    }

    /**
     * update offer
     * @param bound  the updated bound received from the client
     * @return  the updated composition
     */
    public String update(Integer bound) {
        try {
            aut = this.cf.apply(bound);
        }catch(Exception ex){}
        if (aut!=null)
            return aut.toString();
        else
            return "";
    }

    /**
     * quit offer
     * @param arg
     * @return
     */
    public String quit(String arg){
        return "";
    }
}
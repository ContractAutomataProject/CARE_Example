package io.github.contractautomata.care.examples.compositionService.withCARE;

import java.io.Serializable;
import java.util.List;

/**
 * Class containing the payload of a create request send by the client to the service
 */
public class Payload implements Serializable {
    private final List<String> automata;
    private final boolean isClosedAgreement;
    private final int bound;

    public Payload(List<String> automata, boolean isClosedAgreement, int bound) {
        this.automata = automata;
        this.isClosedAgreement = isClosedAgreement;
        this.bound = bound;
    }

    public int getBound() {
        return bound;
    }

    public boolean isClosedAgreement() {
        return isClosedAgreement;
    }

    public List<String> getAutomata() {
        return automata;
    }
}

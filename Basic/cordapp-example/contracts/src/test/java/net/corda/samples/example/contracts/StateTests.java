package net.corda.samples.example.contracts;

import net.corda.samples.example.states.EvalState;
import net.corda.testing.node.MockServices;
import org.junit.Test;

public class StateTests {
    private final MockServices ledgerServices = new MockServices();

    @Test
    public void hasAmountFieldOfCorrectType() throws NoSuchFieldException {
        // Does the message field exist?
        EvalState.class.getDeclaredField("value");
        // Is the message field of the correct type?
        assert(EvalState.class.getDeclaredField("value").getType().equals(Integer.class));
    }
}
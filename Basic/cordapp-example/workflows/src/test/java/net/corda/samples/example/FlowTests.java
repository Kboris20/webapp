package net.corda.samples.example;

import net.corda.samples.example.flows.EvalFlow;
import net.corda.samples.example.states.EvalState;
import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;

    @Before
    public void setup() {

        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("net.corda.samples.example.contracts"),
                TestCordapp.findCordapp("net.corda.samples.example.flows"))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /* @Test
    public void flowRejectsInvalidIOUs() throws Exception {
        // The EvalContract specifies that IOUs cannot have negative values.
        EvalFlow.Initiator flow = new EvalFlow.Initiator(-1, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        // The EvalContract specifies that IOUs cannot have negative values.
        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheInitiator() throws Exception {
        EvalFlow.Initiator flow = new EvalFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        SignedTransaction signedTx = future.get();
        signedTx.verifySignaturesExcept(b.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheAcceptor() throws Exception {
        EvalFlow.Initiator flow = new EvalFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        SignedTransaction signedTx = future.get();
        signedTx.verifySignaturesExcept(a.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void flowRecordsATransactionInBothPartiesTransactionStorages() throws Exception {
        EvalFlow.Initiator flow = new EvalFlow.Initiator(, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTx = future.get();

        // We check the recorded transaction in both vaults.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            assertEquals(signedTx, node.getServices().getValidatedTransactions().getTransaction(signedTx.getId()));
        }
    }*/

    @Test
    public void recordedTransactionHasNoInputsAndASingleOutputTheInputIOU() throws Exception {
        String titreArticle= "test";
        String c1= "test";
        String c2= "test";
        String c3= "test";
        String c4= "test";
        String forme= "test";
        String comment= "test";
        String dateArticle= "test";

        EvalFlow.Initiator flow = new EvalFlow.Initiator(titreArticle, dateArticle,
                c1, c2, c3, c4, forme, comment, b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTx = future.get();

        // We check the recorded transaction in both vaults.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            SignedTransaction recordedTx = node.getServices().getValidatedTransactions().getTransaction(signedTx.getId());
            List<TransactionState<ContractState>> txOutputs = recordedTx.getTx().getOutputs();
            assert (txOutputs.size() == 1);

            EvalState recordedState = (EvalState) txOutputs.get(0).getData();
            assertEquals(recordedState.getTitreArticle(), titreArticle);
            assertEquals(recordedState.getTeamAcademy(), a.getInfo().getLegalIdentities().get(0));
            assertEquals(recordedState.getStudent(), b.getInfo().getLegalIdentities().get(0));
        }
    }

    @Test
    public void flowRecordsTheCorrectIOUInBothPartiesVaults() throws Exception {
        String titreArticle= "test";
        String c1= "test";
        String c2= "test";
        String c3= "test";
        String c4= "test";
        String forme= "test";
        String comment= "test";
        String dateArticle= "test";


        EvalFlow.Initiator flow = new EvalFlow.Initiator("test", "test", "test","test","test",
                "test","test","test",b.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        future.get();

        // We check the recorded IOU in both vaults.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            node.transaction(() -> {
                List<StateAndRef<EvalState>> ious = node.getServices().getVaultService().queryBy(EvalState.class).getStates();
                assertEquals(1, ious.size());
                EvalState recordedState = ious.get(0).getState().getData();
                assertEquals(recordedState.getTitreArticle(), titreArticle);
                assertEquals(recordedState.getTeamAcademy(), a.getInfo().getLegalIdentities().get(0));
                assertEquals(recordedState.getStudent(), b.getInfo().getLegalIdentities().get(0));
                return null;
            });
        }
    }
}
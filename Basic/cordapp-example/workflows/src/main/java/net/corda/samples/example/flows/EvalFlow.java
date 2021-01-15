package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.*;
import net.corda.samples.example.contracts.EvalContract;
import net.corda.samples.example.states.EdupointTokenState;
import net.corda.samples.example.states.EvalState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.util.Arrays;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the eval encapsulated
 * within an [EvalState].
 *
 * In our simple example, the [Acceptor] always accepts a valid eval.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
public class EvalFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final String titreArticle;
        private final String dateArticle;
        private final String c1;
        private final String c2;
        private final String c3;
        private final String c4;
        private final String forme;
        private final String comment;
        private final String edupoint;
        private final Party student;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new evaluation.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step EDUPOINT = new ProgressTracker.Step("Move edupoint from Team academy to student!");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step GATHERING_SIGS = new Step("Gathering the student's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
        // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
        // function.
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                EDUPOINT,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public Initiator(String titreArticle, String dateArticle,
                         String c1,
                         String c2,
                         String c3,
                         String c4,
                         String forme,
                         String comment, Party student) {
            this.titreArticle = titreArticle;
            this.dateArticle = dateArticle;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
            this.forme = forme;
            this.comment = comment;
            this.edupoint = "0";
            this.student = student;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // Obtain a reference to a notary we wish to use.
            /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
            // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

            // Stage 1.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // Generate an unsigned transaction.
            Party me = getOurIdentity();




            /* ================================================================================================================================
             * Get remind available edupoint
             * ================================================================================================================================*/
            progressTracker.setCurrentStep(EDUPOINT);

            //get house states on ledger with uuid as input tokenId
            StateAndRef<EdupointTokenState> stateAndRef = getServiceHub().getVaultService().
                    queryBy(EdupointTokenState.class).getStates().stream()
                    .filter(sf->sf.getState().getData().getSymbol().equals("edupoint")).findAny()
                    .orElseThrow(()-> new IllegalArgumentException("StockState edupoint not found from vault"));

            //get the RealEstateEvolvableTokenType object
            EdupointTokenState evolvableTokenType = stateAndRef.getState().getData();

            //get the pointer pointer to the house
            TokenPointer<EdupointTokenState> tokenPointer = evolvableTokenType.toPointer(EdupointTokenState.class);

            Amount<TokenType> amount = QueryUtilities.tokenBalance(getServiceHub().getVaultService(), tokenPointer);
            double test = Integer.valueOf((int) amount.getQuantity());
            int edupointSt = (int) Math.round((test * 5)/100);

            /* =================================================================================================================================
             * ================================================================================================================================*/


            /* =================================================================================================================================
             * Move edupoint from Team academy to student
             * ================================================================================================================================*/

            //specify how much amount to transfer to which student
            Amount<TokenType> amountP = new Amount(edupointSt, tokenPointer);

            //use built in flow to move fungible tokens to student
            subFlow(new MoveFungibleTokens(amountP, student));





            /* =================================================================================================================================
             * Persist evaluation for student
             * ================================================================================================================================*/

            EvalState evalState = new EvalState(titreArticle, me, dateArticle, c1, c2, c3, c4, forme, comment, String.valueOf(edupointSt) , student, new UniqueIdentifier());
            final Command<EvalContract.Commands.Create> txCommand = new Command<>(
                    new EvalContract.Commands.Create(),
                    Arrays.asList(evalState.getTeamAcademy().getOwningKey(), evalState.getStudent().getOwningKey()));
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(evalState, EvalContract.ID)
                    .addCommand(txCommand);

            // Stage 2.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession otherPartySession = initiateFlow(student);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an eval transaction.", output instanceof EvalState);
                        return null;
                    });
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();

            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }
}

package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.states.EdupointTokenState;

public class QueryTokens {
    @InitiatingFlow
    @StartableByRPC
    public static class GetTokenBalance extends FlowLogic<String> {
        private final ProgressTracker progressTracker = new ProgressTracker();
        private final String symbol;


        public GetTokenBalance(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            //get house states on ledger with uuid as input tokenId
            StateAndRef<EdupointTokenState> stateAndRef = getServiceHub().getVaultService().
                    queryBy(EdupointTokenState.class).getStates().stream()
                    .filter(sf->sf.getState().getData().getSymbol().equals(symbol)).findAny()
                    .orElseThrow(()-> new IllegalArgumentException("StockState symbol=\""+symbol+"\" not found from vault"));

            //get the RealEstateEvolvableTokenType object
            EdupointTokenState evolvableTokenType = stateAndRef.getState().getData();

            //get the pointer pointer to the house
            TokenPointer<EdupointTokenState> tokenPointer = evolvableTokenType.toPointer(EdupointTokenState.class);

            Amount<TokenType> amount = QueryUtilities.tokenBalance(getServiceHub().getVaultService(), tokenPointer);

                return String.valueOf(amount.getQuantity());
        }
    }

}


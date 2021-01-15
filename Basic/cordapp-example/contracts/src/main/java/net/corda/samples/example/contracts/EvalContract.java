package net.corda.samples.example.contracts;

import net.corda.samples.example.states.EvalState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * A implementation of a basic smart contracts in Corda.
 *
 * This contracts enforces rules regarding the creation of a valid [EvalState], which in turn encapsulates an [EVALUATION].
 *
 * For a new [EVALUATION] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output states: the new [EVALUATION].
 * - An Create() command with the public keys of both the TA and the student.
 *
 * All contracts must sub-class the [Contract] interface.
 */
public class EvalContract implements Contract {
    public static final String ID = "net.corda.samples.example.contracts.EvalContract";

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands.Create> command = requireSingleCommand(tx.getCommands(), Commands.Create.class);
        requireThat(require -> {
            // Generic constraints around the EVAL transaction.
            require.using("No inputs should be consumed when issuing an eval.",
                    tx.getInputs().isEmpty());
            require.using("Only one output states should be created.",
                    tx.getOutputs().size() == 1);
            final EvalState out = tx.outputsOfType(EvalState.class).get(0);
            require.using("The TA and the student cannot be the same entity.",
                    !out.getTeamAcademy().equals(out.getStudent()));
            require.using("All of the participants must be signers.",
                    command.getSigners().containsAll(out.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));

            // eval-specific constraints.
            //require.using("The IOU's value must be non-negative.",
                    //out.getEvaluation() > 0);

            return null;
        });
    }

    /**
     * This contracts only implements one command, Create.
     */
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}
package net.corda.samples.example.contracts;

import net.corda.samples.example.states.EvalState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class ContractTests {
    static private final MockServices ledgerServices = new MockServices();
    static private final TestIdentity megaCorp = new TestIdentity(new CordaX500Name("MegaCorp", "London", "GB"));
    static private final TestIdentity miniCorp = new TestIdentity(new CordaX500Name("MiniCorp", "London", "GB"));
    static private final String titreArticle = "test";
    static private final String dateArticle = "test";
    static private final String c1 = "test";
    static private final String c2 = "test";
    static private final String c3 = "test";
    static private final String c4 = "test";
    static private final String forme = "test";
    static private final String comment = "test";
    static private final String edupoint = "test";


    @Test
    public void transactionMustIncludeCreateCommand() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.fails();
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new EvalContract.Commands.Create());
                tx.verifies();
                return null;
            });
            return null;
        }));
    }

    @Test
    public void transactionMustHaveNoInputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.input(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new EvalContract.Commands.Create());
                tx.failsWith("No inputs should be consumed when issuing an IOU.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void transactionMustHaveOneOutput() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new EvalContract.Commands.Create());
                tx.failsWith("Only one output states should be created.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void lenderMustSignTransaction() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(miniCorp.getPublicKey(), new EvalContract.Commands.Create());
                tx.failsWith("All of the participants must be signers.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void borrowerMustSignTransaction() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(EvalContract.ID, new EvalState(titreArticle, miniCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(megaCorp.getPublicKey(), new EvalContract.Commands.Create());
                tx.failsWith("All of the participants must be signers.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void lenderIsNotBorrower() {
        final TestIdentity megaCorpDupe = new TestIdentity(megaCorp.getName(), megaCorp.getKeyPair());
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(EvalContract.ID, new EvalState(titreArticle, megaCorp.getParty(), dateArticle, c1, c2, c3, c4, forme, comment, edupoint, megaCorpDupe.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new EvalContract.Commands.Create());
                tx.failsWith("The lender and the borrower cannot be the same entity.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void cannotCreateNegativeValueIOUs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new EvalContract.Commands.Create());
                tx.failsWith("The IOU's value must be non-negative.");
                return null;
            });
            return null;
        }));
    }
}
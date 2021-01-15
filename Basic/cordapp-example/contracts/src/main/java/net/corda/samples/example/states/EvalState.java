package net.corda.samples.example.states;

import net.corda.samples.example.contracts.EvalContract;
import net.corda.samples.example.schema.IOUSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;

/**
 * The states object recording evaluation.
 *
 * A states must implement [ContractState] or one of its descendants.
 */
@BelongsToContract(EvalContract.class)
public class EvalState implements LinearState, QueryableState {
    private final String titreArticle;
    private final Party teamAcademy;
    private final String dateArticle;
    private final String c1;
    private final String c2;
    private final String c3;
    private final String c4;
    private final String forme;
    private final String comment;
    private final String edupoint;
    private final Party student;
    private final UniqueIdentifier linearId;

    /**
     * @param titreArticle the value of the evaluation.
     * @param teamAcademy
     * @param dateArticle
     * @param c1
     * @param c2
     * @param c3
     * @param c4
     * @param forme
     * @param comment
     * @param edupoint
     * @param student receiving and approving the eval.
     */
    public EvalState(String titreArticle,
                     Party teamAcademy,
                     String dateArticle,
                     String c1,
                     String c2,
                     String c3,
                     String c4,
                     String forme,
                     String comment,
                     String edupoint,
                     Party student,
                     UniqueIdentifier linearId)
    {
        this.titreArticle = titreArticle;
        this.teamAcademy = teamAcademy;
        this.dateArticle = dateArticle;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.forme = forme;
        this.comment = comment;
        this.edupoint = edupoint;
        this.student = student;
        this.linearId = linearId;
    }

    public String getTitreArticle() { return titreArticle; }
    public Party getTeamAcademy() { return teamAcademy; }
    public Party getStudent() { return student; }
    @Override public UniqueIdentifier getLinearId() { return linearId; }
    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList(teamAcademy, student);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IOUSchemaV1) {
            return new IOUSchemaV1.PersistentEVAL(
                    this.teamAcademy.getName().toString(),
                    this.student.getName().toString(),
                    this.titreArticle,
                    this.dateArticle,
                    this.c1,
                    this.c2,
                    this.c3,
                    this.c4,
                    this.forme,
                    this.comment,
                    this.edupoint, this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new IOUSchemaV1());
    }

    @Override
    public String toString() {
        return String.format("EvalState(titreArticle=%s," +
                "dateArticle=%s, c1=%s, c2=%s, c3=%s, c4=%s" +
                "forme=%s, comment=%s, edupoint=%s, teamAcademy=%s, student=%s, linearId=%s)",
                titreArticle, dateArticle, c1, c2, c3, c4, forme, comment, edupoint, teamAcademy, student, linearId);
    }
}
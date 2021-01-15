package net.corda.samples.example.schema;

import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;
//4.6 changes
import org.hibernate.annotations.Type;
import javax.annotation.Nullable;

/**
 * An EvalState schema.
 */
public class IOUSchemaV1 extends MappedSchema {
    public IOUSchemaV1() {
        super(IOUSchema.class, 1, Arrays.asList(PersistentEVAL.class));
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "iou.changelog-master";
    }

    @Entity
    @Table(name = "eval_states")
    public static class PersistentEVAL extends PersistentState {
        @Column(name = "teamAcademy") private final String teamAcademy;
        @Column(name = "student") private final String student;
        @Column(name = "titreArticle") private final String titreArticle;

        @Column(name = "dateArticle") private final String dateArticle;
        @Column(name = "c1") private final String c1;
        @Column(name = "c2") private final String c2;
        @Column(name = "c3") private final String c3;
        @Column(name = "c4") private final String c4;
        @Column(name = "forme") private final String forme;
        @Column(name = "comment") private final String comment;
        @Column(name = "edupoint") private final String edupoint;



        @Column(name = "linear_id") @Type (type = "uuid-char") private final UUID linearId;


        public PersistentEVAL(String teamAcademy, String student, String titreArticle, String dateArticle, String c1, String c2, String c3, String c4, String forme, String comment, String edupoint, UUID linearId) {
            this.teamAcademy = teamAcademy;
            this.student = student;
            this.titreArticle = titreArticle;
            this.dateArticle = dateArticle;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
            this.forme = forme;
            this.comment = comment;
            this.edupoint = edupoint;
            this.linearId = linearId;
        }

        // Default constructor required by hibernate.
        public PersistentEVAL() {
            this.teamAcademy = null;
            this.student = null;
            this.titreArticle = null;
            this.dateArticle = null;
            this.c1 = null;
            this.c2 = null;
            this.c3 = null;
            this.c4 = null;
            this.forme = null;
            this.comment = null;
            this.edupoint = null;
            this.linearId = null;
        }

        public String getTeamAcademy() {
            return teamAcademy;
        }

        public String getStudent() {
            return student;
        }

        public String getTitreArticle() {
            return titreArticle;
        }

        public UUID getId() {
            return linearId;
        }

        public String getDateArticle() {
            return dateArticle;
        }

        public String getC1() {
            return c1;
        }

        public String getC2() {
            return c2;
        }

        public String getC3() {
            return c3;
        }

        public String getC4() {
            return c4;
        }

        public String getForme() {
            return forme;
        }

        public String getComment() {
            return comment;
        }

        public String getEdupoint() {
            return edupoint;
        }
    }
}
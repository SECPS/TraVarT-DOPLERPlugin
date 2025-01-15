package edu.kit.dopler.transformation;

import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.EquivalenceConstraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;
import edu.kit.dopler.injection.Injector;
import edu.kit.dopler.transformation.exceptions.UnexpectedTypeException;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.DnfToTreeConverter;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.TreeToDnfConverter;
import edu.kit.dopler.transformation.feature.to.decision.constraint.dnf.UnwantedConstraintsReplacer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class DnfTests {

    private static final LiteralConstraint A = new LiteralConstraint("A");
    private static final LiteralConstraint B = new LiteralConstraint("B");
    private static final LiteralConstraint C = new LiteralConstraint("C");
    private static final LiteralConstraint D = new LiteralConstraint("D");
    private static final LiteralConstraint E = new LiteralConstraint("E");
    private static final LiteralConstraint G = new LiteralConstraint("G");
    private static final LiteralConstraint O = new LiteralConstraint("O");

    private final TreeToDnfConverter treeToDnfConverter;
    private final UnwantedConstraintsReplacer replacer;
    private final DnfToTreeConverter dnfToTreeConverter;

    DnfTests() {
        Injector injector = new Injector();
        treeToDnfConverter = injector.getInstance(TreeToDnfConverter.class);
        replacer = injector.getInstance(UnwantedConstraintsReplacer.class);
        dnfToTreeConverter = injector.getInstance(DnfToTreeConverter.class);
    }

    @Test
    void toDnf1() {
        Constraint given = new NotConstraint(new NotConstraint(new NotConstraint(A)));
        assertDnf("!A", given);
    }

    @Test
    void toDnf2() {
        Constraint given = new NotConstraint(new OrConstraint(A, B));
        assertDnf("!A&!B", given);
    }

    @Test
    void toDnf3() {
        Constraint given = new NotConstraint(new AndConstraint(A, B));
        assertDnf("!A|!B", given);
    }

    @Test
    void toDnf4() {
        Constraint given = new AndConstraint(A, new OrConstraint(B, C));
        assertDnf("(A&B)|(A&C)", given);
    }

    @Test
    void toDnf5() {
        Constraint given = new AndConstraint(new OrConstraint(A, B), C);
        assertDnf("A&C | B&C", given);
    }

    @Test
    void toDnf6() {
        Constraint given = new ImplicationConstraint(new ImplicationConstraint(A, B), new ImplicationConstraint(C, D));
        assertDnf("!C | D | (!B & A)", given);
    }

    @Test
    void toDnf7() {
        Constraint given = new NotConstraint(new EquivalenceConstraint(A, B));
        assertDnf("(!B&A) | (!A&B)", given);
    }

    @Test
    void toDnf8() {
        Constraint given = new AndConstraint(new EquivalenceConstraint(A, B), new EquivalenceConstraint(B, C));
        assertDnf("(A & B & C) | (!A & !B & !C)", given);
    }

    @Test
    void toDnf9() {
        Constraint given = new NotConstraint(
                new EquivalenceConstraint(new ImplicationConstraint(new AndConstraint(A, B), B),
                        new OrConstraint(C, A)));
        assertDnf("!A & !C", given);
    }

    @Test
    void toDnf10() {
        Constraint given = new AndConstraint(new OrConstraint(
                new ImplicationConstraint(new NotConstraint(A), new AndConstraint(new NotConstraint(B), C)),
                new NotConstraint(G)),
                new NotConstraint(new EquivalenceConstraint(new ImplicationConstraint(D, E), new OrConstraint(O, G))));
        String expected = "(!D & !G & !O) | (!G & !O & E) | (!E & !G & D & O) | (!E & A & D & O) | (!E & A & D & G) " +
                "| (!B & !E & C & D & O) | (!B & !E & C & D & G)";
        assertDnf(expected, given);
    }

    @Test
    void testReplace1() {
        Constraint givenConstraint = new ImplicationConstraint(A, B);
        Constraint real = replacer.replaceUnwantedConstraints(givenConstraint);
        Constraint expected = new OrConstraint(new NotConstraint(A), B);
        Assertions.assertEquals(expected, real);
    }

    @Test
    void testReplace2() {
        //A <=> B
        Constraint givenConstraint = new EquivalenceConstraint(A, B);
        Constraint real = replacer.replaceUnwantedConstraints(givenConstraint);
        Assertions.assertEquals("!A | B & !B | A", real.toString());
    }

    @Test
    void testReplace3() {
        //(A => B) => (C => D)
        Constraint givenConstraint =
                new ImplicationConstraint(new ImplicationConstraint(A, B), new ImplicationConstraint(C, D));
        Constraint real = replacer.replaceUnwantedConstraints(givenConstraint);
        Constraint expected = new OrConstraint((new NotConstraint(new OrConstraint(new NotConstraint(A), B))),
                new OrConstraint(new NotConstraint(C), D));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void testReplace4() {
        //(A => B) => (C => D)
        Constraint givenConstraint =
                new AndConstraint(new EquivalenceConstraint(A, B), new EquivalenceConstraint(B, C));
        Constraint real = replacer.replaceUnwantedConstraints(givenConstraint);
        Constraint expected = new AndConstraint(
                new AndConstraint(new OrConstraint(new NotConstraint(A), B), new OrConstraint(new NotConstraint(B), A)),
                new AndConstraint(new OrConstraint(new NotConstraint(B), C),
                        new OrConstraint(new NotConstraint(C), B)));

        Assertions.assertEquals(expected, real);
    }

    @Test
    void testReplace5() {
        //(A => B) & (A => B)
        Constraint given = new AndConstraint(new ParenthesisConstraint(new ImplicationConstraint(A, B)),
                new ParenthesisConstraint(new ImplicationConstraint(A, C)));
        Assertions.assertEquals("((!(A) | B) & (!(A) | C))",
                constraintToString(replacer.replaceUnwantedConstraints(given)));
    }

    @Test
    void testReplace6() {
        //(Changeover2 => Rocker1_1) & (Changeover2 => Rocker1_2)
        Constraint given = new AndConstraint(
                new ImplicationConstraint(new LiteralConstraint("Changeover2"), new LiteralConstraint("Rocker1_1")),
                new ImplicationConstraint(new LiteralConstraint("Changeover2"), new LiteralConstraint("Rocker1_2")));
        Assertions.assertEquals("((!(Changeover2) | Rocker1_1) & (!(Changeover2) | Rocker1_2))",
                constraintToString(replacer.replaceUnwantedConstraints(given)));
    }

    private void assertDnf(String expected, Constraint given) {
        Constraint dnf = dnfToTreeConverter.createDnfFromList(treeToDnfConverter.convertToDnf(given));

        String realString = constraintToString(dnf);

        Function<String, String> sanitise =
                s -> s.replace(" ", "").replace("|", " | ").replace("(", "").replace(")", "");

        String message = String.format("""

                        Given:
                        %s

                        Converted to DNF:
                        %s

                        Expected:
                        %s

                        """, constraintToString(given).replace("!", "~"), dnfToString(dnf).replace("!", "~"),
                expected.replace("!", "~"));

        Assertions.assertEquals(sanitise.apply(expected), sanitise.apply(realString), message);
    }

    private String dnfToString(Constraint dnf) {
        String dnfAsString = constraintToStringDnf(dnf);
        return "(" + dnfAsString.replace(" | ", ") | (") + ")";
    }

    private String constraintToStringDnf(Constraint constraint) {
        return switch (constraint) {
            case AndConstraint andConstraint -> String.format("%s & %s", constraintToStringDnf(andConstraint.getLeft()),
                    constraintToStringDnf(andConstraint.getRight()));
            case OrConstraint orConstraint -> String.format("%s | %s", constraintToStringDnf(orConstraint.getLeft()),
                    constraintToStringDnf(orConstraint.getRight()));
            case NotConstraint notConstraint -> String.format("!%s", constraintToStringDnf(notConstraint.getContent()));
            case LiteralConstraint ignored -> constraint.toString();
            case ExpressionConstraint ignored -> constraint.toString();
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }

    private String constraintToString(Constraint constraint) {
        return switch (constraint) {
            case AndConstraint andConstraint -> String.format("(%s & %s)", constraintToString(andConstraint.getLeft()),
                    constraintToString(andConstraint.getRight()));
            case OrConstraint orConstraint -> String.format("(%s | %s)", constraintToString(orConstraint.getLeft()),
                    constraintToString(orConstraint.getRight()));
            case ImplicationConstraint implicationConstraint ->
                    String.format("(%s -> %s)", constraintToString(implicationConstraint.getLeft()),
                            constraintToString(implicationConstraint.getRight()));
            case EquivalenceConstraint equivalenceConstraint ->
                    String.format("(%s <-> %s)", constraintToString(equivalenceConstraint.getLeft()),
                            constraintToString(equivalenceConstraint.getRight()));
            case NotConstraint notConstraint -> String.format("!(%s)", constraintToString(notConstraint.getContent()));
            case LiteralConstraint ignored -> constraint.toString();
            case ExpressionConstraint ignored -> constraint.toString();
            case null, default -> throw new UnexpectedTypeException(constraint);
        };
    }
}
package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf;

import at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.constraint.dnf.*;
import de.vill.model.constraint.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class DnfConverterTest {

    private static final LiteralConstraint A = new LiteralConstraint("A");
    private static final LiteralConstraint B = new LiteralConstraint("B");
    private static final LiteralConstraint C = new LiteralConstraint("C");
    private static final LiteralConstraint D = new LiteralConstraint("D");
    private static final LiteralConstraint E = new LiteralConstraint("E");
    private static final LiteralConstraint G = new LiteralConstraint("G");
    private static final LiteralConstraint O = new LiteralConstraint("O");

    private final DnfConverter dnfConverter;
    private final UnwantedConstraintsReplacer replacer;
    private final DnfSimplifier dnfSimplifier;

    DnfConverterTest() {
        dnfConverter = new DnfConverterImpl();
        replacer = new UnwantedConstraintsReplacerImpl();
        dnfSimplifier = new DnfSimplifierImpl();
    }

    @Test
    void toDnf1() {
        Constraint given = new NotConstraint(new NotConstraint(A));
        Constraint real = dnfSimplifier.createDnfFromList(dnfConverter.convertToDnf(given));
        Constraint expected = A;
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf2() {
        Constraint given = new NotConstraint(new OrConstraint(A, B));
        Constraint real = dnfSimplifier.createDnfFromList(dnfConverter.convertToDnf(given));
        Constraint expected = new AndConstraint(new NotConstraint(A), new NotConstraint(B));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf3() {
        Constraint given = new NotConstraint(new AndConstraint(A, B));
        Constraint real = dnfSimplifier.createDnfFromList(dnfConverter.convertToDnf(given));
        Constraint expected = new OrConstraint(new NotConstraint(A), new NotConstraint(B));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf4() {
        Constraint given = new AndConstraint(A, new OrConstraint(B, C));
        assertDnfs("(A&B)|(A&C)", given);
    }

    @Test
    void toDnf5() {
        Constraint given = new AndConstraint(new OrConstraint(A, B), C);
        assertDnfs("A&C | B&C", given);
    }

    @Test
    void toDnf6() {
        Constraint given = new ImplicationConstraint(new ImplicationConstraint(A, B), new ImplicationConstraint(C, D));
        assertDnfs("!C | D | (!B & A)", given);
    }

    @Test
    void toDnf7() {
        Constraint given = new NotConstraint(new EquivalenceConstraint(A, B));
        assertDnfs("(!B&A) | (!A&B)", given);
    }

    @Test
    void toDnf8() {
        Constraint given = new AndConstraint(new EquivalenceConstraint(A, B), new EquivalenceConstraint(B, C));
        assertDnfs("(A & B & C) | (!A & !B & !C)", given);
    }

    @Test
    void toDnf9() {
        Constraint given = new NotConstraint(
                new EquivalenceConstraint(new ImplicationConstraint(new AndConstraint(A, B), B),
                        new OrConstraint(C, A)));
        assertDnfs("!A & !C", given);
    }

    @Test
    void toDnf10() {
        Constraint given = new AndConstraint(new OrConstraint(
                new ImplicationConstraint(new NotConstraint(A), new AndConstraint(new NotConstraint(B), C)),
                new NotConstraint(G)),
                new NotConstraint(new EquivalenceConstraint(new ImplicationConstraint(D, E), new OrConstraint(O, G))));
        String expected = "(!D & !G & !O) | (!G & !O & E) | (!E & !G & D & O) | (!E & A & D & O) | (!E & A & D & G) " +
                "| (!B & !E & C & D & O) | (!B & !E & C & D & G)";
        assertDnfs(expected, given);
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

    private void assertDnfs(String expected, Constraint given) {

        Constraint dnf = dnfSimplifier.createDnfFromList(dnfConverter.convertToDnf(given));
        ;
        String realString = constraintToString(dnf);

        Function<String, String> sanitise =
                s -> s.replace(" ", "").replace("|", " | ").replace("(", "").replace(")", "");

        String message = String.format("\nGiven:\n%s\n\nConverted to DNF:\n%s\n\nExpected:\n%s\n\n",
                constraintToString(given).replace("!", "~"), dnfToString(dnf).replace("!", "~"),
                expected.replace("!", "~"));

        Assertions.assertEquals(sanitise.apply(expected), sanitise.apply(realString), message);
    }

    String dnfToString(Constraint dnf) {
        String dnfAsString = constraintToStringDnf(dnf);
        return "(" + dnfAsString.replace(" | ", ") | (") + ")";
    }

    String constraintToStringDnf(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            return String.format("%s & %s", constraintToStringDnf(andConstraint.getLeft()),
                    constraintToStringDnf(andConstraint.getRight()));
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;
            return String.format("%s | %s", constraintToStringDnf(orConstraint.getLeft()),
                    constraintToStringDnf(orConstraint.getRight()));
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            return String.format("!%s", constraintToStringDnf(notConstraint.getContent()));
        } else if (constraint instanceof LiteralConstraint) {
            return constraint.toString();
        } else if (constraint instanceof ExpressionConstraint) {
            return constraint.toString();
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }

    String constraintToString(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            return String.format("(%s & %s)", constraintToString(andConstraint.getLeft()),
                    constraintToString(andConstraint.getRight()));
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;
            return String.format("(%s | %s)", constraintToString(orConstraint.getLeft()),
                    constraintToString(orConstraint.getRight()));
        } else if (constraint instanceof ImplicationConstraint) {
            ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
            return String.format("(%s -> %s)", constraintToString(implicationConstraint.getLeft()),
                    constraintToString(implicationConstraint.getRight()));
        } else if (constraint instanceof EquivalenceConstraint) {
            EquivalenceConstraint equivalenceConstraint = (EquivalenceConstraint) constraint;
            return String.format("(%s <-> %s)", constraintToString(equivalenceConstraint.getLeft()),
                    constraintToString(equivalenceConstraint.getRight()));
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            return String.format("!(%s)", constraintToString(notConstraint.getContent()));
        } else if (constraint instanceof LiteralConstraint) {
            return constraint.toString();
        } else if (constraint instanceof ExpressionConstraint) {
            return constraint.toString();
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }
}
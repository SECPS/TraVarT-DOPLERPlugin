package at.jku.cps.travart.dopler.transformation.oneway.feature.to.decision.dnf;

import de.vill.model.constraint.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class ToDnfConverterTest {

    private static final LiteralConstraint A = new LiteralConstraint("A");
    private static final LiteralConstraint B = new LiteralConstraint("B");
    private static final LiteralConstraint C = new LiteralConstraint("C");
    private static final LiteralConstraint D = new LiteralConstraint("D");
    private static final LiteralConstraint E = new LiteralConstraint("E");
    private static final LiteralConstraint F = new LiteralConstraint("F");

    private final ToDnfConverter toDnfConverter;
    private final UnwantedConstraintsReplacer replacer;

    ToDnfConverterTest() {
        toDnfConverter = new ToDnfConverter();
        replacer = new UnwantedConstraintsReplacer();
    }

    @Test
    void toDnf1() {
        Constraint given = new NotConstraint(new NotConstraint(A));
        Constraint real = toDnfConverter.convertToDnf(given);
        Constraint expected = A;
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf2() {
        Constraint given = new NotConstraint(new OrConstraint(A, B));
        Constraint real = toDnfConverter.convertToDnf(given);
        Constraint expected = new AndConstraint(new NotConstraint(A), new NotConstraint(B));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf3() {
        Constraint given = new NotConstraint(new AndConstraint(A, B));
        Constraint real = toDnfConverter.convertToDnf(given);
        Constraint expected = new OrConstraint(new NotConstraint(A), new NotConstraint(B));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf4() {
        Constraint given = new AndConstraint(A, new OrConstraint(B, C));
        Constraint real = toDnfConverter.convertToDnf(given);
        Constraint expected = new OrConstraint(new AndConstraint(A, B), new AndConstraint(A, C));
        Assertions.assertEquals(expected, real);
    }

    @Test
    void toDnf5() {
        Constraint given = new AndConstraint(new OrConstraint(A, B), C);
        Constraint real = toDnfConverter.convertToDnf(given);
        assertDnfs("A&C | B&C", real);
    }

    @Test
    void toDnf6() {
        Constraint given = new ImplicationConstraint(new ImplicationConstraint(A, B), new ImplicationConstraint(C, D));
        Constraint real = toDnfConverter.convertToDnf(given);
        assertDnfs("(A & !B) | !C | D", real);
    }

    @Test
    void toDnf7() {
        Constraint given = new NotConstraint(new EquivalenceConstraint(A, B));

        Constraint real = toDnfConverter.convertToDnf(given);
        assertDnfs("(A&!B) | (B&!A)", real);
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

    private void assertDnfs(String expected, Constraint real) {
        String realString = constraintToString(real);

        Function<String, String> sanitise =
                s -> s.replace(" ", "").replace("|", " | ").replace("(", "").replace(")", "");

        Assertions.assertEquals(sanitise.apply(expected), sanitise.apply(realString));
    }

    String constraintToString(Constraint constraint) {
        if (constraint instanceof AndConstraint) {
            AndConstraint andConstraint = (AndConstraint) constraint;
            return String.format("%s & %s", constraintToString(andConstraint.getLeft()),
                    constraintToString(andConstraint.getRight()));
        } else if (constraint instanceof OrConstraint) {
            OrConstraint orConstraint = (OrConstraint) constraint;
            return String.format("%s | %s", constraintToString(orConstraint.getLeft()),
                    constraintToString(orConstraint.getRight()));
        } else if (constraint instanceof NotConstraint) {
            NotConstraint notConstraint = (NotConstraint) constraint;
            return String.format("!%s", constraintToString(notConstraint.getContent()));
        } else if (constraint instanceof LiteralConstraint) {
            return constraint.toString();
        } else if (constraint instanceof ExpressionConstraint) {
            return constraint.toString();
        } else {
            throw new RuntimeException("Unexpected constraint");
        }
    }
}
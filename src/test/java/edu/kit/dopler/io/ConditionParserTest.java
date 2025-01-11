package edu.kit.dopler.io;

import at.jku.cps.travart.core.exception.NotSupportedVariabilityTypeException;
import edu.kit.dopler.exceptions.ParserException;
import edu.kit.dopler.io.parser.ConditionParser;
import edu.kit.dopler.model.Dopler;
import edu.kit.dopler.model.IExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ConditionParserTest {

    private static final String DATA = """
            ID;Question;Type;Range;Cardinality;Constraint/Rule;Visible/relevant if
            A;Which Topping?;Enumeration;a | b | c | d;1:3;;true
            B;B?;Boolean;true | false;;;true
            """;
    private static final Path PATH = Paths.get("src", "test", "java", "edu", "kit", "dopler", "io", "data.csv");

    private ConditionParser conditionParser;

    @BeforeEach
    void setUp() throws IOException, NotSupportedVariabilityTypeException {
        Files.writeString(PATH, DATA);
        Dopler dopler = new DecisionModelReader().read(PATH);
        conditionParser = new ConditionParser(dopler);
    }

    @Test
    void test1() throws ParserException {
        assertParsing("!A.c", "!(getValue(A) = c)");
    }

    @Test
    void test2() throws ParserException {
        assertParsing("!!A.c", "!!(getValue(A) = c)");
    }

    @Test
    void test3() throws ParserException {
        assertParsing("((A.c))", "(getValue(A) = c)");
    }

    @Test
    void test4() throws ParserException {
        assertParsing("!(A.a || A.b)", "!((getValue(A) = a) || (getValue(A) = b))");
    }

    @Test
    void test5() throws ParserException {
        assertParsing("!A.a && A.c", "(!(getValue(A) = a) && (getValue(A) = c))");
    }

    @Test
    void test6() throws ParserException {
        assertParsing("!A.a && A.c", "(!(getValue(A) = a) && (getValue(A) = c))");
    }

    @Test
    void test7() throws ParserException {
        assertParsing("!(A.a) && A.c", "(!(getValue(A) = a) && (getValue(A) = c))");
    }

    @Test
    void test8() throws ParserException {
        assertParsing("!(A.a && A.b) || A.c", "(!((getValue(A) = a) && (getValue(A) = b)) || (getValue(A) = c))");
    }

    @Test
    void test9() throws ParserException {
        assertParsing("!(A.a && A.b && A.c) || A.d",
                "(!((getValue(A) = a) && ((getValue(A) = b) && (getValue(A) = c))) || (getValue(A) = d))");
    }

    @Test
    void test10() throws ParserException {
        assertParsing("(B)", "(getValue(B) = true)");
    }

    @Test
    void test11() throws ParserException {
        assertParsing("!B", "!(getValue(B) = true)");
    }

    @Test
    void test12() throws ParserException {
        assertParsing("B && B && B", "((getValue(B) = true) && ((getValue(B) = true) && (getValue(B) = true)))");
    }

    @Test
    void test13() throws ParserException {
        assertParsing("A.a && A.a && A.a", "((getValue(A) = a) && ((getValue(A) = a) && (getValue(A) = a)))");
    }

    @Test
    void test14() throws ParserException {
        assertParsing("(!(B && !B && !B) || !!!!B)",
                "(!((getValue(B) = true) && (!(getValue(B) = true) && !(getValue(B) = true))) || !!!!(getValue(B) = " +
                        "true))");
    }

    @Test
    void test15() throws ParserException {
        assertParsing("getValue(B) = true", "(getValue(B) = true)");
    }

    @Test
    void test16() throws ParserException {
        assertParsing("(getValue(B) = true)", "(getValue(B) = true)");
    }

    @Test
    void test17() throws ParserException {
        assertParsing("!(getValue(B) = true)", "!(getValue(B) = true)");
    }

    @Test
    void test18() throws ParserException {
        assertParsing("getValue(A) = a && getValue(A) = a && getValue(A) = a", "((getValue(A) = a) && ((getValue(A) = a) && (getValue(A) = a)))");
    }

    private void assertParsing(String input, String expected) throws ParserException {
        IExpression iExpression = conditionParser.parse(input);
        Assertions.assertEquals(expected, iExpression.toString());
    }
}

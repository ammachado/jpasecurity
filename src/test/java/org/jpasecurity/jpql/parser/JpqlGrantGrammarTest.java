package org.jpasecurity.jpql.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class JpqlGrantGrammarTest {

    private final String source;

    public JpqlGrantGrammarTest(String source) {
        this.source = source;
    }

    @Test
    public void parseStatement() {
        try {
            if (source.startsWith("WHERE")) {
                JpqlParsingHelper.parseWhereClause(source);
            } else {
                JpqlParsingHelper.parseAccessRule(source);
            }
        } catch (Exception e) {
            fail("failed to parse jpql:\n" + source + "\n\n" + e.getMessage());
        }
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"GRANT CREATE ACCESS TO FieldAccessAnnotationTestBean bean WHERE 'creator' IN (CURRENT_ROLES)"},
                {"WHERE 'admin' IN CURRENT_ROLES"},
                {"WHERE EXISTS (SELECT contact FROM Contact contact WHERE this = contact AND (contact.owner = CURRENT_PRINCIPAL OR contact.owner = 'public'))"}
        });
    }
}

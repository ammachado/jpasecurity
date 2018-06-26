package org.jpasecurity.jpql.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ToStringVisitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ToStringVisitorTest.class);

    private final String source;

    public ToStringVisitorTest(String source) {
        this.source = source;
    }

    @Test
    public void toStringVisitor() {
        assertJpql(source);
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() throws IOException {
        final Collection<String> coll = Files.readAllLines(
                Paths.get("src", "test", "resources", "queries.txt"),
                StandardCharsets.UTF_8
        );

        final Collection<Object[]> params = new ArrayList<>();
        for (String s : coll) {
            params.add(new Object[]{s});
        }

        return params;
    }

    private static void assertJpql(String source) {
        JpqlParser.StatementContext statement = JpqlParsingHelper.parseQuery(source);
        final ToStringVisitor ToStringVisitor = new ToStringVisitor();
        String actual = ToStringVisitor.visit(statement).toString();
        LOG.debug("Source: {}", source);
        LOG.debug("Result: {}", actual);
        assertEquals("HQL", source, actual);
        ToStringVisitor.defaultResult().delete(0, ToStringVisitor.defaultResult().length());
    }
}

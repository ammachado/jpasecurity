package org.jpasecurity.jpql.parser;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JpqlParsingHelperTest {

    private final String source;

    public JpqlParsingHelperTest(String source) {
        this.source = source;
    }

    @Test
    public void parseStatement() {
        try {
            JpqlParsingHelper.parseQuery(source);
        } catch (Exception e) {
            final String message = e.getMessage();
            fail("failed to parse jpql:\n" + source + "\n\n" + message);
        }
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
}

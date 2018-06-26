/*
 * Copyright 2008 - 2016 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.jpql.compiler;

import static org.junit.Assert.assertEquals;

import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class JpqlQueryRewriterTest {

    private static final Logger LOG = LoggerFactory.getLogger(JpqlQueryRewriterTest.class);

    private final String source;
    private final String expected;

    public JpqlQueryRewriterTest(String source, String expected) {
        this.source = source;
        this.expected = expected;
    }

    @Test
    public void rewrite() {
        assertJpql(source, expected);
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Object[][] data() {
        return new Object[][]{
            {"GRANT CREATE ACCESS TO FieldAccessAnnotationTestBean bean WHERE 'creator' IN (CURRENT_ROLES)",
                "GRANT CREATE ACCESS TO FieldAccessAnnotationTestBean bean WHERE 'creator' IN (:CURRENT_ROLES)"},
            {"GRANT CREATE ACCESS TO FieldAccessAnnotationTestBean bean WHERE "
                + "(EXISTS (SELECT r FROM Roles r WHERE r.name = 'creator'))",
                    "GRANT CREATE ACCESS TO FieldAccessAnnotationTestBean bean WHERE "
                            + "EXISTS (SELECT r FROM Roles r WHERE r.name = 'creator')",}
        };
    }

    private static void assertJpql(String source, String expected) {
        JpqlParser.AccessRuleContext statement = JpqlParsingHelper.parseAccessRule(source);
        final JpqlQueryRewriter rewriter = new JpqlQueryRewriter();
        String actual = rewriter.visit(statement).toString();
        LOG.debug("Source  : {}", source);
        LOG.debug("Result  : {}", actual);
        LOG.debug("Expected: {}", expected);
        assertEquals("HQL", expected, actual);
        rewriter.clear();
    }
}

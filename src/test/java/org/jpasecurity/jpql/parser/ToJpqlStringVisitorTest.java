/*
 * Copyright 2008 Arne Limburg
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
package org.jpasecurity.jpql.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Arne Limburg */
public class ToJpqlStringVisitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ToJpqlStringVisitorTest.class);

    private ToJpqlStringVisitor toJpqlStringVisitor;

    @Test
    public void toStringVisitor() {
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                        + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                    + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                        + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                    + " (SELECT /* QUERY_OPTIMIZE_NOCACHE */ bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                        + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                    + " (SELECT /* IS_ACCESSIBLE_NODB */ bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                        + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                    + " (SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB */ bean"
                    + " FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                        + " (SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean WHERE NOT EXISTS"
                    + " (SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB IS_ACCESSIBLE_NOCACHE*/ bean"
                    + " FROM TestBean bean WHERE bean.id = :id)");
    }

    public void assertJpql(String expected, String source) {
        JpqlParser.StatementContext statement;
        try {
            statement = JpqlParsingHelper.parseQuery(source);
        } catch (Exception e) {
            fail("failed to parse jpql:\n\n" + source + "\n\n" + e.getMessage());
            return;
        }
        String actual = toJpqlStringVisitor.visit(statement).toString();
        LOG.debug("Expected: {}", expected);
        LOG.debug("Actual  : {}", actual);
        assertEquals("JPQL", expected, actual);
        toJpqlStringVisitor.defaultResult().delete(0, toJpqlStringVisitor.defaultResult().length());
    }

    @Before
    public void initializeVisitor() {
        toJpqlStringVisitor = new ToJpqlStringVisitor(new StringBuilder());
    }
}

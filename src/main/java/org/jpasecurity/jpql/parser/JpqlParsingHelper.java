/*
 * Copyright 2017 Arne Limburg
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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Objects;

public class JpqlParsingHelper {

    private JpqlParsingHelper() {
    }

    public static JpqlParser.StatementContext parseQuery(String statement) {
        return createParser(statement, false).statement();
    }

    public static JpqlParser.StatementContext parseQuery(String statement, boolean trace) {
        return createParser(statement, trace).statement();
    }

    public static JpqlParser.WhereClauseContext parseWhereClause(String whereClause) {
        return createParser(whereClause, false).whereClause();
    }

    public static JpqlParser.WhereClauseContext parseWhereClause(String whereClause, boolean trace) {
        return createParser(whereClause, trace).whereClause();
    }

    public static JpqlParser.AccessRuleContext parseAccessRule(String accessRule) {
        return parseAccessRule(accessRule, false);
    }

    public static JpqlParser.AccessRuleContext parseAccessRule(String accessRule, boolean trace) {
        return createParser(accessRule, trace).accessRule();
    }

    public static JpqlParser createParser(String statement) {
        return createParser(statement, false);
    }

    public static JpqlParser createParser(String statement, boolean trace) {
        final CodePointCharStream input =
                CharStreams.fromString(Objects.requireNonNull(statement, "The statement cannot be null"));
        final JpqlLexer lexer = new JpqlLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final JpqlParser parser = new JpqlParser(tokens);
        parser.setTrace(trace);
        //parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }
}

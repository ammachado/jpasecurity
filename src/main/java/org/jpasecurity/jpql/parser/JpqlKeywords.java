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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JpqlKeywords {

    public static final Set<String> ALL = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // JPA 2.2 Reserved Keywords  (4.4.1)
            "ABS",
            "ALL",
            "AND",
            "ANY",
            "AS",
            "ASC",
            "AVG",
            "BETWEEN",
            "BIT_LENGTH",
            "BOTH",
            "BY",
            "CASE",
            "CAST",
            "CHAR_LENGTH",
            "CHARACTER_LENGTH",
            "CLASS",
            "COALESCE",
            "CONCAT",
            "COUNT",
            "CURRENT_DATE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "DELETE",
            "DESC",
            "DISTINCT",
            "ELSE",
            "EMPTY",
            "END",
            "ENTRY",
            "ESCAPE",
            "EXISTS",
            "FALSE",
            "FETCH",
            "FROM",
            "FUNCTION",
            "GROUP",
            "HAVING",
            "IN",
            "INDEX",
            "INNER",
            "IS",
            "JOIN",
            "KEY",
            "LEADING",
            "LEFT",
            "LENGTH",
            "LIKE",
            "LOCATE",
            "LOWER",
            "MAX",
            "MEMBER",
            "MIN",
            "MOD",
            "NEW",
            "NOT",
            "NULLIF",
            "OBJECT",
            "OF",
            "ON",
            "OR",
            "ORDER",
            "OUTER",
            "POSITION",
            "SELECT",
            "SET",
            "SIZE",
            "SOME",
            "SQRT",
            "SUBSTRING",
            "SUM",
            "THEN",
            "TRAILING",
            "TREAT",
            "TRUE",
            "TYPE",
            "UNKNOWN",
            "UPDATE",
            "UPPER",
            "VALUE",
            "WHEN",
            "WHERE",

            // Hibernate specific keywords
            "COLLATE",
            "CROSS",
            "DAY",
            "ELEMENTS",
            "FULL",
            "HOUR",
            "INSERT",
            "LIST",
            "MAP",
            "MINUTE",
            "MONTH",
            "RIGHT",
            "SECOND",
            "WITH",
            "YEAR"
    )));
}

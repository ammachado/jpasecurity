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

import java.math.RoundingMode;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jpasecurity.jpql.parser.JpqlParserVisitor;
import org.jpasecurity.jpql.parser.JpqlParserVisitor;

/**
 * This extension of the {@link JpqlParserVisitor} evaluates queries in memory, storing the result in the specified
 * {@link QueryEvaluationParameters}. If the evaluation cannot be performed due to missing information the result
 * is set to <quote>undefined</quote>. T evaluate subselect-query, pluggable implementations of
 * {@link SubQueryEvaluator} are used.
 *
 * @author Arne Limburg
 */
public interface QueryEvaluator extends JpqlParserVisitor<QueryEvaluationParameters> {

    int DECIMAL_PRECISION = 100;

    RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    boolean canEvaluate(ParserRuleContext node, QueryEvaluationParameters parameters);

    <R> R evaluate(ParserRuleContext node, QueryEvaluationParameters parameters) throws NotEvaluatableException;
}

/*
 * Copyright 2011 - 2016 Arne Limburg
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

import java.util.Collection;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParser;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSubselectEvaluator implements SubQueryEvaluator {

    protected QueryEvaluator evaluator;

    @Override
    public void setQueryEvaluator(QueryEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Collection<?> evaluate(JpqlCompiledStatement subQuery,
                                  QueryEvaluationParameters parameters) throws NotEvaluatableException {
        parameters.setResultUndefined();
        throw new NotEvaluatableException(getClass().getSimpleName() + " cannot evaluate subselects");
    }

    protected boolean isEvaluationDisabledByHint(ParserRuleContext node, Class<? extends ParserRuleContext> hintType) {
        if (node instanceof JpqlParser.SelectClauseContext) {
            final ParserRuleContext possibleHint = ((JpqlParser.SelectClauseContext)node).hintStatement();
            if (possibleHint != null) {
                for (int i = 0; i < possibleHint.getChildCount(); i++) {
                    if (hintType.isAssignableFrom(possibleHint.getChild(i).getClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean isAccessCheck(QueryEvaluationParameters parameters) {
        return parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.ACCESS_CHECK;
    }

    protected boolean isQueryOptimize(QueryEvaluationParameters parameters) {
        return parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.GET_ALWAYS_EVALUATABLE_RESULT
            || parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY;
    }
}

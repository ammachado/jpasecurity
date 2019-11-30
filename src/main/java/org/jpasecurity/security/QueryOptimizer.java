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
package org.jpasecurity.security;

import java.util.Map;
import javax.persistence.metamodel.Metamodel;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jpasecurity.Alias;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.compiler.NotEvaluatableException;
import org.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import org.jpasecurity.jpql.compiler.QueryEvaluator;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;

/**
 * Optimizes a query by evaluating subtrees in memory.
 *
 * @author Arne Limburg
 */
public class QueryOptimizer {

    private final NodeOptimizerVisitor nodeOptimizer;

     QueryOptimizer(Metamodel metamodel,
                    SecurePersistenceUnitUtil persistenceUnitUtil,
                    Map<Alias, Object> aliases,
                    Map<String, Object> namedParameters,
                    Map<Integer, Object> positionalParameters,
                    QueryEvaluator evaluator) {
        this.nodeOptimizer = new NodeOptimizerVisitor(
                new QueryEvaluationParameters(
                        metamodel,
                        persistenceUnitUtil,
                        aliases,
                        namedParameters,
                        positionalParameters,
                        QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY
                ),
                evaluator
        );
    }

    public void optimize(JpqlCompiledStatement compiledStatement) {
        optimize(compiledStatement.getWhereClause());
    }

    public void optimize(ParserRuleContext node) {
        nodeOptimizer.visit(node);
    }

    private static class NodeOptimizerVisitor extends JpqlVisitorAdapter<QueryEvaluationParameters> {

        private final QueryPreparator queryPreparator = new QueryPreparator();
        private final QueryEvaluator evaluator;

        NodeOptimizerVisitor(QueryEvaluationParameters value, QueryEvaluator evaluator) {
            super(value);
            this.evaluator = evaluator;
        }

        @Override
        public QueryEvaluationParameters visitWhereClause(JpqlParser.WhereClauseContext ctx) {
            try {
                if (evaluator.evaluate(ctx.predicate(), defaultResult())) {
                    queryPreparator.remove(ctx);
                } else {
                    ParserRuleContext node = queryPreparator.createBoolean(false);
                    TreeRewriteSupport.replace(ctx.predicate(), node);
                }
            } catch (NotEvaluatableException e) {
                defaultResult().setResult(true);
            }
            return defaultResult();
        }

        @Override
        public QueryEvaluationParameters visitAndPredicate(JpqlParser.AndPredicateContext ctx) {
            try {
                if (evaluator.<Boolean>evaluate(ctx.predicate(0), defaultResult())) {
                    TreeRewriteSupport.replace(ctx, ctx.predicate(1));
                } else {
                    TreeRewriteSupport.replace(ctx.predicate(0), queryPreparator.createBoolean(false));
                }
                defaultResult().setResult(true);
            } catch (NotEvaluatableException nee1) {
                try {
                    if (evaluator.<Boolean>evaluate(ctx.predicate(1), defaultResult())) {
                        TreeRewriteSupport.replace(ctx, ctx.getChild(0));
                    } else {
                        TreeRewriteSupport.replace(ctx.predicate(1), queryPreparator.createBoolean(false));
                    }
                    defaultResult().setResult(true);
                } catch (NotEvaluatableException nee2) {
                    defaultResult().setResult(true);
                }
            }
            return defaultResult();
        }

        @Override
        public QueryEvaluationParameters visitOrPredicate(JpqlParser.OrPredicateContext ctx) {
            try {
                if (evaluator.<Boolean>evaluate(ctx.predicate(0), defaultResult())) {
                    TreeRewriteSupport.replace(ctx, QueryPreparator.createOneEqualsOne());
                } else {
                    TreeRewriteSupport.replace(ctx, ctx.predicate(1));
                }
                defaultResult().setResult(true);
            } catch (NotEvaluatableException nee1) {
                try {
                    if (evaluator.<Boolean>evaluate(ctx.predicate(1), defaultResult())) {
                        TreeRewriteSupport.replace(ctx, QueryPreparator.createOneEqualsOne());
                    } else {
                        TreeRewriteSupport.replace(ctx, ctx.predicate(0));
                    }
                    defaultResult().setResult(true);
                } catch (NotEvaluatableException nee2) {
                    defaultResult().setResult(true);
                }
            }
            return defaultResult();
        }

        @Override
        public QueryEvaluationParameters visitGroupedPredicate(JpqlParser.GroupedPredicateContext ctx) {
            JpqlParser.PredicateContext context = ctx.predicate();
            while (context instanceof JpqlParser.GroupedPredicateContext) {
                context = (JpqlParser.PredicateContext)TreeRewriteSupport.replace(ctx.predicate(),
                        ((JpqlParser.GroupedPredicateContext)ctx.predicate()).predicate());
            }
            return defaultResult();
        }

        @Override
        public QueryEvaluationParameters visitGroupedExpression(JpqlParser.GroupedExpressionContext ctx) {
            JpqlParser.ExpressionContext context = ctx.expression();
            while (context instanceof JpqlParser.GroupedExpressionContext) {
                context = (JpqlParser.ExpressionContext)TreeRewriteSupport.replace(ctx.expression(),
                        ((JpqlParser.GroupedExpressionContext)ctx.expression()).expression());
            }
            return defaultResult();
        }
    }
}

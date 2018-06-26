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

import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ToStringVisitor;

public class JpqlQueryRewriter extends ToStringVisitor {

    private static final String CURRENT_PRINCIPAL = "CURRENT_PRINCIPAL";
    private static final String CURRENT_ROLES = "CURRENT_ROLES";
    private static final String CURRENT_TENANT = "CURRENT_TENANT";

    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final StringBuilder sb;

    JpqlQueryRewriter() {
        this.sb = defaultResult();
    }

    @Override
    public StringBuilder visitNamedParameter(JpqlParser.NamedParameterContext ctx) {
        return super.visitNamedParameter(ctx);
    }

    @Override
    public StringBuilder visitIdentifier(JpqlParser.IdentifierContext ctx) {
        switch (ctx.getText()) {
            case CURRENT_PRINCIPAL:
            case CURRENT_ROLES:
            case CURRENT_TENANT:
                sb.append(":");
        }
        return sb.append(ctx.getText());
    }

    @Override
    public StringBuilder visitGroupedPredicate(JpqlParser.GroupedPredicateContext ctx) {
        JpqlParser.PredicateContext context = ctx.predicate();
        while (context instanceof JpqlParser.GroupedPredicateContext) {
            context = (JpqlParser.PredicateContext) TreeRewriteSupport.replace(ctx.predicate(),
                    ((JpqlParser.GroupedPredicateContext) ctx.predicate()).predicate());
        }
        return visit(context);
    }

    @Override
    public StringBuilder visitGroupedExpression(JpqlParser.GroupedExpressionContext ctx) {
        JpqlParser.ExpressionContext context = ctx.expression();
        while (context instanceof JpqlParser.GroupedExpressionContext) {
            context = (JpqlParser.ExpressionContext) TreeRewriteSupport.replace(ctx.expression(),
                    ((JpqlParser.GroupedExpressionContext) ctx.expression()).expression());
        }
        return visit(context);
    }


    public void clear() {
        sb.delete(0, sb.length());
    }
}

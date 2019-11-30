/*
 * Copyright 2010 Arne Limburg
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
package org.jpasecurity.jpql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParserBaseVisitor;
import org.jpasecurity.jpql.parser.JpqlValueHolderVisitor;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.util.ValueHolder;

/**
 * @author Arne Limburg
 */
public class JpqlStatementHolder implements Cloneable {

    private ParserRuleContext statement;
    private BaseContext fromClause;
    private JpqlParser.WhereClauseContext whereClause;
    private List<JpqlParser.EntityNameContext> whereClausePaths;

    public JpqlStatementHolder(ParserRuleContext statement) {
        this.statement = statement;
    }

    /**
     * Returns the node representing this statement.
     */
    public ParserRuleContext getStatement() {
        return statement;
    }

    public BaseContext getFromClause() {
        if (fromClause == null) {
            fromClause = new FromVisitor().visit(statement).getValue();
        }
        return fromClause;
    }

    public JpqlParser.WhereClauseContext getWhereClause() {
        if (this.whereClause == null) {
            this.whereClause = new WhereVisitor().visit(statement).getValue();
        }
        return this.whereClause;
    }

    public List<JpqlParser.EntityNameContext> getWhereClausePaths() {
        if (this.whereClausePaths == null) {
            JpqlParser.WhereClauseContext whereClause = getWhereClause();
            if (whereClause != null) {
                PathVisitor visitor = new PathVisitor();
                this.whereClausePaths = Collections.unmodifiableList(visitor.visit(whereClause.predicate()));
            }
        }
        return this.whereClausePaths;
    }

    @Override
    public JpqlStatementHolder clone() {
        try {
            JpqlStatementHolder statement = (JpqlStatementHolder)super.clone();
            statement.statement = TreeRewriteSupport.copy(statement.statement);
            statement.fromClause = null;
            statement.whereClause = null;
            statement.whereClausePaths = null;
            return statement;
        } catch (CloneNotSupportedException e) {
            //this should not happen since we are cloneable
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return getClass() + "[\"" + ((BaseContext)statement).toJpqlString() + "\"]";
    }

    protected <T> T visit(JpqlParserBaseVisitor<T> visitor) {
        return visitor.visit(statement);
    }

    private static class FromVisitor extends JpqlValueHolderVisitor<BaseContext> {

        @Override
        public ValueHolder<BaseContext> visitSelectStatement(JpqlParser.SelectStatementContext ctx) {
            defaultResult().setValue(ctx.querySpec().fromClause());
            return stopVisitingChildren();
        }

        @Override
        public ValueHolder<BaseContext> visitUpdateStatement(JpqlParser.UpdateStatementContext ctx) {
            defaultResult().setValue(ctx.pathRoot());
            return stopVisitingChildren();
        }

        @Override
        public ValueHolder<BaseContext> visitDeleteStatement(JpqlParser.DeleteStatementContext ctx) {
            defaultResult().setValue(ctx.pathRoot());
            return stopVisitingChildren();
        }
    }

    private static class WhereVisitor extends JpqlValueHolderVisitor<JpqlParser.WhereClauseContext> {

        @Override
        public ValueHolder<JpqlParser.WhereClauseContext> visitWhereClause(JpqlParser.WhereClauseContext ctx) {
            defaultResult().setValue(ctx);
            return stopVisitingChildren();
        }
    }

    private static class PathVisitor extends JpqlVisitorAdapter<List<JpqlParser.EntityNameContext>> {

        PathVisitor() {
            super(new ArrayList<JpqlParser.EntityNameContext>());
        }

        @Override
        public List<JpqlParser.EntityNameContext> visitPathRoot(JpqlParser.PathRootContext ctx) {
            defaultResult().add(ctx.entityName());
            return defaultResult();
        }
    }
}

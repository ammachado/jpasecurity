/*
 * Copyright 2012 - 2016 Arne Limburg
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

import static org.jpasecurity.util.Validate.notNull;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.BaseContext;
import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.parser.JpqlParser;

/**
 * @author Arne Limburg
 */
public class ConditionalPath extends Path {

    private final BaseContext condition;

    public ConditionalPath(String path, JpqlParser.SimpleCaseStatementContext conditionNode) {
        super(path);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public ConditionalPath(Alias alias, String subPath, JpqlParser.SearchedCaseStatementContext conditionNode) {
        super(alias, subPath);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public ConditionalPath(String path, JpqlParser.ExpressionContext conditionNode) {
        super(path);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public ConditionalPath(Alias alias, String subPath, JpqlParser.ExpressionContext conditionNode) {
        super(alias, subPath);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public ConditionalPath(String path, JpqlParser.PredicateContext conditionNode) {
        super(path);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public ConditionalPath(Alias alias, String subPath, JpqlParser.PredicateContext conditionNode) {
        super(alias, subPath);
        notNull("Condition", conditionNode);
        condition = TreeRewriteSupport.copy(conditionNode);
    }

    public BaseContext getCondition() {
        return TreeRewriteSupport.copy(condition);
    }

    public ConditionalPath newCondition(JpqlParser.ExpressionContext condition) {
        return new ConditionalPath(getRootAlias(), getSubpath(), condition);
    }

    @Override
    public ConditionalPath getParentPath() {
        //return new ConditionalPath(super.getParentPath().toString(), condition);
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "WHEN " + condition + " THEN " + super.toString();
    }
}

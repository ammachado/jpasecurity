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
package org.jpasecurity.jpql.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public abstract class JpqlVisitorAdapter<T> extends JpqlParserBaseVisitor<T> {

    private T value;

    private boolean shouldVisitNextChild;

    protected JpqlVisitorAdapter(final T value) {
        this.value = value;
    }

    @Override
    protected final T defaultResult() {
        return value;
    }

    @Override
    public T visit(ParseTree tree) {
        this.shouldVisitNextChild = true;
        return super.visit(tree);
    }

    /**
     * Indicates if the visitor should keep visiting the children's node.
     *
     * @return <code>true</code> if the visitor shoud keep visiting the children's node.
     */
    protected boolean isShouldVisitNextChild() {
        return shouldVisitNextChild;
    }

    /**
     * Sets if this visitor should continue visiting children.
     * @param shouldVisitNextChild value to set
     */
    protected void setShouldVisitNextChild(boolean shouldVisitNextChild) {
        this.shouldVisitNextChild = shouldVisitNextChild;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldVisitNextChild(RuleNode node, T currentResult) {
        return shouldVisitNextChild;
    }

    protected final T stopVisitingChildren() {
        return stopVisitingChildren(defaultResult());
    }

    protected final T stopVisitingChildren(T result) {
        setShouldVisitNextChild(false);
        return result;
    }

    protected final T keepVisitingChildren() {
        return keepVisitingChildren(defaultResult());
    }

    protected final T keepVisitingChildren(T result) {
        setShouldVisitNextChild(true);
        return result;
    }

    protected final T visitOptionalToken(ParseTree ctx) {
        if (ctx != null) {
            visit(ctx);
        }
        return defaultResult();
    }

    protected void setDefaultResult(T defaultResult) {
        this.value = defaultResult;
    }
}

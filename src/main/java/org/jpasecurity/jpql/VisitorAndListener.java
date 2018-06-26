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
package org.jpasecurity.jpql;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jpasecurity.jpql.parser.JpqlParserBaseVisitor;
import org.jpasecurity.jpql.parser.JpqlParserListener;

public abstract class VisitorAndListener<T> extends JpqlParserBaseVisitor<T> {

    private JpqlParserListener listener;

    public VisitorAndListener(JpqlParserListener listener) {
        this.listener = listener;
    }

    @Override
    public final T visit(ParseTree tree) {
        try {
            if (listener != null) {
                if (tree instanceof ParserRuleContext) {
                    ((ParserRuleContext)tree).enterRule(listener);
                } else if (tree instanceof ErrorNode) {
                    listener.visitErrorNode((ErrorNode)tree);
                } else if (tree instanceof TerminalNode) {
                    listener.visitTerminal((TerminalNode)tree);
                }
            }

            return super.visit(tree);
        } finally {
            if (listener != null && tree instanceof ParserRuleContext) {
                ((ParserRuleContext)tree).exitRule(listener);
            }
        }
    }
}

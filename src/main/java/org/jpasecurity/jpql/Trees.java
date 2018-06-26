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

/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */
package org.jpasecurity.jpql;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.antlr.v4.runtime.tree.Tree;

/**
 * A set of utility routines useful for all kinds of ANTLR trees.
 */
public final class Trees {

    /**
     * A reflection cache tracking constructor methods for various tree nodes
     * so we can clone nodes. If this starts to get too big,
     * call {@link #resetCtorCache}.
     */
    private static final Map<Class<? extends ParserRuleContext>,
            Constructor<? extends ParserRuleContext>> CTOR_CACHE = new ConcurrentHashMap<>();

    /**
     * Print out a whole tree in LISP form. {@link #getNodeText} is used on the
     * node payloads to get the text for the nodes.  Detect
     * parse trees and extract data appropriately.
     */
    public static String toStringTree(Tree t) {
        return toStringTree(t, (List<String>)null);
    }

    /**
     * Print out a whole tree in LISP form. {@link #getNodeText} is used on the
     * node payloads to get the text for the nodes.  Detect
     * parse trees and extract data appropriately.
     */
    public static String toStringTree(Tree t, Parser recog) {
        String[] ruleNames = recog != null ? recog.getRuleNames() : null;
        List<String> ruleNamesList = ruleNames != null ? Arrays.asList(ruleNames) : null;
        return toStringTree(t, ruleNamesList);
    }

    /**
     * Print out a whole tree in LISP form. {@link #getNodeText} is used on the
     * node payloads to get the text for the nodes.
     */
    public static String toStringTree(final Tree t, final List<String> ruleNames) {
        String s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
        if (t.getChildCount() == 0) {
            return s;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
        buf.append(s);
        buf.append(' ');
        for (int i = 0; i < t.getChildCount(); i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append(toStringTree(t.getChild(i), ruleNames));
        }
        buf.append(")");
        return buf.toString();
    }

    public static String getNodeText(Tree t, List<String> ruleNames) {
        if (ruleNames != null) {
            if (t instanceof RuleContext) {
                int ruleIndex = ((RuleContext)t).getRuleContext().getRuleIndex();
                String ruleName = ruleNames.get(ruleIndex);
                int altNumber = ((RuleContext)t).getAltNumber();
                if (altNumber != ATN.INVALID_ALT_NUMBER) {
                    return ruleName + ":" + altNumber;
                }
                return ruleName;
            } else if (t instanceof ErrorNode) {
                return t.toString();
            } else if (t instanceof TerminalNode) {
                Token symbol = ((TerminalNode)t).getSymbol();
                if (symbol != null) {
                    return symbol.getText();
                }
            }
        }
        // no recog for rule names
        Object payload = t.getPayload();
        if (payload instanceof Token) {
            return ((Token)payload).getText();
        }
        return t.getPayload().toString();
    }

    /**
     * Return ordered list of all children of this node
     */
    public static List<Tree> getChildren(Tree t) {
        List<Tree> kids = new ArrayList<>();
        for (int i = 0; i < t.getChildCount(); i++) {
            kids.add(t.getChild(i));
        }
        return kids;
    }

    /**
     * Return a new TerminalNode with a copy of all relevant fields
     * from t and with null parent.
     *
     * @since 4.6.1
     */
    public static TerminalNodeImpl shallowCopy(TerminalNode t) {
        return new TerminalNodeImpl(t.getSymbol());
    }

    /**
     * Create a shallow copy of node t; the copy has same type as t. Copy
     * all relevant fields to make a proper clone of t except
     * the children list. The only caveat is that the error nodes from t
     * are in fact copied into the clone.
     * <p>
     * t is not altered.
     * <p>
     * Because this uses reflection, it might be slower than using
     * "new X()" directly.  It uses {@link #CTOR_CACHE} to speed things up a lot.
     *
     * @since 4.6.1
     */
    public static ParserRuleContext shallowCopy(ParserRuleContext t) {
        Class<? extends ParserRuleContext> cl = t.getClass();
        if (!CTOR_CACHE.containsKey(cl)) {
            for (final Constructor<?> ctor : cl.getDeclaredConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 0) {
                    continue;
                }
                boolean firstParamIsParserRuleContext = ParserRuleContext.class.isAssignableFrom(params[0]);
                if (params.length == 1 && firstParamIsParserRuleContext) {
                    CTOR_CACHE.put(cl, (Constructor<? extends ParserRuleContext>)ctor);
                    break;
                } else if (params.length == 2 && firstParamIsParserRuleContext
                        && int.class.isAssignableFrom(params[1])) {
                    CTOR_CACHE.put(cl, (Constructor<? extends ParserRuleContext>)ctor);
                    break;
                }
            }
        }

        Constructor<? extends ParserRuleContext> ctor = CTOR_CACHE.get(cl);
        try {
            if (ctor.getParameterTypes().length == 1) {
                return ctor.newInstance(t);
            } else {
                ParserRuleContext copy = ctor.newInstance(t.getParent(), t.invokingState);
                copy.copyFrom(t); // make sure we copy all necessary fields (and possibly error node children)
                return copy;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Make a shallow copy of a generic parse tree node t
     *
     * @since 4.6.1
     */
    public static ParseTree shallowCopy(ParseTree t) {
        if (t instanceof TerminalNode) {
            return shallowCopy((TerminalNode)t);
        }
        return shallowCopy((ParserRuleContext)t);
    }

    /**
     * Just in case the constructor cache gets too big, you can clear
     * with this method.
     *
     * @since 4.6.1
     */
    public static void resetCtorCache() {
        CTOR_CACHE.clear();
    }

    private Trees() {
    }
}

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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.BaseContext;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParser.AndPredicateContext;
import org.jpasecurity.jpql.parser.JpqlParser.ExpressionContext;
import org.jpasecurity.jpql.parser.JpqlParser.GroupedExpressionContext;
import org.jpasecurity.jpql.parser.JpqlParser.GroupedPredicateContext;
import org.jpasecurity.jpql.parser.JpqlParser.InequalityPredicateContext;
import org.jpasecurity.jpql.parser.JpqlParser.OrPredicateContext;
import org.jpasecurity.jpql.parser.JpqlParser.PredicateContext;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;

import javax.persistence.metamodel.EntityType;
import java.util.List;

/**
 * @author Arne Limburg
 */
public class QueryPreparator {

    /**
     * Removes the specified <tt>With</tt>-BaseContext from its parent
     * and appends the condition to the <tt>Where</tt>-BaseContext of the specified subselect.
     *
     * @param subQuery the sub query node.
     * @param with     the with-node
     * @deprecated JPA 2.1 supports qualified join predicates.
     */
    @Deprecated
    public void appendToWhereClause(JpqlParser.SubQueryContext subQuery,
                                    JpqlParser.QualifiedJoinPredicateContext with) {
        throw new UnsupportedOperationException("JPA 2.1 supports qualified join predicates.");
    }

    /**
     * Appends the specified BaseContext to the specified <tt>Where</tt>-BaseContext with <tt>and</tt>.
     *
     * @param where the <tt>Where</tt>-node
     * @param node  the node
     */
    public void append(JpqlParser.WhereClauseContext where, BaseContext node) {
        new AppendWhereClauseVisitor(where).visit(node);
    }

    /**
     * Appends the specified access rule to the specified BaseContext with <tt>or</tt>.
     *
     * @param node      the node
     * @param alias     the alias to be selected from the access rule
     * @param statement the access rule
     * @return the <tt>Or</tt>-node.
     */
    public OrPredicateContext append(BaseContext node, Path alias, JpqlCompiledStatement statement) {
        final BaseContext in = createIn(alias, statement);
        return createOr((PredicateContext)node, (PredicateContext)in);
    }

    /**
     * Prepends the specified path to the specified pathNode.
     *
     * @param path     the path
     * @param pathNode the new node
     * @return the new path
     */
    public static JpqlParser.PathContext prepend(Path path, JpqlParser.PathContext pathNode) {
        Alias alias = path.getRootAlias();
        String[] subpathComponents = path.getSubpathComponents();
        String[] pathComponents = new String[subpathComponents.length + 1];
        pathComponents[0] = alias.getName();
        System.arraycopy(subpathComponents, 0, pathComponents, 1, subpathComponents.length);
        return prepend(pathComponents, pathNode, path.isKeyPath(), path.isValuePath());
    }

    private static JpqlParser.PathContext prepend(String[] pathComponents,
                                                 JpqlParser.PathContext path,
                                                 boolean isKeyPath,
                                                 boolean isValuePath) {
        if (pathComponents.length == 0) {
            return path;
        }
        for (int i = path.getChildCount() - 1; i >= 0; i--) {
            path.addChild((RuleContext)path.getChild(i));
        }
        if (path.getChildCount() > pathComponents.length) {
            ParseTree oldVariable = path.getChild(pathComponents.length);
            TreeRewriteSupport.setChild(
                    path,
                    pathComponents.length,
                    createIdentificationVariable(new Alias(oldVariable.getText()))
            );
        }
        /*
        if (path.getChildCount() > pathComponents.length) {
            //Replace first identification variable with identifier
            BaseContext oldVariable = path.getChild(pathComponents.length);
            path.setChild(createIdentificationVariable(new Alias(oldVariable.getValue())), pathComponents.length);
        }
        path.addChild(createVariable(pathComponents[0]), 0);
        for (int i = 1; i < pathComponents.length; i++) {
            path.addChild(createIdentificationVariable(new Alias(pathComponents[i])), i);
        }
        if (isKeyPath) {
            path.setChild(createKey(path.getChild(0)), 0);
        }
        if (isValuePath) {
            path.setChild(createValue(path.getChild(0)), 0);
        }
        return path;
        */

        throw new UnsupportedOperationException();
    }

    /**
     * Creates a <tt>JpqlParser.MapKeyPathRootContext</tt> node.
     */
    public JpqlParser.MapKeyPathRootContext createKey(BaseContext child) {
        throw new UnsupportedOperationException();
        /*
        JpqlKey key = new JpqlKey(JpqlParserTreeConstants.JJTKEY);
        child.setParent(key);
        key.addChild(child, 0);
        return key;
        */
    }

    /**
     * Creates a <tt>JpqlParser.CollectionValuePathRootContext</tt> node.
     */
    public JpqlParser.CollectionValuePathRootContext createValue(BaseContext child) {
        throw new UnsupportedOperationException();
        /*
        JpqlValue value = new JpqlValue(JpqlParserTreeConstants.JJTVALUE);
        child.setParent(value);
        value.addChild(child, 0);
        return value;
        */
    }

    /**
     * Creates a <tt>JpqlWhere</tt> node.
     */
    public JpqlParser.WhereClauseContext createWhere(BaseContext child) {
        return JpqlParsingHelper.parseWhereClause(String.format("WHERE %s", child.toJpqlString()));
    }

    /**
     * Creates a <tt>JpqlBooleanLiteral</tt> BaseContext with the specified value.
     */
    public JpqlParser.PredicateContext createBoolean(boolean value) {
        return value ? createOneEqualsOne() : createOneNotEqualsOne();
    }

    /**
     * Creates a <tt>JpqlIntegerLiteral</tt> BaseContext with the specified value.
     */
    public JpqlParser.PredicateContext createNumber(int value) {
        return JpqlParsingHelper.createParser(String.valueOf(value)).predicate();
    }

    /**
     * Creates a <tt>JpqlIdentificationVariable</tt> BaseContext with the specified value.
     */
    public JpqlParser.IdentificationVariableContext createVariable(String value) {
        throw new UnsupportedOperationException();
        /*
        JpqlIdentificationVariable variable
            = new JpqlParser.IdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        variable.setValue(value);
        return variable;
        */
    }

    /**
     * Creates a <tt>JpqlNamedInputParameter</tt> BaseContext with the specified name.
     */
    public JpqlParser.ExpressionContext createNamedParameter(String name) {
        return JpqlParsingHelper.createParser(String.format(":%s", name)).expression();
    }

    /**
     * Connects the specified BaseContext with <tt>JpqlAnd</tt>.
     */
    public static <T extends PredicateContext, U extends PredicateContext> AndPredicateContext createAnd(T ctx1, U  ctx2) {
        String predicate = String.format("%s AND %s", ctx1.toJpqlString(), ctx2.toJpqlString());
        return (AndPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    /**
     * Connects the specified ParserRuleContext with <tt>JpqlOr</tt>.
     */
    public static <T extends PredicateContext, U extends PredicateContext> OrPredicateContext createOr(T ctx1, U  ctx2) {
        String predicate = String.format("%s OR %s", ctx1.toJpqlString(), ctx2.toJpqlString());
        return (OrPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    /**
     * Creates an impliciation, that means: <tt>if (and only if) node1 then node2</tt>.
     */
    public static <T extends PredicateContext, U extends PredicateContext> OrPredicateContext createImplication(
            T ctx1, U ctx2) {
        String predicate = String.format("(NOT %s) OR %s", ctx1, ctx2);
        return (OrPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    public static <T extends PredicateContext> JpqlParser.NegatedPredicateContext createNot(T ctx) {
        String predicate = String.format("NOT %s", ctx);
        return (JpqlParser.NegatedPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    public JpqlParser.IsNullPredicateContext createIsNull(ExpressionContext ctx) {
        return createIsNullPredicate(ctx, false);
    }

    public JpqlParser.IsNullPredicateContext createIsNotNull(ExpressionContext ctx) {
        return createIsNullPredicate(ctx, true);
    }

    /**
     * Connects the specified BaseContext with <tt>JpqlEquals</tt>.
     */
    public static <T extends ExpressionContext, U extends ExpressionContext> JpqlParser.EqualityPredicateContext createEquals(
            T ctx1, U ctx2) {
        String predicate = String.format("%s = %s", ctx1.toJpqlString(), ctx2.toJpqlString());
        return (JpqlParser.EqualityPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    /**
     * Connects the specified BaseContext with <tt>JpqlNotEquals</tt>.
     */
    public static <T extends ExpressionContext, U extends ExpressionContext> InequalityPredicateContext createNotEquals(
            T ctx1, U ctx2) {
        String predicate = String.format("%s <> %s", ctx1.toJpqlString(), ctx2.toJpqlString());
        return (JpqlParser.InequalityPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    /**
     * Appends the specified children to the list of children of the specified parent.
     *
     * @return the parent
     */
    public <N extends BaseContext> N appendChildren(N parent, BaseContext... children) {
        for (int i = 0; i < children.length; i++) {
            parent.addChild(children[i]);
            children[i].setParent(parent);
        }
        return parent;
    }

    /**
     * Creates an <tt>JpqlIn</tt> subtree for the specified access rule.
     */
    public BaseContext createIn(Path alias, JpqlCompiledStatement statement) {
        throw new UnsupportedOperationException();
        /*
        JpqlIn in = new JpqlParser.In(JpqlParserTreeConstants.JJTIN);
        BaseContext path = createPath(alias);
        path.setParent(in);
        in.addChild(path, 0);
        BaseContext subselect = createSubselect(statement);
        subselect.setParent(in);
        in.addChild(subselect, 1);
        return createBrackets(in);
        */
    }

    /**
     * Creates brackets for the specified node.
     */
    public static GroupedPredicateContext createBrackets(PredicateContext node) {
        return (GroupedPredicateContext) JpqlParsingHelper.createParser(String.format("(%s)", node.toJpqlString())).predicate();
    }

    /**
     * Creates brackets for the specified node.
     */
    public static GroupedExpressionContext createBrackets(ExpressionContext node) {
        return (GroupedExpressionContext) JpqlParsingHelper.createParser(
                String.format("(%s)", node.toJpqlString())).expression();
    }

    /**
     * Creates a <tt>JpqlPath</tt> BaseContext for the specified string.
     */
    public JpqlParser.PathContext createPath(Path path) {
        throw new UnsupportedOperationException();
        /*
        JpqlIdentificationVariable identifier = createIdentificationVariable(path.getRootAlias());
        JpqlParser.PathContext pathParserRuleContext = appendChildren(new JpqlParser.Path(JpqlParserTreeConstants.JJTPATH), identifier);
        for (String pathComponent: path.getSubpathComponents()) {
            JpqlIdentificationVariable identificationVariable
                = new JpqlParser.IdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
            identificationVariable.setValue(pathComponent);
            identificationVariable.setParent(pathNode);
            pathNode.addChild(identificationVariable, pathNode.getChildCount());
        }
        return pathNode;
        */
    }

    /**
     * Creates a <tt>JpqlPath</tt> BaseContext for the specified string.
     */
    public JpqlParser.CollectionReferenceContext createCollectionValuedPath(JpqlParser.PathContext path) {
        throw new UnsupportedOperationException();
        /*
        BaseContext clonedPath = path.clone();
        JpqlCollectionValuedPath newPath
            = new JpqlParser.CollectionValuedPath(JpqlParserTreeConstants.JJTCOLLECTIONVALUEDPATH);
        for (int i = 0; i < clonedPath.getChildCount(); i++) {
            newPath.addChild(clonedPath.getChild(i), i);
            clonedPath.getChild(i).setParent(newPath);
        }
        return newPath;
        */
    }

    /**
     * Creates a <tt>JpqlSubselect</tt> BaseContext for the specified access rule.
     */
    public JpqlParser.SubQueryContext createSubselect(JpqlCompiledStatement statement) {
        if (statement.getSelectedPaths().size() > 1) {
            throw new IllegalArgumentException("Cannot create subselect from statements with scalar select-clause");
        }
        BaseContext select = createSelectClause(statement.getSelectedPaths().get(0));
        BaseContext from = statement.getFromClause();
        BaseContext where = statement.getWhereClause();
        String subQuery = String.format("%s %s %s", select.toJpqlString(), from.toJpqlString(), where.toJpqlString());
        final JpqlParser.SubQueryContext subQueryContext = JpqlParsingHelper.createParser(subQuery).subQuery();
        return appendChildren(subQueryContext);
    }

    /**
     * Creates a <tt>JpqlSelectClause</tt> BaseContext to select the specified path.
     */
    public JpqlParser.SelectionContext createSelectClause(Path selectedPath) {
        throw new UnsupportedOperationException();
        /*
        JpqlSelectExpression expression = createSelectExpression(createPath(selectedPath));
        JpqlSelectExpressions expressions = new JpqlParser.SelectExpressions(JpqlParserTreeConstants.sELECTEXPRESSIONS);
        expressions = appendChildren(expressions, expression);
        return appendChildren(new JpqlParser.SelectClause(JpqlParserTreeConstants.sELECTCLAUSE), expressions);
        */
    }

    public JpqlParser.SelectExpressionContext createSelectExpression(BaseContext node) {
        throw new UnsupportedOperationException();
        /*
        JpqlSelectExpression expression = new JpqlParser.SelectExpression(JpqlParserTreeConstants.sELECTEXPRESSION);
        expression = appendChildren(expression, node);
        return expression;
        */
    }

    /**
     * Creates a <tt>JpqlSelectClause</tt> BaseContext to select the specified path.
     */
    public JpqlParser.FromElementSpaceContext createFrom(EntityType<?> classMapping, Alias alias) {
        throw new UnsupportedOperationException();
        /*
        JpqlIdentificationVariableDeclaration declaration
            = new JpqlParser.IdentificationVariableDeclaration(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLEDECLARATION);
        declaration = appendChildren(declaration, createFromItem(new Alias(classMapping.getName()), alias));
        return appendChildren(new JpqlParser.From(JpqlParserTreeConstants.JJTFROM), declaration);
        */
    }

    public JpqlParser.FromElementSpaceRootContext createFromItem(Alias type, Alias alias) {
        throw new UnsupportedOperationException();
        /*
        JpqlAbstractSchemaName schemaName = new JpqlParser.AbstractSchemaName(JpqlParserTreeConstants.aBSTRACTSCHEMANAME);
        return appendChildren(new JpqlParser.FromItem(JpqlParserTreeConstants.JJTFROMITEM),
                              appendChildren(schemaName, createIdentificationVariable(type)),
                              createIdentificationVariable(alias));
        */
    }

    public JpqlParser.PredicateContext createInstanceOf(Path path, EntityType<?> classMapping) {
        String statement = String.format("TYPE(%s) = %s", path, classMapping.getName());
        return JpqlParsingHelper.createParser(statement).predicate();
    }

    public JpqlParser.SubQueryContext createSubselectById(Path path, EntityType<?> classMapping) {
        throw new UnsupportedOperationException();
        /*
        Alias alias = new Alias(Introspector.decapitalize(classMapping.getName()));
        if (!path.hasSubpath() && path.getRootAlias().equals(alias)) {
            alias = new Alias(alias.toString() + '0');
        }
        JpqlSelectClause select = createSelectClause(alias.toPath());
        JpqlFrom from = createFrom(classMapping, alias);
        JpqlParser.WhereClauseContext where = createWhere(createEquals(createPath(alias.toPath()), createPath(path)));
        return appendChildren(new JpqlParser.Subselect(JpqlParserTreeConstants.sUBSELECT), select, from, where);
        */
    }

    public static JpqlParser.IdentificationVariableContext createIdentificationVariable(Alias value) {
        return createIdentificationVariable(value.toString());
    }

    public static JpqlParser.IdentificationVariableContext createIdentificationVariable(String value) {
        throw new UnsupportedOperationException();
        /*
        JpqlIdentificationVariable identificationVariable
            = new JpqlParser.IdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        identificationVariable.setValue(value);
        return identificationVariable;
        */
    }

    public static JpqlParser.EqualityPredicateContext createOneEqualsOne() {
        JpqlParser.LiteralExpressionContext numberExpression1 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        JpqlParser.LiteralExpressionContext numberExpression2 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        return createEquals(numberExpression1, numberExpression2);
    }

    public static JpqlParser.PredicateContext createOneNotEqualsOne() {
        JpqlParser.LiteralExpressionContext numberExpression1 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        JpqlParser.LiteralExpressionContext numberExpression2 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        return createNotEquals(numberExpression1, numberExpression2);
    }

    public BaseContext removeConstructor(final BaseContext statementNode) {
        throw new UnsupportedOperationException();
        /*
        statementNode.visit(constructorReplacer);
        return statementNode;
        */
    }

    public void remove(BaseContext node) {
        throw new UnsupportedOperationException();
        /*
        if (node.getParent() != null) {
            BaseContext parent = node.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (node.getParent().getChild(i) == node) {
                    node.getParent().jjtRemoveChild(i);
                    node.setParent(null);
                }
            }
        }
        */
    }

    public static ParserRuleContext replace(ParserRuleContext oldCtx, ParseTree newCtx) {
        RuleNode parent = oldCtx.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == oldCtx) {
                TreeRewriteSupport.setChild(parent, i, newCtx);
                return (ParserRuleContext)newCtx;
            }
        }

        return null;
    }

    public void replace(BaseContext node, Path oldPath, Path newPath) {
        new PathReplacer(new ReplaceParameters(oldPath, newPath)).visit(node);
    }

    private int getIndex(BaseContext parent, BaseContext child) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == child) {
                return i;
            }
        }
        return -1;
    }

    private static JpqlParser.IsNullPredicateContext createIsNullPredicate(ExpressionContext ctx, boolean negated) {
        String predicate = String.format("%s IS %sNULL", ctx.toJpqlString(), negated ? "NOT " : "");
        return (JpqlParser.IsNullPredicateContext) JpqlParsingHelper.createParser(predicate).predicate();
    }

    private static class PathReplacer extends JpqlVisitorAdapter<ReplaceParameters> {

        PathReplacer(ReplaceParameters value) {
            super(value);
        }

        @Override
        public ReplaceParameters visitSimplePath(JpqlParser.SimplePathContext ctx) {
            if (canReplace(ctx.simplePathQualifier(), defaultResult())) {
                replace(ctx.simplePathQualifier(), defaultResult());
            }
            return stopVisitingChildren();
        }

        @Override
        public ReplaceParameters visitMapEntryPath(JpqlParser.MapEntryPathContext ctx) {
            visit(ctx.mapReference());
            return stopVisitingChildren();
        }

        @Override
        public ReplaceParameters visitIndexedPath(JpqlParser.IndexedPathContext ctx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReplaceParameters visitCompoundPath(JpqlParser.CompoundPathContext ctx) {
            throw new UnsupportedOperationException();
        }

        private boolean canReplace(JpqlParser.SimplePathQualifierContext path, ReplaceParameters parameters) {
            Path oldPath = parameters.getOldPath();
            if (path.getChildCount() <= oldPath.getSubpathComponents().length) {
                return false;
            }
            if (!oldPath.getRootAlias().getName().equals(path.getChild(0).getText())) {
                return false;
            }
            String[] pathComponents = oldPath.getSubpathComponents();
            for (int i = 1; i <= pathComponents.length; i++) {
                if (!pathComponents[i - 1].equals(path.getChild(i).getText())) {
                    return false;
                }
            }
            return true;
        }

        private void replace(JpqlParser.SimplePathQualifierContext path, ReplaceParameters parameters) {
            StringBuilder pathParts = new StringBuilder();
            for (int i = 0; i < path.getChildCount(); i++) {
                ParseTree child = path.getChild(i);
                if (parameters.getOldPath().toString().equals(child.getText())) {
                    pathParts.append(parameters.getNewPath().toString());
                } else {
                    pathParts.append(child.getText());
                }
            }

            JpqlParser.DotIdentifierSequenceContext identifier = JpqlParsingHelper.createParser(pathParts.toString())
                    .dotIdentifierSequence();
            QueryPreparator.replace(path, identifier);
        }
    }

    private static final class ConstructorReplacer extends JpqlVisitorAdapter<List<ParseTree>> {

        private final List<ParseTree> childNodes;

        private ConstructorReplacer(List<ParseTree> childNodes) {
            super(childNodes);
            this.childNodes = childNodes;
        }

        @Override
        public List<ParseTree> visitSelectionList(JpqlParser.SelectionListContext ctx) {
            return super.visitSelectionList(ctx);
        }

        /*
        public boolean visit(JpqlSelectExpressions node, List<ParserTree> nodes) {
            if (node.getChildCount() == 1) {
                List<ParserTree> constructorParameters = new ArrayList<ParserTree>();
                node.getChild(0).visit(this, constructorParameters);
                if (!constructorParameters.isEmpty()) {
                    remove(node.getChild(0));
                    JpqlParser.SelectExpression[] selectExpressions = new JpqlParser.SelectExpression[constructorParameters.size()];
                    for (int i = 0; i < selectExpressions.length; i++) {
                        selectExpressions[i] = createSelectExpression(constructorParameters.get(i));
                    }
                    appendChildren(node, selectExpressions);
                }
            }
            return false;
        }

        public boolean visit(JpqlParser.ConstructorExpressionContext node, List<ParserTree> parameters) {
            for (int i = 0; i < node.getChildCount(); i++) {
                parameters.add(node.getChild(i));
            }
            return false;
        }
        */
    }

    private static final class AppendWhereClauseVisitor extends AbstractParseTreeVisitor<ParseTree> {

        private final JpqlParser.WhereClauseContext whereClause;

        AppendWhereClauseVisitor(JpqlParser.WhereClauseContext whereClause) {
            this.whereClause = whereClause;
        }

        @Override
        public ParseTree visit(ParseTree tree) {
            ParseTree clause = whereClause.getChild(0);
            throw new UnsupportedOperationException();

            /*
            if (!(clause instanceof JpqlBrackets)) {
                clause = createBrackets(clause);
                clause.setParent(whereClause);
            }
            BaseContext and = createAnd(clause, node);
            and.setParent(whereClause);
            whereClause.setChild(and, 0);

            return whereClause;
            */
        }
    }

    private static class ReplaceParameters {

        private final Path oldPath;
        private final Path newPath;

        @java.beans.ConstructorProperties({"oldPath", "newPath"})
        ReplaceParameters(Path oldPath, Path newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        Path getOldPath() {
            return this.oldPath;
        }

        Path getNewPath() {
            return this.newPath;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ReplaceParameters)) {
                return false;
            }
            final ReplaceParameters other = (ReplaceParameters) o;
            final Object this$oldPath = this.getOldPath();
            final Object other$oldPath = other.getOldPath();
            if (this$oldPath == null ? other$oldPath != null : !this$oldPath.equals(other$oldPath)) {
                return false;
            }
            final Object this$newPath = this.getNewPath();
            final Object other$newPath = other.getNewPath();
            if (this$newPath == null ? other$newPath != null : !this$newPath.equals(other$newPath)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $oldPath = this.getOldPath();
            result = result * PRIME + ($oldPath == null ? 43 : $oldPath.hashCode());
            final Object $newPath = this.getNewPath();
            result = result * PRIME + ($newPath == null ? 43 : $newPath.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "QueryPreparator.ReplaceParameters(oldPath=" + this.getOldPath() + ", newPath=" + this.getNewPath() + ")";
        }
    }
}

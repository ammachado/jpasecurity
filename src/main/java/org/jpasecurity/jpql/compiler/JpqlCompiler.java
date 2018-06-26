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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.BaseContext;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.Trees;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParser.CollectionValuePathRootContext;
import org.jpasecurity.jpql.parser.JpqlParser.MainEntityPersisterReferenceContext;
import org.jpasecurity.jpql.parser.JpqlParser.MapEntryPathContext;
import org.jpasecurity.jpql.parser.JpqlParser.MapKeyPathRootContext;
import org.jpasecurity.jpql.parser.JpqlValueHolderVisitor;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;
import org.jpasecurity.util.ValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles a {@link JpqlParser.StatementContext} into a {@link JpqlCompiledStatement}.
 *
 * @author Arne Limburg
 */
public class JpqlCompiler {

    private static final Logger LOG = LoggerFactory.getLogger(JpqlCompiler.class);

    private final Metamodel metamodel;

    public JpqlCompiler(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    public JpqlCompiledStatement compile(JpqlParser.StatementContext statement) {
        return compile((BaseContext)statement);
    }

    public JpqlCompiledStatement compile(JpqlParser.SubQueryContext statement) {
        return compile((BaseContext)statement);
    }

    private JpqlCompiledStatement compile(BaseContext statement) {
        Class<?> constructorArgReturnType = getConstructorArgReturnType(statement);
        List<Path> selectedPathes = getSelectedPaths(statement);
        Set<TypeDefinition> typeDefinitions = getAliasDefinitions(statement);
        Set<String> namedParameters = getNamedParameters(statement);
        return new JpqlCompiledStatement(statement,
                constructorArgReturnType,
                selectedPathes,
                typeDefinitions,
                namedParameters);
    }

    public Class<?> getConstructorArgReturnType(BaseContext node) {
        if (node == null) {
            return null;
        }
        return new ConstructorArgReturnTypeVisitor(metamodel).visit(node).getValue();
    }

    public List<Path> getSelectedPaths(BaseContext node) {
        if (node == null) {
            return Collections.emptyList();
        }
        final List<Path> selectedPaths = new ArrayList<>();
        final SelectVisitor selectVisitor = new SelectVisitor(selectedPaths);
        for (int i = 0; i < node.getChildCount(); i++) {
            selectVisitor.visit(node.getChild(i));
        }
        return Collections.unmodifiableList(selectedPaths);
    }

    public Set<TypeDefinition> getAliasDefinitions(BaseContext node) {
        if (node == null) {
            return Collections.emptySet();
        }
        final Set<TypeDefinition> typeDefinitions = new HashSet<>();
        final AliasVisitor aliasVisitor = new AliasVisitor(typeDefinitions, metamodel);
        for (int i = 0; i < node.getChildCount(); i++) {
            aliasVisitor.visit(node.getChild(i));
        }
        return Collections.unmodifiableSet(typeDefinitions);
    }

    public Set<String> getNamedParameters(ParserRuleContext node) {
        if (node == null) {
            return Collections.emptySet();
        }
        final Set<String> namedParameters = new HashSet<>();
        final NamedParameterVisitor namedParameterVisitor = new NamedParameterVisitor(namedParameters);
        for (int i = 0; i < node.getChildCount(); i++) {
            namedParameterVisitor.visit(node.getChild(i));
        }
        return Collections.unmodifiableSet(namedParameters);
    }

    public Set<String> getPositionalParameters(BaseContext node) {
        if (node == null) {
            return Collections.emptySet();
        }
        Set<String> positionalParameters = new HashSet<>();
        final PositionalParameterVisitor positionalParameterVisitor
                = new PositionalParameterVisitor(positionalParameters);
        for (int i = 0; i < node.getChildCount(); i++) {
            positionalParameterVisitor.visit(node.getChild(i));
        }
        return Collections.unmodifiableSet(positionalParameters);
    }

    private static Class<?> toClass(String classname) throws ClassNotFoundException {
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(classname);
        }
    }

    private static Alias getAlias(BaseContext ctx) {
        if (ctx instanceof JpqlParser.MainEntityPersisterReferenceContext) {
            JpqlParser.MainEntityPersisterReferenceContext context = (JpqlParser.MainEntityPersisterReferenceContext)ctx;
            if (context.identificationVariableDef() == null) {
                throw new PersistenceException("Missing alias for type " + context.simplePathQualifier().toJpqlString());
            }
            return new Alias(context.identificationVariableDef().getText());
        }
        throw new PersistenceException("Missing alias for type " + ((BaseContext)ctx.getChild(0)).toJpqlString());
    }

    private static final class ConstructorArgReturnTypeVisitor extends JpqlValueHolderVisitor<Class<?>> {

        private final Metamodel metamodel;

        ConstructorArgReturnTypeVisitor(Metamodel metamodel) {
            this.metamodel = metamodel;
        }

        @Override
        public ValueHolder<Class<?>> visitMainEntityPersisterReference(
                JpqlParser.MainEntityPersisterReferenceContext ctx) {
            String entityClassName = ctx.simplePathQualifier().getText();

            Class<?> clazz;
            try {
                clazz = toClass(entityClassName);
                if (clazz.isInterface()) {
                    for (ManagedType managedType : metamodel.getManagedTypes()) {
                        if (clazz.isAssignableFrom(managedType.getJavaType())) {
                            defaultResult().setValue(clazz);
                        }
                    }
                    return stopVisitingChildren();
                } else {
                    defaultResult().setValue(clazz);
                    return stopVisitingChildren();
                }
            } catch (ClassNotFoundException ignored) {
                // Ignore if not found
            }

            for (EntityType entityType : metamodel.getEntities()) {
                if (entityType.getName().equals(entityClassName)) {
                    defaultResult().setValue(entityType.getJavaType());
                    return stopVisitingChildren();
                }
            }

            return defaultResult();
        }
    }

    private static final class SelectVisitor extends JpqlVisitorAdapter<List<Path>> {

        private final SelectPathVisitor selectPathVisitor;

        SelectVisitor(List<Path> value) {
            super(value);
            this.selectPathVisitor = new SelectPathVisitor(value);
        }

        @Override
        public List<Path> visitSelectExpression(JpqlParser.SelectExpressionContext node) {
            selectPathVisitor.visit(node);
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitConstructionParameter(JpqlParser.ConstructionParameterContext ctx) {
            selectPathVisitor.visit(ctx);
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitSubQuery(JpqlParser.SubQueryContext ctx) {
            return stopVisitingChildren();
        }
    }

    private static final class SelectPathVisitor extends JpqlVisitorAdapter<List<Path>> {

        private final QueryPreparator queryPreparator = new QueryPreparator();

        SelectPathVisitor(List<Path> value) {
            super(value);
        }

        @Override
        public List<Path> visitMainEntityPersisterReference(JpqlParser.MainEntityPersisterReferenceContext ctx) {
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitSimpleCaseStatement(JpqlParser.SimpleCaseStatementContext node) {
            List<ConditionalPath> conditionalPaths = new ArrayList<>();
            throw new UnsupportedOperationException();
            /*
            int start = 1isSimpleCase(node) ? 1 : 0;
            for (int i = start; i < node.getChildCount() - 1; i++) {
                this.visit(node.getChild(i));
            }

            BaseContext condition = getBaseContext(conditionalPaths);
            defaultResult().add(new ConditionalPath(node.getChild(node.getChildCount() - 1).toString(), condition));
            return stopVisitingChildren();
            */
        }

        @Override
        public List<Path> visitCoalesce(JpqlParser.CoalesceContext node) {
            List<ConditionalPath> conditionalPaths = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                JpqlParser.ExpressionContext childNode = node.expression(i);
                JpqlParser.IsNullPredicateContext conditionNode
                        = queryPreparator.createIsNotNull(TreeRewriteSupport.copy(childNode));
                conditionalPaths.add(new ConditionalPath(childNode.toString(), conditionNode));
            }

            throw new UnsupportedOperationException();
            /*
            BaseContext condition = getBaseContext(conditionalPaths);
            defaultResult().add(new ConditionalPath(node.getChild(node.getChildCount() - 1).getText(), condition));
            return stopVisitingChildren();
            */
        }

        /*
        private BaseContext getBaseContext(List<ConditionalPath> conditionalPaths) {
            BaseContext condition = null;
            for (ConditionalPath path: conditionalPaths) {
                if (condition == null) {
                    defaultResult().add(path);
                    condition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                } else {
                    BaseContext composedCondition
                            = queryPreparator.createAnd(condition, queryPreparator.createBrackets(path.getCondition()));
                    defaultResult().add(path.newCondition(composedCondition));
                    BaseContext newCondition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                    condition = queryPreparator.createAnd(condition, newCondition);
                }
            }
            return condition;
        }
        */

        @Override
        public List<Path> visitSimpleCaseWhen(JpqlParser.SimpleCaseWhenContext ctx) {
            defaultResult().add(extractConditionalPath(ctx));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitSearchedCaseWhen(JpqlParser.SearchedCaseWhenContext ctx) {
            defaultResult().add(extractConditionalPath(ctx));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitNullIf(JpqlParser.NullIfContext node) {
            JpqlParser.ExpressionContext child1 = TreeRewriteSupport.copy(node.expression(0));
            JpqlParser.ExpressionContext child2 = TreeRewriteSupport.copy(node.expression(1));
            defaultResult().add(
                    new ConditionalPath(child1.toString(), queryPreparator.createNotEquals(child1, child2)));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitMapEntryPath(MapEntryPathContext ctx) {
            Path entryPath = new Path(ctx.toString());
            defaultResult().add(new Path("ENTRY(" + ctx.mapReference().getText() + ")"));
            defaultResult().add(new Path("KEY(" + entryPath.getRootAlias().getName() + ")"));
            defaultResult().add(new Path("VALUE(" + entryPath.getRootAlias().getName() + ")"));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitMapKeyPathRoot(MapKeyPathRootContext ctx) {
            defaultResult().add(new Path("KEY(" + ctx.mapReference().getText() + ")"));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitCollectionValuePathRoot(CollectionValuePathRootContext ctx) {
            defaultResult().add(new Path("VALUE(" + ctx.collectionReference().getText() + ")"));
            defaultResult().add(new Path(ctx.getText()));
            return stopVisitingChildren();
        }

        @Override
        public List<Path> visitIdentificationVariable(JpqlParser.IdentificationVariableContext node) {
            defaultResult().add(new Path(node.toString()));
            return stopVisitingChildren();
        }

        private ConditionalPath extractConditionalPath(JpqlParser.SimpleCaseWhenContext node) {
            return new ConditionalPath(
                    node.getChild(1).getText(),
                    (JpqlParser.ExpressionContext)Trees.shallowCopy(node.expression(0))
            );
        }

        private ConditionalPath extractConditionalPath(JpqlParser.SearchedCaseWhenContext node) {
            return new ConditionalPath(
                    node.getChild(1).getText(),
                    (JpqlParser.ExpressionContext)Trees.shallowCopy(node.predicate())
            );
        }

        private static boolean isSimpleCase(JpqlParser.SimpleCaseWhenContext caseWhenContext) {
            return true;
        }

        private static boolean isSimpleCase(JpqlParser.SearchedCaseWhenContext caseWhenContext) {
            return false;
        }
    }

    private static final class AliasVisitor extends JpqlVisitorAdapter<Set<TypeDefinition>> {

        private final PathVisitor pathVisitor = new PathVisitor();

        private final Metamodel metamodel;

        AliasVisitor(Set<TypeDefinition> value, Metamodel metamodel) {
            super(value);
            this.metamodel = metamodel;
        }

        @Override
        public Set<TypeDefinition> visit(ParseTree tree) {
            final Set<TypeDefinition> typeDefinitions = super.visit(tree);
            determinePreliminaryTypes(typeDefinitions);
            return typeDefinitions;
        }

        @Override
        public Set<TypeDefinition> visitMainEntityPersisterReference(MainEntityPersisterReferenceContext ctx) {
            String entityClassName = ctx.simplePathQualifier().getText();
            Path path = pathVisitor.getPath(ctx);
            Alias alias = getAlias(ctx);

            Class<?> clazz;
            try {
                clazz = toClass(entityClassName);
                if (clazz.isInterface()) {
                    for (ManagedType managedType: metamodel.getManagedTypes()) {
                        if (clazz.isAssignableFrom(managedType.getJavaType())) {
                            defaultResult().add(new TypeDefinition(alias, managedType.getJavaType()));
                        }
                    }
                    return stopVisitingChildren();
                } else if (metamodel.entity(clazz) != null) {
                    defaultResult().add(new TypeDefinition(alias, clazz));
                    return stopVisitingChildren();
                }
            } catch (ClassNotFoundException ignored) {
                // Ignore if not found
            }

            try {
                clazz = TypeDefinition.Filter.managedTypeForPath(path).filter(defaultResult()).getJavaType();
                defaultResult().add(new TypeDefinition(alias, clazz));
                return stopVisitingChildren();
            } catch (TypeNotPresentException e) {
                // Ignore if not found
            }

            for (EntityType entityType: metamodel.getEntities()) {
                if (entityType.getName().equals(entityClassName)) {
                    defaultResult().add(new TypeDefinition(alias, entityType.getJavaType()));
                    return stopVisitingChildren();
                }
            }

            // type must be determined later
            defaultResult().add(new TypeDefinition(alias, null));
            return stopVisitingChildren();
        }

        @Override
        public Set<TypeDefinition> visitSelectionList(JpqlParser.SelectionListContext ctx) {
            if (ctx.selection().size() == 1) {
                return stopVisitingChildren();
            }
            Path path = pathVisitor.getPath(ctx);
            Alias alias = getAlias(ctx);
            Class<?> type;
            if (new CountVisitor().isCount(ctx)) {
                type = Long.class;
            } else {
                try {
                    type = TypeDefinition.Filter.managedTypeForPath(path).filter(defaultResult()).getJavaType();
                } catch (TypeNotPresentException e) {
                    type = null; // must be determined later
                }
            }
            defaultResult().add(new TypeDefinition(alias, type, path, path.hasSubpath(), false));
            return stopVisitingChildren();
        }

        @Override
        public Set<TypeDefinition> visitFromElementSpaceRoot(JpqlParser.FromElementSpaceRootContext node) {
            String abstractSchemaName = node.getChild(0).toString().trim();
            Alias alias = getAlias(node);
            Collection<Class<?>> types = new HashSet<>();
            EntityType<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(abstractSchemaName);
            if (entityType != null) {
                types.add(entityType.getJavaType());
            } else {
                try {
                    Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(abstractSchemaName);
                    for (ManagedType<?> managedTypes: ManagedTypeFilter.forModel(metamodel).filterAll(type)) {
                        types.add(managedTypes.getJavaType());
                    }
                } catch (ClassNotFoundException e) {
                    throw new PersistenceException("Managed type not found for type " + abstractSchemaName.trim());
                }
            }
            if (types.isEmpty()) {
                throw new PersistenceException("Managed type not found for type " + abstractSchemaName.trim());
            }
            for (Class<?> type: types) {
                defaultResult().add(new TypeDefinition(alias, type));
            }
            return stopVisitingChildren();
        }

        @Override
        public Set<TypeDefinition> visitQualifiedJoin(JpqlParser.QualifiedJoinContext ctx) {
            boolean innerJoin = ctx.LEFT() == null || ctx.OUTER() == null;
            boolean fetchJoin = ctx.FETCH() != null;
            Path fetchPath = new Path(ctx.getChild(0).toString());
            Class<?> keyType = null;
            Attribute<?, ?> attribute = TypeDefinition.Filter.attributeForPath(fetchPath)
                    .withMetamodel(metamodel)
                    .filter(defaultResult());
            Class<?> type;
            if (attribute instanceof MapAttribute) {
                MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>)attribute;
                keyType = mapAttribute.getKeyJavaType();
                type = mapAttribute.getBindableJavaType();
            } else {
                type = TypeDefinition.Filter.managedTypeForPath(fetchPath)
                        .withMetamodel(metamodel)
                        .filter(defaultResult())
                        .getJavaType();
            }
            if (keyType != null) {
                defaultResult().add(new TypeDefinition(keyType, fetchPath, innerJoin, fetchJoin));
            }
            if (ctx.getChildCount() == 1) {
                defaultResult().add(new TypeDefinition(type, fetchPath, innerJoin, fetchJoin));
            } else {
                Alias alias = getAlias(ctx);
                defaultResult().add(new TypeDefinition(alias, type, fetchPath, innerJoin, fetchJoin));
            }
            return stopVisitingChildren();
        }

        @Override
        public Set<TypeDefinition> visitSubQuery(JpqlParser.SubQueryContext ctx) {
            return stopVisitingChildren();
        }

        private Alias getAlias(BaseContext ctx) {
            if (ctx instanceof JpqlParser.MainEntityPersisterReferenceContext) {
                JpqlParser.MainEntityPersisterReferenceContext context
                        = (JpqlParser.MainEntityPersisterReferenceContext)ctx;
                if (context.identificationVariableDef() == null) {
                    throw new PersistenceException(
                            "Missing alias for type " + context.simplePathQualifier().toJpqlString());
                }
                return new Alias(context.identificationVariableDef().getText());
            }
            throw new PersistenceException(
                    "Missing alias for type " + ((BaseContext)ctx.getChild(0)).toJpqlString());
        }

        private void determinePreliminaryTypes(Set<TypeDefinition> typeDefinitions) {
            for (TypeDefinition typeDefinition: typeDefinitions) {
                if (typeDefinition.isPreliminary()) {
                    try {
                        Class<?> type;
                        if (typeDefinition.getJoinPath() != null) {
                            type = TypeDefinition.Filter.managedTypeForPath(typeDefinition.getJoinPath())
                                    .withMetamodel(metamodel)
                                    .filter(typeDefinitions)
                                    .getJavaType();
                            typeDefinition.setType(type);
                        } else {
                            for (EntityType<?> entityType: metamodel.getEntities()) {
                                if (entityType.getName().equals(typeDefinition.toString())) {
                                    typeDefinition.setType(entityType.getJavaType());
                                }
                            }
                        }
                    } catch (TypeNotPresentException e) {
                        // must be determined later
                    }
                }
            }
        }
    }

    private static final class NamedParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        NamedParameterVisitor(Set<String> namedParameters) {
            super(namedParameters);
        }

        @Override
        public Set<String> visitNamedParameter(JpqlParser.NamedParameterContext node) {
            defaultResult().add(node.getText());
            return defaultResult();
        }
    }

    private static final class PositionalParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        PositionalParameterVisitor(Set<String> namedParameters) {
            super(namedParameters);
        }

        @Override
        public Set<String> visitPositionalParameter(JpqlParser.PositionalParameterContext node) {
            defaultResult().add(node.getText());
            return defaultResult();
        }
    }

    private static final class PathVisitor extends JpqlValueHolderVisitor<Path> {

        @Override
        public ValueHolder<Path> visit(ParseTree tree) {
            defaultResult().setValue(null);
            return super.visit(tree);
        }

        @Override
        public ValueHolder<Path> visitMainEntityPersisterReference(MainEntityPersisterReferenceContext ctx) {
            defaultResult().setValue(new Path(ctx.simplePathQualifier().toJpqlString()));
            return stopVisitingChildren();
        }

        @Override
        public ValueHolder<Path> visitPathExpression(JpqlParser.PathExpressionContext ctx) {
            defaultResult().setValue(new Path(ctx.getText()));
            return stopVisitingChildren();
        }

        Path getPath(BaseContext node) {
            return this.visit(node).getValue();
        }
    }

    private static final class CountVisitor extends JpqlValueHolderVisitor<Boolean> {

        @Override
        public ValueHolder<Boolean> visit(ParseTree tree) {
            defaultResult().setValue(false);
            return super.visit(tree);
        }

        @Override
        public ValueHolder<Boolean> visitCountFunction(JpqlParser.CountFunctionContext ctx) {
            defaultResult().setValue(true);
            return defaultResult();
        }

        boolean isCount(BaseContext node) {
            return this.visit(node).getValue();
        }
    }
}

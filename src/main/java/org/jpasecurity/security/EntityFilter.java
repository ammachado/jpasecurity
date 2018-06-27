/*
 * Copyright 2008 - 2017 Arne Limburg
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

import static javax.persistence.metamodel.Attribute.PersistentAttributeType.BASIC;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;
import static org.jpasecurity.jpql.TypeDefinition.Filter.attributeForPath;
import static org.jpasecurity.jpql.TypeDefinition.Filter.typeForAlias;
import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;
import static org.jpasecurity.util.ReflectionUtils.getConstructor;
import static org.jpasecurity.util.ReflectionUtils.throwThrowable;
import static org.jpasecurity.util.Validate.notNull;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.BaseContext;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TreeRewriteSupport;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.TypeDefinition.Filter;
import org.jpasecurity.jpql.compiler.ConditionalPath;
import org.jpasecurity.jpql.compiler.JpqlCompiler;
import org.jpasecurity.jpql.compiler.NotEvaluatableException;
import org.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import org.jpasecurity.jpql.compiler.QueryEvaluator;
import org.jpasecurity.jpql.compiler.QueryEvaluatorImpl;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.compiler.SubQueryEvaluator;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the access control.
 *
 * It filters JPQL queries and can evaluate for a specific entity whether it is accessible or not.
 *
 * @author Arne Limburg
 * @author Stefan Hildebrandt
 */
public class EntityFilter implements AccessManager {

    private static final Logger LOG = LoggerFactory.getLogger(EntityFilter.class);

    protected final JpqlCompiler compiler;
    private final Metamodel metamodel;
    private final SecurePersistenceUnitUtil persistenceUnitUtil;
    private final QueryEvaluator queryEvaluator;
    private final Collection<AccessRule> accessRules;
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<>();
    private final QueryPreparator queryPreparator = new QueryPreparator();

    public EntityFilter(Metamodel metamodel,
                        SecurePersistenceUnitUtil util,
                        Collection<AccessRule> accessRules,
                        SubQueryEvaluator... evaluators) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.persistenceUnitUtil = notNull(SecurePersistenceUnitUtil.class, util);
        this.compiler = new JpqlCompiler(metamodel);
        this.queryEvaluator = new QueryEvaluatorImpl(compiler, util, evaluators);
        this.accessRules = accessRules;
    }

    public QueryPreparator getQueryPreparator() {
        return queryPreparator;
    }

    @Override
    public boolean isAccessible(AccessType accessType, Object entity) {
        EntityType<?> mapping = forModel(metamodel).filterEntity(entity.getClass());
        LOG.debug("Evaluating {} access for entity of type {}", accessType, mapping.getName());
        Alias alias = new Alias(Introspector.decapitalize(mapping.getName()));
        AccessDefinition accessDefinition = createAccessDefinition(alias, mapping.getJavaType(), accessType);
        LOG.debug("Using access definition {}", accessDefinition);
        QueryEvaluationParameters evaluationParameters
            = new QueryEvaluationParameters(metamodel,
                                            persistenceUnitUtil,
                                            Collections.singletonMap(alias, entity),
                                            accessDefinition.getQueryParameters(),
                                            Collections.<Integer, Object>emptyMap());
        try {
            return queryEvaluator.<Boolean>evaluate(accessDefinition.getAccessRules(), evaluationParameters);
        } catch (NotEvaluatableException e) {
            throw new SecurityException(e);
        }
    }

    public FilterResult<String> filterQuery(String query, AccessType accessType) {
        LOG.debug("Filtering query: {}", query);

        JpqlCompiledStatement statement = compile(query);
        AccessDefinition accessDefinition = createAccessDefinition(statement, accessType);
        FilterResult<String> filterResult = getAlwaysEvaluatableResult(statement, query, accessDefinition);
        if (filterResult != null) {
            return filterResult;
        }

        JpqlParser.WhereClauseContext where = statement.getWhereClause();
        if (where == null) {
            where = queryPreparator.createWhere(accessDefinition.getAccessRules());
            ParserRuleContext parent = statement.getFromClause().getParent();
            parent.children.add(where);
            where.setParent(parent);
        } else {
            JpqlParser.PredicateContext condition = where.predicate();
            if (!(condition instanceof JpqlParser.GroupedPredicateContext)) {
                condition = QueryPreparator.createBrackets(condition);
            }
            JpqlParser.AndPredicateContext and = QueryPreparator.createAnd(condition, accessDefinition.getAccessRules());
            and.setParent(where);
            TreeRewriteSupport.setChild(where, 0, and);
        }

        BaseContext statementNode = (BaseContext) statement.getStatement();
        LOG.debug("Optimizing filtered query: {}", statementNode.toJpqlString());

        optimize(accessDefinition);
        Set<String> parameterNames = compiler.getNamedParameters(accessDefinition.getAccessRules());
        Map<String, Object> parameters = accessDefinition.getQueryParameters();
        parameters.keySet().retainAll(parameterNames);
        if (statement.getConstructorArgReturnType() != null) {
            statementNode = queryPreparator.removeConstructor(statementNode);
        }
        final String optimizedJpqlStatement = statementNode.toJpqlString();
        LOG.debug("Returning optimized query {}", optimizedJpqlStatement);
        return new FilterResult<>(optimizedJpqlStatement,
                parameters.size() > 0 ? parameters : null,
                statement.getConstructorArgReturnType(),
                statement.getSelectedPaths(),
                statement.getTypeDefinitions());
    }

    protected AccessDefinition createAccessDefinition(JpqlCompiledStatement statement, AccessType accessType) {
        return createAccessDefinition(getSelectedEntityTypes(statement), accessType, getAliases(statement));
    }

    private AccessDefinition createAccessDefinition(Alias alias, Class<?> type, AccessType accessType) {
        return createAccessDefinition(
                Collections.<Path, Class<?>>singletonMap(alias.toPath(), type),
                accessType,
                Collections.<Alias>emptySet());
    }

    protected AccessDefinition createAccessDefinition(
            Map<Path, Class<?>> selectedTypes, AccessType accessType, Set<Alias> usedAliases) {
        AccessDefinition accessDefinition = null;
        boolean restricted = false;
        for (Map.Entry<Path, Class<?>> selectedType: selectedTypes.entrySet()) {
            Set<JpqlParser.AccessRuleContext> appliedRules = new HashSet<>();
            Set<Class<?>> restrictedTypes = new HashSet<>();
            AccessDefinition typedAccessDefinition = null;
            for (AccessRule accessRule: accessRules) {
                if (!appliedRules.contains(accessRule.getStatement())
                    && accessRule.isAssignable(selectedType.getValue(), metamodel)) {
                    restricted = true;
                    restrictedTypes.add(selectedType.getValue());
                    appliedRules.add((JpqlParser.AccessRuleContext)accessRule.getStatement());
                    if (accessRule.grantsAccess(accessType)) {
                        typedAccessDefinition = appendAccessDefinition(typedAccessDefinition,
                                                                       accessRule,
                                                                       selectedType.getKey(),
                                                                       usedAliases);
                    }
                }
            }
            Map<JpqlParser.AccessRuleContext, Set<AccessRule>> mayBeRules = new HashMap<>();
            for (AccessRule accessRule : accessRules) {
                if (!appliedRules.contains(accessRule.getStatement())) {
                    if (accessRule.mayBeAssignable(selectedType.getValue(), metamodel)) {
                        Set<AccessRule> accessRulesSet = mayBeRules.get(accessRule.getStatement());
                        if (accessRulesSet == null) {
                            accessRulesSet = new HashSet<>();
                            mayBeRules.put((JpqlParser.AccessRuleContext)accessRule.getStatement(), accessRulesSet);
                        }
                        accessRulesSet.add(accessRule);
                    }
                }
            }
            Set<AccessRule> bestMayBeRules = new HashSet<>();
            for (Set<AccessRule> accessRulesByStatement : mayBeRules.values()) {
                AccessRule bestRule = null;
                Class<?> bestRuleSelectedType = null;
                for (AccessRule accessRule : accessRulesByStatement) {
                    final Class<?> accessRuleSelectedType = accessRule.getSelectedType(metamodel);
                    //TODO remove filter mapped superclasses
                    if (bestRule == null) {
                        bestRule = accessRule;
                        bestRuleSelectedType = bestRule.getSelectedType(metamodel);
                    } else {
                        if (accessRuleSelectedType.isAssignableFrom(bestRuleSelectedType)) {
                            bestRule = accessRule;
                            bestRuleSelectedType = accessRuleSelectedType;
                        }
                    }
                }
                bestMayBeRules.add(bestRule);
            }
            for (AccessRule accessRule : bestMayBeRules) {
                if (accessRule.mayBeAssignable(selectedType.getValue(), metamodel)) {
                    restricted = true;
                    restrictedTypes.add(accessRule.getSelectedType(metamodel));
                    if (accessRule.grantsAccess(accessType)) {
                        Class<?> type = accessRule.getSelectedType(metamodel);
                        JpqlParser.PredicateContext instanceOf = null;
                        for (EntityType<?> entityType: forModel(metamodel).filterEntities(type)) {
                            JpqlParser.PredicateContext node = queryPreparator.createInstanceOf(selectedType.getKey(),
                                    entityType);
                            if (instanceOf == null) {
                                instanceOf = node;
                            } else {
                                instanceOf = QueryPreparator.createOr(instanceOf, node);
                            }
                        }
                        AccessDefinition preparedAccessRule
                            = prepareAccessRule(accessRule, selectedType.getKey(), usedAliases);
                        preparedAccessRule.mergeNode(instanceOf);
                        typedAccessDefinition = preparedAccessRule.append(typedAccessDefinition);
                    }
                }
            }
            if (restrictedTypes.size() > 0 && !restrictedTypes.contains(selectedType.getValue())) {
                JpqlParser.PredicateContext superClassNode = null;
                for (Class<?> restrictedType: restrictedTypes) {
                    JpqlParser.PredicateContext instanceOf = null;
                    for (EntityType<?> entityType: forModel(metamodel).filterEntities(restrictedType)) {
                        JpqlParser.PredicateContext node = queryPreparator.createInstanceOf(selectedType.getKey(),
                                entityType);
                        if (instanceOf == null) {
                            instanceOf = node;
                        } else {
                            instanceOf = QueryPreparator.createOr(instanceOf, node);
                        }
                    }
                    if (superClassNode == null) {
                        superClassNode = QueryPreparator.createNot(instanceOf);
                    } else {
                        superClassNode = QueryPreparator.createAnd(superClassNode,
                                                                   QueryPreparator.createNot(instanceOf));
                    }
                }
                if (typedAccessDefinition == null) {
                    typedAccessDefinition = new AccessDefinition(QueryPreparator.createBrackets(superClassNode));
                } else {
                    typedAccessDefinition.appendNode(superClassNode);
                    typedAccessDefinition.group();
                }
            }
            if (typedAccessDefinition != null && selectedType.getKey() instanceof ConditionalPath) {
                ConditionalPath path = (ConditionalPath)selectedType.getKey();
                JpqlParser.OrPredicateContext conditionalNode = QueryPreparator.createImplication((
                        JpqlParser.PredicateContext)path.getCondition(),
                        typedAccessDefinition.getAccessRules()
                );
                typedAccessDefinition.setAccessRules(QueryPreparator.createBrackets(conditionalNode));
            }
            if (accessDefinition == null) {
                accessDefinition = typedAccessDefinition;
            } else {
                accessDefinition.merge(typedAccessDefinition);
            }
        }
        if (accessDefinition == null) {
            return new AccessDefinition(queryPreparator.createBoolean(!restricted));
        } else {
            accessDefinition.setAccessRules(QueryPreparator.createBrackets(accessDefinition.getAccessRules()));
            return accessDefinition;
        }
    }

    protected <Q> FilterResult<Q> getAlwaysEvaluatableResult(JpqlCompiledStatement statement,
                                                             Q query,
                                                             AccessDefinition accessDefinition) {
        LOG.debug("Using access definition {}", accessDefinition);

        try {
            QueryEvaluationParameters evaluationParameters
                = new QueryEvaluationParameters(metamodel,
                                                persistenceUnitUtil,
                                                Collections.<Alias, Object>emptyMap(),
                                                accessDefinition.getQueryParameters(),
                                                Collections.<Integer, Object>emptyMap(),
                                                QueryEvaluationParameters.EvaluationType.GET_ALWAYS_EVALUATABLE_RESULT);
            boolean result = queryEvaluator.<Boolean>evaluate(accessDefinition.getAccessRules(), evaluationParameters);
            if (result) {
                LOG.debug("Access rules are always true for current user and roles. Returning unfiltered query");
                return new FilterResult<>(query, statement.getConstructorArgReturnType());
            } else {
                LOG.debug("Access rules are always false for current user and roles. Returning empty result");
                return new FilterResult<>(statement.getConstructorArgReturnType());
            }
        } catch (NotEvaluatableException e) {
            //access rules need to be applied then
            return null;
        }
    }

    protected void optimize(AccessDefinition accessDefinition) {
        QueryOptimizer optimizer = new QueryOptimizer(metamodel,
                                                      persistenceUnitUtil,
                                                      Collections.<Alias, Object>emptyMap(),
                                                      accessDefinition.getQueryParameters(),
                                                      Collections.<Integer, Object>emptyMap(),
                                                      queryEvaluator);
        optimizer.optimize(accessDefinition.getAccessRules());
    }

    private AccessDefinition appendAccessDefinition(AccessDefinition accessDefinition,
                                                    AccessRule accessRule,
                                                    Path selectedPath,
                                                    Set<Alias> usedAliases) {
        return prepareAccessRule(accessRule, selectedPath, usedAliases).append(accessDefinition);
    }

    private JpqlParser.PredicateContext appendNode(JpqlParser.PredicateContext accessRules,
                                                  JpqlParser.PredicateContext accessRule) {
        if (accessRules == null) {
            return accessRule;
        } else {
            return QueryPreparator.createOr(accessRules, accessRule);
        }
    }

    private AccessDefinition prepareAccessRule(AccessRule accessRule, Path selectedPath, Set<Alias> usedAliases) {
        if (accessRule.getWhereClause() == null) {
            return new AccessDefinition(queryPreparator.createBoolean(true));
        }
        accessRule = accessRule.clone();
        Set<Alias> ruleAliases = accessRule.getAliases();
        for (Alias usedAlias: usedAliases) {
            if (ruleAliases.contains(usedAlias)) {
                accessRule = replace(accessRule, usedAlias, getUnusedAlias(usedAlias, ruleAliases));
            }
        }
        Map<String, Object> queryParameters = new HashMap<>();
        expand(accessRule, queryParameters);
        JpqlParser.PredicateContext preparedAccessRule = QueryPreparator
                .createBrackets(accessRule.getWhereClause().predicate());
        queryPreparator.replace(preparedAccessRule, accessRule.getSelectedPath(), selectedPath);
        return new AccessDefinition(preparedAccessRule, queryParameters);
    }

    private JpqlCompiledStatement compile(String query) {
        JpqlCompiledStatement compiledStatement = statementCache.get(query);
        if (compiledStatement == null) {
            try {
                JpqlParser.StatementContext statement = JpqlParsingHelper.parseQuery(query);
                compiledStatement = compiler.compile(statement);
                statementCache.put(query, compiledStatement);
            } catch (RecognitionException | ParseCancellationException e) {
                throw new PersistenceException(e);
            }
        }
        return compiledStatement.clone();
    }

    private void expand(AccessRule accessRule, Map<String, Object> queryParameters) {
        SecurityContext securityContext = DefaultAccessManager.Instance.get().getContext();
        for (Alias alias: securityContext.getAliases()) {
            Collection<JpqlParser.ExplicitTupleInListContext> inNodes = accessRule.getInNodes(alias);
            if (inNodes.size() > 0) {
                expand(alias.getName(), inNodes, securityContext.getAliasValues(alias), queryParameters);
            } else {
                for (JpqlParser.IdentificationVariableContext identifier: accessRule
                        .getIdentificationVariableNodes(alias)) {
                    ParserRuleContext nodeToReplace = identifier;
                    if (nodeToReplace.getParent() instanceof JpqlParser.SimplePathContext) {
                        nodeToReplace = nodeToReplace.getParent();
                    }
                    TreeRewriteSupport.replace(nodeToReplace, queryPreparator.createNamedParameter(alias.getName()));
                }
                try {
                    DefaultAccessManager.Instance.get().delayChecks();
                    queryParameters.put(alias.getName(), securityContext.getAliasValue(alias));
                } finally {
                    DefaultAccessManager.Instance.get().checkNow();
                }
            }
        }
    }

    private void expand(String alias,
                        Collection<JpqlParser.ExplicitTupleInListContext> inNodes, //JpqlIn
                        Collection<Object> aliasValues,
                        Map<String, Object> queryParameters) {
        for (JpqlParser.ExplicitTupleInListContext inNode: inNodes) {
            if (aliasValues.size() == 0) {
                JpqlParser.InequalityPredicateContext notEquals = createOneNotEqualsOne();
                TreeRewriteSupport.replace(inNode, notEquals);
            } else {
                List<Object> parameterValues = new ArrayList<>(aliasValues);
                String parameterName = alias + "0";
                queryParameters.put(parameterName, parameterValues.get(0));

                JpqlParser.ExpressionContext parameter = queryPreparator.createNamedParameter(parameterName);

                if (inNode instanceof JpqlParser.ExplicitTupleInListContext) {
                    JpqlParser.PredicateContext parent = QueryPreparator
                            .createEquals(inNode.expression(0), parameter);
                    for (int i = 1; i < aliasValues.size(); i++) {
                        parameterName = alias + i;
                        queryParameters.put(parameterName, parameterValues.get(i));
                        parameter = queryPreparator.createNamedParameter(parameterName);
                        JpqlParser.EqualityPredicateContext node = QueryPreparator
                                .createEquals((JpqlParser.ExpressionContext)inNode.getChild(0), parameter);
                        parent = QueryPreparator.createOr(parent, node);
                    }
                    QueryPreparator.replace(inNode, QueryPreparator.createBrackets(parent));
                }
            }
        }
    }

    private Map<Path, Class<?>> getSelectedEntityTypes(JpqlCompiledStatement statement) {
        Set<TypeDefinition> typeDefinitions = statement.getTypeDefinitions();
        //ToDo: change to HashMap then moving to java 8
        Map<Path, Class<?>> selectedTypes = new LinkedHashMap<>();
        // first process all non-conditional paths
        for (Path selectedPath: statement.getSelectedPaths()) {
            if (!(selectedPath instanceof ConditionalPath)) {
                Path entityPath = getSelectedEntityPath(selectedPath, typeDefinitions);
                selectedTypes.put(entityPath, getSelectedType(entityPath, typeDefinitions));
            }
        }
        // then process all remaining conditional paths
        for (Path selectedPath: statement.getSelectedPaths()) {
            if (selectedPath instanceof ConditionalPath) {
                Path entityPath = getSelectedEntityPath(selectedPath, typeDefinitions);
                // just add it, if there is no non-conditional path
                if (!selectedTypes.containsKey(new Path(entityPath))) {
                    selectedTypes.put(entityPath, getSelectedType(entityPath, typeDefinitions));
                }
            }
        }
        return selectedTypes;
    }

    private Path getSelectedEntityPath(Path selectedPath, Set<TypeDefinition> typeDefinitions) {
        if (!selectedPath.hasParentPath()) {
            return selectedPath;
        }
        Set<PersistentAttributeType> basicAttributeTypes = EnumSet.of(BASIC, EMBEDDED, ELEMENT_COLLECTION);
        Attribute<?, ?> attribute
            = Filter.attributeForPath(selectedPath).withMetamodel(metamodel).filter(typeDefinitions);
        while (selectedPath.hasParentPath() && basicAttributeTypes.contains(attribute.getPersistentAttributeType())) {
            selectedPath = selectedPath.getParentPath();
            attribute = Filter.attributeForPath(selectedPath).withMetamodel(metamodel).filter(typeDefinitions);
        }
        return selectedPath;
    }

    private Class<?> getSelectedType(Path entityPath, Set<TypeDefinition> typeDefinitions) {
        if (entityPath.isKeyPath()) {
            TypeDefinition typeDefinition = typeForAlias(entityPath.getRootAlias())
                    .withMetamodel(metamodel)
                    .filter(typeDefinitions);
            MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>)attributeForPath(typeDefinition.getJoinPath())
                    .withMetamodel(metamodel)
                    .filter(typeDefinitions);
            Class<?> keyType = mapAttribute.getKeyJavaType();
            if (!entityPath.hasSubpath()) {
                return keyType;
            }
            return attributeForPath(new Path(entityPath.getSubpath()))
                    .withMetamodel(metamodel)
                    .withRootType(keyType)
                    .filter()
                    .getJavaType();
        } else if (entityPath.hasSubpath()) {
            SingularAttribute<?, ?> attribute = (SingularAttribute<?, ?>)attributeForPath(entityPath)
                    .withMetamodel(metamodel)
                    .filter(typeDefinitions);
            return attribute.getType().getJavaType();
        } else {
            return typeForAlias(entityPath.getRootAlias()).withMetamodel(metamodel).filter(typeDefinitions).getType();
        }
    }

    private Set<Alias> getAliases(JpqlCompiledStatement statement) {
        Set<Alias> aliases = new HashSet<>();
        for (TypeDefinition typeDefinition: statement.getTypeDefinitions()) {
            if (typeDefinition.getAlias() != null) {
                aliases.add(typeDefinition.getAlias());
            }
        }
        return aliases;
    }

    private Alias getUnusedAlias(Alias usedAlias, Set<Alias> ruleAliases) {
        int i = 0;
        Alias unusedAlias;
        do {
            unusedAlias = new Alias(usedAlias.getName() + i++);
        } while (ruleAliases.contains(unusedAlias));
        return unusedAlias;
    }

    private AccessRule replace(AccessRule rule, Alias source, Alias target) {
        TypeDefinition typeDefinition = rule.getTypeDefinition();
        if (typeDefinition.getAlias().equals(source)) {
            if (typeDefinition.getJoinPath() == null) {
                typeDefinition = new TypeDefinition(target, typeDefinition.getType());
            } else {
                typeDefinition = new TypeDefinition(
                        target,
                        typeDefinition.getKeyType(),
                        typeDefinition.getType(),
                        typeDefinition.getJoinPath(),
                        typeDefinition.isInnerJoin(),
                        typeDefinition.isFetchJoin());
            }
        }
        new ReplaceAliasVisitor(new AliasReplacement(source, target)).visit(rule.getStatement());
        return new AccessRule((JpqlParser.AccessRuleContext)rule.getStatement(), typeDefinition);
    }

    @Override
    public boolean isAccessible(AccessType accessType, String entityName, Object... constructorArgs) {
        try {
            Class<?> entityType = forModel(metamodel).filter(entityName).getJavaType();
            Object entity = getConstructor(entityType, constructorArgs).newInstance(constructorArgs);
            return isAccessible(accessType, entity);
        } catch (Exception e) {
            return throwThrowable(e);
        }
    }

    private JpqlParser.InequalityPredicateContext createOneNotEqualsOne() {
        JpqlParser.LiteralExpressionContext numberExpression1 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        JpqlParser.LiteralExpressionContext numberExpression2 = (JpqlParser.LiteralExpressionContext) JpqlParsingHelper
                .createParser("1").expression();
        return QueryPreparator.createNotEquals(numberExpression1, numberExpression2);
    }

    public static class AccessDefinition {

        private JpqlParser.PredicateContext accessRules;
        private Map<String, Object> queryParameters;

        AccessDefinition(JpqlParser.PredicateContext accessRules) {
            this(accessRules, new HashMap<String, Object>());
        }

        AccessDefinition(JpqlParser.PredicateContext accessRules, Map<String, Object> queryParameters) {
            if (accessRules == null) {
                throw new IllegalArgumentException("accessRules may not be null");
            }
            if (queryParameters == null) {
                throw new IllegalArgumentException("queryParameters may not be null");
            }
            this.accessRules = accessRules;
            this.queryParameters = queryParameters;
        }

        public JpqlParser.PredicateContext getAccessRules() {
            return accessRules;
        }

        public void setAccessRules(JpqlParser.PredicateContext accessRules) {
            if (accessRules == null) {
                throw new IllegalArgumentException("accessRules may not be null");
            }
            this.accessRules = accessRules;
        }

        public Map<String, Object> getQueryParameters() {
            return queryParameters;
        }

        public AccessDefinition append(AccessDefinition accessDefinition) {
            if (accessDefinition != null) {
                queryParameters.putAll(accessDefinition.getQueryParameters());
                appendNode(accessDefinition.getAccessRules());
            }
            return this;
        }

        public void appendNode(BaseContext node) {
            if (accessRules == null) {
                return;
            }
            final String predicate = String.format("%s OR %s", accessRules.toJpqlString(), node.toJpqlString());
            accessRules = JpqlParsingHelper.createParser(predicate).predicate();
        }

        public AccessDefinition merge(AccessDefinition accessDefinition) {
            if (accessDefinition != null) {
                queryParameters.putAll(accessDefinition.getQueryParameters());
                mergeNode(accessDefinition.getAccessRules());
            }
            return this;
        }

        public void mergeNode(JpqlParser.PredicateContext node) {
            accessRules = QueryPreparator.createAnd(node, accessRules);
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "[query=\"" + accessRules.toJpqlString() + "\", parameters=" + queryParameters.toString() + "]";
        }

        public void group() {
            accessRules = QueryPreparator.createBrackets(accessRules);
        }
    }

    protected enum Evaluatable {
        ALWAYS_TRUE, ALWAYS_FALSE, DEPENDING;
    }

    private static class ReplaceAliasVisitor extends JpqlVisitorAdapter<AliasReplacement> {

        ReplaceAliasVisitor(AliasReplacement value) {
            super(value);
        }

        @Override
        public AliasReplacement visitIdentificationVariable(JpqlParser.IdentificationVariableContext ctx) {
            if (ctx.value.equals(defaultResult().getSource().getName())) {
                ctx.value = defaultResult().getTarget().getName();
            }

            return stopVisitingChildren();
        }
    }

    static class AliasReplacement {

        private Alias source;
        private Alias target;

        AliasReplacement(Alias source, Alias target) {
            this.source = source;
            this.target = target;
        }

        Alias getSource() {
            return source;
        }

        Alias getTarget() {
            return target;
        }
    }
}

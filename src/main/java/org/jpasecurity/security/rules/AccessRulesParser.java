/*
 * Copyright 2016 - 2017 Arne Limburg
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
package org.jpasecurity.security.rules;

import static org.jpasecurity.util.Validate.notNull;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.jpasecurity.jpql.parser.ToStringVisitor;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlKeywords;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.Permit;
import org.jpasecurity.security.PermitAny;
import org.jpasecurity.util.ListHashMap;
import org.jpasecurity.util.ListMap;

public class AccessRulesParser {

    private static final Alias THIS_ALIAS = new Alias("this");
    private Metamodel metamodel;
    private SecurityContext securityContext;
    private AccessRulesProvider accessRulesProvider;
    private AccessRulesCompiler compiler;

    public AccessRulesParser(Metamodel metamodel,
                             SecurityContext securityContext,
                             AccessRulesProvider accessRulesProvider) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.securityContext = notNull(SecurityContext.class, securityContext);
        this.accessRulesProvider = notNull(AccessRulesProvider.class, accessRulesProvider);
        compiler = new AccessRulesCompiler(metamodel);
    }

    public Collection<AccessRule> parseAccessRules() {
        try {
            Set<AccessRule> rules = new HashSet<>();
            ListMap<Class<?>, Permit> permissions = parsePermissions();
            for (Map.Entry<Class<?>, List<Permit>> annotations: permissions.entrySet()) {
                for (Permit permission: annotations.getValue()) {
                    final JpqlParser.AccessRuleContext accessRule = createAccessRule(annotations.getKey(), permission);
                    final Collection<AccessRule> compiledAccessRules = compiler.compile(accessRule);
                    rules.addAll(compiledAccessRules);
                }
            }
            for (String rule: accessRulesProvider.getAccessRules()) {
                final JpqlParser.AccessRuleContext accessRule = JpqlParsingHelper.parseAccessRule(rule);
                final Collection<AccessRule> compiledAccessRules = compiler.compile(accessRule);
                rules.addAll(compiledAccessRules);
            }
            return rules;
        } catch (RecognitionException | ParseCancellationException e) {
            throw new PersistenceException(e);
        }
    }

    private JpqlParser.AccessRuleContext createAccessRule(Class<?> type, Permit permission) {
        Alias alias = new Alias(Introspector.decapitalize(type.getSimpleName()));
        Set<Alias> declaredAliases = new HashSet<>();
        JpqlParser.WhereClauseContext whereClause = null;
        if (permission.where().trim().length() > 0) {
            whereClause = JpqlParsingHelper.parseWhereClause("WHERE " + permission.where());
            alias = findUnusedAlias(whereClause, alias);
            appendAlias(whereClause, alias, declaredAliases);
        }
        StringBuilder rule = new StringBuilder("GRANT ");
        List<AccessType> access = Arrays.asList(permission.access());
        if (access.contains(AccessType.CREATE)) {
            rule.append("CREATE ");
        }
        if (access.contains(AccessType.READ)) {
            rule.append("READ ");
        }
        if (access.contains(AccessType.UPDATE)) {
            rule.append("UPDATE ");
        }
        if (access.contains(AccessType.DELETE)) {
            rule.append("DELETE ");
        }
        rule.append("ACCESS TO ");
        rule.append(type.getName()).append(' ');
        rule.append(alias);
        if (whereClause != null) {
            ToStringVisitor toStringVisitor = new ToStringVisitor();
            rule.append(' ').append(toStringVisitor.visit(whereClause));
        }
        return JpqlParsingHelper.parseAccessRule(rule.toString());
    }

    private ListMap<Class<?>, Permit> parsePermissions() {
        ListMap<Class<?>, Permit> permissions = new ListHashMap<>();
        for (ManagedType<?> managedType: metamodel.getManagedTypes()) {
            Class<?> type = managedType.getJavaType();
            Permit permit = type.getAnnotation(Permit.class);
            if (permit != null) {
                permissions.add(type, permit);
            }
            PermitAny permitAny = type.getAnnotation(PermitAny.class);
            if (permitAny != null) {
                permissions.addAll(type, Arrays.asList(permitAny.value()));
            }
        }
        return permissions;
    }

    private Alias findUnusedAlias(JpqlParser.WhereClauseContext whereClause, Alias alias) {
        AliasVisitor aliasVisitor = new AliasVisitor();
        Set<Alias> aliases = aliasVisitor.visit(whereClause);
        int i = 0;
        while (aliases.contains(alias) || JpqlKeywords.ALL.contains(alias.toString().toUpperCase())) {
            alias = new Alias(alias.getName() + i);
            i++;
        }
        return alias;
    }

    private void appendAlias(JpqlParser.WhereClauseContext whereClause, Alias alias, Set<Alias> declaredAliases) {
        PathVisitor pathVisitor = new PathVisitor(alias, declaredAliases);
        pathVisitor.visit(whereClause);
    }

    private static class AliasVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        AliasVisitor() {
            super(new HashSet<Alias>());
        }

        @Override
        public Set<Alias> visitIdentificationVariableDef(JpqlParser.IdentificationVariableDefContext ctx) {
            defaultResult().add(new Alias(ctx.getText()));
            return stopVisitingChildren();
        }

        Set<Alias> visitAlias(ParserRuleContext node) {
            if (node.getChildCount() == 2) {
                defaultResult().add(new Alias(node.getChild(1).getText().toLowerCase()));
            }
            return stopVisitingChildren();
        }
    }

    private static class PathVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        private final QueryPreparator queryPreparator = new QueryPreparator();
        private final Alias alias;

        PathVisitor(Alias alias, Set<Alias> declaredAliases) {
            super(declaredAliases);
            this.alias = alias;
        }

        @Override
        public Set<Alias> visitSubQuery(JpqlParser.SubQueryContext select) {
            AliasVisitor aliasVisitor = new AliasVisitor();
            aliasVisitor.visit(select);
            for (int i = 0; i < select.getChildCount(); i++) {
                aliasVisitor.visit(select.getChild(i));
            }
            return stopVisitingChildren();
        }

        /*
        @Override
        public Set<Alias> visitSimpleSubPath(JpqlParser.SimpleSubPathContext path) {
            Alias a = new Alias(path.getChild(0).getText().toLowerCase());
            if (THIS_ALIAS.equals(a)) {
                queryPreparator.replace(path.getChild(0), queryPreparator.createIdentificationVariable(alias));
            } else if (!defaultResult().contains(a)
                && (path.getChildCount() > 1 || (!securityContext.getAliases().contains(a)))
                && (!new Path(path.toString()).isEnumValue())) {
                queryPreparator.prepend(alias.toPath(), path);
            }
            return stopVisitingChildren();
        }
        */
    }
}

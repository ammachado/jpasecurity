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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.BaseContext;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlParser.AccessRuleContext;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;

/**
 * This class represents compiled JPA Security access rules.
 * It contains methods to access the structure of an access rule.
 * @author Arne Limburg
 */
public class AccessRule extends JpqlCompiledStatement {

    private Set<AccessType> access;
    private Set<Alias> aliases;

    public AccessRule(JpqlParser.AccessRuleContext rule, TypeDefinition typeDefinition) {
        super(
                rule,
                null,
                Collections.singletonList(typeDefinition.getAlias().toPath()),
                Collections.singleton(typeDefinition),
                Collections.<String>emptySet()
        );
    }

    public Path getSelectedPath() {
        return getSelectedPaths().get(0);
    }

    Class<?> getSelectedType(Metamodel metamodel) {
        return getSelectedTypes(metamodel).values().iterator().next();
    }

    TypeDefinition getTypeDefinition() {
        return getTypeDefinitions().iterator().next();
    }

    Collection<JpqlParser.IdentificationVariableContext> getIdentificationVariableNodes(Alias alias) {
        List<JpqlParser.IdentificationVariableContext> identificationVariableNodes = new ArrayList<>();
        visit(new IdentificationVariableVisitor(alias.getName(), identificationVariableNodes));
        return Collections.unmodifiableCollection(identificationVariableNodes);
    }

    Collection<JpqlParser.ExplicitTupleInListContext> getInNodes(Alias alias) {
        return Collections.unmodifiableCollection(visit(new InNodeVisitor(alias.getName())));
    }

    boolean isAssignable(Class<?> type, Metamodel metamodel) {
        if (type == null) {
            return false;
        }
        return getSelectedType(metamodel).isAssignableFrom(type);
    }

    /**
     * Returns <tt>true</tt>, if the specified type is a superclass of the selected type
     * of this access rule and so this rule may be assignable if the type of the concrete
     * entity is of the selected type or a subclass.
     */
    boolean mayBeAssignable(Class<?> type, Metamodel metamodel) {
        if (type == null) {
            return false;
        }
        return type.isAssignableFrom(getSelectedType(metamodel));
    }

    public Set<Alias> getAliases() {
        if (aliases == null) {
            Set<Alias> declaredAliases = new HashSet<>();
            visit(new AliasVisitor(declaredAliases));
            aliases = Collections.unmodifiableSet(declaredAliases);
        }
        return aliases;
    }

    public boolean grantsCreateAccess() {
        return grantsAccess(AccessType.CREATE);
    }

    public boolean grantsReadAccess() {
        return grantsAccess(AccessType.READ);
    }

    public boolean grantsUpdateAccess() {
        return grantsAccess(AccessType.UPDATE);
    }

    public boolean grantsDeleteAccess() {
        return grantsAccess(AccessType.DELETE);
    }

    public boolean grantsAccess(AccessType type) {
        return getAccess().contains(type);
    }

    @Override
    public AccessRule clone() {
        return (AccessRule)super.clone();
    }

    private Set<AccessType> getAccess() {
        if (access == null) {
            Set<AccessType> access = new HashSet<>();
            visit(new AccessVisitor(access));
            if (access.size() == 0) {
                access.addAll(Arrays.asList(AccessType.values()));
            }
            this.access = Collections.unmodifiableSet(access);
        }
        return access;
    }

    private static class AccessVisitor extends JpqlVisitorAdapter<Collection<AccessType>> {

        AccessVisitor(Collection<AccessType> value) {
            super(value);
        }

        @Override
        public Collection<AccessType> visitAccessRule(AccessRuleContext ctx) {
            if (ctx.CREATE() != null) {
                defaultResult().add(AccessType.CREATE);
            }

            if (ctx.READ() != null) {
                defaultResult().add(AccessType.CREATE);
            }

            if (ctx.UPDATE() != null) {
                defaultResult().add(AccessType.READ);
            }

            if (ctx.DELETE() != null) {
                defaultResult().add(AccessType.UPDATE);
            }

            return keepVisitingChildren();
        }
    }

    private static class AliasVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        AliasVisitor(Set<Alias> declaredAliases) {
            super(declaredAliases);
        }

        @Override
        public Set<Alias> visitFromElementSpace(JpqlParser.FromElementSpaceContext ctx) {
            if (ctx.getChildCount() == 2) {
                defaultResult().add(new Alias(ctx.getChild(1).getText().toLowerCase()));
            }
            return stopVisitingChildren();
        }

        @Override
        public Set<Alias> visitEntityName(JpqlParser.EntityNameContext ctx) {
            if (ctx.getChildCount() == 2) {
                defaultResult().add(new Alias(ctx.getChild(1).getText().toLowerCase()));
            }
            return stopVisitingChildren();
        }

        @Override
        public Set<Alias> visitDotIdentifierSequence(JpqlParser.DotIdentifierSequenceContext ctx) {
            if (ctx.getChildCount() == 2) {
                defaultResult().add(new Alias(ctx.getChild(1).getText().toLowerCase()));
            }
            return stopVisitingChildren();
        }
    }

    private static class IdentificationVariableVisitor
            extends JpqlVisitorAdapter<List<JpqlParser.IdentificationVariableContext>> {

        private final String identifier;

        IdentificationVariableVisitor(String identifier,
                                      List<JpqlParser.IdentificationVariableContext> identificationVariableNodes) {
            super(identificationVariableNodes);
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        @Override
        public List<JpqlParser.IdentificationVariableContext> visitIdentificationVariable(
                JpqlParser.IdentificationVariableContext ctx) {
            if (identifier.equals(ctx.getText().toLowerCase())) {
                defaultResult().add(ctx);
            }
            return keepVisitingChildren();
        }
    }

    private static class InNodeVisitor extends JpqlVisitorAdapter<Collection<JpqlParser.ExplicitTupleInListContext>> {

        private final String identifier;

        InNodeVisitor(String identifier) {
            super(new ArrayList<JpqlParser.ExplicitTupleInListContext>());
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        @Override
        public Collection<JpqlParser.ExplicitTupleInListContext> visitExplicitTupleInList(
                JpqlParser.ExplicitTupleInListContext ctx) {
            if (identifier.equals(((BaseContext)ctx.getChild(1)).toJpqlString().toLowerCase())) {
                defaultResult().add(ctx);
            }
            return keepVisitingChildren();
        }
    }
}

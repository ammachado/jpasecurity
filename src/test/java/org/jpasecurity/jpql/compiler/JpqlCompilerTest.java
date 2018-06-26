/*
 * Copyright 2009 - 2016 Arne Limburg
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.jpasecurity.model.MethodAccessTestBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Arne Limburg
 */
public class JpqlCompilerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Metamodel metamodel;

    private JpqlCompiler compiler;

    @Before
    public void initialize() throws NoSuchMethodException {
        EntityType methodAccessTestBeanType = mock(EntityType.class);
        BasicType intType = mock(BasicType.class);
        BasicType stringType = mock(BasicType.class);
        SingularAttribute idAttribute = mock(SingularAttribute.class);
        SingularAttribute nameAttribute = mock(SingularAttribute.class);
        SingularAttribute parentAttribute = mock(SingularAttribute.class);
        PluralAttribute childrenAttribute = mock(PluralAttribute.class);
        PluralAttribute relatedAttribute = mock(PluralAttribute.class);
        when(metamodel.getManagedTypes())
            .thenReturn(new HashSet<>(Collections.<ManagedType<?>>singletonList(methodAccessTestBeanType)));
        when(metamodel.getEntities())
            .thenReturn(new HashSet<>(Collections.<EntityType<?>>singletonList(methodAccessTestBeanType)));
        doReturn(methodAccessTestBeanType).when(metamodel).entity(MethodAccessTestBean.class);
        doReturn(methodAccessTestBeanType).when(metamodel).managedType(MethodAccessTestBean.class);
        when(methodAccessTestBeanType.getName()).thenReturn(MethodAccessTestBean.class.getSimpleName());
        when(methodAccessTestBeanType.getJavaType()).thenReturn(MethodAccessTestBean.class);
        when(methodAccessTestBeanType.getAttributes()).thenReturn(new HashSet<>(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute)));
        when(methodAccessTestBeanType.getAttribute("id")).thenReturn(idAttribute);
        when(methodAccessTestBeanType.getAttribute("name")).thenReturn(nameAttribute);
        when(methodAccessTestBeanType.getAttribute("parent")).thenReturn(parentAttribute);
        when(methodAccessTestBeanType.getAttribute("children")).thenReturn(childrenAttribute);
        when(methodAccessTestBeanType.getAttribute("related")).thenReturn(relatedAttribute);
        when(idAttribute.getName()).thenReturn("id");
        when(idAttribute.isCollection()).thenReturn(false);
        when(idAttribute.getType()).thenReturn(intType);
        when(idAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getId"));
        when(nameAttribute.getName()).thenReturn("name");
        when(nameAttribute.isCollection()).thenReturn(false);
        when(nameAttribute.getType()).thenReturn(stringType);
        when(nameAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getName"));
        when(parentAttribute.getName()).thenReturn("parent");
        when(parentAttribute.isCollection()).thenReturn(false);
        when(parentAttribute.getType()).thenReturn(methodAccessTestBeanType);
        when(parentAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getParent"));
        when(childrenAttribute.getName()).thenReturn("children");
        when(childrenAttribute.isCollection()).thenReturn(true);
        when(childrenAttribute.getElementType()).thenReturn(methodAccessTestBeanType);
        when(childrenAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getChildren"));
        when(relatedAttribute.getName()).thenReturn("related");
        when(relatedAttribute.isCollection()).thenReturn(true);
        when(relatedAttribute.getElementType()).thenReturn(methodAccessTestBeanType);
        when(relatedAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getRelated"));

        compiler = new JpqlCompiler(metamodel);
    }

    @Test
    public void selectedPathsForCount() {
        String statement = "SELECT COUNT(net) FROM org.jpasecurity.model.MethodAccessTestBean net";
        JpqlCompiledStatement compiledStatement = compiler.compile(JpqlParsingHelper.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("net", compiledStatement.getSelectedPaths().get(0).toString());
    }

    @Test
    public void selectedPathsForDistinct() {
        String statement = "SELECT DISTINCT tb1, tb2 FROM MethodAccessTestBean tb1, MethodAccessTestBean tb2";
        JpqlCompiledStatement compiledStatement = compiler.compile(JpqlParsingHelper.parseQuery(statement));
        assertEquals(2, compiledStatement.getSelectedPaths().size());
        assertEquals("tb1", compiledStatement.getSelectedPaths().get(0).toString());
        assertEquals("tb2", compiledStatement.getSelectedPaths().get(1).toString());
    }

    @Test
    public void selectedPathsForExists() {
        String statement = "SELECT tb FROM MethodAccessTestBean tb "
                         + "WHERE EXISTS(SELECT tb2 FROM MethodAccessTestBean tb2)";
        JpqlCompiledStatement compiledStatement = compiler.compile(JpqlParsingHelper.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("tb", compiledStatement.getSelectedPaths().get(0).toString());
    }

    @Test
    public void updateStatementAlias() {
        String statement = "Update MethodAccessTestBean tb set tb.beanName='horst' where tb.identifier=34";
        JpqlCompiledStatement compiledStatement = compiler.compile(JpqlParsingHelper.parseQuery(statement));
        assertEquals(1, compiledStatement.getTypeDefinitions().size());
        assertEquals("tb", compiledStatement.getTypeDefinitions().iterator().next().getAlias().getName());
    }

    @Test
    public void updateStatementMissingAlias() {
        expectedException.expect(PersistenceException.class);
        expectedException.expectMessage("Missing alias for type MethodAccessTestBean");
        String statement = "Update MethodAccessTestBean set beanName='horst' where identifier=34";
        compiler.compile(JpqlParsingHelper.parseQuery(statement));
    }
}

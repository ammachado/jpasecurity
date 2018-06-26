/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.persistence.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.jpql.parser.JpqlParsingHelper;
import org.jpasecurity.model.TestBean;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

/**
 * @author Arne Limburg
 */
public class CriteriaVisitorTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private DefaultAccessManager accessManager;

    @Mock
    private Metamodel metamodel;

    private AccessRulesCompiler compiler;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void initialize() {
        EntityType testBeanType = mock(EntityType.class);
        when(metamodel.getEntities()).thenReturn(Collections.<EntityType<?>>singleton(testBeanType));
        when(testBeanType.getName()).thenReturn(TestBean.class.getSimpleName());
        when(testBeanType.getJavaType()).thenReturn(TestBean.class);
        DefaultAccessManager.Instance.register(accessManager);

        compiler = new AccessRulesCompiler(metamodel);
        entityManagerFactory = Persistence.createEntityManagerFactory("hibernate");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestBean bean1 = new TestBean();
        TestBean bean2 = new TestBean();
        entityManager.persist(bean1);
        entityManager.persist(bean2);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @After
    public void unregisterAccessManager() {
        DefaultAccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void appendAccessRule() {
        AccessRule accessRule = compile("GRANT READ ACCESS TO TestBean testBean WHERE testBean.id = 1");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TestBean> query = criteriaBuilder.createQuery(TestBean.class);
        Root<TestBean> from = query.from(TestBean.class);
        from.alias("testBean");
        query.where(from.get("parent").isNull());
        CriteriaHolder criteriaHolder = new CriteriaHolder(query);
        CriteriaVisitor criteriaVisitor = new CriteriaVisitor(
                metamodel,
                entityManagerFactory.getCriteriaBuilder(),
                criteriaHolder
        );
        criteriaVisitor.visit(accessRule.getWhereClause());
        List<TestBean> result = entityManager.createQuery(query).getResultList();
        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().getId());
    }

    private AccessRule compile(String accessRule) {
        Collection<AccessRule> accessRules = compiler.compile(JpqlParsingHelper.parseAccessRule(accessRule));
        assertEquals(1, accessRules.size());
        return accessRules.iterator().next();
    }
}

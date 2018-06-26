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
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.FieldAccessAnnotationTestBean_;
import org.jpasecurity.model.SimpleEmbeddable;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaEntityFilterTest {

    public static final String USER = "user";

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("annotation-based-field-access");

    private CriteriaBuilder criteriaBuilder;
    private FieldAccessAnnotationTestBean accessibleBean;
    private FieldAccessAnnotationTestBean inaccessibleBean;

    @Before
    public void setUp() {
        TestSecurityContext.authenticate("admin", "admin");
        inaccessibleBean = new FieldAccessAnnotationTestBean("");
        accessibleBean = new FieldAccessAnnotationTestBean(USER, inaccessibleBean);
        accessibleBean.setSimpleEmbeddable(new SimpleEmbeddable("embedded-name"));
        ENTITY_MANAGER.persist(inaccessibleBean);
        ENTITY_MANAGER.persist(accessibleBean);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();
        criteriaBuilder = ENTITY_MANAGER.getCriteriaBuilder();
    }

    @After
    public void tearDown() {
        TestSecurityContext.authenticate("admin", "admin");
        ENTITY_MANAGER.remove(ENTITY_MANAGER.merge(accessibleBean));
        ENTITY_MANAGER.remove(ENTITY_MANAGER.merge(inaccessibleBean));
        ENTITY_MANAGER.getTransaction().commit();
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void noSelection() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        query.from(FieldAccessAnnotationTestBean.class);
        List<FieldAccessAnnotationTestBean> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
    }

    @Test
    public void simpleSelection() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean);
        List<FieldAccessAnnotationTestBean> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
    }

    @Test
    public void simpleSelectionWithBasicPath() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<String> query
            = criteriaBuilder.createQuery(String.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean.get(FieldAccessAnnotationTestBean_.name));
        List<String> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean.getBeanName(), beans.iterator().next());
    }

    @Test
    public void simpleSelectionWithEmbeddedPath() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<SimpleEmbeddable> query = criteriaBuilder.createQuery(SimpleEmbeddable.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean.get(FieldAccessAnnotationTestBean_.embeddable));
        List<SimpleEmbeddable> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean.getSimpleEmbeddable(), beans.iterator().next());
    }

    @Test
    public void aggregateSelection() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(criteriaBuilder.count(bean));
        List<Long> count = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, count.size());
        assertEquals(Long.valueOf(1), count.iterator().next());
    }

    @Test
    public void compountSelection() {
        TestSecurityContext.authenticate("admin", "admin");
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.multiselect(bean, bean.get(FieldAccessAnnotationTestBean_.parent));
        List<Tuple> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next().get(0));
        assertEquals(inaccessibleBean, beans.iterator().next().get(1));

        TestSecurityContext.authenticate(USER);
        beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertTrue(beans.isEmpty());
    }

    @Test
    public void compountSelectionWithBasicAndEmbeddedPath() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.multiselect(
                bean.get(FieldAccessAnnotationTestBean_.name), bean.get(FieldAccessAnnotationTestBean_.embeddable)
        );
        List<Tuple> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean.getBeanName(), beans.iterator().next().get(0));
        assertEquals(accessibleBean.getSimpleEmbeddable(), beans.iterator().next().get(1));
    }

    @Test
    public void condition() {
        TestSecurityContext.authenticate(USER);
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean);
        query.where(criteriaBuilder.equal(bean.get(FieldAccessAnnotationTestBean_.id), accessibleBean.getIdentifier()));
        List<FieldAccessAnnotationTestBean> beans = ENTITY_MANAGER.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
        query.where(
                criteriaBuilder.equal(bean.get(FieldAccessAnnotationTestBean_.id), inaccessibleBean.getIdentifier())
        );
        assertTrue(ENTITY_MANAGER.createQuery(query).getResultList().isEmpty());
    }
}

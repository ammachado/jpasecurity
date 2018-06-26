/*
 * Copyright 2014 - 2016 Stefan Hildebrandt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Stefan Hildebrandt
 */
public class EntityManagerFirstLevelCacheTest {

    public static final String USER = "user";

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("entity-lifecycle-test");

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);

        if (!ENTITY_MANAGER.getTransaction().isActive()) {
            ENTITY_MANAGER.beginTransaction();
        }
    }

    @After
    public void unauthenticate() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterFlush() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.flush();
        FieldAccessAnnotationTestBean entityByFind =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
        ENTITY_MANAGER.getTransaction().rollback();
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterCommit() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        FieldAccessAnnotationTestBean entityByFind =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
    }

    @Test
    public void removeEntityCreatedWithinSameTransaction() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.flush();
        ENTITY_MANAGER.remove(test);
        ENTITY_MANAGER.flush();
        FieldAccessAnnotationTestBean shouldNotExists =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
        ENTITY_MANAGER.getTransaction().rollback();
    }

    @Test
    public void removeEntityCreatedWithinSameLongRunningEntityManager() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();
        ENTITY_MANAGER.remove(test);
        ENTITY_MANAGER.getTransaction().commit();
        FieldAccessAnnotationTestBean shouldNotExists =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerCommitting() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        ENTITY_MANAGER.persist(child);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();
        ENTITY_MANAGER.createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        ENTITY_MANAGER.getTransaction().commit();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, fieldAccessAnnotationTestBean);
        assertNotNull(fieldAccessAnnotationTestBean);
        ENTITY_MANAGER.getTransaction().begin();
        ENTITY_MANAGER.remove(ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        ENTITY_MANAGER.getTransaction().commit();
        FieldAccessAnnotationTestBean shouldNotExists =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerFlushing() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.flush();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        ENTITY_MANAGER.persist(child);
        ENTITY_MANAGER.flush();
        ENTITY_MANAGER.createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        ENTITY_MANAGER.flush();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotNull(fieldAccessAnnotationTestBean);
        assertSame(test, fieldAccessAnnotationTestBean);
        ENTITY_MANAGER.flush();
        ENTITY_MANAGER.remove(ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        ENTITY_MANAGER.flush();
        FieldAccessAnnotationTestBean shouldNotExists =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        ENTITY_MANAGER.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        ENTITY_MANAGER.remove(fieldAccessAnnotationTestBean2);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();
        assertNull(
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        ENTITY_MANAGER.getTransaction().commit();
    }

    @Test
    public void removeEntityWithoutFlushingLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        ENTITY_MANAGER.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        ENTITY_MANAGER.remove(fieldAccessAnnotationTestBean2);
        assertNull(
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        ENTITY_MANAGER.getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectIsSameWithinOneEntityManager() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        ENTITY_MANAGER.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            ENTITY_MANAGER.getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        ENTITY_MANAGER.getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectNotSameAfterRemove() {
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        ENTITY_MANAGER.persist(test);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        ENTITY_MANAGER.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        ENTITY_MANAGER.remove(fieldAccessAnnotationTestBean1);
        ENTITY_MANAGER.getTransaction().commit();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            ENTITY_MANAGER.getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
    }
}

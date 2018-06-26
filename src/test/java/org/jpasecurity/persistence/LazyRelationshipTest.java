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
package org.jpasecurity.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.TestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class LazyRelationshipTest {

    public static final String USER = "user";

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("lazy-relationship");

    private int childId;
    private int parentId;

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);
        ENTITY_MANAGER.getTransaction().begin();
        TestBean testBean = new TestBean(USER);
        ENTITY_MANAGER.persist(testBean);
        TestBean child = new TestBean();
        child.setParent(testBean);
        ENTITY_MANAGER.persist(child);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        TestSecurityContext.authenticate(null);
        childId = child.getId();
        parentId = testBean.getId();
    }

    @After
    public void unauthenticate() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void accessChild() {
        TestSecurityContext.authenticate(USER);
        assertThat(ENTITY_MANAGER.find(TestBean.class, childId), is(not(nullValue())));
    }

    @Test
    public void flushBeforeFind() {
        TestSecurityContext.authenticate(USER);

        ENTITY_MANAGER.getTransaction().begin();
        TestBean child = ENTITY_MANAGER.find(TestBean.class, childId);
        ENTITY_MANAGER.find(TestBean.class, parentId);
        ENTITY_MANAGER.flush();
        ENTITY_MANAGER.getTransaction().rollback();
        assertFalse(child.isPreUpdate());
    }
}

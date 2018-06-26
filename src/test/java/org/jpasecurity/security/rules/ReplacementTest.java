/*
 * Copyright 2011 - 2017 Arne Limburg
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.persistence.PersistenceException;

import org.jpasecurity.Alias;
import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.MethodAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ReplacementTest {

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("grandparent-grandchild");

    private MethodAccessAnnotationTestBean grandparent;

    private MethodAccessAnnotationTestBean grandchild;

    @Before
    public void createTestData() {
        ENTITY_MANAGER.getTransaction().begin();
        grandparent = new MethodAccessAnnotationTestBean();
        ENTITY_MANAGER.persist(grandparent);
        MethodAccessAnnotationTestBean parent = new MethodAccessAnnotationTestBean();
        parent.setParent(grandparent);
        ENTITY_MANAGER.persist(parent);
        grandchild = new MethodAccessAnnotationTestBean();
        grandchild.setParent(parent);
        ENTITY_MANAGER.persist(grandchild);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();
        grandparent = ENTITY_MANAGER.find(MethodAccessAnnotationTestBean.class, grandparent.getId());
        ENTITY_MANAGER.clear();
    }

    @Test
    public void canUpdateGrandchild() {
        TestSecurityContext.register(new Alias("CURRENT_GRANDPARENT"), grandparent);

        ENTITY_MANAGER.getTransaction().begin();
        MethodAccessAnnotationTestBean foundGrandchild
            = ENTITY_MANAGER.find(MethodAccessAnnotationTestBean.class, grandchild.getId());
        foundGrandchild.setName("grandchild");
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        foundGrandchild = ENTITY_MANAGER.find(MethodAccessAnnotationTestBean.class, grandchild.getId());
        assertThat(foundGrandchild.getName(), is("grandchild"));
    }

    @Test
    public void cannotUpdateGrandparent() {
        TestSecurityContext.register(new Alias("CURRENT_GRANDPARENT"), grandparent);

        ENTITY_MANAGER.getTransaction().begin();
        MethodAccessAnnotationTestBean foundGrandparent
            = ENTITY_MANAGER.find(MethodAccessAnnotationTestBean.class, grandparent.getId());
        foundGrandparent.setName("grandparent");
        try {
            ENTITY_MANAGER.getTransaction().commit();
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertThat(e.getCause(), is(instanceOf(SecurityException.class)));
        }
    }
}

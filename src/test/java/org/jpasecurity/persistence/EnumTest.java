/*
 * Copyright 2010 - 2016 Arne Limburg
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.persistence.RollbackException;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.Task;
import org.jpasecurity.model.TaskStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EnumTest {

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("task");

    @Before
    public void createTestData() {
        ENTITY_MANAGER.getTransaction().begin();
        Task closedTask = new Task("Closed Task");
        ENTITY_MANAGER.persist(closedTask);
        ENTITY_MANAGER.persist(new Task("Open Task"));
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();
        closedTask.setStatus(TaskStatus.CLOSED);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();
    }

    @After
    public void closeEntityManager() {
        if (ENTITY_MANAGER.getTransaction().isActive()) {
            ENTITY_MANAGER.getTransaction().rollback();
        }
        ENTITY_MANAGER.close();
        ENTITY_MANAGER.getEntityManagerFactory().close();
    }

    @Test
    public void create() {
        ENTITY_MANAGER.getTransaction().begin();
        Task createdTask = new Task("Created Task");
        ENTITY_MANAGER.persist(createdTask);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        Task foundTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t WHERE t.name = 'Created Task'", Task.class)
                .getSingleResult();
        assertThat(foundTask, is(createdTask));
    }

    @Test
    public void createFails() {
        try {
            ENTITY_MANAGER.getTransaction().begin();
            Task createdTask = new Task("Created Closed Task");
            createdTask.setStatus(TaskStatus.CLOSED);
            ENTITY_MANAGER.persist(createdTask);
            ENTITY_MANAGER.getTransaction().commit();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertThat(ENTITY_MANAGER.getTransaction().getRollbackOnly(), is(true));
        }
    }

    @Test
    public void read() {
        Task foundTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();
        assertThat(foundTask.getName(), is("Open Task"));
    }

    @Test
    public void update() {
        Task updateTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        ENTITY_MANAGER.getTransaction().begin();
        updateTask.setStatus(TaskStatus.CLOSED);
        ENTITY_MANAGER.getTransaction().commit();

        assertThat(updateTask.getStatus(), is(TaskStatus.CLOSED));
    }

    @Test
    public void updateFails() {
        Task updateTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        try {
            ENTITY_MANAGER.getTransaction().begin();
            updateTask.setStatus(null);
            ENTITY_MANAGER.getTransaction().commit();
            fail("expected SecurityException");
        } catch (RollbackException e) {
            assertThat(e.getCause(), is(instanceOf(SecurityException.class)));
        }
    }

    @Test
    public void deleteClosed() {
        Task deleteTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        ENTITY_MANAGER.getTransaction().begin();
        deleteTask.setStatus(TaskStatus.CLOSED);
        ENTITY_MANAGER.remove(deleteTask);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        assertThat(ENTITY_MANAGER.createQuery("SELECT t FROM Task t").getResultList().size(), is(0));
    }

    @Test
    public void deleteNullStatus() {
        Task deleteTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        ENTITY_MANAGER.getTransaction().begin();
        deleteTask.setStatus(null);
        ENTITY_MANAGER.remove(deleteTask);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();

        assertThat(ENTITY_MANAGER.createQuery("SELECT t FROM Task t").getResultList().size(), is(0));
    }

    @Test
    public void deleteFails() {
        Task deleteTask = ENTITY_MANAGER.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        try {
            ENTITY_MANAGER.getTransaction().begin();
            ENTITY_MANAGER.remove(deleteTask);
            ENTITY_MANAGER.getTransaction().commit();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertThat(ENTITY_MANAGER.getTransaction().getRollbackOnly(), is(true));
        }
    }
}

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
package org.jpasecurity.acl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.acl.Acl;
import org.jpasecurity.model.acl.AclEntry;
import org.jpasecurity.model.acl.AclProtectedEntity;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.Privilege;
import org.jpasecurity.model.acl.Role;
import org.jpasecurity.model.acl.User;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AclSyntaxTest {

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("acl-model-nocache");

    private static User user;
    private static User user2;
    private static AclProtectedEntity entity;
    private static final int FULL_ACCESS_PRIVILEGE = -2;
    private static final int READ_ACCESS_PRIVILEGE = -1;

    @BeforeClass
    public static void createEntityManagerFactory() {
        TestSecurityContext.authenticate(FULL_ACCESS_PRIVILEGE, FULL_ACCESS_PRIVILEGE);
        ENTITY_MANAGER.getTransaction().begin();
        Privilege privilege1 = new Privilege();
        privilege1.setName("modifySomething");
        ENTITY_MANAGER.persist(privilege1);
        Privilege privilege2 = new Privilege();
        privilege2.setName("viewSomething");
        ENTITY_MANAGER.persist(privilege2);
        Group group = new Group();
        group.setName("USERS");
        group.getFullHierarchy().add(group);
        ENTITY_MANAGER.persist(group);
        Group group2 = new Group();
        group2.setName("ADMINS");
        group2.getFullHierarchy().add(group2);
        ENTITY_MANAGER.persist(group2);
        Role role = new Role();
        role.setName("Test Role");
        //role.setPrivileges(Arrays.asList(privilege1, privilege2));
        ENTITY_MANAGER.persist(role);
        user = new User();
        user.setGroups(Collections.singletonList(group));
        user.setRoles(Collections.singletonList(role));
        ENTITY_MANAGER.persist(user);
        user2 = new User();
        user2.setGroups(Collections.singletonList(group2));
        user2.setRoles(Collections.singletonList(role));
        ENTITY_MANAGER.persist(user);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.getTransaction().begin();

        Acl acl = new Acl();
        ENTITY_MANAGER.persist(acl);
        AclEntry entry = new AclEntry();
        entry.setAccessControlList(acl);
        acl.getEntries().add(entry);
        //entry.setPrivilege(privilege1);
        entry.setGroup(group);
        ENTITY_MANAGER.persist(entry);

        entity = new AclProtectedEntity();
        entity.setAccessControlList(acl);
        ENTITY_MANAGER.persist(entity);

        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.close();
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void queryAclProtectedEntity() {
        TestSecurityContext.authenticate(FULL_ACCESS_PRIVILEGE, FULL_ACCESS_PRIVILEGE);
        AclProtectedEntity entity = ENTITY_MANAGER
                .createQuery("select e from AclProtectedEntity e", AclProtectedEntity.class)
                .getSingleResult();
        assertNotNull(entity);
    }

    @Test
    public void queryAclProtectedEntityWithNoPrivileges() {
        TestSecurityContext.authenticate(user2.getId());
        try {
            ENTITY_MANAGER.createQuery("select e from AclProtectedEntity e", AclProtectedEntity.class)
                    .getSingleResult();
            fail();
        } catch (NoResultException e) {
            //expected
        }
    }

    @Test
    public void queryAclProtectedEntityWithReadAllPrivilege() {
        TestSecurityContext.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        final List<AclProtectedEntity> resultList = ENTITY_MANAGER
                    .createQuery("select e from AclProtectedEntity e", AclProtectedEntity.class)
                    .getResultList();
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test
    public void updateAclProtectedEntityWithReadAllPrivilege() {
        TestSecurityContext.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        transaction.begin();
        final AclProtectedEntity result = ENTITY_MANAGER.find(AclProtectedEntity.class, entity.getId());
        assertNotNull(result);
        result.setSomeProperty("OtherValue");
        try {
            transaction.commit();
            fail("Expect SecurityException");
        } catch (PersistenceException e) {
            assertEquals(SecurityException.class, e.getCause().getClass());
        }
    }

    @Test
    public void queryAclProtectedEntityWithFullAccessPrivilege() {
        TestSecurityContext.authenticate(user2.getId(), FULL_ACCESS_PRIVILEGE);
        final List<AclProtectedEntity> resultList = ENTITY_MANAGER
                    .createQuery("select e from AclProtectedEntity e", AclProtectedEntity.class)
                    .getResultList();
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test
    public void updateAclProtectedEntity() {
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        try {
            TestSecurityContext.authenticate(user.getId());
            transaction.begin();
            ENTITY_MANAGER.find(User.class, user.getId());
            AclProtectedEntity e = ENTITY_MANAGER.find(AclProtectedEntity.class, entity.getId());
            e.getAccessControlList().getEntries().size();
            e.setSomeProperty("test");
            transaction.commit();
        } catch (RollbackException e) {
            if (!(e.getCause() instanceof  SecurityException)) {
                fail("SecurityExpect exception!");
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ENTITY_MANAGER.close();
        }
    }

    @Test
    public void updateAclProtectedEntityNoAccess() {
        TestSecurityContext.authenticate(user2.getId());
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        try {
            transaction.begin();
            ENTITY_MANAGER.find(User.class, user.getId());
            AclProtectedEntity e = ENTITY_MANAGER.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test");
            transaction.commit();
            fail("Expect exception!");
        } catch (RollbackException e) {
            if (!(e.getCause() instanceof  SecurityException)) {
                fail("SecurityExpect exception!");
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ENTITY_MANAGER.close();
        }
    }

    @Test
    public void updateAclProtectedEntityNoAccessOnlyFullRead() {
        TestSecurityContext.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        try {
            transaction.begin();
            ENTITY_MANAGER.find(User.class, user.getId());
            AclProtectedEntity e = ENTITY_MANAGER.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test" + System.currentTimeMillis());
            transaction.commit();
            fail("Expect exception!");
        } catch (PersistenceException e) {
            assertEquals(SecurityException.class, e.getCause().getClass());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ENTITY_MANAGER.close();
        }
    }

    @Test
    public void updateAclProtectedEntityFullAccessPrivilege() {
        TestSecurityContext.authenticate(user2.getId(), FULL_ACCESS_PRIVILEGE);
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        try {
            transaction.begin();
            ENTITY_MANAGER.find(User.class, user.getId());
            AclProtectedEntity e = ENTITY_MANAGER.find(AclProtectedEntity.class, entity.getId());
            e.getAccessControlList().getEntries().size();
            e.setSomeProperty("test" + System.currentTimeMillis());
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ENTITY_MANAGER.close();
        }
    }
}

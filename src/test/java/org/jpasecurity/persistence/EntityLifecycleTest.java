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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityTransaction;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.User;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EntityLifecycleTest {

    public static final String USER = "user";

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("entity-lifecycle-test");

    @Test
    public void persist() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0);
        ENTITY_MANAGER.persist(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 0, 0, 0);
    }

    @Test
    public void cascadePersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();

        assertLifecycleCount(child, 0);
        ENTITY_MANAGER.persist(parent);

        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }

    @Test
    public void existingCascadePersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(parent);
        closeEntityManager();

        openEntityManager();

        parent = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        FieldAccessAnnotationTestBean child = createChild(parent);
        assertLifecycleCount(child, 0);

        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }

    @Test
    public void persistCascadeMerge() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(child);
        closeEntityManager();

        openEntityManager();
        child = ENTITY_MANAGER.merge(child);
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        parent.getChildBeans().add(child);
        child.setParentBean(parent);

        ENTITY_MANAGER.persist(parent);

        closeEntityManager();

        assertLifecycleCount(child, 0, 0, 1, 1);
    }

    @Test
    public void mergeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0);
        bean = ENTITY_MANAGER.merge(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 0, 0, 0);
    }

    @Test
    public void cascadeMergeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(parent);
        closeEntityManager();

        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild(parent);

        assertLifecycleCount(child, 0);

        parent = ENTITY_MANAGER.merge(parent);
        child = parent.getChildBeans().iterator().next();

        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }

    @Test
    public void remove() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();

        bean = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        assertLifecycleCount(bean, 0, 0, 0, 1);
        ENTITY_MANAGER.remove(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 0, 1, 0, 1);
    }

    @Test
    public void removeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0, 0, 0, 0);
        ENTITY_MANAGER.persist(bean);
        ENTITY_MANAGER.remove(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 1, 0, 0);
    }

    @Test
    public void cascadeRemoveNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();
        ENTITY_MANAGER.persist(parent);
        child = parent.getChildBeans().iterator().next();
        ENTITY_MANAGER.remove(parent);

        closeEntityManager();

        assertLifecycleCount(child, 1, 1, 0, 0);
    }

    @Test
    public void cascadeRemove() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();
        ENTITY_MANAGER.persist(parent);
        closeEntityManager();

        openEntityManager();

        parent = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        child = parent.getChildBeans().iterator().next();
        assertLifecycleCount(child, 0, 0, 0, 1);
        ENTITY_MANAGER.remove(parent);

        closeEntityManager();

        assertLifecycleCount(child, 0, 1, 0, 1);
    }

    @Test
    public void update() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();

        bean = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void updateNewReferenceInstanceWithOldId() {
        openEntityManager();
        FieldAccessAnnotationTestBean parentBean = new FieldAccessAnnotationTestBean(USER);
        FieldAccessAnnotationTestBean childBean = new FieldAccessAnnotationTestBean(USER, parentBean);
        ENTITY_MANAGER.persist(childBean);
        ENTITY_MANAGER.persist(parentBean);
        closeEntityManager();

        openEntityManager();

        childBean = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, childBean.getIdentifier());
        FieldAccessAnnotationTestBean newParentBeanWithSameIdentifier = new FieldAccessAnnotationTestBean(USER);
        newParentBeanWithSameIdentifier.setIdentifier(parentBean.getIdentifier());
        childBean.setParentBean(newParentBeanWithSameIdentifier);
        closeEntityManager();

        assertLifecycleCount(childBean, 0, 0, 0, 1);
    }

    @Test
    public void updateNullReference() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();
        bean = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void merge() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();

        bean = ENTITY_MANAGER.merge(bean);
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void mergeModified() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();

        createChild(bean);
        assertLifecycleCount(bean, 1, 0, 0, 0);
        bean = ENTITY_MANAGER.merge(bean);
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void commitCollectionChanges() {
        openEntityManager();
        User user = new User();
        ENTITY_MANAGER.persist(user);
        Group group1 = new Group();
        ENTITY_MANAGER.persist(group1);
        Group group2 = new Group();
        ENTITY_MANAGER.persist(group2);
        Group group3 = new Group();
        ENTITY_MANAGER.persist(group3);
        closeEntityManager();

        openEntityManager();
        final User user1 = ENTITY_MANAGER.find(User.class, user.getId());
        final List<Group> groups = user1.getGroups();
        final Group groupToAdd1 = ENTITY_MANAGER.find(Group.class, group1.getId());
        groups.add(groupToAdd1);
        closeEntityManager();
        openEntityManager();
        final User user2 = ENTITY_MANAGER.find(User.class, user.getId());
        assertEquals(1, user2.getGroups().size());
        closeEntityManager();
    }

    @Test
    public void commitReplacedCollection() {
        openEntityManager();
        User user = new User();
        ENTITY_MANAGER.persist(user);
        Group group1 = new Group();
        ENTITY_MANAGER.persist(group1);
        Group group2 = new Group();
        ENTITY_MANAGER.persist(group2);
        Group group3 = new Group();
        ENTITY_MANAGER.persist(group3);
        closeEntityManager();

        openEntityManager();
        final User user1 = ENTITY_MANAGER.find(User.class, user.getId());
        final ArrayList<Group> groupsReplacement = new ArrayList<>();
        user1.setGroups(groupsReplacement);
        final Group groupToAdd1 = ENTITY_MANAGER.find(Group.class, group1.getId());
        final Group groupToAdd2 = ENTITY_MANAGER.find(Group.class, group2.getId());
        groupsReplacement.add(groupToAdd1);
        groupsReplacement.add(groupToAdd2);
        closeEntityManager();
        openEntityManager();
        final User user2 = ENTITY_MANAGER.find(User.class, user.getId());
        assertEquals(groupsReplacement.size(), user2.getGroups().size());
        closeEntityManager();
    }

    @Test
    public void find() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();
        bean = ENTITY_MANAGER.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void query() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        ENTITY_MANAGER.persist(bean);
        closeEntityManager();

        openEntityManager();
        String query = "select b from FieldAccessAnnotationTestBean b";
        bean = ENTITY_MANAGER.createQuery(query, FieldAccessAnnotationTestBean.class).getSingleResult();
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Before
    public void login() {
        TestSecurityContext.authenticate(USER);
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    private static FieldAccessAnnotationTestBean createChild() {
        return createChild(new FieldAccessAnnotationTestBean(USER));
    }

    private static FieldAccessAnnotationTestBean createChild(FieldAccessAnnotationTestBean parent) {
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER);
        child.setParentBean(parent);
        parent.getChildBeans().add(child);
        return child;
    }

    private static void openEntityManager() {
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
    }

    private static void closeEntityManager() {
        final EntityTransaction transaction = ENTITY_MANAGER.getTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
        ENTITY_MANAGER.close();
    }

    private static void assertLifecycleCount(FieldAccessAnnotationTestBean bean, int count) {
        assertLifecycleCount(bean, count, count, count, count);
    }

    private static void assertLifecycleCount(FieldAccessAnnotationTestBean bean,
                                             int persistCount,
                                             int removeCount,
                                             int updateCount,
                                             int loadCount) {
        assertEquals("wrong prePersistCount", persistCount, bean.getPrePersistCount());
        assertEquals("wrong postPersistCount", persistCount, bean.getPostPersistCount());
        assertEquals("wrong preRemoveCount", removeCount, bean.getPreRemoveCount());
        assertEquals("wrong postRemoveCount", removeCount, bean.getPostRemoveCount());
        assertEquals("wrong preUpdateCount", updateCount, bean.getPreUpdateCount());
        assertEquals("wrong postUpdateCount", updateCount, bean.getPostUpdateCount());
        assertEquals("wrong postLoadCount", loadCount, bean.getPostLoadCount());
    }
}

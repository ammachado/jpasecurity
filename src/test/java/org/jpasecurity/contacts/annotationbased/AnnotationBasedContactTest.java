/*
 * Copyright 2017 Arne Limburg
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
package org.jpasecurity.contacts.annotationbased;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class AnnotationBasedContactTest {

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("annotation-based-contacts");

    private Contact marysContact;
    private Contact johnsContact;
    private Contact publicContact;
    private Contact rootContact;

    @Before
    public void insertTestData() {
        TestSecurityContext.authenticate("admin", "admin");
        if (!ENTITY_MANAGER.getTransaction().isActive()) {
            ENTITY_MANAGER.getTransaction().begin();
        }
        marysContact = new Contact("mary", "Marys contact");
        johnsContact = new Contact("john", "Johns contact");
        publicContact = new Contact("public", "public");
        rootContact = new Contact("root", "root contact");
        ENTITY_MANAGER.persist(marysContact);
        ENTITY_MANAGER.persist(johnsContact);
        ENTITY_MANAGER.persist(publicContact);
        ENTITY_MANAGER.persist(rootContact);
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.clear();
        TestSecurityContext.unauthenticate();
    }

    @After
    public void deleteTestData() {
        TestSecurityContext.authenticate("admin", "admin");
        if (!ENTITY_MANAGER.getTransaction().isActive()) {
            ENTITY_MANAGER.getTransaction().begin();
        }
        ENTITY_MANAGER.createQuery("DELETE FROM Contact contact", Contact.class).executeUpdate();
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.close();
        TestSecurityContext.unauthenticate();
    }

    @Test
    public void findAllIsFiltered() {
        List<Contact> result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(publicContact));

        TestSecurityContext.authenticate("admin", "admin");
        result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems(marysContact, johnsContact, publicContact, rootContact));

        TestSecurityContext.authenticate("john", "user");
        result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(johnsContact, publicContact));

        TestSecurityContext.authenticate("mary", "user");
        result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(marysContact, publicContact));
    }

    @Test
    public void findAllWithCriteria() {
        CriteriaBuilder cb = ENTITY_MANAGER.getCriteriaBuilder();
        CriteriaQuery<Contact> query = cb.createQuery(Contact.class);
        query.from(Contact.class);
        List<Contact> result = ENTITY_MANAGER.createQuery(query).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(publicContact));

        TestSecurityContext.authenticate("admin", "admin");
        query = cb.createQuery(Contact.class);
        query.from(Contact.class);
        result = ENTITY_MANAGER.createQuery(query).getResultList();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems(marysContact, johnsContact, publicContact, rootContact));

        TestSecurityContext.authenticate("john", "user");
        result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(johnsContact, publicContact));

        TestSecurityContext.authenticate("mary", "user");
        result = ENTITY_MANAGER.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(marysContact, publicContact));
    }
}

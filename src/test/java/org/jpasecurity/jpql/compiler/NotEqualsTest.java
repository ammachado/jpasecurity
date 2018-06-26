/*
 * Copyright 2008 Arne Limburg
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

import static org.junit.Assert.assertNotNull;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.contacts.model.User;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Michael Kotten
 */
public class NotEqualsTest {

    @Rule
    public final TestEntityManager entityManager = new TestEntityManager("not-equals");

    @Test(expected = SecurityException.class)
    public void testNotEquals() {
        assertNotNull(entityManager);
        entityManager.getTransaction().begin();
        User user = new User("user");
        // persisting must fail with SecurityException because of access rule using !=
        entityManager.persist(user);
    }
}

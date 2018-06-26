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
package org.jpasecurity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.dto.IdAndNameDto;
import org.jpasecurity.model.client.Client;
import org.jpasecurity.model.client.ClientOperationsTracking;
import org.jpasecurity.model.client.ClientProcessInstance;
import org.jpasecurity.model.client.ClientStaffing;
import org.jpasecurity.model.client.ClientStatus;
import org.jpasecurity.model.client.ClientTask;
import org.jpasecurity.model.client.Employee;
import org.jpasecurity.model.client.ProcessInstanceProcessTaskInstance;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/** @author Arne Limburg */
public class ClientTest {

    @ClassRule
    public static final TestEntityManager ENTITY_MANAGER = new TestEntityManager("client");

    private static final String EMAIL = "test@test.org";

    private static ClientStatus active = new ClientStatus("Active");

    private static ClientStatus closed = new ClientStatus("Closed");

    private static int clientId;

    private static int operationsTrackingId;

    private static ClientTask clientTaskPersisted;

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(EMAIL);
        Client parent = new Client("parentClient");
        Client client = new Client("originalClient");
        client.setParent(parent);
        Employee employee = new Employee(EMAIL);
        ClientStaffing clientStaffing = new ClientStaffing(client, employee);
        ClientOperationsTracking parentTracking = new ClientOperationsTracking();
        parent.setOperationsTracking(parentTracking);
        parentTracking.setClient(parent);
        ClientOperationsTracking tracking = new ClientOperationsTracking();

        // We create the ClientProcessInstance first
        ClientProcessInstance clientProcessInstance = new ClientProcessInstance(client, new Date(), "Client Process");

        // Then the Task that will be associated to it
        ClientTask clientTask = new ClientTask();
        clientTask.setAssignedEmployee(employee);
        clientTask.setDescription("Task Description");
        clientTask.setSequence(0);

        // Then we create the association (manyToMany) between the Instance and the Task
        ProcessInstanceProcessTaskInstance processInstanceProcessTaskInstance =
            new ProcessInstanceProcessTaskInstance(clientProcessInstance, clientTask);

        active = ENTITY_MANAGER.merge(active);
        closed = ENTITY_MANAGER.merge(closed);
        ENTITY_MANAGER.persist(parent);
        ENTITY_MANAGER.persist(parentTracking);
        ENTITY_MANAGER.persist(employee);
        ENTITY_MANAGER.persist(client);
        ENTITY_MANAGER.persist(clientStaffing);
        tracking.setClient(client);
        client.setOperationsTracking(tracking);
        ENTITY_MANAGER.persist(tracking);

        ENTITY_MANAGER.persist(clientProcessInstance);
        ENTITY_MANAGER.persist(clientTask);
        ENTITY_MANAGER.persist(processInstanceProcessTaskInstance);

        ENTITY_MANAGER.getTransaction().commit();

        ENTITY_MANAGER.close();

        clientId = client.getId();
        operationsTrackingId = tracking.getId();

        // Saved for the test
        clientTaskPersisted = clientTask;
        TestSecurityContext.authenticate(null);
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void access() {
        TestSecurityContext.authenticate(EMAIL);
        Client client = ENTITY_MANAGER.find(Client.class, clientId);
        assertNotNull(client);
        ENTITY_MANAGER.getTransaction().begin();
        client.setCurrentStatus(ENTITY_MANAGER.merge(active));
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.close();

        assertNotNull(ENTITY_MANAGER.find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongEmail() {
        TestSecurityContext.authenticate("wrong@email.org");
        assertNotNull(ENTITY_MANAGER.find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongStatus() {
        TestSecurityContext.authenticate(EMAIL);
        Client client = ENTITY_MANAGER.find(Client.class, clientId);
        assertNotNull(client);
        ENTITY_MANAGER.getTransaction().begin();
        ENTITY_MANAGER.createQuery("UPDATE Client c SET c.currentStatus = :status WHERE c.id = :id")
            .setParameter("id", clientId)
            .setParameter("status", ENTITY_MANAGER.merge(closed))
            .executeUpdate();
        ENTITY_MANAGER.getTransaction().commit();
        ENTITY_MANAGER.close();

        try {
            ENTITY_MANAGER.getTransaction().begin();
            Client foundClient = ENTITY_MANAGER.find(Client.class, clientId);
            foundClient.setAnotherProperty("new value");
            ENTITY_MANAGER.getTransaction().commit();
        } finally {
            if (ENTITY_MANAGER.getTransaction().isActive()) {
                ENTITY_MANAGER.getTransaction().rollback();
            }
            ENTITY_MANAGER.close();
            ENTITY_MANAGER.getTransaction().begin();
            ENTITY_MANAGER.createQuery("UPDATE Client c SET c.currentStatus = :status WHERE c.id = :id")
                .setParameter("id", clientId)
                .setParameter("status", ENTITY_MANAGER.merge(active))
                .executeUpdate();
            ENTITY_MANAGER.getTransaction().commit();
            ENTITY_MANAGER.close();
        }
    }

    @Test
    public void query() {
        TestSecurityContext.authenticate(EMAIL);
        List<Client> clients = ENTITY_MANAGER
                .createQuery("SELECT cl FROM Client cl WHERE cl.id = :id", Client.class)
                .setParameter("id", clientId)
                .getResultList();
        assertEquals(1, clients.size());
    }

    @Test
    public void queryOperationsTracking() {
        TestSecurityContext.authenticate(EMAIL);
        List<ClientOperationsTracking> tracking = ENTITY_MANAGER
            .createQuery("SELECT t FROM ClientOperationsTracking t WHERE t.id = :id",
                ClientOperationsTracking.class)
            .setParameter("id", operationsTrackingId)
            .getResultList();
        assertEquals(1, tracking.size());
    }

    @Test
    public void testProcessInstance() {
        TestSecurityContext.authenticate(EMAIL);
        assertEquals(clientId, clientTaskPersisted.getClient().getId().intValue());
    }

    @Test
    public void testIdAndNameDtoGetSingleResult() {
        TestSecurityContext.authenticate(EMAIL);
        List<IdAndNameDto> idAndNameDtoList = ENTITY_MANAGER
            .createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getResultList();
        assertEquals(1, idAndNameDtoList.size());
        assertEquals(IdAndNameDto.class, idAndNameDtoList.get(0).getClass());
        assertEquals(clientId, idAndNameDtoList.get(0).getId().intValue());

        IdAndNameDto dto
            = ENTITY_MANAGER.createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getSingleResult();
        assertEquals(clientId, dto.getId().intValue());
    }

    @Test
    public void testIdAndNameDtoGetResultList() {
        TestSecurityContext.authenticate(EMAIL);
        List<IdAndNameDto> idAndNameDtoList = ENTITY_MANAGER
            .createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getResultList();
        assertEquals(1, idAndNameDtoList.size());
        assertEquals(IdAndNameDto.class, idAndNameDtoList.get(0).getClass());
        assertEquals(clientId, idAndNameDtoList.get(0).getId().intValue());

        IdAndNameDto dto
            = ENTITY_MANAGER.createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getResultList().get(0);
        assertEquals(clientId, dto.getId().intValue());
    }
}

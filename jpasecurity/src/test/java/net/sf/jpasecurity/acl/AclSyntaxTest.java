package net.sf.jpasecurity.acl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.acl.Acl;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.model.acl.AclProtectedEntity;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.Privilege;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class AclSyntaxTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;
    
    public void setUp() {
       entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();
       Privilege privilege1 = new Privilege();
       privilege1.setName("MODIFY");
       entityManager.persist(privilege1);
       Privilege privilege2 = new Privilege();
       privilege2.setName("DELETE");
       entityManager.persist(privilege2);
       Group group = new Group();
       group.setName("USERS");
       entityManager.persist(group);
       TestAuthenticationProvider.authenticate(group, privilege1, privilege2);

       Acl acl = new Acl();
       entityManager.persist(acl);
       AclEntry entry = new AclEntry();
       entry.setAccessControlList(acl);
       entry.setPrivilege(privilege1);
       entry.setGroup(group);
       entityManager.persist(entry);
       AclProtectedEntity entity = new AclProtectedEntity();
       entity.setAccessControlList(acl);
       entityManager.persist(entity);
       entityManager.getTransaction().commit();
       entityManager.close();
   }
   
   public void testAccess() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       AclProtectedEntity entity = (AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       
   }
}

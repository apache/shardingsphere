package org.apache.shardingsphere.authority.provider.simple;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Optional;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.subject.SchemaAccessSubject;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.junit.Test;


public class AllPrivilegesProviderAlgorithmTest {

  @Test
  public void assertFindPrivileges() {
    AllPrivilegesPermittedAuthorityProviderAlgorithm authorityProviderAlgorithm = new AllPrivilegesPermittedAuthorityProviderAlgorithm();
    Optional<ShardingSpherePrivileges> shardingSpherePrivilegesOptional = authorityProviderAlgorithm
        .findPrivileges(new Grantee("TestUser", "testHost"));
    assertNotNull(shardingSpherePrivilegesOptional.get());
    assertTrue(shardingSpherePrivilegesOptional.get().hasPrivileges("testSchema"));
    assertTrue(shardingSpherePrivilegesOptional.get().hasPrivileges(Collections.emptyList()));
    assertTrue(shardingSpherePrivilegesOptional.get()
        .hasPrivileges(new SchemaAccessSubject("testSchema"), Collections.emptyList()));
  }

}

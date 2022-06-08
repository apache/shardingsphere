/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.authority.provider.natived.builder;

import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.TablePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StoragePrivilegeMergerTest {
    
    @Test
    public void assertPrivilegeMergeResult() {
        ShardingSphereUser user = new ShardingSphereUser("test", "test", "%");
        Map<ShardingSphereUser, Collection<NativePrivileges>> userPrivilegeMap = Collections.singletonMap(user, Collections.singleton(buildPrivileges()));
        Map<ShardingSphereUser, NativePrivileges> actual = StoragePrivilegeMerger.merge(userPrivilegeMap, "schema", Collections.singleton(buildRule()));
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(user).getAdministrativePrivileges().getPrivileges().isEmpty());
        assertTrue(actual.get(user).getDatabasePrivileges().getGlobalPrivileges().isEmpty());
        assertThat(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().size(), is(1));
        assertTrue(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getGlobalPrivileges().isEmpty());
        assertThat(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().size(), is(1));
        assertThat("TableName assert error.", actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("tbl").getTableName(), is("tbl"));
        assertThat(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("tbl").getPrivileges().size(), is(1));
        assertTrue(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("tbl").getPrivileges().contains(PrivilegeType.SELECT));
    }
    
    private NativePrivileges buildPrivileges() {
        Collection<PrivilegeType> tablePrivileges = Collections.singleton(PrivilegeType.SELECT);
        SchemaPrivileges schema0Privilege = new SchemaPrivileges("schema_0");
        schema0Privilege.getSpecificPrivileges().put("tbl_0", new TablePrivileges("tbl_0", tablePrivileges));
        schema0Privilege.getSpecificPrivileges().put("tbl_1", new TablePrivileges("tbl_1", tablePrivileges));
        SchemaPrivileges schema1Privilege = new SchemaPrivileges("schema_1");
        schema1Privilege.getSpecificPrivileges().put("tbl_2", new TablePrivileges("tbl_2", tablePrivileges));
        schema1Privilege.getSpecificPrivileges().put("tbl_3", new TablePrivileges("tbl_3", tablePrivileges));
        NativePrivileges result = new NativePrivileges();
        result.getDatabasePrivileges().getSpecificPrivileges().put("schema_0", schema0Privilege);
        result.getDatabasePrivileges().getSpecificPrivileges().put("schema_1", schema1Privilege);
        return result;
    }
    
    private DataNodeContainedRule buildRule() {
        DataNodeContainedRule result = mock(DataNodeContainedRule.class);
        when(result.findLogicTableByActualTable("tbl_0")).thenReturn(Optional.of("tbl"));
        when(result.findLogicTableByActualTable("tbl_1")).thenReturn(Optional.of("tbl"));
        when(result.findLogicTableByActualTable("tbl_2")).thenReturn(Optional.of("tbl"));
        when(result.findLogicTableByActualTable("tbl_3")).thenReturn(Optional.of("tbl"));
        return result;
    }
}

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

package org.apache.shardingsphere.authority.merge;

import org.apache.shardingsphere.authority.provider.natived.builder.StoragePrivilegeMerger;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.TablePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PrivilegeMergeTest {
    
    @Test
    public void assertPrivilegeMergeResult() {
        NativePrivileges privileges = buildPrivilege();
        ShardingSphereUser user = new ShardingSphereUser("test", "test", "%");
        Map<ShardingSphereUser, Collection<NativePrivileges>> privilegeMap = new HashMap();
        privilegeMap.put(user, Collections.singletonList(privileges));
        DataNodeContainedRule rule = buildShardingSphereRule();
        Map<ShardingSphereUser, NativePrivileges> result = StoragePrivilegeMerger.merge(privilegeMap, "schema", Collections.singletonList(rule));
        assertEquals(1, result.size());
        assertTrue(result.containsKey(user));
        assertTrue(result.get(user).getAdministrativePrivileges().getPrivileges().isEmpty());
        assertTrue(result.get(user).getDatabasePrivileges().getGlobalPrivileges().isEmpty());
        assertEquals(1, result.get(user).getDatabasePrivileges().getSpecificPrivileges().size());
        assertTrue(result.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getGlobalPrivileges().isEmpty());
        assertEquals(1, result.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().size());
        assertEquals("TableName assert error.", "test", result.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("test").getTableName());
        assertEquals(1, result.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("test").getPrivileges().size());
        assertTrue(result.get(user).getDatabasePrivileges().getSpecificPrivileges().get("schema").getSpecificPrivileges().get("test").getPrivileges().contains(PrivilegeType.SELECT));
    }
    
    private NativePrivileges buildPrivilege() {
        Collection<PrivilegeType> tablePrivileges = new LinkedList<>();
        tablePrivileges.add(PrivilegeType.SELECT);
        SchemaPrivileges schema1Privilege = new SchemaPrivileges("schema1");
        schema1Privilege.getSpecificPrivileges().put("table1", new TablePrivileges("table1", tablePrivileges));
        schema1Privilege.getSpecificPrivileges().put("table2", new TablePrivileges("table2", tablePrivileges));
        SchemaPrivileges schema2Privilege = new SchemaPrivileges("schema2");
        schema2Privilege.getSpecificPrivileges().put("table3", new TablePrivileges("table3", tablePrivileges));
        schema2Privilege.getSpecificPrivileges().put("table4", new TablePrivileges("table4", tablePrivileges));
        NativePrivileges result = new NativePrivileges();
        result.getDatabasePrivileges().getSpecificPrivileges().put("schema1", schema1Privilege);
        result.getDatabasePrivileges().getSpecificPrivileges().put("schema2", schema2Privilege);
        return result;
    }
    
    private DataNodeContainedRule buildShardingSphereRule() {
        DataNodeContainedRule result = mock(DataNodeContainedRule.class);
        when(result.findLogicTableByActualTable("table1")).thenReturn(Optional.of("test"));
        when(result.findLogicTableByActualTable("table2")).thenReturn(Optional.of("test"));
        when(result.findLogicTableByActualTable("table3")).thenReturn(Optional.of("test"));
        when(result.findLogicTableByActualTable("table4")).thenReturn(Optional.of("test"));
        return result;
    }
}

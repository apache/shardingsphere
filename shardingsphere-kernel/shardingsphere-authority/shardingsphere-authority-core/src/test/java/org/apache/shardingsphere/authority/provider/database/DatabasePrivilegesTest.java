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

package org.apache.shardingsphere.authority.provider.database;

import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.DatabasePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.TablePrivileges;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public final class DatabasePrivilegesTest {

    private static DatabasePrivileges privileges = new DatabasePrivileges();

    @Before
    public void setUp() {
        privileges = buildPrivilege();
    }

    @Test
    public void assertGetGlobalPrivileges() {
        assertThat(privileges.getGlobalPrivileges(), instanceOf(Collection.class));
        assertTrue(privileges.getGlobalPrivileges().isEmpty());
        privileges.getGlobalPrivileges().add(PrivilegeType.SELECT);
        assertTrue(privileges.getGlobalPrivileges().containsAll(Collections.singletonList(PrivilegeType.SELECT)));
        assertFalse(privileges.getGlobalPrivileges().containsAll(Collections.singletonList(PrivilegeType.DELETE)));
        privileges.getGlobalPrivileges().add(PrivilegeType.DELETE);
        assertTrue(privileges.getGlobalPrivileges().containsAll(Collections.singletonList(PrivilegeType.DELETE)));
    }

    @Test
    public void assertGetSpecificPrivileges() {
        assertThat(privileges.getSpecificPrivileges(), instanceOf(Map.class));
        assertThat(privileges.getSpecificPrivileges().get("schema1"), instanceOf(SchemaPrivileges.class));
        assertThat(privileges.getSpecificPrivileges().get("schema1").getSpecificPrivileges().get("table1"), instanceOf(TablePrivileges.class));
        assertTrue(privileges.getSpecificPrivileges().get("schema1").getSpecificPrivileges().get("table1").getPrivileges().containsAll(Collections.singletonList(PrivilegeType.SELECT)));
        assertFalse(privileges.getSpecificPrivileges().get("schema1").getSpecificPrivileges().get("table1").getPrivileges().containsAll(Collections.singletonList(PrivilegeType.DELETE)));
        assertTrue(privileges.getSpecificPrivileges().get("schema2").getSpecificPrivileges().get("table3").getPrivileges().containsAll(Collections.singletonList(PrivilegeType.DELETE)));
        assertFalse(privileges.getSpecificPrivileges().get("schema2").getSpecificPrivileges().get("table3").getPrivileges().containsAll(Collections.singletonList(PrivilegeType.UPDATE)));
    }

    @Test
    public void assertHasPrivileges() {
        assertTrue(privileges.hasPrivileges("schema1", "table1", Collections.singletonList(PrivilegeType.SELECT)));
        assertFalse(privileges.hasPrivileges("schema1", "table3", Collections.singletonList(PrivilegeType.SELECT)));
        assertTrue(privileges.hasPrivileges("schema2", "table3", Collections.singletonList(PrivilegeType.SELECT)));
        assertFalse(privileges.hasPrivileges("schema1", "table1", Collections.singletonList(PrivilegeType.DELETE)));
        assertFalse(privileges.hasPrivileges("schema1", "table2", Collections.singletonList(PrivilegeType.DELETE)));
        assertTrue(privileges.hasPrivileges("schema2", "table3", Collections.singletonList(PrivilegeType.DELETE)));
        privileges.getGlobalPrivileges().add(PrivilegeType.DELETE);
        assertTrue(privileges.hasPrivileges("schema1", "table1", Collections.singletonList(PrivilegeType.DELETE)));
        assertTrue(privileges.hasPrivileges("schema1", Collections.singletonList(PrivilegeType.DELETE)));
        assertTrue(privileges.hasPrivileges("schema2", Collections.singletonList(PrivilegeType.DELETE)));
        assertFalse(privileges.hasPrivileges("schema1", Collections.singletonList(PrivilegeType.UPDATE)));
        assertFalse(privileges.hasPrivileges("schema2", Collections.singletonList(PrivilegeType.UPDATE)));
        privileges.getGlobalPrivileges().add(PrivilegeType.UPDATE);
        assertTrue(privileges.hasPrivileges("schema1", Collections.singletonList(PrivilegeType.UPDATE)));
        assertTrue(privileges.hasPrivileges("schema2", Collections.singletonList(PrivilegeType.UPDATE)));
    }

    private DatabasePrivileges buildPrivilege() {
        Collection<PrivilegeType> tablePrivileges1 = new LinkedList<>();
        Collection<PrivilegeType> tablePrivileges2 = new LinkedList<>();
        tablePrivileges1.add(PrivilegeType.SELECT);
        tablePrivileges2.add(PrivilegeType.SELECT);
        tablePrivileges2.add(PrivilegeType.DELETE);
        SchemaPrivileges schema1Privilege = new SchemaPrivileges("schema1");
        schema1Privilege.getSpecificPrivileges().put("table1", new TablePrivileges("table1", tablePrivileges1));
        schema1Privilege.getSpecificPrivileges().put("table2", new TablePrivileges("table2", tablePrivileges1));
        SchemaPrivileges schema2Privilege = new SchemaPrivileges("schema2");
        schema2Privilege.getSpecificPrivileges().put("table3", new TablePrivileges("table3", tablePrivileges2));
        schema2Privilege.getSpecificPrivileges().put("table4", new TablePrivileges("table4", tablePrivileges2));
        DatabasePrivileges result = new DatabasePrivileges();
        result.getSpecificPrivileges().put("schema1", schema1Privilege);
        result.getSpecificPrivileges().put("schema2", schema2Privilege);
        return result;
    }
}

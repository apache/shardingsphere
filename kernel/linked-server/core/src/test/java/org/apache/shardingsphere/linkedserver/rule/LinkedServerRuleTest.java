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

package org.apache.shardingsphere.linkedserver.rule;

import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.linkedserver.config.LinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.config.rule.LinkedServerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedServerRuleTest {
    
    private LinkedServerRule rule;
    
    @BeforeEach
    void setUp() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources.dbo.Department", "t_department");
        tables.put("HumanResources.dbo.Employee", "t_employee");
        LinkedServerConfiguration server = new LinkedServerConfiguration("MyLinkedServer", "FIXTURE", tables);
        LinkedServerRuleConfiguration config = new LinkedServerRuleConfiguration(Arrays.asList(server));
        rule = new LinkedServerRule(config);
    }
    
    @Test
    void assertFindLogicalTable() {
        assertTrue(rule.findLogicalTable("MyLinkedServer", "HumanResources.dbo.Department").isPresent());
        assertThat(rule.findLogicalTable("MyLinkedServer", "HumanResources.dbo.Department").get(), is("t_department"));
    }
    
    @Test
    void assertFindLogicalTableWithCaseInsensitiveServerName() {
        assertTrue(rule.findLogicalTable("mylinkedserver", "HumanResources.dbo.Department").isPresent());
        assertThat(rule.findLogicalTable("mylinkedserver", "HumanResources.dbo.Department").get(), is("t_department"));
    }

    @Test
    void assertFindLogicalTableWithCaseDifferentTableIdentityReturnsEmpty() {
        assertFalse(rule.findLogicalTable("MyLinkedServer", "humanresources.dbo.department").isPresent());
    }
    
    @Test
    void assertFindLogicalTableWithNonExistentServer() {
        assertFalse(rule.findLogicalTable("NonExistent", "HumanResources.dbo.Department").isPresent());
    }
    
    @Test
    void assertFindLogicalTableWithNonExistentTable() {
        assertFalse(rule.findLogicalTable("MyLinkedServer", "NonExistent.dbo.NonExistent").isPresent());
    }
    
    @Test
    void assertDistinctCatalogSchemaWithSameTableName() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources.dbo.Department", "t_hr_department");
        tables.put("Sales.dbo.Department", "t_sales_department");
        LinkedServerConfiguration server = new LinkedServerConfiguration("MultiSchemaServer", "FIXTURE", tables);
        LinkedServerRule multiRule = new LinkedServerRule(new LinkedServerRuleConfiguration(Arrays.asList(server)));
        assertThat(multiRule.findLogicalTable("MultiSchemaServer", "HumanResources.dbo.Department").get(), is("t_hr_department"));
        assertThat(multiRule.findLogicalTable("MultiSchemaServer", "Sales.dbo.Department").get(), is("t_sales_department"));
    }
    
    @Test
    void assertFindDatabaseType() {
        assertTrue(rule.findDatabaseType("MyLinkedServer").isPresent());
        assertThat(rule.findDatabaseType("MyLinkedServer").get().getType(), is("FIXTURE"));
    }
    
    @Test
    void assertFindDatabaseTypeWithNonExistentServer() {
        assertFalse(rule.findDatabaseType("NonExistent").isPresent());
    }
    
    @Test
    void assertContainsServer() {
        assertTrue(rule.containsServer("MyLinkedServer"));
        assertFalse(rule.containsServer("NonExistent"));
    }
    
    @Test
    void assertGetConfiguration() {
        assertThat(rule.getConfiguration().getServers().size(), is(1));
    }
    
    @Test
    void assertEmptyConfiguration() {
        LinkedServerRule emptyRule = new LinkedServerRule(new LinkedServerRuleConfiguration(Collections.emptyList()));
        assertFalse(emptyRule.containsServer("Any"));
        assertFalse(emptyRule.findLogicalTable("Any", "Any").isPresent());
    }
    
    @Test
    void assertEnhancedTableNamesEmpty() {
        TableMapperRuleAttribute attribute = rule.getAttributes().getAttribute(TableMapperRuleAttribute.class);
        assertTrue(attribute.getEnhancedTableNames().isEmpty());
        assertFalse(attribute.getLogicTableNames().isEmpty());
    }
    
    @Test
    void assertBareTableNameRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("Department", "t_department");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertTwoPartTableNameRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("dbo.Department", "t_department");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertOmittedSchemaRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources..Department", "t_department");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertSurplusComponentsRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("server.catalog.dbo.Department", "t_department");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertNullLogicalTableNameRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources.dbo.Department", null);
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertEmptyLogicalTableNameRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources.dbo.Department", "");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertCaseEquivalentTableKeysRejected() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("HumanResources.dbo.Department", "t_hr_department");
        tables.put("humanresources.dbo.department", "t_hr_dept_lower");
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerConfiguration("Server", "FIXTURE", tables));
    }
    
    @Test
    void assertCaseEquivalentServerNamesRejected() {
        Map<String, String> tablesA = new LinkedHashMap<>();
        tablesA.put("HumanResources.dbo.Department", "t_department_a");
        LinkedServerConfiguration serverA = new LinkedServerConfiguration("MyServer", "FIXTURE", tablesA);
        Map<String, String> tablesB = new LinkedHashMap<>();
        tablesB.put("Sales.dbo.Order", "t_order_b");
        LinkedServerConfiguration serverB = new LinkedServerConfiguration("myserver", "FIXTURE", tablesB);
        assertThrows(IllegalArgumentException.class, () -> new LinkedServerRule(new LinkedServerRuleConfiguration(Arrays.asList(serverA, serverB))));
    }
}

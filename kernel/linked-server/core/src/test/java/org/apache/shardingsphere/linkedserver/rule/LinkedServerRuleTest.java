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
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedServerRuleTest {
    
    private LinkedServerRule rule;
    
    @BeforeEach
    void setUp() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("Department", "t_department");
        tables.put("Employee", "t_employee");
        LinkedServerConfiguration server = new LinkedServerConfiguration("MyLinkedServer", "FIXTURE", tables);
        LinkedServerRuleConfiguration config = new LinkedServerRuleConfiguration(Arrays.asList(server));
        rule = new LinkedServerRule(config);
    }
    
    @Test
    void assertFindLogicalTable() {
        assertTrue(rule.findLogicalTable("MyLinkedServer", "Department").isPresent());
        assertThat(rule.findLogicalTable("MyLinkedServer", "Department").get(), is("t_department"));
    }
    
    @Test
    void assertFindLogicalTableCaseInsensitive() {
        assertTrue(rule.findLogicalTable("mylinkedserver", "department").isPresent());
        assertThat(rule.findLogicalTable("mylinkedserver", "department").get(), is("t_department"));
    }
    
    @Test
    void assertFindLogicalTableWithNonExistentServer() {
        assertFalse(rule.findLogicalTable("NonExistent", "Department").isPresent());
    }
    
    @Test
    void assertFindLogicalTableWithNonExistentTable() {
        assertFalse(rule.findLogicalTable("MyLinkedServer", "NonExistent").isPresent());
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
}

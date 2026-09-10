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

package org.apache.shardingsphere.linkedserver.yaml.swapper;

import org.apache.shardingsphere.linkedserver.config.LinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.config.rule.LinkedServerConfiguration;
import org.apache.shardingsphere.linkedserver.constant.LinkedServerOrder;
import org.apache.shardingsphere.linkedserver.yaml.config.YamlLinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.yaml.config.rule.YamlLinkedServerConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlLinkedServerRuleConfigurationSwapperTest {
    
    private final YamlLinkedServerRuleConfigurationSwapper swapper = new YamlLinkedServerRuleConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("Department", "t_department");
        LinkedServerConfiguration server = new LinkedServerConfiguration("MyServer", "SQLServer", tables);
        LinkedServerRuleConfiguration config = new LinkedServerRuleConfiguration(Arrays.asList(server));
        YamlLinkedServerRuleConfiguration result = swapper.swapToYamlConfiguration(config);
        assertThat(result.getServers().size(), is(1));
        assertTrue(result.getServers().containsKey("MyServer"));
        YamlLinkedServerConfiguration yamlServer = result.getServers().get("MyServer");
        assertThat(yamlServer.getDatabaseType(), is("SQLServer"));
        assertThat(yamlServer.getTables().get("Department"), is("t_department"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlLinkedServerConfiguration yamlServer = new YamlLinkedServerConfiguration();
        yamlServer.setDatabaseType("SQLServer");
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("Department", "t_department");
        yamlServer.setTables(tables);
        YamlLinkedServerRuleConfiguration yamlConfig = new YamlLinkedServerRuleConfiguration();
        yamlConfig.getServers().put("MyServer", yamlServer);
        LinkedServerRuleConfiguration result = swapper.swapToObject(yamlConfig);
        assertThat(result.getServers().size(), is(1));
        LinkedServerConfiguration server = result.getServers().iterator().next();
        assertThat(server.getName(), is("MyServer"));
        assertThat(server.getDatabaseType(), is("SQLServer"));
        assertThat(server.getTables().get("Department"), is("t_department"));
    }
    
    @Test
    void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("LINKED_SERVER"));
    }
    
    @Test
    void assertGetOrder() {
        assertThat(swapper.getOrder(), is(LinkedServerOrder.ORDER));
    }
    
    @Test
    void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), is(LinkedServerRuleConfiguration.class));
    }
}

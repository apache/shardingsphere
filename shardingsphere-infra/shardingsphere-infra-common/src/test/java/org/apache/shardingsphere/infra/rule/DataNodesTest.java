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

package org.apache.shardingsphere.infra.rule;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingRule;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingSphereRule;
import org.apache.shardingsphere.infra.rule.fixture.TestTableRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DataNodesTest {
    
    private final String logicTableName1 = "user";
    
    private final String logicTableName2 = "dept";
    
    private final List<String> dataSourceNames1 = Arrays.asList("master_db_1", "master_db_2", "slave_db_1", "slave_db_2");
    
    private final List<String> dataSourceNames2 = Arrays.asList("master_db_3", "slave_db_3");
    
    @Test(expected = NullPointerException.class)
    public void assertWrongTable() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        dataNodes.getDataNodes("wrongTableName");
    }
    
    @Test
    public void assertGetEmpty() {
        DataNodes nonRoutedRuleDataNodes = getNonRoutedRuleDataNodes();
        assertThat(nonRoutedRuleDataNodes.getDataNodes("tableName"), is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodes() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        assertEquals(dataNodes.getDataNodes(logicTableName1), getExpectedDataNodes(dataSourceNames1, logicTableName1));
        assertEquals(dataNodes.getDataNodes(logicTableName2), getExpectedDataNodes(dataSourceNames2, logicTableName2));
    }
    
    @Test
    public void assertGetDataNodeGroups() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        assertEquals(dataNodes.getDataNodeGroups(logicTableName1), getExpectedDataNodeGroups(dataSourceNames1, logicTableName1));
        assertEquals(dataNodes.getDataNodeGroups(logicTableName2), getExpectedDataNodeGroups(dataSourceNames2, logicTableName2));
    }
    
    private DataNodes getRoutedRuleDataNodes() {
        TestTableRule tableRule1 = new TestTableRule(dataSourceNames1, logicTableName1);
        TestTableRule tableRule2 = new TestTableRule(dataSourceNames2, logicTableName2);
        List<TestTableRule> tableRules = Arrays.asList(tableRule1, tableRule2);
        ShardingSphereRule rule = new TestShardingRule(tableRules);
        return new DataNodes(Collections.singleton(rule));
    }
    
    private DataNodes getNonRoutedRuleDataNodes() {
        return new DataNodes(Collections.singleton(new TestShardingSphereRule()));
    }
    
    private LinkedList<DataNode> getExpectedDataNodes(final List<String> dataSourceNames, final String logicTableName) {
        LinkedList<DataNode> result = new LinkedList<>();
        for (String each : dataSourceNames) {
            result.add(new DataNode(each, logicTableName));
        }
        return result;
    }
    
    private Map<String, List<DataNode>> getExpectedDataNodeGroups(final List<String> dataSourceNames, final String logicTableName) {
        Map<String, List<DataNode>> result = new LinkedHashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            LinkedList<DataNode> self = new LinkedList<>();
            self.add(new DataNode(each, logicTableName));
            result.put(each, self);
        }
        return result;
    }
    
}

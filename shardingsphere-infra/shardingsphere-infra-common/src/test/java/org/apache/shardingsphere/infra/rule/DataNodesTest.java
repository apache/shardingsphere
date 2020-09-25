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
import org.apache.shardingsphere.infra.rule.fixture.TestDataSourceRoutedRule;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingRule;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingSphereRule;
import org.apache.shardingsphere.infra.rule.fixture.TestTableRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataNodesTest {
    
    private final String logicTableName1 = "user";
    
    private final String logicTableName2 = "dept";
    
    private final Collection<String> dataSourceNames1 = Arrays.asList("primary_db_1", "primary_db_2", "replica_db_1", "replica_db_2");
    
    private final Collection<String> dataSourceNames2 = Arrays.asList("primary_db_3", "replica_db_3");
    
    private final String logicDataSourceName = "primary_db_1";
    
    private final Collection<String> replicaDataSourceNames = Arrays.asList("route_db_1", "route_db_2");
    
    @Test(expected = NullPointerException.class)
    public void assertWrongTable() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        dataNodes.getDataNodes("wrongTableName");
    }
    
    @Test
    public void assertGetEmpty() {
        assertThat(getNonRoutedRuleDataNodes().getDataNodes("tableName"), is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodes() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        assertTrue(dataNodes.getDataNodes(logicTableName1).containsAll(getExpectedDataNodes(dataSourceNames1, logicTableName1)));
        assertTrue(dataNodes.getDataNodes(logicTableName2).containsAll(getExpectedDataNodes(dataSourceNames2, logicTableName2)));
    }
    
    @Test
    public void assertGetDataNodeGroups() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        assertThat(dataNodes.getDataNodeGroups(logicTableName1), is(getExpectedDataNodeGroups(dataSourceNames1, logicTableName1)));
        assertThat(dataNodes.getDataNodeGroups(logicTableName2), is(getExpectedDataNodeGroups(dataSourceNames2, logicTableName2)));
    }
    
    private DataNodes getRoutedRuleDataNodes() {
        TestTableRule tableRule1 = new TestTableRule(dataSourceNames1, logicTableName1);
        TestTableRule tableRule2 = new TestTableRule(dataSourceNames2, logicTableName2);
        List<TestTableRule> tableRules = Arrays.asList(tableRule1, tableRule2);
        ShardingSphereRule rule1 = new TestShardingRule(tableRules);
        Map<String, Collection<String>> dataSourceMapper = Collections.singletonMap(logicDataSourceName, replicaDataSourceNames);
        TestDataSourceRoutedRule rule2 = new TestDataSourceRoutedRule(dataSourceMapper);
        return new DataNodes(Arrays.asList(rule1, rule2));
    }
    
    private DataNodes getNonRoutedRuleDataNodes() {
        return new DataNodes(Collections.singleton(new TestShardingSphereRule()));
    }
    
    private Collection<DataNode> getExpectedDataNodes(final Collection<String> dataSourceNames, final String logicTableName) {
        Collection<DataNode> result = new LinkedList<>();
        for (String each : dataSourceNames) {
            if (logicDataSourceName.equals(each)) {
                replicaDataSourceNames.forEach(dataSourceName -> result.add(new DataNode(dataSourceName, logicTableName)));
            } else {
                result.add(new DataNode(each, logicTableName));
            }
        }
        return result;
    }
    
    private Map<String, List<DataNode>> getExpectedDataNodeGroups(final Collection<String> dataSourceNames, final String logicTableName) {
        return getExpectedDataNodes(dataSourceNames, logicTableName).stream().collect(Collectors.groupingBy(DataNode::getDataSourceName));
    }
}

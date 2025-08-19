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

package org.apache.shardingsphere.distsql.handler.fixture;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.resoure.UnregisterStorageUnitRuleAttribute;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DistSQLHandlerFixtureRule implements ShardingSphereRule {
    
    @Override
    public RuleConfiguration getConfiguration() {
        return mock(RuleConfiguration.class);
    }
    
    @Override
    public RuleAttributes getAttributes() {
        return new RuleAttributes(mockDataNodeRuleAttribute(), mockStorageUnitDefinitionProcessorRuleAttribute());
    }
    
    private DataNodeRuleAttribute mockDataNodeRuleAttribute() {
        DataNodeRuleAttribute result = mock(DataNodeRuleAttribute.class);
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(result.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(result.findFirstActualTable(any())).thenReturn(Optional.empty());
        when(result.findLogicTableByActualTable(any())).thenReturn(Optional.empty());
        when(result.findActualTableByCatalog(any(), any())).thenReturn(Optional.empty());
        return result;
    }
    
    private UnregisterStorageUnitRuleAttribute mockStorageUnitDefinitionProcessorRuleAttribute() {
        UnregisterStorageUnitRuleAttribute result = mock(UnregisterStorageUnitRuleAttribute.class);
        when(result.ignoreUsageCheck(true, false)).thenReturn(true);
        return result;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}

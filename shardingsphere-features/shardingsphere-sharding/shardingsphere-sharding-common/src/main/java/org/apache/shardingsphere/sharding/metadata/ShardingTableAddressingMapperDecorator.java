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

package org.apache.shardingsphere.sharding.metadata;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.TableAddressingMapperDecorator;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Table addressing mapper decorator of sharding.
 */
public final class ShardingTableAddressingMapperDecorator implements TableAddressingMapperDecorator<ShardingRule> {
    
    @Override
    public void decorate(final ShardingRule rule, final Map<String, Collection<String>> tableAddressingMapper) {
        rule.getTableRules().forEach(each -> decorate(each, tableAddressingMapper));
    }
    
    private void decorate(final TableRule tableRule, final Map<String, Collection<String>> tableDataSourceNamesMapper) {
        boolean found = false;
        for (String each : tableRule.getActualDataNodes().stream().map(DataNode::getTableName).collect(Collectors.toSet())) {
            found = null != tableDataSourceNamesMapper.remove(each) || found;
        }
        if (found) {
            tableDataSourceNamesMapper.put(tableRule.getLogicTable(), new LinkedList<>(tableRule.getActualDatasourceNames()));
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}

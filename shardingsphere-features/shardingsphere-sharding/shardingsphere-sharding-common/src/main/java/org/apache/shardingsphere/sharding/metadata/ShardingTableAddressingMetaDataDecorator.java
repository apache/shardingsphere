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
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaDataDecorator;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.stream.Collectors;

/**
 * Sharding table addressing meta data decorator.
 */
public final class ShardingTableAddressingMetaDataDecorator implements TableAddressingMetaDataDecorator<ShardingRule> {
    
    @Override
    public void decorate(final ShardingRule rule, final TableAddressingMetaData metaData) {
        rule.getTableRules().forEach(each -> decorate(each, metaData));
    }
    
    private void decorate(final TableRule tableRule, final TableAddressingMetaData metaData) {
        for (String each : tableRule.getActualDataNodes().stream().map(DataNode::getTableName).collect(Collectors.toSet())) {
            metaData.getTableDataSourceNamesMapper().remove(each);
        }
        metaData.getTableDataSourceNamesMapper().put(tableRule.getLogicTable(), tableRule.getActualDatasourceNames());
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

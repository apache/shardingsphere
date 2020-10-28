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

package org.apache.shardingsphere.replicaquery.metadata;

import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaDataDecorator;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * Table addressing meta data decorator of replica query.
 */
public final class ReplicaQueryTableAddressingMetaDataDecorator implements TableAddressingMetaDataDecorator<ReplicaQueryRule> {
    
    @Override
    public void decorate(final ReplicaQueryRule rule, final TableAddressingMetaData metaData) {
        for (String each : rule.getAllLogicDataSourceNames()) {
            rule.findDataSourceRule(each).ifPresent(dataSourceRule -> decorate(dataSourceRule, metaData));
        }
    }
    
    private void decorate(final ReplicaQueryDataSourceRule dataSourceRule, final TableAddressingMetaData metaData) {
        for (Entry<String, Collection<String>> entry : metaData.getTableDataSourceNamesMapper().entrySet()) {
            entry.getValue().remove(dataSourceRule.getPrimaryDataSourceName());
            entry.getValue().removeAll(dataSourceRule.getReplicaDataSourceNames());
            entry.getValue().add(dataSourceRule.getName());
        }
    }
    
    @Override
    public int getOrder() {
        return ReplicaQueryOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaQueryRule> getTypeClass() {
        return ReplicaQueryRule.class;
    }
}

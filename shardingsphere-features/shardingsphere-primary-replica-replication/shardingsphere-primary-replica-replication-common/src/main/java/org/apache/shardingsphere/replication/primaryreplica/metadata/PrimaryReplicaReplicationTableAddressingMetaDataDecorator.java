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

package org.apache.shardingsphere.replication.primaryreplica.metadata;

import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaDataDecorator;
import org.apache.shardingsphere.replication.primaryreplica.constant.PrimaryReplicaReplicationOrder;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationDataSourceRule;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * Table addressing meta data decorator of primary replica replication.
 */
public final class PrimaryReplicaReplicationTableAddressingMetaDataDecorator implements TableAddressingMetaDataDecorator<PrimaryReplicaReplicationRule> {
    
    @Override
    public void decorate(final PrimaryReplicaReplicationRule rule, final TableAddressingMetaData metaData) {
        for (String each : rule.getAllLogicDataSourceNames()) {
            rule.findDataSourceRule(each).ifPresent(dataSourceRule -> decorate(dataSourceRule, metaData));
        }
    }
    
    public void decorate(final PrimaryReplicaReplicationDataSourceRule dataSourceRule, final TableAddressingMetaData metaData) {
        for (Entry<String, Collection<String>> entry : metaData.getTableDataSourceNamesMapper().entrySet()) {
            entry.getValue().remove(dataSourceRule.getPrimaryDataSourceName());
            entry.getValue().removeAll(dataSourceRule.getReplicaDataSourceNames());
            entry.getValue().add(entry.getKey());
        }
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaReplicationOrder.ORDER;
    }
    
    @Override
    public Class<PrimaryReplicaReplicationRule> getTypeClass() {
        return PrimaryReplicaReplicationRule.class;
    }
}

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

package org.apache.shardingsphere.ha.metadata;

import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaDataDecorator;
import org.apache.shardingsphere.ha.constant.HAOrder;
import org.apache.shardingsphere.ha.rule.HADataSourceRule;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * Table addressing meta data decorator of HA.
 */
public final class HATableAddressingMetaDataDecorator implements TableAddressingMetaDataDecorator<HARule> {
    
    @Override
    public void decorate(final HARule rule, final TableAddressingMetaData metaData) {
        for (String each : rule.getAllLogicDataSourceNames()) {
            rule.findDataSourceRule(each).ifPresent(dataSourceRule -> decorate(dataSourceRule, metaData));
        }
    }
    
    private void decorate(final HADataSourceRule dataSourceRule, final TableAddressingMetaData metaData) {
        for (Entry<String, Collection<String>> entry : metaData.getTableDataSourceNamesMapper().entrySet()) {
            entry.getValue().remove(dataSourceRule.getPrimaryDataSourceName());
            entry.getValue().removeAll(dataSourceRule.getReplicaDataSourceNames());
            entry.getValue().add(dataSourceRule.getName());
        }
    }
    
    @Override
    public int getOrder() {
        return HAOrder.ORDER;
    }
    
    @Override
    public Class<HARule> getTypeClass() {
        return HARule.class;
    }
}

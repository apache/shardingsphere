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

package org.apache.shardingsphere.readwritesplitting.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AddReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AlterReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.DeleteReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AlterLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.DeleteLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.metadata.nodepath.ReadwriteSplittingNodePath;

import java.util.Optional;

/**
 * Readwrite-splitting rule configuration event builder.
 */
public final class ReadwriteSplittingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private final RuleNodePath readwriteSplittingRuleNodePath = ReadwriteSplittingNodePath.getInstance();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!readwriteSplittingRuleNodePath.getRoot().isValidatedPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> groupName = readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingNodePath.DATA_SOURCES).getNameByActiveVersion(event.getKey());
        if (groupName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createReadwriteSplittingConfigEvent(databaseName, groupName.get(), event);
        }
        Optional<String> loadBalancerName = readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingNodePath.LOAD_BALANCERS).getNameByActiveVersion(event.getKey());
        if (loadBalancerName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createLoadBalanceEvent(databaseName, loadBalancerName.get(), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createReadwriteSplittingConfigEvent(final String databaseName, final String groupName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddReadwriteSplittingDataSourceEvent(databaseName, groupName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterReadwriteSplittingDataSourceEvent(databaseName, groupName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteReadwriteSplittingDataSourceEvent(databaseName, groupName));
    }
    
    private Optional<GovernanceEvent> createLoadBalanceEvent(final String databaseName, final String loadBalancerName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterLoadBalanceEvent(databaseName, loadBalancerName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteLoadBalanceEvent(databaseName, loadBalancerName));
    }
}

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

package org.apache.shardingsphere.readwritesplitting.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.RuleConfigurationChangedEvent;
import org.apache.shardingsphere.readwritesplitting.event.ReadwriteSplittingRuleConfigurationChangedEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Optional;
import java.util.Map;

/**
 * Readwrite-splitting configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ReadwriteSplittingConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew readwrite-splitting configuration.
     *
     * @param event readwrite-splitting configuration changed event
     */
    @Subscribe
    public synchronized void renew(final ReadwriteSplittingRuleConfigurationChangedEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Optional<ReadwriteSplittingRule> readwriteSplittingRule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (!readwriteSplittingRule.isPresent()) {
            return;
        }
        instanceContext.getEventBusContext().post(new RuleConfigurationChangedEvent(event.getDatabaseName(), readwriteSplittingRule.get().getConfiguration()));
    }
}

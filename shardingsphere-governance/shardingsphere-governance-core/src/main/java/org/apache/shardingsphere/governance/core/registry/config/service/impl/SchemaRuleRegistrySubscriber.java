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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

/**
 * Schema rule registry subscriber.
 */
public final class SchemaRuleRegistrySubscriber {
    
    private final SchemaRulePersistService persistService;
    
    public SchemaRuleRegistrySubscriber(final SchemaRulePersistService persistService) {
        this.persistService = persistService;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Update rule configurations for alter.
     *
     * @param event rule configurations altered event
     */
    @Subscribe
    public void update(final RuleConfigurationsAlteredSQLNotificationEvent event) {
        persistService.persist(event.getSchemaName(), event.getRuleConfigurations());
    }
}

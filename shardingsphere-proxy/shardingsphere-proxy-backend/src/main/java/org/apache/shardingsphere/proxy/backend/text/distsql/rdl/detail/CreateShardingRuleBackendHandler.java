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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail;

import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.infra.binder.statement.rdl.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.converter.CreateShardingRuleStatementContextConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * Create sharding rule backend handler.
 */
public final class CreateShardingRuleBackendHandler extends SchemaRequiredBackendHandler<CreateShardingRuleStatementContext> {
    
    @Override
    public ResponseHeader execute(final String schemaName, final CreateShardingRuleStatementContext sqlStatementContext) {
        YamlShardingRuleConfiguration config = CreateShardingRuleStatementContextConverter.convert(sqlStatementContext);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(config));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatementContext.getSqlStatement());
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(schemaName, rules));
        // TODO Need to get the executed feedback from registry center for returning.
    }
}

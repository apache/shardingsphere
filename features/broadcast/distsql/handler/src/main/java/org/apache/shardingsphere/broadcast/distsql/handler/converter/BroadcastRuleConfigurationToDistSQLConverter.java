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

package org.apache.shardingsphere.broadcast.distsql.handler.converter;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;

/**
 * Broadcast rule configuration to DistSQL converter.
 */
public final class BroadcastRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<BroadcastRuleConfiguration> {
    
    @Override
    public String convert(final BroadcastRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().isEmpty() ? "" : String.format(BroadcastConvertDistSQLConstants.SQL_PATTERN, Joiner.on(",").join(ruleConfig.getTables()));
    }
    
    @Override
    public Class<BroadcastRuleConfiguration> getType() {
        return BroadcastRuleConfiguration.class;
    }
}

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

package org.apache.shardingsphere.broadcast.distsql.handler.provider;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.handler.constant.BroadcastDistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;

import java.util.Iterator;

/**
 * Broadcast convert rule configuration provider.
 */
public final class BroadcastConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        return getBroadcastDistSQL((BroadcastRuleConfiguration) ruleConfig);
    }
    
    private String getBroadcastDistSQL(final BroadcastRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(BroadcastDistSQLConstants.CREATE_BROADCAST_RULE);
        Iterator<String> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            String tableName = iterator.next();
            result.append(tableName);
            if (iterator.hasNext()) {
                result.append(BroadcastDistSQLConstants.COMMA);
            }
        }
        result.append(BroadcastDistSQLConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    @Override
    public Class<? extends RuleConfiguration> getType() {
        return BroadcastRuleConfiguration.class;
    }
}

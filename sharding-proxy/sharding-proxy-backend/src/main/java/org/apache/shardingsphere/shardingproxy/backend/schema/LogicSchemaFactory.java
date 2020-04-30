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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShadowSchema;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.TransparentSchema;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;

import java.sql.SQLException;
import java.util.Map;

/**
 * Logic schema factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogicSchemaFactory {
    
    /**
     * Create new instance of logic schema.
     * 
     * @param schemaName schema name
     * @param schemaDataSources schema data sources
     * @param ruleConfiguration rule configuration
     * @param isUsingRegistry is using registry or not
     * @return new instance of logic schema
     * @throws SQLException SQL exception
     */
    public static LogicSchema newInstance(final String schemaName, final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources,
                                          final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) throws SQLException {
        if (ruleConfiguration instanceof ShardingRuleConfiguration) {
            return new ShardingSchema(schemaName, schemaDataSources.get(schemaName), (ShardingRuleConfiguration) ruleConfiguration);
        }
        if (ruleConfiguration instanceof MasterSlaveRuleConfiguration) {
            return new MasterSlaveSchema(schemaName, schemaDataSources.get(schemaName), (MasterSlaveRuleConfiguration) ruleConfiguration, isUsingRegistry);
        }
        if (ruleConfiguration instanceof EncryptRuleConfiguration) {
            return new EncryptSchema(schemaName, schemaDataSources.get(schemaName), (EncryptRuleConfiguration) ruleConfiguration);
        }
        if (ruleConfiguration instanceof ShadowRuleConfiguration) {
            return new ShadowSchema(schemaName, schemaDataSources.get(schemaName), (ShadowRuleConfiguration) ruleConfiguration);
        }
        return new TransparentSchema(schemaName, schemaDataSources.get(schemaName));
    }
}

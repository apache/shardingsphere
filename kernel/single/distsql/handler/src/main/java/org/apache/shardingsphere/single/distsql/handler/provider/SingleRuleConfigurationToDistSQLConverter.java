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

package org.apache.shardingsphere.single.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.handler.constant.SingleDistSQLConstants;

import java.util.Iterator;

/**
 * Single rule configuration to DistSQL converter.
 */
public final class SingleRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<SingleRuleConfiguration> {
    
    @Override
    public String convert(final SingleRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(SingleDistSQLConstants.LOAD_SINGLE_TABLE);
        Iterator<String> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            String tableName = iterator.next();
            result.append(String.format(SingleDistSQLConstants.DATASOURCE_AND_TABLE, ruleConfig.getDefaultDataSource(), tableName));
            if (iterator.hasNext()) {
                result.append(SingleDistSQLConstants.COMMA);
            }
        }
        result.append(SingleDistSQLConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    @Override
    public Class<SingleRuleConfiguration> getType() {
        return SingleRuleConfiguration.class;
    }
}

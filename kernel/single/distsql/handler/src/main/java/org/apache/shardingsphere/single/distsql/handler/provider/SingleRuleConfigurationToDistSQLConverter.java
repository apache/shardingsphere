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

import com.google.common.base.Joiner;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;

/**
 * Single rule configuration to DistSQL converter.
 */
public final class SingleRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<SingleRuleConfiguration> {
    
    public static final String LOAD_SINGLE_TABLE = "LOAD SINGLE TABLE %s;";
    
    public static final String SET_DEFAULT_SINGLE_TABLE_STORAGE_UNIT = "SET DEFAULT SINGLE TABLE STORAGE UNIT = %s;";
    
    @Override
    public String convert(final SingleRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty() && !ruleConfig.getDefaultDataSource().isPresent()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getTables().isEmpty()) {
            result.append(convertLoadTable(ruleConfig));
        }
        if (ruleConfig.getDefaultDataSource().isPresent()) {
            if (!ruleConfig.getTables().isEmpty()) {
                result.append(System.lineSeparator()).append(System.lineSeparator());
            }
            result.append(convertSetDefaultSingleTableStorageUnit(ruleConfig.getDefaultDataSource().get()));
        }
        return result.toString();
    }
    
    private String convertLoadTable(final SingleRuleConfiguration ruleConfig) {
        return String.format(LOAD_SINGLE_TABLE, Joiner.on(",").join(ruleConfig.getTables()));
    }
    
    private String convertSetDefaultSingleTableStorageUnit(final String defaultStorageUnitName) {
        return String.format(SET_DEFAULT_SINGLE_TABLE_STORAGE_UNIT, defaultStorageUnitName);
    }
    
    @Override
    public Class<SingleRuleConfiguration> getType() {
        return SingleRuleConfiguration.class;
    }
}

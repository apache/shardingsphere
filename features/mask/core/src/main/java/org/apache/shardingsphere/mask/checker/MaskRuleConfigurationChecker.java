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

package org.apache.shardingsphere.mask.checker;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.exception.checker.InvalidMaskAlgorithmNameException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Mask rule configuration checker.
 */
public final class MaskRuleConfigurationChecker implements RuleConfigurationChecker<MaskRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final MaskRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkTables(databaseName, config.getTables(), config.getMaskAlgorithms());
    }
    
    private void checkTables(final String databaseName, final Collection<MaskTableRuleConfiguration> tables, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        for (MaskTableRuleConfiguration each : tables) {
            checkColumns(databaseName, each.getColumns(), maskAlgorithms);
        }
    }
    
    private void checkColumns(final String databaseName, final Collection<MaskColumnRuleConfiguration> columns, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        for (MaskColumnRuleConfiguration each : columns) {
            ShardingSpherePreconditions.checkState(maskAlgorithms.containsKey(each.getMaskAlgorithm()), () -> new InvalidMaskAlgorithmNameException(databaseName, each.getMaskAlgorithm()));
        }
    }
    
    @Override
    public int getOrder() {
        return MaskOrder.ORDER;
    }
    
    @Override
    public Class<MaskRuleConfiguration> getTypeClass() {
        return MaskRuleConfiguration.class;
    }
}

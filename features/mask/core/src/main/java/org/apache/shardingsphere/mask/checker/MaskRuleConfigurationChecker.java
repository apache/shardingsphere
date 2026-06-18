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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mask rule configuration checker.
 */
public final class MaskRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<MaskRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final MaskRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkMaskAlgorithms(ruleConfig.getMaskAlgorithms());
        checkTables(databaseName, ruleConfig.getTables(), ruleConfig.getMaskAlgorithms());
    }
    
    private void checkMaskAlgorithms(final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        maskAlgorithms.values().forEach(each -> TypedSPILoader.checkService(MaskAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkTables(final String databaseName, final Collection<MaskTableRuleConfiguration> tables, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        tables.forEach(each -> checkColumns(databaseName, each, maskAlgorithms));
    }
    
    private void checkColumns(final String databaseName, final MaskTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        tableRuleConfig.getColumns().forEach(each -> checkColumn(databaseName, tableRuleConfig.getName(), each, maskAlgorithms));
    }
    
    private void checkColumn(final String databaseName, final String tableName, final MaskColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        ShardingSpherePreconditions.checkContainsKey(maskAlgorithms, columnRuleConfig.getMaskAlgorithm(),
                () -> new UnregisteredAlgorithmException("Mask", columnRuleConfig.getMaskAlgorithm(), new SQLExceptionIdentifier(databaseName, tableName, columnRuleConfig.getLogicColumn())));
    }
    
    @Override
    public Collection<String> getTableNames(final MaskRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
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

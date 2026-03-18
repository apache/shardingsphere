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

package org.apache.shardingsphere.mask.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mask rule configuration to DistSQL converter.
 */
public final class MaskRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<MaskRuleConfiguration> {
    
    @Override
    public String convert(final MaskRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().isEmpty() ? "" : MaskConvertDistSQLConstants.CREATE_MASK_RULE + convertMaskTables(ruleConfig) + DistSQLConstants.SEMI;
    }
    
    private String convertMaskTables(final MaskRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().stream().map(each -> convertMaskTable(each, ruleConfig.getMaskAlgorithms())).collect(Collectors.joining(DistSQLConstants.COMMA + System.lineSeparator()));
    }
    
    private String convertMaskTable(final MaskTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        return String.format(MaskConvertDistSQLConstants.MASK_TABLE, tableRuleConfig.getName(), convertMaskColumns(tableRuleConfig.getColumns(), maskAlgorithms));
    }
    
    private String convertMaskColumns(final Collection<MaskColumnRuleConfiguration> columnRuleConfigs, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        return columnRuleConfigs.stream().map(each -> convertMaskColumn(each, maskAlgorithms)).collect(Collectors.joining(DistSQLConstants.COMMA + System.lineSeparator()));
    }
    
    private String convertMaskColumn(final MaskColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        return String.format(MaskConvertDistSQLConstants.MASK_COLUMN,
                columnRuleConfig.getLogicColumn(), AlgorithmDistSQLConverter.getAlgorithmType(maskAlgorithms.get(columnRuleConfig.getMaskAlgorithm())));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getType() {
        return MaskRuleConfiguration.class;
    }
}

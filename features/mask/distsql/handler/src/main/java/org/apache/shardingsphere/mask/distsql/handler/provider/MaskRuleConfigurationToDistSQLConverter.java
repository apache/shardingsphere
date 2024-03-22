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

package org.apache.shardingsphere.mask.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Mask rule configuration to DistSQL converter.
 */
public final class MaskRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<MaskRuleConfiguration> {
    
    private static final String CREATE_MASK = "CREATE MASK RULE";
    
    private static final String MASK = " %s ("
            + System.lineSeparator()
            + "COLUMNS("
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + "),";
    
    private static final String MASK_COLUMN = "(NAME=%s, %s)";
    
    @Override
    public String convert(final MaskRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(CREATE_MASK);
        Iterator<MaskTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            MaskTableRuleConfiguration tableRuleConfig = iterator.next();
            result.append(String.format(MASK, tableRuleConfig.getName(), getMaskColumns(tableRuleConfig.getColumns(), ruleConfig.getMaskAlgorithms())));
            if (iterator.hasNext()) {
                result.append(",").append(System.lineSeparator());
            }
        }
        result.append(";").append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private String getMaskColumns(final Collection<MaskColumnRuleConfiguration> columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        StringBuilder result = new StringBuilder();
        Iterator<MaskColumnRuleConfiguration> iterator = columnRuleConfig.iterator();
        if (iterator.hasNext()) {
            MaskColumnRuleConfiguration column = iterator.next();
            result.append(String.format(MASK_COLUMN, column.getLogicColumn(), getMaskAlgorithms(column, maskAlgorithms)));
        }
        return result.toString();
    }
    
    private String getMaskAlgorithms(final MaskColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        return AlgorithmDistSQLConverter.getAlgorithmType(maskAlgorithms.get(columnRuleConfig.getMaskAlgorithm()));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getType() {
        return MaskRuleConfiguration.class;
    }
}

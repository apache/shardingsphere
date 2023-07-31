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

package org.apache.shardingsphere.mask.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.parser.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show mask rule executor.
 */
public final class ShowMaskRuleExecutor implements RQLExecutor<ShowMaskRulesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowMaskRulesStatement sqlStatement) {
        Optional<MaskRule> rule = database.getRuleMetaData().findSingleRule(MaskRule.class);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        if (rule.isPresent()) {
            result = buildData((MaskRuleConfiguration) rule.get().getConfiguration(), sqlStatement);
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> buildData(final MaskRuleConfiguration ruleConfig, final ShowMaskRulesStatement sqlStatement) {
        return ruleConfig.getTables().stream().filter(each -> null == sqlStatement.getTableName() || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, ruleConfig.getMaskAlgorithms())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildColumnData(final MaskTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> algorithmMap) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        tableRuleConfig.getColumns().forEach(each -> {
            AlgorithmConfiguration maskAlgorithmConfig = algorithmMap.get(each.getMaskAlgorithm());
            result.add(new LocalDataQueryResultRow(Arrays.asList(tableRuleConfig.getName(), each.getLogicColumn(),
                    maskAlgorithmConfig.getType(), PropertiesConverter.convert(maskAlgorithmConfig.getProps()))));
        });
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "column", "algorithm_type", "algorithm_props");
    }
    
    @Override
    public String getType() {
        return ShowMaskRulesStatement.class.getName();
    }
}

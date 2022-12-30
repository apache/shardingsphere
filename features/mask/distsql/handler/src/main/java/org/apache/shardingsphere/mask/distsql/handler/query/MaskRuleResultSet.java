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

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.parser.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Result set for show mask rule.
 */
public final class MaskRuleResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<MaskRule> rule = database.getRuleMetaData().findSingleRule(MaskRule.class);
        rule.ifPresent(optional -> data = buildData((MaskRuleConfiguration) optional.getConfiguration(), (ShowMaskRulesStatement) sqlStatement).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final MaskRuleConfiguration ruleConfig, final ShowMaskRulesStatement sqlStatement) {
        return ruleConfig.getTables().stream().filter(each -> Objects.isNull(sqlStatement.getTableName()) || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, ruleConfig.getMaskAlgorithms())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<Collection<Object>> buildColumnData(final MaskTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> algorithmMap) {
        Collection<Collection<Object>> result = new LinkedList<>();
        tableRuleConfig.getColumns().forEach(each -> {
            AlgorithmConfiguration maskAlgorithmConfig = algorithmMap.get(each.getMaskAlgorithm());
            result.add(Arrays.asList(tableRuleConfig.getName(), each.getLogicColumn(),
                    maskAlgorithmConfig.getType(), PropertiesConverter.convert(maskAlgorithmConfig.getProps())));
        });
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "column", "algorithm_type", "algorithm_props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowMaskRulesStatement.class.getName();
    }
}

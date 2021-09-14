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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Result set for shadow algorithm.
 */
public final class ShadowAlgorithmQueryResultSet implements DistSQLResultSet {
    
    private Map<String, List<String>> tableMap = new HashMap<>();
    
    private Iterator<Entry<String, ShardingSphereAlgorithmConfiguration>> algorithms;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShadowRuleConfiguration> rule = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShadowRuleConfiguration).map(each -> (ShadowRuleConfiguration) each).findAny();
        rule.ifPresent(op -> {
            tableMap = convertToMap(op.getTables());
            algorithms = op.getShadowAlgorithms().entrySet().iterator();
        });
    }
    
    private Map<String, List<String>> convertToMap(final Map<String, ShadowTableConfiguration> tables) {
        Map<String, List<String>> result = new HashMap<>();
        tables.forEach((key, value) -> putInResult(key, value.getShadowAlgorithmNames(), result));
        return result;
    }
    
    private void putInResult(final String tableName, final Collection<String> shadowAlgorithmNames, final Map<String, List<String>> result) {
        shadowAlgorithmNames.forEach(each -> {
            result.putIfAbsent(each, new ArrayList<>());
            result.get(each).add(tableName);
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("shadow_algorithm_id", "type", "props", "shadow_tables");
    }
    
    @Override
    public boolean next() {
        return algorithms.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(algorithms.next());
    }
    
    private Collection<Object> buildTableRowData(final Entry<String, ShardingSphereAlgorithmConfiguration> data) {
        return Arrays.asList(data.getKey(), data.getValue().getType(), convertToString(data.getValue().getProps()), convertToString(tableMap.get(data.getKey())));
    }
    
    private String convertToString(final Properties props) {
        return Objects.nonNull(props) ? PropertiesConverter.convert(props) : "";
    }
    
    private String convertToString(final Collection<String> shadowTables) {
        if (null != shadowTables) {
            return String.join(",", shadowTables);
        }
        return "";
    }
    
    @Override
    public String getType() {
        return ShowShadowAlgorithmsStatement.class.getCanonicalName();
    }
}

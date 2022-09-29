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

import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Query result set for show shadow table rule.
 */
public final class ShadowTableRuleQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final String SHADOW_TABLE = "shadow_table";
    
    private static final String SHADOW_ALGORITHM_NAME = "shadow_algorithm_name";
    
    private Iterator<Map<String, String>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        rule.ifPresent(optional -> data = buildData((ShadowRuleConfiguration) optional.getConfiguration()).iterator());
    }
    
    private List<Map<String, String>> buildData(final ShadowRuleConfiguration shadowRuleConfiguration) {
        List<Map<String, String>> result = new ArrayList<>();
        shadowRuleConfiguration.getTables().forEach((key, value) -> {
            Map<String, String> map = new HashMap<>();
            map.put(SHADOW_TABLE, key);
            map.put(SHADOW_ALGORITHM_NAME, convertToString(value.getShadowAlgorithmNames()));
            result.add(map);
        });
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(SHADOW_TABLE, SHADOW_ALGORITHM_NAME);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(data.next());
    }
    
    private Collection<Object> buildTableRowData(final Map<String, String> data) {
        return Arrays.asList(data.get(SHADOW_TABLE), data.get(SHADOW_ALGORITHM_NAME));
    }
    
    private String convertToString(final Collection<String> shadowTables) {
        return null == shadowTables ? "" : String.join(",", shadowTables);
    }
    
    @Override
    public String getType() {
        return ShowShadowTableRulesStatement.class.getName();
    }
}

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

package org.apache.shardingsphere.scaling.distsql.handler.query;

import com.google.gson.Gson;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.scaling.distsql.statement.ShowShardingScalingRulesStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Query result set for show sharding scaling rules.
 */
public final class ShardingScalingRulesQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Entry<String, OnRuleAlteredActionConfiguration>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        data = rule.map(optional -> ((ShardingRuleConfiguration) optional.getConfiguration()).getScaling().entrySet().iterator()).orElse(Collections.emptyIterator());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "input", "output", "stream_channel", "completion_detector", "data_consistency_checker");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildRowData(data.next());
    }
    
    private Collection<Object> buildRowData(final Entry<String, OnRuleAlteredActionConfiguration> data) {
        Collection<Object> result = new LinkedList<>();
        result.add(data.getKey());
        OnRuleAlteredActionConfiguration shardingScalingRule = data.getValue();
        result.add(null == shardingScalingRule ? "" : getString(shardingScalingRule.getInput()));
        result.add(null == shardingScalingRule ? "" : getString(shardingScalingRule.getOutput()));
        result.add(null == shardingScalingRule ? "" : getString(shardingScalingRule.getStreamChannel()));
        result.add(null == shardingScalingRule ? "" : getString(shardingScalingRule.getCompletionDetector()));
        result.add(null == shardingScalingRule ? "" : getString(shardingScalingRule.getDataConsistencyCalculator()));
        return result;
    }
    
    private String getString(final Object obj) {
        return null == obj ? "" : new Gson().toJson(obj);
    }
    
    @Override
    public String getType() {
        return ShowShardingScalingRulesStatement.class.getName();
    }
}

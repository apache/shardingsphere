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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Show sharding algorithm executor.
 */
public final class ShowShardingAlgorithmExecutor implements RQLExecutor<ShowShardingAlgorithmsStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingAlgorithmsStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        Iterator<Entry<String, AlgorithmConfiguration>> data =
                rule.map(optional -> ((ShardingRuleConfiguration) optional.getConfiguration()).getShardingAlgorithms().entrySet().iterator()).orElse(Collections.emptyIterator());
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Entry<String, AlgorithmConfiguration> entry = data.next();
            result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), buildProps(entry.getValue().getProps())));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    private String buildProps(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShowShardingAlgorithmsStatement> getType() {
        return ShowShardingAlgorithmsStatement.class;
    }
}

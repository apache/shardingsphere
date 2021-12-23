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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Result set for show sharding key generators.
 */
public final class ShardingKeyGeneratorsQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Entry<String, ShardingSphereAlgorithmConfiguration>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        metaData.getRuleMetaData().findRuleConfiguration(ShardingRuleConfiguration.class)
                .forEach(each -> data = each.getKeyGenerators().entrySet().iterator());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Map.Entry<String, ShardingSphereAlgorithmConfiguration> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue().getType(), entry.getValue().getProps());
    }
    
    @Override
    public String getType() {
        return ShowShardingKeyGeneratorsStatement.class.getCanonicalName();
    }
}

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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowUnusedShardingKeyGeneratorExecutor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowUnusedShardingKeyGeneratorExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowUnusedShardingKeyGeneratorsStatement> executor = new ShowUnusedShardingKeyGeneratorExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(ShowUnusedShardingKeyGeneratorsStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("uuid_key_generator"));
        assertThat(row.getCell(2), is("UUID"));
        assertThat(row.getCell(3), is(""));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowUnusedShardingKeyGeneratorsStatement> executor = new ShowUnusedShardingKeyGeneratorExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("name"));
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("props"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getKeyGenerators().put("snowflake_key_generator", createSnowflakeKeyGeneratorConfiguration());
        result.getKeyGenerators().put("uuid_key_generator", new AlgorithmConfiguration("UUID", null));
        result.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        return result;
    }
    
    private AlgorithmConfiguration createSnowflakeKeyGeneratorConfiguration() {
        return new AlgorithmConfiguration("SNOWFLAKE", new Properties());
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table", null);
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake_key_generator"));
        return result;
    }
}

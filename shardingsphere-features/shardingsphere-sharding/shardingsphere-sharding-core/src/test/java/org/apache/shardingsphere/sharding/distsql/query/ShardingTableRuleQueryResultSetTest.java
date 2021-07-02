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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.RQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingTableRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(createRuleConfiguration());
        RQLResultSet resultSet = new ShardingTableRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingTableRulesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(14));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.get(2), is(""));
        assertThat(actual.get(3), is("INLINE"));
        assertThat(actual.get(4), is("user_id"));
        assertThat(actual.get(5), is("INLINE"));
        assertThat(actual.get(6), is("algorithm-expression=ds_${user_id % 2}"));
        assertThat(actual.get(7), is("INLINE"));
        assertThat(actual.get(8), is("order_id"));
        assertThat(actual.get(9), is("INLINE"));
        assertThat(actual.get(10), is("algorithm-expression=t_order_${order_id % 2}"));
        assertThat(actual.get(11), is("order_id"));
        assertThat(actual.get(12), is("SNOWFLAKE"));
        assertThat(actual.get(13), is("worker-id=123"));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(), Collection.class));
    }
    
    @SneakyThrows
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/distsql/sharding-rule-config.yaml").toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}

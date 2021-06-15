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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingTableRulesQueryBackendHandlerTest {

    @Mock
    private BackendConnection backendConnection;

    @Mock
    private ShowShardingTableRulesStatement sqlStatement;

    @Mock
    private MetaDataContexts metaDataContexts;

    @Mock
    private TransactionContexts transactionContexts;

    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;

    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;

    private ShardingTableRulesQueryBackendHandler handler;

    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        handler = new ShardingTableRulesQueryBackendHandler(sqlStatement, backendConnection);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfiguration());
    }

    @Test
    public void assertExecute() {
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof QueryResponseHeader);
        Collection<Object> rowData = handler.getRowData();
        assertNotNull(rowData);
        List<Object> rowDataList = new ArrayList<>(rowData);
        assertThat(rowDataList.size(), is(14));
        assertThat(rowDataList.get(0), is("t_order"));
        assertThat(rowDataList.get(1), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(rowDataList.get(2), is(""));
        assertThat(rowDataList.get(3), is("INLINE"));
        assertThat(rowDataList.get(4), is("user_id"));
        assertThat(rowDataList.get(5), is("INLINE"));
        assertThat(rowDataList.get(6), is("algorithm-expression=ds_${user_id % 2}"));
        assertThat(rowDataList.get(7), is("INLINE"));
        assertThat(rowDataList.get(8), is("order_id"));
        assertThat(rowDataList.get(9), is("INLINE"));
        assertThat(rowDataList.get(10), is("algorithm-expression=t_order_${order_id % 2}"));
        assertThat(rowDataList.get(11), is("order_id"));
        assertThat(rowDataList.get(12), is("SNOWFLAKE"));
        assertThat(rowDataList.get(13), is("worker-id=123"));
    }

    private Collection<RuleConfiguration> buildShardingConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(), Collection.class));
    }

    @SneakyThrows
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/config-sharding.yaml").toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}

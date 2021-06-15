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

import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingBroadcastTableRulesQueryBackendHandlerTest {

    @Mock
    private BackendConnection backendConnection;

    @Mock
    private ShowShardingBroadcastTableRulesStatement sqlStatement;

    @Mock
    private MetaDataContexts metaDataContexts;

    @Mock
    private TransactionContexts transactionContexts;

    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;

    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;

    private ShardingBroadcastTableRulesQueryBackendHandler handler;

    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(sqlStatement.getSchema()).thenReturn(Optional.of(new SchemaSegment(0, 1, new IdentifierValue("test"))));
        handler = new ShardingBroadcastTableRulesQueryBackendHandler(sqlStatement, backendConnection);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(buildShardingRuleConfiguration()));
    }

    @Test
    public void assertExecute() {
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof QueryResponseHeader);
        assertTrue(handler.next());
        assertThat(handler.getRowData(), is(Arrays.asList("t_order")));
    }

    private ShardingRuleConfiguration buildShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getBroadcastTables().addAll(Arrays.asList("t_order", "t_order_item"));
        return shardingRuleConfiguration;
    }
}

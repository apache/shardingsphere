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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    private final ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertGetColumns() {
        ShowStatusFromReadwriteSplittingRulesExecutor executor = new ShowStatusFromReadwriteSplittingRulesExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("storage_unit"));
        assertThat(iterator.next(), is("status"));
    }
    
    @Test
    void assertGetRowsWithEmptyResult() {
        when(connectionSession.getDatabaseName()).thenReturn("readwrite_db");
        ShowStatusFromReadwriteSplittingRulesExecutor executor = new ShowStatusFromReadwriteSplittingRulesExecutor();
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("readwrite_db")).thenReturn(true);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockMetaData(), connectionSession,
                new ShowStatusFromReadwriteSplittingRulesStatement(new DatabaseSegment(1, 1, new IdentifierValue("readwrite_db")), null));
        assertTrue(actual.isEmpty());
    }
    
    private ContextManager mockContextManager() {
        MetaDataPersistService persistService = new MetaDataPersistService(mock(ZookeeperRepository.class));
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, mockMetaData());
        return new ContextManager(metaDataContexts, mock(InstanceContext.class, RETURNS_DEEP_STUBS));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("readwrite_db", mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singletonList(mock(ShardingSphereRule.class))), Collections.emptyMap());
        Map<String, ShardingSphereDatabase> databaseMap = new LinkedHashMap<>();
        databaseMap.put("readwrite_db", database);
        return new ShardingSphereMetaData(databaseMap, mock(ShardingSphereResourceMetaData.class),
                new ShardingSphereRuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
}

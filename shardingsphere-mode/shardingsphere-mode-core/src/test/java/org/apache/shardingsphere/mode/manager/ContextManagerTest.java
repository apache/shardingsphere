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

package org.apache.shardingsphere.mode.manager;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContext;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ContextManagerTest {

    private static Map<String, DataSource> dataSourceMap;

    @Mock
    private MetaDataContexts metaDataContexts;

    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ModeScheduleContext modeScheduleContext;

    private ContextManager contextManager;

    @Before
    public void setUp() throws SQLException {
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts, modeScheduleContext);
        dataSourceMap = new HashMap<>(2, 1);
        DataSource primaryDataSource = mock(DataSource.class);
        DataSource replicaDataSource = mock(DataSource.class);
        dataSourceMap.put("test_primary_ds", primaryDataSource);
        dataSourceMap.put("test_replica_ds", replicaDataSource);
    }

    @SneakyThrows
    @Test
    public void assertClose() {
        contextManager.close();
        verify(metaDataContexts).close();
    }

    @Test
    public void assertRenewMetaDataContexts() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(metaDataContexts);
        assertThat(contextManager.getMetaDataContexts(), is(metaDataContexts));
    }

    @Test
    public void assertRenewTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        contextManager.renewTransactionContexts(transactionContexts);
        assertThat(contextManager.getTransactionContexts(), is(transactionContexts));
    }

    @Test
    public void assertGetDataSourceMap() {
        DataSourcesMetaData dataSourceMetadata = mock(DataSourcesMetaData.class);
        CachedDatabaseMetaData cachedMetadata = mock(CachedDatabaseMetaData.class);
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereRuleMetaData sphereRuleMetadata = mock(ShardingSphereRuleMetaData.class);
        ShardingSphereResource shardingSphereResource = new ShardingSphereResource(
                dataSourceMap,
                dataSourceMetadata,
                cachedMetadata,
                databaseType
        );
        ShardingSphereMetaData metadata = new ShardingSphereMetaData("logic_schema", shardingSphereResource, sphereRuleMetadata, new ShardingSphereSchema());
        when(metaDataContexts.getMetaData(anyString())).thenReturn(metadata);
        Map<String, DataSource> dataSourceMap = contextManager.getDataSourceMap(DefaultSchema.LOGIC_NAME);
        assertThat(2, equalTo(dataSourceMap.size()));
    }
}

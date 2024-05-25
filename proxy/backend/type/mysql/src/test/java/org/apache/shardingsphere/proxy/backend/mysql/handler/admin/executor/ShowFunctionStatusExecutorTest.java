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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.data.ShardingSphereDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ShowFunctionStatusExecutorTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    @Test
    void assertExecute() throws SQLException {
        ShowFunctionStatusExecutor executor = new ShowFunctionStatusExecutor(new MySQLShowFunctionStatusStatement());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.execute(mock(ConnectionSession.class));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(11));
    }
    
    private ContextManager mockContextManager() {
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        ShardingSphereDataPersistService shardingSphereDataPersistService = mock(ShardingSphereDataPersistService.class);
        when(shardingSphereDataPersistService.load(any())).thenReturn(Optional.empty());
        when(metaDataPersistService.getShardingSphereDataPersistService()).thenReturn(shardingSphereDataPersistService);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService,
                new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1F);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
}

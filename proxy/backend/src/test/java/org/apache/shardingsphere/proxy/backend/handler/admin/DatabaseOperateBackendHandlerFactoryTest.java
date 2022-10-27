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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.database.DatabaseOperateBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseOperateBackendHandlerFactoryTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(connectionSession.getDatabaseName()).thenReturn("db");
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new PostgreSQLCreateDatabaseStatement());
    }
    
    private void assertExecuteCreateDatabaseContext(final CreateDatabaseStatement sqlStatement) throws SQLException {
        sqlStatement.setDatabaseName("new_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new MySQLDropDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new PostgreSQLDropDatabaseStatement());
    }
    
    private void assertExecuteDropDatabaseContext(final DropDatabaseStatement sqlStatement) throws SQLException {
        sqlStatement.setDatabaseName("db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContextWithException() {
        assertExecuteCreateDatabaseContextWithException(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLCreateDatabaseContextWithException() {
        assertExecuteCreateDatabaseContextWithException(new PostgreSQLCreateDatabaseStatement());
    }
    
    public void assertExecuteCreateDatabaseContextWithException(final CreateDatabaseStatement sqlStatement) {
        sqlStatement.setDatabaseName("db");
        setGovernanceMetaDataContexts(true);
        try {
            DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession);
        } catch (final DatabaseCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
    }
    
    private void setGovernanceMetaDataContexts(final boolean isGovernance) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        MetaDataContexts metaDataContexts = isGovernance
                ? mockMetaDataContexts()
                : new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        when(result.getMetaData().getDatabase("db").getResourceMetaData().getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getMetaData().getDatabase("db").getResourceMetaData().getNotExistedResources(any())).thenReturn(Collections.emptyList());
        return result;
    }
    
    @After
    public void setDown() {
        setGovernanceMetaDataContexts(false);
    }
}

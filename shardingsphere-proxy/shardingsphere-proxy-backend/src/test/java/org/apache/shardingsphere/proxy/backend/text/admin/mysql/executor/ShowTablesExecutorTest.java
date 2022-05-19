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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesExecutorTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereDatabaseMetaData> databaseMetaDataMap = getDatabaseMetaDataMap();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                databaseMetaDataMap, mock(ShardingSphereRuleMetaData.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereDatabaseMetaData> getDatabaseMetaDataMap() {
        Map<String, TableMetaData> tables = new HashMap<>(4, 1);
        tables.put("t_account", new TableMetaData("t_account", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_bak", new TableMetaData("t_account_bak", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_detail", new TableMetaData("t_account_detail", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_test", new TableMetaData("T_TEST", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        ShardingSphereSchema schema = new ShardingSphereSchema(tables);
        ShardingSphereDatabaseMetaData databaseMetaData = mock(ShardingSphereDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(databaseMetaData.getDatabase().getSchema(String.format(DATABASE_PATTERN, 0))).thenReturn(schema);
        when(databaseMetaData.isComplete()).thenReturn(true);
        when(databaseMetaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap(String.format(DATABASE_PATTERN, 0), databaseMetaData);
    }
    
    @Test
    public void assertShowTablesExecutorWithoutFilter() throws SQLException {
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(new MySQLShowTablesStatement(), DatabaseTypeEngine.getDatabaseType("MySQL"));
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithLikeFilter() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account%")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithSpecificTable() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithUpperCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "T_TEST")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithLowerCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_test")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", ""));
        when(result.getDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        return result;
    }
}

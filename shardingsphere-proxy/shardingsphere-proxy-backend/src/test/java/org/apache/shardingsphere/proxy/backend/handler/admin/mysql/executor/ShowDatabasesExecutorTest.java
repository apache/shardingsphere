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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowDatabasesExecutorTest extends ProxyContextRestorer {
    
    private static final String DATABASE_PATTERN = "database_%s";
    
    private ShowDatabasesExecutor showDatabasesExecutor;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        showDatabasesExecutor = new ShowDatabasesExecutor(new MySQLShowDatabasesStatement());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
            when(database.getName()).thenReturn(String.format(DATABASE_PATTERN, i));
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        showDatabasesExecutor.execute(mockConnectionSession());
        assertThat(showDatabasesExecutor.getQueryResultMetaData().getColumnCount(), is(1));
        assertThat(getActual(), is(getExpected()));
    }
    
    @Test
    public void assertExecuteWithPrefixLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database%");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        showDatabasesExecutor = new ShowDatabasesExecutor(showDatabasesStatement);
        showDatabasesExecutor.execute(mockConnectionSession());
        assertThat(getActual(), is(getExpected()));
    }
    
    private Collection<String> getActual() throws SQLException {
        Map<String, String> result = new ConcurrentHashMap<>(10, 1);
        while (showDatabasesExecutor.getMergedResult().next()) {
            String value = showDatabasesExecutor.getMergedResult().getValue(1, Object.class).toString();
            result.put(value, value);
        }
        return result.keySet();
    }
    
    private Collection<String> getExpected() {
        Map<String, String> result = new ConcurrentHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            String value = String.format(DATABASE_PATTERN, i);
            result.put(value, value);
        }
        return result.keySet();
    }
    
    @Test
    public void assertExecuteWithSuffixLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "%_1");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        showDatabasesExecutor = new ShowDatabasesExecutor(showDatabasesStatement);
        showDatabasesExecutor.execute(mockConnectionSession());
        assertThat(showDatabasesExecutor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (showDatabasesExecutor.getMergedResult().next()) {
            assertThat(showDatabasesExecutor.getMergedResult().getValue(1, Object.class), is("database_1"));
            count++;
        }
        assertThat(count, is(1));
    }
    
    @Test
    public void assertExecuteWithPreciseLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database_9");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        showDatabasesExecutor = new ShowDatabasesExecutor(showDatabasesStatement);
        showDatabasesExecutor.execute(mockConnectionSession());
        assertThat(showDatabasesExecutor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (showDatabasesExecutor.getMergedResult().next()) {
            assertThat(showDatabasesExecutor.getMergedResult().getValue(1, Object.class), is("database_9"));
            count++;
        }
        assertThat(count, is(1));
    }
    
    @Test
    public void assertExecuteWithLikeMatchNone() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "not_exist_database");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        showDatabasesExecutor = new ShowDatabasesExecutor(showDatabasesStatement);
        showDatabasesExecutor.execute(mockConnectionSession());
        assertThat(showDatabasesExecutor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (showDatabasesExecutor.getMergedResult().next()) {
            assertThat(showDatabasesExecutor.getMergedResult().getValue(1, Object.class), is("not_exist_database"));
            count++;
        }
        assertThat(count, is(0));
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", ""));
        return result;
    }
}

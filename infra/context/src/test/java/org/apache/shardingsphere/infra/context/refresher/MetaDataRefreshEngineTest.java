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

package org.apache.shardingsphere.infra.context.refresher;

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.fixture.MetaDataRefresherFixture;
import org.apache.shardingsphere.infra.context.refresher.fixture.MetaDataRefresherSQLStatementFixture;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MetaDataRefreshEngineTest {
    
    private final MetaDataRefreshEngine engine = new MetaDataRefreshEngine(mock(ShardingSphereDatabase.class), new ConfigurationProperties(new Properties()));
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertRefresh() throws Exception {
        SQLStatementContext sqlStatementContext = mockStatementContext();
        int dropTimes = 3;
        for (int i = 0; i < dropTimes; i++) {
            assertTrue(engine.refresh(sqlStatementContext, Collections.emptyList()).isPresent());
        }
        Optional<MetaDataRefresher> refresher = MetaDataRefresherFactory.findInstance(mock(MetaDataRefresherSQLStatementFixture.class).getClass());
        assertTrue(refresher.isPresent());
        assertThat(((MetaDataRefresherFixture) refresher.get()).getCount(), is(dropTimes));
    }
    
    @SuppressWarnings("unchecked")
    private SQLStatementContext<?> mockStatementContext() {
        SQLStatementContext<MetaDataRefresherSQLStatementFixture> result = mock(SQLStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(MetaDataRefresherSQLStatementFixture.class));
        TablesContext tableContext = mock(TablesContext.class, RETURNS_DEEP_STUBS);
        when(tableContext.getSchemaName()).thenReturn(Optional.of("db_schema"));
        when(result.getTablesContext()).thenReturn(tableContext);
        when(result.getDatabaseType()).thenReturn(mock(DatabaseType.class));
        return result;
    }
    
    @Test
    public void assertRefreshWithIgnoredSQLStatement() throws SQLException {
        assertFalse(engine.refresh(mockSelectStatementContext(), Collections.emptyList()).isPresent());
        assertFalse(engine.refresh(mockSelectStatementContext(), Collections.emptyList()).isPresent());
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        return result;
    }
}

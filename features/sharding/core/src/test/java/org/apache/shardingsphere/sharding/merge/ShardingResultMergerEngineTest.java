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

package org.apache.shardingsphere.sharding.merge;

import org.apache.shardingsphere.infra.binder.context.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.engine.merger.impl.TransparentResultMerger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dal.ShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.ddl.ShardingDDLResultMerger;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingResultMergerEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstanceWithSelectStatement() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext sqlStatementContext = new SelectStatementContext(new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()),
                Collections.emptyList(), selectStatement, "foo_db", Collections.emptyList());
        assertThat(new ShardingResultMergerEngine().newInstance("foo_db", databaseType, null, new ConfigurationProperties(new Properties()), sqlStatementContext),
                instanceOf(ShardingDQLResultMerger.class));
    }
    
    @Test
    void assertNewInstanceWithDALStatement() {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        UnknownSQLStatementContext sqlStatementContext = new UnknownSQLStatementContext(mock(ShowStatement.class));
        assertThat(new ShardingResultMergerEngine().newInstance("foo_db", databaseType, null, props, sqlStatementContext), instanceOf(ShardingDALResultMerger.class));
    }
    
    @Test
    void assertNewInstanceWithOtherStatement() {
        InsertStatement insertStatement = new SQL92InsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        InsertStatementContext sqlStatementContext = createInsertStatementContext(insertStatement);
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        assertThat(new ShardingResultMergerEngine().newInstance("foo_db", databaseType, null, props, sqlStatementContext), instanceOf(TransparentResultMerger.class));
    }
    
    private InsertStatementContext createInsertStatementContext(final InsertStatement insertStatement) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new InsertStatementContext(new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), Collections.emptyList(), insertStatement, "foo_db");
    }
    
    @Test
    void assertNewInstanceWithDDLStatement() {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        UnknownSQLStatementContext sqlStatementContext = new UnknownSQLStatementContext(mock(FetchStatement.class));
        assertThat(new ShardingResultMergerEngine().newInstance("foo_db", databaseType, null, props, sqlStatementContext), instanceOf(ShardingDDLResultMerger.class));
    }
}

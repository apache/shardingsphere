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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.merge.engine.merger.impl.TransparentResultMerger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.merge.dal.ShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingResultMergerEngineTest {

    @Test
    public void assertNewInstanceWithSelectStatementForMySQL() {
        assertNewInstanceWithSelectStatement(new MySQLSelectStatement());
    }

    @Test
    public void assertNewInstanceWithSelectStatementForOracle() {
        assertNewInstanceWithSelectStatement(new OracleSelectStatement());
    }

    @Test
    public void assertNewInstanceWithSelectStatementForPostgreSQL() {
        assertNewInstanceWithSelectStatement(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertNewInstanceWithSelectStatementForSQL92() {
        assertNewInstanceWithSelectStatement(new SQL92SelectStatement());
    }

    @Test
    public void assertNewInstanceWithSelectStatementForSQLServer() {
        assertNewInstanceWithSelectStatement(new SQLServerSelectStatement());
    }
    
    private void assertNewInstanceWithSelectStatement(final SelectStatement selectStatement) {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext sqlStatementContext = new SelectStatementContext(Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData),
                Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), null, props, sqlStatementContext), instanceOf(ShardingDQLResultMerger.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        CommonSQLStatementContext<PostgreSQLShowStatement> sqlStatementContext = new CommonSQLStatementContext<>(new PostgreSQLShowStatement());
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), null, props, sqlStatementContext), instanceOf(ShardingDALResultMerger.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        InsertStatementContext sqlStatementContext = createInsertStatementContext(insertStatement);
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), null, props, sqlStatementContext), instanceOf(TransparentResultMerger.class));
    }
    
    private InsertStatementContext createInsertStatementContext(final InsertStatement insertStatement) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        Map<String, ShardingSphereMetaData> metaDataMap = Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData);
        return new InsertStatementContext(metaDataMap, Collections.emptyList(), insertStatement, DefaultSchema.LOGIC_NAME);
    }
}

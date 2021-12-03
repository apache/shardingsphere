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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.refresher;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.fixture.CommonFixtureRule;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.refresher.type.AlterTableFederationMetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterTableFederationMetaDataRefresherTest {
    
    @Test
    public void refreshTable() throws SQLException {
        refreshTable(new MySQLAlterTableStatement());
        refreshTable(new OracleAlterTableStatement());
        refreshTable(new PostgreSQLAlterTableStatement());
        refreshTable(new SQLServerAlterTableStatement());
        refreshTable(new SQL92AlterTableStatement());
    }
    
    private void refreshTable(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), buildSchema());
        FederationMetaDataRefresher<AlterTableStatement> federationMetaDataRefresher = new AlterTableFederationMetaDataRefresher();
        FederationSchemaMetaData schema = buildFederationSchema();
        federationMetaDataRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, metaData, mock(ConfigurationProperties.class));
        assertTrue(schema.getTables().containsKey("t_order"));
    }
    
    @Test
    public void renameTable() throws SQLException {
        renameTable(new MySQLAlterTableStatement());
        renameTable(new OracleAlterTableStatement());
        renameTable(new PostgreSQLAlterTableStatement());
        renameTable(new SQLServerAlterTableStatement());
        renameTable(new SQL92AlterTableStatement());
    }
    
    private void renameTable(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        alterTableStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_new"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds", dataSource);
        when(resource.getDataSources()).thenReturn(dataSourceMap);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getResource()).thenReturn(resource);
        when(metaData.getRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(new CommonFixtureRule()));
        FederationMetaDataRefresher<AlterTableStatement> federationMetaDataRefresher = new AlterTableFederationMetaDataRefresher();
        FederationSchemaMetaData schema = buildFederationSchema();
        federationMetaDataRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, metaData, mock(ConfigurationProperties.class));
        assertFalse(schema.getTables().containsKey("t_order"));
        assertTrue(schema.getTables().containsKey("t_order_new"));
    }
    
    private FederationSchemaMetaData buildFederationSchema() {
        Map<String, TableMetaData> metaData = ImmutableMap.of("t_order", new TableMetaData("t_order", Collections.singletonList(new ColumnMetaData("order_id", 1, false, false, false)),
                        Collections.singletonList(new IndexMetaData("index"))));
        return new FederationSchemaMetaData("t_order", metaData);
    }
    
    private ShardingSphereSchema buildSchema() {
        return new ShardingSphereSchema(ImmutableMap.of("t_order",
                new TableMetaData("t_order", Collections.singletonList(new ColumnMetaData("order_id", 1, false, false, false)), Collections.singletonList(new IndexMetaData("index")))));
    }
}

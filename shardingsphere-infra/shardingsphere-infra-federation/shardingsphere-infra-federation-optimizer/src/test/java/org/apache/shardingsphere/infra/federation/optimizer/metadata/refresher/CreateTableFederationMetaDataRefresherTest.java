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
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.refresher.type.CreateTableFederationMetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateTableFederationMetaDataRefresherTest {
    
    @Mock
    private SchemaBuilderMaterials materials;
    
    @Test
    public void refreshForMySQL() throws SQLException {
        refreshTableWithRule(new MySQLCreateTableStatement());
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        refreshTableWithRule(new OracleCreateTableStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        refreshTableWithRule(new PostgreSQLCreateTableStatement());
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        refreshTableWithRule(new SQL92CreateTableStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        refreshTableWithRule(new SQLServerCreateTableStatement());
    }
    
    private void refreshTableWithRule(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), buildSchema());
        FederationMetaDataRefresher<CreateTableStatement> federationMetaDataRefresher = new CreateTableFederationMetaDataRefresher();
        FederationSchemaMetaData schema = buildFederationSchema();
        federationMetaDataRefresher.refresh(schema, Collections.singletonList("ds"), createTableStatement, metaData, mock(ConfigurationProperties.class));
        assertTrue(schema.getTables().containsKey("t_order"));
        assertTrue(schema.getTables().get("t_order").getColumnNames().contains("order_id"));
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

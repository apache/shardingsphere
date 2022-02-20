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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
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
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class CreateTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refresh(createTableStatement);
    }
    
    private void refresh(final CreateTableStatement sqlStatement) throws SQLException {
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        ShardingSphereMetaData schemaMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema shardingSphereSchema = new ShardingSphereSchema(getTableMetaDataMap("t_order_0"));
        when(schemaMetaData.getSchema()).thenReturn(shardingSphereSchema);
        FederationSchemaMetaData federationSchemaMetaData = mock(FederationSchemaMetaData.class);
        when(federationSchemaMetaData.getName()).thenReturn("sharding_db");
        MetaDataRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        try (MockedStatic<TableMetaDataBuilder> tableMetaDataBuilder = mockStatic(TableMetaDataBuilder.class)) {
            tableMetaDataBuilder.when(() -> TableMetaDataBuilder.load(any(), any())).thenReturn(getTableMetaDataMap("t_order_0"));
            schemaRefresher.refresh(schemaMetaData, federationSchemaMetaData, new HashMap<>(), Collections.emptyList(), sqlStatement, mock(ConfigurationProperties.class));
            assertTrue(shardingSphereSchema.containsTable("t_order_0"));
        }
    }
    
    private Map<String, TableMetaData> getTableMetaDataMap(final String tableName) {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        result.put(tableName, mock(TableMetaData.class));
        return result;
    }
}

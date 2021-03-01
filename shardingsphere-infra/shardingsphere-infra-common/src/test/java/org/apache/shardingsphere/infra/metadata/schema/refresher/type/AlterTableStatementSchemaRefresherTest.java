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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
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

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public final class AlterTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        refresh(new MySQLAlterTableStatement());
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        refresh(new OracleAlterTableStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        refresh(new PostgreSQLAlterTableStatement());
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        refresh(new SQL92AlterTableStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        refresh(new SQLServerAlterTableStatement());
    }
    
    private void refresh(final AlterTableStatement alterTableStatement) throws SQLException {
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        SchemaRefresher<AlterTableStatement> schemaRefresher = new AlterTableStatementSchemaRefresher();
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        schemaRefresher.refresh(schema, Collections.emptyList(), alterTableStatement, mock(SchemaBuilderMaterials.class));
        // TODO mock result of TableMetaDataBuilder.build and assert alter
    }
}

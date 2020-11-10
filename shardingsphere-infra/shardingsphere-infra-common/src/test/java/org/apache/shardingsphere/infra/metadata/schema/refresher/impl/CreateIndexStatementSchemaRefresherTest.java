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

package org.apache.shardingsphere.infra.metadata.schema.refresher.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.refresher.AbstractSchemaRefresherTest;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateIndexStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class CreateIndexStatementSchemaRefresherTest extends AbstractSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        refresh(new MySQLCreateIndexStatement());
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        refresh(new OracleCreateIndexStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        refresh(new PostgreSQLCreateIndexStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        refresh(new SQLServerCreateIndexStatement());
    }
    
    private void refresh(final CreateIndexStatement createIndexStatement) throws SQLException {
        SchemaRefresher<CreateIndexStatement> schemaRefresher = new CreateIndexStatementSchemaRefresher();
        createIndexStatement.setIndex(new IndexSegment(1, 2, new IdentifierValue("t_order_index")));
        createIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        schemaRefresher.refresh(getSchema(), mock(DatabaseType.class), Collections.emptyList(), createIndexStatement, tableName -> Optional.empty());
        assertTrue(getSchema().get("t_order").getIndexes().containsKey("t_order_index"));
    }
    
    @Test
    public void refreshIfIndexIsNullForMySQL() throws SQLException {
        refreshIfIndexIsNull(new MySQLCreateIndexStatement());
    }
    
    @Test
    public void refreshIfIndexIsNullForOracle() throws SQLException {
        refreshIfIndexIsNull(new OracleCreateIndexStatement());
    }
    
    @Test
    public void refreshIfIndexIsNullForPostgreSQL() throws SQLException {
        refreshIfIndexIsNull(new PostgreSQLCreateIndexStatement());
    }
    
    @Test
    public void refreshIfIndexIsNullForSQLServer() throws SQLException {
        refreshIfIndexIsNull(new SQLServerCreateIndexStatement());
    }
    
    private void refreshIfIndexIsNull(final CreateIndexStatement createIndexStatement) throws SQLException {
        SchemaRefresher<CreateIndexStatement> schemaRefresher = new CreateIndexStatementSchemaRefresher();
        createIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        schemaRefresher.refresh(getSchema(), mock(DatabaseType.class), Collections.emptyList(), createIndexStatement, tableName -> Optional.empty());
        assertFalse(getSchema().get("t_order").getIndexes().containsKey("t_order_index"));
    }
}

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

package org.apache.shardingsphere.infra.metadata.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.AbstractMetaDataRefreshStrategyTest;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
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
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class AlterTableStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMySQLAlterTableMetaData() throws SQLException {
        refreshMetaData(new MySQLAlterTableStatement());
    }

    @Test
    public void refreshOracleAlterTableMetaData() throws SQLException {
        refreshMetaData(new OracleAlterTableStatement());
    }

    @Test
    public void refreshPostgreSQLAlterTableMetaData() throws SQLException {
        refreshMetaData(new PostgreSQLAlterTableStatement());
    }

    @Test
    public void refreshSQL92AlterTableMetaData() throws SQLException {
        refreshMetaData(new SQL92AlterTableStatement());
    }

    @Test
    public void refreshSQLServerAlterTableMetaData() throws SQLException {
        refreshMetaData(new SQLServerAlterTableStatement());
    }

    private void refreshMetaData(final AlterTableStatement alterTableStatement) throws SQLException {
        MetaDataRefreshStrategy<AlterTableStatement> metaDataRefreshStrategy = new AlterTableStatementMetaDataRefreshStrategy();
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyMap(), alterTableStatement, tableName -> Optional.of(new PhysicalTableMetaData(
                Collections.singletonList(new PhysicalColumnMetaData("order_id", 1, "String", true, false, false)),
                Collections.singletonList(new PhysicalIndexMetaData("index_alter")))));
        assertTrue(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("index_alter"));
    }
}

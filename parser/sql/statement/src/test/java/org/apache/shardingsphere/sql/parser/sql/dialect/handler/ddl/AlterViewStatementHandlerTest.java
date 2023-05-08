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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AlterViewStatementHandlerTest {
    
    @Test
    void assertGetSelectStatementWithSelectStatementForMySQL() {
        MySQLAlterViewStatement alterViewStatement = new MySQLAlterViewStatement();
        alterViewStatement.setSelect(new MySQLSelectStatement());
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(alterViewStatement);
        assertTrue(selectStatement.isPresent());
    }
    
    @Test
    void assertGetRenameViewWithRenameViewSegmentForPostgreSQL() {
        PostgreSQLAlterViewStatement alterViewStatement = new PostgreSQLAlterViewStatement();
        IdentifierValue identifierValue = new IdentifierValue("t_order");
        TableNameSegment tableNameSegment = new TableNameSegment(0, 6, identifierValue);
        alterViewStatement.setRenameView(new SimpleTableSegment(tableNameSegment));
        Optional<SimpleTableSegment> renameViewSegment = AlterViewStatementHandler.getRenameView(alterViewStatement);
        assertTrue(renameViewSegment.isPresent());
    }
    
    @Test
    void assertGetRenameViewWithoutRenameViewSegmentForPostgreSQL() {
        PostgreSQLAlterViewStatement alterViewStatement = new PostgreSQLAlterViewStatement();
        Optional<SimpleTableSegment> renameViewSegment = AlterViewStatementHandler.getRenameView(alterViewStatement);
        assertFalse(renameViewSegment.isPresent());
    }
    
    @Test
    void assertGetRenameViewWithRenameViewSegmentForOpenGauss() {
        OpenGaussAlterViewStatement alterViewStatement = new OpenGaussAlterViewStatement();
        IdentifierValue identifierValue = new IdentifierValue("t_order");
        TableNameSegment tableNameSegment = new TableNameSegment(0, 6, identifierValue);
        alterViewStatement.setRenameView(new SimpleTableSegment(tableNameSegment));
        Optional<SimpleTableSegment> renameViewSegment = AlterViewStatementHandler.getRenameView(alterViewStatement);
        assertTrue(renameViewSegment.isPresent());
    }
    
    @Test
    void assertGetRenameViewWithoutRenameViewSegmentForOpenGauss() {
        OpenGaussAlterViewStatement alterViewStatement = new OpenGaussAlterViewStatement();
        Optional<SimpleTableSegment> renameViewSegment = AlterViewStatementHandler.getRenameView(alterViewStatement);
        assertFalse(renameViewSegment.isPresent());
    }
    
    @Test
    void assertGetSelectStatementForOtherDatabases() {
        assertFalse(AlterViewStatementHandler.getSelectStatement(new OpenGaussAlterViewStatement()).isPresent());
        assertFalse(AlterViewStatementHandler.getSelectStatement(new OracleAlterViewStatement()).isPresent());
        assertFalse(AlterViewStatementHandler.getSelectStatement(new PostgreSQLAlterViewStatement()).isPresent());
        SQLServerAlterViewStatement alterViewStatement = new SQLServerAlterViewStatement();
        alterViewStatement.setSelect(mock(SQLServerSelectStatement.class));
        assertTrue(AlterViewStatementHandler.getSelectStatement(alterViewStatement).isPresent());
    }
    
    @Test
    void assertGetRenameViewForOtherDatabases() {
        assertFalse(AlterViewStatementHandler.getRenameView(new MySQLAlterViewStatement()).isPresent());
        assertFalse(AlterViewStatementHandler.getRenameView(new OracleAlterViewStatement()).isPresent());
        assertFalse(AlterViewStatementHandler.getRenameView(new SQLServerAlterViewStatement()).isPresent());
    }
}

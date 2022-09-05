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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterIndexStatement;
import org.junit.Test;

import java.util.Optional;

public final class AlterIndexStatementHandlerTest {
    
    @Test
    public void assertGetSimpleTableSegmentWithSimpleTableSegmentForSQLServer() {
        SQLServerAlterIndexStatement alterIndexStatement = new SQLServerAlterIndexStatement();
        alterIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        Optional<SimpleTableSegment> simpleTableSegment = AlterIndexStatementHandler.getSimpleTableSegment(alterIndexStatement);
        assertTrue(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentWithoutSimpleTableSegmentForSQLServer() {
        SQLServerAlterIndexStatement alterIndexStatement = new SQLServerAlterIndexStatement();
        Optional<SimpleTableSegment> simpleTableSegment = AlterIndexStatementHandler.getSimpleTableSegment(alterIndexStatement);
        assertFalse(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetRenameIndexSegmentWithRenameIndexSegmentForPostgreSQL() {
        PostgreSQLAlterIndexStatement alterIndexStatement = new PostgreSQLAlterIndexStatement();
        alterIndexStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue(""))));
        Optional<IndexSegment> indexSegment = AlterIndexStatementHandler.getRenameIndexSegment(alterIndexStatement);
        assertTrue(indexSegment.isPresent());
    }
    
    @Test
    public void assertGetRenameIndexSegmentWithoutRenameIndexSegmentForPostgreSQL() {
        PostgreSQLAlterIndexStatement alterIndexStatement = new PostgreSQLAlterIndexStatement();
        Optional<IndexSegment> indexSegment = AlterIndexStatementHandler.getRenameIndexSegment(alterIndexStatement);
        assertFalse(indexSegment.isPresent());
    }
    
    @Test
    public void assertGetRenameIndexSegmentForOpenGauss() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("")));
        OpenGaussAlterIndexStatement alterIndexStatement = new OpenGaussAlterIndexStatement();
        alterIndexStatement.setRenameIndex(indexSegment);
        Optional<IndexSegment> actual = AlterIndexStatementHandler.getRenameIndexSegment(alterIndexStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(indexSegment));
    }
    
    @Test
    public void assertGetSimpleTableSegmentForOtherDatabases() {
        assertFalse(AlterIndexStatementHandler.getSimpleTableSegment(new OpenGaussAlterIndexStatement()).isPresent());
        assertFalse(AlterIndexStatementHandler.getSimpleTableSegment(new OracleAlterIndexStatement()).isPresent());
        assertFalse(AlterIndexStatementHandler.getSimpleTableSegment(new PostgreSQLAlterIndexStatement()).isPresent());
    }
    
    @Test
    public void assertGetRenameIndexSegmentForOtherDatabases() {
        assertFalse(AlterIndexStatementHandler.getRenameIndexSegment(new OracleAlterIndexStatement()).isPresent());
        assertFalse(AlterIndexStatementHandler.getRenameIndexSegment(new SQLServerAlterIndexStatement()).isPresent());
    }
}

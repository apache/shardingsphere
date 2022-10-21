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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussCopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.junit.Test;

public final class CopyStatementHandlerTest {
    
    @Test
    public void assertGetPrepareStatementQuerySegmentWithSegmentForPostgreSQL() {
        PostgreSQLCopyStatement postgreSQLCopyStatement = new PostgreSQLCopyStatement();
        postgreSQLCopyStatement.setPrepareStatementQuerySegment(new PrepareStatementQuerySegment(0, 2));
        Optional<PrepareStatementQuerySegment> actual = CopyStatementHandler.getPrepareStatementQuerySegment(postgreSQLCopyStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(postgreSQLCopyStatement.getPrepareStatementQuerySegment().get()));
    }
    
    @Test
    public void assertGetPrepareStatementQuerySegmentWithoutSegmentForPostgreSQL() {
        PostgreSQLCopyStatement postgreSQLCopyStatement = new PostgreSQLCopyStatement();
        Optional<PrepareStatementQuerySegment> actual = CopyStatementHandler.getPrepareStatementQuerySegment(postgreSQLCopyStatement);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertGetColumnsWithSegmentForPostgreSQL() {
        PostgreSQLCopyStatement postgreSQLCopyStatement = new PostgreSQLCopyStatement();
        postgreSQLCopyStatement.getColumns().add(new ColumnSegment(0, 2, new IdentifierValue("identifier")));
        Collection<ColumnSegment> actual = CopyStatementHandler.getColumns(postgreSQLCopyStatement);
        assertFalse(actual.isEmpty());
        assertThat(actual, is(postgreSQLCopyStatement.getColumns()));
    }
    
    @Test
    public void assertGetColumnsWithoutSegmentForPostgreSQL() {
        PostgreSQLCopyStatement postgreSQLCopyStatement = new PostgreSQLCopyStatement();
        Collection<ColumnSegment> actual = CopyStatementHandler.getColumns(postgreSQLCopyStatement);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertGetPrepareStatementQuerySegmentForOpenGauss() {
        assertFalse(CopyStatementHandler.getPrepareStatementQuerySegment(new OpenGaussCopyStatement()).isPresent());
    }
    
    @Test
    public void assertGetColumnsForOpenGauss() {
        assertTrue(CopyStatementHandler.getColumns(new OpenGaussCopyStatement()).isEmpty());
    }
}

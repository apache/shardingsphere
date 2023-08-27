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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussCopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyStatementHandlerTest {
    
    @Test
    void assertGetPrepareStatementQuerySegmentWithSegmentForPostgreSQL() {
        PostgreSQLCopyStatement copyStatement = new PostgreSQLCopyStatement();
        copyStatement.setPrepareStatementQuerySegment(new PrepareStatementQuerySegment(0, 2));
        Optional<PrepareStatementQuerySegment> actual = CopyStatementHandler.getPrepareStatementQuerySegment(copyStatement);
        assertTrue(actual.isPresent());
        Preconditions.checkState(copyStatement.getPrepareStatementQuerySegment().isPresent());
        assertThat(actual.get(), is(copyStatement.getPrepareStatementQuerySegment().get()));
    }
    
    @Test
    void assertGetPrepareStatementQuerySegmentWithoutSegmentForPostgreSQL() {
        PostgreSQLCopyStatement copyStatement = new PostgreSQLCopyStatement();
        Optional<PrepareStatementQuerySegment> actual = CopyStatementHandler.getPrepareStatementQuerySegment(copyStatement);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetColumnsWithSegmentForPostgreSQL() {
        PostgreSQLCopyStatement copyStatement = new PostgreSQLCopyStatement();
        copyStatement.getColumns().add(new ColumnSegment(0, 2, new IdentifierValue("identifier")));
        Collection<ColumnSegment> actual = CopyStatementHandler.getColumns(copyStatement);
        assertFalse(actual.isEmpty());
        assertThat(actual, is(copyStatement.getColumns()));
    }
    
    @Test
    void assertGetColumnsWithoutSegmentForPostgreSQL() {
        PostgreSQLCopyStatement copyStatement = new PostgreSQLCopyStatement();
        Collection<ColumnSegment> actual = CopyStatementHandler.getColumns(copyStatement);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertGetPrepareStatementQuerySegmentForOpenGauss() {
        assertFalse(CopyStatementHandler.getPrepareStatementQuerySegment(new OpenGaussCopyStatement()).isPresent());
    }
    
    @Test
    void assertGetColumnsForOpenGauss() {
        assertTrue(CopyStatementHandler.getColumns(new OpenGaussCopyStatement()).isEmpty());
    }
}

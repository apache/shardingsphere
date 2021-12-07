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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptAssignmentParameterRewriterTest {

    @InjectMocks
    private EncryptAssignmentParameterRewriter encryptAssignmentParameterRewriter;

    @Test
    public void isNeedRewriteForEncryptForUpdateContextTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(updateStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForInsertContextTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);


        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));

        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(insertStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForDeleteContextTest() {
        final DeleteStatementContext deleteStatementContext = mock(DeleteStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(deleteStatementContext);
        assertFalse(result);
    }
}

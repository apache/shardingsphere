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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyAssignmentTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.LiteralGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ParameterMarkerGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GeneratedKeyAssignmentTokenGeneratorTest {
    
    @Test
    public void assertGenerateSQLToken() {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class, RETURNS_DEEP_STUBS);
        when(generatedKeyContext.getColumnName()).thenReturn("testColumnName");
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.singleton(4));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        when(setAssignmentSegment.getStopIndex()).thenReturn(2);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        List<Object> testParameters = new LinkedList<>();
        GeneratedKeyAssignmentTokenGenerator generator = new GeneratedKeyAssignmentTokenGenerator();
        generator.setParameters(testParameters);
        assertThat(generator.generateSQLToken(insertStatementContext), instanceOf(LiteralGeneratedKeyAssignmentToken.class));
        testParameters.add("testObject");
        assertThat(generator.generateSQLToken(insertStatementContext), instanceOf(ParameterMarkerGeneratedKeyAssignmentToken.class));
    }
}

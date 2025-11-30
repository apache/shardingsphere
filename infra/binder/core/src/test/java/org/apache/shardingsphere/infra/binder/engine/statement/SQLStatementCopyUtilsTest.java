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

package org.apache.shardingsphere.infra.binder.engine.statement;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for SQLStatementCopyUtils.
 */
class SQLStatementCopyUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private SQLStatement originalStatement;
    
    private SQLStatement targetStatement;
    
    @BeforeEach
    void setUp() {
        originalStatement = new SQLStatement(databaseType);
        targetStatement = new SQLStatement(databaseType);
    }
    
    @Test
    void assertCopyAttributes() {
        ParameterMarkerExpressionSegment param = new ParameterMarkerExpressionSegment(0, 0, 1);
        originalStatement.addParameterMarkers(Collections.singleton(param));
        originalStatement.getVariableNames().add("variable");
        CommentSegment comment = new CommentSegment("comment", 0, 6);
        originalStatement.getComments().add(comment);
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        assertThat(targetStatement.getParameterCount(), is(1));
        assertThat(targetStatement.getParameterMarkers().size(), is(1));
        assertTrue(targetStatement.getParameterMarkers().contains(param));
        assertThat(targetStatement.getVariableNames().size(), is(1));
        assertTrue(targetStatement.getVariableNames().contains("variable"));
        assertThat(targetStatement.getComments().size(), is(1));
        assertTrue(targetStatement.getComments().contains(comment));
    }
}

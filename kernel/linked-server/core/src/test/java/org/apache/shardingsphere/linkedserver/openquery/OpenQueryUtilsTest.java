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

package org.apache.shardingsphere.linkedserver.openquery;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenQueryUtilsTest {
    
    @Test
    void assertIsOpenQuery() {
        FunctionTableSegment funcTableSegment = createOpenQueryFunctionTableSegment();
        assertTrue(OpenQueryUtils.isOpenQuery(funcTableSegment));
    }
    
    @Test
    void assertIsNotOpenQuery() {
        FunctionSegment funcSeg = new FunctionSegment(0, 20, "SOME_OTHER_FUNC", "SOME_OTHER_FUNC()");
        FunctionTableSegment funcTableSegment = new FunctionTableSegment(0, 20, funcSeg);
        assertFalse(OpenQueryUtils.isOpenQuery(funcTableSegment));
    }
    
    @Test
    void assertIsOpenQueryCaseInsensitive() {
        FunctionSegment funcSeg = new FunctionSegment(0, 50, "openquery", "openquery(Server, 'SELECT 1')");
        funcSeg.getParameters().add(new ColumnSegment(10, 15, new IdentifierValue("Server")));
        funcSeg.getParameters().add(new LiteralExpressionSegment(19, 28, "SELECT 1"));
        FunctionTableSegment funcTableSegment = new FunctionTableSegment(0, 50, funcSeg);
        assertTrue(OpenQueryUtils.isOpenQuery(funcTableSegment));
    }
    
    @Test
    void assertExtractLinkedServerName() {
        FunctionSegment funcSeg = createOpenQueryFunctionSegment();
        assertThat(OpenQueryUtils.extractLinkedServerName(funcSeg), is("MyLinkedServer"));
    }
    
    @Test
    void assertExtractInnerSQLSegment() {
        FunctionSegment funcSeg = createOpenQueryFunctionSegment();
        Optional<LiteralExpressionSegment> result = OpenQueryUtils.extractInnerSQLSegment(funcSeg);
        assertTrue(result.isPresent());
        assertThat(result.get().getLiterals().toString(), is("SELECT GroupName FROM Department"));
    }
    
    @Test
    void assertExtractInnerSQLSegmentWithInsufficientParams() {
        FunctionSegment funcSeg = new FunctionSegment(0, 30, "OPENQUERY", "OPENQUERY(Server)");
        funcSeg.getParameters().add(new ColumnSegment(10, 15, new IdentifierValue("Server")));
        assertFalse(OpenQueryUtils.extractInnerSQLSegment(funcSeg).isPresent());
    }
    
    @Test
    void assertDecodeTSqlEscaping() {
        assertThat(OpenQueryUtils.decodeTSqlEscaping("SELECT Note FROM T WHERE Note = ''test''"), is("SELECT Note FROM T WHERE Note = 'test'"));
    }
    
    @Test
    void assertDecodeTSqlEscapingNoEscapes() {
        assertThat(OpenQueryUtils.decodeTSqlEscaping("SELECT 1"), is("SELECT 1"));
    }
    
    @Test
    void assertEncodeTSqlEscaping() {
        assertThat(OpenQueryUtils.encodeTSqlEscaping("SELECT Note FROM T WHERE Note = 'test'"), is("SELECT Note FROM T WHERE Note = ''test''"));
    }
    
    @Test
    void assertEncodeTSqlEscapingNoQuotes() {
        assertThat(OpenQueryUtils.encodeTSqlEscaping("SELECT 1"), is("SELECT 1"));
    }
    
    @Test
    void assertRoundTripEscaping() {
        String original = "SELECT Note FROM T WHERE Note = ''it''s a test''";
        String decoded = OpenQueryUtils.decodeTSqlEscaping(original);
        String reEncoded = OpenQueryUtils.encodeTSqlEscaping(decoded);
        assertThat(reEncoded, is(original));
    }
    
    private FunctionTableSegment createOpenQueryFunctionTableSegment() {
        FunctionSegment funcSeg = createOpenQueryFunctionSegment();
        return new FunctionTableSegment(0, 60, funcSeg);
    }
    
    private FunctionSegment createOpenQueryFunctionSegment() {
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        funcSeg.getParameters().add(new ColumnSegment(10, 23, new IdentifierValue("MyLinkedServer")));
        funcSeg.getParameters().add(new LiteralExpressionSegment(27, 58, "SELECT GroupName FROM Department"));
        return funcSeg;
    }
}

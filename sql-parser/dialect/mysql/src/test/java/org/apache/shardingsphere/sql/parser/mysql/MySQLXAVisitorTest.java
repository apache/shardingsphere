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

package org.apache.shardingsphere.sql.parser.mysql;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.mysql.parser.MySQLLexer;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.impl.MySQLTCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLXAStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class MySQLXAVisitorTest {
    
    private static Collection<Object[]> testUnits = new LinkedList<>();
    
    static {
        testUnits.add(new Object[]{"xa_start", "XA START 0x6262,b'000',7", "START", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_begin", "XA BEGIN 0x6262,b'000',7", "BEGIN", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_end", "XA END 0x6262,b'000',7", "END", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_prepare", "XA PREPARE 0x6262,b'000',7", "PREPARE", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_commit", "XA COMMIT 0x6262,b'000',7", "COMMIT", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_rollback", "XA ROLLBACK 0x6262,b'000',7", "ROLLBACK", "0x6262,b'000',7"});
        testUnits.add(new Object[]{"xa_recover", "XA RECOVER", "RECOVER", null});
    }
    
    private final String caseId;
    
    private final String inputSql;
    
    private final String op;
    
    private final String xid;
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return testUnits;
    }
    
    @Test
    public void assertXA() {
        CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(inputSql.toCharArray()));
        MySQLLexer lexer = new MySQLLexer(CodePointCharStream.fromBuffer(buffer));
        MySQLStatementParser parser = new MySQLStatementParser(new CommonTokenStream(lexer));
        MySQLTCLStatementSQLVisitor visitor = new MySQLTCLStatementSQLVisitor();
        MySQLXAStatement xa = (MySQLXAStatement) visitor.visitXa(parser.xa());
        assertThat("parse error", parser.getNumberOfSyntaxErrors(), is(0));
        assertThat("xa op error", xa.getOp(), is(op));
        assertThat("xa xid error", xa.getXid(), is(xid));
    }
}

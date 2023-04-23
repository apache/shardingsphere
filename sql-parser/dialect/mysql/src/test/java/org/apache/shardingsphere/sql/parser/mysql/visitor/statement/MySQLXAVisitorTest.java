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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement;

import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.mysql.parser.MySQLLexer;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLTCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLXAStatement;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.nio.CharBuffer;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLXAVisitorTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertXA(final String caseId, final String inputSQL, final String operation, final String xid) {
        CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(inputSQL.toCharArray()));
        MySQLLexer lexer = new MySQLLexer(CodePointCharStream.fromBuffer(buffer));
        MySQLStatementParser parser = new MySQLStatementParser(new CommonTokenStream(lexer));
        MySQLTCLStatementVisitor visitor = new MySQLTCLStatementVisitor();
        MySQLXAStatement xaStatement = (MySQLXAStatement) visitor.visitXa(parser.xa());
        assertThat("XA parse error.", parser.getNumberOfSyntaxErrors(), is(0));
        assertThat("XA operation error.", xaStatement.getOp(), is(operation));
        assertThat("XA xid error.", xaStatement.getXid(), is(xid));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("xa_start", "XA START 0x6262,b'000',7", "START", "0x6262,b'000',7"),
                    Arguments.of("xa_begin", "XA BEGIN 0x6262,b'000',7", "BEGIN", "0x6262,b'000',7"),
                    Arguments.of("xa_end", "XA END 0x6262,b'000',7", "END", "0x6262,b'000',7"),
                    Arguments.of("xa_commit", "XA COMMIT 0x6262,b'000',7", "COMMIT", "0x6262,b'000',7"),
                    Arguments.of("xa_rollback", "XA ROLLBACK 0x6262,b'000',7", "ROLLBACK", "0x6262,b'000',7"),
                    Arguments.of("xa_recover", "XA RECOVER", "RECOVER", null));
        }
    }
}

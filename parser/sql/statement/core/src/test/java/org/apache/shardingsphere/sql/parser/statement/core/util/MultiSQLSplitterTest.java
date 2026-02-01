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

package org.apache.shardingsphere.sql.parser.statement.core.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MultiSQLSplitterTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Fixture");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasSameTypeArguments")
    void assertHasSameTypeMultiStatements(final String name, final SQLStatement sqlStatement, final Collection<String> sqls, final boolean expected) {
        assertThat(name, MultiSQLSplitter.hasSameTypeMultiStatements(sqlStatement, sqls), is(expected));
    }
    
    private static Stream<Arguments> provideHasSameTypeArguments() {
        return Stream.of(
                Arguments.of("nonDmlSample", new UpdateStatement(DATABASE_TYPE), Arrays.asList("select * from t_order;", "select * from t_order_item;"), false),
                Arguments.of("singleStatementFalse", new UpdateStatement(DATABASE_TYPE), Collections.singletonList("update t_order set status='OK' where id=1"), false),
                Arguments.of("insertWithBlockComment", new InsertStatement(DATABASE_TYPE),
                        Arrays.asList("   /*comment*/ INSERT INTO t_order VALUES (1);", "/*remark*/ insert into t_order values (2)"), true),
                Arguments.of("updateWithDashComment", new UpdateStatement(DATABASE_TYPE),
                        Arrays.asList("-- comment before\r\nupdate t_order set status='PAID' where id=1;", "-- \t\nupdate t_order set status='FAIL' where id=2;"), true),
                Arguments.of("deleteWithHashComment", new DeleteStatement(DATABASE_TYPE),
                        Arrays.asList("# comment before\n delete from t_order where id=1;", "#\t\n delete from t_order where id=2;"), true),
                Arguments.of("hashCommentWithCRLF", new DeleteStatement(DATABASE_TYPE),
                        Arrays.asList("# comment\r\ndelete from t_order where id=1;", "# comment\r\ndelete from t_order where id=2;"), true),
                Arguments.of("hashCommentWithCROnly", new DeleteStatement(DATABASE_TYPE),
                        Arrays.asList("# comment\rdelete from t_order where id=1;", "# comment\rdelete from t_order where id=2;"), true),
                Arguments.of("updateTypeMismatch", new UpdateStatement(DATABASE_TYPE), Arrays.asList("update t_order set status='PAID' where id=1;", "select * from t_order"), false),
                Arguments.of("unterminatedBlockComment", new InsertStatement(DATABASE_TYPE), Arrays.asList("/* incomplete comment", "insert into t_order values (1);"), false),
                Arguments.of("dashCommentOnlySegment", new UpdateStatement(DATABASE_TYPE), Arrays.asList("--", "update t_order set status='DONE' where id=1;"), false),
                Arguments.of("whitespaceOnlySegment", new UpdateStatement(DATABASE_TYPE), Arrays.asList("   \t ", "update t_order set status='DONE' where id=1;"), false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSplitArguments")
    void assertSplit(final String name, final String sql, final Collection<String> expected) {
        assertThat(name, MultiSQLSplitter.split(sql), is(expected));
    }
    
    private static Stream<Arguments> provideSplitArguments() {
        return Stream.of(
                Arguments.of("nullSqlReturnsEmpty", null, Collections.emptyList()),
                Arguments.of("emptySqlReturnsEmpty", "", Collections.emptyList()),
                Arguments.of("semicolonInsideLiteral", "update t_order set status='WAIT;PAID' where id=1", Collections.singletonList("update t_order set status='WAIT;PAID' where id=1")),
                Arguments.of("multipleStatementsWithTrailingSemicolon", "update t_order set status='PAID' where id=1; update t_order set status='FAILED' where id=2;",
                        Arrays.asList("update t_order set status='PAID' where id=1", "update t_order set status='FAILED' where id=2")),
                Arguments.of("hintBlockComment", "/* ShardingSphere hint: dataSourceName=foo_ds; foo=bar */ delete from t_order where id=1;",
                        Collections.singletonList("/* ShardingSphere hint: dataSourceName=foo_ds; foo=bar */ delete from t_order where id=1")),
                Arguments.of("dashCommentIgnoresSemicolon", "-- comment; still comment\r\nupdate t_order set status=1; insert into t_order values (2);",
                        Arrays.asList("-- comment; still comment\r\nupdate t_order set status=1", "insert into t_order values (2)")),
                Arguments.of("hashCommentIgnoresSemicolon", "# comment ; still comment\nupdate t_order set status=1;",
                        Collections.singletonList("# comment ; still comment\nupdate t_order set status=1")),
                Arguments.of("blockCommentInsideStatement", "select /* block; comment */ 1; select /*another*/ 2;", Arrays.asList("select /* block; comment */ 1", "select /*another*/ 2")),
                Arguments.of("repeatedQuotesInsideLiteral", "insert into t_order values ('it''s;ok');", Collections.singletonList("insert into t_order values ('it''s;ok')")),
                Arguments.of("escapedQuoteInsideLiteral", "insert into t_order values ('need\\'escape;');", Collections.singletonList("insert into t_order values ('need\\'escape;')")),
                Arguments.of("backtickIdentifiersWithSemicolon", "insert into `t;order` values (1);", Collections.singletonList("insert into `t;order` values (1)")),
                Arguments.of("doubleQuoteIdentifiersWithSemicolon", "insert into \"T;ORDER\" values (1);", Collections.singletonList("insert into \"T;ORDER\" values (1)")),
                Arguments.of("unterminatedStringHandled", "update t_order set status='OPEN\\", Collections.singletonList("update t_order set status='OPEN\\")),
                Arguments.of("doubleDashWithoutWhitespaceTreatedAsText", "--not comment; update t_order set status=1;", Arrays.asList("--not comment", "update t_order set status=1")),
                Arguments.of("singleTrailingDash", "update t_order set price = price -", Collections.singletonList("update t_order set price = price -")));
    }
}

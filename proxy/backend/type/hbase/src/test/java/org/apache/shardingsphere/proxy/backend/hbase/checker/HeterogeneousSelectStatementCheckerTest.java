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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBaseProperties;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeterogeneousSelectStatementCheckerTest {
    
    @BeforeEach
    void setUp() {
        HBaseProperties props = mock(HBaseProperties.class);
        when(props.getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE)).thenReturn(5000L);
        HBaseContext.getInstance().setProps(props);
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(SupportedTestCaseArgumentsProvider.class)
    void assertExecuteSuccess(final String name, final String sql) {
        assertDoesNotThrow(() -> HBaseCheckerFactory.newInstance(HBaseSupportedSQLStatement.parseSQLStatement(sql)).execute());
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(UnsupportedTestCaseArgumentsProvider.class)
    void assertExecuteFailed(final String name, final String sql, final String expectedErrorMessage) {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is(expectedErrorMessage));
    }
    
    private static class SupportedTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("pointSelect", HBaseSupportedSQLStatement.getSelectStatement()),
                    Arguments.of("parameterMarker", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (?, ?, ?)"),
                    Arguments.of("selectIn", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (1, 2, 3)"),
                    Arguments.of("between", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey BETWEEN 1 AND 2"),
                    Arguments.of("useCrc32", "SELECT /*+ HBase */ crc32(concat_ws('#',rowKey)) FROM t_order WHERE rowKey IN (1, 2, 3)"),
                    Arguments.of("useCrc32", "SELECT /*+ HBase */ crc32(concat_ws('#',rowKey)) FROM t_order WHERE rowKey IN (1, 2, 3)"));
        }
    }
    
    private static class UnsupportedTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("largeRowCount", "SELECT /*+ hbase */ * FROM t_order WHERE id = 1 LIMIT 5001", "Row count must less than 5000."),
                    Arguments.of("limit", "SELECT /*+ hbase */ * FROM t_order WHERE id = 1 LIMIT 5 OFFSET 3", "Do not supported offset segment."),
                    Arguments.of("lock", "SELECT /*+ hbase */ * FROM t_order WHERE id = 1 lock in share mode", "Do not supported lock segment."),
                    Arguments.of("function", "SELECT /*+ HBase */ sum(score) FROM person", "Only supported shorthand, column and crc32 expression projections."),
                    Arguments.of("join", "SELECT /*+ HBase */ * FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id", "Only supported simple table segment."),
                    Arguments.of("multipleIn", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (?, ?) AND id IN (?, ?)", "Left segment must column segment."),
                    Arguments.of("errorKey", "SELECT /*+ HBase */ * FROM t_order WHERE age IN (1, 2, 3)", "age is not a allowed key."),
                    Arguments.of("notIn", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey NOT IN (1, 2, 3)", "Do not supported `not in`."),
                    Arguments.of("errorInExpression", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (SELECT rowKey FROM t_order_item)", "Only supported list expression."),
                    Arguments.of("notBetween", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey not BETWEEN 1 AND 2", "Do not supported `not between...and...`"),
                    Arguments.of("betweenErrorKey", "SELECT /*+ HBase */ * FROM t_order WHERE age BETWEEN 1 AND 2", "age is not a allowed key."),
                    Arguments.of("errorBetweenExpr", "SELECT /*+ HBase */ * FROM t_order WHERE rowKey BETWEEN 1 AND now()", "Between expr must literal or parameter marker."),
                    Arguments.of("groupBy", "SELECT /*+ HBase */ * FROM t_order GROUP BY order_id", "Do not supported group by segment."),
                    Arguments.of("notAllowedOperator", "SELECT /*+ hbase */ * FROM t_order WHERE rowKey != 1", "Only Supported `=` operator."),
                    Arguments.of("notAllowedColumn", "SELECT /*+ hbase */ * FROM t_order WHERE age = 1", "age is not a allowed key."),
                    Arguments.of("multipleExpressions", "SELECT /*+ hbase */ * FROM t_order WHERE rowKey = 1 AND age = 2", "Do not supported multiple expressions."),
                    Arguments.of("notColumnExpression", "SELECT /*+ hbase */ * FROM t_order WHERE 1 = 1", "Left segment must column segment."),
                    Arguments.of("parameterMarker", "SELECT /*+ hbase */ rowKey, name, ? FROM t_order WHERE rowKey = 'kid'", "Only supported shorthand, column and crc32 expression projections."));
        }
    }
}

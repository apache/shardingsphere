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

import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
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

class HeterogeneousInsertStatementCheckerTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(SupportedTestCaseArgumentsProvider.class)
    void assertExecuteSuccess(final String name, final String sql) {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        assertDoesNotThrow(() -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
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
            return Stream.of(Arguments.of(
                    "standard", HBaseSupportedSQLStatement.getInsertStatement(),
                    "literalAndParameterMarker", "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', '2017-08-08')"));
        }
    }
    
    private static class UnsupportedTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("withoutRowKey", "INSERT /*+ HBase */ INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)", "First column must be rowKey."),
                    Arguments.of("withoutColumns", "INSERT /*+ HBase */ INTO t_order VALUES (?, ?, ?)", "The inserted column must be explicitly specified."),
                    Arguments.of("withMultipleRowKey", "INSERT /*+ HBase */ INTO t_order (rowKey, id, status) VALUES (?, ?, ?)", "Cannot contain multiple rowKeys."),
                    Arguments.of("onDuplicateKey",
                            "INSERT /*+ HBase */ INTO t_order (rowKey, user_id, status) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE status = ?", "Do not supported ON DUPLICATE KEY UPDATE"),
                    Arguments.of("function",
                            "INSERT /*+ HBase */ INTO t_order_item (rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', now())", "Value must is literal or parameter marker."),
                    Arguments.of("subQuery",
                            "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id) select rowKey, order_id, user_id from t_order", "Do not supported `insert into...select...`"));
        }
    }
}

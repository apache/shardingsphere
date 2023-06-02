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

class HeterogeneousDeleteStatementCheckerTest {
    
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
            return Stream.of(Arguments.of("standard", HBaseSupportedSQLStatement.getDeleteStatement()));
        }
    }
    
    private static class UnsupportedTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("operatorIsNotEqual", "delete /*+ hbase */ from t_test_order where rowKey > 1", "Only Supported `=` operator."),
                    Arguments.of("columnIsNotRowKey", "delete /*+ hbase */ from t_test_order where age = 1", "age is not a allowed key."),
                    Arguments.of("leftIsNotColumn", "delete /*+ hbase */ from t_test_order where 1 = 1", "Left segment must column segment."),
                    Arguments.of("MultipleExpressions", "DELETE /*+ hbase */ FROM t_order WHERE order_id = ? AND user_id = ? AND status=?", "Do not supported multiple expressions."),
                    Arguments.of("between", "DELETE /*+ hbase */ FROM t_order WHERE rowKey between 1 and 5", "Only support binary operation expression."),
                    Arguments.of("withoutWhere", "DELETE /*+ hbase */ FROM t_order", "Must contain where segment."));
        }
    }
}

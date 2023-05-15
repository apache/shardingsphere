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
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeterogeneousDeleteStatementCheckerTest {
    
    @Test
    void assertExecuteDeleteStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getDeleteStatement());
        assertDoesNotThrow(() -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
    }
    
    @Test
    void assertOperatorIsNotEqual() {
        String sql = "delete /*+ hbase */ from t_test_order where rowKey > 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only Supported `=` operator"));
    }
    
    @Test
    void assertColumnIsNotRowKey() {
        String sql = "delete /*+ hbase */ from t_test_order where age = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("age is not a allowed key"));
    }
    
    @Test
    void assertLeftIsNotColumn() {
        String sql = "delete /*+ hbase */ from t_test_order where 1 = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("left segment must is ColumnSegment"));
    }
    
    @Test
    void assertMultiExpression() {
        String sql = "DELETE /*+ hbase */ FROM t_order WHERE order_id = ? AND user_id = ? AND status=?";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported Multiple expressions"));
    }
    
    @Test
    void assertWithBetweenExpression() {
        String sql = "DELETE /*+ hbase */ FROM t_order WHERE rowKey between 1 and 5";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only Support BinaryOperationExpression"));
    }
    
    @Test
    void assertNotWhereSegment() {
        String sql = "DELETE /*+ hbase */ FROM t_order";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Must Have Where Segment"));
    }
}

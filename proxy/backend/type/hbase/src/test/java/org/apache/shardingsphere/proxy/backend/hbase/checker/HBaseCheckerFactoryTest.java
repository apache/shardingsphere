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

import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class HBaseCheckerFactoryTest {
    
    @Test
    void assertExecuteSelectStatement() {
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        HeterogeneousSQLStatementChecker actual = HBaseCheckerFactory.newInstance(sqlStatement);
        assertThat(actual, instanceOf(HeterogeneousSelectStatementChecker.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertExecuteInsertStatement() {
        SQLStatement sqlStatement = mock(MySQLInsertStatement.class);
        HeterogeneousSQLStatementChecker actual = HBaseCheckerFactory.newInstance(sqlStatement);
        assertThat(actual, instanceOf(HeterogeneousInsertStatementChecker.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertExecuteUpdateStatement() {
        SQLStatement sqlStatement = mock(MySQLUpdateStatement.class);
        HeterogeneousSQLStatementChecker actual = HBaseCheckerFactory.newInstance(sqlStatement);
        assertThat(actual, instanceOf(HeterogeneousUpdateStatementChecker.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertExecuteDeleteStatement() {
        SQLStatement sqlStatement = mock(MySQLDeleteStatement.class);
        HeterogeneousSQLStatementChecker actual = HBaseCheckerFactory.newInstance(sqlStatement);
        assertThat(actual, instanceOf(HeterogeneousDeleteStatementChecker.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertExecuteOtherStatement() {
        SQLStatement sqlStatement = mock(MySQLShowCreateTableStatement.class);
        HeterogeneousSQLStatementChecker actual = HBaseCheckerFactory.newInstance(sqlStatement);
        assertThat(actual, instanceOf(CommonHeterogeneousSQLStatementChecker.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
}

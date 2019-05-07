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

package org.apache.shardingsphere.core.parse.integrate.asserts;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.integrate.asserts.condition.ConditionAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.SQLCasesLoader;

/**
 * SQL statement assert.
 *
 * @author duhongjun
 */
public final class EncryptSQLStatementAssert {
    
    private final SQLStatement actual;
    
    private final ParserResult expected;
    
    private final TableAssert tableAssert;
    
    private final ConditionAssert conditionAssert;
    
    private final DatabaseType databaseType;
    
    private final InsertNamesAndValuesAssert insertNamesAndValuesAssert;
    
    public EncryptSQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType, final DatabaseType databaseType) {
        this(actual, sqlCaseId, sqlCaseType, SQLCasesLoader.getInstance(), ParserResultSetLoader.getInstance(), databaseType);
    }
    
    public EncryptSQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType, final SQLCasesLoader sqlLoader, final ParserResultSetLoader parserResultSetLoader, final DatabaseType databaseType) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlLoader, parserResultSetLoader, sqlCaseId, sqlCaseType);
        this.actual = actual;
        expected = parserResultSetLoader.getParserResult(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        conditionAssert = new ConditionAssert(assertMessage);
        insertNamesAndValuesAssert = new InsertNamesAndValuesAssert(assertMessage, sqlCaseType);
        this.databaseType = databaseType;
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.getTables(), expected.getTables());
        if (DatabaseType.MySQL == databaseType) {
            conditionAssert.assertOrCondition(actual.getEncryptConditions().getOrCondition(), expected.getEncryptCondition());
        }
        if (actual instanceof InsertStatement) {
            insertNamesAndValuesAssert.assertInsertNamesAndValues((InsertStatement) actual, expected.getInsertColumnsAndValues());
        }
    }
}

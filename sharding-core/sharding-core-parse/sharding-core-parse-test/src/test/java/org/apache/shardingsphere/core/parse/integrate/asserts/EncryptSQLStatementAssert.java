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

import org.apache.shardingsphere.core.parse.integrate.asserts.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.core.parse.integrate.jaxb.EncryptParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.encrypt.EncryptSQLCasesRegistry;

/**
 * SQL statement assert for encrypt.
 *
 * @author duhongjun
 */
public final class EncryptSQLStatementAssert {
    
    private final SQLStatement actual;
    
    private final ParserResult expected;
    
    private final TableAssert tableAssert;
    
    private final InsertNamesAndValuesAssert insertNamesAndValuesAssert;
    
    public EncryptSQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(
                EncryptSQLCasesRegistry.getInstance().getSqlCasesLoader(), EncryptParserResultSetRegistry.getInstance().getRegistry(), sqlCaseId, sqlCaseType);
        this.actual = actual;
        expected = EncryptParserResultSetRegistry.getInstance().getRegistry().get(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        insertNamesAndValuesAssert = new InsertNamesAndValuesAssert(assertMessage, sqlCaseType);
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof InsertStatement) {
            insertNamesAndValuesAssert.assertInsertNamesAndValues((InsertStatement) actual, expected.getInsertColumnsAndValues());
        }
    }
}

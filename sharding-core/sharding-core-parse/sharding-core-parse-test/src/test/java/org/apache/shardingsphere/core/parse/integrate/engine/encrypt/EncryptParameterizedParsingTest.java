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

package org.apache.shardingsphere.core.parse.integrate.engine.encrypt;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.integrate.asserts.EncryptSQLStatementAssert;
import org.apache.shardingsphere.core.parse.integrate.jaxb.EncryptParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.integrate.jaxb.ParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.encrypt.EncryptSQLCasesRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class EncryptParameterizedParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = EncryptSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private static ParserResultSetRegistry parserResultSetRegistry = EncryptParserResultSetRegistry.getInstance().getRegistry();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSQLTestParameters();
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSQL(sqlCaseId, sqlCaseType, parserResultSetRegistry.get(sqlCaseId).getParameters());
        SQLStatement sqlStatement = new SQLParseEngine(DatabaseTypes.getActualDatabaseType(databaseType)).parse(sql, false);
        new EncryptSQLStatementAssert(sqlStatement, sqlCaseId, sqlCaseType).assertSQLStatement();
    }
}

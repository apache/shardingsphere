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

package org.apache.shardingsphere.sql.parser.integrate.engine;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.ParserResultSetRegistry;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.ParserResultSetRegistryFactory;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.VisitorParserResultSetRegistry;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.visitor.VisitorSQLCasesRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class VisitorParameterizedParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = VisitorSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private static ParserResultSetRegistry parserResultSetRegistry = VisitorParserResultSetRegistry.getInstance().getRegistry();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Before
    public void setUp() {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
    }
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        assertThat(sqlCasesLoader.countAllSQLCases(), is(parserResultSetRegistry.countAllTestCases()));
        return sqlCasesLoader.getSQLTestParameters();
    }
    
    @Test
    public void assertSupportedSQL() {
        ParserResult expected = ParserResultSetRegistryFactory.getInstance().getRegistry().get(sqlCaseId);
        if (expected.isLongSQL()) {
            return;
        }
        String databaseTypeName = "H2".equals(databaseType) ? "MySQL" : databaseType;
        String sql = sqlCasesLoader.getSQL(sqlCaseId, sqlCaseType, parserResultSetRegistry.get(sqlCaseId).getParameters());
        ParseTree parseTree = SQLParserFactory.newInstance(databaseTypeName, sql).execute().getChild(0);
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlCaseId, sqlCaseType);
        SQLStatement actual = (SQLStatement) new SQLVisitorEngine(databaseTypeName, parseTree).parse();
        SQLStatementAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}

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
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.VisitorSQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.SQLParserTestCase;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Slf4j
public final class VisitorParameterizedParsingTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = VisitorSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private static final SQLParserTestCasesRegistry PARSER_RESULT_SET_REGISTRY = VisitorSQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private static final Properties PROPS = new Properties();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    static {
        try {
            PROPS.load(new FileInputStream(SQLParserParameterizedTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/runtime-config.properties"));
        } catch (final IOException ex) {
            log.warn("Can not find file `runtime-config.properties`, use default properties configuration.", ex);
        }
    }
    
    @Before
    public void setUp() {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
    }
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        assertThat(SQL_CASES_LOADER.countAllSQLCases(), is(PARSER_RESULT_SET_REGISTRY.countAllSQLParserTestCases()));
        return SQL_CASES_LOADER.getSQLTestParameters();
    }
    
    @Test
    public void assertSupportedSQL() {
        SQLParserTestCase expected = SQLParserTestCasesRegistryFactory.getInstance().getRegistry().get(sqlCaseId);
        if (expected.isLongSQL() && Boolean.valueOf(PROPS.getProperty("long.sql.skip", Boolean.TRUE.toString()))) {
            return;
        }
        String databaseTypeName = "H2".equals(databaseType) ? "MySQL" : databaseType;
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, PARSER_RESULT_SET_REGISTRY.get(sqlCaseId).getParameters());
        ParseTree parseTree = SQLParserFactory.newInstance(databaseTypeName, sql).execute().getChild(0);
        SQLCaseAssertContext assertContext = new SQLCaseAssertContext(sqlCaseId, sqlCaseType);
        SQLStatement actual = (SQLStatement) new SQLVisitorEngine(databaseTypeName, parseTree).parse();
        if (!expected.isLongSQL()) {
            SQLStatementAssert.assertIs(assertContext, actual, expected);
        }
    }
}

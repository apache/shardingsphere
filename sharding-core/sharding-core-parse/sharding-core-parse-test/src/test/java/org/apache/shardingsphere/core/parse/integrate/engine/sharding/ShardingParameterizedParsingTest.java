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

package org.apache.shardingsphere.core.parse.integrate.engine.sharding;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parse.entry.ShardingSQLParseEntry;
import org.apache.shardingsphere.core.parse.integrate.asserts.ShardingSQLStatementAssert;
import org.apache.shardingsphere.core.parse.integrate.engine.AbstractBaseParameterizedParsingTest;
import org.apache.shardingsphere.core.parse.integrate.jaxb.ParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.integrate.jaxb.ShardingParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiredArgsConstructor
public final class ShardingParameterizedParsingTest extends AbstractBaseParameterizedParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserResultSetRegistry parserResultSetLoader = ShardingParserResultSetRegistry.getInstance().getRegistry();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(parserResultSetLoader.countAllTestCases()));
        return sqlCasesLoader.getSupportedSQLTestParameters();
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResultSetLoader.get(sqlCaseId).getParameters());
        SQLStatement sqlStatement = new ShardingSQLParseEntry(
                DatabaseTypes.getTrunkDatabaseType(databaseType), getShardingRule(), getShardingTableMetaData(), new ParsingResultCache()).parse(sql, false);
        new ShardingSQLStatementAssert(sqlStatement, sqlCaseId, sqlCaseType).assertSQLStatement();
    }
}

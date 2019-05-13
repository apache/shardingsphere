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

package org.apache.shardingsphere.core.parse;

import lombok.RequiredArgsConstructor;
import org.junit.Test;

//@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class EncryptIntegrateParsingTest { //extends AbstractBaseIntegrateSQLParsingTest {
    
//    private static SQLCasesLoader sqlCasesLoader = AntlrSQLCasesLoader.getInstance();
//    
//    private static AntlrParserResultSetLoader parserResultSetLoader = AntlrParserResultSetLoader.getInstance();
//    
//    private final String sqlCaseId;
//    
//    private final DatabaseType databaseType;
//    
//    private final SQLCaseType sqlCaseType;
//    
//    @Parameterized.Parameters(name = "{0} ({2}) -> {1}")
//    public static Collection<Object[]> getTestParameters() {
//        sqlCasesLoader.switchSQLCase("encrypt_sql");
//        parserResultSetLoader.switchResult("encrypt");
//        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
//    }
//    
//    private static EncryptRule buildShardingRule() throws IOException {
//        URL url = AbstractBaseIntegrateSQLParsingTest.class.getClassLoader().getResource("yaml/encrypt-rule-for-parser.yaml");
//        Preconditions.checkNotNull(url, "Cannot found parser rule yaml configuration.");
//        YamlEncryptRuleConfiguration encryptConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlEncryptRuleConfiguration.class);
//        return new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(encryptConfig));
//    }
//    
//    @Test
//    @Ignore
//    public void parsingSupportedSQL() throws Exception {
//        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
//        SQLParser sqlParser = SQLParserFactory.newInstance(databaseType, sql);
//        Method addErrorListener = sqlParser.getClass().getMethod("addErrorListener", ANTLRErrorListener.class);
//        addErrorListener.invoke(sqlParser, new BaseErrorListener() {
//            
//            @Override
//            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException ex) {
//                throw new RuntimeException();
//            }
//        });
//        sqlParser.execute();
//    }
//    
//    @Test
//    @Ignore
//    public void assertSupportedSQL() throws Exception {
//        ParserResult parserResult = parserResultSetLoader.getParserResult(sqlCaseId);
//        if (null != parserResult) {
//            String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResult.getParameters());
//            DatabaseType execDatabaseType = databaseType;
//            if (DatabaseType.H2 == databaseType) {
//                execDatabaseType = DatabaseType.MySQL;
//            }
//            new EncryptSQLStatementAssert(new EncryptSQLParseEngine(execDatabaseType, buildShardingRule(), AbstractBaseIntegrateSQLParsingTest.getShardingTableMetaData()).parse(false, sql), 
//                    sqlCaseId, sqlCaseType, sqlCasesLoader, parserResultSetLoader, execDatabaseType).assertSQLStatement();
//        }
//    }
    
    @Test
    public void assertTemp() {
    }
}

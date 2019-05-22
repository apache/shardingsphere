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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.api.SQLParser;
import org.apache.shardingsphere.core.parse.integrate.asserts.EncryptParserResultSetLoader;
import org.apache.shardingsphere.core.parse.integrate.asserts.EncryptSQLStatementAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.ParserResultSetLoader;
import org.apache.shardingsphere.core.parse.integrate.engine.AbstractBaseIntegrateSQLParsingTest;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.core.parse.parser.SQLParserFactory;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.test.sql.EncryptSQLCasesLoader;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class EncryptIntegrateParsingTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = EncryptSQLCasesLoader.getInstance();
    
    private static ParserResultSetLoader parserResultSetLoader = EncryptParserResultSetLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    private static EncryptRule buildShardingRule() throws IOException {
        URL url = AbstractBaseIntegrateSQLParsingTest.class.getClassLoader().getResource("yaml/encrypt-rule-for-parser.yaml");
        Preconditions.checkNotNull(url, "Cannot found parser rule yaml configuration.");
        YamlEncryptRuleConfiguration encryptConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlEncryptRuleConfiguration.class);
        return new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(encryptConfig));
    }
    
    @Test
    public void parsingSupportedSQL() throws Exception {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
        SQLParser sqlParser = SQLParserFactory.newInstance(databaseType, sql);
        Method addErrorListener = sqlParser.getClass().getMethod("addErrorListener", ANTLRErrorListener.class);
        addErrorListener.invoke(sqlParser, new BaseErrorListener() {
            
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException ex) {
                throw new RuntimeException();
            }
        });
        sqlParser.execute();
    }
    
    @Test
    public void assertSupportedSQL() throws Exception {
        ParserResult parserResult = parserResultSetLoader.getParserResult(sqlCaseId);
        if (null != parserResult) {
            String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResult.getParameters());
            DatabaseType execDatabaseType = databaseType;
            if (DatabaseType.H2 == databaseType) {
                execDatabaseType = DatabaseType.MySQL;
            }
            new EncryptSQLStatementAssert(new EncryptSQLParseEngine(execDatabaseType, buildShardingRule(), AbstractBaseIntegrateSQLParsingTest.getShardingTableMetaData()).parse(sql, false), 
                    sqlCaseId, sqlCaseType, sqlCasesLoader, parserResultSetLoader, execDatabaseType).assertSQLStatement();
        }
    }
}

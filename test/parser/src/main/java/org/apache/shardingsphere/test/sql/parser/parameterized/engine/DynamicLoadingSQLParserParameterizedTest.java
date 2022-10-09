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

package org.apache.shardingsphere.test.sql.parser.parameterized.engine;

import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

@RunWith(Parameterized.class)
public final class DynamicLoadingSQLParserParameterizedTest extends SoftAssert {
    
    private static final String SQL_CASE_FILE_MARKER = "js-navigation-open Link--primary";
    
    private static final String SQL_CASE_MARKER = "blob-code blob-code-inner js-file-line";
    
    private static boolean inBracket;
    
    private static StringBuilder sqlCase;
    
    private static int sqlCaseId;
    
    private static String sqlCaseURL;
    
    private static String databaseType;

    public DynamicLoadingSQLParserParameterizedTest() {
        sqlCaseURL = System.getProperty("url");
        databaseType = System.getProperty("database");
    }
    
    private static String[] getSqlCaseFiles(final String sqlCaseURL) throws IOException {
        Document document = Jsoup.connect(sqlCaseURL).get();
        return document.getElementsByClass(SQL_CASE_FILE_MARKER).text().split("\\s+");
    }
    
    private static Collection<Object[]> getTestParameters(final String databaseType) throws IOException {
        Collection<Object[]> result = new LinkedList<>();
        String[] sqlCaseFiles = getSqlCaseFiles(sqlCaseURL);
        for (String each : sqlCaseFiles) {
            sqlCaseId = 1;
            inBracket = false;
            sqlCase = new StringBuilder();
            
            Document document = Jsoup.connect(sqlCaseURL + "/" + each).get();
            result.addAll(getSqlCases(document, each));
        }
        return result;
    }
    
    private static Collection<Object[]> getSqlCases(final Document document, final String sqlCaseFile) {
        Collection<Object[]> result = new LinkedList<>();
        int fileLength = document.getElementsByClass(SQL_CASE_MARKER).size();
        for (int line = 1; line <= fileLength; line++) {
            StringBuilder curSqlCase = new StringBuilder().append(Objects.requireNonNull(document.getElementById("LC" + line)).text());
            if (0 == curSqlCase.length() || '#' == curSqlCase.charAt(0) || '-' == curSqlCase.charAt(0)) {
                continue;
            }
            char lastChar = curSqlCase.charAt(curSqlCase.length() - 1);
            if (';' != lastChar && !inBracket) {
                result.add(getCurSqlCase(sqlCaseFile, curSqlCase));
                sqlCaseId++;
                sqlCase = new StringBuilder();
                continue;
            }
            if (0 != sqlCase.length()) {
                sqlCase.append(" ");
            }
            sqlCase.append(curSqlCase);
            if ('{' == lastChar) {
                inBracket = true;
            } else if ('}' == lastChar) {
                result.add(getSqlCase(databaseType, sqlCaseFile, sqlCase));
                sqlCase = new StringBuilder();
                sqlCaseId++;
                inBracket = false;
            }
        }
        return result;
    }
    
    private static Object[] getCurSqlCase(final String sqlCaseFile, final StringBuilder curSqlCase) {
        return sqlCase.length() > 0 ? getSqlCase(databaseType, sqlCaseFile, sqlCase) : getSqlCase(databaseType, sqlCaseFile, curSqlCase);
    }
    
    private static Object[] getSqlCase(final String databaseType, final String sqlCaseFile, final StringBuilder sqlCase) {
        Object[] parameters = new Object[3];
        parameters[0] = sqlCaseFile.split("\\.")[0] + sqlCaseId;
        parameters[1] = sqlCase.toString();
        parameters[2] = databaseType;
        return parameters;
    }
    
    @Test
    public void assertDynamicLoadingSQL() throws IOException {
        Collection<Object[]> testParameters = getTestParameters(databaseType);
        DynamicLoadingSQLParserParameterizedTest softAssert = new DynamicLoadingSQLParserParameterizedTest();
        CacheOption cacheOption = new CacheOption(128, 1024L);
        for (Object[] each : testParameters) {
            String sql = each[1].toString();
            try {
                ParseASTNode parseContext = new SQLParserEngine(databaseType, cacheOption).parse(sql, false);
                SQLStatement sqlStatement = new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseContext);
                softAssert.assertEquals(sqlStatement, parseContext, "<sql-case id=" + each[0] + " value=" + each[1] + " db-types=" + each[2] + " />");
            } catch (SQLParsingException | ClassCastException ignored) {
                softAssert.assertTrue(false, "<sql-case id=" + each[0] + " value=" + each[1] + " db-types=" + each[2] + " />");
            }
        }
    }

    @Override
    public void onAssertFailure(final IAssert<?> ignored, final AssertionError ex) {
        System.out.println(ex);
    }
}

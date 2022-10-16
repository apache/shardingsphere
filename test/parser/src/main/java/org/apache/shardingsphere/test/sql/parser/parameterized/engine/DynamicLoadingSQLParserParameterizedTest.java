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

import org.apache.commons.io.IOUtils;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Arrays;
import java.util.Properties;

@RunWith(Parameterized.class)
public class DynamicLoadingSQLParserParameterizedTest {
    
    private static String sqlCasesOwner;
    
    private static String sqlCasesRepo;
    
    private static String sqlCasesDirectory;
    
    private final String databaseType;
    
    public DynamicLoadingSQLParserParameterizedTest(final String database, final String url) {
        databaseType = database;
        String[] patches = url.split("/", 8);
        sqlCasesOwner = patches[3];
        sqlCasesRepo = patches[4];
        sqlCasesDirectory = patches[7];
    }
    
    @Parameterized.Parameters(name = "dynamic test with {0}")
    public static Collection<Object[]> getTestCases() {
        return Arrays.asList(new Object[][]{
                {"MySQL", "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t"},
                {"PostgreSQL", "https://github.com/postgres/postgres/tree/master/src/test/regress/sql"},
        });
    }
    
    private static Collection<Object[]> getTestParameters() throws IOException, URISyntaxException {
        Collection<Object[]> result = new ArrayList<>();
        LinkedList<Map<String, String>> response = new RestTemplate().getForObject(
                "https://api.github.com/repos/{owner}/{repo}/contents/{directory}", LinkedList.class,
                sqlCasesOwner, sqlCasesRepo, sqlCasesDirectory);
        for (Map<String, String> each : response) {
            result.addAll(getSqlCases(each));
        }
        return result;
    }
    
    private static Collection<Object[]> getSqlCases(final Map<String, String> elements) throws IOException, URISyntaxException {
        Collection<Object[]> result = new ArrayList<>();
        String sqlCaseFileName = elements.get("name");
        String sqlCaseFileURL = elements.get("download_url");
        String sqlCaseFileContent = IOUtils.toString(new URI(sqlCaseFileURL), Charset.defaultCharset());
        String[] lines = sqlCaseFileContent.split("\n");
        int sqlCaseId = 1;
        StringBuilder sqlCase = new StringBuilder();
        for (String each : lines) {
            if (each.contains("--") || each.contains("#")) {
                continue;
            }
            sqlCase.append(each);
            if (each.contains(";") || each.contains("}")) {
                result.add(new Object[]{
                        sqlCaseFileName + sqlCaseId, sqlCase.toString(),
                });
                sqlCaseId++;
                sqlCase = new StringBuilder();
            }
        }
        return result;
    }
    
    @Test
    public void assertDynamicLoadingSQL() throws IOException, URISyntaxException {
        Collection<Object[]> testParameters = getTestParameters();
        Collection<String> result = new LinkedList<>();
        CacheOption cacheOption = new CacheOption(128, 1024L);
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        for (Object[] each : testParameters) {
            String sql = each[1].toString();
            System.out.println(sql);
            ParseASTNode parseContext = new SQLParserEngine(databaseType, cacheOption).parse(sql, false);
            SQLStatement sqlStatement = new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseContext);
            if (!parseContext.toString().equals(sqlStatement.toString())) {
                result.add("<sql-case id=" + each[0] + " value=" + sql + " db-types=" + databaseType + " />");
            }
        }
        result.forEach(System.out::println);
        Assert.assertFalse(result.isEmpty());
    }
}

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

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;

@RequiredArgsConstructor
@SingletonSPI
public abstract class DynamicLoadingSQLParserParameterizedTest {
    
    private final String sqlCaseId;
    
    private final String sqlCaseValue;
    
    private final String databaseType;
    
    private static ArrayList<Map<String, String>> getResponse(final String sqlCaseURL) {
        String[] patches = sqlCaseURL.split("/", 8);
        String sqlCasesOwner = patches[3];
        String sqlCasesRepo = patches[4];
        String sqlCasesDirectory = patches[7];
        return new RestTemplate().getForObject(
                "https://api.github.com/repos/{owner}/{repo}/contents/{directory}", ArrayList.class,
                sqlCasesOwner, sqlCasesRepo, sqlCasesDirectory);
    }
    
    protected static Collection<Object[]> getTestParameters(final String sqlCaseURL) throws IOException, URISyntaxException {
        Collection<Object[]> result = new ArrayList<>();
        ArrayList<Map<String, String>> response = getResponse(sqlCaseURL);
        for (Map<String, String> each : response) {
            result.addAll(getSqlCases(each));
        }
        return result;
    }
    
    private static Collection<Object[]> getSqlCases(final Map<String, String> elements) throws IOException, URISyntaxException {
        Collection<Object[]> result = new ArrayList<>();
        String sqlCaseFileName = elements.get("name");
        String sqlCaseFileContent = IOUtils.toString(new URI(elements.get("download_url")), StandardCharsets.UTF_8);
        String[] lines = sqlCaseFileContent.split("\n");
        int sqlCaseEnum = 1;
        for (String each : lines) {
            if (each.isEmpty()) {
                continue;
            }
            if (Character.isLetter(each.charAt(0)) && each.charAt(each.length() - 1) == ';') {
                String sqlCaseId = sqlCaseFileName.split("\\.")[0] + sqlCaseEnum;
                result.add(new Object[]{
                        sqlCaseId, each,
                });
                sqlCaseEnum++;
            }
        }
        return result;
    }
    
    @Test(expected = Exception.class)
    public final void assertDynamicLoadingSQL() {
        CacheOption cacheOption = new CacheOption(128, 1024L);
        ParseASTNode parseContext = new SQLParserEngine(databaseType, cacheOption).parse(sqlCaseValue, false);
        new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseContext);
        System.out.println("ParserError: " + sqlCaseId + " value: " + sqlCaseValue + " db-type: " + databaseType);
        throw new RuntimeException();
    }
}

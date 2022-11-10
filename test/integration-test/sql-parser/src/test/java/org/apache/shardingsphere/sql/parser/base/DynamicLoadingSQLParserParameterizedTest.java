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

package org.apache.shardingsphere.sql.parser.base;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.result.SQLParserResultProcessor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Slf4j
public abstract class DynamicLoadingSQLParserParameterizedTest {
    
    private final String sqlCaseId;
    
    private final String sql;
    
    private final String databaseType;

    private final SQLParserResultProcessor resultGenerator;

    protected static Collection<Map<String, String>> getResponse(final String sqlCaseAPI, final URI sqlCaseURI) {
        Collection<Map<String, String>> result = new LinkedList<>();
        URI casesAPI = getAPI(sqlCaseAPI, sqlCaseURI);
        String caseContent = getContent(casesAPI);
        if (caseContent.isEmpty()) {
            return result;
        }
        List<String> casesName = JsonPath.parse(caseContent).read("$..name");
        List<String> casesDownloadURL = JsonPath.parse(caseContent).read("$..download_url");
        List<String> casesHtmlURL = JsonPath.parse(caseContent).read("$..html_url");
        List<String> casesType = JsonPath.parse(caseContent).read("$..type");
        IntStream.range(0, JsonPath.parse(caseContent).read("$.length()"))
                .forEach(each -> {
                    if ("file".equals(casesType.get(each))) {
                        result.add(ImmutableMap.of("name", casesName.get(each), "download_url", casesDownloadURL.get(each)));
                    } else if ("dir".equals(casesType.get(each))) {
                        result.addAll(getResponse(sqlCaseAPI, URI.create(casesHtmlURL.get(each))));
                    }
                });
        return result;
    }
    
    private static URI getAPI(final String sqlCaseAPI, final URI sqlCaseURI) {
        String[] patches = sqlCaseURI.toString().split("/", 8);
        String casesOwner = patches[3];
        String casesRepo = patches[4];
        String casesDirectory = patches[7];
        return URI.create(sqlCaseAPI + casesOwner + "/" + casesRepo + "/contents/" + casesDirectory);
    }
    
    protected static String getContent(final URI casesURI) {
        String result = "";
        try {
            InputStreamReader in = new InputStreamReader(casesURI.toURL().openStream());
            result = new BufferedReader(in).lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException ignore) {
            log.warn("Error: GitHub API rate limit exceeded");
        }
        return result;
    }
    
    protected static Collection<Object[]> getSQLCases(final String sqlCaseFileName, final String sqlCaseFileContent) {
        Collection<Object[]> result = new LinkedList<>();
        String[] lines = sqlCaseFileContent.split("\n");
        int sqlCaseEnum = 1;
        for (int i = 0; i < lines.length; i++) {
            if (isStatement(lines[i]) && (i + 1 == lines.length || !lines[i + 1].contains("ERROR"))) {
                String sqlCaseId = sqlCaseFileName + sqlCaseEnum;
                result.add(new Object[]{sqlCaseId, lines[i]});
                sqlCaseEnum++;
            }
        }
        return result;
    }
    
    private static boolean isStatement(final String statement) {
        return !statement.isEmpty() && Character.isLetter(statement.charAt(0)) && statement.charAt(statement.length() - 1) == ';';
    }
    
    @Test
    public final void assertParseSQL() {
        String result = "success";
        try {
            ParseASTNode parseASTNode = new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false);
            new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseASTNode);
        } catch (final ShardingSphereExternalException | ClassCastException | NullPointerException | IllegalArgumentException | IndexOutOfBoundsException ignore) {
            result = "failed";
            log.warn("ParserError: " + sqlCaseId + " value: " + sql + " db-type: " + databaseType);
        }
        resultGenerator.processResult(sqlCaseId, databaseType, result, sql);
    }
}

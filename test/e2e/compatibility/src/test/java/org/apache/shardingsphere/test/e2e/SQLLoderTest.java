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

package org.apache.shardingsphere.test.e2e;

import com.jayway.jsonpath.JsonPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SQLLoderTest {
    
    static final String CASE_URL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t";
    
    static final String RESULT_URL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/r";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/proxy_db?userSSL=false", "root", "root");
        System.out.println(loadSQLCaseFileSummaries(URI.create(CASE_URL)));
        Map<String, FileSummary> sqlCaseFileSummaries = loadSQLCaseFileSummaries(URI.create(CASE_URL)).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        Map<String, FileSummary> resultFileSummaries = loadSQLCaseFileSummaries(URI.create(RESULT_URL)).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        for (Entry<String, FileSummary> entry : sqlCaseFileSummaries.entrySet()) {
            String fileName = entry.getKey();
            if (!fileName.equals("alias")) {
                continue;
            }
            String sqlCaseFileContent = loadContent(URI.create(entry.getValue().getAccessURI()));
            String[] eachRowCase = sqlCaseFileContent.split("\n");
            List<String> statements = new LinkedList<>();
            StringBuilder completedSQL = new StringBuilder();
            for (String each : eachRowCase) {
                if (each.startsWith("--")) {
                    continue;
                }
                if (each.startsWith("#")) {
                    continue;
                }
                if (each.equals("\n")) {
                    continue;
                }
                completedSQL.append(each);
                if (completedSQL.indexOf(";") >= 0) {
                    statements.add(completedSQL.toString());
                    completedSQL = new StringBuilder();
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            try {
                for (String each : statements) {
                    stringBuilder.append(each).append("\n");
                    try (Statement statement = con.createStatement()) {
                        boolean result = false;
                        try {
                            result = statement.execute(each);
                        } catch (SQLException ex) {
                            //System.out.println(each);
                            // System.out.println(ex.getMessage());
                        }
                        if (!result) {
                            continue;
                        }
                        ResultSet resultSet = statement.getResultSet();
                        stringBuilder = buildCaseOutPut(stringBuilder, resultSet);
                    }
                }
            } catch (SQLException ex) {
                System.out.println(fileName);
                System.out.println(ex.getMessage());
            }
            System.out.println(stringBuilder.toString());
            // String resultFileContent = resultFileSummaries.containsKey(fileName) ? loadContent(URI.create(resultFileSummaries.get(fileName).getAccessURI())) : "";
            
        }
    }
    
    private Collection<Void> createTestParameters(final String sqlCaseFileContent, final String resultFileContent) {
        Collection<Void> result = new LinkedList<>();
        String[] rawCaseLines = sqlCaseFileContent.split("\n");
        String[] rawResultLines = resultFileContent.split("\n");
        String completedSQL = "";
        int sqlCaseEnum = 1;
        int statementLines = 0;
        int resultIndex = 0;
        boolean inProcedure = false;
        for (String each : rawCaseLines) {
        }
        return result;
    }
    
    public static Collection<FileSummary> loadSQLCaseFileSummaries(final URI uri) {
        String content = loadContent(getGitHubApiUri(uri));
        if (content.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<FileSummary> result = new LinkedList<>();
        List<String> fileNames = JsonPath.parse(content).read("$..name");
        List<String> folderTypes = JsonPath.parse(content).read("$..type");
        List<String> downloadURLs = JsonPath.parse(content).read("$..download_url");
        List<String> htmlURLs = JsonPath.parse(content).read("$..html_url");
        int length = JsonPath.parse(content).read("$.length()");
        for (int i = 0; i < length; i++) {
            String fileName = fileNames.get(i).split("\\.")[0];
            String folderType = folderTypes.get(i);
            String downloadURL = downloadURLs.get(i);
            String htmlURL = htmlURLs.get(i);
            if ("file".equals(folderType)) {
                result.add(new FileSummary(fileName, downloadURL));
            } else if ("dir".equals(folderType)) {
                result.addAll(loadSQLCaseFileSummaries(URI.create(htmlURL)));
            }
        }
        return result;
    }
    
    private static StringBuilder buildCaseOutPut(StringBuilder sb, ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            sb.append(resultSet.getMetaData().getColumnName(i));
            if (i < columnCount) {
                sb.append("\t");
            } else {
                sb.append("\n");
            }
        }
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                sb.append(resultSet.getObject(i));
                if (i < columnCount) {
                    sb.append("\t");
                } else {
                    sb.append("\n");
                }
            }
        }
        return sb;
    }
    
    private static URI getGitHubApiUri(final URI sqlCaseURI) {
        String[] patches = sqlCaseURI.toString().split("/", 8);
        String casesOwner = patches[3];
        String casesRepo = patches[4];
        String casesDirectory = patches[7];
        return URI.create(String.join("/", "https://api.github.com/repos", casesOwner, casesRepo, "contents", casesDirectory));
    }
    
    private static String loadContent(final URI casesURI) {
        try (
                InputStreamReader in = new InputStreamReader(casesURI.toURL().openStream());
                BufferedReader reader = new BufferedReader(in)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException ex) {
            return "";
        }
    }
}

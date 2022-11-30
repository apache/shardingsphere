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

package org.apache.shardingsphere.test.sql.parser.external.loader.strategy.impl;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.sql.parser.external.loader.strategy.SQLCaseLoadStrategy;
import org.apache.shardingsphere.test.sql.parser.external.loader.summary.FileSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL case loader with GitHub.
 */
@Slf4j
public final class GitHubSQLCaseLoadStrategy implements SQLCaseLoadStrategy {
    
    @Override
    public Collection<FileSummary> loadSQLCaseFileSummaries(final URI uri) {
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
    
    private URI getGitHubApiUri(final URI sqlCaseURI) {
        String[] patches = sqlCaseURI.toString().split("/", 8);
        String casesOwner = patches[3];
        String casesRepo = patches[4];
        String casesDirectory = patches[7];
        return URI.create(String.join("/", "https://api.github.com/repos", casesOwner, casesRepo, "contents", casesDirectory));
    }
    
    private String loadContent(final URI casesURI) {
        try (
                InputStreamReader in = new InputStreamReader(casesURI.toURL().openStream());
                BufferedReader reader = new BufferedReader(in)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException ex) {
            log.warn("Load failed, reason is: ", ex);
            return "";
        }
    }
}

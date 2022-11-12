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

package org.apache.shardingsphere.test.integration.sql.parser.loader.impl;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.sql.parser.loader.SQLCaseLoadStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL case loader with GitHub.
 */
@Slf4j
public final class GitHubSQLCaseLoadStrategy implements SQLCaseLoadStrategy {
    
    @Override
    public Collection<Map<String, String>> loadSQLCases(final URI uri) {
        String caseContent = loadContent(getGitHubApiUri(uri));
        if (caseContent.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<Map<String, String>> result = new LinkedList<>();
        List<String> casesName = JsonPath.parse(caseContent).read("$..name");
        List<String> casesDownloadURL = JsonPath.parse(caseContent).read("$..download_url");
        List<String> casesHtmlURL = JsonPath.parse(caseContent).read("$..html_url");
        List<String> casesType = JsonPath.parse(caseContent).read("$..type");
        int bound = JsonPath.parse(caseContent).read("$.length()");
        for (int each = 0; each < bound; each++) {
            if ("file".equals(casesType.get(each))) {
                result.add(ImmutableMap.of("name", casesName.get(each), "download_url", casesDownloadURL.get(each)));
            } else if ("dir".equals(casesType.get(each))) {
                result.addAll(loadSQLCases(URI.create(casesHtmlURL.get(each))));
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
        try {
            InputStreamReader in = new InputStreamReader(casesURI.toURL().openStream());
            return new BufferedReader(in).lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException ex) {
            log.warn("Load SQL cases failed, reason is: ", ex);
            return "";
        }
    }
    
    @Override
    public Map<String, String> loadSQLCaseResults(final URI uri) {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> each : loadSQLCases(uri)) {
            result.put(each.get("name").split("\\.")[0], each.get("download_url"));
        }
        return result;
    }
}

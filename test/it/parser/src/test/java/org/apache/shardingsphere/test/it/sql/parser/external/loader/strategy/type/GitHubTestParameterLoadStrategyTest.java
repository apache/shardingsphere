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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.strategy.type;

import org.apache.shardingsphere.test.it.sql.parser.external.loader.summary.FileSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class GitHubTestParameterLoadStrategyTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sources")
    void assertLoadSQLCaseFileSummaries(final String name, final String sourceType, final String ref) throws IOException {
        String casePath = "blob".equals(sourceType) ? "cases/sample.sql" : "cases";
        URI sourceURI = URI.create("https://github.com/owner/repo/" + sourceType + "/" + ref + "/" + casePath);
        String apiURL = "https://api.github.com/repos/owner/repo/contents/" + casePath + "?ref=" + ref;
        String downloadURL = "https://raw.githubusercontent.com/owner/repo/" + ref + "/cases/sample.sql";
        String fileContent = "{\"name\":\"sample.sql\",\"type\":\"file\",\"download_url\":\"" + downloadURL + "\"}";
        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            mockContent(uriMock, apiURL, "blob".equals(sourceType) ? fileContent : "[" + fileContent + "]");
            Collection<FileSummary> actual = new GitHubTestParameterLoadStrategy().loadSQLCaseFileSummaries(sourceURI);
            assertThat(actual.size(), is(1));
            FileSummary actualFileSummary = actual.iterator().next();
            assertThat(actualFileSummary.getFileName(), is("sample.sql"));
            assertThat(actualFileSummary.getAccessURI(), is(downloadURL));
            uriMock.verify(() -> URI.create(apiURL));
        }
    }
    
    private void mockContent(final MockedStatic<URI> uriMock, final String apiURL, final String content) throws IOException {
        URI apiURI = mock(URI.class, RETURNS_DEEP_STUBS);
        uriMock.when(() -> URI.create(apiURL)).thenReturn(apiURI);
        when(apiURI.toURL().openConnection().getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }
    
    private static Stream<Arguments> sources() {
        return Stream.of(
                Arguments.of("branch directory", "tree", "8.0"),
                Arguments.of("commit directory", "tree", "f58ab18a90460cd297249bc80b13d84b2671bf0c"),
                Arguments.of("branch file", "blob", "8.0"));
    }
    
    @Test
    void assertLoadSQLCaseFileSummariesRecursively() throws IOException {
        String ref = "f58ab18a90460cd297249bc80b13d84b2671bf0c";
        URI sourceURI = URI.create("https://github.com/owner/repo/tree/" + ref + "/cases");
        String childURL = "https://github.com/owner/repo/tree/" + ref + "/cases/nested";
        URI childURI = URI.create(childURL);
        String apiURL = "https://api.github.com/repos/owner/repo/contents/cases?ref=" + ref;
        String childApiURL = "https://api.github.com/repos/owner/repo/contents/cases/nested?ref=" + ref;
        String downloadURL = "https://raw.githubusercontent.com/owner/repo/" + ref + "/cases/sample.sql";
        String childDownloadURL = "https://raw.githubusercontent.com/owner/repo/" + ref + "/cases/nested/child.sql";
        String content = "[{\"name\":\"sample.sql\",\"type\":\"file\",\"download_url\":\"" + downloadURL + "\"},{\"name\":\"nested\",\"type\":\"dir\",\"html_url\":\"" + childURL + "\"}]";
        String childContent = "[{\"name\":\"child.sql\",\"type\":\"file\",\"download_url\":\"" + childDownloadURL + "\"}]";
        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            mockContent(uriMock, apiURL, content);
            mockContent(uriMock, childApiURL, childContent);
            uriMock.when(() -> URI.create(childURL)).thenReturn(childURI);
            Collection<FileSummary> actual = new GitHubTestParameterLoadStrategy().loadSQLCaseFileSummaries(sourceURI);
            assertThat(actual.stream().map(FileSummary::getFileName).collect(Collectors.toList()), is(Arrays.asList("sample.sql", "child.sql")));
            assertThat(actual.stream().map(FileSummary::getAccessURI).collect(Collectors.toList()), is(Arrays.asList(downloadURL, childDownloadURL)));
            uriMock.verify(() -> URI.create(apiURL));
            uriMock.verify(() -> URI.create(childApiURL));
        }
    }
    
    @Test
    void assertLoadSQLCaseFileSummariesWithEmptyURI() {
        URI sourceURI = URI.create("");
        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            assertTrue(new GitHubTestParameterLoadStrategy().loadSQLCaseFileSummaries(sourceURI).isEmpty());
            uriMock.verifyNoInteractions();
        }
    }
}

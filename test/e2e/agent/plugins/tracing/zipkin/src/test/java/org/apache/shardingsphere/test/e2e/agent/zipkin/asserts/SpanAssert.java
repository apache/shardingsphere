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

package org.apache.shardingsphere.test.e2e.agent.zipkin.asserts;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.SpanTestCase;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.TagAssertion;
import org.apache.shardingsphere.test.e2e.agent.zipkin.result.SpanResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Span assert.
 */
public final class SpanAssert {
    
    /**
     * Assert span is correct with expected result.
     *
     * @param baseUrl zipkin url
     * @param expected expected span
     */
    public static void assertIs(final String baseUrl, final SpanTestCase expected) {
        assertTagKey(baseUrl, expected);
        expected.getTagCases().stream().filter(TagAssertion::isNeedAssertValue).forEach(each -> assertTagValue(baseUrl, expected, each));
    }
    
    private static void assertTagKey(final String baseUrl, final SpanTestCase expected) {
        String baseTraceApiUrl = String.format("%s/api/v2/traces?serviceName=%s&spanName=%s&limit=%s", baseUrl, getEncodeValue(expected.getServiceName()),
                getEncodeValue(expected.getSpanName()), 1000);
        Collection<SpanResult> spanResults = getSpanResults(expected, baseTraceApiUrl);
        Collection<String> actualTags = spanResults.stream().flatMap(each -> each.getTags().keySet().stream()).collect(Collectors.toSet());
        Collection<String> expectedTags = expected.getTagCases().stream().map(TagAssertion::getTagKey).collect(Collectors.toSet());
        Collection<String> nonExistentTags = expectedTags.stream().filter(each -> !actualTags.contains(each)).collect(Collectors.toSet());
        assertTrue(nonExistentTags.isEmpty(), String.format("The tags `%s` does not exist in `%s` span", nonExistentTags, expected.getSpanName()));
    }
    
    private static void assertTagValue(final String baseUrl, final SpanTestCase expected, final TagAssertion expectedTagCase) {
        String baseTraceApiUrl = String.format("%s/api/v2/traces?serviceName=%s&spanName=%s&annotationQuery=%s&limit=%s", baseUrl, getEncodeValue(expected.getServiceName()),
                getEncodeValue(expected.getSpanName()), getEncodeValue(String.format("%s=%s", expectedTagCase.getTagKey(), expectedTagCase.getTagValue())), 1000);
        Collection<SpanResult> spanResults = getSpanResults(expected, baseTraceApiUrl);
        assertFalse(spanResults.isEmpty(), String.format("The tag `%s`=`%s` does not exist in `%s` span", expectedTagCase.getTagKey(), expectedTagCase.getTagValue(), expected.getSpanName()));
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String getEncodeValue(final String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<SpanResult> getSpanResults(final SpanTestCase expected, final String url) {
        List<List<SpanResult>> result = JsonUtils.fromJsonString(OkHttpUtils.getInstance().get(url), new TypeReference<List<List<SpanResult>>>() {
        });
        assertNotNull(result);
        return result.stream().findFirst().orElse(Collections.emptyList()).stream()
                .filter(each -> expected.getSpanName().equalsIgnoreCase(each.getName())).collect(Collectors.toList());
    }
}

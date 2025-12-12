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
import org.apache.shardingsphere.test.e2e.agent.engine.util.AgentE2EHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.zipkin.asserts.response.ZipkinSpanResponse;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.ZipkinE2ETestCase;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.ZipkinTagAssertion;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Zipkin span assert.
 */
public final class ZipkinSpanAssert {
    
    /**
     * Assert zipkin span.
     *
     * @param zipkinUrl zipkin query URL
     * @param expected expected test case
     */
    public static void assertIs(final String zipkinUrl, final ZipkinE2ETestCase expected) {
        assertTagKey(zipkinUrl, expected);
        expected.getTags().stream().filter(each -> null != each.getTagValue()).forEach(each -> assertTagValue(zipkinUrl, expected, each));
    }
    
    private static void assertTagKey(final String zipkinUrl, final ZipkinE2ETestCase expected) {
        String baseTraceApiUrl = String.format("%s/api/v2/traces?serviceName=%s&spanName=%s&limit=%s", zipkinUrl, encode(expected.getServiceName()), encode(expected.getSpanName()), 1000);
        Collection<ZipkinSpanResponse> spanResponses = queryTraceResponses(expected, baseTraceApiUrl);
        Collection<String> actualTags = spanResponses.stream().flatMap(each -> each.getTags().keySet().stream()).collect(Collectors.toSet());
        Collection<String> expectedTags = expected.getTags().stream().map(ZipkinTagAssertion::getTagKey).collect(Collectors.toSet());
        Collection<String> notExistedTags = expectedTags.stream().filter(each -> !actualTags.contains(each)).collect(Collectors.toSet());
        assertTrue(notExistedTags.isEmpty(), String.format("The tags `%s` does not exist in `%s` span", notExistedTags, expected.getSpanName()));
    }
    
    private static void assertTagValue(final String zipkinUrl, final ZipkinE2ETestCase expected, final ZipkinTagAssertion expectedTagCase) {
        String queryURL = String.format("%s/api/v2/traces?serviceName=%s&spanName=%s&annotationQuery=%s&limit=%s", zipkinUrl, encode(expected.getServiceName()),
                encode(expected.getSpanName()), encode(String.format("%s=%s", expectedTagCase.getTagKey(), expectedTagCase.getTagValue())), 1000);
        assertFalse(queryTraceResponses(expected, queryURL).isEmpty(),
                String.format("The tag `%s`=`%s` does not exist in `%s` span", expectedTagCase.getTagKey(), expectedTagCase.getTagValue(), expected.getSpanName()));
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encode(final String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<ZipkinSpanResponse> queryTraceResponses(final ZipkinE2ETestCase expected, final String url) {
        List<List<ZipkinSpanResponse>> result = JsonUtils.fromJsonString(AgentE2EHttpUtils.query(url), new TypeReference<List<List<ZipkinSpanResponse>>>() {
        });
        return result.stream().findFirst().orElse(Collections.emptyList()).stream().filter(each -> expected.getSpanName().equalsIgnoreCase(each.getName())).collect(Collectors.toList());
    }
}

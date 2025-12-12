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

package org.apache.shardingsphere.test.e2e.agent.jaeger.asserts;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.agent.engine.util.AgentE2EHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.jaeger.asserts.response.JaegerSpanResponse;
import org.apache.shardingsphere.test.e2e.agent.jaeger.asserts.response.JaegerSpanResponse.Tag;
import org.apache.shardingsphere.test.e2e.agent.jaeger.asserts.response.JaegerTraceResponse;
import org.apache.shardingsphere.test.e2e.agent.jaeger.asserts.response.JaegerTraceResponseData;
import org.apache.shardingsphere.test.e2e.agent.jaeger.cases.JaegerE2ETestCase;
import org.apache.shardingsphere.test.e2e.agent.jaeger.cases.JaegerTagAssertion;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Jaeger Span assert.
 */
public final class JaegerSpanAssert {
    
    /**
     * Assert jaeger span.
     *
     * @param jaegerUrl jaeger query URL
     * @param expected expected test case
     */
    public static void assertIs(final String jaegerUrl, final JaegerE2ETestCase expected) {
        assertTagKey(jaegerUrl, expected);
        expected.getTags().stream().filter(each -> null != each.getTagValue()).forEach(each -> assertTagValue(jaegerUrl, expected, each));
    }
    
    private static void assertTagKey(final String jaegerUrl, final JaegerE2ETestCase expected) {
        String queryURL = String.format("%s/api/traces?service=%s&operation=%s&limit=%s", jaegerUrl, encode(expected.getServiceName()), encode(expected.getSpanName()), 1000);
        Optional<JaegerSpanResponse> spanResponses = queryTraceResponses(queryURL).stream().flatMap(each -> each.getSpans().stream())
                .filter(each -> expected.getSpanName().equalsIgnoreCase(each.getOperationName())).findFirst();
        assertTrue(spanResponses.isPresent());
        Collection<String> actualTags = spanResponses.get().getTags().stream().map(Tag::getKey).collect(Collectors.toSet());
        Collection<String> expectedTags = expected.getTags().stream().map(JaegerTagAssertion::getTagKey).collect(Collectors.toSet());
        Collection<String> notExistedTags = expectedTags.stream().filter(each -> !actualTags.contains(each)).collect(Collectors.toSet());
        assertTrue(notExistedTags.isEmpty(), String.format("The tags `%s` does not exist in `%s` span", notExistedTags, expected.getSpanName()));
    }
    
    private static void assertTagValue(final String jaegerUrl, final JaegerE2ETestCase expected, final JaegerTagAssertion expectedTagCase) {
        String queryURL = String.format("%s/api/traces?service=%s&operation=%s&tags=%s&limit=%s", jaegerUrl, encode(expected.getServiceName()),
                encode(expected.getSpanName()), encode(JsonUtils.toJsonString(ImmutableMap.of(expectedTagCase.getTagKey(), expectedTagCase.getTagValue()))), 1000);
        assertFalse(queryTraceResponses(queryURL).isEmpty(),
                String.format("The tag `%s`=`%s` does not exist in `%s` span", expectedTagCase.getTagKey(), expectedTagCase.getTagValue(), expected.getSpanName()));
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encode(final String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<JaegerTraceResponseData> queryTraceResponses(final String queryURL) {
        return JsonUtils.fromJsonString(AgentE2EHttpUtils.query(queryURL), JaegerTraceResponse.class).getData();
    }
}

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.SpanTestCase;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.TagAssertion;
import org.apache.shardingsphere.test.e2e.agent.zipkin.result.SpanResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
        for (TagAssertion each : expected.getTagCases()) {
            assertSpan(baseUrl, expected, each);
        }
    }
    
    private static void assertSpan(final String baseUrl, final SpanTestCase expected, final TagAssertion expectedTagCase) {
        String tracesApiUrl = getTraceApiUrl(baseUrl, expected, expectedTagCase);
        List<List<SpanResult>> spanResults = null;
        try {
            spanResults = new Gson().fromJson(OkHttpUtils.getInstance().get(tracesApiUrl), new TypeToken<List<List<SpanResult>>>() {
            }.getType());
        } catch (final IOException ignored) {
        }
        assertNotNull(spanResults);
        List<SpanResult> spanList = spanResults.stream().findFirst().orElse(Collections.emptyList()).stream()
                .filter(each -> expected.getSpanName().equalsIgnoreCase(each.getName())).collect(Collectors.toList());
        assertFalse(expectedTagCase.isNeedAssertValue()
                ? String.format("The tag `%s`=`%s` does not exist in `%s` span", expectedTagCase.getTagKey(), expectedTagCase.getTagValue(), expected.getSpanName())
                : String.format("The tag `%s` does not exist in `%s` span", expectedTagCase.getTagKey(), expected.getSpanName()), spanList.isEmpty());
    }
    
    private static String getTraceApiUrl(final String baseUrl, final SpanTestCase expected, final TagAssertion expectedTagCase) {
        String baseTraceApiUrl = String.format("%s/api/v2/traces?serviceName=%s&spanName=%s", baseUrl, getEncodeValue(expected.getServiceName()), getEncodeValue(expected.getSpanName()));
        return expectedTagCase.isNeedAssertValue()
                ? String.format("%s&annotationQuery=%s", baseTraceApiUrl, getEncodeValue(String.format("%s=%s", expectedTagCase.getTagKey(), expectedTagCase.getTagValue())))
                : String.format("%s&annotationQuery=%s", baseTraceApiUrl, getEncodeValue(expectedTagCase.getTagKey()));
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String getEncodeValue(final String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
}

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

package org.apache.shardingsphere.mcp.support.resource;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPUriPathSegmentUtilsTest {
    
    @Test
    void assertEncodePathSegment() {
        assertThat(MCPUriPathSegmentUtils.encodePathSegment("订单 / archive?"), is("%E8%AE%A2%E5%8D%95%20%2F%20archive%3F"));
    }
    
    @Test
    void assertDecodePathSegment() {
        Optional<String> actualPathSegment = MCPUriPathSegmentUtils.decodePathSegment("%E8%AE%A2%E5%8D%95%20%2F%20archive%3F");
        assertThat(actualPathSegment, is(Optional.of("订单 / archive?")));
    }
    
    @Test
    void assertDecodePathSegmentKeepsPlusLiteral() {
        Optional<String> actualPathSegment = MCPUriPathSegmentUtils.decodePathSegment("logic+db");
        assertThat(actualPathSegment, is(Optional.of("logic+db")));
    }
    
    @Test
    void assertDecodePathSegmentWithMalformedPercentEncoding() {
        Optional<String> actualPathSegment = MCPUriPathSegmentUtils.decodePathSegment("logic%ZZdb");
        assertThat(actualPathSegment, is(Optional.empty()));
    }
}

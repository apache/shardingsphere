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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProcessOutputDiagnosticsTest {
    
    @Test
    void assertFormatEmptyOutput() {
        assertThat(ProcessOutputDiagnostics.format(List.of()), is("<empty>"));
    }
    
    @Test
    void assertFormatOutput() {
        assertThat(ProcessOutputDiagnostics.format(List.of("first", "second")), is("first" + System.lineSeparator() + "second"));
    }
    
    @Test
    void assertFormatTruncatedOutput() {
        String actual = ProcessOutputDiagnostics.format(List.of("x".repeat(ProcessOutputDiagnostics.MAX_OUTPUT_CHARS + 1)));
        assertThat(actual.length(), is(ProcessOutputDiagnostics.MAX_OUTPUT_CHARS + "...<truncated>".length()));
        assertThat(actual.substring(ProcessOutputDiagnostics.MAX_OUTPUT_CHARS), is("...<truncated>"));
    }
}

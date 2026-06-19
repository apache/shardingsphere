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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProcessOutputDiagnostics {
    
    static final int MAX_OUTPUT_CHARS = 4096;
    
    static String format(final List<String> outputMessages) {
        if (outputMessages.isEmpty()) {
            return "<empty>";
        }
        String result = String.join(System.lineSeparator(), outputMessages);
        return result.length() <= MAX_OUTPUT_CHARS ? result : result.substring(0, MAX_OUTPUT_CHARS) + "...<truncated>";
    }
}

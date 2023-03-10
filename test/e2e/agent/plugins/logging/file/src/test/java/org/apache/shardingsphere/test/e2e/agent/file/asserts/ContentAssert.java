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

package org.apache.shardingsphere.test.e2e.agent.file.asserts;

import org.apache.shardingsphere.test.e2e.agent.file.loader.LogLoader;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Content assert.
 */
public final class ContentAssert {
    
    /**
     * Assertion specifies regular log content.
     *
     * @param expectedLogRegex expected log regex
     */
    public static void assertIs(final String expectedLogRegex) {
        Collection<String> actualLogLines = LogLoader.getLogLines();
        assertThat("Actual log is empty", actualLogLines.size(), greaterThan(0));
        Pattern pattern = Pattern.compile(expectedLogRegex);
        Collection<String> expectedLogs = actualLogLines.stream().filter(each -> pattern.matcher(each).find()).collect(Collectors.toList());
        assertThat(String.format("The log for the specified regular `%s` does not exist", expectedLogRegex), expectedLogs.size(), greaterThan(0));
    }
}

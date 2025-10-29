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

package org.apache.shardingsphere.test.infra.framework.extension.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Log capture assertion.
 */
@RequiredArgsConstructor
public final class LogCaptureAssertion {
    
    private final List<ILoggingEvent> loggingEvents;
    
    /**
     * Assert the number of captured logs.
     *
     * @param expectedCount expected log count
     */
    public void assertLogCount(final int expectedCount) {
        assertThat(loggingEvents.size(), is(expectedCount));
    }
    
    /**
     * Assert the number of captured logs with specific level and message.
     *
     * @param actualLogEventIndex actual log event index
     * @param expectedLevel expected log level
     * @param expectedMessage expected log message
     * @param isFormatMessage format message or not
     */
    public void assertLogContent(final int actualLogEventIndex, final Level expectedLevel, final String expectedMessage, final boolean isFormatMessage) {
        assertThat(loggingEvents.get(actualLogEventIndex).getLevel(), is(expectedLevel));
        if (isFormatMessage) {
            assertThat(loggingEvents.get(actualLogEventIndex).getFormattedMessage(), is(expectedMessage));
        } else {
            assertThat(loggingEvents.get(actualLogEventIndex).getMessage(), is(expectedMessage));
        }
    }
}

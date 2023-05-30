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

package org.apache.shardingsphere.agent.plugin.core.recorder;

import org.apache.shardingsphere.agent.api.advice.AgentAdvice;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

class MethodTimeRecorderTest {
    
    @Test
    void assertGetElapsedTimeAndCleanWithRecorded() throws NoSuchMethodException {
        MethodTimeRecorder methodTimeRecorder = new MethodTimeRecorder(AgentAdvice.class);
        methodTimeRecorder.recordNow(Object.class.getDeclaredMethod("toString"));
        Awaitility.await().pollDelay(5L, TimeUnit.MILLISECONDS).until(() -> true);
        assertThat(methodTimeRecorder.getElapsedTimeAndClean(Object.class.getDeclaredMethod("toString")), greaterThanOrEqualTo(5L));
    }
    
    @Test
    void assertGetElapsedTimeAndCleanWithoutRecorded() throws NoSuchMethodException {
        assertThat(new MethodTimeRecorder(AgentAdvice.class).getElapsedTimeAndClean(Object.class.getDeclaredMethod("toString")), is(0L));
    }
}

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

package org.apache.shardingsphere.data.pipeline.core.task;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(LogCaptureExtension.class)
@LogCaptureSettings(suppressOutput = true)
class TaskExecuteCallbackTest {
    
    @Test
    void assertOnFailure(final LogCaptureAssertion logCaptureAssertion) {
        PipelineTask task = mock(PipelineTask.class);
        RuntimeException expectedException = new RuntimeException("");
        new TaskExecuteCallback(task).onFailure(expectedException);
        verify(task).stop();
        logCaptureAssertion.assertErrorLog(actualException -> assertThat(((ThrowableProxy) actualException).getThrowable(), sameInstance(expectedException)));
    }
}

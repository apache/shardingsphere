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

package org.apache.shardingsphere.agent.plugin.logging.file.advice;

import ch.qos.logback.classic.Level;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.mock;

@ExtendWith(LogCaptureExtension.class)
class MetaDataContextsFactoryAdviceTest {
    
    @Test
    void assertLog(final LogCaptureAssertion logCaptureAssertion) {
        MetaDataContextsFactoryAdvice advice = new MetaDataContextsFactoryAdvice();
        TargetAdviceMethod method = mock(TargetAdviceMethod.class);
        advice.beforeMethod(null, method, new Object[]{}, "FIXTURE");
        advice.afterMethod(null, method, new Object[]{}, null, "FIXTURE");
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.INFO, "Build meta data contexts finished, cost {} milliseconds.", false);
    }
}

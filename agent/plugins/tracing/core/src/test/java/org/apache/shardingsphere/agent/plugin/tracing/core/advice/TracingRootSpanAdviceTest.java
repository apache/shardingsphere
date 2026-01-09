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

package org.apache.shardingsphere.agent.plugin.tracing.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TracingRootSpanAdviceTest {
    
    @AfterEach
    void reset() {
        RootSpanContext.set(null);
    }
    
    @Test
    void assertCreateRootSpanAndContextSet() {
        RecordingTracingRootSpanAdvice advice = new RecordingTracingRootSpanAdvice();
        TargetAdviceObject target = new SimpleTargetAdviceObject();
        TargetAdviceMethod method = new TargetAdviceMethod("mock");
        advice.beforeMethod(target, method, new Object[]{"arg"}, "FIXTURE");
        assertThat(RootSpanContext.get(), is("root-mock"));
    }
    
    @Test
    void assertFinishRootSpan() {
        RecordingTracingRootSpanAdvice advice = new RecordingTracingRootSpanAdvice();
        TargetAdviceObject target = new SimpleTargetAdviceObject();
        TargetAdviceMethod method = new TargetAdviceMethod("mock");
        advice.beforeMethod(target, method, new Object[]{"arg"}, "FIXTURE");
        advice.afterMethod(target, method, new Object[]{"arg"}, null, "FIXTURE");
        assertThat(advice.getFinished(), is("root-mock"));
    }
    
    @Test
    void assertRecordException() {
        RecordingTracingRootSpanAdvice advice = new RecordingTracingRootSpanAdvice();
        TargetAdviceObject target = new SimpleTargetAdviceObject();
        TargetAdviceMethod method = new TargetAdviceMethod("mock");
        advice.beforeMethod(target, method, new Object[]{"arg"}, "FIXTURE");
        advice.onThrowing(target, method, new Object[]{"arg"}, new IllegalStateException("error"), "FIXTURE");
        assertThat(advice.getExceptionRecorded(), is("root-mock"));
    }
    
    private static final class RecordingTracingRootSpanAdvice extends TracingRootSpanAdvice<String> {
        
        private String finished;
        
        private String exceptionRecorded;
        
        @Override
        protected String createRootSpan(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args) {
            return "root-" + method.getName();
        }
        
        @Override
        protected void finishRootSpan(final String rootSpan, final TargetAdviceObject target) {
            finished = rootSpan;
        }
        
        @Override
        protected void recordException(final String rootSpan, final TargetAdviceObject target, final Throwable throwable) {
            exceptionRecorded = rootSpan;
        }
        
        String getFinished() {
            return finished;
        }
        
        String getExceptionRecorded() {
            return exceptionRecorded;
        }
    }
    
    private static final class SimpleTargetAdviceObject implements TargetAdviceObject {
        
        private Object attachment;
        
        @Override
        public Object getAttachment() {
            return attachment;
        }
        
        @Override
        public void setAttachment(final Object attachment) {
            this.attachment = attachment;
        }
    }
}

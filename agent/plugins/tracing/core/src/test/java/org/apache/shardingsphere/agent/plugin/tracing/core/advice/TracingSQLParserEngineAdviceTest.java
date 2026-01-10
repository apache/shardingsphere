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

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TracingSQLParserEngineAdviceTest {
    
    @BeforeEach
    void setUp() {
        RootSpanContext.set("root-span");
    }
    
    @AfterEach
    void reset() {
        RootSpanContext.set(null);
    }
    
    @Test
    void assertBeforeMethod() {
        RecordingTracingSQLParserEngineAdvice advice = new RecordingTracingSQLParserEngineAdvice();
        TargetAdviceObject expectedTarget = new SimpleTargetAdviceObject();
        advice.beforeMethod(expectedTarget, new TargetAdviceMethod("parse"), new Object[]{"SELECT 1"}, "FIXTURE");
        assertThat(advice.getRecordedParentSpan(), is("root-span"));
        assertThat(advice.getRecordedTarget(), is(expectedTarget));
        assertThat(advice.getRecordedSql(), is("SELECT 1"));
    }
    
    private static final class RecordingTracingSQLParserEngineAdvice extends TracingSQLParserEngineAdvice<String> {
        
        private String recordedParentSpan;
        
        private TargetAdviceObject recordedTarget;
        
        private String recordedSql;
        
        @Override
        protected Object recordSQLParseInfo(final String parentSpan, final TargetAdviceObject target, final String sql) {
            recordedParentSpan = parentSpan;
            recordedTarget = target;
            recordedSql = sql;
            return null;
        }
        
        String getRecordedParentSpan() {
            return recordedParentSpan;
        }
        
        TargetAdviceObject getRecordedTarget() {
            return recordedTarget;
        }
        
        String getRecordedSql() {
            return recordedSql;
        }
    }
    
    @Getter
    @Setter
    private static final class SimpleTargetAdviceObject implements TargetAdviceObject {
        
        private Object attachment;
    }
}

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

package org.apache.shardingsphere.test.e2e.agent.jaeger.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.json.JsonConfiguration;

import java.util.List;

/**
 * Jaeger trace result.
 */
@Getter
@Setter
public final class JaegerTraceResult implements JsonConfiguration {
    
    private List<JaegerTraceResultData> data;
    
    private int total;
    
    private int limit;
    
    private int offset;
    
    private String errors;
    
    @Getter
    @Setter
    public static final class JaegerTraceResultData {
        
        @JsonProperty("traceID")
        private String traceId;
        
        private List<Object> spans;
        
        private Object processes;
        
        private String warnings;
    }
    
    @Getter
    @Setter
    public static final class Span {
        
        @JsonProperty("traceID")
        private String traceId;
        
        @JsonProperty("spanID")
        private String spanId;
        
        private int flags;
        
        private String operationName;
        
        private List<String> references;
        
        private long startTime;
        
        private int duration;
        
        private List<Object> tags;
        
        private List<String> logs;
        
        @JsonProperty("processID")
        private String processId;
        
        private String warnings;
    }
}

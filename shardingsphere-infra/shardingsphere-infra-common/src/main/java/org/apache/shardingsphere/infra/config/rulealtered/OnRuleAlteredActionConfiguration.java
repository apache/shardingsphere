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

package org.apache.shardingsphere.infra.config.rulealtered;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

/**
 * On rule altered action configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class OnRuleAlteredActionConfiguration {
    
    private final InputConfiguration input;
    
    private final OutputConfiguration output;
    
    private final ShardingSphereAlgorithmConfiguration streamChannel;
    
    private final ShardingSphereAlgorithmConfiguration completionDetector;
    
    private final ShardingSphereAlgorithmConfiguration dataConsistencyChecker;
    
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static final class InputConfiguration {
        
        private final Integer workerThread;
        
        private final Integer batchSize;
        
        private final ShardingSphereAlgorithmConfiguration rateLimiter;
    }
    
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static final class OutputConfiguration {
        
        private final Integer workerThread;
        
        private final Integer batchSize;
        
        private final ShardingSphereAlgorithmConfiguration rateLimiter;
    }
}

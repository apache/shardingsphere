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

package org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;

/**
 * YAML on rule altered action configuration.
 */
@Getter
@Setter
@ToString
public final class YamlOnRuleAlteredActionConfiguration implements YamlConfiguration {
    
    private YamlInputConfiguration input;
    
    private YamlOutputConfiguration output;
    
    private YamlShardingSphereAlgorithmConfiguration streamChannel;
    
    private YamlShardingSphereAlgorithmConfiguration completionDetector;
    
    private YamlShardingSphereAlgorithmConfiguration dataConsistencyChecker;
    
    @Data
    public static final class YamlInputConfiguration implements YamlConfiguration {
        
        private static final Integer DEFAULT_WORKER_THREAD = 40;
        
        private static final Integer DEFAULT_BATCH_SIZE = 1000;
        
        private static final Integer DEFAULT_SHARDING_SIZE = 1000_0000;
        
        private Integer workerThread = DEFAULT_WORKER_THREAD;
        
        private Integer batchSize = DEFAULT_BATCH_SIZE;
        
        private Integer shardingSize = DEFAULT_SHARDING_SIZE;
        
        private YamlShardingSphereAlgorithmConfiguration rateLimiter;
        
        /**
         * Build with default value.
         *
         * @return input configuration
         */
        public static YamlInputConfiguration buildWithDefaultValue() {
            return new YamlInputConfiguration();
        }
        
        /**
         * Fill in null fields with default value.
         */
        public void fillInNullFieldsWithDefaultValue() {
            if (null == workerThread) {
                workerThread = DEFAULT_WORKER_THREAD;
            }
            if (null == batchSize) {
                batchSize = DEFAULT_BATCH_SIZE;
            }
            if (null == shardingSize) {
                shardingSize = DEFAULT_SHARDING_SIZE;
            }
        }
    }
    
    @Data
    public static final class YamlOutputConfiguration implements YamlConfiguration {
        
        private static final Integer DEFAULT_WORKER_THREAD = 40;
        
        private static final Integer DEFAULT_BATCH_SIZE = 1000;
        
        private Integer workerThread = DEFAULT_WORKER_THREAD;
        
        private Integer batchSize = DEFAULT_BATCH_SIZE;
        
        private YamlShardingSphereAlgorithmConfiguration rateLimiter;
        
        /**
         * Build with default value.
         *
         * @return output configuration
         */
        public static YamlOutputConfiguration buildWithDefaultValue() {
            return new YamlOutputConfiguration();
        }
        
        /**
         * Fill in null fields with default value.
         */
        public void fillInNullFieldsWithDefaultValue() {
            if (null == workerThread) {
                workerThread = DEFAULT_WORKER_THREAD;
            }
            if (null == batchSize) {
                batchSize = DEFAULT_BATCH_SIZE;
            }
        }
    }
}

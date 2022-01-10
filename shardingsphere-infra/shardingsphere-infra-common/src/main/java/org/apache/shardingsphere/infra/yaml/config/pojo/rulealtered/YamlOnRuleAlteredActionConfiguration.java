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
        
        private int workerThread = 40;
        
        private int batchSize = 1000;
        
        private YamlShardingSphereAlgorithmConfiguration rateLimiter;
    }
    
    @Data
    public static final class YamlOutputConfiguration implements YamlConfiguration {
        
        private int workerThread = 40;
        
        private int batchSize = 1000;
        
        private YamlShardingSphereAlgorithmConfiguration rateLimiter;
    }
}

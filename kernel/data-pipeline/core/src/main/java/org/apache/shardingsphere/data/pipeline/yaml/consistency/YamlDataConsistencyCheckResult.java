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

package org.apache.shardingsphere.data.pipeline.yaml.consistency;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;

/**
 * Yaml data consistency check result config.
 */
@Getter
@Setter
public final class YamlDataConsistencyCheckResult implements YamlConfiguration {
    
    private YamlDataConsistencyCountCheckResult countCheckResult;
    
    private YamlDataConsistencyContentCheckResult contentCheckResult;
    
    /**
     * YAML data consistency count result.
     */
    @Getter
    @Setter
    public static class YamlDataConsistencyCountCheckResult implements YamlConfiguration {
        
        private long sourceRecordsCount;
        
        private long targetRecordsCount;
        
        private boolean matched;
    }
    
    /**
     * YAML data consistency content result.
     */
    @Getter
    @Setter
    public static class YamlDataConsistencyContentCheckResult implements YamlConfiguration {
        
        private boolean matched;
    }
}

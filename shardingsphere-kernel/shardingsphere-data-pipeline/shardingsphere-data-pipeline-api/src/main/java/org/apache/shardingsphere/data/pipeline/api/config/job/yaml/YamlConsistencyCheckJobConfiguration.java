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

package org.apache.shardingsphere.data.pipeline.api.config.job.yaml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * Consistency check job configuration for YAML.
 */
@Getter
@Setter
@Slf4j
@ToString
public final class YamlConsistencyCheckJobConfiguration implements YamlPipelineJobConfiguration {
    
    private String jobId;
    
    private String parentJobId;
    
    private String algorithmTypeName;
    
    private Properties algorithmProperties;
    
    @Override
    public String getTargetDatabaseName() {
        throw new UnsupportedOperationException("");
    }
}

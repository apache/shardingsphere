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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;

/**
 * Pipeline configuration.
 */
@Getter
public final class PipelineConfiguration {
    
    private YamlPipelineDataSourceConfiguration source;
    
    private YamlPipelineDataSourceConfiguration target;
    
    /**
     * Set source.
     *
     * @param source source configuration
     */
    public void setSource(final YamlPipelineDataSourceConfiguration source) {
        checkParameters(source);
        this.source = source;
    }
    
    /**
     * Set target.
     *
     * @param target target configuration
     */
    public void setTarget(final YamlPipelineDataSourceConfiguration target) {
        checkParameters(target);
        this.target = target;
    }
    
    private void checkParameters(final YamlPipelineDataSourceConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig);
        Preconditions.checkNotNull(yamlConfig.getType());
        Preconditions.checkNotNull(yamlConfig.getParameter());
    }
}

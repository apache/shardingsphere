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

package org.apache.shardingsphere.data.pipeline.spi.rulealtered;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.HandleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.spi.required.RequiredSPI;

import java.util.Collection;

/**
 * Rule altered job configuration preparer.
 */
public interface RuleAlteredJobConfigurationPreparer extends RequiredSPI {
    
    /**
     * Create handle configuration, used to build job configuration.
     *
     * @param pipelineConfig pipeline configuration
     * @return handle configuration
     */
    HandleConfiguration createHandleConfiguration(PipelineConfiguration pipelineConfig);
    
    /**
     * Create task configurations, used by underlying scheduler.
     *
     * @param pipelineConfig pipeline configuration
     * @param handleConfig handle configuration
     * @return task configurations
     */
    Collection<TaskConfiguration> createTaskConfigurations(PipelineConfiguration pipelineConfig, HandleConfiguration handleConfig);
}

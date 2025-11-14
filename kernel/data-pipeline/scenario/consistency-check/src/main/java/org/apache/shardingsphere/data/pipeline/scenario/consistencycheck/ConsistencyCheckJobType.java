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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;

/**
 * Consistency check job type.
 */
public final class ConsistencyCheckJobType implements PipelineJobType<PipelineJobConfiguration> {
    
    @Override
    public PipelineJobOption getOption() {
        return new PipelineJobOption("02", ConsistencyCheckJob.class, false, new YamlConsistencyCheckJobConfigurationSwapper(), true, null, null, false);
    }
    
    @Override
    public PipelineJobTarget getJobTarget(final PipelineJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public String getType() {
        return "CONSISTENCY_CHECK";
    }
}

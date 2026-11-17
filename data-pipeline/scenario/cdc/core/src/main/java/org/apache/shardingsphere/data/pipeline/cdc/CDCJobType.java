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

package org.apache.shardingsphere.data.pipeline.cdc;

import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;

/**
 * CDC job type.
 */
public final class CDCJobType implements PipelineJobType<CDCJobConfiguration> {
    
    @Override
    public PipelineJobOption getOption() {
        return new PipelineJobOption("03", CDCJob.class, true, new YamlCDCJobConfigurationSwapper(), false, null, null, true);
    }
    
    @Override
    public PipelineJobTarget getJobTarget(final CDCJobConfiguration jobConfig) {
        return new PipelineJobTarget(jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}

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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.metadata.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.config.processor.impl.AbstractChangedJobConfigurationProcessor;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

/**
 * Changed consistency check job configuration processor.
 */
@Slf4j
public final class ChangedConsistencyCheckJobConfigurationProcessor extends AbstractChangedJobConfigurationProcessor {
    
    @Override
    protected void onDeleted(final JobConfiguration jobConfig) {
    }
    
    @Override
    protected AbstractPipelineJob buildPipelineJob(final String jobId) {
        return new ConsistencyCheckJob(jobId);
    }
    
    @Override
    protected JobType getJobType() {
        return new ConsistencyCheckJobType();
    }
}

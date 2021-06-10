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

package org.apache.shardingsphere.scaling.core.executor.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.executor.AbstractScalingExecutor;
import org.apache.shardingsphere.scaling.core.job.FinishedCheckJob;

/**
 * Finished check job executor.
 */
@Slf4j
public final class FinishedCheckJobExecutor extends AbstractScalingExecutor {
    
    private static final String JOB_NAME = "_finished_check";
    
    private static final String CRON_EXPRESSION = "0 * * * * ?";
    
    @Override
    public void start() {
        super.start();
        log.info("Start finished check job executor.");
        new ScheduleJobBootstrap(ScalingAPIFactory.getRegistryCenter(), new FinishedCheckJob(), createJobConfig()).schedule();
    }
    
    private JobConfiguration createJobConfig() {
        return JobConfiguration.newBuilder(JOB_NAME, 1).cron(CRON_EXPRESSION).build();
    }
}

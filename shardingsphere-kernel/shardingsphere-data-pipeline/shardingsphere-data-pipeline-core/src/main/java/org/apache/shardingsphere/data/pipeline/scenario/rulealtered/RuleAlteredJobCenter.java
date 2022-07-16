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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rule altered job center.
 */
@Slf4j
public final class RuleAlteredJobCenter {
    
    private static final Map<String, RuleAlteredJob> JOB_MAP = new ConcurrentHashMap<>();
    
    /**
     * Add job.
     *
     * @param jobId job id
     * @param job job
     */
    public static void addJob(final String jobId, final RuleAlteredJob job) {
        JOB_MAP.put(jobId, job);
    }
    
    /**
     * Is job existing.
     *
     * @param jobId job id
     * @return true when job exists, else false
     */
    public static boolean isJobExisting(final String jobId) {
        return JOB_MAP.containsKey(jobId);
    }
    
    /**
     * Stop job.
     *
     * @param jobId job id
     */
    public static void stop(final String jobId) {
        RuleAlteredJob job = JOB_MAP.get(jobId);
        if (null == job) {
            log.info("job is null, ignore, jobId={}", jobId);
            return;
        }
        job.stop();
        JOB_MAP.remove(jobId);
    }
}

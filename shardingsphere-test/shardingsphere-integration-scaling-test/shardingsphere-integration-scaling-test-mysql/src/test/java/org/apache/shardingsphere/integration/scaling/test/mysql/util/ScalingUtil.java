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

package org.apache.shardingsphere.integration.scaling.test.mysql.util;

import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Scaling util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingUtil {
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     * @return started job Id
     */
    public static String startJob(final String jobConfig) {
        // TODO startJob
        return "";
    }
    
    /**
     * Get job status.
     *
     * @param jobId job ID
     * @return job status
     */
    public static String getJobStatus(final String jobId) {
        try {
            // TODO getJobStatus
            return "";
            //CHECKSTYLE:OFF
        } catch (Exception ignored) {
            //CHECKSTYLE:ON
        }
        return null;
    }
    
    /**
     * Check job.
     *
     * @param jobId job ID
     * @return check result
     */
    public static Map<String, Boolean> getJobCheckResult(final String jobId) {
        // TODO getJobCheckResult
        return Collections.emptyMap();
    }
    
    /**
     * Get job list.
     *
     * @return result
     * @throws IOException IO exception
     */
    public static JsonElement getJobList() throws IOException {
        // TODO getJobList
        return null;
    }
}

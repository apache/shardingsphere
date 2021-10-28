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
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Ok http utils.
 */
public final class ScalingUtil {
    
    private static final ScalingUtil INSTANCE = new ScalingUtil();
    
    private ScalingUtil() {
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static ScalingUtil getInstance() {
        return INSTANCE;
    }
    
    /**
     * Start job.
     *
     * @param configuration configuration
     * @return result
     * @throws IOException io exception
     */
    public Pair<Boolean, String> startJob(final String configuration) throws IOException {
        // TODO startJob
        return Pair.of(false, "");
    }
    
    /**
     * Get job status.
     *
     * @param jobId job id
     * @return job status
     */
    public String getJobStatus(final String jobId) {
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
     * @param jobId job id
     * @return check result
     * @throws IOException io exception
     */
    public Map<String, Pair<Boolean, Boolean>> getJobCheckResult(final String jobId) throws IOException {
        // TODO getJobCheckResult
        return Collections.emptyMap();
    }
    
    /**
     * Get job list.
     *
     * @return result
     * @throws IOException io exception
     */
    public JsonElement getJobList() throws IOException {
        // TODO getJobList
        return null;
    }
}

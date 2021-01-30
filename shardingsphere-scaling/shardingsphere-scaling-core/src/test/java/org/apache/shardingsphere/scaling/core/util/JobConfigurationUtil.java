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

package org.apache.shardingsphere.scaling.core.util;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Job configuration util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationUtil {
    
    /**
     * Init job config.
     *
     * @param configFile config file
     * @return job configuration
     * @throws IOException IO exception
     */
    public static JobConfiguration initJobConfig(final String configFile) throws IOException {
        try (InputStream fileInputStream = JobConfigurationUtil.class.getResourceAsStream(configFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
            return new Gson().fromJson(inputStreamReader, JobConfiguration.class);
        }
    }
    
    /**
     * Init job context by config file.
     *
     * @param configFile config file
     * @return scaling job
     * @throws IOException IO exception
     */
    public static JobContext initJobContext(final String configFile) throws IOException {
        return new JobContext(initJobConfig(configFile));
    }
}

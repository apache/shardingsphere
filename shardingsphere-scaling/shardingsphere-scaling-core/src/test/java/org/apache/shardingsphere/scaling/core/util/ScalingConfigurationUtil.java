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
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.utils.SyncConfigurationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Scaling configuration util.
 */
public final class ScalingConfigurationUtil {
    
    private static final Gson GSON = new Gson();
    
    /**
     * Init job from config file.
     *
     * @param configFile config file
     * @return ShardingScalingJob
     * @throws IOException IO exception
     */
    public static ShardingScalingJob initJob(final String configFile) throws IOException {
        try (InputStream fileInputStream = ScalingConfigurationUtil.class.getResourceAsStream(configFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
            ScalingConfiguration scalingConfiguration = GSON.fromJson(inputStreamReader, ScalingConfiguration.class);
            ShardingScalingJob shardingScalingJob = new ShardingScalingJob(scalingConfiguration);
            shardingScalingJob.getSyncConfigurations().addAll(SyncConfigurationUtil.toSyncConfigurations(scalingConfiguration));
            return shardingScalingJob;
        }
    }
}

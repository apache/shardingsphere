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

package org.apache.shardingsphere.data.pipeline.common.ratelimit;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.job.ratelimit.JobRateLimitAlgorithmInitializationException;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Properties;

/**
 * TPS job rate limit algorithm.
 */
public final class TPSJobRateLimitAlgorithm implements JobRateLimitAlgorithm {
    
    private static final String TPS_KEY = "tps";
    
    private int tps = 2000;
    
    private RateLimiter rateLimiter;
    
    @Override
    public void init(final Properties props) {
        String tpsValue = props.getProperty(TPS_KEY);
        if (!Strings.isNullOrEmpty(tpsValue)) {
            tps = Integer.parseInt(tpsValue);
            ShardingSpherePreconditions.checkState(tps > 0, () -> new JobRateLimitAlgorithmInitializationException(getType(), "TPS must be a positive number"));
        }
        rateLimiter = RateLimiter.create(tps);
    }
    
    @Override
    public void intercept(final JobOperationType type, final Number data) {
        switch (type) {
            case INSERT:
            case DELETE:
            case UPDATE:
                rateLimiter.acquire(null != data ? data.intValue() : 1);
                break;
            default:
        }
    }
    
    @Override
    public String getType() {
        return "TPS";
    }
}

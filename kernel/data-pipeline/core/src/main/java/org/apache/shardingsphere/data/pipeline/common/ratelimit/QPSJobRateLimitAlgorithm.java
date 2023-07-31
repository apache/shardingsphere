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
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Properties;

/**
 * QPS job rate limit algorithm.
 */
public final class QPSJobRateLimitAlgorithm implements JobRateLimitAlgorithm {
    
    private static final String QPS_KEY = "qps";
    
    private int qps = 50;
    
    private RateLimiter rateLimiter;
    
    @Override
    public void init(final Properties props) {
        String qpsValue = props.getProperty(QPS_KEY);
        if (!Strings.isNullOrEmpty(qpsValue)) {
            qps = Integer.parseInt(qpsValue);
            ShardingSpherePreconditions.checkState(qps > 0, () -> new JobRateLimitAlgorithmInitializationException(getType(), "QPS must be a positive number"));
        }
        rateLimiter = RateLimiter.create(qps);
    }
    
    @Override
    public void intercept(final JobOperationType type, final Number data) {
        if (type != JobOperationType.SELECT) {
            return;
        }
        rateLimiter.acquire(null != data ? data.intValue() : 1);
    }
    
    @Override
    public String getType() {
        return "QPS";
    }
}

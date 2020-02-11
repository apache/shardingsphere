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

package org.apache.shardingsphere.ui.servcie.forward.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import org.apache.shardingsphere.ui.servcie.forward.ShardingScalingForwardService;
import org.apache.shardingsphere.ui.util.HttpClientUtil;
import org.springframework.stereotype.Service;

/**
 * Implementation of forward server for sharding scaling.
 *
 * @author yangyi
 */
@Service
public final class ShardingScalingForwardServiceImpl implements ShardingScalingForwardService {
    
    @Override
    public String listAllShardingScalingJobs() {
        try {
            return HttpClientUtil.doGet("http://172.25.28.30:8084/shardingscaling/job/list");
        } catch (IOException e) {
            throw forwardShardingScalingException();
        } catch (URISyntaxException e) {
            throw invalidShardingScalingException();
        }
    }
    
    @Override
    public String startShardingScalingJobs(final String requestBody) {
        try {
            return HttpClientUtil.doPostWithJsonRequestBody("http://172.25.28.30:8084/shardingscaling/job/start", requestBody);
        } catch (IOException e) {
            throw forwardShardingScalingException();
        }
    }
    
    @Override
    public String getShardingScalingJobProgress(final int jobId) {
        try {
            return HttpClientUtil.doGet("http://172.25.28.30:8084/shardingscaling/job/progress/" + jobId);
        } catch (IOException e) {
            throw forwardShardingScalingException();
        } catch (URISyntaxException e) {
            throw invalidShardingScalingException();
        }
    }
    
    @Override
    public String stopShardingScalingJob(final String requestBody) {
        try {
            return HttpClientUtil.doPostWithJsonRequestBody("http://172.25.28.30:8084/shardingscaling/job/stop", requestBody);
        } catch (IOException e) {
            throw forwardShardingScalingException();
        }
    }
    
    private ShardingSphereUIException forwardShardingScalingException() {
        return new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "Forward Sharding Scaling Service failed");
    }
    
    private ShardingSphereUIException invalidShardingScalingException() {
        return new ShardingSphereUIException(ShardingSphereUIException.INVALID_PARAM, "Invalid Sharding Scaling Service");
    }
}

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

import java.util.Optional;
import org.apache.shardingsphere.ui.common.constant.ForwardServiceType;
import org.apache.shardingsphere.ui.common.domain.ForwardServiceConfig;
import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import org.apache.shardingsphere.ui.repository.ForwardServiceConfigsRepository;
import org.apache.shardingsphere.ui.servcie.forward.ShardingScalingForwardService;
import org.apache.shardingsphere.ui.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of forward server for sharding scaling.
 */
@Service
public final class ShardingScalingForwardServiceImpl implements ShardingScalingForwardService {
    
    private static final String LIST_JOBS_FORWARD_URL = "http://%s/shardingscaling/job/list";
    
    private static final String START_JOB_FORWARD_URL = "http://%s/shardingscaling/job/start";
    
    private static final String PROGRESS_JOB_FORWARD_URL = "http://%s/shardingscaling/job/progress/%d";
    
    private static final String STOP_JOB_FORWARD_URL = "http://%s/shardingscaling/job/stop";
    
    @Autowired
    private ForwardServiceConfigsRepository forwardServiceConfigsRepository;
    
    @Override
    public String listAllShardingScalingJobs() {
        try {
            return HttpClientUtil.doGet(String.format(LIST_JOBS_FORWARD_URL, getShardingScalingUrl()));
        } catch (IOException e) {
            throw forwardShardingScalingException();
        } catch (URISyntaxException e) {
            throw invalidShardingScalingException();
        }
    }
    
    @Override
    public String startShardingScalingJobs(final String requestBody) {
        try {
            return HttpClientUtil.doPostWithJsonRequestBody(String.format(START_JOB_FORWARD_URL, getShardingScalingUrl()), requestBody);
        } catch (IOException e) {
            throw forwardShardingScalingException();
        }
    }
    
    @Override
    public String getShardingScalingJobProgress(final int jobId) {
        try {
            return HttpClientUtil.doGet(String.format(PROGRESS_JOB_FORWARD_URL, getShardingScalingUrl(), jobId));
        } catch (IOException e) {
            throw forwardShardingScalingException();
        } catch (URISyntaxException e) {
            throw invalidShardingScalingException();
        }
    }
    
    @Override
    public String stopShardingScalingJob(final String requestBody) {
        try {
            return HttpClientUtil.doPostWithJsonRequestBody(String.format(STOP_JOB_FORWARD_URL, getShardingScalingUrl()), requestBody);
        } catch (IOException e) {
            throw forwardShardingScalingException();
        }
    }
    
    private String getShardingScalingUrl() {
        Optional<ForwardServiceConfig> shardingScalingConfig = forwardServiceConfigsRepository.load().getForwardServiceConfig(ForwardServiceType.SHARDING_SCALING.getName());
        if (shardingScalingConfig.isPresent()) {
            return shardingScalingConfig.get().getServiceUrl();
        }
        throw new ShardingSphereUIException(ShardingSphereUIException.INVALID_PARAM, "No sharding scaling service configured");
    }
    
    private ShardingSphereUIException forwardShardingScalingException() {
        return new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "Forward Sharding Scaling Service failed");
    }
    
    private ShardingSphereUIException invalidShardingScalingException() {
        return new ShardingSphereUIException(ShardingSphereUIException.INVALID_PARAM, "Invalid Sharding Scaling Service");
    }
}

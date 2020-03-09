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

package org.apache.shardingsphere.ui.web.controller;

import org.apache.shardingsphere.ui.common.constant.ForwardServiceType;
import org.apache.shardingsphere.ui.common.domain.ForwardServiceConfig;
import org.apache.shardingsphere.ui.common.domain.ForwardServiceConfigs;
import org.apache.shardingsphere.ui.repository.ForwardServiceConfigsRepository;
import org.apache.shardingsphere.ui.servcie.forward.ShardingScalingForwardService;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Sharding scaling forward controller.
 */
@RestController
@RequestMapping("/api/shardingscaling")
public final class ShardingScalingForwardController {
    
    @Autowired
    private ShardingScalingForwardService shardingScalingForwardService;
    
    @Autowired
    private ForwardServiceConfigsRepository forwardServiceConfigsRepository;
    
    /**
     * Get sharding scaling service config.
     *
     * @return sharding scaling service config
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult getService() {
        Optional<ForwardServiceConfig> result = forwardServiceConfigsRepository.load().getForwardServiceConfig(ForwardServiceType.SHARDING_SCALING.getName());
        return result.isPresent() ? ResponseResultUtil.build(result.get()) : ResponseResultUtil.handleIllegalArgumentException("No configured sharding scaling services");
    }
    
    /**
     * Configure sharding scaling service.
     *
     * @param shardingScalingServiceConfig sharding scaling service config
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseResult configService(@RequestBody final ForwardServiceConfig shardingScalingServiceConfig) {
        ForwardServiceConfigs currentForwardServiceConfig = forwardServiceConfigsRepository.load();
        currentForwardServiceConfig.putForwardServiceConfig(ForwardServiceType.SHARDING_SCALING.getName(), shardingScalingServiceConfig);
        forwardServiceConfigsRepository.save(currentForwardServiceConfig);
        return ResponseResultUtil.success();
    }
    
    /**
     * Delete sharding scaling service.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseResult deleteService() {
        ForwardServiceConfigs currentForwardServiceConfig = forwardServiceConfigsRepository.load();
        currentForwardServiceConfig.removeForwardServiceConfig(ForwardServiceType.SHARDING_SCALING.getName());
        forwardServiceConfigsRepository.save(currentForwardServiceConfig);
        return ResponseResultUtil.success();
    }
    
    /**
     * List all sharding scaling jobs.
     *
     * @return response result
     */
    @RequestMapping(value = "/job/list", method = RequestMethod.GET)
    public ResponseResult listAllShardingScalingJobs() {
        return ResponseResultUtil.buildFromJson(shardingScalingForwardService.listAllShardingScalingJobs());
    }
    
    /**
     * Start sharding scaling job progress.
     *
     * @param requestBody request body of start sharding scaling job
     * @return response result
     */
    @RequestMapping(value = "/job/start", method = RequestMethod.POST)
    public ResponseResult startShardingScalingJob(@RequestBody final String requestBody) {
        return ResponseResultUtil.buildFromJson(shardingScalingForwardService.startShardingScalingJobs(requestBody));
    }
    
    /**
     * Get sharding scaling job progress.
     *
     * @param jobId job id
     * @return response result
     */
    @RequestMapping(value = "/job/progress/{jobId}", method = RequestMethod.GET)
    public ResponseResult getShardingScalingJobProgress(@PathVariable("jobId") final int jobId) {
        return ResponseResultUtil.buildFromJson(shardingScalingForwardService.getShardingScalingJobProgress(jobId));
    }
    
    /**
     * Stop sharding scaling job progress.
     *
     * @param requestBody request body of stop sharding scaling job
     * @return response result
     */
    @RequestMapping(value = "/job/stop", method = RequestMethod.POST)
    public ResponseResult stopShardingScalingJob(@RequestBody final String requestBody) {
        return ResponseResultUtil.buildFromJson(shardingScalingForwardService.stopShardingScalingJob(requestBody));
    }
}

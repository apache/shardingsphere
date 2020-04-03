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

import org.apache.shardingsphere.ui.common.constant.OrchestrationType;
import org.apache.shardingsphere.ui.common.domain.CenterConfig;
import org.apache.shardingsphere.ui.common.dto.CenterConfigDTO;
import org.apache.shardingsphere.ui.servcie.CenterConfigService;
import org.apache.shardingsphere.ui.util.CenterRepositoryFactory;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RESTful API of config center configuration.
 */
@RestController
@RequestMapping("/api/config-center")
public final class ConfigCenterController {
    
    @Autowired
    private CenterConfigService centerConfigService;
    
    /**
     * Load all config center configs.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<List<CenterConfig>> loadConfigs() {
        return ResponseResultUtil.build(centerConfigService.loadAll(OrchestrationType.CONFIG_CENTER.getValue()).getCenterConfigs());
    }
    
    /**
     * Add config center config.
     *
     * @param config config center config
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseResult add(@RequestBody final CenterConfig config) {
        centerConfigService.add(config);
        return ResponseResultUtil.success();
    }
    
    /**
     * Delete config center config.
     *
     * @param config config center config
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseResult delete(@RequestBody final CenterConfig config) {
        centerConfigService.delete(config.getName(), OrchestrationType.CONFIG_CENTER.getValue());
        return ResponseResultUtil.success();
    }
    
    /**
     * Connect config center.
     * 
     * @param config config center config
     * @return response result
     */
    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    public ResponseResult<Boolean> connect(@RequestBody final CenterConfig config) {
        CenterRepositoryFactory.createConfigCenter(centerConfigService.load(config.getName(), OrchestrationType.CONFIG_CENTER.getValue()));
        centerConfigService.setActivated(config.getName(), OrchestrationType.CONFIG_CENTER.getValue());
        return ResponseResultUtil.build(Boolean.TRUE);
    }
    
    /**
     * Get activated config center config.
     *
     * @return response result
     */
    @RequestMapping(value = "/activated", method = RequestMethod.GET)
    public ResponseResult<CenterConfig> activated() {
        return ResponseResultUtil.build(centerConfigService.loadActivated(OrchestrationType.CONFIG_CENTER.getValue()).orElse(null));
    }
    
    /**
     * Update config center.
     *
     * @param config config center config
     * @return response result
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseResult update(@RequestBody final CenterConfigDTO config) {
        centerConfigService.update(config);
        return ResponseResultUtil.success();
    }
}

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

import org.apache.shardingsphere.ui.common.domain.RegistryCenterConfig;
import org.apache.shardingsphere.ui.servcie.RegistryCenterConfigService;
import org.apache.shardingsphere.ui.util.RegistryCenterFactory;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RESTful API of registry center configuration.
 *
 * @author chenqingyang
 */
@RestController
@RequestMapping("/api/reg-center")
public final class RegistryCenterController {
    
    @Autowired
    private RegistryCenterConfigService registryCenterConfigService;
    
    /**
     * Load all registry center configs.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<List<RegistryCenterConfig>> loadConfigs() {
        return ResponseResultUtil.build(registryCenterConfigService.loadAll().getRegistryCenterConfigs());
    }
    
    /**
     * Add registry center config.
     *
     * @param config registry center config
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseResult add(@RequestBody final RegistryCenterConfig config) {
        registryCenterConfigService.add(config);
        return ResponseResultUtil.success();
    }
    
    /**
     * Delete registry center config.
     *
     * @param config registry center config
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseResult delete(@RequestBody final RegistryCenterConfig config) {
        registryCenterConfigService.delete(config.getName());
        return ResponseResultUtil.success();
    }
    
    /**
     * Connect registry center.
     * 
     * @param config registry center config
     * @return response result
     */
    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    public ResponseResult<Boolean> connect(@RequestBody final RegistryCenterConfig config) {
        RegistryCenterFactory.createRegistryCenter(registryCenterConfigService.load(config.getName()));
        registryCenterConfigService.setActivated(config.getName());
        return ResponseResultUtil.build(Boolean.TRUE);
    }
    
    /**
     * Get activated registry center config.
     *
     * @return response result
     */
    @RequestMapping(value = "/activated", method = RequestMethod.GET)
    public ResponseResult<RegistryCenterConfig> activated() {
        return ResponseResultUtil.build(registryCenterConfigService.loadActivated().orNull());
    }
    
}

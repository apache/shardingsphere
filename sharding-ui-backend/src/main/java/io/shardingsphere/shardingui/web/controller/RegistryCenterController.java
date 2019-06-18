/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.web.controller;

import io.shardingsphere.shardingui.common.domain.RegistryCenterConfig;
import io.shardingsphere.shardingui.servcie.RegistryCenterConfigService;
import io.shardingsphere.shardingui.util.RegistryCenterFactory;
import io.shardingsphere.shardingui.web.response.ResponseResult;
import io.shardingsphere.shardingui.web.response.ResponseResultUtil;
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

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

import org.apache.shardingsphere.ui.servcie.ShardingPropertiesService;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RESTful API of sharding properties.
 */
@RestController
@RequestMapping("/api/props")
public final class ShardingPropertiesController {
    
    @Autowired
    private ShardingPropertiesService shardingPropertiesService;
    
    /**
     * Load sharding properties.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<String> loadShardingProperties() {
        return ResponseResultUtil.build(shardingPropertiesService.loadShardingProperties());
    }
    
    /**
     * Update sharding properties.
     *
     * @param configMap config map
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseResult updateShardingProperties(@RequestBody final Map<String, String> configMap) {
        shardingPropertiesService.updateShardingProperties(configMap.get("props"));
        return ResponseResultUtil.success();
    }
}

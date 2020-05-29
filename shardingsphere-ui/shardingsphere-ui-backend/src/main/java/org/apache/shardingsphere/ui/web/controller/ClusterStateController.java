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

import org.apache.shardingsphere.cluster.state.DataSourceState;
import org.apache.shardingsphere.cluster.state.InstanceState;
import org.apache.shardingsphere.ui.servcie.ClusterService;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RESTful API of cluster state.
 */
@RestController
@RequestMapping("/api/cluster-state")
public final class ClusterStateController {
    
    @Autowired
    private ClusterService clusterService;
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<Map<String, Object>> loadAllInstanceStates() {
        return ResponseResultUtil.build(mergeDataSources(clusterService.loadAllInstanceStates()));
    }
    
    private Map<String, Object> mergeDataSources(final Map<String, InstanceState> instanceStateMap) {
        Map<String, Object> result = new HashMap<>();
        Map<String, DataSourceState> dataSourceStateMap = new HashMap<>();
        result.put("instanceStates", instanceStateMap);
        result.put("dataSourceStates", dataSourceStateMap);
        instanceStateMap.values().forEach(each ->
            each.getDataSources().entrySet().forEach(entry -> dataSourceStateMap.put(entry.getKey(), entry.getValue()))
        );
        return result;
    }
}

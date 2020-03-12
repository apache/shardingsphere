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

import org.apache.shardingsphere.ui.common.dto.ShardingSchemaDTO;
import org.apache.shardingsphere.ui.servcie.ShardingSchemaService;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

/**
 * RESTful API of sharding schema configuration.
 */
@RestController
@RequestMapping("/api/schema")
public final class ShardingSchemaController {
    
    @Autowired
    private ShardingSchemaService shardingSchemaService;
    
    /**
     * Load all schema names.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<Collection<String>> loadAllSchemaNames() {
        return ResponseResultUtil.build(shardingSchemaService.getAllSchemaNames());
    }
    
    /**
     * Add schema configuration.
     *
     * @param shardingSchema sharding schema DTO.
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseResult addSchema(final @RequestBody ShardingSchemaDTO shardingSchema) {
        shardingSchemaService.addSchemaConfiguration(shardingSchema.getName(), shardingSchema.getRuleConfiguration(), shardingSchema.getDataSourceConfiguration());
        return ResponseResultUtil.success();
    }
    
    /**
     * Load rule configuration.
     *
     * @param schemaName schema name
     * @return response result
     */
    @RequestMapping(value = "/rule/{schemaName}", method = RequestMethod.GET)
    public ResponseResult<String> loadRuleConfiguration(@PathVariable("schemaName") final String schemaName) {
        return ResponseResultUtil.build(shardingSchemaService.getRuleConfiguration(schemaName));
    }
    
    /**
     * Update rule configuration.
     *
     * @param schemaName schema name
     * @param configMap config map
     * @return response result
     */
    @RequestMapping(value = "/rule/{schemaName}", method = RequestMethod.PUT)
    public ResponseResult updateRuleConfiguration(@PathVariable("schemaName") final String schemaName, @RequestBody final Map<String, String> configMap) {
        shardingSchemaService.updateRuleConfiguration(schemaName, configMap.get("ruleConfig"));
        return ResponseResultUtil.success();
    }
    
    /**
     * Load data source configuration.
     *
     * @param schemaName schema name
     * @return response result
     */
    @RequestMapping(value = "/datasource/{schemaName}", method = RequestMethod.GET)
    public ResponseResult<String> loadDataSourceConfiguration(@PathVariable("schemaName") final String schemaName) {
        return ResponseResultUtil.build(shardingSchemaService.getDataSourceConfiguration(schemaName));
    }
    
    /**
     * Update data source configuration.
     *
     * @param schemaName schema name
     * @param configMap config map
     * @return response result
     */
    @RequestMapping(value = "/datasource/{schemaName}", method = RequestMethod.PUT)
    public ResponseResult updateDataSourceConfiguration(@PathVariable("schemaName") final String schemaName, @RequestBody final Map<String, String> configMap) {
        shardingSchemaService.updateDataSourceConfiguration(schemaName, configMap.get("dataSourceConfig"));
        return ResponseResultUtil.success();
    }
    
}

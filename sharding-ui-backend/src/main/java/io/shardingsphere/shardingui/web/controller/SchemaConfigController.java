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

import io.shardingsphere.shardingui.servcie.SchemaConfigService;
import io.shardingsphere.shardingui.web.response.ResponseResult;
import io.shardingsphere.shardingui.web.response.ResponseResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * RESTful API of schema configuration.
 *
 * @author chenqingyang
 */
@Slf4j
@RestController
@RequestMapping("/api/schema")
public final class SchemaConfigController {
    
    @Autowired
    private SchemaConfigService schemaConfigService;
    
    /**
     * Load all schema names.
     *
     * @return response result
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseResult<Collection<String>> loadAllSchemaNames() {
        return ResponseResultUtil.build(schemaConfigService.getAllSchemaNames());
    }
    
    /**
     * Load rule configuration.
     *
     * @param schemaName schema name
     * @return response result
     */
    @RequestMapping(value = "/rule/{schemaName}", method = RequestMethod.GET)
    public ResponseResult<String> loadRuleConfiguration(@PathVariable("schemaName") final String schemaName) {
        return ResponseResultUtil.build(schemaConfigService.getRuleConfiguration(schemaName));
    }
    
    /**
     * Update rule configuration.
     *
     * @param schemaName schema name
     * @param ruleConfig rule config
     * @return response result
     */
    @RequestMapping(value = "/rule/{schemaName}", method = RequestMethod.PUT)
    public ResponseResult updateRuleConfiguration(@PathVariable("schemaName") final String schemaName, @RequestBody final String ruleConfig) {
        schemaConfigService.updateRuleConfiguration(schemaName, ruleConfig);
        return ResponseResultUtil.success();
    }
    
    /**
     * Load datasource configuration.
     *
     * @param schemaName schema name
     * @return response result
     */
    @RequestMapping(value = "/datasource/{schemaName}", method = RequestMethod.GET)
    public ResponseResult<String> loadDataSourceConfiguration(@PathVariable("schemaName") final String schemaName) {
        return ResponseResultUtil.build(schemaConfigService.getDataSourceConfiguration(schemaName));
    }
    
    /**
     * Update datasource configuration.
     *
     * @param schemaName schema name
     * @param datasourceConfig datasource config
     * @return response result
     */
    @RequestMapping(value = "/datasource/{schemaName}", method = RequestMethod.PUT)
    public ResponseResult updateDataSourceConfiguration(@PathVariable("schemaName") final String schemaName, @RequestBody final String datasourceConfig) {
        schemaConfigService.updateDataSourceConfiguration(schemaName, datasourceConfig);
        return ResponseResultUtil.success();
    }
    
}

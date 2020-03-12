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

package org.apache.shardingsphere.ui.servcie;

import java.util.Collection;

/**
 * Sharding schema service.
 */
public interface ShardingSchemaService {
    
    /**
     * Get all schema names.
     *
     * @return all schema names
     */
    Collection<String> getAllSchemaNames();
    
    /**
     * Get rule configuration.
     *
     * @param schemaName schema name
     * @return rule configuration
     */
    String getRuleConfiguration(String schemaName);
    
    /**
     * Get data source configuration.
     *
     * @param schemaName schema name
     * @return data source configuration
     */
    String getDataSourceConfiguration(String schemaName);
    
    /**
     * Update rule configuration.
     *
     * @param schemaName schema name
     * @param configData config data
     */
    void updateRuleConfiguration(String schemaName, String configData);
    
    /**
     * Update data source configuration.
     *
     * @param schemaName schema name
     * @param configData config data
     */
    void updateDataSourceConfiguration(String schemaName, String configData);
    
    /**
     * Add schema configuration.
     *
     * @param schemaName schema name
     * @param ruleConfiguration rule configuration
     * @param dataSourceConfiguration data source configuration
     */
    void addSchemaConfiguration(String schemaName, String ruleConfiguration, String dataSourceConfiguration);
}

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

package org.apache.shardingsphere.infra.context.manager;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Context manager builder.
 */
public interface ContextManagerBuilder extends TypedSPI {
    
    /**
     * Build context manager.
     * 
     * @param mode ShardingSphere mode
     * @param dataSourcesMap data sources map
     * @param schemaRuleConfigs schema rule configurations
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite to persistence
     * @return context manager
     * @throws SQLException SQL exception
     */
    ContextManager build(ShardingSphereMode mode, Map<String, Map<String, DataSource>> dataSourcesMap,
                         Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, Collection<RuleConfiguration> globalRuleConfigs, Properties props, boolean isOverwrite) throws SQLException;
}

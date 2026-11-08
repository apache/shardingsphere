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

package org.apache.shardingsphere.data.pipeline.api.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;

import java.util.Collection;
import java.util.Map;

/**
 * Pipeline data source configuration for ShardingSphere-JDBC.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "parameter")
public final class ShardingSpherePipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    public static final String TYPE = "ShardingSphereJDBC";
    
    private final String parameter;
    
    private final String databaseName;
    
    private final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    /**
     * Original JDBC URL property keys used when restoring actual data source parameters.
     */
    private final Map<String, String> jdbcUrlKeys;
    
    /**
     * Original customPoolProps keys; an absent entry differs from an explicitly empty group.
     */
    private final Map<String, Collection<String>> customPoolPropertiesKeys;
    
    private final Collection<RuleConfiguration> ruleConfigurations;
    
    private final DatabaseType databaseType;
    
    @Override
    public Object getDataSourceConfiguration() {
        return this;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}

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

package org.apache.shardingsphere.scaling.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.scaling.core.utils.ConfigurationYamlConverter;

import java.util.Map;

/**
 * ShardingSphere-JDBC scaling data source configuration.
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = "databaseType")
public final class ShardingSphereJDBCScalingDataSourceConfiguration implements ScalingDataSourceConfiguration {
    
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    
    @Expose
    private String dataSource;
    
    @Expose
    private String rule;
    
    private DatabaseType databaseType;
    
    public ShardingSphereJDBCScalingDataSourceConfiguration(final String dataSource, final String rule) {
        this.dataSource = dataSource;
        this.rule = rule;
        databaseType = getDatabaseType();
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        if (null == databaseType) {
            Map<String, Object> props = ConfigurationYamlConverter.loadDataSourceConfigurations(dataSource).values().iterator().next().getProps();
            databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(props.getOrDefault("url", props.get("jdbcUrl")).toString());
        }
        return databaseType;
    }
    
    /**
     * To json tree.
     *
     * @return json element
     */
    public JsonElement toJsonTree() {
        return GSON.toJsonTree(this);
    }
}

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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.scaling.core.utils.ConfigurationYamlConverter;

import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(exclude = "databaseType")
public class ShardingSphereJDBCConfiguration implements DataSourceConfiguration {
    
    private String dataSource;
    
    private String rule;
    
    private DatabaseType databaseType;
    
    public ShardingSphereJDBCConfiguration(final String dataSource, final String rule) {
        this.dataSource = dataSource;
        this.rule = rule;
        this.databaseType = getDatabaseType();
    }
    
    public DatabaseType getDatabaseType() {
        if (databaseType == null) {
            Map<String, org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration> sourceDataSource = ConfigurationYamlConverter.loadDataSourceConfigurations(dataSource);
            this.databaseType = DatabaseTypes.getDatabaseTypeByURL(sourceDataSource.values().iterator().next().getProps().get("jdbcUrl").toString());
        }
        return databaseType;
    }
}

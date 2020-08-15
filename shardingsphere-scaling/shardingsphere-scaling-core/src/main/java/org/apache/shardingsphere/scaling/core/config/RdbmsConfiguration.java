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
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;

import java.util.Map;
import java.util.Set;

/**
 * Relational database management system configuration.
 */
@Setter
@Getter
@EqualsAndHashCode
public final class RdbmsConfiguration implements Cloneable {
    
    private String dataSourceName;
    
    private DataSourceConfiguration dataSourceConfiguration;
    
    private String tableName;
    
    private Map<String, Set<String>> shardingColumnsMap;
    
    private String primaryKey;
    
    @SuppressWarnings("rawtypes")
    private PositionManager positionManager;
    
    private Integer spiltNum;
    
    private Map<String, String> tableNameMap;
    
    private int retryTimes;
    
    /**
     * Clone to new rdbms configuration.
     *
     * @param origin origin rdbms configuration
     * @return new rdbms configuration
     */
    @SneakyThrows
    public static RdbmsConfiguration clone(final RdbmsConfiguration origin) {
        return (RdbmsConfiguration) origin.clone();
    }
    
}

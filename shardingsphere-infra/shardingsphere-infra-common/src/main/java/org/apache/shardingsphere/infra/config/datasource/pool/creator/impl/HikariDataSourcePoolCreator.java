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

package org.apache.shardingsphere.infra.config.datasource.pool.creator.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Hikari data source pool creator.
 */
@Getter
public final class HikariDataSourcePoolCreator extends AbstractDataSourcePoolCreator {
    
    private final Map<String, String> propertySynonyms = new HashMap<>(2, 1);
    
    private final Map<String, Object> invalidProperties = new HashMap<>(2, 1);
    
    public HikariDataSourcePoolCreator() {
        buildPropertySynonyms();
        buildInvalidProperties();
    }
    
    private void buildPropertySynonyms() {
        propertySynonyms.put("maxPoolSize", "maximumPoolSize");
        propertySynonyms.put("minPoolSize", "minimumIdle");
    }
    
    private void buildInvalidProperties() {
        invalidProperties.put("minimumIdle", -1);
        invalidProperties.put("maximumPoolSize", -1);
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}

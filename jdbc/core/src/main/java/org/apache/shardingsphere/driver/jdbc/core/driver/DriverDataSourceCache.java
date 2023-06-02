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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver data source cache.
 */
public final class DriverDataSourceCache {
    
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    
    /**
     * Get data source.
     * 
     * @param url URL
     * @return got data source
     */
    public DataSource get(final String url) {
        if (dataSourceMap.containsKey(url)) {
            return dataSourceMap.get(url);
        }
        return dataSourceMap.computeIfAbsent(url, this::createDataSource);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Throwable> DataSource createDataSource(final String url) throws T {
        try {
            return YamlShardingSphereDataSourceFactory.createDataSource(ShardingSphereDriverURLManager.getContent(url));
        } catch (final IOException ex) {
            throw (T) new SQLException(ex);
        } catch (final SQLException ex) {
            throw (T) ex;
        }
    }
}

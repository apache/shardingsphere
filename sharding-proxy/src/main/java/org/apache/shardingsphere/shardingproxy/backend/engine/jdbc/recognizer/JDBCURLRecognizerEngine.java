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

package org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.recognizer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.recognizer.spi.JDBCURLRecognizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 * JDBC URL recognizer engine.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCURLRecognizerEngine {
    
    private static final Map<String, String> URL_PREFIX_AND_DRIVER_CLASS_NAME_MAPPER = new HashMap<>();
    
    static {
        load();
    }
    
    private static void load() {
        for (JDBCURLRecognizer each : ServiceLoader.load(JDBCURLRecognizer.class)) {
            for (String prefix : each.getURLPrefixes()) {
                URL_PREFIX_AND_DRIVER_CLASS_NAME_MAPPER.put(prefix, each.getDriverClassName());
            }
        }
    }
    
    /**
     * Get JDBC driver class name.
     * 
     * @param url JDBC URL
     * @return driver class name
     */
    public static String getDriverClassName(final String url) {
        for (Entry<String, String> entry : URL_PREFIX_AND_DRIVER_CLASS_NAME_MAPPER.entrySet()) {
            if (url.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new ShardingException("Cannot resolve JDBC url `%s`. Please implements `%s` and add to SPI.", url, JDBCURLRecognizer.class.getName());
    }
    
    /**
     * Get database type.
     *
     * @param url JDBC URL
     * @return database type
     */
    public static DatabaseType getDatabaseType(final String url) {
        switch (getDriverClassName(url)) {
            case "com.mysql.cj.jdbc.Driver":
            case "com.mysql.jdbc.Driver":
                return DatabaseType.MySQL;
            case "org.postgresql.Driver":
                return DatabaseType.PostgreSQL;
            case "oracle.jdbc.driver.OracleDriver":
                return DatabaseType.Oracle;
            case "com.microsoft.sqlserver.jdbc.SQLServerDriver":
                return DatabaseType.SQLServer;
            case "org.h2.Driver":
                return DatabaseType.H2;
            default:
                throw new ShardingException("Cannot resolve JDBC url `%s`", url);
        }
    }
}

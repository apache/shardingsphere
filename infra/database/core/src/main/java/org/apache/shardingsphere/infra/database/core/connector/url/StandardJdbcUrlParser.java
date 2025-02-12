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

package org.apache.shardingsphere.infra.database.core.connector.url;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Properties;

/**
 * Standard JDBC URL parser.
 */
public final class StandardJdbcUrlParser {
    
    /**
     * Parse JDBC URL.
     *
     * @param jdbcUrl JDBC URL to be parsed
     * @return parsed JDBC URL
     */
    public JdbcUrl parse(final String jdbcUrl) {
        for (DialectJdbcUrlParser each : ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlParser.class)) {
            if (each.accept(jdbcUrl)) {
                return each.parse(jdbcUrl);
            }
        }
        return new DefaultJdbcUrlParser().parse(jdbcUrl);
    }
    
    /**
     * Parse query properties.
     *
     * @param query query parameter
     * @return query properties
     */
    public Properties parseQueryProperties(final String query) {
        if (Strings.isNullOrEmpty(query)) {
            return new Properties();
        }
        Properties result = new Properties();
        for (String each : Splitter.on("&").split(query)) {
            String[] property = each.split("=", 2);
            result.setProperty(property[0], property[1]);
        }
        return result;
    }
}

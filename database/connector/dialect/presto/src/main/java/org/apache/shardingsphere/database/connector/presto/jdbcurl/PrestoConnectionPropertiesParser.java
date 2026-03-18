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

package org.apache.shardingsphere.database.connector.presto.jdbcurl;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;

/**
 * Connection properties parser of Presto.
 */
public final class PrestoConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final int DEFAULT_PORT = 8080;
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        return new StandardJdbcUrlParser().parse(url, DEFAULT_PORT);
    }
    
    @Override
    public String getDatabaseType() {
        return "Presto";
    }
}

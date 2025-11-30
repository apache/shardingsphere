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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.jdbcurl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connection properties parser of SQLServer.
 */
public final class SQLServerConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final int DEFAULT_PORT = 1433;
    
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:(microsoft:|tc:)?sqlserver:.*//([\\w-.]+):?(\\d*);\\S*(databaseName|database)=([\\w-.]+);?", Pattern.CASE_INSENSITIVE);
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        Matcher matcher = URL_PATTERN.matcher(url);
        ShardingSpherePreconditions.checkState(matcher.find(), () -> new UnrecognizedDatabaseURLException(url, URL_PATTERN.pattern()));
        return new ConnectionProperties(matcher.group(2), Strings.isNullOrEmpty(matcher.group(3)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(3)), matcher.group(5), null, new Properties());
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLServer";
    }
}

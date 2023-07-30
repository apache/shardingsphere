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

package org.apache.shardingsphere.infra.database.sql92.connector;

import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.connector.StandardConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connection properties parser of SQL92.
 */
public final class SQL92ConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final int DEFAULT_PORT = -1;
    
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:.*", Pattern.CASE_INSENSITIVE);
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        Matcher matcher = URL_PATTERN.matcher(url);
        ShardingSpherePreconditions.checkState(matcher.find(), () -> new UnrecognizedDatabaseURLException(url, URL_PATTERN.pattern()));
        return new StandardConnectionProperties("", DEFAULT_PORT, "", null);
    }
    
    @Override
    public String getDatabaseType() {
        return "SQL92";
    }
}

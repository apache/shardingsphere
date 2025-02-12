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

package org.apache.shardingsphere.infra.database.oracle.connector.url;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.core.connector.url.DialectJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC URL parser for Oracle.
 */
public final class OracleJdbcUrlParser implements DialectJdbcUrlParser {
    
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:oracle:thin:@(?<hostDelimiter>//)?(?<host>[\\w\\-\\.]+):(?<port>\\d+)(?<databaseDelimiter>[:/]{1}){1}(?<database>.*)",
            Pattern.CASE_INSENSITIVE);
    
    @Override
    public boolean accept(final String jdbcUrl) {
        return jdbcUrl.contains(":oracle:");
    }
    
    @Override
    public JdbcUrl parse(final String jdbcUrl) {
        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
        ShardingSpherePreconditions.checkState(matcher.find(), () -> new UnrecognizedDatabaseURLException(jdbcUrl, JDBC_URL_PATTERN.pattern()));
        return new JdbcUrl(matcher.group("host"), Strings.isNullOrEmpty(matcher.group("port")) ? 1521 : Integer.parseInt(matcher.group("port")),
                matcher.group("database"), new Properties());
    }
}

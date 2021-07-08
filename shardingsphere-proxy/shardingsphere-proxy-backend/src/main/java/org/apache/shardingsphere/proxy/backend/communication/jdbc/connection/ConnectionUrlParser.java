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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC connection url parser.
 */
public final class ConnectionUrlParser {
    
    private static final String KEY_SCHEME = "scheme";
    
    private static final String KEY_AUTHORITY = "authority";
    
    private static final String KEY_PATH = "path";
    
    private static final String KEY_QUERY = "query";
    
    private static final Pattern CONNECTION_URL_PATTERN = Pattern.compile(
            // scheme
            "(?<scheme>[\\w\\+:%]+)\\s*"
                    // authority
                    + "(?://(?<authority>[^/?#]*))?\\s*"
                    // path
                    + "(?:/(?!\\s*/)(?<path>[^?#]*))?"
                    // query
                    + "(?:\\?(?!\\s*\\?)(?<query>[^#]*))?");
    
    private final Matcher matcher;
    
    private String scheme;
    
    private String authority;
    
    private String path;
    
    private String query;
    
    public ConnectionUrlParser(final String jdbcUrl) {
        matcher = CONNECTION_URL_PATTERN.matcher(jdbcUrl);
        if (!matcher.matches()) {
            throw new ShardingSphereConfigurationException("Incorrect JDBC url format: %s", jdbcUrl);
        }
        this.scheme = matcher.group(KEY_SCHEME);
        this.authority = matcher.group(KEY_AUTHORITY);
        this.path = matcher.group(KEY_PATH);
        this.query = matcher.group(KEY_QUERY);
    }
    
    /**
     * Get properties of JDBC url.
     *
     * @return connection properties
     */
    public Optional<String> getQuery() {
        return Strings.isNullOrEmpty(query) ? Optional.empty() : Optional.of(query);
    }
    
    /**
     * Get properties map of JDBC url.
     *
     * @return properties map
     */
    public Map<String, String> getQueryMap() {
        if (getQuery().isPresent()) {
            return Splitter.on("&").withKeyValueSeparator("=").split(getQuery().get());
        }
        return new HashMap<>(16, 1);
    }
}

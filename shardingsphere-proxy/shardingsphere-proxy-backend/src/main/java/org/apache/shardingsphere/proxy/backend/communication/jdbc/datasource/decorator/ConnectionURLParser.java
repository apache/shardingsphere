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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.decorator;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC connection URL parser.
 */
public final class ConnectionURLParser {
    
    private static final String PROPS_GROUP_KEY = "props";
    
    private static final String SCHEMA_PATTERN = "(?<schema>[\\w\\+:%]+)\\s*";
    
    private static final String AUTHORITY_PATTERN = "(?://(?<authority>[^/?#]*))?\\s*";
    
    private static final String PATH_PATTERN = "(?:/(?!\\s*/)(?<path>[^?#]*))?";
    
    private static final String PROPS_PATTERN = "(?:\\?(?!\\s*\\?)(?<props>[^#]*))?";
    
    private static final Pattern CONNECTION_URL_PATTERN = Pattern.compile(SCHEMA_PATTERN + AUTHORITY_PATTERN + PATH_PATTERN + PROPS_PATTERN);
    
    private final String props;
    
    public ConnectionURLParser(final String jdbcURL) {
        Matcher matcher = CONNECTION_URL_PATTERN.matcher(jdbcURL);
        if (!matcher.matches()) {
            throw new ShardingSphereConfigurationException("Incorrect JDBC URL format: %s", jdbcURL);
        }
        props = matcher.group(PROPS_GROUP_KEY);
    }
    
    /**
     * Get properties from JDBC connection URL.
     *
     * @return properties
     */
    public Map<String, String> getProperties() {
        return Strings.isNullOrEmpty(props) ? Collections.emptyMap() : Splitter.on("&").withKeyValueSeparator("=").split(props);
    }
}

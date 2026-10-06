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

package org.apache.shardingsphere.database.connector.oracle.jdbcurl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connection properties parser of Oracle.
 */
public final class OracleConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final int DEFAULT_PORT = 1521;
    
    private static final int THIN_MATCH_GROUP_COUNT = 6;
    
    private static final String QUERY_DELIMITER = "?";
    
    private static final Pattern THIN_URL_PATTERN = Pattern.compile(
            "jdbc:oracle:(thin|oci|kprb):@((?:(?:tcp|tcps):)?//)?(\\[[^]]+\\]|[\\w\\-\\.]+)(?::(\\d+))?"
                    + "(?:/([\\w\\-\\.]+)(?::(?:DEDICATED|SHARED|POOLED))?(?:/[\\w\\-\\.]+)?|:([\\w\\-\\.]+)|//[\\w\\-\\.]+|/:(?:DEDICATED|SHARED|POOLED)(?:/[\\w\\-\\.]+)?)?",
            Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CONNECT_DESCRIPTOR_URL_PATTERN = Pattern.compile(
            "jdbc:oracle:(thin|oci|kprb):@[(\\w\\s=)]+HOST\\s*=\\s*(\\[[^]]+\\]|[\\w\\-\\.]+).*PORT\\s*=\\s*(\\d+).*SERVICE_NAME\\s*=\\s*([\\w\\.]+)\\).*\\)");
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        String urlWithoutQuery = url.contains(QUERY_DELIMITER) ? url.substring(0, url.indexOf(QUERY_DELIMITER)) : url;
        Properties queryProps = url.contains(QUERY_DELIMITER) ? new StandardJdbcUrlParser().parseQueryProperties(url.substring(url.indexOf(QUERY_DELIMITER) + 1)) : new Properties();
        List<Matcher> matchers = Arrays.asList(THIN_URL_PATTERN.matcher(urlWithoutQuery), CONNECT_DESCRIPTOR_URL_PATTERN.matcher(urlWithoutQuery));
        Matcher matcher = matchers.stream().filter(Matcher::matches).filter(each -> THIN_MATCH_GROUP_COUNT == each.groupCount() || hasNoDescriptorTail(urlWithoutQuery))
                .findAny().orElseThrow(() -> new UnrecognizedDatabaseURLException(url, THIN_URL_PATTERN.pattern()));
        int groupCount = matcher.groupCount();
        return THIN_MATCH_GROUP_COUNT == groupCount ? getThinConnectionProperties(username, matcher, queryProps) : getStandardConnectionProperties(username, matcher, queryProps);
    }
    
    private boolean hasNoDescriptorTail(final String url) {
        int depth = 0;
        for (int index = url.indexOf('@') + 1; index < url.length(); index++) {
            char current = url.charAt(index);
            if ('(' == current) {
                depth++;
                continue;
            }
            if (')' != current || --depth > 0) {
                continue;
            }
            int nextIndex = index + 1;
            while (nextIndex < url.length() && Character.isWhitespace(url.charAt(nextIndex))) {
                nextIndex++;
            }
            if (nextIndex < url.length() && '(' != url.charAt(nextIndex) && ')' != url.charAt(nextIndex)) {
                return false;
            }
        }
        return true;
    }
    
    private ConnectionProperties getThinConnectionProperties(final String username, final Matcher matcher, final Properties queryProps) {
        String serviceName = null == matcher.group(5) ? matcher.group(6) : matcher.group(5);
        String hostname = matcher.group(3);
        return new ConnectionProperties(hostname.startsWith("[") ? hostname.substring(1, hostname.length() - 1) : hostname,
                Strings.isNullOrEmpty(matcher.group(4)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(4)), serviceName, username, queryProps);
    }
    
    private ConnectionProperties getStandardConnectionProperties(final String username, final Matcher matcher, final Properties queryProps) {
        String hostname = matcher.group(2);
        return new ConnectionProperties(hostname.startsWith("[") ? hostname.substring(1, hostname.length() - 1) : hostname,
                Integer.parseInt(matcher.group(3)), matcher.group(4), username, queryProps);
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}

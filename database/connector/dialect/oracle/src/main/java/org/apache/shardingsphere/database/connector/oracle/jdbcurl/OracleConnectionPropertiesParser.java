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
    
    private static final int THIN_MATCH_GROUP_COUNT = 5;
    
    private static final Pattern THIN_URL_PATTERN = Pattern.compile("jdbc:oracle:(thin|oci|kprb):@(//)?([\\w\\-\\.]+):?(\\d*)[:/]([\\w\\-]+)", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CONNECT_DESCRIPTOR_URL_PATTERN = Pattern.compile(
            "jdbc:oracle:(thin|oci|kprb):@[(\\w\\s=)]+HOST\\s*=\\s*([\\w\\-\\.]+).*PORT\\s*=\\s*(\\d+).*SERVICE_NAME\\s*=\\s*(\\w+)\\)");
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        List<Matcher> matchers = Arrays.asList(THIN_URL_PATTERN.matcher(url), CONNECT_DESCRIPTOR_URL_PATTERN.matcher(url));
        Matcher matcher = matchers.stream().filter(Matcher::find).findAny().orElseThrow(() -> new UnrecognizedDatabaseURLException(url, THIN_URL_PATTERN.pattern()));
        int groupCount = matcher.groupCount();
        return THIN_MATCH_GROUP_COUNT == groupCount ? getThinConnectionProperties(username, matcher) : getStandardConnectionProperties(username, matcher);
    }
    
    private ConnectionProperties getThinConnectionProperties(final String username, final Matcher matcher) {
        return new ConnectionProperties(matcher.group(3), Strings.isNullOrEmpty(matcher.group(4)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(4)), matcher.group(5), username, new Properties());
    }
    
    private ConnectionProperties getStandardConnectionProperties(final String username, final Matcher matcher) {
        return new ConnectionProperties(matcher.group(2), Strings.isNullOrEmpty(matcher.group(3)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(3)), matcher.group(4), username, new Properties());
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}

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

package org.apache.shardingsphere.database.connector.h2.jdbcurl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connection properties parser of H2.
 */
public final class H2ConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final int DEFAULT_PORT = -1;
    
    private static final String DEFAULT_HOST_NAME = "";
    
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:h2:((?<modelMem>mem|~)[:/](?<catalog>[\\w\\-]+)|"
            + "(?<modelSslOrTcp>ssl:|tcp:)(//)?(?<hostname>[\\w\\-.]+)(:(?<port>\\d{1,4})/)?[/~\\w\\-.]+/(?<name>[\\-\\w]*)|"
            + "(?<modelFile>file:)[/~\\w\\-]+/(?<fileName>[\\-\\w]*));?\\S*", Pattern.CASE_INSENSITIVE);
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        Matcher matcher = URL_PATTERN.matcher(url);
        ShardingSpherePreconditions.checkState(matcher.find(), () -> new UnrecognizedDatabaseURLException(url, URL_PATTERN.pattern()));
        return new ConnectionProperties(getHostname(matcher), getPort(matcher), getCatalog(matcher), null, PropertiesBuilder.build(new Property("model", getModel(matcher))));
    }
    
    private String getHostname(final Matcher matcher) {
        String hostname = matcher.group("hostname");
        return null == hostname ? DEFAULT_HOST_NAME : hostname;
    }
    
    private int getPort(final Matcher matcher) {
        String port = matcher.group("port");
        return Strings.isNullOrEmpty(port) ? DEFAULT_PORT : Integer.parseInt(port);
    }
    
    private static String getCatalog(final Matcher matcher) {
        String name = matcher.group("name");
        if (null != name) {
            return name;
        }
        String fileName = matcher.group("fileName");
        if (null != fileName) {
            return fileName;
        }
        return matcher.group("catalog");
    }
    
    private static String getModel(final Matcher matcher) {
        String modelMem = matcher.group("modelMem");
        if (null != modelMem) {
            return modelMem;
        }
        String modelSslOrTcp = matcher.group("modelSslOrTcp");
        if (null != modelSslOrTcp) {
            return modelSslOrTcp;
        }
        return matcher.group("modelFile");
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}

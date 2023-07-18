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

package org.apache.shardingsphere.infra.database.sqlserver;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.url.UnrecognizedDatabaseURLException;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for SQLServer.
 */
@Getter
public final class SQLServerDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 1433;
    
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:(microsoft:)?sqlserver://([\\w\\-\\.]+):?(\\d*);\\S*(DatabaseName|database)=([\\w\\-\\.]+);?", Pattern.CASE_INSENSITIVE);
    
    private final String hostname;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    public SQLServerDataSourceMetaData(final String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new UnrecognizedDatabaseURLException(url, URL_PATTERN.pattern());
        }
        hostname = matcher.group(2);
        port = Strings.isNullOrEmpty(matcher.group(3)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(3));
        catalog = matcher.group(5);
        schema = null;
    }
    
    @Override
    public Properties getQueryProperties() {
        return new Properties();
    }
    
    @Override
    public Properties getDefaultQueryProperties() {
        return new Properties();
    }
}

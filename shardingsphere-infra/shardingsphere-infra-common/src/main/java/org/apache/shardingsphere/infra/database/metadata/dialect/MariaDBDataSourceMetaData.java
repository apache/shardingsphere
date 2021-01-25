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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for MariaDB.
 */
@Getter
public final class MariaDBDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 3306;
    
    private final String hostName;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Pattern pattern = Pattern.compile("jdbc:(mysql|mariadb)(:replication|:failover|:sequential|:aurora)?:(\\w*:)?//([\\w\\-\\.]+):?([0-9]*)/([\\w\\-]+);?\\S*", Pattern.CASE_INSENSITIVE);
    
    public MariaDBDataSourceMetaData(final String url) {
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            throw new UnrecognizedDatabaseURLException(url, pattern.pattern());
        }
        hostName = matcher.group(4);
        port = Strings.isNullOrEmpty(matcher.group(5)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(5));
        catalog = matcher.group(6);
        schema = null;
    }
}

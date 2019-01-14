/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.datasource.dialect;

import com.google.common.base.Strings;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for MySQL.
 *
 * @author panjuan
 */
@Getter
public final class MySQLDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 3306;
    
    private final String hostName;
    
    private final int port;
    
    private final String schemeName;
    
    private final Pattern pattern = Pattern.compile("jdbc:mysql:(\\w*:)?//([\\w\\-\\.]+):?([0-9]*)/([\\w\\-]+);?\\S*", Pattern.CASE_INSENSITIVE);
    
    public MySQLDataSourceMetaData(final String url) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            hostName = matcher.group(2);
            port = Strings.isNullOrEmpty(matcher.group(3)) ? DEFAULT_PORT : Integer.valueOf(matcher.group(3));
            schemeName = matcher.group(4);
        } else {
            throw new ShardingException("The URL of JDBC is not supported. Please refer to this pattern: %s.", pattern.pattern());
        }
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return hostName.equals(dataSourceMetaData.getHostName()) && port == dataSourceMetaData.getPort();
    }
}

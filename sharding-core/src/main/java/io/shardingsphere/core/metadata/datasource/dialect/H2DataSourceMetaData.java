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

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for H2.
 *
 * @author panjuan
 */
@Getter
public final class H2DataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = -1;
    
    private final String hostName;
    
    private final int port;
    
    private final String schemeName;
    
    private final Pattern pattern = Pattern.compile("jdbc:h2:(mem|~)[:/]([\\w\\-]+);?\\S*", Pattern.CASE_INSENSITIVE);
    
    public H2DataSourceMetaData(final String url) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            hostName = matcher.group(1);
            port = DEFAULT_PORT;
            schemeName = matcher.group(2);
        } else {
            throw new ShardingException("The URL of JDBC is not supported. Please refer to this pattern: %s.", pattern.pattern());
        }
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return hostName.equals(dataSourceMetaData.getHostName()) && port == dataSourceMetaData.getPort() && schemeName.equals(dataSourceMetaData.getSchemeName());
    }
}

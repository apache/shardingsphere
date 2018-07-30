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

import com.google.common.base.Splitter;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import lombok.Getter;

import java.net.URI;

/**
 * Data source meta data for H2.
 *
 * @author panjuan
 */
@Getter
public final class H2DataSourceMetaData implements DataSourceMetaData {
    
    private static final String DEFAULT_HOST = "localhost";
    
    private final String hostName;
    
    private final int port;
    
    private final String schemeName;
    
    public H2DataSourceMetaData(final String url) {
        URI uri = getURI(url);
        hostName = uri.getHost();
        port = uri.getPort();
        schemeName = uri.getPath().isEmpty() ? "" : uri.getPath().substring(1);
    }
    
    private URI getURI(final String url) {
        String cleanUrl = url.substring(5);
        if (cleanUrl.contains("h2:~")) {
            cleanUrl = Splitter.on(";").splitToList(cleanUrl).get(0);
            cleanUrl = cleanUrl.replace(":", "://").replace("~", DEFAULT_HOST);
        } else if (cleanUrl.contains("h2:mem")) {
            cleanUrl = Splitter.on(";").splitToList(cleanUrl).get(0);
            String[] parts = cleanUrl.split(":");
            if (3 == parts.length) {
                cleanUrl = parts[0] + "://" + parts[1] + "/" + parts[2];
            }
        } else {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        URI result = URI.create(cleanUrl);
        if (null == result.getHost()) {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        return result;
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return hostName.equals(dataSourceMetaData.getHostName()) && port == dataSourceMetaData.getPort() && schemeName.equals(dataSourceMetaData.getSchemeName());
    }
}

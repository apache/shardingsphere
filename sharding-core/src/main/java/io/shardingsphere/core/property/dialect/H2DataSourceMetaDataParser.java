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

package io.shardingsphere.core.property.dialect;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.property.DataSourceMetaData;
import io.shardingsphere.core.property.DataSourceMetaDataParser;

import java.net.URI;

/**
 * H2 data source meta data parser.
 *
 * @author panjuan
 */
public final class H2DataSourceMetaDataParser implements DataSourceMetaDataParser {
    
    private static final Integer DEFAULT_PORT = -1;
    
    private static final String DEFAULT_HOST = "localhost";
    
    @Override
    public DataSourceMetaData getDataSourceMetaData(final String url) {
        String cleanUrl = url.substring(5);
        if (cleanUrl.contains("h2:~")) {
            cleanUrl = cleanUrl.split(";")[0];
            cleanUrl = cleanUrl.replace(":", "://").replace("~", DEFAULT_HOST);
        } else if (cleanUrl.contains("h2:mem")) {
            cleanUrl = cleanUrl.split(";")[0];
            String[] parts = cleanUrl.split(":");
            if (3 == parts.length) {
                cleanUrl = parts[0] + "://" + parts[1] + "/" + parts[2];
            }
        } else {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        URI uri = URI.create(cleanUrl);
        if (null == uri.getHost()) {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        return new DataSourceMetaData(uri.getHost(), -1 == uri.getPort() ? DEFAULT_PORT : uri.getPort(), uri.getPath().isEmpty() ? "" : uri.getPath().substring(1));
    }
}

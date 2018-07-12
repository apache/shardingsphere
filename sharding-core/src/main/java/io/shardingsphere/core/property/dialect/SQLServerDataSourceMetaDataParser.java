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
 * SQLServer data source meta data parser.
 *
 * @author panjuan
 */
public final class SQLServerDataSourceMetaDataParser extends DataSourceMetaDataParser {
    
    private static final Integer DEFAULT_PORT = 1433;
    
    @Override
    protected DataSourceMetaData getDataSourceMetaData(final String url) {
        String cleanUrl = url.substring(5);
        cleanUrl = cleanUrl.replace("microsoft:", "").replace(";DatabaseName=", "/");
        URI uri = URI.create(cleanUrl);
        if (null == uri.getHost()) {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        return new DataSourceMetaData(uri.getHost(), -1 == uri.getPort() ? DEFAULT_PORT : uri.getPort(), uri.getPath().isEmpty() ? "" : uri.getPath().substring(1));
    }
}

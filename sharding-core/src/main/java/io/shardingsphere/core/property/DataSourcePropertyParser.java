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

package io.shardingsphere.core.property;

import io.shardingsphere.core.exception.ShardingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Data source parameter parser.
 *
 * @author panjuan
 */
public abstract class DataSourcePropertyParser {
    
    /**
     * Parse data source.
     *
     * @param dataSource data source
     * @return data source property
     */
    public DataSourceProperty parseDataSource(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return parseJDBCUrl(connection.getMetaData().getURL());
        } catch (SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    protected abstract DataSourceProperty parseJDBCUrl(String url);
}

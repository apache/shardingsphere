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

package io.shardingsphere.transaction.base.servicecomb.transport;

import lombok.RequiredArgsConstructor;
import org.apache.servicecomb.saga.core.TransportFailedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Data source map SQL transport.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class DataSourceMapSQLTransport extends AbstractSQLTransport {
    
    private final Map<String, DataSource> dataSourceMap;
    
    @Override
    protected Connection getConnection(final String datasource) throws TransportFailedException {
        try {
            return dataSourceMap.get(datasource).getConnection();
        } catch (SQLException ex) {
            throw new TransportFailedException("get connection of [" + datasource + "] occur exception ", ex);
        }
    }
}

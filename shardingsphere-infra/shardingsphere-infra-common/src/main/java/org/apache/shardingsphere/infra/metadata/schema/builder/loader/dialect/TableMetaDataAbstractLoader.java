/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class TableMetaDataAbstractLoader {
    
    /**
     * Load table meta data by executor service.
     * @param dataSourceTables data source table names map
     * @param executorService executor service
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    public Map<String, TableMetaData> load(final Map<DataSource, Collection<String>> dataSourceTables, final ExecutorService executorService) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (Map.Entry<DataSource, Collection<String>> each : dataSourceTables.entrySet()) {
            futures.add(executorService.submit(() -> ((DialectTableMetaDataLoader) this)
                    .loadWithTables(each.getKey(), each.getValue())));
        }
        try {
            for (Future<Map<String, TableMetaData>> each : futures) {
                result.putAll(each.get());
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result;
    }
}

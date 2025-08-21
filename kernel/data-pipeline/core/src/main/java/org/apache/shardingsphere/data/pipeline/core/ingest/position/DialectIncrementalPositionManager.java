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

package org.apache.shardingsphere.data.pipeline.core.ingest.position;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Dialect incremental position manager.
 */
@SingletonSPI
public interface DialectIncrementalPositionManager extends DatabaseTypedSPI {
    
    /**
     * Init position by string data.
     *
     * @param data string data
     * @return position
     */
    IngestPosition init(String data);
    
    /**
     * Init position by data source.
     *
     * @param dataSource data source
     * @param slotNameSuffix slot name suffix
     * @return position
     * @throws SQLException SQL exception
     */
    IngestPosition init(DataSource dataSource, String slotNameSuffix) throws SQLException;
    
    /**
     * Clean up by data source if necessary.
     *
     * @param dataSource data source
     * @param slotNameSuffix slot name suffix
     * @throws SQLException SQL exception
     */
    default void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
    }
}

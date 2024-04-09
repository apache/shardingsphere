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

package org.apache.shardingsphere.sharding.metadata.data.dialect;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Dialect sharding statistics table data collector.
 */
@SingletonSPI
public interface DialectShardingStatisticsTableCollector extends DatabaseTypedSPI {
    
    String TABLE_ROWS_COLUMN_NAME = "TABLE_ROWS";
    
    String DATA_LENGTH_COLUMN_NAME = "DATA_LENGTH";
    
    /**
     * Append dialect content into row.
     * 
     * @param connection connection
     * @param dataNode data node
     * @param row row to be appended
     * @return is appended or not
     * @throws SQLException SQL exception
     */
    boolean appendRow(Connection connection, DataNode dataNode, List<Object> row) throws SQLException;
}

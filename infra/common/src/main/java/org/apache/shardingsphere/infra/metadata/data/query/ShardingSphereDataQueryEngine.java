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

package org.apache.shardingsphere.infra.metadata.data.query;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * ShardingSphere data query engine.
 */
@SingletonSPI
public interface ShardingSphereDataQueryEngine extends RequiredSPI {
    
    /**
     * Init.
     * 
     * @param metaData meta data
     * @param databaseData database data
     */
    void init(ShardingSphereMetaData metaData, Map<String, ShardingSphereDatabaseData> databaseData);
    
    /**
     * Create connection.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return Connection
     * @throws SQLException sql exception
     */
    Connection createConnection(String databaseName, String schemaName) throws SQLException;
}

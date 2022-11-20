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

package org.apache.shardingsphere.infra.metadata.data.collector;

import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere data collector.
 */
@SingletonSPI
public interface ShardingSphereDataCollector extends TypedSPI {
    
    /**
     * Collect.
     *
     * @param databaseName database name
     * @param table table
     * @param shardingSphereDatabases ShardingSphere databases
     * @return ShardingSphere table data
     * @throws SQLException sql exception
     */
    Optional<ShardingSphereTableData> collect(String databaseName, ShardingSphereTable table, Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException;
}

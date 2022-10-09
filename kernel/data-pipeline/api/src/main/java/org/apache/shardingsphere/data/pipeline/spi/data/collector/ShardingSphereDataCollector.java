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

package org.apache.shardingsphere.data.pipeline.spi.data.collector;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * ShardingSphere data collector.
 */
@SingletonSPI
public interface ShardingSphereDataCollector extends TypedSPI {
    
    /**
     * Collect ShardingSphere data.
     * 
     * @param shardingSphereData ShardingSphere data
     * @param databaseName database name
     * @param rules rules
     * @param dataSources data sources
     * @param databaseType database type
     * @throws SQLException sql exception
     */
    void collectShardingSphereData(ShardingSphereData shardingSphereData, String databaseName, Collection<ShardingSphereRule> rules,
                                   Map<String, DataSource> dataSources, DatabaseType databaseType) throws SQLException;
}

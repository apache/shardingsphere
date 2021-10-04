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

package org.apache.shardingsphere.infra.metadata.schema.refresher;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere schema refresher.
 *
 * @param <T> type of SQL statement
 */
public interface SchemaRefresher<T extends SQLStatement> extends MetaDataRefresher {
    
    /**
     * Refresh ShardingSphere schema.
     *
     * @param schemaMetaData schema meta data
     * @param logicDataSourceNames route dataSource names
     * @param sqlStatement SQL statement
     * @param props configuration properties
     * @throws SQLException SQL exception
     */
    void refresh(ShardingSphereMetaData schemaMetaData, Collection<String> logicDataSourceNames, T sqlStatement, ConfigurationProperties props) throws SQLException;
}

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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.SQLException;

/**
 * Push down meta data refresher.
 *
 * @param <T> type of SQL statement
 */
@SingletonSPI
public interface PushDownMetaDataRefresher<T extends SQLStatement> extends TypedSPI {
    
    /**
     * Refresh schema.
     *
     * @param metaDataManagerPersistService meta data manager persist service
     * @param database database
     * @param logicDataSourceName route data source name
     * @param schemaName schema name
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param props configuration properties
     * @throws SQLException SQL exception
     */
    void refresh(MetaDataManagerPersistService metaDataManagerPersistService, ShardingSphereDatabase database, String logicDataSourceName, String schemaName,
                 DatabaseType databaseType, T sqlStatement, ConfigurationProperties props) throws SQLException;
    
    @Override
    Class<T> getType();
}

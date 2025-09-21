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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;

import java.util.Optional;

/**
 * Dialect pipeline native container drop table option.
 */
@SingletonSPI
public interface DialectPipelineNativeContainerDropTableOption extends DatabaseTypedSPI {
    
    /**
     * Get JDBC URL.
     *
     * @param storageContainerConnectOption data source environment
     * @param actualDatabasePort actual database port
     * @param databaseName database name
     * @return JDBC URL
     */
    String getJdbcUrl(StorageContainerConnectOption storageContainerConnectOption, int actualDatabasePort, String databaseName);
    
    /**
     * Get query all schema and table mapper SQL.
     *
     * @param databaseName database name
     * @return query all schema and table mapper SQL
     */
    String getQueryAllSchemaAndTableMapperSQL(String databaseName);
    
    /**
     * Get drop schema SQL.
     *
     * @return drop schema SQL
     */
    Optional<String> getDropSchemaSQL();
}

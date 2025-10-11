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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived.dialect;

import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived.DialectPipelineNativeContainerDropTableOption;

import java.util.Optional;

/**
 * Pipeline native container drop table option for MariaDB.
 */
public final class MariaDBPipelineNativeContainerDropTableOption implements DialectPipelineNativeContainerDropTableOption {
    
    private final DialectPipelineNativeContainerDropTableOption delegate = new MySQLPipelineNativeContainerDropTableOption();
    
    @Override
    public String getJdbcUrl(final StorageContainerConnectOption storageContainerConnectOption, final int actualDatabasePort, final String databaseName) {
        return delegate.getJdbcUrl(storageContainerConnectOption, actualDatabasePort, databaseName);
    }
    
    @Override
    public String getQueryAllSchemaAndTableMapperSQL(final String databaseName) {
        return delegate.getQueryAllSchemaAndTableMapperSQL(databaseName);
    }
    
    @Override
    public Optional<String> getDropSchemaSQL() {
        return delegate.getDropSchemaSQL();
    }
    
    @Override
    public String getDatabaseType() {
        return "MariaDB";
    }
}

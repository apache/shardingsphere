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

import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived.DialectPipelineNativeContainerDropTableOption;

import java.util.Optional;

/**
 * Pipeline native container drop table option for Oracle.
 */
public final class OraclePipelineNativeContainerDropTableOption implements DialectPipelineNativeContainerDropTableOption {
    
    @Override
    public String getJdbcUrl(final DataSourceEnvironment dataSourceEnvironment, final int actualDatabasePort, final String databaseName) {
        return dataSourceEnvironment.getURL("localhost", actualDatabasePort, "");
    }
    
    @Override
    public String getQueryAllSchemaAndTableMapperSQL(final String databaseName) {
        return String.format("SELECT OWNER, TABLE_NAME FROM ALL_TABLES WHERE OWNER='%s'", databaseName);
    }
    
    @Override
    public Optional<String> getDropSchemaSQL() {
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}

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

package org.apache.shardingsphere.test.integration.env.database.initialization.type;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.database.initialization.DatabaseSQLInitialization;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Database SQL initialization for MySQL.
 */
public final class MySQLDatabaseSQLInitialization extends DefaultDatabaseSQLInitialization implements DatabaseSQLInitialization {
    
    @Override
    public void executeInitSQLs(final String scenario, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws IOException, SQLException {
        super.executeInitSQLs(scenario, databaseType, dataSourceMap);
    }
    
    @Override
    public String getType() {
        return new MySQLDatabaseType().getName();
    }
}

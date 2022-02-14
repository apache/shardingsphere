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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

/**
 * H2 container.
 */
public final class H2Container extends StorageContainer {
    
    public H2Container(final String scenario) {
        super(DatabaseTypeRegistry.getActualDatabaseType("H2"), "h2:fake", true, scenario);
    }
    
    @Override
    @SneakyThrows({IOException.class, SQLException.class})
    protected void execute() {
        File file = new File(EnvironmentPath.getInitSQLFile(getDatabaseType(), getScenario()));
        for (Entry<String, DataSource> each : getActualDataSourceMap().entrySet()) {
            String databaseFileName = "init-" + each.getKey() + ".sql";
            boolean sqlFileExist = EnvironmentPath.checkSQLFileExist(getDatabaseType(), getScenario(), databaseFileName);
            try (Connection connection = each.getValue().getConnection(); FileReader reader = new FileReader(file)) {
                RunScript.execute(connection, reader);
                if (sqlFileExist) {
                    executeDatabaseFile(getDatabaseType(), connection, databaseFileName);
                }
            }
        }
    }
    
    private void executeDatabaseFile(final DatabaseType databaseType, final Connection connection, final String databaseFileName) throws IOException, SQLException {
        File databaseFile = new File(EnvironmentPath.getInitSQLFile(databaseType, getScenario(), databaseFileName));
        try (FileReader databaseFileReader = new FileReader(databaseFile)) {
            RunScript.execute(connection, databaseFileReader);
        }
    }
    
    @Override
    public boolean isHealthy() {
        return true;
    }
    
    @Override
    protected String getUsername() {
        return "sa";
    }
    
    @Override
    protected String getPassword() {
        return "";
    }
    
    @Override
    protected int getPort() {
        return 0;
    }
}

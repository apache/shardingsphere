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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.EmbeddedStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * H2 container.
 */
public final class H2Container extends EmbeddedStorageContainer {
    
    private final ScenarioDataPath scenarioDataPath;
    
    public H2Container(final String scenario) {
        super(TypedSPILoader.getService(DatabaseType.class, "H2"), scenario);
        scenarioDataPath = new ScenarioDataPath(scenario);
    }
    
    @Override
    @SneakyThrows({IOException.class, SQLException.class})
    public void start() {
        fillActualDataSet();
        fillExpectedDataSet();
    }
    
    private void fillActualDataSet() throws SQLException, IOException {
        for (Entry<String, DataSource> entry : getActualDataSourceMap().entrySet()) {
            executeInitSQL(entry.getValue(), scenarioDataPath.getInitSQLFile(Type.ACTUAL, getDatabaseType()));
            Optional<String> dbInitSQLFile = scenarioDataPath.findActualDatabaseInitSQLFile(entry.getKey(), getDatabaseType());
            if (dbInitSQLFile.isPresent()) {
                executeInitSQL(entry.getValue(), dbInitSQLFile.get());
            }
        }
    }
    
    private void fillExpectedDataSet() throws SQLException, IOException {
        for (Entry<String, DataSource> entry : getExpectedDataSourceMap().entrySet()) {
            executeInitSQL(entry.getValue(), scenarioDataPath.getInitSQLFile(Type.EXPECTED, getDatabaseType()));
        }
    }
    
    private void executeInitSQL(final DataSource dataSource, final String initSQLFile) throws SQLException, IOException {
        try (
                Connection connection = dataSource.getConnection();
                FileReader reader = new FileReader(initSQLFile)) {
            RunScript.execute(connection, reader);
        }
    }
}

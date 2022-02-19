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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.scenario.ScenarioPath;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.EmbeddedStorageContainer;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

/**
 * H2 container.
 */
public final class H2Container extends EmbeddedStorageContainer {
    
    private final ScenarioPath scenarioPath;
    
    public H2Container(final String scenario) {
        super(DatabaseTypeRegistry.getActualDatabaseType("H2"), scenario);
        scenarioPath = new ScenarioPath(scenario);
    }
    
    @Override
    @SneakyThrows({IOException.class, SQLException.class})
    public void start() {
        for (Entry<String, DataSource> entry : getActualDataSourceMap().entrySet()) {
            for (String each : scenarioPath.getInitSQLFiles(entry.getKey(), getDatabaseType())) {
                executeInitSQL(entry.getValue(), each);
            }
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

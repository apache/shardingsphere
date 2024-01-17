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

package org.apache.shardingsphere.test.e2e.env.runtime.scenario.database;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Database environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    /**
     * Get database types.
     *
     * @param scenario scenario
     * @param defaultDatabaseType default database type
     * @return database types
     */
    public static Map<String, DatabaseType> getDatabaseTypes(final String scenario, final DatabaseType defaultDatabaseType) {
        Collection<String> datasourceNames = unmarshal(new ScenarioDataPath(scenario).getDatabasesFile(Type.ACTUAL)).getDatabases();
        return crateDatabaseTypes(datasourceNames, defaultDatabaseType);
    }
    
    private static Map<String, DatabaseType> crateDatabaseTypes(final Collection<String> datasourceNames, final DatabaseType defaultDatabaseType) {
        Map<String, DatabaseType> result = new LinkedHashMap<>();
        for (String each : datasourceNames) {
            List<String> items = Splitter.on(":").splitToList(each);
            DatabaseType databaseType = items.size() > 1 ? TypedSPILoader.getService(DatabaseType.class, items.get(1)) : defaultDatabaseType;
            result.put(items.get(0), databaseType);
        }
        return result;
    }
    
    /**
     * Get expected database types.
     *
     * @param scenario scenario
     * @param defaultDatabaseType default database type
     * @return expected database types
     */
    public static Map<String, DatabaseType> getExpectedDatabaseTypes(final String scenario, final DatabaseType defaultDatabaseType) {
        return crateDatabaseTypes(unmarshal(new ScenarioDataPath(scenario).getDatabasesFile(Type.EXPECTED)).getDatabases(), defaultDatabaseType);
    }
    
    @SneakyThrows({IOException.class, JAXBException.class})
    private static DatabaseNameEnvironment unmarshal(final String databasesFile) {
        try (FileReader reader = new FileReader(databasesFile)) {
            return (DatabaseNameEnvironment) JAXBContext.newInstance(DatabaseNameEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
}

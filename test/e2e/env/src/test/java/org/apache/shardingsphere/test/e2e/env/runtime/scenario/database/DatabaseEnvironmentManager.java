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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

/**
 * Database environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    /**
     * Get database names.
     *
     * @param scenario scenario
     * @return database names
     */
    public static Collection<String> getDatabaseNames(final String scenario) {
        return unmarshal(new ScenarioDataPath(scenario).getDatabasesFile(Type.ACTUAL)).getDatabases();
    }
    
    /**
     * Get expected database names.
     *
     * @param scenario scenario
     * @return expected database names
     */
    public static Collection<String> getExpectedDatabaseNames(final String scenario) {
        return unmarshal(new ScenarioDataPath(scenario).getDatabasesFile(Type.EXPECTED)).getDatabases();
    }
    
    @SneakyThrows({IOException.class, JAXBException.class})
    private static DatabaseNameEnvironment unmarshal(final String databasesFile) {
        try (FileReader reader = new FileReader(databasesFile)) {
            return (DatabaseNameEnvironment) JAXBContext.newInstance(DatabaseNameEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
}

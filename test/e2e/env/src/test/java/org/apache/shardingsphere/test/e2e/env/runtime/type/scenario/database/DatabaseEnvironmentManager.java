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

package org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Database environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    /**
     * Get database types.
     *
     * @param scenario scenario
     * @param type type
     * @return database types
     */
    public static Collection<String> getDatabaseNames(final String scenario, final Type type) {
        if (null == scenario) {
            return Collections.emptyList();
        }
        return unmarshal(new ScenarioDataPath(scenario, type).getDatabasesFile()).getDatabases().stream().map(DatabaseEnvironmentAttribute::getName).collect(Collectors.toList());
    }
    
    @SneakyThrows({IOException.class, JAXBException.class})
    private static DatabaseEnvironment unmarshal(final String databasesFile) {
        try (FileReader reader = new FileReader(databasesFile)) {
            return (DatabaseEnvironment) JAXBContext.newInstance(DatabaseEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
}

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

package org.apache.shardingsphere.test.integration.env.scenario.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.env.scenario.ScenarioPath;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    private static final String DEFAULT_VERIFICATION_DATA_SOURCE_NAME = "verification_dataset";
    
    /**
     * Get database names.
     *
     * @param scenario scenario
     * @return database names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getDatabaseNames(final String scenario) throws IOException, JAXBException {
        return unmarshal(new ScenarioPath(scenario).getDatabasesFile()).getDatabases();
    }
    
    /**
     * Get verification database names.
     *
     * @param scenario scenario
     * @return verification database names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getVerificationDatabaseNames(final String scenario) throws IOException, JAXBException {
        Optional<String> verificationDatabasesFile = new ScenarioPath(scenario).getVerificationDatabasesFile();
        return verificationDatabasesFile.isPresent() ? unmarshal(verificationDatabasesFile.get()).getDatabases() : Collections.singleton(DEFAULT_VERIFICATION_DATA_SOURCE_NAME);
    }
    
    private static DatabaseNameEnvironment unmarshal(final String databasesFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(databasesFile)) {
            return (DatabaseNameEnvironment) JAXBContext.newInstance(DatabaseNameEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
}

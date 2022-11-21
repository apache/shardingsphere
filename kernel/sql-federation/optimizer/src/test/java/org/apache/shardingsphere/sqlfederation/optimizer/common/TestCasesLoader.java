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

package org.apache.shardingsphere.sqlfederation.optimizer.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;

/**
 * Integration test cases loader.
 */
public final class TestCasesLoader {
    
    private static final TestCasesLoader INSTANCE = new TestCasesLoader();
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static TestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Read a case file and generate a case object.
     *
     * @return collection of test cases
     * @throws IOException exception for read file.
     * @throws JAXBException exception for parse xml file.
     */
    public Collection<TestCase> generate() throws IOException, JAXBException {
        URL url = Objects.requireNonNull(TestCasesLoader.class.getClassLoader().getResource("cases/federation-query-sql-cases.xml"));
        try (FileReader reader = new FileReader(url.getFile())) {
            TestCases testCases = (TestCases) JAXBContext.newInstance(TestCases.class).createUnmarshaller().unmarshal(reader);
            return testCases.getTestCases();
        }
    }
}

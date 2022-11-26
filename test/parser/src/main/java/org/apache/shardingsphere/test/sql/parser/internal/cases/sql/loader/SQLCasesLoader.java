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

package org.apache.shardingsphere.test.sql.parser.internal.cases.sql.loader;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.sql.parser.internal.cases.CaseFileLoader;
import org.apache.shardingsphere.test.sql.parser.internal.cases.sql.jaxb.RootSQLCases;
import org.apache.shardingsphere.test.sql.parser.internal.cases.sql.jaxb.SQLCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * SQL cases loader.
 */
public final class SQLCasesLoader {
    
    /**
     * Load SQL cases.
     * 
     * @param rootDirectory root directory
     * @return loaded cases
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public Map<String, SQLCase> load(final String rootDirectory) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(rootDirectory, file) : loadFromDirectory(rootDirectory);
    }
    
    private Map<String, SQLCase> loadFromJar(final String rootDirectory, final File file) throws JAXBException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (String each : CaseFileLoader.loadFileNamesFromJar(file, rootDirectory)) {
            buildCaseMap(result, SQLCasesLoader.class.getClassLoader().getResourceAsStream(each));
        }
        return result;
    }
    
    private Map<String, SQLCase> loadFromDirectory(final String rootDirectory) throws JAXBException, FileNotFoundException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (File each : CaseFileLoader.loadFilesFromDirectory(rootDirectory)) {
            buildCaseMap(result, new FileInputStream(each));
        }
        return result;
    }
    
    private void buildCaseMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        RootSQLCases root = (RootSQLCases) JAXBContext.newInstance(RootSQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : root.getSqlCases()) {
            if (null == each.getDatabaseTypes()) {
                each.setDatabaseTypes(root.getDatabaseTypes());
            }
            Preconditions.checkState(!sqlCaseMap.containsKey(each.getId()), "Find duplicated SQL Case ID: %s", each.getId());
            sqlCaseMap.put(each.getId(), each);
        }
    }
}

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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.loader;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.jaxb.RootSQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.jaxb.SQLCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseFileLoader;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseLoaderCallback;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;

/**
 * SQL case loader callback.
 */
public final class SQLCaseLoaderCallback implements CaseLoaderCallback<SQLCase> {
    
    @Override
    public Map<String, SQLCase> loadFromJar(final File jarFile, final String rootDirectory) throws JAXBException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (String each : CaseFileLoader.loadFileNamesFromJar(jarFile, rootDirectory)) {
            buildCaseMap(result, Thread.currentThread().getContextClassLoader().getResourceAsStream(each));
        }
        return result;
    }
    
    @Override
    public Map<String, SQLCase> loadFromDirectory(final String rootDirectory) throws JAXBException, IOException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (File each : CaseFileLoader.loadFilesFromDirectory(rootDirectory)) {
            buildCaseMap(result, Files.newInputStream(each.toPath()));
        }
        return result;
    }
    
    private void buildCaseMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        RootSQLCases root = (RootSQLCases) JAXBContext.newInstance(RootSQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : root.getSqlCases()) {
            Preconditions.checkState(!sqlCaseMap.containsKey(each.getId()), "Find duplicated SQL Case ID: %s.", each.getId());
            sqlCaseMap.put(each.getId(), each);
        }
    }
}

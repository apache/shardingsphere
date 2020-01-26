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

package org.apache.shardingsphere.sql.parser.integrate.jaxb;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL parser test cases registry.
 *
 * @author zhangliang
 */
public final class SQLParserTestCasesRegistry {
    
    private Map<String, SQLParserTestCase> sqlParserTestCases;
    
    public SQLParserTestCasesRegistry(final String rootDirectory) {
        sqlParserTestCases = load(rootDirectory);
    }
    
    private Map<String, SQLParserTestCase> load(final String directory) {
        URL url = SQLParserTestCasesRegistry.class.getClassLoader().getResource(directory);
        Preconditions.checkNotNull(url, "Can not find SQL parser test cases.");
        File[] files = new File(url.getPath()).listFiles();
        Preconditions.checkNotNull(files, "Can not find SQL parser test cases.");
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : files) {
            result.putAll(load(each));
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> load(final File file) {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File each : files) {
                    result.putAll(load(each));
                }
            }
        } else {
            result.putAll(getSQLParserTestCases(file));
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> getSQLParserTestCases(final File file) {
        SQLParserTestCases expectedSQLStatements;
        try {
            expectedSQLStatements = (SQLParserTestCases) JAXBContext.newInstance(SQLParserTestCases.class).createUnmarshaller().unmarshal(file);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        final Map<String, SQLParserTestCase> result = new HashMap<>(expectedSQLStatements.getParserResults().size(), 1);
        for (SQLParserTestCase each : expectedSQLStatements.getParserResults()) {
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
    
    /**
     * Get SQL parser test case.
     * 
     * @param sqlCaseId SQL case ID
     * @return SQL parser test case
     */
    public SQLParserTestCase get(final String sqlCaseId) {
        Preconditions.checkState(sqlParserTestCases.containsKey(sqlCaseId), "Can not find SQL of id: %s", sqlCaseId);
        return sqlParserTestCases.get(sqlCaseId);
    }
    
    /**
     * Count all SQL parser test cases.
     *
     * @return count of all test cases
     */
    public int countAllSQLParserTestCases() {
        return sqlParserTestCases.size();
    }
}

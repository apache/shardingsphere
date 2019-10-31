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

package org.apache.shardingsphere.core.parse.integrate.jaxb;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResultSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser result set registry.
 *
 * @author zhangliang
 */
public final class ParserResultSetRegistry {
    
    private Map<String, ParserResult> parserResultMap;
    
    public ParserResultSetRegistry(final String rootDirectory) {
        parserResultMap = load(rootDirectory);
    }
    
    private Map<String, ParserResult> load(final String directory) {
        URL url = ParserResultSetRegistry.class.getClassLoader().getResource(directory);
        Preconditions.checkNotNull(url, "Cannot find parse test cases.");
        File[] files = new File(url.getPath()).listFiles();
        Preconditions.checkNotNull(files, "Cannot find parse test cases.");
        Map<String, ParserResult> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : files) {
            result.putAll(load(each));
        }
        return result;
    }
    
    private Map<String, ParserResult> load(final File file) {
        Map<String, ParserResult> result = new HashMap<>(Short.MAX_VALUE, 1);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File each : files) {
                    result.putAll(load(each));
                }
            }
        } else {
            result.putAll(getParserResults(file));
        }
        return result;
    }
    
    private Map<String, ParserResult> getParserResults(final File file) {
        ParserResultSet resultSet;
        try {
            resultSet = (ParserResultSet) JAXBContext.newInstance(ParserResultSet.class).createUnmarshaller().unmarshal(file);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        final Map<String, ParserResult> result = new HashMap<>(resultSet.getParserResults().size(), 1);
        for (ParserResult each : resultSet.getParserResults()) {
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
    
    /**
     * Get parser result.
     * 
     * @param sqlCaseId SQL case ID
     * @return parser result
     */
    public ParserResult get(final String sqlCaseId) {
        Preconditions.checkState(parserResultMap.containsKey(sqlCaseId), "Can't find SQL of id: %s", sqlCaseId);
        return parserResultMap.get(sqlCaseId);
    }
    
    /**
     * Count all test cases.
     *
     * @return count of all test cases
     */
    public int countAllTestCases() {
        return parserResultMap.size();
    }
}

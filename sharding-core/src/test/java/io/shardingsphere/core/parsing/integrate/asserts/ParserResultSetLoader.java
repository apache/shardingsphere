/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.integrate.asserts;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.parsing.integrate.jaxb.root.ParserResult;
import io.shardingsphere.core.parsing.integrate.jaxb.root.ParserResultSet;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser result set loader.
 *
 * @author zhangliang
 */
public class ParserResultSetLoader {
    
    private static final ParserResultSetLoader INSTANCE = new ParserResultSetLoader();
    
    protected Map<String, ParserResult> parserResultMap;
    
    protected ParserResultSetLoader() {
        parserResultMap = loadParserResultSet("parser/");
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static ParserResultSetLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Switch result.
     * 
     * @param dirName dir name
     */
    public void switchResult(final String dirName) {
        parserResultMap = loadParserResultSet(dirName);
    }
    
    protected Map<String, ParserResult> loadParserResultSet(final String dirName) {
        URL url = ParserResultSetLoader.class.getClassLoader().getResource(dirName);
        Preconditions.checkNotNull(url, "Cannot found parser test cases.");
        File[] files = new File(url.getPath()).listFiles();
        Preconditions.checkNotNull(files, "Cannot found parser test cases.");
        Map<String, ParserResult> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : files) {
            result.putAll(loadParserResultSet(each));
        }
        return result;
    }
    
    @SneakyThrows
    protected Map<String, ParserResult> loadParserResultSet(final File file) {
        Map<String, ParserResult> result = new HashMap<>(Short.MAX_VALUE, 1);
        try {
            if (file.isDirectory()) {
                for (File each : file.listFiles()) {
                    result.putAll(loadParserResultSet(each));
                } 
            } else {
                ParserResultSet resultSet = (ParserResultSet) JAXBContext.newInstance(ParserResultSet.class).createUnmarshaller().unmarshal(file);
                for (ParserResult each : resultSet.getParserResults()) {
                    if (null != resultSet.getNamespace()) {
                        result.put(resultSet.getNamespace() + "." + each.getSqlCaseId(), each);
                    } else {
                        result.put(each.getSqlCaseId(), each);
                    }
                }
            }
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
    
    /**
     * Get parser assert.
     * 
     * @param sqlCaseId SQL case ID
     * @return parser assert
     */
    public ParserResult getParserResult(final String sqlCaseId) {
        Preconditions.checkState(parserResultMap.containsKey(sqlCaseId), "Can't find SQL of id: %s", sqlCaseId);
        return parserResultMap.get(sqlCaseId);
    }
    
    /**
     * Count all parser test cases.
     *
     * @return count of all parser test cases
     */
    public int countAllParserTestCases() {
        return parserResultMap.size();
    }
}

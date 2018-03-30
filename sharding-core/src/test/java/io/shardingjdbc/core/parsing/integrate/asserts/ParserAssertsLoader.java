/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.integrate.asserts;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAsserts;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser asserts loader.
 *
 * @author zhangliang
 */
public final class ParserAssertsLoader {
    
    private static final ParserAssertsLoader INSTANCE = new ParserAssertsLoader();
    
    private final Map<String, ParserAssert> parserAssertMap;
    
    private ParserAssertsLoader() {
        parserAssertMap = loadParserAsserts();
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static ParserAssertsLoader getInstance() {
        return INSTANCE;
    }
    
    private Map<String, ParserAssert> loadParserAsserts() {
        URL url = ParserAssertsLoader.class.getClassLoader().getResource("parser/");
        Preconditions.checkNotNull(url, "Cannot found parser test cases.");
        File[] files = new File(url.getPath()).listFiles();
        Preconditions.checkNotNull(files, "Cannot found parser test cases.");
        Map<String, ParserAssert> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : files) {
            result.putAll(loadParserAsserts(each));
        }
        return result;
    }
    
    private Map<String, ParserAssert> loadParserAsserts(final File file) {
        Map<String, ParserAssert> result = new HashMap<>(Short.MAX_VALUE, 1);
        try {
            for (ParserAssert each : ((ParserAsserts) JAXBContext.newInstance(ParserAsserts.class).createUnmarshaller().unmarshal(file)).getParserAsserts()) {
                result.put(each.getSqlCaseId(), each);
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
    public ParserAssert getParserAssert(final String sqlCaseId) {
        Preconditions.checkState(parserAssertMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        return parserAssertMap.get(sqlCaseId);
    }
}

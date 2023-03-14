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

package org.apache.shardingsphere.test.it.sql.parser.internal.loader;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Case loader callback.
 * 
 * @param <T> type of test case
 */
public interface CaseLoaderCallback<T> {
    
    /**
     * Load test cases from jar.
     * 
     * @param jarFile jar file
     * @param rootDirectory root directory of test cases
     * @return loaded test cases
     * @throws JAXBException JAXB exception
     */
    Map<String, T> loadFromJar(File jarFile, String rootDirectory) throws JAXBException;
    
    /**
     * Load test cases from directory.
     * 
     * @param rootDirectory root directory of test cases
     * @return loaded test cases
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    Map<String, T> loadFromDirectory(String rootDirectory) throws IOException, JAXBException;
}

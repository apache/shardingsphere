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

package org.apache.shardingsphere.test.sql.parser.internal.engine.loader;

import lombok.SneakyThrows;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Cases loader template.
 * 
 * @param <T> type of test case
 */
public abstract class CasesLoaderTemplate<T> {
    
    /**
     * Load test cases.
     * 
     * @param rootDirectory root directory
     * @return loaded cases
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public Map<String, T> load(final String rootDirectory) {
        File file = new File(CasesLoaderTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(rootDirectory, file) : loadFromDirectory(rootDirectory);
    }
    
    protected abstract Map<String, T> loadFromJar(String rootDirectory, File file) throws JAXBException;
    
    protected abstract Map<String, T> loadFromDirectory(String rootDirectory) throws IOException, JAXBException;
}

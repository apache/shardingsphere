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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Case loader template.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaseLoaderTemplate {
    
    /**
     * Load test cases.
     * 
     * @param rootDirectory root directory
     * @param callback callback of cases loader
     * @param <T> type of test case
     * @return loaded cases
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public static <T> Map<String, T> load(final String rootDirectory, final CaseLoaderCallback<T> callback) {
        File file = new File(CaseLoaderTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? callback.loadFromJar(file, rootDirectory) : callback.loadFromDirectory(rootDirectory);
    }
}

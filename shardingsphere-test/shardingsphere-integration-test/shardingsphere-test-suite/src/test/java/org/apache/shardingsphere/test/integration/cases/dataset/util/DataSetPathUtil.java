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

package org.apache.shardingsphere.test.integration.cases.dataset.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Data set path util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSetPathUtil {
    
    private static final String DATA_SET_FOLDER_NAME = "dataset";
    
    /**
     * Load data set.
     * 
     * @param expectedDataFile expected data file
     * @return data set
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public static DataSet loadDataSet(final String expectedDataFile) {
        try (FileReader reader = new FileReader(expectedDataFile)) {
            return (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Get data set absolute path.
     * 
     * @param parentPath parent path of data set file
     * @param ruleType rule type
     * @param databaseType database type
     * @param dataSetFile name of data set file
     * @return data set absolute path
     */
    public static String getDataSetPath(final String parentPath, final String ruleType, final DatabaseType databaseType, final String dataSetFile) {
        String result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, ruleType, databaseType.getName().toLowerCase(), dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, ruleType, dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        throw new IllegalArgumentException(String.format("%s not found, path=%s, ruleType=%s, databaseType=%s", dataSetFile, parentPath, ruleType, databaseType.getName()));
    }
}

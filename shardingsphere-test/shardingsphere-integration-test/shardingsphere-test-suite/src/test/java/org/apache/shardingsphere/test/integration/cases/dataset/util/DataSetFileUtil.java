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
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.io.File;

/**
 * Data set file util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSetFileUtil {
    
    private static final String DATA_SET_FOLDER_NAME = "dataset";
    
    /**
     * Get data set file.
     * 
     * @param parentPath parent path of data set file
     * @param ruleType rule type
     * @param databaseType database type
     * @param dataSetFileName data set file name
     * @return data set file
     */
    public static String getDataSetFile(final String parentPath, final String ruleType, final DatabaseType databaseType, final String dataSetFileName) {
        String result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, ruleType, databaseType.getName().toLowerCase(), dataSetFileName);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, ruleType, dataSetFileName);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, dataSetFileName);
        if (new File(result).exists()) {
            return result;
        }
        throw new IllegalArgumentException(String.format("%s not found, path=%s, ruleType=%s, databaseType=%s", dataSetFileName, parentPath, ruleType, databaseType.getName()));
    }
}

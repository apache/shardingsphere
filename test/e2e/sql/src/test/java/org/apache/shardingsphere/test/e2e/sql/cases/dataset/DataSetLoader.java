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

package org.apache.shardingsphere.test.e2e.sql.cases.dataset;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Data set loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSetLoader {
    
    private static final String DATA_SET_FOLDER_NAME = "dataset";
    
    /**
     * Load data set.
     *
     * @param parentPath parent path of data set file
     * @param scenario scenario
     * @param databaseType database type
     * @param mode mode
     * @param dataSetFile name of data set file
     * @return data set
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public static DataSet load(final String parentPath, final String scenario, final DatabaseType databaseType, final Mode mode, final String dataSetFile) {
        try (FileReader reader = new FileReader(getFile(parentPath, scenario, databaseType, mode, dataSetFile))) {
            return (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private static String getFile(final String parentPath, final String scenario, final DatabaseType databaseType, final Mode mode, final String dataSetFile) {
        String result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, scenario, mode.name().toLowerCase(), databaseType.getType().toLowerCase(), dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, scenario, databaseType.getType().toLowerCase(), dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        if (databaseType.getTrunkDatabaseType().isPresent()) {
            result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, scenario, databaseType.getTrunkDatabaseType().get().getType().toLowerCase(), dataSetFile);
            if (new File(result).exists()) {
                return result;
            }
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, scenario, mode.name().toLowerCase(), dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, scenario, dataSetFile);
        if (new File(result).exists()) {
            return result;
        }
        result = String.join(File.separator, parentPath, DATA_SET_FOLDER_NAME, dataSetFile);
        Preconditions.checkArgument(new File(result).exists(), "%s not found, path=%s, scenario=%s, databaseType=%s, mode=%s", dataSetFile, parentPath, scenario, databaseType.getType(), mode);
        return result;
    }
}

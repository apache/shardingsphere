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

package org.apache.shardingsphere.driver;

import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// TODO move to new acceptance test module with mode
public abstract class AbstractYamlDataSourceTest {
    
    @BeforeClass
    public static void createSchema() throws SQLException {
        for (String each : getSchemaFiles()) {
            RunScript.execute(
                    createDataSource(getFileName(each)).getConnection(), new InputStreamReader(Objects.requireNonNull(AbstractYamlDataSourceTest.class.getClassLoader().getResourceAsStream(each))));
        }
    }
    
    protected static DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL", dataSourceName));
        result.setUsername("sa");
        result.setMaximumPoolSize(100);
        return result;
    }
    
    private static String getFileName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf('.')) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    private static List<String> getSchemaFiles() {
        return Arrays.asList("yaml/schema/sharding/db0.sql",
                "yaml/schema/sharding/db1.sql",
                "yaml/schema/readwrite_splitting/write_ds.sql",
                "yaml/schema/readwrite_splitting/read_ds_0.sql",
                "yaml/schema/readwrite_splitting/read_ds_1.sql",
                "yaml/schema/sharding_readwrite_splitting/write_ds_0.sql",
                "yaml/schema/sharding_readwrite_splitting/write_ds_1.sql",
                "yaml/schema/sharding_readwrite_splitting/read_ds_0.sql",
                "yaml/schema/sharding_readwrite_splitting/read_ds_1.sql");
    }
}

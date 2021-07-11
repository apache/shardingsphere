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

package org.apache.shardingsphere.infra.executor.sql.federate;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.H2DataSourceMetaData;

import javax.sql.DataSource;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractSQLFederationTest {
    
    static final Map<String, DataSource> ACTUAL_DATA_SOURCES = new HashMap<>();

    static final Map<String, DataSourceMetaData> DATA_SOURCE_META_DATAS = new HashMap<>();
    
    private static final String INIT_FEDERATE_DATABASE_0 = "sql/jdbc_init_federate_0.sql";
    
    @BeforeClass
    public static synchronized void initializeDataSource() throws SQLException {
        createDataSources("federate_jdbc_0", INIT_FEDERATE_DATABASE_0);
    }
    
    private static void createDataSources(final String dataSourceName, final String initSql) throws SQLException {
        ACTUAL_DATA_SOURCES.put(dataSourceName, DataSourceBuilder.build(dataSourceName));
        DATA_SOURCE_META_DATAS.put(dataSourceName, new H2DataSourceMetaData(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName)));
        initializeSchema(dataSourceName, initSql);
    }
    
    private static void initializeSchema(final String dataSourceName, final String initSql) throws SQLException {
        try (Connection conn = ACTUAL_DATA_SOURCES.get(dataSourceName).getConnection()) {
            RunScript.execute(conn, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream(initSql))));
        }
    }
    
    protected static Map<String, DataSource> getActualDataSources() {
        return ACTUAL_DATA_SOURCES;
    }
}

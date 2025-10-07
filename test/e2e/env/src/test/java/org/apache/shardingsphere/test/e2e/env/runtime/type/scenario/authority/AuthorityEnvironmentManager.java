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

package org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.authority;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Authority environment manager.
 */
public final class AuthorityEnvironmentManager implements AutoCloseable {
    
    private final Collection<DataSource> dataSources;
    
    private final DatabaseType databaseType;
    
    private final AuthorityEnvironment env;
    
    public AuthorityEnvironmentManager(final String path, final Collection<DataSource> dataSources, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        this.dataSources = dataSources;
        this.databaseType = databaseType;
        try (FileReader reader = new FileReader(path)) {
            env = (AuthorityEnvironment) JAXBContext.newInstance(AuthorityEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
        init();
    }
    
    private void init() throws SQLException {
        Collection<String> initSQLs = env.getInitSQLs(databaseType);
        if (initSQLs.isEmpty()) {
            return;
        }
        for (DataSource each : dataSources) {
            execute(each, initSQLs);
        }
    }
    
    @Override
    public void close() throws SQLException {
        Collection<String> cleanSQLs = env.getCleanSQLs(databaseType);
        if (cleanSQLs.isEmpty()) {
            return;
        }
        for (DataSource each : dataSources) {
            execute(each, cleanSQLs);
        }
    }
    
    private void execute(final DataSource dataSource, final Collection<String> sqls) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.addBatch(each);
            }
            statement.executeBatch();
        }
    }
}

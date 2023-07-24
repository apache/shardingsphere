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

package org.apache.shardingsphere.test.e2e.env.runtime.scenario.authority;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

/**
 * Authority environment manager.
 */
@Slf4j
public final class AuthorityEnvironmentManager implements AutoCloseable {
    
    private final Map<String, DataSource> instanceDataSourceMap;
    
    private final DatabaseType databaseType;
    
    private final AuthorityEnvironment authorityEnvironment;
    
    public AuthorityEnvironmentManager(final String path, final Map<String, DataSource> instanceDataSourceMap, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        this.instanceDataSourceMap = instanceDataSourceMap;
        this.databaseType = databaseType;
        try (FileReader reader = new FileReader(path)) {
            authorityEnvironment = (AuthorityEnvironment) JAXBContext.newInstance(AuthorityEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
        init();
    }
    
    private void init() throws SQLException {
        Collection<String> initSQLs = authorityEnvironment.getInitSQLs(databaseType);
        if (initSQLs.isEmpty()) {
            return;
        }
        for (DataSource each : instanceDataSourceMap.values()) {
            executeOnInstanceDataSource(each, initSQLs);
        }
    }
    
    @Override
    public void close() throws SQLException {
        Collection<String> cleanSQLs = authorityEnvironment.getCleanSQLs(databaseType);
        if (cleanSQLs.isEmpty()) {
            return;
        }
        for (DataSource each : instanceDataSourceMap.values()) {
            executeOnInstanceDataSource(each, cleanSQLs);
        }
    }
    
    private void executeOnInstanceDataSource(final DataSource dataSource, final Collection<String> sqls) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            for (String each : sqls) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(each);
                } catch (final SQLException ex) {
                    log.error("execute '{}' failed.", each, ex);
                }
            }
        }
    }
}

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

package org.apache.shardingsphere.test.integration.engine.it.dcl;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.MemorizedDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.it.SingleIT;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.authority.AuthorityEnvironmentManager;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public abstract class BaseDCLIT extends SingleIT {
    
    private final AuthorityEnvironmentManager authorityEnvironmentManager;
    
    protected BaseDCLIT(final String parentPath, final IntegrationTestCaseAssertion assertion, final String adapter, final String scenario,
                        final DatabaseType databaseType, final SQLExecuteType sqlExecuteType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, adapter, scenario, databaseType, sqlExecuteType, sql);
        authorityEnvironmentManager = new AuthorityEnvironmentManager(
                EnvironmentPath.getAuthorityFile(scenario), null == getActualDataSources() ? null : createInstanceDataSourceMap(), databaseType);
    }
    
    private Map<String, DataSource> createInstanceDataSourceMap() throws SQLException {
        return "shadow".equals(getScenario()) ? getActualDataSources() : getShardingInstanceDataSourceMap();
    }
    
    private Map<String, DataSource> getShardingInstanceDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new LinkedHashMap<>(getActualDataSources().size(), 1);
        Map<String, DataSourceMetaData> dataSourceMetaDataMap = getDataSourceMetaDataMap();
        for (Entry<String, DataSource> entry : getActualDataSources().entrySet()) {
            if (!isExisted(entry.getKey(), result.keySet(), dataSourceMetaDataMap)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, DataSourceMetaData> getDataSourceMetaDataMap() throws SQLException {
        Map<String, DataSourceMetaData> result = new LinkedHashMap<>(getActualDataSources().size(), 1);
        for (Entry<String, DataSource> entry : getActualDataSources().entrySet()) {
            try (Connection connection = entry.getValue().getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                result.put(entry.getKey(), getDatabaseType().getDataSourceMetaData(metaData.getURL(), metaData.getUserName()));
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames, final Map<String, DataSourceMetaData> dataSourceMetaDataMap) {
        DataSourceMetaData sample = dataSourceMetaDataMap.get(dataSourceName);
        for (String each : existedDataSourceNames) {
            if (isInSameDatabaseInstance(sample, dataSourceMetaDataMap.get(each))) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isInSameDatabaseInstance(final DataSourceMetaData sample, final DataSourceMetaData target) {
        return sample instanceof MemorizedDataSourceMetaData
                ? (Objects.equals(target.getSchema(), sample.getSchema())) : target.getHostName().equals(sample.getHostName()) && target.getPort() == sample.getPort();
    }
    
    @Before
    public final void insertData() throws SQLException {
        authorityEnvironmentManager.initialize();
    }
    
    @After
    public final void cleanData() throws SQLException {
        authorityEnvironmentManager.clean();
    }
}

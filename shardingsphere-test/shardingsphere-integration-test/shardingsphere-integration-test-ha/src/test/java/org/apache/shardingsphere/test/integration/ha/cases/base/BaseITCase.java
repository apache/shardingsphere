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

package org.apache.shardingsphere.test.integration.ha.cases.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.ha.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.integration.ha.framework.parameter.HAParameterized;

import javax.sql.DataSource;
import java.util.List;

/**
 * Base integration test.
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    private final String databaseName;
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private List<DataSource> exposedDataSources;
    
    private List<DataSource> mappedDataSources;
    
    private DataSource proxyDataSource;
    
    public BaseITCase(final HAParameterized haParameterized) {
        databaseType = haParameterized.getDatabaseType();
        containerComposer = new DockerContainerComposer(haParameterized.getScenario(), haParameterized.getAdapterContainerImage(), haParameterized.getDatabaseType(), haParameterized.getStorageContainerImage());
        containerComposer.start();
        databaseName = (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) ? "postgres" : "";
        initStorageDataSources();
        initProxyDataSource();
    }
    
    private void initProxyDataSource() {
        proxyDataSource = containerComposer.getProxyDatasource(databaseName);
    }
    
    private void initStorageDataSources() {
        exposedDataSources = containerComposer.getExposedDatasource(databaseName);
        mappedDataSources = containerComposer.getMappedDatasource(databaseName);
    }
}

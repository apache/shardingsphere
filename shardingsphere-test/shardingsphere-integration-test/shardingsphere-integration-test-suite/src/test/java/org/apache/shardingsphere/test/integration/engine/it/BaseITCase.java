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

package org.apache.shardingsphere.test.integration.engine.it;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.integration.junit.compose.ContainerCompose;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.runner.ShardingSphereRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.text.ParseException;
import java.util.TimeZone;

@Getter(AccessLevel.PROTECTED)
@RunWith(ShardingSphereRunner.class)
public abstract class BaseITCase {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    @Rule
    public final ContainerCompose compose;
    
    private final String adapter;
    
    private final String scenario;
    
    private final DatabaseType databaseType;
    
    private final SQLCommandType sqlCommandType;
    
    private final IntegrationTestCase integrationTestCase;
    
    private final ShardingSphereStorageContainer storageContainer;
    
    private final ShardingSphereAdapterContainer adapterContainer;
    
    private DataSource targetDataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    BaseITCase(final ParameterizedArray parameterizedArray) {
        this.adapter = parameterizedArray.getAdapter();
        this.compose = parameterizedArray.getCompose();
        this.scenario = parameterizedArray.getScenario();
        this.databaseType = parameterizedArray.getDatabaseType();
        this.sqlCommandType = parameterizedArray.getSqlCommandType();
        this.storageContainer = compose.getStorageContainer();
        this.adapterContainer = compose.getAdapterContainer();
        this.integrationTestCase = parameterizedArray.getTestCaseContext().getTestCase();
    }
    
    @Before
    public final void createDataSource() {
        targetDataSource = compose.getDataSourceMap().get("adapterForWriter");
    }
    
    @After
    public final void tearDown() {
        if (targetDataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) targetDataSource).getMetaDataContexts().getExecutorEngine().close();
        }
    }
    
    protected abstract String getSQL() throws ParseException;
    
}

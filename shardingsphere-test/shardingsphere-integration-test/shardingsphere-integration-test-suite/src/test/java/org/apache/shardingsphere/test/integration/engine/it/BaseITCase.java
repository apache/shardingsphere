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
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.env.EnvironmentType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.junit.annotation.ContainerInitializer;
import org.apache.shardingsphere.test.integration.junit.annotation.ContainerType;
import org.apache.shardingsphere.test.integration.junit.annotation.OnContainer;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITInject;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.runner.ShardingSphereRunner;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
@RunWith(ShardingSphereRunner.class)
public abstract class BaseITCase {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    @OnContainer(name = "adapter")
    private ShardingSphereAdapterContainer proxy;
    
    @OnContainer(name = "storage", type = ContainerType.STORAGE, hostName = "mysql.db.host")
    private ShardingSphereStorageContainer storage;
    
    @ShardingSphereITInject
    private IntegrationTestCaseAssertion assertion;
    
    @ShardingSphereITInject
    private DataSetEnvironmentManager dataSetEnvironmentManager;
    
    @ShardingSphereITInject
    private String sql;
   
    @ShardingSphereITInject
    private TestCaseDescription description;
    
    @ShardingSphereITInject
    private SQLExecuteType sqlExecuteType;
    
    @ShardingSphereITInject
    private String parentPath;
    
    private DataSource targetDataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    @ContainerInitializer
    protected final void initialize() {
        if (Objects.nonNull(proxy) && Objects.nonNull(storage)) {
            proxy.dependsOn(storage);
        }
    }
    
    @BeforeClass
    public static void executeInitSQLs() throws IOException, JAXBException, SQLException {
        if (EnvironmentType.DOCKER != IntegrationTestEnvironment.getInstance().getEnvType()) {
            DatabaseEnvironmentManager.executeInitSQLs();
        }
    }
    
    @Before
    public final void createDataSource() {
        targetDataSource = proxy.getDataSource();
    }
    
    protected final String getSQL() throws ParseException {
        return sqlExecuteType == SQLExecuteType.Literal ? getLiteralSQL(sql) : sql;
    }
    
    protected final String getLiteralSQL(final String sql) throws ParseException {
        List<Object> parameters = null == assertion ? Collections.emptyList() : assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        return parameters.isEmpty() ? sql : String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
    
    @After
    public final void tearDown() {
        if (targetDataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) targetDataSource).getMetaDataContexts().getExecutorEngine().close();
        }
    }
}

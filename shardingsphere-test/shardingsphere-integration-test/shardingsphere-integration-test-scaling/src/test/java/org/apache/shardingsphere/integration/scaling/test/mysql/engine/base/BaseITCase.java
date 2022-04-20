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

package org.apache.shardingsphere.integration.scaling.test.mysql.engine.base;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.scaling.test.mysql.container.ComposedContainer;
import org.junit.AfterClass;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.Map;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    private DataSource targetDataSource;
    
    @Getter(AccessLevel.NONE)
    private final ComposedContainer composedContainer;
    
    private Map<String, DataSource> actualDataSourceMap;
    
    private Map<String, DataSource> expectedDataSourceMap;
    
    private String databaseNetworkAlias;
    
    public BaseITCase(final DatabaseType databaseType) {
        composedContainer = new ComposedContainer(databaseType);
    }
    
    @Before
    public void setUp() {
        composedContainer.start();
        targetDataSource = composedContainer.getTargetDataSource();
        actualDataSourceMap = composedContainer.getActualDataSourceMap();
        expectedDataSourceMap = composedContainer.getExpectedDataSourceMap();
        databaseNetworkAlias = composedContainer.getDatabaseNetworkAlias();
    }
    
    @AfterClass
    public static void closeContainers() {
        
    }
}

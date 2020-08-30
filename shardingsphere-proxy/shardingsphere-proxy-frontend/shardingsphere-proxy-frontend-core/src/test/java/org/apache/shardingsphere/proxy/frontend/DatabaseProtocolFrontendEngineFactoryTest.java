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

package org.apache.shardingsphere.proxy.frontend;

import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseProtocolFrontendEngineFactoryTest {
    
    @Before
    public void init() throws Exception {
        Method method = ShardingSphereServiceLoader.class.getDeclaredMethod("registerServiceClass", Class.class, Object.class);
        method.setAccessible(true);
        method.invoke(null, DatabaseProtocolFrontendEngine.class, new MockDatabaseProtocolFrontendEngine());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertNewInstanceWhenUnsupported() {
        DatabaseProtocolFrontendEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("Oracle"));
    }
    
    @Test
    public void assertNewInstanceMysql() {
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = DatabaseProtocolFrontendEngineFactory.newInstance(new MySQLDatabaseType());
        Assert.assertNotNull(databaseProtocolFrontendEngine);
    }
}

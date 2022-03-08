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

package org.apache.shardingsphere.infra.metadata.resource;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereResourceTest {
    
    @Test
    public void assertClose() throws SQLException, IOException, InterruptedException {
        MockedDataSource dataSource = new MockedDataSource();
        new ShardingSphereResource(Collections.singletonMap("foo_ds", dataSource), mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class), mock(DatabaseType.class)).close(dataSource);
        while (null == dataSource.getClosed() || !dataSource.getClosed()) {
            Thread.sleep(10L);
        }
        assertTrue(dataSource.getClosed());
    }
}

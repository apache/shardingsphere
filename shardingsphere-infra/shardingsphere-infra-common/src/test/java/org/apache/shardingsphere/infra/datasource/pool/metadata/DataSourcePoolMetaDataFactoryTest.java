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

package org.apache.shardingsphere.infra.datasource.pool.metadata;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.pool.metadata.impl.DefaultDataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.impl.HikariDataSourcePoolMetaData;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourcePoolMetaDataFactoryTest {
    
    @Test
    public void assertNewInstance() {
        DataSourcePoolMetaData defaultDataSourcePoolMetaData = DataSourcePoolMetaDataFactory.newInstance("");
        assertTrue(Objects.nonNull(defaultDataSourcePoolMetaData));
        assertThat(defaultDataSourcePoolMetaData, instanceOf(DefaultDataSourcePoolMetaData.class));
        DataSourcePoolMetaData hikariDataSourcePoolMetaData = DataSourcePoolMetaDataFactory.newInstance(HikariDataSource.class.getCanonicalName());
        assertTrue(Objects.nonNull(hikariDataSourcePoolMetaData));
        assertThat(hikariDataSourcePoolMetaData, instanceOf(HikariDataSourcePoolMetaData.class));
    }
}

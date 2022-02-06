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

package org.apache.shardingsphere.infra.datasource.pool.metadata.fixture;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourceJdbcUrlMetaData;
import org.apache.shardingsphere.test.mock.MockedDataSource;

import java.util.Properties;

public final class MockedDataSourceJdbcUrlMetaData implements DataSourceJdbcUrlMetaData<MockedDataSource> {
    
    @Override
    public String getJdbcUrl(final MockedDataSource targetDataSource) {
        return targetDataSource.getUrl();
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return null;
    }
    
    @Override
    public Properties getJdbcUrlProperties(final MockedDataSource targetDataSource) {
        return new Properties();
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final MockedDataSource targetDataSource) {
    }
}

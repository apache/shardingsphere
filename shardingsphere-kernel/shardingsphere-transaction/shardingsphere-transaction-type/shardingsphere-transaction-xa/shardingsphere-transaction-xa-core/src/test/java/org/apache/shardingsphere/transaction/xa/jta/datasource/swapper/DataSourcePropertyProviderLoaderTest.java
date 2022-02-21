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

package org.apache.shardingsphere.transaction.xa.jta.datasource.swapper;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.DefaultDataSourcePropertyProvider;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.HikariCPPropertyProvider;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class DataSourcePropertyProviderLoaderTest {
    
    @Test
    public void assertGetProviderByDefault() {
        assertThat(DataSourcePropertyProviderLoader.getProvider(new BasicDataSource()), instanceOf(DefaultDataSourcePropertyProvider.class));
    }
    
    @Test
    public void assertGetProviderBySPI() {
        assertThat(DataSourcePropertyProviderLoader.getProvider(new HikariDataSource()), instanceOf(HikariCPPropertyProvider.class));
    }
}

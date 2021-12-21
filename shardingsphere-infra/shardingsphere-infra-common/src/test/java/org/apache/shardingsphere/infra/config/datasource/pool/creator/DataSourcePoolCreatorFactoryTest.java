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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.impl.DefaultDataSourcePoolCreator;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.impl.HikariDataSourcePoolCreator;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class DataSourcePoolCreatorFactoryTest {
    
    @Test
    public void assertGetInstanceWhenDataSourcePoolCreatorExisted() {
        assertThat(DataSourcePoolCreatorFactory.getInstance(HikariDataSource.class.getName()), instanceOf(HikariDataSourcePoolCreator.class));
    }
    
    @Test
    public void assertGetInstanceWhenDataSourcePoolCreatorNotExisted() {
        assertThat(DataSourcePoolCreatorFactory.getInstance("NOT_EXISTED_DATA_SOURCE"), instanceOf(DefaultDataSourcePoolCreator.class));
    }
}

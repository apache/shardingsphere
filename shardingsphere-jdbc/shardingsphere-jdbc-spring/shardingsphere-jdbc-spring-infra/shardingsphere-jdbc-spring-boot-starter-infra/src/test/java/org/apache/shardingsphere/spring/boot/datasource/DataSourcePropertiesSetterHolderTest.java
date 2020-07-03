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

package org.apache.shardingsphere.spring.boot.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.spring.boot.datasource.prop.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.datasource.prop.impl.DataSourcePropertiesSetterHolder;
import org.apache.shardingsphere.spring.boot.datasource.prop.impl.HikariDataSourcePropertiesSetter;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourcePropertiesSetterHolderTest {
    
    @Test
    public void assertGetDataSourcePropertiesSetterByType() {
        Optional<DataSourcePropertiesSetter> actual = DataSourcePropertiesSetterHolder.getDataSourcePropertiesSetterByType(HikariDataSource.class.getName());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(HikariDataSourcePropertiesSetter.class));
    }
}

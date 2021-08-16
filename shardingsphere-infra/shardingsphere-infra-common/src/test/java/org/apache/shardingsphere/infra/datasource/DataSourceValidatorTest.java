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

package org.apache.shardingsphere.infra.datasource;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataSourceValidatorTest {

    @Test
    public void assertValidate() {
        DataSourceValidator dataSourceValidator = new DataSourceValidator();
        DataSourceConfiguration dataSourceConfiguration = mock(DataSourceConfiguration.class);
        when(dataSourceConfiguration.createDataSource()).thenReturn(mock(DataSource.class));
        assertThat(dataSourceValidator.validate(dataSourceConfiguration), is(Boolean.TRUE));
        when(dataSourceConfiguration.createDataSource()).thenReturn(null);
        assertThat(dataSourceValidator.validate(dataSourceConfiguration), is(Boolean.TRUE));
        when(dataSourceConfiguration.createDataSource()).thenThrow(new RuntimeException());
        assertThat(dataSourceValidator.validate(dataSourceConfiguration), is(Boolean.FALSE));
    }
}

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

package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.dialect.SQL92DataSourceMetaData;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQL92DatabaseTypeTest {
    
    @Test
    public void assertGetName() {
        assertThat(new SQL92DatabaseType().getName(), is("SQL92"));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new SQL92DatabaseType().getJdbcUrlPrefixes(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:h2:mem:primary_ds_0", "sa"), instanceOf(SQL92DataSourceMetaData.class));
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:mariadb://localhost:3306/demo_ds_0", "sa"), instanceOf(SQL92DataSourceMetaData.class));
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:mysql://127.0.0.1/demo_ds_0", "root"), instanceOf(SQL92DataSourceMetaData.class));
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:postgresql://localhost:5432/demo_ds_0", "postgres"), instanceOf(SQL92DataSourceMetaData.class));
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:oracle:oci:@127.0.0.1/demo_ds_0", "scott"), instanceOf(SQL92DataSourceMetaData.class));
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=ds_0", "sa"), instanceOf(SQL92DataSourceMetaData.class));
    }
}

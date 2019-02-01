/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.datasource.dialect;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLDataSourceMetaDataTest {
    
    @Test
    public void assertGetALLProperties() {
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(9999));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test
    public void assertGetALLPropertiesWithDefaultPort() {
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1/ds_0?serverTimezone=UTC&useSSL=false");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test
    public void assertGetPropertiesWithMinus() {
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql://host-0:3306/ds-0?serverTimezone=UTC&useSSL=false");
        assertThat(actual.getHostName(), is("host-0"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getSchemeName(), is("ds-0"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetALLPropertiesFailure() {
        new MySQLDataSourceMetaData("jdbc:mysql:xxxxxxxx");
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        MySQLDataSourceMetaData target = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:3306/ds_0");
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1/ds_0?serverTimezone=UTC&useSSL=false");
        assertThat(actual.isInSameDatabaseInstance(target), is(true));
    }
}


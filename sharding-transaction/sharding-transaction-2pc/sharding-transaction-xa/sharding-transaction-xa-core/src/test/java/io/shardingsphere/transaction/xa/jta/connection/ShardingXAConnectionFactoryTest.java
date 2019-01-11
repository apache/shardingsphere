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

package io.shardingsphere.transaction.xa.jta.connection;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ShardingXAConnectionFactoryTest {
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private Connection connection;
    
    @Test(expected = ShardingException.class)
    public void assertCreateMysqlMySQLShardingXAConnection() {
        ShardingXAConnectionFactory.createShardingXAConnection(DatabaseType.MySQL, "ds1", xaDataSource, connection);
    }
    
    @Test
    @SneakyThrows
    public void assertCreateH2ShardingXAConnection() {
        ShardingXAConnection actual = ShardingXAConnectionFactory.createShardingXAConnection(DatabaseType.H2, "ds1", xaDataSource, connection);
        assertThat(actual.getResourceName(), is("ds1"));
    }
}

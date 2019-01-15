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

package io.shardingsphere.transaction.xa.jta;

import io.shardingsphere.transaction.xa.jta.connection.SingleXAConnectionTest;
import io.shardingsphere.transaction.xa.jta.connection.XAConnectionFactoryTest;
import io.shardingsphere.transaction.xa.jta.connection.dialect.MySQLXAConnectionWrapperTest;
import io.shardingsphere.transaction.xa.jta.datasource.SingleXADataSourceTest;
import io.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactoryTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.XAPropertiesFactoryTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.dialect.H2XAPropertiesTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MySQLXAPropertiesTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.dialect.OracleXAPropertiesTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.dialect.PostgreSQLXAPropertiesTest;
import io.shardingsphere.transaction.xa.jta.datasource.properties.dialect.SQLServerXAPropertiesTest;
import io.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourcePropertyProviderLoaderTest;
import io.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapperTest;
import io.shardingsphere.transaction.xa.jta.datasource.swapper.impl.DefaultDataSourcePropertyProviderTest;
import io.shardingsphere.transaction.xa.jta.resource.SingleXAResourceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SingleXAResourceTest.class,
        XAPropertiesFactoryTest.class, 
        SingleXADataSourceTest.class,
        XADataSourceFactoryTest.class, 
        DataSourcePropertyProviderLoaderTest.class, 
        DataSourceSwapperTest.class,
        DefaultDataSourcePropertyProviderTest.class, 
        H2XAPropertiesTest.class, 
        MySQLXAPropertiesTest.class, 
        PostgreSQLXAPropertiesTest.class, 
        OracleXAPropertiesTest.class, 
        SQLServerXAPropertiesTest.class, 
        XAConnectionFactoryTest.class, 
        SingleXAConnectionTest.class,
        MySQLXAConnectionWrapperTest.class
})
public final class AllJTATests {
}

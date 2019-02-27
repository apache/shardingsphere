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

package org.apache.shardingsphere.transaction.xa.jta;

import org.apache.shardingsphere.transaction.xa.jta.connection.SingleXAConnectionTest;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionFactoryTest;
import org.apache.shardingsphere.transaction.xa.jta.connection.dialect.MySQLXAConnectionWrapperTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.SingleXADataSourceTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactoryTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XAPropertiesFactoryTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.H2XAPropertiesTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MySQLXAPropertiesTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.OracleXAPropertiesTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.PostgreSQLXAPropertiesTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.SQLServerXAPropertiesTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourcePropertyProviderLoaderTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapperTest;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.DefaultDataSourcePropertyProviderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
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

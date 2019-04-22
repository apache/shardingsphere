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

package org.apache.shardingsphere.core.metadata;

import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactoryTest;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaDataTest;
import org.apache.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaDataTest;
import org.apache.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaDataTest;
import org.apache.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaDataTest;
import org.apache.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaDataTest;
import org.apache.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaDataTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        H2DataSourceMetaDataTest.class, 
        MySQLDataSourceMetaDataTest.class, 
        OracleDataSourceMetaDataTest.class, 
        PostgreSQLDataSourceMetaDataTest.class, 
        SQLServerDataSourceMetaDataTest.class, 
        DataSourceMetaDataFactoryTest.class, 
        ShardingDataSourceMetaDataTest.class
})
public final class AllMetaDataTests {
}

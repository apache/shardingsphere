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

package io.shardingsphere.core.metadata;

import io.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactoryTest;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaDataTest;
import io.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaDataTest;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaDataTest;
import io.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaDataTest;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaDataTest;
import io.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaDataTest;
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

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

package io.shardingsphere.transaction.xa.manager;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.transaction.xa.convert.dialect.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.extractor.DataSourceParameterFactory;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;

import javax.sql.DataSource;

public final class AtomikosTransactionManagerRecoveryTest extends TransactionManagerRecoveryTest {
    
    @Override
    protected DataSource createXADataSource(final String dsName) {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.H2, dsName);
        return getAtomikosTransactionManager().wrapDataSource(XADataSourceFactory.build(DatabaseType.H2), dsName, DataSourceParameterFactory.build(dataSource));
    }
}

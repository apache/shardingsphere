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

package org.apache.shardingsphere.integration.data.pipline.container.compose;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.container.proxy.ShardingSphereProxyLocalContainer;
import org.apache.shardingsphere.integration.data.pipline.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;

import javax.sql.DataSource;

/**
 * Local composed container.
 */
public final class LocalComposedContainer extends BaseComposedContainer {
    
    private ShardingSphereProxyLocalContainer shardingSphereProxyContainer;
    
    public LocalComposedContainer(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @SneakyThrows
    @Override
    public void start() {
        super.start();
        shardingSphereProxyContainer = new ShardingSphereProxyLocalContainer(getDatabaseContainer().getDatabaseType());
        shardingSphereProxyContainer.start();
    }
    
    @Override
    public DataSource getProxyDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseContainer().getDatabaseType()));
        String jdbcUrl = DataSourceEnvironment.getURL(getDatabaseContainer().getDatabaseType(), "localhost", 3307, databaseName);
        if (DatabaseTypeUtil.isMySQL(getDatabaseContainer().getDatabaseType())) {
            jdbcUrl = StringUtils.appendIfMissing(jdbcUrl, "&rewriteBatchedStatements=true");
        }
        result.setJdbcUrl(jdbcUrl);
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}

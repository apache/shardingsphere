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

package io.shardingsphere.transaction.xa.jta.datasource.swapper.impl;

import io.shardingsphere.core.config.DatabaseAccessConfiguration;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.util.ReflectiveUtil;
import io.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper;

import javax.sql.DataSource;

/**
 * Default swapper.
 *
 * @author zhangliang
 */
public final class DefaultDataSourceSwapper implements DataSourceSwapper {
    
    @Override
    public Class getDataSourceClass() {
        return null;
    }
    
    @Override
    public DatabaseAccessConfiguration swap(final DataSource dataSource) {
        try {
            String url = (String) ReflectiveUtil.findMethod(dataSource, "getUrl").invoke(dataSource);
            String username = (String) ReflectiveUtil.findMethod(dataSource, "getUsername").invoke(dataSource);
            String password = (String) ReflectiveUtil.findMethod(dataSource, "getPassword").invoke(dataSource);
            return new DatabaseAccessConfiguration(url, username, password);
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException("Cannot swap data source type: `%s`", dataSource.getClass().getName());
        } 
    }
}

/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.dataSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.ShardingRule;

public class DataSourceUtil {

    /**
     * Generating DataSource from yaml.
     * @param path path
     * @return DataSource
     * @throws IOException IOException
     * @throws SQLException SQLException
     */
    public static DataSource getDataSource(final String path) throws IOException, SQLException {
        return ShardingDataSourceFactory.createDataSource(new File(path));
    }

    /**
     * According to the sub DataSource set of shardingDataSource.
     * @param shardingDataSource shardingDataSource
     * @return DataSource map
     * @throws NoSuchFieldException NoSuchFieldException
     * @throws SecurityException SecurityException
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     */
    public static Map<String, DataSource> getDataSourceMap(final ShardingDataSource shardingDataSource)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ShardingContext shardingContext = getShardingContext(shardingDataSource);
        return shardingContext.getDataSourceMap();
    }

    /**
     * According to ShardingRule in shardingDataSource.
     * @param shardingDataSource shardingDataSource
     * @return ShardingRule
     * @throws NoSuchFieldException NoSuchFieldException
     * @throws SecurityException SecurityException
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     */
    public static ShardingRule getShardingRule(final ShardingDataSource shardingDataSource)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ShardingContext shardingContext = getShardingContext(shardingDataSource);
        return shardingContext.getShardingRule();
    }

    /**
     * According to ShardingContext in shardingDataSource.
     * @param shardingDataSource shardingDataSource
     * @return ShardingContext
     * @throws NoSuchFieldException NoSuchFieldException
     * @throws SecurityException SecurityException
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     */
    public static ShardingContext getShardingContext(final ShardingDataSource shardingDataSource)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = shardingDataSource.getClass().getDeclaredField("shardingContext");
        field.setAccessible(true);
        return (ShardingContext) field.get(shardingDataSource);
    }

}

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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.check.AbstractDataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

/**
 * MySQL data consistency checker.
 */
public final class MySQLDataConsistencyChecker extends AbstractDataConsistencyChecker implements DataConsistencyChecker {
    
    public MySQLDataConsistencyChecker(final ShardingScalingJob shardingScalingJob) {
        super(shardingScalingJob);
    }
    
    @Override
    public boolean dataCheck() {
        return getShardingScalingJob().getSyncConfigurations().stream().allMatch(each -> dataValid(each.getDumperConfiguration().getTableNameMap()));
    }
    
    private boolean dataValid(final Map<String, String> tableNameMap) {
        return distinctByValue(tableNameMap).entrySet().stream().allMatch(entry -> getColumns(entry.getKey()).stream().allMatch(columnValid(entry.getValue())));
    }
    
    private Map<String, String> distinctByValue(final Map<String, String> tableNameMap) {
        Map<String, String> result = new HashMap<>();
        Set<String> set = new HashSet<>();
        for (Entry<String, String> entry : tableNameMap.entrySet()) {
            if (set.add(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private List<String> getColumns(final String tableName) {
        List<String> result = new ArrayList<>();
        try (Connection connection = getSourceDataSource().getConnection();
             ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, tableName, "%")) {
            while (resultSet.next()) {
                result.add(resultSet.getString(4));
            }
        } catch (SQLException ex) {
            throw new DataCheckFailException("get columns failed.", ex);
        }
        return result;
    }
    
    private Predicate<? super String> columnValid(final String tableName) {
        return each -> sumCrc32(getSourceDataSource(), tableName, each) == sumCrc32(getDestinationDataSource(), tableName, each);
    }
    
    private long sumCrc32(final DataSource dataSource, final String tableName, final String column) {
        String sql = getSqlBuilder().buildSumCrc32SQL(tableName, column);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new DataCheckFailException(String.format("execute %s failed.", sql), ex);
        }
    }
    
    @Override
    protected MySQLSqlBuilder getSqlBuilder() {
        return new MySQLSqlBuilder();
    }
}

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

package org.apache.shardingsphere.scaling.core.job.check.consistency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract data consistency checker.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractDataConsistencyChecker implements DataConsistencyChecker {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private final JobContext jobContext;
    
    @Override
    public Map<String, DataConsistencyCheckResult> countCheck() {
        return jobContext.getTaskConfigs()
                .stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet())
                .stream().collect(Collectors.toMap(Function.identity(), this::countCheck, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private DataConsistencyCheckResult countCheck(final String table) {
        try (DataSourceWrapper sourceDataSource = getSourceDataSource();
             DataSourceWrapper targetDataSource = getTargetDataSource()) {
            long sourceCount = count(sourceDataSource, table);
            long targetCount = count(targetDataSource, table);
            return new DataConsistencyCheckResult(sourceCount, targetCount);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s count check failed.", table), ex);
        }
    }
    
    private long count(final DataSource dataSource, final String table) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getSqlBuilder().buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s count failed.", table), ex);
        }
    }
    
    protected DataSourceWrapper getSourceDataSource() {
        return dataSourceFactory.newInstance(jobContext.getJobConfig().getRuleConfig().getSource().unwrap());
    }
    
    protected DataSourceWrapper getTargetDataSource() {
        return dataSourceFactory.newInstance(jobContext.getJobConfig().getRuleConfig().getTarget().unwrap());
    }
    
    protected abstract ScalingSQLBuilder getSqlBuilder();
}

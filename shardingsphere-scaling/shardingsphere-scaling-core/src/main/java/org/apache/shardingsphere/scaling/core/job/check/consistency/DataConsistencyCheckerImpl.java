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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilderFactory;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Data consistency checker implementation.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DataConsistencyCheckerImpl implements DataConsistencyChecker {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private final JobContext jobContext;
    
    @Override
    public final Map<String, DataConsistencyCheckResult> countCheck() {
        return jobContext.getTaskConfigs()
                .stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet())
                .stream().collect(Collectors.toMap(Function.identity(), this::countCheck, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private DataConsistencyCheckResult countCheck(final String table) {
        ScalingDataSourceConfiguration sourceConfig = jobContext.getJobConfig().getRuleConfig().getSource().unwrap();
        ScalingDataSourceConfiguration targetConfig = jobContext.getJobConfig().getRuleConfig().getTarget().unwrap();
        try (DataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceConfig);
             DataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetConfig)) {
            long sourceCount = count(sourceDataSource, table, sourceConfig.getDatabaseType());
            long targetCount = count(targetDataSource, table, targetConfig.getDatabaseType());
            return new DataConsistencyCheckResult(sourceCount, targetCount);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s count check failed.", table), ex);
        }
    }
    
    private long count(final DataSource dataSource, final String table, final DatabaseType databaseType) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ScalingSQLBuilderFactory.newInstance(databaseType.getName()).buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s count failed.", table), ex);
        }
    }
    
    @Override
    public Map<String, Boolean> dataCheck() {
        //TODO
        return Collections.emptyMap();
    }
}

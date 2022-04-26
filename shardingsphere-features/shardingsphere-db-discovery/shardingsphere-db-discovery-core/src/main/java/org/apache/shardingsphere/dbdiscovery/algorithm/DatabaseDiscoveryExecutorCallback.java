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

package org.apache.shardingsphere.dbdiscovery.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DatabaseDiscoveryExecutorCallback implements ExecutorCallback<DataSource, String> {

    public static final String DATABASE_NAME = "databaseName";

    private static final String SUCCEED = "succeed";

    private final DatabaseDiscoveryProviderAlgorithm databaseDiscoveryProviderAlgorithm;

    @Override
    public Collection<String> execute(final Collection<DataSource> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        List<String> result = new LinkedList<>();
        String databaseName = (String) dataMap.get(DATABASE_NAME);
        inputs.forEach(dataSource -> {
            try {
                databaseDiscoveryProviderAlgorithm.checkEnvironment(databaseName, dataSource);
                result.add(SUCCEED);
            } catch (SQLException e) {
                throw new IllegalStateException(String.format("Error while loading highly available Status with %s", dataSource), e);
            }
        });
        return result;
    }
}

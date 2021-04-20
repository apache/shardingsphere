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

package org.apache.shardingsphere.test.integration.junit.container.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.h2.tools.RunScript;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * H2 container.
 */
public final class H2Container extends ShardingSphereStorageContainer {
    
    private static final ConcurrentSkipListSet<String> DATABASES = new ConcurrentSkipListSet<>();
    
    public H2Container(final ParameterizedArray parameterizedArray) {
        super("h2-embedded", "h2:fake", new H2DatabaseType(), true, parameterizedArray);
    }
    
    @Override
    @SneakyThrows
    protected void execute() {
        super.execute();
        // TODO initialize SQL script
        File file = new File(EnvironmentPath.getInitSQLFile(getDatabaseType(), getParameterizedArray().getScenario()));
        for (Map.Entry<String, DataSource> each : getDataSourceMap().entrySet()) {
            if (!DATABASES.contains(each.getKey()) && DATABASES.add(each.getKey())) {
                try (Connection connection = each.getValue().getConnection();
                     FileReader reader = new FileReader(file)) {
                    RunScript.execute(connection, reader);
                }
            }
        }
    }
    
    @Override
    public boolean isHealthy() {
        return true;
    }
    
    @Override
    public synchronized Map<String, DataSource> getDataSourceMap() {
        ImmutableMap.Builder<String, DataSource> builder = ImmutableMap.builder();
        getDatabases().forEach(e -> builder.put(e, createDataSource(e)));
        return builder.build();
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return String.format("jdbc:h2:mem:%s;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=false;MODE=MySQL", Objects.isNull(dataSourceName) ? "test_db" : dataSourceName);
    }
    
    @Override
    protected int getPort() {
        return 0;
    }
    
    @Override
    protected String getUsername() {
        return "sa";
    }
    
    @Override
    protected String getPassword() {
        return "";
    }
    
}

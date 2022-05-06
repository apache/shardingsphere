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

package org.apache.shardingsphere.example.proxy.distsql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.example.proxy.distsql.factory.DataSourceFactory;
import org.apache.shardingsphere.example.proxy.distsql.hint.HintType;
import org.apache.shardingsphere.example.proxy.distsql.utils.FileUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public final class DistSQLHintExample {
    
    public static void main(final String[] args) throws SQLException, IOException {
        HintType hintType = selectedHintType();
        DataSource dataSource = DataSourceFactory.createDataSource(FileUtil.getFile(hintType.getConfigPath()));
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        execute(dataSource, hintType);
        exampleService.cleanEnvironment();
    }
    
    private static HintType selectedHintType() {
        return HintType.SET_SHARDING;
//        return HintType.ADD_SHARDING;
//        return HintType.SET_READWRITE_SPLITTING;
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void execute(final DataSource dataSource, HintType hintType) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            DistSQLExecutor executor = hintType.getExecutor();
            executor.init(statement);
            executor.execute();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

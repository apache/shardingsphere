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
import org.apache.shardingsphere.example.proxy.distsql.factory.DataSourceFactory;
import org.apache.shardingsphere.example.proxy.distsql.feature.FeatureType;
import org.apache.shardingsphere.example.proxy.distsql.util.FileUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
public final class DistSQLFeatureExample {
    
    public static void main(final String[] args) throws IOException {
        DataSource dataSource = DataSourceFactory.createDataSource(FileUtils.getFile("/client/datasource-config.yaml"));
        execute(dataSource);
    }
    
    private static void execute(final DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            DistSQLExecutor featureExecutor = selectFeature().getExecutor();
            featureExecutor.init(statement);
            featureExecutor.execute();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error(ex.getMessage());
        }
    }
    
    private static FeatureType selectFeature() {
//        return FeatureType.RESOURCE;
//        return FeatureType.SHADOW;
//        return FeatureType.ENCRYPT;
//        return FeatureType.SHARDING;
        return FeatureType.READWRITE_SPLITTING;
    }
}

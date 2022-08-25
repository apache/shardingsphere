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

package org.apache.shardingsphere.data.pipeline.mysql.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ServerVersion;
import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtension;

import java.util.Properties;

/**
 * MySQL JDBC query properties extension.
 */
@Slf4j
public final class MySQLJdbcQueryPropertiesExtension implements JdbcQueryPropertiesExtension {
    
    private static String mysqlConnectorVersion;
    
    static {
        try {
            Class<?> mysqlDriverClass = MySQLJdbcQueryPropertiesExtension.class.getClassLoader().loadClass("com.mysql.jdbc.Driver");
            mysqlConnectorVersion = mysqlDriverClass.getPackage().getImplementationVersion();
            log.info("mysql connector version {}", mysqlConnectorVersion);
        } catch (final ClassNotFoundException ex) {
            log.warn("not find com.mysql.jdbc.Driver class");
        }
    }
    
    private final Properties queryProps = new Properties();
    
    public MySQLJdbcQueryPropertiesExtension() {
        queryProps.setProperty("useSSL", Boolean.FALSE.toString());
        queryProps.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        queryProps.setProperty("yearIsDateType", Boolean.FALSE.toString());
        // refer https://bugs.mysql.com/bug.php?id=91065
        String zeroDateTimeBehavior = "convertToNull";
        if (null != mysqlConnectorVersion) {
            zeroDateTimeBehavior = new ServerVersion(mysqlConnectorVersion).greaterThanOrEqualTo(8, 0, 0) ? "CONVERT_TO_NULL" : zeroDateTimeBehavior;
        }
        queryProps.setProperty("zeroDateTimeBehavior", zeroDateTimeBehavior);
        queryProps.setProperty("noDatetimeStringSync", Boolean.TRUE.toString());
        queryProps.setProperty("jdbcCompliantTruncation", Boolean.FALSE.toString());
    }
    
    @Override
    public Properties extendQueryProperties() {
        return queryProps;
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}

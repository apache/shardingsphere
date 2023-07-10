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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * MySQL JDBC query properties extension.
 */
@Slf4j
public final class MySQLJdbcQueryPropertiesExtension implements JdbcQueryPropertiesExtension {
    
    private static final String MYSQL_CONNECTOR_VERSION = initMysqlConnectorVersion();
    
    private static final List<String> NOT_OVERRIDE_PROPERTIES = Collections.singletonList("netTimeoutForStreamingResults");
    
    private final Properties queryProps = new Properties();
    
    public MySQLJdbcQueryPropertiesExtension() {
        queryProps.setProperty("useSSL", Boolean.FALSE.toString());
        queryProps.setProperty("useServerPrepStmts", Boolean.FALSE.toString());
        queryProps.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        queryProps.setProperty("yearIsDateType", Boolean.FALSE.toString());
        queryProps.setProperty("zeroDateTimeBehavior", getZeroDateTimeBehavior());
        queryProps.setProperty("noDatetimeStringSync", Boolean.TRUE.toString());
        queryProps.setProperty("jdbcCompliantTruncation", Boolean.FALSE.toString());
        queryProps.setProperty("netTimeoutForStreamingResults", "600");
    }
    
    private String getZeroDateTimeBehavior() {
        // refer https://bugs.mysql.com/bug.php?id=91065
        String zeroDateTimeBehavior = "convertToNull";
        if (null != MYSQL_CONNECTOR_VERSION) {
            zeroDateTimeBehavior = new ServerVersion(MYSQL_CONNECTOR_VERSION).greaterThanOrEqualTo(8, 0, 0) ? "CONVERT_TO_NULL" : zeroDateTimeBehavior;
        }
        return zeroDateTimeBehavior;
    }
    
    private static String initMysqlConnectorVersion() {
        try {
            Class<?> mysqlDriverClass = Thread.currentThread().getContextClassLoader().loadClass("com.mysql.jdbc.Driver");
            return mysqlDriverClass.getPackage().getImplementationVersion();
        } catch (final ClassNotFoundException ex) {
            log.warn("not find com.mysql.jdbc.Driver class");
            return null;
        }
    }
    
    @Override
    public void extendQueryProperties(final Properties props) {
        for (String each : queryProps.stringPropertyNames()) {
            if (NOT_OVERRIDE_PROPERTIES.contains(each) && props.containsKey(each)) {
                continue;
            }
            props.setProperty(each, queryProps.getProperty(each));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}

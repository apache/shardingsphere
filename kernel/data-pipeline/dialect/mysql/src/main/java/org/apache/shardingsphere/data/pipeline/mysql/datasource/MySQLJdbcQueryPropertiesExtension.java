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
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * MySQL JDBC query properties extension.
 */
@Slf4j
public final class MySQLJdbcQueryPropertiesExtension implements JdbcQueryPropertiesExtension {
    
    private static final String MYSQL_CONNECTOR_VERSION = initMySQLConnectorVersion();
    
    private static final Collection<String> NOT_OVERRIDE_PROPERTIES = Collections.singleton("netTimeoutForStreamingResults");
    
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
        return null != MYSQL_CONNECTOR_VERSION && new ServerVersion(MYSQL_CONNECTOR_VERSION).greaterThanOrEqualTo(8, 0, 0) ? "CONVERT_TO_NULL" : "convertToNull";
    }
    
    private static String initMySQLConnectorVersion() {
        try {
            Class<?> driverClass = Thread.currentThread().getContextClassLoader().loadClass("com.mysql.jdbc.Driver");
            return driverClass.getPackage().getImplementationVersion();
        } catch (final ClassNotFoundException ex) {
            log.warn("Can not find `com.mysql.jdbc.Driver` class.");
            return null;
        }
    }
    
    @Override
    public void extendQueryProperties(final Properties props) {
        for (String each : queryProps.stringPropertyNames()) {
            if (!NOT_OVERRIDE_PROPERTIES.contains(each) || !props.containsKey(each)) {
                props.setProperty(each, queryProps.getProperty(each));
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}

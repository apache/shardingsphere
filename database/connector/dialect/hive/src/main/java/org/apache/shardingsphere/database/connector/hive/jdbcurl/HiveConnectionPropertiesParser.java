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

package org.apache.shardingsphere.database.connector.hive.jdbcurl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Connection properties parser of Hive.
 */
public final class HiveConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    private static final String HIVE_JDBC_UTILS_CLASS_NAME = "org.apache.hive.jdbc.Utils";
    
    @Override
    @SneakyThrows
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        Object params = parseURL(url);
        String host = getString(params, "getHost");
        int port = getInt(params, "getPort");
        if (null == host && 0 == port) {
            throw new RuntimeException("HiveServer2 in embedded mode has been deprecated by Apache Hive, "
                    + "See https://issues.apache.org/jira/browse/HIVE-28418 . "
                    + "Users should start local HiveServer2 through Docker Image https://hub.docker.com/r/apache/hive .");
        }
        Properties queryProps = new Properties();
        queryProps.putAll(getMap(params, "getSessionVars"));
        queryProps.putAll(getMap(params, "getHiveConfs"));
        queryProps.putAll(getMap(params, "getHiveVars"));
        return new ConnectionProperties(host, port, getString(params, "getDbName"), null, queryProps);
    }
    
    private Object parseURL(final String url) throws ReflectiveOperationException {
        Method parseURLMethod = Class.forName(HIVE_JDBC_UTILS_CLASS_NAME).getMethod("parseURL", String.class, Properties.class);
        try {
            return parseURLMethod.invoke(null, url, new Properties());
        } catch (final InvocationTargetException ex) {
            return throwTargetException(ex);
        }
    }
    
    @SneakyThrows
    private Object throwTargetException(final InvocationTargetException ex) {
        throw ex.getTargetException();
    }
    
    private String getString(final Object target, final String methodName) throws ReflectiveOperationException {
        return (String) target.getClass().getMethod(methodName).invoke(target);
    }
    
    private int getInt(final Object target, final String methodName) throws ReflectiveOperationException {
        return (Integer) target.getClass().getMethod(methodName).invoke(target);
    }
    
    private Map<?, ?> getMap(final Object target, final String methodName) throws ReflectiveOperationException {
        return (Map<?, ?>) target.getClass().getMethod(methodName).invoke(target);
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}

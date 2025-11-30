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
import org.apache.hive.jdbc.JdbcUriParseException;
import org.apache.hive.jdbc.Utils;
import org.apache.hive.jdbc.Utils.JdbcConnectionParams;
import org.apache.hive.jdbc.ZooKeeperHiveClientException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Connection properties parser of Hive.
 */
public final class HiveConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    @SneakyThrows({ZooKeeperHiveClientException.class, JdbcUriParseException.class, SQLException.class})
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        JdbcConnectionParams params = Utils.parseURL(url, new Properties());
        if (null == params.getHost() && 0 == params.getPort()) {
            throw new RuntimeException("HiveServer2 in embedded mode has been deprecated by Apache Hive, "
                    + "See https://issues.apache.org/jira/browse/HIVE-28418 . "
                    + "Users should start local HiveServer2 through Docker Image https://hub.docker.com/r/apache/hive .");
        }
        Properties queryProps = new Properties();
        queryProps.putAll(params.getSessionVars());
        queryProps.putAll(params.getHiveConfs());
        queryProps.putAll(params.getHiveVars());
        return new ConnectionProperties(params.getHost(), params.getPort(), params.getDbName(), null, queryProps);
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}

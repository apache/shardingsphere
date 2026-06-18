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

package org.apache.shardingsphere.database.connector.firebird.jdbcurl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBDriver;

import java.util.Properties;

/**
 * Connection properties parser of Firebird.
 */
public final class FirebirdConnectionPropertiesParser implements ConnectionPropertiesParser {
    
    @SneakyThrows(Exception.class)
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        GDSType type = GDSFactory.getTypeForProtocol(url);
        String databaseURL = GDSFactory.getDatabasePath(type, url);
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString(databaseURL);
        String attachObjectName = dbAttachInfo.getAttachObjectName();
        String databaseName = attachObjectName.contains("?") ? attachObjectName.split("\\?")[0] : attachObjectName;
        Properties queryProps = new Properties();
        queryProps.putAll(FBDriver.normalizeProperties(url, new Properties()));
        return new ConnectionProperties(dbAttachInfo.getServerName(), dbAttachInfo.getPortNumber(), databaseName, null, queryProps);
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}

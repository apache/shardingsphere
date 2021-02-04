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

package org.apache.shardingsphere.test.integration.env.database.type;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.database.EmbeddedDatabaseDistributionProperties;
import org.apache.shardingsphere.test.integration.env.database.EmbeddedDatabaseResource;

import java.io.File;

/**
 * Embedded database resource for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLEmbeddedDatabaseResource implements EmbeddedDatabaseResource {
    
    private final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps;
    
    private final int port;
    
    private EmbeddedMysql embeddedMySQL;
    
    @Override
    public void start() {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("MySQL");
        DownloadConfig downloadConfig = DownloadConfig.aDownloadConfig().withBaseUrl(embeddedDatabaseProps.getURL(databaseType)).build();
        MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(Version.valueOf(embeddedDatabaseProps.getVersion(databaseType)))
                .withCharset(Charset.UTF8MB4)
                .withTempDir(new File(downloadConfig.getCacheDir(), "runtime").getPath())
                .withPort(port)
                .withUser("test", "test")
                .withServerVariable("bind-address", "0.0.0.0")
                .withServerVariable("innodb_flush_log_at_trx_commit", 2)
                .build();
        embeddedMySQL = EmbeddedMysql.anEmbeddedMysql(mysqldConfig, downloadConfig).start();
    }
    
    @Override
    public void stop() {
        if (null != embeddedMySQL) {
            embeddedMySQL.stop();
        }
    }
}

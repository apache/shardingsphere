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

package org.apache.shardingsphere.test.integration.env.database;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;

import java.io.File;

/**
 * Embedded database resource for MySQL.
 */
@RequiredArgsConstructor
@Slf4j
public final class MySQLEmbeddedDatabaseResource implements EmbeddedDatabaseResource {
    
    private final DatabaseEnvironment databaseEnvironment;
    
    private EmbeddedMysql embeddedMySQL;
    
    @Override
    public void start() {
        long startTime = System.currentTimeMillis();
        log.info("Test embedded database resources MySQL prepare.");
        DownloadConfig downloadConfig = DownloadConfig.aDownloadConfig()
                .withBaseUrl(databaseEnvironment.getDistributionUrl())
                .build();
        MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(Version.valueOf(databaseEnvironment.getDistributionVersion()))
                .withCharset(Charset.UTF8MB4)
                .withTempDir(new File(downloadConfig.getCacheDir(), "runtime").getPath())
                .withPort(databaseEnvironment.getPort())
                .withUser("test", "test")
                .withServerVariable("bind-address", "0.0.0.0")
                .withServerVariable("innodb_flush_log_at_trx_commit", 2)
                .build();
        embeddedMySQL = EmbeddedMysql.anEmbeddedMysql(mysqldConfig, downloadConfig).start();
        log.info("Test embedded database resources MySQL start mysqld elapsed time {}s", (System.currentTimeMillis() - startTime) / 1000);
    }
    
    @Override
    public void stop() {
        if (null != embeddedMySQL) {
            embeddedMySQL.stop();
        }
    }
}

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

package org.apache.shardingsphere.test.integration.env.database.embedded.type;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jna.Platform;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabase;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabaseDistributionProperties;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Embedded database for MySQL.
 */
public final class MySQLEmbeddedDatabase implements EmbeddedDatabase {
    
    private volatile EmbeddedMysql embeddedMySQL;
    
    @Override
    public void start(final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType(getType());
        DownloadConfig downloadConfig = DownloadConfig.aDownloadConfig().withBaseUrl(embeddedDatabaseProps.getURL(databaseType)).build();
        MysqldConfig.Builder mysqldConfigBuilder = MysqldConfig.aMysqldConfig(detectVersion(embeddedDatabaseProps.getVersion(databaseType)))
                .withCharset(Charset.UTF8MB4)
                .withTempDir(new File(downloadConfig.getCacheDir(), "runtime").getPath())
                .withPort(port)
                .withUser("test", "test")
                .withServerVariable("bind-address", "0.0.0.0")
                .withServerVariable("innodb_flush_log_at_trx_commit", 2);
        if (!Platform.isWindows()) {
            mysqldConfigBuilder = mysqldConfigBuilder.withServerVariable("innodb_flush_method", "O_DIRECT");
        }
        embeddedMySQL = EmbeddedMysql.anEmbeddedMysql(mysqldConfigBuilder.build(), downloadConfig).start();
    }
    
    private Version detectVersion(final String distributionVersion) {
        Version version;
        if (Strings.isNullOrEmpty(distributionVersion) && Platform.isMac()) {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            if (osVersion.startsWith("10.6")) {
                version = Version.v5_5_40;
            } else if (osVersion.startsWith("10.9")) {
                version = Version.v5_6_24;
            } else if (osVersion.startsWith("10.10")) {
                version = Version.v5_7_10;
            } else if (osVersion.startsWith("10.11")) {
                version = Version.v5_7_16;
            } else if (osVersion.startsWith("10.12")) {
                version = Version.v5_7_19;
            } else if (osVersion.startsWith("10.13")) {
                version = Version.v8_0_11;
            } else if (osVersion.startsWith("10.14") || osVersion.startsWith("10.15")) {
                version = Version.v5_7_27;
            } else {
                throw new UnsupportedOperationException(String.format("%s-%s is not supported", osName, osVersion));
            }
        } else if (Strings.isNullOrEmpty(distributionVersion) && com.sun.jna.Platform.isLinux()) {
            version = Version.v5_7_latest;
        } else if (Strings.isNullOrEmpty(distributionVersion) && com.sun.jna.Platform.isWindows()) {
            version = Version.v5_7_latest;
        } else {
            version = Enums.getIfPresent(Version.class, distributionVersion).orNull();
            Preconditions.checkArgument(null != version, String.format("The current setup version %s is not supported, only the following versions [%s] are currently supported",
                    distributionVersion, Arrays.stream(Version.values()).map(v -> String.join(".", v.getMajorVersion(), v.getMinorVersion() + "")).collect(Collectors.joining(", "))));
        }
        return version;
    }
    
    @Override
    public void stop() {
        if (null != embeddedMySQL) {
            embeddedMySQL.stop();
            embeddedMySQL = null;
        }
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}

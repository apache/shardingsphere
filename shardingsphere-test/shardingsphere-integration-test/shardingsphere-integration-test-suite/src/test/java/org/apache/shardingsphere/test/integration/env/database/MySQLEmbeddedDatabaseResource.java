package org.apache.shardingsphere.test.integration.env.database;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;

/**
 * MySQL database resource.
 */
@Slf4j
@RequiredArgsConstructor
public final class MySQLEmbeddedDatabaseResource implements EmbeddedDatabaseResource {

    private final DatabaseEnvironment databaseEnvironment;

    private EmbeddedMysql embeddedMySQL;

    @Override
    public void start() {
        final long t = System.currentTimeMillis();
        log.info("Test embedded database resources MySQL prepare.");
        DownloadConfig downloadConfig = DownloadConfig.aDownloadConfig()
                .withBaseUrl(databaseEnvironment.getDistributionUrl())
                .build();
        MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(Version.valueOf(databaseEnvironment.getDistributionVersion()))
                .withCharset(Charset.UTF8MB4)
                .withPort(databaseEnvironment.getPort())
                .withUser("test", "test")
                .withServerVariable("bind-address", "0.0.0.0")
                .withServerVariable("innodb_flush_log_at_trx_commit", 2)
                .build();
        embeddedMySQL = EmbeddedMysql.anEmbeddedMysql(mysqldConfig, downloadConfig).start();
        log.info("Test embedded database resources MySQL start mysqld elapsed time {}s", (System.currentTimeMillis() - t) / 1000);
    }

    @Override
    public void stop() {
        if (null != embeddedMySQL) {
            embeddedMySQL.stop();
        }
    }
}

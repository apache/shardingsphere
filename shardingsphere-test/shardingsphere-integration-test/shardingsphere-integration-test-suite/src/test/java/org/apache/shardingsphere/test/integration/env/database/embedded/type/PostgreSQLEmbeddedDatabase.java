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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabase;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabaseDistributionProperties;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.distribution.Version.Main;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Embedded database for PostgreSQL.
 */
@Slf4j
@RequiredArgsConstructor
public final class PostgreSQLEmbeddedDatabase implements EmbeddedDatabase {
    
    private EmbeddedPostgres postgres;
    
    @SneakyThrows
    @Override
    public void start(final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        final long startTime = System.currentTimeMillis();
        log.info("Test embedded database resources PostgreSQL prepare.");
        String cacheDir = new File(System.getProperty("user.home"), ".embedpostgresql").getPath();
        postgres = new EmbeddedPostgres(Main.V10, new File(cacheDir, "runtime" + File.separator + UUID.randomUUID().toString()).getPath());
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.Postgres)
                .artifactStore(new PostgresArtifactStoreBuilder()
                        .defaults(Command.Postgres)
                        .useCache(true)
                        .download(new PostgresDownloadConfigBuilder()
                                .defaultsForCommand(Command.Postgres)
                                .downloadPath(embeddedDatabaseProps.getURL(DatabaseTypeRegistry.getActualDatabaseType(getType())))
                                .build()))
                .commandLinePostProcessor(privilegedWindowsRunasPostprocessor())
                .build();
        List<String> additionalParams = Arrays.asList("-E", "SQL_ASCII",
                "--locale=C",
                "--lc-collate=C",
                "--lc-ctype=C");
        postgres.start(runtimeConfig, "127.0.0.1", port, EmbeddedPostgres.DEFAULT_DB_NAME, "postgres", "postgres", additionalParams);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
        log.info("Test embedded database resources PostgreSQL start postgres elapsed time {}s", (System.currentTimeMillis() - startTime) / 1000);
    }
    
    @SneakyThrows
    private ICommandLinePostProcessor privilegedWindowsRunasPostprocessor() {
        if (Platform.detect().equals(Platform.Windows)) {
            // Based on https://stackoverflow.com/a/11995662
            final int adminCommandResult = Runtime.getRuntime().exec("net session").waitFor();
            if (adminCommandResult == 0) {
                return runWithoutPrivileges();
            }
        }
        return doNothing();
    }
    
    private ICommandLinePostProcessor runWithoutPrivileges() {
        return (distribution, args) -> {
            if (args.size() > 0 && args.get(0).endsWith("postgres.exe")) {
                return Arrays.asList("runas", "/trustlevel:0x20000", String.format("\"%s\"", String.join(" ", args)));
            }
            return args;
        };
    }
    
    private static ICommandLinePostProcessor doNothing() {
        return (distribution, args) -> args;
    }
    
    @Override
    public void stop() {
        if (null != postgres) {
            postgres.stop();
        }
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}

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
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabase;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabaseDistributionProperties;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.PackagePaths;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Embedded database for PostgreSQL.
 */
public final class PostgreSQLEmbeddedDatabase implements EmbeddedDatabase {
    
    private volatile EmbeddedPostgres embeddedPostgres;
    
    @SneakyThrows
    @Override
    public void start(final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        String cacheDir = new File(System.getProperty("user.home"), ".embedpostgresql").getPath();
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType(getType());
        Version version = detectVersion(embeddedDatabaseProps.getVersion(databaseType));
        String extractedDir = new File(cacheDir, "extracted").getPath();
        String instanceDir = new File(cacheDir, "runtime" + File.separator + "PostgreSQL-" + version.name() + File.separator + UUID.randomUUID()).getPath();
        embeddedPostgres = new EmbeddedPostgres(version, new File(instanceDir).getPath());
        Command cmd = Command.Postgres;
        FixedPath extractedCache = new FixedPath(extractedDir);
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(cmd)
                .artifactStore(new PostgresArtifactStoreBuilder()
                        .defaults(cmd)
                        .useCache(true)
                        .tempDir(extractedCache)
                        .download(new PostgresDownloadConfigBuilder()
                                .defaultsForCommand(cmd)
                                .downloadPath(embeddedDatabaseProps.getURL(DatabaseTypeRegistry.getActualDatabaseType(getType())))
                                .packageResolver(new PackagePaths(cmd, extractedCache))
                                .build()))
                .commandLinePostProcessor(privilegedWindowsRunasPostprocessor())
                .build();
        List<String> additionalParams = Arrays.asList(
                "-E", "UTF-8",
                "--lc-collate=en_US.UTF-8",
                "--lc-ctype=en_US.UTF-8");
        List<String> additionalPostgresParams = Arrays.asList(
                "-c", "max_connections=512",
                "-c", "logging_collector=on",
                "-c", "log_directory=log",
                "-c", "fsync=off"
        );
        embeddedPostgres.start(runtimeConfig, "127.0.0.1", port, EmbeddedPostgres.DEFAULT_DB_NAME,
                EmbeddedPostgres.DEFAULT_USER, EmbeddedPostgres.DEFAULT_PASSWORD, additionalParams, additionalPostgresParams);
    }
    
    private Version detectVersion(final String distributionVersion) {
        Version version;
        if (Strings.isNullOrEmpty(distributionVersion) && com.sun.jna.Platform.isMac()) {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            if (osVersion.startsWith("10.10") || osVersion.startsWith("10.11") || osVersion.startsWith("10.12")) {
                version = Version.V10_6;
            } else if (osVersion.startsWith("10.13") || osVersion.startsWith("10.14") || osVersion.startsWith("10.15")) {
                version = Version.V11_1;
            } else {
                throw new UnsupportedOperationException(String.format("%s-%s is not supported", osName, osVersion));
            }
        } else if (Strings.isNullOrEmpty(distributionVersion) && com.sun.jna.Platform.isLinux()) {
            version = Version.V10_6;
        } else if (Strings.isNullOrEmpty(distributionVersion) && com.sun.jna.Platform.isWindows()) {
            version = Version.V11_1;
        } else {
            version = Enums.getIfPresent(Version.class, distributionVersion).orNull();
            Preconditions.checkArgument(null != version, String.format("The current setup version %s is not supported, only the following versions [%s] are currently supported",
                    distributionVersion, Arrays.stream(Version.values()).map(Enum::name).collect(Collectors.joining(", "))));
        }
        return version;
    }
    
    @SneakyThrows
    private ICommandLinePostProcessor privilegedWindowsRunasPostprocessor() {
        if (Platform.detect() == Platform.Windows) {
            // Based on https://stackoverflow.com/a/11995662
            int adminCommandResult = Runtime.getRuntime().exec("net session").waitFor();
            if (adminCommandResult == 0) {
                return runWithoutPrivileges();
            }
        }
        return doNothing();
    }
    
    private ICommandLinePostProcessor runWithoutPrivileges() {
        return (distribution, args) -> {
            if (!args.isEmpty() && args.get(0).endsWith("postgres.exe")) {
                return Arrays.asList("runas", "/trustlevel:0x20000", String.format("\"%s\"", String.join(" ", args)));
            }
            return args;
        };
    }
    
    private static ICommandLinePostProcessor doNothing() {
        return (distribution, args) -> args;
    }
    
    @SneakyThrows
    @Override
    public void stop() {
        if (null != embeddedPostgres) {
            embeddedPostgres.stop();
            embeddedPostgres = null;
        }
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}

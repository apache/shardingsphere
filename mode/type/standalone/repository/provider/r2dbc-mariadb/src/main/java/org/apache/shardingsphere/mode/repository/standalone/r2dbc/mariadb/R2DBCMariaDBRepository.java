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

package org.apache.shardingsphere.mode.repository.standalone.r2dbc.mariadb;

import com.google.common.base.Strings;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * R2DBC MariaDB repository.
 */
@Slf4j
public class R2DBCMariaDBRepository implements StandalonePersistRepository {
    
    private static final String SEPARATOR = "/";
    
    private ConnectionPool connectionPool;
    
    @Override
    public void init(final Properties props) {
        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "mariadb")
                .option(ConnectionFactoryOptions.HOST, props.getProperty("host"))
                .option(ConnectionFactoryOptions.PORT, Integer.parseInt(props.getProperty("port")))
                .option(ConnectionFactoryOptions.USER, props.getProperty("user"))
                .option(ConnectionFactoryOptions.PASSWORD, props.getProperty("password"))
                .option(ConnectionFactoryOptions.DATABASE, props.getProperty("database"))
                .build();
        ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
        ConnectionPoolConfiguration connectionPoolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory).build();
        connectionPool = new ConnectionPool(connectionPoolConfiguration);
        Mono.usingWhen(connectionPool.create(),
                connection -> Mono.from(connection.createBatch()
                        .add("DROP TABLE IF EXISTS `repository`")
                        .add("CREATE TABLE IF NOT EXISTS `repository`(id varchar(36) PRIMARY KEY, `key` TEXT, `value` TEXT, parent TEXT)")
                        .execute()),
                Connection::close)
                .block();
    }
    
    @Override
    public String getDirectly(final String key) {
        return Mono.usingWhen(connectionPool.create(),
                connection -> Mono.from(connection.createStatement("SELECT `value` FROM `repository` WHERE `key` = ?")
                        .bind(0, key)
                        .execute())
                        .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> row.get("value", String.class))))
                        .doOnError(throwable -> log.error("Get {} data by key: {} failed", getType(), key, throwable)),
                Connection::close)
                .block();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return Flux.usingWhen(connectionPool.create(),
                connection -> Flux.from(connection.createStatement("SELECT `key` FROM `repository` WHERE parent = ?")
                        .bind(0, key)
                        .execute())
                        .flatMap(result -> Flux.from(result.map((row, rowMetadata) -> row.get("key", String.class))))
                        .filter(childrenKey -> !Strings.isNullOrEmpty(childrenKey))
                        .map(childrenKey -> {
                            int lastIndexOf = childrenKey.lastIndexOf(SEPARATOR);
                            return childrenKey.substring(lastIndexOf + 1);
                        })
                        .collectList()
                        .defaultIfEmpty(Collections.emptyList())
                        .doOnError(throwable -> log.error("Get children {} data by key: {} failed", getType(), key, throwable)),
                Connection::close)
                .blockLast();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return !Strings.isNullOrEmpty(getDirectly(key));
    }
    
    @Override
    public void persist(final String key, final String value) {
        if (isExisted(key)) {
            update(key, value);
            return;
        }
        String tempPrefix = "";
        String parent = SEPARATOR;
        String[] paths = Arrays.stream(key.split(SEPARATOR)).filter(each -> !Strings.isNullOrEmpty(each)).toArray(String[]::new);
        for (int i = 0; i < paths.length - 1; i++) {
            String tempKey = tempPrefix + SEPARATOR + paths[i];
            String tempKeyVal = getDirectly(tempKey);
            if (Strings.isNullOrEmpty(tempKeyVal)) {
                if (i != 0) {
                    parent = tempPrefix;
                }
                insert(tempKey, "", parent);
            }
            tempPrefix = tempKey;
            parent = tempKey;
        }
        insert(key, value, parent);
    }
    
    private void insert(final String key, final String value, final String parent) {
        Mono.usingWhen(connectionPool.create(),
                connection -> Mono.from(connection.createStatement("INSERT INTO `repository` VALUES(?, ?, ?, ?)")
                        .bind(0, UUID.randomUUID().toString())
                        .bind(1, key)
                        .bind(2, value)
                        .bind(3, parent)
                        .execute())
                        .doOnError(throwable -> log.error("Persist {} data to key: {} failed", getType(), key, throwable)),
                Connection::close)
                .block();
    }
    
    @Override
    public void update(final String key, final String value) {
        Mono.usingWhen(connectionPool.create(),
                connection -> Mono.from(connection.createStatement("UPDATE `repository` SET `value` = ? WHERE `key` = ?")
                        .bind(0, value)
                        .bind(1, key)
                        .execute())
                        .onErrorMap(throwable -> new SQLException())
                        .doOnError(throwable -> log.error("Update {} data to key: {} failed", getType(), key, throwable)),
                Connection::close)
                .block();
    }
    
    @Override
    public void delete(final String key) {
        Mono.usingWhen(connectionPool.create(),
                connection -> Mono.from(connection.createStatement("DELETE FROM `repository` WHERE `key` = ?")
                        .bind(0, key)
                        .execute())
                        .doOnError(throwable -> log.error(String.format("Delete %s data by key: {} failed", getType()), key, throwable)),
                Connection::close)
                .block();
    }
    
    @Override
    public void close() {
        connectionPool.close().block();
    }
    
    @Override
    public String getType() {
        return "R2DBC_MARIADB";
    }
}

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

package org.apache.shardingsphere.proxy.backend.hbase.connector;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.shardingsphere.proxy.backend.hbase.config.YamlHBaseConfiguration;
import org.apache.shardingsphere.proxy.backend.hbase.config.YamlHBaseParameter;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Connection factory of HBase.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseConnectionFactory {
    
    /**
     * Create HBase connection.
     * 
     * @param yamlProxyHBaseConfig YAML HBase configuration
     * @return A connection for per HBase cluster
     */
    public static Map<String, Connection> createHBaseConnections(final YamlHBaseConfiguration yamlProxyHBaseConfig) {
        Map<String, Connection> result = new LinkedHashMap<>(yamlProxyHBaseConfig.getDataSources().size(), 1F);
        for (Entry<String, YamlHBaseParameter> entry : yamlProxyHBaseConfig.getDataSources().entrySet()) {
            result.put(entry.getKey(), createConnection(entry.getValue()));
        }
        return result;
    }
    
    private static Connection createConnection(final YamlHBaseParameter parameter) {
        Configuration config = createConfiguration(parameter);
        try {
            return Strings.isNullOrEmpty(parameter.getAccessUser())
                    ? ConnectionFactory.createConnection(config)
                    : ConnectionFactory.createConnection(config, User.create(UserGroupInformation.createRemoteUser(parameter.getAccessUser())));
        } catch (final IOException ex) {
            throw new HBaseOperationException(ex.getMessage());
        }
    }
    
    private static Configuration createConfiguration(final YamlHBaseParameter parameter) {
        Configuration result = HBaseConfiguration.create();
        result.set("fs.defaultFS", parameter.getFsDefaultFs());
        result.set("hbase.rootdir", parameter.getHbaseRootDir());
        result.setLong("hbase.rpc.timeout", parameter.getHbaseRpcTimeout());
        result.setLong("hbase.client.operation.timeout", parameter.getHbaseClientOperationTimeout());
        result.setLong("hbase.client.scanner.timeout.period", parameter.getHbaseClientScannerTimeoutPeriod());
        result.set("hbase.zookeeper.property.dataDir", parameter.getHbaseZookeeperPropertyDataDir());
        result.set("hbase.zookeeper.quorum", parameter.getHbaseZookeeperQuorum());
        result.set("zookeeper.znode.parent", parameter.getZookeeperZNodeParent());
        result.setInt("hbase.client.ipc.pool.size", parameter.getIpcPoolSize());
        return result;
    }
}

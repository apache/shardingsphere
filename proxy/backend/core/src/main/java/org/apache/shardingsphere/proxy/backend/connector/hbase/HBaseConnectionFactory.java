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

package org.apache.shardingsphere.proxy.backend.connector.hbase;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.shardingsphere.proxy.backend.config.YamlHBaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlHBaseParameter;
import org.apache.shardingsphere.proxy.backend.exception.HBaseOperationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HBase Connection Factory.
 */
public class HBaseConnectionFactory {
    
    /**
     * create HBase connection.
     * @param yamlProxyHBaseConfiguration HBase config.
     * @return A connection for per HBase cluster.
     */
    public static Map<String, Connection> createHBaseConnections(final YamlHBaseConfiguration yamlProxyHBaseConfiguration) {
        Map<String, Connection> result = new LinkedHashMap<>(yamlProxyHBaseConfiguration.getDataSources().size());
        for (Map.Entry<String, YamlHBaseParameter> entry : yamlProxyHBaseConfiguration.getDataSources().entrySet()) {
            result.put(entry.getKey(), createConnection(entry.getValue()));
        }
        return result;
    }
    
    private static Connection createConnection(final YamlHBaseParameter parameter) {
        Configuration config = createConfiguration(parameter);
        try {
            if (StringUtils.isEmpty(parameter.getAccessUser())) {
                return ConnectionFactory.createConnection(config);
            } else {
                return ConnectionFactory.createConnection(config, createUser(parameter.getAccessUser()));
            }
        } catch (IOException e) {
            throw new HBaseOperationException(e.getMessage());
        }
    }
    
    private static User createUser(final String accessUser) {
        return User.create(UserGroupInformation.createRemoteUser(accessUser));
    }
    
    private static Configuration createConfiguration(final YamlHBaseParameter parameter) {
        Configuration config = HBaseConfiguration.create();
        config.set("fs.defaultFS", parameter.getFsDefaultFs());
        config.set("hbase.rootdir", parameter.getHbaseRootDir());
        config.setLong("hbase.rpc.timeout", parameter.getHbaseRpcTimeout());
        config.setLong("hbase.client.operation.timeout", parameter.getHbaseClientOperationTimeout());
        config.setLong("hbase.client.scanner.timeout.period", parameter.getHbaseClientScannerTimeoutPeriod());
        config.set("hbase.zookeeper.property.dataDir", parameter.getHbaseZookeeperPropertyDataDir());
        config.set("hbase.zookeeper.quorum", parameter.getHbaseZookeeperQuorum());
        config.set("zookeeper.znode.parent", parameter.getZookeeperZNodeParent());
        config.setInt("hbase.client.ipc.pool.size", parameter.getIpcPoolSize());
        return config;
    }
}

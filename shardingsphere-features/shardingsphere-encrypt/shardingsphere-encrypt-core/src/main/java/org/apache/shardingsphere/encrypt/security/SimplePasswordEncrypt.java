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

package org.apache.shardingsphere.encrypt.security;

import org.apache.shardingsphere.authority.api.config.PasswordEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.algorithm.NoneEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.security.PasswordEncrypt;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ShardingSphere password secure factory.
 */
public class SimplePasswordEncrypt implements PasswordEncrypt {

    private static final String BACKEND_PASSWORD_ENCRYPT_TYPE = "backend-password-encrypt-type";

    private static final String FRONTEND_PASSWORD_ENCRYPT_TYPE = "frontend-password-encrypt-type";

    private static final EncryptAlgorithm DEFAULT_NONE_ALGORITHM = new NoneEncryptAlgorithm();

    private Map<String, EncryptAlgorithm> algorithms = new ConcurrentHashMap<>(2);

    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }

    /**
     * Init all security algorithm by server.yaml config, this interface not threadsafe.
     * @param config the config of backend and frontend password encrypt algorithm
     */
    public void init(PasswordEncryptRuleConfiguration config) {
        if (config == null || config.getProps() == null || config.getEncryptors() == null) {
            return;
        }
        init(config.getProps(), config.getEncryptors());
    }

    /**
     * Init all security algorithm by server.yaml config, this interface not threadsafe.
     * @param props the prop of backend and frontend password encrypt algorithm
     * @param yamlConfig the alg yaml config
     */
    public void initYamlConfig(Properties props, Map<String, YamlShardingSphereAlgorithmConfiguration> yamlConfig) {
        if (yamlConfig == null || yamlConfig == null) {
            return;
        }
        Map<String, ShardingSphereAlgorithmConfiguration> algMaps = yamlConfig.entrySet().stream().collect(
            Collectors.toMap(entry -> entry.getKey(),
                entry -> new ShardingSphereAlgorithmConfiguration(
                    entry.getValue().getType(),
                    entry.getValue().getProps())
            )
        );
        init(props, algMaps);
    }

    /**
     * Init all security algorithm by server.yaml config, this interface not threadsafe.
     * @param props the prop of backend and frontend password encrypt algorithm
     * @param maps the name of algorithm which belongs props' key
     */
    public void init(Properties props, Map<String, ShardingSphereAlgorithmConfiguration> maps) {
        String[] keys = {BACKEND_PASSWORD_ENCRYPT_TYPE, FRONTEND_PASSWORD_ENCRYPT_TYPE};
        for (String key: keys) {
            if (null != props && props.containsKey(key)) {
                String propOfAlgorithmRule = props.getProperty(key);
                EncryptAlgorithm alg = ShardingSphereAlgorithmFactory.createAlgorithm(maps.get(propOfAlgorithmRule),
                        EncryptAlgorithm.class);
                algorithms.put(key, alg);
            }
        }
    }

    @Override
    public String decryptFrontend(final String encrypt) {
        Object obj = getAlgorithm(FRONTEND_PASSWORD_ENCRYPT_TYPE).decrypt(encrypt);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    @Override
    public String decryptBackend(final String encrypt) {
        return getAlgorithm(BACKEND_PASSWORD_ENCRYPT_TYPE).decrypt(encrypt).toString();
    }

    private EncryptAlgorithm getAlgorithm(final String key) {
        return algorithms.getOrDefault(key, DEFAULT_NONE_ALGORITHM);
    }
}

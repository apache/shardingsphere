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

package org.apache.shardingsphere.infra.security;

import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere algorithm secure factory.
 */
public final class AlgorithmSecureFactory {
    private static AlgorithmSecureFactory instance = new AlgorithmSecureFactory();

    private Map<String, AlgorithmSecure> algorithms = new ConcurrentHashMap<>(2);

    private Map<String, String> secure2AlgType = new ConcurrentHashMap<>(2);

    private boolean isInited = false;

    static {
        ShardingSphereServiceLoader.register(AlgorithmSecure.class);
    }

    private AlgorithmSecureFactory() {
        algorithms.put("NONE", new NoneAlgorithmSecure());
    }

    /**
     *  get singleton algorithm secure factory instance.
     * @return the factory
     */
    public static AlgorithmSecureFactory getInstance() {
        return instance;
    }

    /**
     * init all security algorithm by server.yaml config.
     * @param props the server.yaml prop config, use PROXY_FRONTEND_ALGORITHM and PROXY_BACKEND_ALGORITHM params
     */
    public void init(final Properties props) {
        String[] keys = {ConfigurationPropertyKey.PROXY_FRONTEND_ALGORITHM.getKey(),
                ConfigurationPropertyKey.PROXY_BACKEND_ALGORITHM.getKey()};
        for (String key: keys) {
            secure2AlgType.put(key, props.getOrDefault(key, "NONE").toString().trim());
        }
        Collection<AlgorithmSecure> tmpAlgs = ShardingSphereServiceLoader.newServiceInstances(AlgorithmSecure.class);
        for (AlgorithmSecure alg: tmpAlgs) {
            algorithms.put(alg.getType(), alg);
        }
        isInited = true;
    }

    /**
     * decrypt password for frontend of connect database password.
     * @param encrypt  the encrypt password
     * @return String the decrypt password
     * @throws Exception when decrypt with exception, such as iv and key not right
     */
    public String decryptFrontend(final String encrypt) throws Exception {
        return getAlgorithm(ConfigurationPropertyKey.PROXY_FRONTEND_ALGORITHM.getKey()).decrypt(encrypt);
    }

    /**
     * decrypt password for backend of connect database password.
     * @param encrypt  the encrypt password
     * @return String the decrypt password
     * @throws Exception when decrypt with exception, such as iv and key not right
     */
    public String decryptBackend(final String encrypt) throws Exception {
        return getAlgorithm(ConfigurationPropertyKey.PROXY_BACKEND_ALGORITHM.getKey()).decrypt(encrypt);
    }

    private AlgorithmSecure getAlgorithm(final String secureType) {
        return algorithms.get(secure2AlgType.getOrDefault(secureType, "NONE"));
    }
}

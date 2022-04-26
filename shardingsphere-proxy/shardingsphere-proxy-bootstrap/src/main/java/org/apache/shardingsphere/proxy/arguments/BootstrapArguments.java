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

package org.apache.shardingsphere.proxy.arguments;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;

import java.io.IOException;

/**
 * Bootstrap arguments.
 */
@Getter
public final class BootstrapArguments {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    private static final int DEFAULT_PORT = 3307;
    
    private final int port;
    
    private final String configurationPath;
    
    public BootstrapArguments(final String[] args) {
        port = getPort(args);
        configurationPath = getConfigurationPath(args);
    }
    
    private int getPort(final String[] args) {
        if (0 == args.length) {
            return getProxyDefaultPort(DEFAULT_CONFIG_PATH);
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException(String.format("Invalid port `%s`.", args[0]));
        }
    }
    
    private String getConfigurationPath(final String[] args) {
        return args.length < 2 ? DEFAULT_CONFIG_PATH : paddingWithSlash(args[1]);
    }
    
    private String paddingWithSlash(final String arg) {
        StringBuilder result = new StringBuilder(arg);
        if (!arg.startsWith("/")) {
            result.insert(0, '/');
        }
        if (!arg.endsWith("/")) {
            result.append('/');
        }
        return result.toString();
    }
    
    private static int getProxyDefaultPort(final String configurationPath) {
        YamlProxyConfiguration yamlConfig;
        try {
            yamlConfig = ProxyConfigurationLoader.load(configurationPath);
            return yamlConfig.getServerConfiguration().getProps().get(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getKey()) == null
                    ? Integer.parseInt(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getDefaultValue())
                    : Integer.parseInt(
                            yamlConfig.getServerConfiguration().getProps().get(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getKey()).toString());
        } catch (IOException e) {
            return DEFAULT_PORT;
        } catch (NullPointerException e) {
            return DEFAULT_PORT;
        }
    }
}

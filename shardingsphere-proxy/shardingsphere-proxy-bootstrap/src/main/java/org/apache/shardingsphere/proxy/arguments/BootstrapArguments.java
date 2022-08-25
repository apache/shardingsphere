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

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Bootstrap arguments.
 */
@RequiredArgsConstructor
public final class BootstrapArguments {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    
    private final String[] args;
    
    /**
     * Get port.
     *
     * @return port
     */
    public Optional<Integer> getPort() {
        if (0 == args.length) {
            return Optional.empty();
        }
        try {
            int port = Integer.parseInt(args[0]);
            if (port < 0) {
                return Optional.empty();
            }
            return Optional.of(port);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException(String.format("Invalid port `%s`.", args[0]));
        }
    }
    
    /**
     * Get configuration path.
     *
     * @return configuration path
     */
    public String getConfigurationPath() {
        return args.length < 2 ? DEFAULT_CONFIG_PATH : paddingWithSlash(args[1]);
    }
    
    /**
     * Get bind address list.
     *
     * @return address list
     */
    public List<String> getAddresses() {
        return args.length < 3 ? Collections.singletonList(DEFAULT_BIND_ADDRESS) : Arrays.asList(args[2].split(","));
    }
    
    private String paddingWithSlash(final String pathArg) {
        StringBuilder result = new StringBuilder(pathArg);
        if (!pathArg.startsWith("/")) {
            result.insert(0, '/');
        }
        if (!pathArg.endsWith("/")) {
            result.append('/');
        }
        return result.toString();
    }
}

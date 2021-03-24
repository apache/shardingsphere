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

package org.apache.shardingsphere.test.integration.junit.processor;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;

import java.io.InputStream;

public class AuthenticationProcessor implements Processor<AuthenticationProcessor.Authentication> {
    
    @Override
    @SneakyThrows
    public Authentication process(final InputStream stream) {
        YamlProxyServerConfiguration configuration = YamlEngine.unmarshal(ByteStreams.toByteArray(stream), YamlProxyServerConfiguration.class);
        YamlUserConfiguration user = configuration.getAuthentication().getUsers().get("root");
        return new Authentication("root", user.getPassword());
    }
    
    @Getter
    @RequiredArgsConstructor
    public static class Authentication {
        
        private final String user;
        
        private final String password;
    
    }
}

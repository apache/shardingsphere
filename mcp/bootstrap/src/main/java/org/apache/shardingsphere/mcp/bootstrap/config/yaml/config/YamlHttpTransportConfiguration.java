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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.validator.ValidHttpTransportConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML HTTP transport configuration.
 */
@Getter
@Setter
@ValidHttpTransportConfiguration
public final class YamlHttpTransportConfiguration implements YamlConfiguration {
    
    private boolean enabled;
    
    @NotBlank(message = "is required")
    private String bindHost;
    
    private boolean allowRemoteAccess;
    
    private String accessToken;
    
    @NotNull(message = "is required")
    @PositiveOrZero(message = "must be zero or positive")
    private Integer port;
    
    @NotBlank(message = "is required")
    @Pattern(regexp = "/.*", message = "must start with '/'")
    private String endpointPath;
    
    private Collection<String> allowedOrigins = new LinkedList<>();
    
    private Collection<String> authorizationServers = new LinkedList<>();
    
    private Collection<String> scopesSupported = new LinkedList<>();
    
    private String protectedResource;
    
    @Valid
    private YamlOAuthIntrospectionConfiguration oauthIntrospection = new YamlOAuthIntrospectionConfiguration();
}

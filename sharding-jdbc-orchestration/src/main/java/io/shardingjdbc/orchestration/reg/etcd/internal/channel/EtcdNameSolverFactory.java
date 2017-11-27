/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.reg.etcd.internal.channel;

import com.google.common.base.Strings;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

/**
 * Etcd name solver factory.
 * 
 * @author junxiong
 */
@RequiredArgsConstructor
public final class EtcdNameSolverFactory extends NameResolver.Factory {
    
    private static final Pattern SCHEMAS = Pattern.compile("^(http|https)");
    
    private final String scheme;
    
    private final List<String> endpoints;
    
    private ExecutorService executor;
    
    private boolean shutdown;
    
    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final Attributes params) {
        if (!scheme.equals(targetUri.getPath())) {
            return null;
        }
        return new NameResolver() {
            
            @Override
            public String getServiceAuthority() {
                return scheme;
            }
            
            @Override
            public void start(final Listener listener) {
                if (shutdown) {
                    return;
                }
                for (String each : endpoints) {
                    try {
                        URI uri = new URI(each);
                        if (!Strings.isNullOrEmpty(uri.getAuthority()) && SCHEMAS.matcher(uri.getScheme()).matches()) {
                            listener.onAddresses(Collections.singletonList(new EquivalentAddressGroup(new InetSocketAddress(uri.getHost(), uri.getPort()))), Attributes.EMPTY);
                        }
                    } catch (final URISyntaxException ex) {
                        throw new RegException("Illegal endpoint, %s", ex.getMessage());
                    }
                }
            }
            
            @Override
            public void shutdown() {
                if (!shutdown) {
                    shutdown = true;
                    executor = SharedResourceHolder.release(GrpcUtil.SHARED_CHANNEL_EXECUTOR, executor);
                }
            }
        };
    }
    
    @Override
    public String getDefaultScheme() {
        return scheme;
    }
}

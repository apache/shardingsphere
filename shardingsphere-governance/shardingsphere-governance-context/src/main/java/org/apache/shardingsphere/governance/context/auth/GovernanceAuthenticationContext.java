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

package org.apache.shardingsphere.governance.context.auth;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.context.auth.AuthenticationContext;
import org.apache.shardingsphere.infra.context.auth.impl.StandardAuthenticationContext;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

/**
 * Governance authentication context.
 */
public final class GovernanceAuthenticationContext implements AuthenticationContext {
    
    private volatile AuthenticationContext authenticationContext;
    
    public GovernanceAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public Authentication getAuthentication() {
        return authenticationContext.getAuthentication();
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        authenticationContext = new StandardAuthenticationContext(event.getAuthentication());
    }
}

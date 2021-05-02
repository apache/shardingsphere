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

package org.apache.shardingsphere.governance.context.authority;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.governance.context.authority.listener.event.AuthorityChangedEvent;
import org.apache.shardingsphere.infra.context.metadata.MetaDataAwareEventSubscriber;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Optional;

/**
 * Governance authority context.
 */
@Setter
public final class GovernanceAuthorityContext implements MetaDataAwareEventSubscriber {
    
    private volatile MetaDataContexts metaDataContexts;
    
    /**
     * Renew authority.
     *
     * @param event authority changed event
     */
    @Subscribe
    public synchronized void renew(final AuthorityChangedEvent event) {
        reloadAuthority(event.getUsers());
    }
    
    private void reloadAuthority(final Collection<ShardingSphereUser> users) {
        Optional<AuthorityRule> authorityRule = metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).findAny().map(each -> (AuthorityRule) each);
        Preconditions.checkState(authorityRule.isPresent());
        authorityRule.get().refresh(metaDataContexts.getMetaDataMap(), users);
    }
}

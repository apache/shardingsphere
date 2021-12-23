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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class ShowAuthorityRuleExecutor extends AbstractShowExecutor {

    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", "users", "users", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false),
                new QueryHeader("", "", "provider", "provider", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false),
                new QueryHeader("", "", "props", "props", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        Optional<AuthorityRuleConfiguration> authorityRuleConfigurationOptional = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(AuthorityRuleConfiguration.class).stream().findFirst();
        if (!authorityRuleConfigurationOptional.isPresent()) {
            return new MultipleLocalDataMergedResult(Collections.emptyList());
        }
        AuthorityRuleConfiguration authorityRuleConfiguration = authorityRuleConfigurationOptional.get();
        List<Object> row = new LinkedList<>();
        row.add(authorityRuleConfiguration.getUsers().stream().map(each -> each.getGrantee() + ":" + each.getPassword()).collect(Collectors.joining("; ")));
        row.add(authorityRuleConfiguration.getProvider().getType());
        row.add(authorityRuleConfiguration.getProvider().getProps().size() == 0 ? "" : authorityRuleConfiguration.getProvider().getProps());
        LinkedList<List<Object>> rows = new LinkedList<>();
        rows.add(row);
        return new MultipleLocalDataMergedResult(rows);
    }
}

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

package org.apache.shardingsphere.shardingproxy.backend.communication.merge;

import org.apache.shardingsphere.core.merge.MergeEntry;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.util.Collection;
import java.util.List;

/**
 * Proxy merge entry.
 *
 * @author zhangliang
 */
public final class ProxyMergeEntry extends MergeEntry {
    
    private final List<QueryHeader> queryHeaders;
    
    public ProxyMergeEntry(final DatabaseType databaseType, final RelationMetas relationMetas, 
                           final Collection<BaseRule> rules, final SQLRouteResult routeResult, final boolean queryWithCipherColumn, final List<QueryHeader> queryHeaders) {
        super(databaseType, relationMetas, rules, routeResult, queryWithCipherColumn);
        this.queryHeaders = queryHeaders;
    }
    
    @Override
    protected EncryptorMetaData createEncryptorMetaData(final EncryptRule encryptRule) {
        return new QueryHeaderEncryptorMetaData(encryptRule, queryHeaders);
    }
}

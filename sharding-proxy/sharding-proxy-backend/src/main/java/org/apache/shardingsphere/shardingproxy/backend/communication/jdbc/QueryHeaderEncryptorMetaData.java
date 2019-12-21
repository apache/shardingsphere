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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.util.List;

/**
 * Encryptor meta data for query header.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class QueryHeaderEncryptorMetaData implements EncryptorMetaData {
    
    private final EncryptRule encryptRule;
    
    private final List<QueryHeader> queryHeaders;
    
    @Override
    public Optional<ShardingEncryptor> findEncryptor(final int columnIndex) {
        QueryHeader queryHeader = queryHeaders.get(columnIndex - 1);
        String tableName = queryHeader.getTable();
        String columnName = queryHeader.getColumnName();
        return encryptRule.isCipherColumn(tableName, columnName) ? encryptRule.findShardingEncryptor(tableName, encryptRule.getLogicColumnOfCipher(tableName, columnName))
                : Optional.<ShardingEncryptor>absent();
    }
    
}

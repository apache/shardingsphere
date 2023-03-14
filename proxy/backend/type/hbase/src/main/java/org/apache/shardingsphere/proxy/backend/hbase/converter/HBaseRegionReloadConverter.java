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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Operation;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * HBase database region reload converter.
 */
@RequiredArgsConstructor
public final class HBaseRegionReloadConverter implements HBaseDatabaseConverter {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private Operation getOperation() {
        return new Operation() {
            
            @Override
            public Map<String, Object> getFingerprint() {
                return new TreeMap<>();
            }
            
            @Override
            public Map<String, Object> toMap(final int i) {
                return new TreeMap<>();
            }
        };
    }
    
    @Override
    public HBaseOperation convert() {
        List<String> tables = ((FlushStatementContext) sqlStatementContext).getAllTables()
                .stream().map(simpleTableSegment -> simpleTableSegment.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        
        return new HBaseOperation(String.join(",", tables), getOperation());
    }
}

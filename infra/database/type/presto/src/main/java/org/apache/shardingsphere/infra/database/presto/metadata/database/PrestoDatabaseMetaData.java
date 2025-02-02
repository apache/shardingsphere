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

package org.apache.shardingsphere.infra.database.presto.metadata.database;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;

import java.sql.Types;
import java.util.Map;
import java.util.Optional;

/**
 * Database meta data of Presto.
 */
public final class PrestoDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.LOW;
    }
    
    @Override
    public Optional<String> getDefaultSchema() {
        return Optional.of("default");
    }
    
    /**
     * TODO For prestodb/presto 0.290,
     *  the `DATA_TYPE` column of the `INFORMATION_SCHEMA.COLUMNS` table of `Memory` catalog only records strings like `varchar(50)`,
     *  which is expected to have potential optimizations on ShardingSphere side.
     *
     * @return Extra data types
     */
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        result.put("varchar(50)", Types.VARCHAR);
        result.put("varchar(100)", Types.VARCHAR);
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "Presto";
    }
}

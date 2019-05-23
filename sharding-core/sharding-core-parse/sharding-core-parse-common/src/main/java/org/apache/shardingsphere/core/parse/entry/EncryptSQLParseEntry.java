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

package org.apache.shardingsphere.core.parse.entry;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * SQL parse entry for encrypt.
 *
 * @author panjuan
 */
public final class EncryptSQLParseEntry extends SQLParseEntry {
    
    private final DatabaseType dbType;
    
    private final EncryptRule encryptRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    public EncryptSQLParseEntry(final DatabaseType dbType, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        super(new ParsingResultCache());
        this.dbType = dbType;
        this.encryptRule = encryptRule;
        this.shardingTableMetaData = shardingTableMetaData;
    }
    
    @Override
    protected SQLParseEngine getSQLParseEngine(final String sql) {
        return new SQLParseEngine(dbType, sql, encryptRule, shardingTableMetaData);
    }
}

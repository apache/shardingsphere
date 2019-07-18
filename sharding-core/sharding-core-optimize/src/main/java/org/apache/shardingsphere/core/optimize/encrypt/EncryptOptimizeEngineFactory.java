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

package org.apache.shardingsphere.core.optimize.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptWhereOptimizeEngine;
import org.apache.shardingsphere.core.optimize.transparent.engine.TransparentOptimizeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.List;

/**
 * Optimize engine factory for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptOptimizeEngineFactory {
    
    /**
     * Create encrypt optimize engine instance.
     * 
     * @param encryptRule encrypt rule
     * @param shardingTableMetaData sharding table metadata
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final List<Object> parameters) {
        if (sqlStatement instanceof InsertStatement) {
            return new EncryptInsertOptimizeEngine(encryptRule, shardingTableMetaData, (InsertStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof SelectStatement || sqlStatement instanceof UpdateStatement || sqlStatement instanceof DeleteStatement) {
            return new EncryptWhereOptimizeEngine(encryptRule, shardingTableMetaData, sqlStatement);
        }
        return new TransparentOptimizeEngine(sqlStatement);
    }
}

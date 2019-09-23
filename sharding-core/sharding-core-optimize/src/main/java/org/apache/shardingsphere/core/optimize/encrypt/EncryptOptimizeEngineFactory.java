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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptWhereOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptTransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
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
     * @param tableMetas table meta data
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatement SQL statement
     * @return sharding optimize engine instance
     */
    public static EncryptOptimizedStatement newInstance(final EncryptRule encryptRule, final TableMetas tableMetas, final String sql, final List<Object> parameters, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement || sqlStatement instanceof UpdateStatement || sqlStatement instanceof DeleteStatement) {
            return new EncryptWhereOptimizeEngine().optimize(encryptRule, tableMetas, sql, parameters, (DMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new InsertOptimizedStatement(tableMetas, parameters, (InsertStatement) sqlStatement);
        }
        return new EncryptTransparentOptimizedStatement(sqlStatement);
    }
}

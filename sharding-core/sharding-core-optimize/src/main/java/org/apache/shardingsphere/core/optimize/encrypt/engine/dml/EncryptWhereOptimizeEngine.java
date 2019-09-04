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

package org.apache.shardingsphere.core.optimize.encrypt.engine.dml;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.encrypt.condition.engine.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptConditionOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.List;

/**
 * Where optimize engine for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptWhereOptimizeEngine implements EncryptOptimizeEngine<DMLStatement> {
    
    @Override
    public EncryptConditionOptimizedStatement optimize(final EncryptRule encryptRule, final TableMetas tableMetas, final String sql, final List<Object> parameters, final DMLStatement sqlStatement) {
        WhereClauseEncryptConditionEngine encryptConditionEngine = new WhereClauseEncryptConditionEngine(encryptRule, tableMetas);
        return new EncryptConditionOptimizedStatement(sqlStatement, new EncryptConditions(encryptConditionEngine.createEncryptConditions(sqlStatement)));
    }
}

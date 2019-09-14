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

package org.apache.shardingsphere.core.optimize.encrypt.statement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;

import java.util.List;

/**
 * Insert optimized statement for encrypt.
 *
 * @author zhangliang
 */
@Getter
@ToString(exclude = "sqlStatement")
public final class EncryptInsertOptimizedStatement implements InsertOptimizedStatement, EncryptOptimizedStatement {
    
    @Getter(AccessLevel.NONE)
    private final SQLStatement sqlStatement;
    
    private final Tables tables;
    
    private final List<String> columnNames;
    
    private final List<InsertValue> insertValues;
    
    public EncryptInsertOptimizedStatement(final InsertStatement sqlStatement, final TableMetas tableMetas, final List<InsertValue> insertValues) {
        this.sqlStatement = sqlStatement;
        tables = new Tables(sqlStatement);
        columnNames = sqlStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(sqlStatement.getTable().getTableName()) : sqlStatement.getColumnNames();
        this.insertValues = insertValues;
    }
    
    @Override
    public SQLStatement getSQLStatement() {
        return sqlStatement;
    }
}

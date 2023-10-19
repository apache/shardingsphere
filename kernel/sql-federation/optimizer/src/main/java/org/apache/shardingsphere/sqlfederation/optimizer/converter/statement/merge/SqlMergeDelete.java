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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.merge;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.parser.SqlParserPos;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Sql merge delete.
 */
public class SqlMergeDelete extends SqlCall {
    
    public static final SqlSpecialOperator OPERATOR;
    
    private SqlNode targetTable;
    
    @Nullable
    private SqlNode condition;
    
    @Nullable
    private SqlSelect sourceSelect;
    
    @Nullable
    private SqlIdentifier alias;
    
    public SqlMergeDelete(final SqlParserPos pos, final SqlNode targetTable, final @Nullable SqlNode condition, final @Nullable SqlSelect sourceSelect, final @Nullable SqlIdentifier alias) {
        super(pos);
        this.targetTable = targetTable;
        this.condition = condition;
        this.sourceSelect = sourceSelect;
        this.alias = alias;
    }
    
    public SqlKind getKind() {
        return SqlKind.DELETE;
    }
    
    public SqlOperator getOperator() {
        return OPERATOR;
    }
    
    @Override
    public List<SqlNode> getOperandList() {
        return null;
    }
    
    @Override
    public void setOperand(final int i, final @Nullable SqlNode operand) {
        switch (i) {
            case 0:
                this.targetTable = operand;
                break;
            case 1:
                this.condition = operand;
                break;
            case 2:
                this.sourceSelect = (SqlSelect) operand;
                break;
            case 3:
                this.alias = (SqlIdentifier) operand;
                break;
            default:
                throw new AssertionError(i);
        }
        
    }
    
    public SqlNode getTargetTable() {
        return this.targetTable;
    }
    
    public @Nullable SqlIdentifier getAlias() {
        return this.alias;
    }
    
    public @Nullable SqlNode getCondition() {
        return this.condition;
    }
    
    public @Nullable SqlSelect getSourceSelect() {
        return this.sourceSelect;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int leftPrec, final int rightPrec) {
        SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.SELECT, "DELETE", "");
        int opLeft = this.getOperator().getLeftPrec();
        int opRight = this.getOperator().getRightPrec();
        SqlIdentifier alias = this.alias;
        if (alias != null) {
            writer.keyword("AS");
            alias.unparse(writer, opLeft, opRight);
        }
        SqlNode condition = this.condition;
        if (condition != null) {
            writer.sep("WHERE");
            condition.unparse(writer, opLeft, opRight);
        }
        writer.endList(frame);
    }
    
    public void setSourceSelect(final SqlSelect sourceSelect) {
        this.sourceSelect = sourceSelect;
    }
    
    static {
        OPERATOR = new SqlSpecialOperator("DELETE", SqlKind.DELETE);
    }
}

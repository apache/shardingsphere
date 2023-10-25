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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.update;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.SqlWriter.Frame;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import java.util.Iterator;
import java.util.List;

public class CustomSqlUpdate extends SqlCall {
    
    public static final SqlSpecialOperator OPERATOR;
    
    private SqlNode targetTable;
    
    private SqlNodeList targetColumnList;
    
    private SqlNodeList sourceExpressionList;
    
    @Nullable
    private SqlNode condition;
    
    @Nullable
    private SqlSelect sourceSelect;
    
    @Nullable
    private SqlIdentifier alias;
    
    @Nullable
    private SqlNode from;
    
    public CustomSqlUpdate(final SqlParserPos pos, final SqlNode targetTable, final SqlNodeList targetColumnList, final SqlNodeList sourceExpressionList, final @Nullable SqlNode condition,
                           final @Nullable SqlSelect sourceSelect, final @Nullable SqlIdentifier alias, final @Nullable SqlNode from) {
        super(pos);
        this.targetTable = targetTable;
        this.targetColumnList = targetColumnList;
        this.sourceExpressionList = sourceExpressionList;
        this.condition = condition;
        this.sourceSelect = sourceSelect;
        this.from = from;
        assert sourceExpressionList.size() == targetColumnList.size();
        
        this.alias = alias;
    }
    
    public SqlKind getKind() {
        return SqlKind.UPDATE;
    }
    
    public SqlOperator getOperator() {
        return OPERATOR;
    }
    
    public List<@Nullable SqlNode> getOperandList() {
        return ImmutableNullableList.of(this.targetTable, this.targetColumnList, this.sourceExpressionList, this.condition, this.alias, this.from);
    }
    
    @Override
    public void setOperand(final int i, final @Nullable SqlNode operand) {
        switch (i) {
            case 0:
                assert operand instanceof SqlIdentifier;
                
                this.targetTable = operand;
                break;
            case 1:
                this.targetColumnList = (SqlNodeList) operand;
                break;
            case 2:
                this.sourceExpressionList = (SqlNodeList) operand;
                break;
            case 3:
                this.condition = operand;
                break;
            case 4:
                this.sourceExpressionList = (SqlNodeList) operand;
                break;
            case 5:
                this.alias = (SqlIdentifier) operand;
                break;
            case 6:
                this.from = operand;
                break;
            default:
                throw new AssertionError(i);
        }
        
    }
    
    public SqlNode getTargetTable() {
        return this.targetTable;
    }
    
    @Pure
    public final @Nullable SqlNode getFrom() {
        return this.from;
    }
    
    public void setFrom(final @Nullable SqlNode from) {
        this.from = from;
    }
    
    @Pure
    public @Nullable SqlIdentifier getAlias() {
        return this.alias;
    }
    
    public void setAlias(final SqlIdentifier alias) {
        this.alias = alias;
    }
    
    public SqlNodeList getTargetColumnList() {
        return this.targetColumnList;
    }
    
    public SqlNodeList getSourceExpressionList() {
        return this.sourceExpressionList;
    }
    
    public @Nullable SqlNode getCondition() {
        return this.condition;
    }
    
    public @Nullable SqlSelect getSourceSelect() {
        return this.sourceSelect;
    }
    
    public void setSourceSelect(final SqlSelect sourceSelect) {
        this.sourceSelect = sourceSelect;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int leftPrec, final int rightPrec) {
        final Frame frame = writer.startList(SqlWriter.FrameTypeEnum.SELECT, "UPDATE", "");
        int opLeft = this.getOperator().getLeftPrec();
        int opRight = this.getOperator().getRightPrec();
        this.targetTable.unparse(writer, opLeft, opRight);
        SqlIdentifier alias = this.alias;
        if (alias != null) {
            writer.keyword("AS");
            alias.unparse(writer, opLeft, opRight);
        }
        SqlWriter.Frame setFrame = writer.startList(SqlWriter.FrameTypeEnum.UPDATE_SET_LIST, "SET", "");
        Iterator var9 = Pair.zip(this.getTargetColumnList(), this.getSourceExpressionList()).iterator();
        while (var9.hasNext()) {
            Pair<SqlNode, SqlNode> pair = (Pair) var9.next();
            writer.sep(",");
            SqlIdentifier id = (SqlIdentifier) pair.left;
            id.unparse(writer, opLeft, opRight);
            writer.keyword("=");
            SqlNode sourceExp = (SqlNode) pair.right;
            sourceExp.unparse(writer, opLeft, opRight);
        }
        writer.endList(setFrame);
        SqlNode from = this.from;
        if (from != null) {
            writer.sep("FROM");
            from.unparse(writer, opLeft, opRight);
        }
        SqlNode condition = this.condition;
        if (condition != null) {
            writer.sep("WHERE");
            condition.unparse(writer, opLeft, opRight);
        }
        writer.endList(frame);
    }
    
    static {
        OPERATOR = new SqlSpecialOperator("UPDATE", SqlKind.UPDATE);
    }
}

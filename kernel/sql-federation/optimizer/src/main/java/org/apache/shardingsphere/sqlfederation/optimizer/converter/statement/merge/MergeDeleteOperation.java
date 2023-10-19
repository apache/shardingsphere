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
import org.apache.calcite.sql.SqlMerge;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.util.SqlVisitor;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorScope;
import org.apache.calcite.util.Litmus;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Merge delete operation.
 */
public class MergeDeleteOperation extends SqlCall {
    
    private final SqlMerge merge;
    
    private final SqlMergeDelete delete;
    
    public MergeDeleteOperation(final SqlParserPos pos, final SqlMerge merge, final SqlMergeDelete delete) {
        super(pos);
        this.merge = merge;
        this.delete = delete;
    }
    
    @Override
    public SqlOperator getOperator() {
        return null;
    }
    
    @Override
    public List<SqlNode> getOperandList() {
        return null;
    }
    
    @Override
    public SqlNode clone(final SqlParserPos sqlParserPos) {
        return null;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int i, final int i1) {
        merge.unparse(writer, i, i1);
        writer.newlineAndIndent();
        delete.unparse(writer, i, i1);
    }
    
    @Override
    public void validate(final SqlValidator sqlValidator, final SqlValidatorScope sqlValidatorScope) {
        
    }
    
    @Override
    public <R> R accept(final SqlVisitor<R> sqlVisitor) {
        return null;
    }
    
    @Override
    public boolean equalsDeep(final @Nullable SqlNode sqlNode, final Litmus litmus) {
        return false;
    }
}

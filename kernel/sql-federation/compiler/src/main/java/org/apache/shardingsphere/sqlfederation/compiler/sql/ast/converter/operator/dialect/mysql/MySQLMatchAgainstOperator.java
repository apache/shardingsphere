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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.dialect.mysql;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperandCountRange;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.SqlWriter.Frame;
import org.apache.calcite.sql.SqlWriter.FrameType;
import org.apache.calcite.sql.SqlWriter.FrameTypeEnum;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlOperandCountRanges;
import org.apache.calcite.util.Util;

import java.util.List;

/**
 * MySQL match against operator.
 */
public final class MySQLMatchAgainstOperator extends SqlFunction {
    
    private static final FrameType FRAME_TYPE = FrameTypeEnum.create("MATCH");
    
    public MySQLMatchAgainstOperator() {
        super("MATCH_AGAINST", SqlKind.OTHER_FUNCTION, ReturnTypes.DOUBLE, InferTypes.FIRST_KNOWN, OperandTypes.ANY, SqlFunctionCategory.STRING);
    }
    
    @Override
    public void unparse(final SqlWriter writer, final SqlCall call, final int leftPrecedence, final int rightPrecedence) {
        Frame frame = writer.startList(FRAME_TYPE, "MATCH", "");
        writeParameters(writer, call);
        writer.endList(frame);
    }
    
    private void writeParameters(final SqlWriter writer, final SqlCall call) {
        writer.sep("(");
        List<SqlNode> operandList = call.getOperandList();
        int size = operandList.size();
        for (int i = 0; i < size - 2; i++) {
            operandList.get(i).unparse(writer, 0, 0);
            if (i != size - 3) {
                writer.sep(",");
            }
        }
        writer.sep(")");
        writer.sep("AGAINST");
        writer.sep("(");
        operandList.get(size - 2).unparse(writer, 0, 0);
        String searchModifier = ((SqlLiteral) Util.last(operandList)).toValue();
        writer.sep(searchModifier);
        writer.sep(")");
    }
    
    @Override
    public SqlOperandCountRange getOperandCountRange() {
        return SqlOperandCountRanges.any();
    }
    
    @Override
    public SqlSyntax getSyntax() {
        return SqlSyntax.SPECIAL;
    }
}

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

package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.executor.exec.func.FunctionIdentity;
import org.apache.shardingsphere.infra.executor.exec.func.binary.BinaryBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.binary.equals.BigDecimalEqualsBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.binary.equals.EqualsBuiltInFunction;
import org.apache.shardingsphere.infra.executor.exec.tool.RelDataType2JavaTypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Implementor for binary operators. The code of this class is translated the Calcite Java code generator class 
 * <code>org.apache.calcite.adapter.enumerable.RexImpTable.BinaryImplementor</code> which is responsible for Java code 
 * generation of calcite built-in execution phase. 
 */
public class BinaryFunctionImplementor extends AbstractFunctionImplementor<RexCall, BinaryBuiltinFunction> {
    
    /** Types that can be arguments to comparison operators such as {@code <}. */
    private static final List<Primitive> COMP_OP_TYPES =
            ImmutableList.of(
                    Primitive.BYTE,
                    Primitive.CHAR,
                    Primitive.SHORT,
                    Primitive.INT,
                    Primitive.LONG,
                    Primitive.FLOAT,
                    Primitive.DOUBLE);
    
    private static final List<SqlBinaryOperator> COMPARISON_OPERATORS =
            ImmutableList.of(
                    SqlStdOperatorTable.LESS_THAN,
                    SqlStdOperatorTable.LESS_THAN_OR_EQUAL,
                    SqlStdOperatorTable.GREATER_THAN,
                    SqlStdOperatorTable.GREATER_THAN_OR_EQUAL);
    
    private static final List<SqlBinaryOperator> EQUALS_OPERATORS =
            ImmutableList.of(
                    SqlStdOperatorTable.EQUALS,
                    SqlStdOperatorTable.NOT_EQUALS);

    private final String backupMethodName;
    
    public BinaryFunctionImplementor(final NullPolicy nullPolicy, final String backupMethodName) {
        super(nullPolicy);
        this.backupMethodName = backupMethodName;
        
        registerFunction(EqualsBuiltInFunction.INSTANCE);
        registerFunction(BigDecimalEqualsBuiltinFunction.INSTANCE);
    }
    
    /**
     * build function from {@link RexCall}. 
     * @param rexCall <code>RexCode</code> to be implemented
     * @param argTypes argument type for <code>BinaryBuiltinFunction</code>  
     * @return the <code>BinaryBuiltinFunction</code> instance
     */
    public BinaryBuiltinFunction implement(final RexCall rexCall, final RelDataType[] argTypes) {
        if (argTypes.length != 2) {
            throw new IllegalArgumentException();
        }
        if (backupMethodName != null) {
            // If one or both operands have ANY type, use the late-binding backup
            // method.
            if (anyAnyOperands(rexCall)) {
                // TODO use SqlFunctions.eqAny to implement EqualsAnyBuiltinFunction
                return null;
            }
            
            final Type type0 = RelDataType2JavaTypeUtils.getJavaClass(argTypes[0]);
            final Type type1 = RelDataType2JavaTypeUtils.getJavaClass(argTypes[1]);
            final SqlBinaryOperator op = (SqlBinaryOperator) rexCall.getOperator();
            final RelDataType relDataType0 = rexCall.getOperands().get(0).getType();
    
            FunctionIdentity functionIdentity = new FunctionIdentity(op.getKind().name(), Arrays.asList(type0.getTypeName(), type1.getTypeName()));
            BinaryBuiltinFunction builtinFunction = (BinaryBuiltinFunction) getFunctionMap().get(functionIdentity);
            
            /*final Expression fieldComparator = generateCollatorExpression(relDataType0.getCollation());
            if (fieldComparator != null) {
                argValueList.add(fieldComparator);
            }*/
            // TODO get a built-in binary function instance with type and 
            final Primitive primitive = Primitive.ofBoxOr(type0);
            if (primitive == null
                    || type1 == BigDecimal.class
                    || COMPARISON_OPERATORS.contains(op)
                    && !COMP_OP_TYPES.contains(primitive)) {
                return builtinFunction;
            }
            // When checking equals or not equals on two primitive boxing classes
            // (i.e. Long x, Long y), we should fall back to call `SqlFunctions.eq(x, y)`
            // or `SqlFunctions.ne(x, y)`, rather than `x == y`
            final Primitive boxPrimitive0 = Primitive.ofBox(type0);
            final Primitive boxPrimitive1 = Primitive.ofBox(type1);
            if (EQUALS_OPERATORS.contains(op)
                    && boxPrimitive0 != null && boxPrimitive1 != null) {
                // TODO 
                return null;
            }
            return builtinFunction;
            // TODO set nullPolicy to Binary function 
        }
        return null;
        /*return Expressions.makeBinary(expressionType,
                argValueList.get(0), argValueList.get(1));*/
    }
    
    /** Returns whether any of a call's operands have ANY type. */
    private boolean anyAnyOperands(final RexCall call) {
        for (RexNode operand : call.operands) {
            if (operand.getType().getSqlTypeName() == SqlTypeName.ANY) {
                return true;
            }
        }
        return false;
    }
}

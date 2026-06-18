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

package org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl;

import org.apache.calcite.sql.SqlAbstractDateTimeLiteral;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.SqlWriter.FrameTypeEnum;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.TimestampWithTimeZoneString;
import org.apache.calcite.util.Util;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

/**
 * Custom MySQL SQL dialect.
 */
public final class CustomMySQLSQLDialect extends MysqlSqlDialect {
    
    public static final SqlDialect DEFAULT = new CustomMySQLSQLDialect(DEFAULT_CONTEXT);
    
    public CustomMySQLSQLDialect(final Context context) {
        super(context);
    }
    
    @Override
    public void quoteStringLiteral(final StringBuilder builder, final String charsetName, final String value) {
        builder.append(literalQuoteString);
        builder.append(value.replace(literalEndQuoteString, literalEscapedQuote));
        builder.append(literalEndQuoteString);
    }
    
    @Override
    public void unparseCall(final SqlWriter writer, final SqlCall call, final int leftPrec, final int rightPrec) {
        if (SqlKind.CAST == call.getOperator().getKind()) {
            SqlNode parameter1 = call.getOperandList().get(0);
            SqlNode parameter2 = call.getOperandList().get(1);
            String typeName = parameter2 instanceof SqlDataTypeSpec ? Util.last(((SqlDataTypeSpec) parameter2).getTypeName().names) : null;
            if (SqlTypeName.DOUBLE.getName().equals(typeName)) {
                unparseCastDouble(writer, parameter1, typeName);
                return;
            }
        }
        super.unparseCall(writer, call, leftPrec, rightPrec);
    }
    
    private void unparseCastDouble(final SqlWriter writer, final SqlNode parameter1, final String typeName) {
        writer.keyword("CAST");
        parameter1.unparse(writer, 0, 0);
        writer.sep("AS");
        writer.keyword(typeName);
        writer.endList(writer.startList(FrameTypeEnum.FUN_CALL, "(", ")"));
    }
    
    @Override
    public void unparseDateTimeLiteral(final SqlWriter writer, final SqlAbstractDateTimeLiteral literal, final int leftPrec, final int rightPrec) {
        if (SqlTypeName.TIMESTAMP_TZ == literal.getTypeName()) {
            writer.literal(SqlTypeName.TIMESTAMP.getName() + " '" + toFormattedString(literal) + "'");
        } else {
            super.unparseDateTimeLiteral(writer, literal, leftPrec, rightPrec);
        }
    }
    
    private String toFormattedString(final SqlAbstractDateTimeLiteral literal) {
        TimestampWithTimeZoneString timestampWithTimeZone = (TimestampWithTimeZoneString) literal.getValue();
        int precision = literal.getPrec();
        if (precision > 0) {
            timestampWithTimeZone = timestampWithTimeZone.round(precision);
        }
        ShardingSpherePreconditions.checkState(precision >= 0, () -> new IllegalArgumentException("The precision of timestamp with time zone must be non-negative."));
        return timestampWithTimeZone.getLocalTimestampString().toString(precision);
    }
}

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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.aggregate.FirebirdAVGReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.aggregate.FirebirdMinMaxReturnTypeConverter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.aggregate.FirebirdSUMReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.bitwise.FirebirdLogicalReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.bitwise.FirebirdNotReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.bitwise.FirebirdShiftReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.cast.FirebirdCastReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.conditional.FirebirdConditionalReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.cryptographic.FirebirdEncodeDecodeReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.datetime.FirebirdDateTimeReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.mathematical.FirebirdABSReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.mathematical.FirebirdCeilFloorReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.mathematical.FirebirdModReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.mathematical.FirebirdRoundTruncReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.other.FirebirdRDBErrorReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.statistical.FirebirdStatisticalReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.string.FirebirdLengthReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.string.FirebirdUpperLowerReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.string.FirebirdVarcharOrBlobReturnTypeHandler;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;

import java.util.Collection;

/**
 * Firebird function return type.
 */
@Getter
public enum FirebirdFunctionType {
    
    // Scalar Functions
    RDB$GET_CONTEXT(FirebirdBinaryColumnType.VARYING, 255),
    RDB$RESET_CONTEXT(FirebirdBinaryColumnType.LONG),
    RDB$SET_CONTEXT(FirebirdBinaryColumnType.LONG),
    
    // Mathematical Functions
    ABS(new FirebirdABSReturnTypeHandler()),
    ACOS(FirebirdBinaryColumnType.DOUBLE),
    ACOSH(FirebirdBinaryColumnType.DOUBLE),
    ASIN(FirebirdBinaryColumnType.DOUBLE),
    ASINH(FirebirdBinaryColumnType.DOUBLE),
    ATAN(FirebirdBinaryColumnType.DOUBLE),
    ATAN2(FirebirdBinaryColumnType.DOUBLE),
    ATANH(FirebirdBinaryColumnType.DOUBLE),
    CEIL(new FirebirdCeilFloorReturnTypeHandler()),
    CEILING(new FirebirdCeilFloorReturnTypeHandler()),
    COS(FirebirdBinaryColumnType.DOUBLE),
    COSH(FirebirdBinaryColumnType.DOUBLE),
    COT(FirebirdBinaryColumnType.DOUBLE),
    EXP(FirebirdBinaryColumnType.DOUBLE),
    FLOOR(new FirebirdCeilFloorReturnTypeHandler()),
    LN(FirebirdBinaryColumnType.DOUBLE),
    LOG(FirebirdBinaryColumnType.DOUBLE),
    LOG10(FirebirdBinaryColumnType.DOUBLE),
    MOD(new FirebirdModReturnTypeHandler()),
    PI(FirebirdBinaryColumnType.DOUBLE),
    POWER(FirebirdBinaryColumnType.DOUBLE),
    RAND(FirebirdBinaryColumnType.DOUBLE),
    ROUND(new FirebirdRoundTruncReturnTypeHandler()),
    SIGN(FirebirdBinaryColumnType.SHORT),
    SIN(FirebirdBinaryColumnType.DOUBLE),
    SINH(FirebirdBinaryColumnType.DOUBLE),
    SQRT(FirebirdBinaryColumnType.DOUBLE),
    TAN(FirebirdBinaryColumnType.DOUBLE),
    TANH(FirebirdBinaryColumnType.DOUBLE),
    TRUNC(new FirebirdRoundTruncReturnTypeHandler()),
    
    // String and Binary Functions
    ASCII_CHAR(FirebirdBinaryColumnType.VARYING),
    ASCII_VAL(FirebirdBinaryColumnType.SHORT),
    BASE64_DECODE(new FirebirdVarcharOrBlobReturnTypeHandler()),
    BASE64_ENCODE(new FirebirdVarcharOrBlobReturnTypeHandler()),
    BIT_LENGTH(new FirebirdLengthReturnTypeHandler()),
    BLOB_APPEND(FirebirdBinaryColumnType.BLOB),
    CHAR_LENGTH(new FirebirdLengthReturnTypeHandler()),
    CHARACTER_LENGTH(new FirebirdLengthReturnTypeHandler()),
    CRYPT_HASH(FirebirdBinaryColumnType.VARYING),
    HASH(FirebirdBinaryColumnType.INT64),
    HEX_DECODE(new FirebirdVarcharOrBlobReturnTypeHandler()),
    HEX_ENCODE(new FirebirdVarcharOrBlobReturnTypeHandler()),
    LEFT(new FirebirdVarcharOrBlobReturnTypeHandler()),
    LOWER(new FirebirdUpperLowerReturnTypeHandler()),
    LPAD(new FirebirdVarcharOrBlobReturnTypeHandler()),
    OCTET_LENGTH(new FirebirdLengthReturnTypeHandler()),
    OVERLAY(new FirebirdVarcharOrBlobReturnTypeHandler()),
    POSITION(FirebirdBinaryColumnType.LONG),
    REPLACE(new FirebirdVarcharOrBlobReturnTypeHandler()),
    REVERSE(FirebirdBinaryColumnType.VARYING),
    RIGHT(new FirebirdVarcharOrBlobReturnTypeHandler()),
    RPAD(new FirebirdVarcharOrBlobReturnTypeHandler()),
    SUBSTRING(new FirebirdVarcharOrBlobReturnTypeHandler()),
    TRIM(new FirebirdVarcharOrBlobReturnTypeHandler()),
    UNICODE_CHAR(FirebirdBinaryColumnType.VARYING),
    UNICODE_VAL(FirebirdBinaryColumnType.LONG),
    UPPER(new FirebirdUpperLowerReturnTypeHandler()),
    
    // Date and Time Functions
    CURRENT_DATE(FirebirdBinaryColumnType.DATE),
    CURRENT_TIME(FirebirdBinaryColumnType.TIME),
    CURRENT_TIMESTAMP(FirebirdBinaryColumnType.TIMESTAMP),
    DATEADD(new FirebirdDateTimeReturnTypeHandler()),
    DATEDIFF(FirebirdBinaryColumnType.INT64),
    EXTRACT(FirebirdBinaryColumnType.SHORT),
    FIRST_DAY(FirebirdBinaryColumnType.DATE),
    LAST_DAY(FirebirdBinaryColumnType.DATE),
    LOCALTIME(FirebirdBinaryColumnType.TIME),
    LOCALTIMESTAMP(FirebirdBinaryColumnType.TIMESTAMP),
    
    // Type Casting Funtcions
    CAST(new FirebirdCastReturnTypeHandler()),
    
    // Bitwise Functions
    BIN_AND(new FirebirdLogicalReturnTypeHandler()),
    BIN_NOT(new FirebirdNotReturnTypeHandler()),
    BIN_OR(new FirebirdLogicalReturnTypeHandler()),
    BIN_SHL(new FirebirdShiftReturnTypeHandler()),
    BIN_SHR(new FirebirdShiftReturnTypeHandler()),
    BIN_XOR(new FirebirdLogicalReturnTypeHandler()),
    
    // UUID Functions
    CHAR_TO_UUID(FirebirdBinaryColumnType.TEXT, 16),
    GEN_UUID(FirebirdBinaryColumnType.TEXT, 16),
    UUID_TO_CHAR(FirebirdBinaryColumnType.VARYING, 36),
    
    // Functions for Sequences (Generators)
    GEN_ID(FirebirdBinaryColumnType.INT64),
    
    // Conditional Functions
    COALESCE(new FirebirdConditionalReturnTypeHandler()),
    DECODE(new FirebirdConditionalReturnTypeHandler()),
    IIF(new FirebirdConditionalReturnTypeHandler()),
    MAXVALUE(new FirebirdConditionalReturnTypeHandler()),
    MINVALUE(new FirebirdConditionalReturnTypeHandler()),
    NULLIF(new FirebirdConditionalReturnTypeHandler()),
    
    // Special Functions for DECFLOAT
    COMPARE_DECFLOAT(FirebirdBinaryColumnType.SHORT),
    NORMALIZE_DECFLOAT(FirebirdBinaryColumnType.DEC34),
    QUANTIZE(FirebirdBinaryColumnType.DEC34),
    TOTALORDER(FirebirdBinaryColumnType.SHORT),
    
    // Cryptographic Functions
    DECRYPT(new FirebirdEncodeDecodeReturnTypeHandler()),
    ENCRYPT(new FirebirdEncodeDecodeReturnTypeHandler()),
    RSA_DECRYPT(FirebirdBinaryColumnType.VARYING),
    RSA_ENCRYPT(FirebirdBinaryColumnType.VARYING),
    RSA_PRIVATE(FirebirdBinaryColumnType.VARYING),
    RSA_PUBLIC(FirebirdBinaryColumnType.VARYING),
    RSA_SIGN_HASH(FirebirdBinaryColumnType.VARYING),
    RSA_VERIFY_HASH(FirebirdBinaryColumnType.BOOLEAN),
    
    // Other Functions
    MAKE_DBKEY(FirebirdBinaryColumnType.TEXT, 8),
    RDB$ERROR(new FirebirdRDBErrorReturnTypeHandler()),
    RDB$GET_TRANSACTION_CN(FirebirdBinaryColumnType.INT64),
    RDB$ROLE_IN_USE(FirebirdBinaryColumnType.BOOLEAN),
    RDB$SYSTEM_PRIVILEGE(FirebirdBinaryColumnType.BOOLEAN),
    
    // General-purpose Aggregate Functions
    AVG(new FirebirdAVGReturnTypeHandler()),
    COUNT(FirebirdBinaryColumnType.INT64),
    LIST(FirebirdBinaryColumnType.BLOB),
    MAX(new FirebirdMinMaxReturnTypeConverter()),
    MIN(new FirebirdMinMaxReturnTypeConverter()),
    SUM(new FirebirdSUMReturnTypeHandler()),
    
    // Statistical Aggregate Functions
    CORR(FirebirdBinaryColumnType.DOUBLE),
    COVAR_POP(FirebirdBinaryColumnType.DOUBLE),
    COVAR_SAMP(FirebirdBinaryColumnType.DOUBLE),
    STDDEV_POP(new FirebirdStatisticalReturnTypeHandler()),
    STDDEV_SAMP(new FirebirdStatisticalReturnTypeHandler()),
    VAR_POP(new FirebirdStatisticalReturnTypeHandler()),
    VAR_SAMP(new FirebirdStatisticalReturnTypeHandler()),
    
    // Linear Regression Aggregate Functions
    REGR_AVGX(FirebirdBinaryColumnType.DOUBLE),
    REGR_AVGY(FirebirdBinaryColumnType.DOUBLE),
    REGR_COUNT(FirebirdBinaryColumnType.DOUBLE),
    REGR_INTERCEPT(FirebirdBinaryColumnType.DOUBLE),
    REGR_R2(FirebirdBinaryColumnType.DOUBLE),
    REGR_SLOPE(FirebirdBinaryColumnType.DOUBLE),
    REGR_SXX(FirebirdBinaryColumnType.DOUBLE),
    REGR_SXY(FirebirdBinaryColumnType.DOUBLE),
    REGR_SYY(FirebirdBinaryColumnType.DOUBLE),
    
    CURRENT_USER(FirebirdBinaryColumnType.VARYING),
    CURRENT_ROLE(FirebirdBinaryColumnType.VARYING);
    
    private final FirebirdBinaryColumnType type;
    
    private final int length;
    
    private final FirebirdFunctionReturnTypeHandler returnTypeHandler;
    
    FirebirdFunctionType(final FirebirdBinaryColumnType type) {
        this.type = type;
        this.length = -1;
        this.returnTypeHandler = null;
    }
    
    FirebirdFunctionType(final FirebirdBinaryColumnType type, final int length) {
        this.type = type;
        this.length = length;
        this.returnTypeHandler = null;
    }
    
    FirebirdFunctionType(final FirebirdFunctionReturnTypeHandler returnTypeHandler) {
        this.type = FirebirdBinaryColumnType.NULL;
        this.length = -1;
        this.returnTypeHandler = returnTypeHandler;
    }
    
    /**
     * Get return type.
     *
     * @param schema used schema
     * @param parameters function parameters
     * @return return type of function
     */
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        if (null == returnTypeHandler) {
            return type;
        }
        FirebirdBinaryColumnType type = returnTypeHandler.getReturnType(schema, parameters);
        Preconditions.checkNotNull(type, "Can not get return type of function `%s`", this.name());
        return type;
    }
    
    /**
     * Get return type.
     *
     * @param functionName function name
     * @param schema used schema
     * @param parameters function parameters
     * @return return type of function
     */
    public static FirebirdBinaryColumnType getReturnType(final String functionName, final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        try {
            return FirebirdFunctionType.valueOf(functionName.toUpperCase()).getReturnType(schema, parameters);
        } catch (final IllegalArgumentException | NullPointerException ignored) {
            return FirebirdBinaryColumnType.LONG;
        }
    }
}

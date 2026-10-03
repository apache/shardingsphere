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

package org.apache.shardingsphere.database.exception.firebird.mapper;

import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.database.exception.core.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.database.exception.firebird.exception.FirebirdException;
import org.apache.shardingsphere.database.exception.firebird.exception.FirebirdException.StatusVectorEntry;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchAlreadyOpenedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchDefaultBpbChangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchParametersRequiredException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchWithoutBlobsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.CannotUpdateOldBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ConversionErrorException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ExcessTransactionsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchBlobPolicyException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidClumpletStructureException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.MalformedStringException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.NumericOutOfRangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.database.exception.firebird.vendor.FirebirdVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Firebird dialect exception mapper.
 */
public final class FirebirdDialectExceptionMapper implements SQLDialectExceptionMapper {
    
    private static final StatusVectorEntry DYNAMIC_SQL_ERROR = new StatusVectorEntry(ISCConstants.isc_dsql_error, Collections.emptyList());
    
    private static final StatusVectorEntry BATCH_SQL_ERROR_CODE = new StatusVectorEntry(ISCConstants.isc_sqlerr, Collections.singletonList(-104));
    
    private static final StatusVectorEntry PARAMETER_CONVERSION_SQL_ERROR_CODE = new StatusVectorEntry(ISCConstants.isc_sqlerr, Collections.singletonList(-303));
    
    private static final StatusVectorEntry ARITHMETIC_EXCEPTION = new StatusVectorEntry(ISCConstants.isc_arith_except, Collections.emptyList());
    
    @Override
    public SQLException convert(final SQLDialectException sqlDialectException) {
        if (sqlDialectException instanceof UnknownDatabaseException) {
            return toSQLException(FirebirdVendorError.UNAVAILABLE_DATABASE, ((UnknownDatabaseException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof DatabaseCreateExistsException) {
            return toSQLException(FirebirdVendorError.DATABASE_ALREADY_EXISTS, ((DatabaseCreateExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof DatabaseDropNotExistsException) {
            return toSQLException(FirebirdVendorError.UNAVAILABLE_DATABASE, ((DatabaseDropNotExistsException) sqlDialectException).getDatabaseName());
        }
        if (sqlDialectException instanceof AccessDeniedException) {
            return toSQLException(FirebirdVendorError.LOGIN_FAILED);
        }
        if (sqlDialectException instanceof InvalidBatchHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_BATCH_HANDLE);
        }
        if (sqlDialectException instanceof BatchTooBigException) {
            return toSQLException(FirebirdVendorError.BATCH_TOO_BIG);
        }
        if (sqlDialectException instanceof BatchAlreadyOpenedException) {
            return toSQLException(FirebirdVendorError.BATCH_ALREADY_OPENED);
        }
        if (sqlDialectException instanceof InvalidBatchParameterVersionException) {
            InvalidBatchParameterVersionException ex = (InvalidBatchParameterVersionException) sqlDialectException;
            return toSQLException(FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, ex.getVersion(), ex.getExpectedVersion());
        }
        if (sqlDialectException instanceof BatchParametersRequiredException) {
            return toSQLException(FirebirdVendorError.BATCH_PARAMETERS_REQUIRED);
        }
        if (sqlDialectException instanceof InvalidBatchMessageFormatException) {
            return toSQLException(FirebirdVendorError.SQLDA_ERROR);
        }
        if (sqlDialectException instanceof InvalidStatementHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_STATEMENT_HANDLE);
        }
        if (sqlDialectException instanceof InvalidTransactionHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_TRANSACTION_HANDLE);
        }
        if (sqlDialectException instanceof ExcessTransactionsException) {
            return toSQLException(FirebirdVendorError.EXCESS_TRANSACTIONS, ((ExcessTransactionsException) sqlDialectException).getMaxTransactions());
        }
        if (sqlDialectException instanceof DialectSQLParsingException) {
            DialectSQLParsingException ex = (DialectSQLParsingException) sqlDialectException;
            return toSQLException(FirebirdVendorError.DYNAMIC_SQL_ERROR, ex.getMessage());
        }
        if (sqlDialectException instanceof TableExistsException) {
            return toSQLException(FirebirdVendorError.TABLE_ALREADY_EXISTS, ((TableExistsException) sqlDialectException).getTableName());
        }
        if (sqlDialectException instanceof InvalidParameterValueException) {
            return toSQLException(FirebirdVendorError.CHARSET_NOT_FOUND, ((InvalidParameterValueException) sqlDialectException).getParameterValue());
        }
        if (sqlDialectException instanceof InvalidSegstrHandleException) {
            return toSQLException(FirebirdVendorError.INVALID_SEGSTR_HANDLE);
        }
        if (sqlDialectException instanceof InvalidSegstrIdException) {
            return toSQLException(FirebirdVendorError.INVALID_SEGSTR_ID);
        }
        if (sqlDialectException instanceof CannotUpdateOldBlobException) {
            return toSQLException(FirebirdVendorError.CANNOT_UPDATE_OLD_BLOB);
        }
        if (sqlDialectException instanceof UnsupportedBlobFilterException) {
            return toSQLException(FirebirdVendorError.UNSUPPORTED_BLOB_FILTER);
        }
        if (sqlDialectException instanceof BlobFilterNotFoundException) {
            BlobFilterNotFoundException ex = (BlobFilterNotFoundException) sqlDialectException;
            return toFirebirdException(FirebirdVendorError.BLOB_FILTER_NOT_FOUND, ex.getSourceType(), ex.getTargetType());
        }
        if (sqlDialectException instanceof TransliterationFailedException) {
            return toSQLException(FirebirdVendorError.TRANSLITERATION_FAILED);
        }
        if (sqlDialectException instanceof BatchWithoutBlobsException) {
            return toDynamicSQLException(FirebirdVendorError.BATCH_WITHOUT_BLOBS);
        }
        if (sqlDialectException instanceof RepeatedBatchBlobIdException) {
            return toDynamicSQLException(FirebirdVendorError.REPEATED_BATCH_BLOB_ID, toQuad(((RepeatedBatchBlobIdException) sqlDialectException).getBatchBlobId()));
        }
        if (sqlDialectException instanceof UnknownBatchBlobIdException) {
            return toDynamicSQLException(FirebirdVendorError.UNKNOWN_BATCH_BLOB_ID, toQuad(((UnknownBatchBlobIdException) sqlDialectException).getBatchBlobId()));
        }
        if (sqlDialectException instanceof InvalidBatchBlobPolicyException) {
            return toDynamicSQLException(FirebirdVendorError.INVALID_BATCH_BLOB_POLICY, ((InvalidBatchBlobPolicyException) sqlDialectException).getMethodName());
        }
        if (sqlDialectException instanceof BatchDefaultBpbChangeException) {
            return toDynamicSQLException(FirebirdVendorError.BATCH_DEFAULT_BPB_CHANGE);
        }
        if (sqlDialectException instanceof BatchBlobContinuationBpbException) {
            return toBlobBufferFormatException(FirebirdVendorError.BATCH_BLOB_CONTINUATION_BPB, ISCConstants.isc_batch_cont_bpb);
        }
        if (sqlDialectException instanceof BatchBlobBufferFormatException) {
            return toDynamicSQLException(FirebirdVendorError.BATCH_BLOB_BUFFER_FORMAT);
        }
        if (sqlDialectException instanceof InvalidClumpletStructureException) {
            InvalidClumpletStructureException ex = (InvalidClumpletStructureException) sqlDialectException;
            return toSQLException(FirebirdVendorError.INVALID_CLUMPLET_STRUCTURE, ex.getReason(), ex.getData());
        }
        if (sqlDialectException instanceof InvalidBpbVersionException) {
            InvalidBpbVersionException ex = (InvalidBpbVersionException) sqlDialectException;
            return toFirebirdException(FirebirdVendorError.INVALID_BPB_VERSION, ex.getVersion(), ex.getExpectedVersion());
        }
        if (sqlDialectException instanceof BatchSmallDataException) {
            return toBlobBufferFormatException(FirebirdVendorError.BATCH_SMALL_DATA, ISCConstants.isc_batch_small_data, ((BatchSmallDataException) sqlDialectException).getBufferName());
        }
        if (sqlDialectException instanceof BatchBpbTooBigException) {
            BatchBpbTooBigException ex = (BatchBpbTooBigException) sqlDialectException;
            return toBlobBufferFormatException(FirebirdVendorError.BATCH_BPB_TOO_BIG, ISCConstants.isc_batch_big_bpb, (int) ex.getBpbLength(), (int) ex.getAvailableLength());
        }
        if (sqlDialectException instanceof BatchSegmentExceedsBlobException) {
            BatchSegmentExceedsBlobException ex = (BatchSegmentExceedsBlobException) sqlDialectException;
            return toBlobBufferFormatException(FirebirdVendorError.BATCH_SEGMENT_EXCEEDS_BLOB, ISCConstants.isc_batch_big_segment, ex.getSegmentLength(), ex.getBlobLength());
        }
        if (sqlDialectException instanceof BatchSegmentTooBigException) {
            BatchSegmentTooBigException ex = (BatchSegmentTooBigException) sqlDialectException;
            return toBlobBufferFormatException(FirebirdVendorError.BATCH_SEGMENT_TOO_BIG, ISCConstants.isc_batch_big_seg2, (int) ex.getSegmentLength(), (int) ex.getAvailableLength());
        }
        if (sqlDialectException instanceof StringTruncationException) {
            StringTruncationException ex = (StringTruncationException) sqlDialectException;
            return toStringTruncationException(ex.getExpectedLength(), ex.getActualLength());
        }
        if (sqlDialectException instanceof BlobTruncationException) {
            return toArithmeticException(FirebirdVendorError.BLOB_TRUNCATION);
        }
        if (sqlDialectException instanceof NumericOutOfRangeException) {
            return toArithmeticException(FirebirdVendorError.NUMERIC_OUT_OF_RANGE);
        }
        if (sqlDialectException instanceof ConversionErrorException) {
            return toFirebirdException(FirebirdVendorError.CONVERSION_ERROR, ((ConversionErrorException) sqlDialectException).getValue());
        }
        if (sqlDialectException instanceof MalformedStringException) {
            return toSQLException(FirebirdVendorError.MALFORMED_STRING);
        }
        if (sqlDialectException instanceof ParameterConversionException) {
            return toParameterConversionException(convert(((ParameterConversionException) sqlDialectException).getConversionError()));
        }
        return new UnknownSQLException(sqlDialectException).toSQLException();
    }
    
    private SQLException toSQLException(final VendorError vendorError, final Object... messageArgs) {
        return new SQLException(String.format(vendorError.getReason(), messageArgs), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
    
    private SQLException toFirebirdException(final VendorError vendorError, final Object... args) {
        return new FirebirdException(String.format(vendorError.getReason(), args), vendorError.getSqlState().getValue(), vendorError.getVendorCode(),
                Collections.singletonList(new StatusVectorEntry(vendorError.getVendorCode(), Arrays.asList(args))));
    }
    
    private SQLException toDynamicSQLException(final VendorError vendorError, final Object... args) {
        return createDynamicSQLException(vendorError, args, new StatusVectorEntry(vendorError.getVendorCode(), Arrays.asList(args)));
    }
    
    private SQLException createDynamicSQLException(final VendorError vendorError, final Object[] messageArgs, final StatusVectorEntry... errors) {
        Collection<StatusVectorEntry> statusVector = new ArrayList<>(errors.length + 2);
        statusVector.add(DYNAMIC_SQL_ERROR);
        statusVector.add(BATCH_SQL_ERROR_CODE);
        statusVector.addAll(Arrays.asList(errors));
        return new FirebirdException(String.format(vendorError.getReason(), messageArgs), vendorError.getSqlState().getValue(), vendorError.getVendorCode(), statusVector);
    }
    
    private String toQuad(final long quad) {
        return String.format("%x:%x", (int) (quad >>> 32), (int) quad);
    }
    
    private SQLException toBlobBufferFormatException(final VendorError vendorError, final int formatErrorCode, final Object... args) {
        return createDynamicSQLException(vendorError, args, new StatusVectorEntry(vendorError.getVendorCode(), Collections.emptyList()), new StatusVectorEntry(formatErrorCode, Arrays.asList(args)));
    }
    
    private SQLException toStringTruncationException(final int expectedLength, final int actualLength) {
        FirebirdVendorError vendorError = FirebirdVendorError.STRING_TRUNCATION;
        return new FirebirdException(String.format(vendorError.getReason(), expectedLength, actualLength), vendorError.getSqlState().getValue(), vendorError.getVendorCode(),
                Arrays.asList(ARITHMETIC_EXCEPTION, new StatusVectorEntry(vendorError.getVendorCode(), Collections.emptyList()),
                        new StatusVectorEntry(ISCConstants.isc_trunc_limits, Arrays.asList(expectedLength, actualLength))));
    }
    
    private SQLException toArithmeticException(final VendorError vendorError) {
        return new FirebirdException(vendorError.getReason(), vendorError.getSqlState().getValue(), vendorError.getVendorCode(),
                Arrays.asList(ARITHMETIC_EXCEPTION, new StatusVectorEntry(vendorError.getVendorCode(), Collections.emptyList())));
    }
    
    private SQLException toParameterConversionException(final SQLException conversionError) {
        Collection<StatusVectorEntry> statusVector = new ArrayList<>();
        statusVector.add(DYNAMIC_SQL_ERROR);
        statusVector.add(PARAMETER_CONVERSION_SQL_ERROR_CODE);
        statusVector.addAll(conversionError instanceof FirebirdException
                ? ((FirebirdException) conversionError).getStatusVector()
                : Collections.singletonList(new StatusVectorEntry(conversionError.getErrorCode(), Collections.emptyList())));
        return new FirebirdException(conversionError.getMessage(), conversionError.getSQLState(), conversionError.getErrorCode(), statusVector);
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}

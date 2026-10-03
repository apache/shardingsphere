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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

class FirebirdDialectExceptionMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final SQLDialectExceptionMapper mapper = DatabaseTypedSPILoader.getService(SQLDialectExceptionMapper.class, databaseType);
    
    @Test
    void assertConvertWithUnknownDatabase() {
        assertSQLException(mapper.convert(new UnknownDatabaseException("logic_db")), FirebirdVendorError.UNAVAILABLE_DATABASE, "logic_db");
    }
    
    @Test
    void assertConvertWithDatabaseCreateExists() {
        assertSQLException(mapper.convert(new DatabaseCreateExistsException("logic_db")), FirebirdVendorError.DATABASE_ALREADY_EXISTS, "logic_db");
    }
    
    @Test
    void assertConvertWithDatabaseDropNotExists() {
        assertSQLException(mapper.convert(new DatabaseDropNotExistsException("logic_db")), FirebirdVendorError.UNAVAILABLE_DATABASE, "logic_db");
    }
    
    @Test
    void assertConvertWithAccessDenied() {
        assertSQLException(mapper.convert(new AccessDeniedException("root", "127.0.0.1", true)), FirebirdVendorError.LOGIN_FAILED);
    }
    
    @Test
    void assertConvertWithBatchTooBig() {
        assertSQLException(mapper.convert(new BatchTooBigException(42, 1L, 8L, 8L)), FirebirdVendorError.BATCH_TOO_BIG);
    }
    
    @Test
    void assertConvertWithTableExists() {
        assertSQLException(mapper.convert(new TableExistsException("t_order")), FirebirdVendorError.TABLE_ALREADY_EXISTS, "t_order");
    }
    
    @Test
    void assertConvertWithDialectSQLParsing() {
        assertSQLException(mapper.convert(new DialectSQLParsingException("You have an error in your SQL syntax", "SELEC", 1)), FirebirdVendorError.DYNAMIC_SQL_ERROR,
                "You have an error in your SQL syntax");
    }
    
    @Test
    void assertConvertWithInvalidBatchHandle() {
        assertSQLException(mapper.convert(new InvalidBatchHandleException(42)), FirebirdVendorError.INVALID_BATCH_HANDLE);
    }
    
    @Test
    void assertConvertWithBatchAlreadyOpened() {
        assertSQLException(mapper.convert(new BatchAlreadyOpenedException(42)), FirebirdVendorError.BATCH_ALREADY_OPENED);
    }
    
    @Test
    void assertConvertWithInvalidBatchParameterVersion() {
        assertSQLException(mapper.convert(new InvalidBatchParameterVersionException(2, 1)), FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, 2, 1);
    }
    
    @Test
    void assertConvertWithBatchParametersRequired() {
        assertSQLException(mapper.convert(new BatchParametersRequiredException(42)), FirebirdVendorError.BATCH_PARAMETERS_REQUIRED);
    }
    
    @Test
    void assertConvertWithInvalidBatchMessageFormat() {
        assertSQLException(mapper.convert(new InvalidBatchMessageFormatException("invalid message length")), FirebirdVendorError.SQLDA_ERROR);
    }
    
    @Test
    void assertConvertWithInvalidStatementHandle() {
        assertSQLException(mapper.convert(new InvalidStatementHandleException(42)), FirebirdVendorError.INVALID_STATEMENT_HANDLE);
    }
    
    @Test
    void assertConvertWithInvalidTransactionHandle() {
        assertSQLException(mapper.convert(new InvalidTransactionHandleException(42)), FirebirdVendorError.INVALID_TRANSACTION_HANDLE);
    }
    
    @Test
    void assertConvertWithExcessTransactions() {
        assertSQLException(mapper.convert(new ExcessTransactionsException(1)), FirebirdVendorError.EXCESS_TRANSACTIONS, 1);
    }
    
    @Test
    void assertConvertWithInvalidParameterValue() {
        assertSQLException(mapper.convert(new InvalidParameterValueException("names", "foo_charset")), FirebirdVendorError.CHARSET_NOT_FOUND, "foo_charset");
    }
    
    @Test
    void assertConvertWithInvalidSegstrHandle() {
        assertSQLException(mapper.convert(new InvalidSegstrHandleException(42)), FirebirdVendorError.INVALID_SEGSTR_HANDLE);
    }
    
    @Test
    void assertConvertWithInvalidSegstrId() {
        assertSQLException(mapper.convert(new InvalidSegstrIdException(99L)), FirebirdVendorError.INVALID_SEGSTR_ID);
    }
    
    @Test
    void assertConvertWithCannotUpdateOldBlob() {
        assertSQLException(mapper.convert(new CannotUpdateOldBlobException(42)), FirebirdVendorError.CANNOT_UPDATE_OLD_BLOB);
    }
    
    @Test
    void assertConvertWithUnsupportedBlobFilter() {
        assertFirebirdSQLException(mapper.convert(new UnsupportedBlobFilterException()), "0A000", ISCConstants.isc_wish_list,
                "feature is not supported; BLOB filter conversion requested by BLOB parameter buffer");
    }
    
    @Test
    void assertConvertWithBlobFilterNotFound() {
        SQLException actual = mapper.convert(new BlobFilterNotFoundException(-1, 1));
        assertSQLException(actual, FirebirdVendorError.BLOB_FILTER_NOT_FOUND, -1, 1);
        assertStatusVector(actual, Collections.singletonList(Arrays.asList(ISCConstants.isc_nofilter, -1, 1)));
    }
    
    @Test
    void assertConvertWithTransliterationFailed() {
        assertFirebirdSQLException(mapper.convert(new TransliterationFailedException()), "22018", ISCConstants.isc_transliteration_failed, "Cannot transliterate character between character sets");
    }
    
    @Test
    void assertConvertWithBatchWithoutBlobs() {
        SQLException actual = mapper.convert(new BatchWithoutBlobsException(42));
        assertSQLException(actual, FirebirdVendorError.BATCH_WITHOUT_BLOBS);
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blobs));
    }
    
    @Test
    void assertConvertWithRepeatedBatchBlobId() {
        SQLException actual = mapper.convert(new RepeatedBatchBlobIdException(0x100000002L));
        assertSQLException(actual, FirebirdVendorError.REPEATED_BATCH_BLOB_ID, "1:2");
        assertDynamicSQLStatusVector(actual, Arrays.asList(ISCConstants.isc_batch_rpt_blob, "1:2"));
    }
    
    @Test
    void assertConvertWithUnknownBatchBlobId() {
        SQLException actual = mapper.convert(new UnknownBatchBlobIdException(0x300000004L));
        assertSQLException(actual, FirebirdVendorError.UNKNOWN_BATCH_BLOB_ID, "3:4");
        assertDynamicSQLStatusVector(actual, Arrays.asList(ISCConstants.isc_batch_blob_id, "3:4"));
    }
    
    @Test
    void assertConvertWithInvalidBatchBlobPolicy() {
        SQLException actual = mapper.convert(new InvalidBatchBlobPolicyException("addBlobStream"));
        assertSQLException(actual, FirebirdVendorError.INVALID_BATCH_BLOB_POLICY, "addBlobStream");
        assertDynamicSQLStatusVector(actual, Arrays.asList(ISCConstants.isc_batch_policy, "addBlobStream"));
    }
    
    @Test
    void assertConvertWithBatchDefaultBpbChange() {
        SQLException actual = mapper.convert(new BatchDefaultBpbChangeException(42));
        assertSQLException(actual, FirebirdVendorError.BATCH_DEFAULT_BPB_CHANGE);
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_defbpb));
    }
    
    @Test
    void assertConvertWithBatchBlobContinuationBpb() {
        SQLException actual = mapper.convert(new BatchBlobContinuationBpbException(4L));
        assertFirebirdSQLException(actual, "22000", ISCConstants.isc_batch_blob_buf, "Blob buffer format error; Blob continuation should not contain BPB");
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf), Collections.singletonList(ISCConstants.isc_batch_cont_bpb));
    }
    
    @Test
    void assertConvertWithBatchBlobBufferFormat() {
        SQLException actual = mapper.convert(new BatchBlobBufferFormatException(42));
        assertSQLException(actual, FirebirdVendorError.BATCH_BLOB_BUFFER_FORMAT);
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf));
    }
    
    @Test
    void assertConvertWithInvalidClumpletStructure() {
        SQLException actual = mapper.convert(new InvalidClumpletStructureException("empty buffer", 0));
        assertSQLException(actual, FirebirdVendorError.INVALID_CLUMPLET_STRUCTURE, "empty buffer", 0);
    }
    
    @Test
    void assertConvertWithInvalidBpbVersion() {
        SQLException actual = mapper.convert(new InvalidBpbVersionException(2, 1));
        assertSQLException(actual, FirebirdVendorError.INVALID_BPB_VERSION, 2, 1);
        assertStatusVector(actual, Collections.singletonList(Arrays.asList(ISCConstants.isc_bpb_version, 2, 1)));
    }
    
    @Test
    void assertConvertWithBatchSmallData() {
        SQLException actual = mapper.convert(new BatchSmallDataException("BLOB"));
        assertFirebirdSQLException(actual, "22000", ISCConstants.isc_batch_blob_buf, "Blob buffer format error; Unusable (too small) data remained in BLOB buffer");
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf), Arrays.asList(ISCConstants.isc_batch_small_data, "BLOB"));
    }
    
    @Test
    void assertConvertWithBatchBpbTooBig() {
        SQLException actual = mapper.convert(new BatchBpbTooBigException(4L, 2L));
        assertFirebirdSQLException(actual, "22000", ISCConstants.isc_batch_blob_buf, "Blob buffer format error; Size of BPB (4) greater than remaining data (2)");
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf), Arrays.asList(ISCConstants.isc_batch_big_bpb, 4, 2));
    }
    
    @Test
    void assertConvertWithBatchSegmentExceedsBlob() {
        SQLException actual = mapper.convert(new BatchSegmentExceedsBlobException(16, 2));
        assertFirebirdSQLException(actual, "22000", ISCConstants.isc_batch_blob_buf, "Blob buffer format error; Size of segment (16) greater than current BLOB data (2)");
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf), Arrays.asList(ISCConstants.isc_batch_big_segment, 16, 2));
    }
    
    @Test
    void assertConvertWithBatchSegmentTooBig() {
        SQLException actual = mapper.convert(new BatchSegmentTooBigException(5, 3));
        assertFirebirdSQLException(actual, "22000", ISCConstants.isc_batch_blob_buf, "Blob buffer format error; Size of segment (5) greater than available data (3)");
        assertDynamicSQLStatusVector(actual, Collections.singletonList(ISCConstants.isc_batch_blob_buf), Arrays.asList(ISCConstants.isc_batch_big_seg2, 5, 3));
    }
    
    @Test
    void assertConvertWithStringTruncation() {
        SQLException actual = mapper.convert(new StringTruncationException(20, 32));
        assertFirebirdSQLException(actual, "22001", ISCConstants.isc_string_truncation,
                "arithmetic exception, numeric overflow, or string truncation; string right truncation; expected length 20, actual 32");
        assertStatusVector(actual, Arrays.asList(Collections.singletonList(ISCConstants.isc_arith_except), Collections.singletonList(ISCConstants.isc_string_truncation),
                Arrays.asList(ISCConstants.isc_trunc_limits, 20, 32)));
    }
    
    @Test
    void assertConvertWithBlobTruncation() {
        SQLException actual = mapper.convert(new BlobTruncationException());
        assertStatusVector(actual, Arrays.asList(Collections.singletonList(ISCConstants.isc_arith_except), Collections.singletonList(ISCConstants.isc_blob_truncation)));
    }
    
    @Test
    void assertConvertWithNumericOutOfRange() {
        SQLException actual = mapper.convert(new NumericOutOfRangeException());
        assertStatusVector(actual, Arrays.asList(Collections.singletonList(ISCConstants.isc_arith_except), Collections.singletonList(ISCConstants.isc_numeric_out_of_range)));
    }
    
    @Test
    void assertConvertWithConversionError() {
        SQLException actual = mapper.convert(new ConversionErrorException("from-blobs"));
        assertFirebirdSQLException(actual, "22018", ISCConstants.isc_convert_error, "conversion error from string \"from-blobs\"");
        assertStatusVector(actual, Collections.singletonList(Arrays.asList(ISCConstants.isc_convert_error, "from-blobs")));
    }
    
    @Test
    void assertConvertWithMalformedString() {
        assertSQLException(mapper.convert(new MalformedStringException()), FirebirdVendorError.MALFORMED_STRING);
    }
    
    @Test
    void assertConvertWithParameterConversionOfStatusVector() {
        SQLException actual = mapper.convert(new ParameterConversionException(new StringTruncationException(20, 32)));
        assertFirebirdSQLException(actual, "22001", ISCConstants.isc_string_truncation,
                "arithmetic exception, numeric overflow, or string truncation; string right truncation; expected length 20, actual 32");
        assertStatusVector(actual, Arrays.asList(Collections.singletonList(ISCConstants.isc_dsql_error), Arrays.asList(ISCConstants.isc_sqlerr, -303),
                Collections.singletonList(ISCConstants.isc_arith_except), Collections.singletonList(ISCConstants.isc_string_truncation), Arrays.asList(ISCConstants.isc_trunc_limits, 20, 32)));
    }
    
    @Test
    void assertConvertWithParameterConversionOfErrorCode() {
        SQLException actual = mapper.convert(new ParameterConversionException(new InvalidSegstrIdException(1L)));
        assertStatusVector(actual, Arrays.asList(Collections.singletonList(ISCConstants.isc_dsql_error), Arrays.asList(ISCConstants.isc_sqlerr, -303),
                Collections.singletonList(ISCConstants.isc_bad_segstr_id)));
    }
    
    @Test
    void assertConvertWithUnknownException() {
        SQLException actual = mapper.convert(mock(SQLDialectException.class));
        assertThat(actual.getSQLState(), is("HY000"));
    }
    
    private void assertSQLException(final SQLException actual, final VendorError vendorError, final Object... messageArgs) {
        assertThat(actual.getSQLState(), is(vendorError.getSqlState().getValue()));
        assertThat(actual.getErrorCode(), is(vendorError.getVendorCode()));
        assertThat(actual.getMessage(), is(String.format(vendorError.getReason(), messageArgs)));
    }
    
    private void assertFirebirdSQLException(final SQLException actual, final String expectedSQLState, final int expectedErrorCode, final String expectedMessage) {
        assertThat(actual.getSQLState(), is(expectedSQLState));
        assertThat(actual.getErrorCode(), is(expectedErrorCode));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private void assertDynamicSQLStatusVector(final SQLException actual, final List<?>... expectedErrors) {
        List<List<?>> expectedStatusVector = new ArrayList<>(expectedErrors.length + 2);
        expectedStatusVector.add(Collections.singletonList(ISCConstants.isc_dsql_error));
        expectedStatusVector.add(Arrays.asList(ISCConstants.isc_sqlerr, -104));
        expectedStatusVector.addAll(Arrays.asList(expectedErrors));
        assertStatusVector(actual, expectedStatusVector);
    }
    
    private void assertStatusVector(final SQLException actual, final List<List<?>> expectedStatusVector) {
        assertThat(actual, isA(FirebirdException.class));
        Collection<StatusVectorEntry> statusVector = ((FirebirdException) actual).getStatusVector();
        List<List<Object>> actualStatusVector = new ArrayList<>(statusVector.size());
        for (StatusVectorEntry each : statusVector) {
            List<Object> entry = new ArrayList<>(each.getArguments().size() + 1);
            entry.add(each.getGdsCode());
            entry.addAll(each.getArguments());
            actualStatusVector.add(entry);
        }
        assertThat(actualStatusVector, is(expectedStatusVector));
    }
}

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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Reservation;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.firebirdsql.gds.ISCConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FirebirdReservationValidator {
    
    static Optional<FirebirdStatusVector> validate(final FirebirdTransactionOptions options, final String sql, final Charset charset) {
        for (Reservation each : options.getReservations()) {
            Optional<FirebirdStatusVector> error = validateReservation(each, options, sql, charset);
            if (error.isPresent()) {
                return error;
            }
        }
        return Optional.empty();
    }
    
    private static Optional<FirebirdStatusVector> validateReservation(final Reservation reservation, final FirebirdTransactionOptions options, final String sql, final Charset charset) {
        for (SimpleTableSegment each : reservation.getTables()) {
            Optional<OwnerSegment> owner = each.getOwner();
            int position = owner.isPresent() ? owner.get().getStartIndex() : each.getTableName().getStartIndex();
            if (null != options.getValidationPosition() && position >= options.getValidationPosition()) {
                continue;
            }
            int error = getNameError(owner.isPresent() ? owner.get().getIdentifier() : each.getTableName().getIdentifier(), charset);
            if (0 != error) {
                return Optional.of(createStatus(error, sql, position, charset));
            }
            if (owner.isPresent() && ".".equals(options.getInvalidSyntaxToken()) && null != options.getValidationPosition()
                    && owner.get().getStopIndex() < options.getValidationPosition() && options.getValidationPosition() < each.getTableName().getStartIndex()) {
                return Optional.of(createStatus(ISCConstants.isc_dsql_token_unk_err, sql, options.getValidationPosition(), charset));
            }
        }
        return Optional.empty();
    }
    
    private static int getNameError(final IdentifierValue identifier, final Charset charset) {
        boolean quoted = QuoteCharacter.QUOTE == identifier.getQuoteCharacter();
        String name = quoted ? identifier.getValue().replace("\"\"", "\"") : identifier.getValue();
        boolean ascii = isAscii(name);
        if (!quoted && !ascii) {
            return 0;
        }
        int byteLength = name.getBytes(charset).length;
        if (byteLength >= 256) {
            return ISCConstants.isc_token_too_long;
        }
        if (quoted && 0 == byteLength) {
            return ISCConstants.isc_dyn_zero_len_id;
        }
        if (quoted && byteLength > 252 || (ascii || StandardCharsets.UTF_8.equals(charset)) && name.codePointCount(0, name.length()) > 63) {
            return ISCConstants.isc_dyn_name_longer;
        }
        return 0;
    }
    
    private static boolean isAscii(final String name) {
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) >= 128) {
                return false;
            }
        }
        return true;
    }
    
    private static FirebirdStatusVector createStatus(final int error, final String sql, final int position, final Charset charset) {
        int[] coordinates = getCoordinates(sql, position, charset);
        if (ISCConstants.isc_dsql_token_unk_err == error) {
            return FirebirdStatusVector.fromSegments(Arrays.asList(
                    new FirebirdStatusVector.Segment(ISCConstants.isc_dsql_error, Collections.emptyList(), null),
                    FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-104)), null),
                    FirebirdStatusVector.Segment.withArguments(error, Arrays.asList(FirebirdStatusVector.Argument.ofNumber(coordinates[0]),
                            FirebirdStatusVector.Argument.ofNumber(coordinates[1])), null),
                    FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_random, Collections.singletonList(FirebirdStatusVector.Argument.ofString(".")), null)));
        }
        return FirebirdStatusVector.fromSegments(Arrays.asList(
                new FirebirdStatusVector.Segment(ISCConstants.isc_dsql_error, Collections.emptyList(), null),
                FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-104)), null),
                new FirebirdStatusVector.Segment(error, Collections.emptyList(), null),
                FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_dsql_line_col_error,
                        Arrays.asList(FirebirdStatusVector.Argument.ofNumber(coordinates[0]), FirebirdStatusVector.Argument.ofNumber(coordinates[1])), null)));
    }
    
    private static int[] getCoordinates(final String sql, final int position, final Charset charset) {
        int line = 1;
        int lineStart = 0;
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        int i = 0;
        while (i < position) {
            char current = sql.charAt(i++);
            if (0 != quote) {
                if ('\n' == current) {
                    line++;
                    lineStart = i;
                } else if (quote == current && i < position && quote == sql.charAt(i)) {
                    i++;
                } else if (quote == current) {
                    quote = 0;
                }
                continue;
            }
            if ('\r' == current || '\n' == current) {
                if ('\r' == current && i < position && '\n' == sql.charAt(i)) {
                    i++;
                }
                line++;
                lineStart = i;
                lineComment = false;
                continue;
            }
            if (lineComment) {
                continue;
            }
            if (blockComment) {
                if ('*' == current && i < position && '/' == sql.charAt(i)) {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (i < position && '-' == current && '-' == sql.charAt(i)) {
                lineComment = true;
                i++;
            } else if (i < position && '/' == current && '*' == sql.charAt(i)) {
                blockComment = true;
                i++;
            } else if ('\'' == current || '"' == current) {
                quote = current;
            }
        }
        int column = sql.substring(lineStart, position).getBytes(charset).length + 1;
        return new int[]{line, column};
    }
}

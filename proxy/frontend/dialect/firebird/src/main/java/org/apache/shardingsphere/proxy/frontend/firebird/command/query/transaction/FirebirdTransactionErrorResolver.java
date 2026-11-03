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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorSegmentResolver;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector.Argument;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector.Segment;
import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.GDSExceptionHelper.GDSMessage;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Recovers transaction error segments from Jaybird's exception chain.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FirebirdTransactionErrorResolver {
    
    static Optional<FirebirdStatusVector> resolve(final SQLException error) {
        if (!(error.getCause() instanceof FBSQLExceptionInfo)) {
            return Optional.empty();
        }
        SQLException first = (SQLException) error.getCause();
        if (ISCConstants.isc_bad_tpb_content == first.getErrorCode() && null == first.getNextException() && null != first.getMessage() && first.getMessage().endsWith("; isc_tpb_at_snapshot_number")) {
            return Optional.of(FirebirdStatusVector.fromSegments(Collections.singletonList(Segment.withArguments(ISCConstants.isc_bad_tpb_content,
                    Collections.singletonList(Argument.ofString("isc_tpb_at_snapshot_number")), null))));
        }
        List<Segment> result = new ArrayList<>();
        for (SQLException each = first; null != each; each = each.getNextException()) {
            if (!(each instanceof FBSQLExceptionInfo)) {
                return Optional.empty();
            }
            Optional<Segment> reservation = resolveReservationError(each);
            if (reservation.isPresent()) {
                result.add(reservation.get());
                continue;
            }
            for (Segment segment : FirebirdErrorSegmentResolver.resolve(each.getErrorCode(), each.getMessage(), each.getSQLState())) {
                if (hasNumericFirstArgument(each.getErrorCode()) && segment.getGdsCode() == each.getErrorCode()) {
                    Optional<Segment> typed = numericFirstArgument(segment);
                    if (!typed.isPresent()) {
                        return Optional.empty();
                    }
                    result.add(typed.get());
                } else {
                    result.add(segment);
                }
            }
        }
        return Optional.of(FirebirdStatusVector.fromSegments(result));
    }
    
    private static boolean hasNumericFirstArgument(final int code) {
        switch (code) {
            case ISCConstants.isc_tpb_reserv_long_tlen:
            case ISCConstants.isc_tpb_reserv_missing_tname:
            case ISCConstants.isc_tpb_reserv_corrup_tlen:
            case ISCConstants.isc_tpb_missing_value:
            case ISCConstants.isc_tpb_corrupt_len:
            case ISCConstants.isc_tpb_overflow_len:
            case ISCConstants.isc_tpb_invalid_value:
                return true;
            default:
                return false;
        }
    }
    
    private static Optional<Segment> numericFirstArgument(final Segment segment) {
        List<Argument> arguments = segment.getTypedArguments();
        if (arguments.isEmpty()) {
            return Optional.empty();
        }
        try {
            List<Argument> result = new ArrayList<>(arguments);
            result.set(0, Argument.ofNumber(Integer.parseInt(arguments.get(0).getText())));
            return Optional.of(Segment.withArguments(segment.getGdsCode(), result, segment.getSqlState()));
        } catch (final NumberFormatException ex) {
            return Optional.empty();
        }
    }
    
    private static Optional<Segment> resolveReservationError(final SQLException error) {
        if (ISCConstants.isc_tpb_reserv_relnotfound != error.getErrorCode()) {
            return Optional.empty();
        }
        String marker = "" + (char) 1 + (char) 2;
        for (String each : Arrays.asList("isc_tpb_lock_read", "isc_tpb_lock_write")) {
            GDSMessage template = GDSExceptionHelper.getMessage(error.getErrorCode());
            template.setParameters(Arrays.asList(marker, each));
            String rendered = template.toString();
            int markerIndex = rendered.indexOf(marker);
            if (markerIndex < 0) {
                continue;
            }
            String prefix = rendered.substring(0, markerIndex);
            String suffix = rendered.substring(markerIndex + marker.length());
            String message = error.getMessage();
            if (message.length() >= prefix.length() + suffix.length() && message.startsWith(prefix) && message.endsWith(suffix)) {
                String table = message.substring(prefix.length(), message.length() - suffix.length());
                return Optional.of(Segment.withArguments(error.getErrorCode(), Arrays.asList(Argument.ofString(table), Argument.ofString(each)), null));
            }
        }
        return Optional.empty();
    }
}

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

package org.apache.shardingsphere.database.protocol.firebird.err;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector.Segment;
import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.GDSExceptionHelper.GDSMessage;
import org.firebirdsql.gds.ISCConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resolver of Firebird status vector segments that preserves the native gdscode.
 *
 * <p>The backend driver (Jaybird) flattens the status vector into a single message and discards the structured
 * arguments, so to keep the honest gdscode this resolver reverses Jaybird's rendering: it recovers the template
 * arguments from the live Jaybird catalog (verified by re-render) and keeps the native gdscode, falling back to a
 * verbatim {@code isc_random} segment when recovery is not reliable. Used by {@link FirebirdStatusVector}.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdErrorSegmentResolver {
    
    private static final String SEGMENT_SEPARATOR = "; ";
    
    private static final String PARAMETER_MARKER = "" + (char) 1 + (char) 2;
    
    /**
     * Resolve status vector segments keeping the native gdscode.
     *
     * @param errorCode native error code reported by the backend driver
     * @param message error message stripped of the trailing SQLState/ISC code suffix
     * @param sqlState SQL state
     * @return resolved segments
     */
    public static List<Segment> resolve(final int errorCode, final String message, final String sqlState) {
        int code = errorCode >= ISCConstants.isc_arith_except ? errorCode : ISCConstants.isc_random;
        return buildSegments(code, message, sqlState);
    }
    
    private static List<Segment> buildSegments(final int code, final String message, final String sqlState) {
        List<String> parameters = recoverParameters(code, message);
        if (null != parameters) {
            return buildTemplatedSegments(code, parameters, message);
        }
        if (GDSExceptionHelper.getMessage(code).getParamCount() >= 2) {
            return Collections.singletonList(new Segment(ISCConstants.isc_random, toArguments(message), blankToNull(sqlState)));
        }
        return Collections.singletonList(new Segment(code, toArguments(message), null));
    }
    
    private static List<Segment> buildTemplatedSegments(final int code, final List<String> parameters, final String message) {
        List<Segment> result = new ArrayList<>();
        result.add(new Segment(code, parameters, null));
        String remainder = stripSeparator(message.substring(render(code, parameters).length()));
        if (!remainder.isEmpty()) {
            result.add(new Segment(ISCConstants.isc_random, Collections.singletonList(remainder), null));
        }
        return result;
    }
    
    private static List<String> recoverParameters(final int code, final String message) {
        GDSMessage template = GDSExceptionHelper.getMessage(code);
        int count = template.getParamCount();
        for (int i = 0; i < count; i++) {
            template.setParameter(i, PARAMETER_MARKER);
        }
        String[] literals = template.toString().split(Pattern.quote(PARAMETER_MARKER), -1);
        if (literals.length != count + 1 || !message.startsWith(literals[0])) {
            return null;
        }
        List<String> result = new ArrayList<>(count);
        int position = literals[0].length();
        for (int i = 1; i <= count; i++) {
            String tail = literals[i];
            int end = tail.isEmpty() ? message.length() : message.indexOf(tail, position);
            if (end < 0) {
                return null;
            }
            result.add(message.substring(position, end));
            position = end + tail.length();
        }
        return message.startsWith(render(code, result)) ? result : null;
    }
    
    private static String render(final int code, final List<String> parameters) {
        GDSMessage template = GDSExceptionHelper.getMessage(code);
        template.setParameters(parameters);
        return template.toString();
    }
    
    private static List<String> toArguments(final String message) {
        return message.isEmpty() ? Collections.emptyList() : Collections.singletonList(message);
    }
    
    private static String stripSeparator(final String remainder) {
        return remainder.startsWith(SEGMENT_SEPARATOR) ? remainder.substring(SEGMENT_SEPARATOR.length()) : remainder;
    }
    
    private static String blankToNull(final String sqlState) {
        return null == sqlState || sqlState.isEmpty() ? null : sqlState;
    }
}

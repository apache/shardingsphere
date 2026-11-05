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

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebird status vector for errors handling.
 */
@HighFrequencyInvocation
@Getter
public final class FirebirdStatusVector extends FirebirdPacket {
    
    private static final String STATE_SUFFIX = " [SQLState:";
    
    private final int gdsCode;
    
    private final String errorMessage;
    
    private final List<Segment> segments;
    
    public FirebirdStatusVector(final SQLException ex) {
        errorMessage = stripStateSuffix(Strings.nullToEmpty(ex.getMessage()));
        segments = FirebirdErrorSegmentResolver.resolve(ex.getErrorCode(), errorMessage, ex.getSQLState());
        gdsCode = segments.get(0).gdsCode;
    }
    
    private FirebirdStatusVector(final List<Segment> segments) {
        this.segments = segments;
        gdsCode = segments.get(0).gdsCode;
        errorMessage = "";
    }
    
    /**
     * Construct a status vector from ordered segments.
     *
     * @param segments ordered nonempty segments
     * @return status vector
     */
    public static FirebirdStatusVector fromSegments(final List<Segment> segments) {
        return new FirebirdStatusVector(segments);
    }
    
    private static String stripStateSuffix(final String message) {
        int index = message.indexOf(STATE_SUFFIX);
        return (index >= 0 ? message.substring(0, index) : message).trim();
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        for (Segment each : segments) {
            payload.writeInt4(ISCConstants.isc_arg_gds);
            payload.writeInt4(each.gdsCode);
            if (null == each.typedArguments) {
                writeStringArguments(payload, each.arguments);
            } else {
                writeArguments(payload, each.typedArguments);
            }
            if (null != each.sqlState) {
                payload.writeInt4(ISCConstants.isc_arg_sql_state);
                payload.writeString(each.sqlState);
            }
        }
        payload.writeInt4(ISCConstants.isc_arg_end);
    }
    
    private void writeStringArguments(final FirebirdPacketPayload payload, final List<String> arguments) {
        for (String each : arguments) {
            payload.writeInt4(ISCConstants.isc_arg_string);
            payload.writeString(each);
        }
    }
    
    private void writeArguments(final FirebirdPacketPayload payload, final List<Argument> arguments) {
        for (Argument each : arguments) {
            payload.writeInt4(each.type);
            if (ISCConstants.isc_arg_number == each.type) {
                payload.writeInt4(each.number);
            } else {
                payload.writeString(each.text);
            }
        }
    }
    
    /**
     * Single segment of the status vector.
     */
    @HighFrequencyInvocation
    @Getter
    public static final class Segment {
        
        private final int gdsCode;
        
        private final List<String> arguments;
        
        private final String sqlState;
        
        private final List<Argument> typedArguments;
        
        public Segment(final int gdsCode, final List<String> arguments, final String sqlState) {
            this.gdsCode = gdsCode;
            this.arguments = arguments;
            this.sqlState = sqlState;
            typedArguments = null;
        }
        
        private Segment(final int gdsCode, final String sqlState, final List<Argument> typedArguments) {
            this.gdsCode = gdsCode;
            this.sqlState = sqlState;
            this.typedArguments = typedArguments;
            arguments = new ArrayList<>(typedArguments.size());
            for (Argument each : typedArguments) {
                if (ISCConstants.isc_arg_string == each.type) {
                    arguments.add(each.text);
                }
            }
        }
        
        /**
         * Get the arguments with their wire types.
         *
         * @return ordered typed arguments
         */
        public List<Argument> getTypedArguments() {
            if (null != typedArguments) {
                return typedArguments;
            }
            List<Argument> result = new ArrayList<>(arguments.size());
            for (String each : arguments) {
                result.add(Argument.ofString(each));
            }
            return result;
        }
        
        /**
         * Construct a segment with ordered typed arguments.
         *
         * @param gdsCode error code
         * @param arguments ordered arguments
         * @param sqlState optional SQL state, or null
         * @return status segment
         */
        public static Segment withArguments(final int gdsCode, final List<Argument> arguments, final String sqlState) {
            return new Segment(gdsCode, sqlState, arguments);
        }
    }
    
    /**
     * Typed argument in a status segment.
     */
    @HighFrequencyInvocation
    @Getter
    public static final class Argument {
        
        private final int type;
        
        private final String text;
        
        private final int number;
        
        private Argument(final int type, final String text, final int number) {
            this.type = type;
            this.text = text;
            this.number = number;
        }
        
        /**
         * Create a string argument.
         *
         * @param text argument value
         * @return string argument
         */
        public static Argument ofString(final String text) {
            return new Argument(ISCConstants.isc_arg_string, text, 0);
        }
        
        /**
         * Create a numeric argument.
         *
         * @param number argument value
         * @return numeric argument
         */
        public static Argument ofNumber(final int number) {
            return new Argument(ISCConstants.isc_arg_number, "", number);
        }
    }
}

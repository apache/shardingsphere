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

package org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.StringPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;

import java.math.BigInteger;
import java.util.List;

/**
 * Primary key ingest position factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrimaryKeyIngestPositionFactory {
    
    /**
     * Create new instance by string data.
     *
     * @param data string data
     * @return primary key position
     * @throws IllegalArgumentException illegal argument exception
     */
    public static PrimaryKeyIngestPosition<?> newInstance(final String data) {
        List<String> parts = Splitter.on(',').splitToList(data);
        Preconditions.checkArgument(3 == parts.size(), "Unknown primary key position: " + data);
        Preconditions.checkArgument(1 == parts.get(0).length(), "Invalid primary key position type: " + parts.get(0));
        char type = parts.get(0).charAt(0);
        String lowerBound = parts.get(1);
        String upperBound = parts.get(2);
        switch (type) {
            case 'i':
                return new IntegerPrimaryKeyIngestPosition(
                        Strings.isNullOrEmpty(lowerBound) ? null : new BigInteger(lowerBound), Strings.isNullOrEmpty(upperBound) ? null : new BigInteger(upperBound));
            case 's':
                return new StringPrimaryKeyIngestPosition(lowerBound, upperBound);
            case 'u':
                return new UnsupportedKeyIngestPosition();
            default:
                throw new IllegalArgumentException("Unknown primary key position type: " + type);
        }
    }
    
    /**
     * New instance by lower bound and upper bound.
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return ingest position
     */
    public static PrimaryKeyIngestPosition<?> newInstance(final Object lowerBound, final Object upperBound) {
        if (lowerBound instanceof Number) {
            return new IntegerPrimaryKeyIngestPosition(convertToBigInteger((Number) lowerBound), null == upperBound ? null : convertToBigInteger((Number) upperBound));
        }
        if (lowerBound instanceof CharSequence) {
            return new StringPrimaryKeyIngestPosition(lowerBound.toString(), null == upperBound ? null : upperBound.toString());
        }
        // TODO support more types, e.g. byte[] (MySQL varbinary)
        return new UnsupportedKeyIngestPosition();
    }
    
    private static BigInteger convertToBigInteger(final Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        return BigInteger.valueOf(number.longValue());
    }
}

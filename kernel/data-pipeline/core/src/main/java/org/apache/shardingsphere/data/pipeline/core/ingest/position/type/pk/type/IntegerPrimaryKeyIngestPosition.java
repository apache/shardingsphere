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

package org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;

import java.math.BigInteger;

/**
 * Integer primary key ingest position.
 */
public final class IntegerPrimaryKeyIngestPosition implements PrimaryKeyIngestPosition<BigInteger> {
    
    private final BigInteger lowerBound;
    
    private final BigInteger upperBound;
    
    public IntegerPrimaryKeyIngestPosition(final BigInteger lowerBound, final BigInteger upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    
    @Override
    public BigInteger getLowerBound() {
        return lowerBound;
    }
    
    @Override
    public BigInteger getUpperBound() {
        return upperBound;
    }
    
    @Override
    public char getType() {
        return 'i';
    }
    
    @Override
    public String toString() {
        return String.format("%s,%s,%s", getType(), null == lowerBound ? "" : lowerBound, null == upperBound ? "" : upperBound);
    }
}

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

package org.apache.shardingsphere.sharding.merge.dql;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

class ComparableByteArrayTest {
    
    @Test
    void assertCompareToEqual() {
        assertThat(new ComparableByteArray(new byte[]{1, 2, 3}).compareTo(new ComparableByteArray(new byte[]{1, 2, 3})), is(0));
    }
    
    @Test
    void assertCompareToFirstByteDiffers() {
        assertThat(new ComparableByteArray(new byte[]{1}).compareTo(new ComparableByteArray(new byte[]{2})), lessThan(0));
        assertThat(new ComparableByteArray(new byte[]{2}).compareTo(new ComparableByteArray(new byte[]{1})), greaterThan(0));
    }
    
    @Test
    void assertCompareToUnsignedHighByteIsLarger() {
        assertThat(new ComparableByteArray(new byte[]{(byte) 0x80}).compareTo(new ComparableByteArray(new byte[]{(byte) 0x7F})), greaterThan(0));
    }
    
    @Test
    void assertCompareToShorterPrefixIsLess() {
        assertThat(new ComparableByteArray(new byte[]{1, 2}).compareTo(new ComparableByteArray(new byte[]{1, 2, 3})), lessThan(0));
        assertThat(new ComparableByteArray(new byte[]{1, 2, 3}).compareTo(new ComparableByteArray(new byte[]{1, 2})), greaterThan(0));
    }
    
    @Test
    void assertCompareToEmptyArrays() {
        assertThat(new ComparableByteArray(new byte[0]).compareTo(new ComparableByteArray(new byte[0])), is(0));
        assertThat(new ComparableByteArray(new byte[0]).compareTo(new ComparableByteArray(new byte[]{0})), lessThan(0));
    }
}

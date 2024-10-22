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

import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.StringPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimaryKeyIngestPositionFactoryTest {
    
    @Test
    void assertNewInstanceWithIntegerPrimaryKeyIngestPosition() {
        IntegerPrimaryKeyIngestPosition actual = (IntegerPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance("i,100,200");
        assertThat(actual.getType(), is('i'));
        assertThat(actual.getBeginValue(), is(100L));
        assertThat(actual.getEndValue(), is(200L));
    }
    
    @Test
    void assertNewInstanceWithStringPrimaryKeyIngestPosition() {
        StringPrimaryKeyIngestPosition actual = (StringPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance("s,a,b");
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getBeginValue(), is("a"));
        assertThat(actual.getEndValue(), is("b"));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedKeyIngestPosition() {
        UnsupportedKeyIngestPosition actual = (UnsupportedKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance("u,a,b");
        assertThat(actual.getType(), is('u'));
        assertNull(actual.getBeginValue());
        assertNull(actual.getEndValue());
    }
    
    @Test
    void assertNewInstanceWithIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> PrimaryKeyIngestPositionFactory.newInstance("z,100"));
        assertThrows(IllegalArgumentException.class, () -> PrimaryKeyIngestPositionFactory.newInstance("zz,100,200"));
        assertThrows(IllegalArgumentException.class, () -> PrimaryKeyIngestPositionFactory.newInstance("z,100,200"));
    }
    
    @Test
    void assertNewInstanceWithNumberRange() {
        IntegerPrimaryKeyIngestPosition actual = (IntegerPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance(100, 200);
        assertThat(actual.getType(), is('i'));
        assertThat(actual.getBeginValue(), is(100L));
        assertThat(actual.getEndValue(), is(200L));
    }
    
    @Test
    void assertNewInstanceWithNumberNullEndRange() {
        IntegerPrimaryKeyIngestPosition actual = (IntegerPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance(100, null);
        assertThat(actual.getType(), is('i'));
        assertThat(actual.getBeginValue(), is(100L));
        assertThat(actual.getEndValue(), is(Long.MAX_VALUE));
    }
    
    @Test
    void assertNewInstanceWithCharRange() {
        StringPrimaryKeyIngestPosition actual = (StringPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance("a", "b");
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getBeginValue(), is("a"));
        assertThat(actual.getEndValue(), is("b"));
    }
    
    @Test
    void assertNewInstanceWithCharNullEndRange() {
        StringPrimaryKeyIngestPosition actual = (StringPrimaryKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance("a", null);
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getBeginValue(), is("a"));
        assertNull(actual.getEndValue());
    }
    
    @Test
    void assertNewInstanceWithUnsupportedRange() {
        UnsupportedKeyIngestPosition actual = (UnsupportedKeyIngestPosition) PrimaryKeyIngestPositionFactory.newInstance(Collections.emptyList(), Collections.emptyList());
        assertThat(actual.getType(), is('u'));
        assertNull(actual.getBeginValue());
        assertNull(actual.getEndValue());
    }
}

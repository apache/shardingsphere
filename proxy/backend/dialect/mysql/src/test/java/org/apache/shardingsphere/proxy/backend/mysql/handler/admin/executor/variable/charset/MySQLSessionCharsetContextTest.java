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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MySQLSessionCharsetContextTest {
    
    @Test
    void assertCreate() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        assertThat(actual.getClientCharacterSetName(), is("latin1"));
        assertThat(actual.getResultCharacterSetName(), is(Optional.of("latin1")));
        assertThat(actual.getConnectionCharacterSetName(), is("latin1"));
        assertThat(actual.getConnectionCollationName(), is("latin1_swedish_ci"));
    }
    
    @Test
    void assertGetDefaultContext() {
        assertThat(MySQLSessionCharsetContext.get(new DefaultAttributeMap()).getClientCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertGetStoredContext() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionCharsetContext expected = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        attributeMap.attr(MySQLSessionCharsetContext.ATTRIBUTE_KEY).set(expected);
        assertThat(MySQLSessionCharsetContext.get(attributeMap), is(expected));
    }
    
    @Test
    void assertWithClientCharacterSet() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLConstants.DEFAULT_CHARSET).withClientCharacterSet(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        assertThat(actual.getClientCharacterSet(), is(MySQLCharacterSets.LATIN1_SWEDISH_CI));
        assertThat(actual.getResultCharacterSet(), is(Optional.of(MySQLConstants.DEFAULT_CHARSET)));
    }
    
    @Test
    void assertWithResultCharacterSet() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLConstants.DEFAULT_CHARSET).withResultCharacterSet(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        assertThat(actual.getResultCharacterSet(), is(Optional.of(MySQLCharacterSets.LATIN1_SWEDISH_CI)));
        assertThat(actual.getResultCharset(), is(StandardCharsets.ISO_8859_1));
    }
    
    @Test
    void assertWithoutResultConversion() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI).withoutResultConversion();
        assertThat(actual.getResultCharacterSetName(), is(Optional.empty()));
        assertThat(actual.getResultCharset(), is(StandardCharsets.UTF_8));
        assertThat(actual.getResultCollation(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertWithBinaryResult() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI).withBinaryResult();
        assertThat(actual.getResultCharacterSetName(), is(Optional.of("binary")));
        assertThat(actual.getResultCharset(), is(StandardCharsets.UTF_8));
        assertThat(actual.getResultCollation(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertApplyWithoutResultConversion() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI).withoutResultConversion().apply(attributeMap);
        assertThat(attributeMap.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.ISO_8859_1));
        assertThat(attributeMap.attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
        assertThat(attributeMap.attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertWithConnectionCharacterSet() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLConstants.DEFAULT_CHARSET).withConnectionCharacterSet(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        assertThat(actual.getConnectionCharacterSetName(), is("latin1"));
        assertThat(actual.getConnectionCollationName(), is("latin1_swedish_ci"));
    }
    
    @Test
    void assertWithConnectionCollation() {
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.create(MySQLConstants.DEFAULT_CHARSET).withConnectionCollation(MySQLCharacterSets.UTF8MB4_BIN);
        assertThat(actual.getConnectionCharacterSetName(), is("utf8mb4"));
        assertThat(actual.getConnectionCollation(), is(MySQLCharacterSets.UTF8MB4_BIN));
    }
    
    @Test
    void assertApply() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionCharsetContext expected = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        expected.apply(attributeMap);
        assertThat(attributeMap.attr(MySQLSessionCharsetContext.ATTRIBUTE_KEY).get(), is(expected));
        assertThat(attributeMap.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.ISO_8859_1));
        assertThat(attributeMap.attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.ISO_8859_1));
        assertThat(attributeMap.attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get(), is(MySQLCharacterSets.LATIN1_SWEDISH_CI));
    }
}

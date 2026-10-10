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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBlobTransliteratorTest {
    
    private static final Charset WIN1251 = Charset.forName("windows-1251");
    
    @Test
    void assertPut() {
        assertThat(new FirebirdBlobTransliterator(WIN1251, StandardCharsets.UTF_8).put("Текст".getBytes(WIN1251)), is("Текст".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertPutWithCharacterSplitBetweenPuts() {
        FirebirdBlobTransliterator transliterator = new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251);
        assertThat(transliterator.put(new byte[]{'a', (byte) 0xD0}), is(new byte[]{'a'}));
        assertThat(transliterator.put(new byte[]{(byte) 0xB2, 'b'}), is("вb".getBytes(WIN1251)));
    }
    
    @Test
    void assertPutWithoutConvertibleBytes() {
        assertThrows(TransliterationFailedException.class, () -> new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251).put(new byte[]{(byte) 0xFF, 'a'}));
    }
    
    @Test
    void assertPutAfterInvalidCharacter() {
        FirebirdBlobTransliterator transliterator = new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251);
        assertThat(transliterator.put(new byte[]{'a', (byte) 0xFF}), is(new byte[]{'a'}));
        assertThrows(TransliterationFailedException.class, () -> transliterator.put(new byte[]{'b'}));
    }
    
    @Test
    void assertPutEmptyDataAfterIncompleteCharacter() {
        FirebirdBlobTransliterator transliterator = new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251);
        transliterator.put(new byte[]{'a', (byte) 0xD0});
        assertThrows(TransliterationFailedException.class, () -> transliterator.put(new byte[0]));
    }
    
    @Test
    void assertPutEmptyData() {
        assertThat(new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251).put(new byte[0]), is(new byte[0]));
    }
    
    @Test
    void assertPutWithUnmappableTargetCharacter() {
        assertThrows(TransliterationFailedException.class, () -> new FirebirdBlobTransliterator(StandardCharsets.UTF_8, WIN1251).put("a中".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertPutWithUndefinedSourceByte() {
        assertThat(new FirebirdBlobTransliterator(WIN1251, StandardCharsets.UTF_8).put(new byte[]{'a', (byte) 0x98}), is(new byte[]{'a', 0}));
    }
}

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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

/**
 * BLOB transliterator for Firebird.
 *
 * <p>Mirrors the text transliteration BLOB filter of Firebird for written BLOB data.
 * Every put converts the bytes kept by the previous put followed by the new bytes and keeps an invalid or incomplete character at their end for the next put.
 * A put fails if it cannot convert any byte or the target character set cannot represent a character, and then it keeps the bytes of the previous put.
 * A byte sequence that the source character set does not map is converted to {@code U+0000}, as Firebird does for undefined bytes of its single-byte character sets.
 * Bytes kept when the BLOB is closed are discarded, because Firebird ignores the result of the filter at close.</p>
 */
@RequiredArgsConstructor
public final class FirebirdBlobTransliterator {
    
    private static final byte[] EMPTY = new byte[0];
    
    private static final String UNDEFINED_CHARACTER = String.valueOf(Character.MIN_VALUE);
    
    private final Charset sourceCharset;
    
    private final Charset targetCharset;
    
    private byte[] unusedBytes = EMPTY;
    
    /**
     * Put BLOB data.
     *
     * @param data BLOB data in the source character set
     * @return converted BLOB data in the target character set
     * @throws TransliterationFailedException if no byte can be converted or the target character set cannot represent a character
     */
    public byte[] put(final byte[] data) {
        ByteBuffer input = ByteBuffer.allocate(unusedBytes.length + data.length).put(unusedBytes).put(data);
        input.flip();
        CharBuffer chars = decode(input);
        ShardingSpherePreconditions.checkState(0 == input.limit() || 0 != input.position(), TransliterationFailedException::new);
        byte[] result = encode(chars);
        unusedBytes = Arrays.copyOfRange(input.array(), input.position(), input.limit());
        return result;
    }
    
    private CharBuffer decode(final ByteBuffer input) {
        CharsetDecoder decoder = sourceCharset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(UNDEFINED_CHARACTER);
        CharBuffer result = CharBuffer.allocate((int) Math.ceil(input.remaining() * (double) decoder.maxCharsPerByte()));
        decoder.decode(input, result, false);
        result.flip();
        return result;
    }
    
    private byte[] encode(final CharBuffer chars) {
        try {
            ByteBuffer result = targetCharset.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).encode(chars);
            return Arrays.copyOf(result.array(), result.limit());
        } catch (final CharacterCodingException ex) {
            throw new TransliterationFailedException();
        }
    }
}

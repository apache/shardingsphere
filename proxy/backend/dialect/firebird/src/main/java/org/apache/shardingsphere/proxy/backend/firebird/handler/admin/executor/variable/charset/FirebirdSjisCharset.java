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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Firebird's SJIS_0208 encoding.
 */
final class FirebirdSjisCharset extends Charset {
    
    static final Charset INSTANCE = new FirebirdSjisCharset();
    
    private FirebirdSjisCharset() {
        super("X-Firebird-SJIS-0208", new String[0]);
    }
    
    @Override
    public boolean contains(final Charset charset) {
        return charset instanceof FirebirdSjisCharset || StandardCharsets.US_ASCII.equals(charset);
    }
    
    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    
    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    
    @HighFrequencyInvocation
    private static char decodeAscii(final int value) {
        if (0x5C == value) {
            return '¥';
        }
        return 0x7E == value ? '‾' : (char) value;
    }
    
    @HighFrequencyInvocation
    private static boolean isSingleByteExtended(final int value) {
        return value >= 0xA0 && value <= 0xDF;
    }
    
    @HighFrequencyInvocation
    private static boolean isLead(final int value) {
        return value >= 0x80 && value <= 0x9F || value >= 0xE0 && value <= 0xEF;
    }
    
    private static final class Decoder extends CharsetDecoder {
        
        private Decoder(final Charset charset) {
            super(charset, 1F, 1F);
        }
        
        @Override
        @HighFrequencyInvocation
        protected CoderResult decodeLoop(final ByteBuffer input, final CharBuffer output) {
            while (input.hasRemaining()) {
                int position = input.position();
                int first = input.get(position) & 0xFF;
                int length = 1;
                char value;
                if (first < 0x80) {
                    value = decodeAscii(first);
                } else if (isSingleByteExtended(first)) {
                    value = 0xA0 == first ? '�' : (char) ('｡' + first - 0xA1);
                } else if (isLead(first)) {
                    if (input.remaining() < 2) {
                        return CoderResult.UNDERFLOW;
                    }
                    int second = input.get(position + 1) & 0xFF;
                    if (second < 0x40 || second > 0xFC) {
                        return CoderResult.malformedForLength(1);
                    }
                    length = 2;
                    value = Tables.DECODE[(first << 8) | second];
                } else {
                    return CoderResult.malformedForLength(1);
                }
                if (!output.hasRemaining()) {
                    return CoderResult.OVERFLOW;
                }
                output.put(value);
                input.position(position + length);
            }
            return CoderResult.UNDERFLOW;
        }
    }
    
    private static final class Encoder extends CharsetEncoder {
        
        private Encoder(final Charset charset) {
            super(charset, 2F, 2F, new byte[]{0x3F});
        }
        
        @Override
        @HighFrequencyInvocation
        protected CoderResult encodeLoop(final CharBuffer input, final ByteBuffer output) {
            while (input.hasRemaining()) {
                int position = input.position();
                char value = input.get(position);
                if (Character.isSurrogate(value)) {
                    if (!Character.isHighSurrogate(value)) {
                        return CoderResult.malformedForLength(1);
                    }
                    if (input.remaining() < 2) {
                        return CoderResult.UNDERFLOW;
                    }
                    return Character.isLowSurrogate(input.get(position + 1)) ? CoderResult.unmappableForLength(2) : CoderResult.malformedForLength(1);
                }
                int encoded = Tables.ENCODE[value];
                if (encoded < 0) {
                    return CoderResult.unmappableForLength(1);
                }
                int length = encoded >>> 16;
                if (output.remaining() < length) {
                    return CoderResult.OVERFLOW;
                }
                if (2 == length) {
                    output.put((byte) (encoded >>> 8));
                }
                output.put((byte) encoded);
                input.position(position + 1);
            }
            return CoderResult.UNDERFLOW;
        }
    }
    
    private static final class Tables {
        
        private static final char[] DECODE = createDecodeTable();
        
        private static final int[] ENCODE = createEncodeTable();
        
        private static char[] createDecodeTable() {
            char[] result = new char[65536];
            CharsetDecoder decoder = Charset.forName("Shift_JIS").newDecoder();
            ByteBuffer bytes = ByteBuffer.allocate(2);
            CharBuffer chars = CharBuffer.allocate(2);
            for (int first = 0x80; first <= 0xEF; first++) {
                if (!isLead(first)) {
                    continue;
                }
                fillDecodeRow(result, first, decoder, bytes, chars);
            }
            result[0x815C] = '―';
            result[0x815F] = '\\';
            return result;
        }
        
        private static void fillDecodeRow(final char[] decodeTable, final int first, final CharsetDecoder decoder, final ByteBuffer bytes, final CharBuffer chars) {
            for (int second = 0x40; second <= 0xFC; second++) {
                bytes.clear();
                bytes.put((byte) first).put((byte) (0x7F == second ? 0x7E : second)).flip();
                chars.clear();
                CoderResult status = decoder.reset().decode(bytes, chars, true);
                char value = status.isUnderflow() && !bytes.hasRemaining() && 1 == chars.position() ? chars.get(0) : '�';
                decodeTable[(first << 8) | second] = value;
            }
        }
        
        private static int[] createEncodeTable() {
            int[] result = new int[65536];
            Arrays.fill(result, -1);
            CharsetEncoder encoder = Charset.forName("Shift_JIS").newEncoder();
            CharBuffer chars = CharBuffer.allocate(1);
            ByteBuffer bytes = ByteBuffer.allocate(2);
            for (int value = 0; value < result.length; value++) {
                chars.clear();
                chars.put((char) value).flip();
                bytes.clear();
                CoderResult status = encoder.reset().encode(chars, bytes, true);
                if (status.isUnderflow() && !chars.hasRemaining() && bytes.position() > 0) {
                    int length = bytes.position();
                    int encoded = (bytes.get(0) & 0xFF) << (2 == length ? 8 : 0);
                    result[value] = (length << 16) | encoded | (2 == length ? bytes.get(1) & 0xFF : 0);
                }
            }
            result['\\'] = (2 << 16) | 0x815F;
            result['—'] = -1;
            result['―'] = (2 << 16) | 0x815C;
            result['＼'] = -1;
            return result;
        }
    }
}

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

import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ConversionErrorException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.MalformedStringException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.NumericOutOfRangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.gds.ISCConstants;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BLOB parameter converter for Firebird.
 *
 * <p>Mirrors how Firebird moves a BLOB value of a client message to the statement parameter: {@code mapInOut}, {@code blb::move} and {@code move_to_string}.
 * A text BLOB is moved to a text BLOB parameter with transliteration between two different real character sets.
 * A BLOB is moved to another type as text read through the BLOB filter to the parameter character set, or to ASCII for other types, and the text is converted to the parameter type.
 * Firebird describes text parameters in the character set of the connection if it is a real one; otherwise the text keeps the character set of the BLOB.
 * Integer and BOOLEAN parameters get the value converted as Firebird converts it; other types get the ASCII text, because the proxy does not describe their scale.
 * An error of the move is reported as {@link ParameterConversionException}.</p>
 */
@HighFrequencyInvocation
public final class FirebirdBlobParameterConverter {
    
    private static final int MAX_STRING_LENGTH = 65535;
    
    private static final int CHARSET_ASCII = 2;
    
    private static final int CHARSET_SJIS = 5;
    
    private static final int CHARSET_EUCJ = 6;
    
    private static final int CHARSET_KSC5601 = 44;
    
    private static final int CHARSET_BIG5 = 56;
    
    private static final int CHARSET_GB2312 = 57;
    
    private static final Collection<Integer> UTF8_CHARSET_IDS = Arrays.asList(3, 4);
    
    private static final Collection<Integer> DOUBLE_BYTE_CHARSET_IDS = Arrays.asList(CHARSET_SJIS, CHARSET_EUCJ, CHARSET_KSC5601, CHARSET_BIG5, CHARSET_GB2312);
    
    private static final int VALIDATION_BUFFER_SIZE = 1024;
    
    private static final int EXPONENT_LIMIT = 3276;
    
    private static final String UNMAPPABLE_REPLACEMENT = String.valueOf(Character.MIN_VALUE);
    
    private final int connectionCharsetId;
    
    private final Optional<Charset> connectionCharset;
    
    private final Map<Charset, CharsetDecoder> wellFormedDecoders = new HashMap<>(2, 1F);
    
    private final Map<Charset, CharsetDecoder> replacingDecoders = new HashMap<>(2, 1F);
    
    public FirebirdBlobParameterConverter(final int connectionCharsetId) {
        this.connectionCharsetId = connectionCharsetId;
        connectionCharset = FirebirdBlobParameterBufferUtils.findJavaCharset(connectionCharsetId);
    }
    
    /**
     * Judge whether Firebird reads the BLOB while it moves the message to the parameter.
     *
     * <p>Firebird reads the BLOB to convert it to another type, sub type or character set, or to validate text moved from {@code NONE} or {@code BINARY};
     * otherwise it moves the BLOB ID, and the BLOB is read when the statement uses it.</p>
     *
     * @param field BLOB field of message
     * @param parameter statement parameter
     * @return whether Firebird reads the BLOB while it moves the message
     */
    public boolean isReadOnMove(final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        if (FirebirdBinaryColumnType.BLOB != parameter.getColumnType()) {
            return true;
        }
        if (isBlobValidationRequired(field, parameter)) {
            return true;
        }
        Optional<Integer> parameterSubType = parameter.findBlobSubType();
        if (ISCConstants.BLOB_SUB_TYPE_BINARY == field.getSubType() || !parameterSubType.isPresent() || ISCConstants.BLOB_SUB_TYPE_BINARY == parameterSubType.get()) {
            return false;
        }
        int fieldCharsetId = getFieldCharsetId(field);
        return field.getSubType() != parameterSubType.get() || ISCConstants.BLOB_SUB_TYPE_TEXT == field.getSubType()
                && fieldCharsetId > ISCConstants.CS_BINARY && connectionCharsetId > ISCConstants.CS_BINARY && fieldCharsetId != connectionCharsetId;
    }
    
    private boolean isBlobValidationRequired(final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        Optional<Integer> parameterSubType = parameter.findBlobSubType();
        int fieldCharsetId = ISCConstants.BLOB_SUB_TYPE_TEXT == field.getSubType() ? getFieldCharsetId(field) : ISCConstants.CS_BINARY;
        return parameterSubType.isPresent() && ISCConstants.BLOB_SUB_TYPE_TEXT == parameterSubType.get() && connectionCharsetId > ISCConstants.CS_BINARY
                && (ISCConstants.BLOB_SUB_TYPE_BINARY == field.getSubType() || ISCConstants.BLOB_SUB_TYPE_TEXT == field.getSubType())
                && (ISCConstants.CS_NONE == fieldCharsetId || ISCConstants.CS_BINARY == fieldCharsetId);
    }
    
    private int getFieldCharsetId(final FirebirdBatchColumnDescriptor field) {
        return FirebirdBlobParameterBufferUtils.getActualCharset(field.getScale(), connectionCharsetId);
    }
    
    /**
     * Convert BLOB value of message to parameter value.
     *
     * @param content BLOB content
     * @param field BLOB field of message
     * @param parameter statement parameter
     * @return parameter value
     * @throws ParameterConversionException if Firebird cannot move the BLOB value to the parameter
     */
    public Object convert(final byte[] content, final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        try {
            return convertValue(content, field, parameter);
        } catch (final SQLDialectException ex) {
            throw new ParameterConversionException(ex);
        }
    }
    
    private Object convertValue(final byte[] content, final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        FirebirdBinaryColumnType type = parameter.getColumnType();
        if (FirebirdBinaryColumnType.BLOB == type) {
            return toBlob(content, field, parameter);
        }
        if (isText(type)) {
            return toText(content, field, parameter);
        }
        String result = new String(readText(content, field, CHARSET_ASCII), StandardCharsets.ISO_8859_1);
        if (FirebirdBinaryColumnType.BOOLEAN == type) {
            return toBoolean(result);
        }
        return isInteger(type) ? toInteger(result, type) : result;
    }
    
    private Object toBlob(final byte[] content, final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        if (isBlobValidationRequired(field, parameter)) {
            checkBlobWellFormed(content);
            return content;
        }
        if (!isReadOnMove(field, parameter)) {
            return content;
        }
        int targetSubType = parameter.findBlobSubType().get();
        ShardingSpherePreconditions.checkState(ISCConstants.BLOB_SUB_TYPE_TEXT == targetSubType, () -> new BlobFilterNotFoundException(field.getSubType(), targetSubType));
        byte[] text = filter(content, field, connectionCharsetId);
        return connectionCharset.isPresent() ? decode(text, connectionCharset.get(), false) : text;
    }
    
    private void checkBlobWellFormed(final byte[] content) {
        if (CHARSET_ASCII == connectionCharsetId) {
            for (byte each : content) {
                ShardingSpherePreconditions.checkState(each >= 0, MalformedStringException::new);
            }
        } else if (UTF8_CHARSET_IDS.contains(connectionCharsetId)) {
            checkUtf8BlobWellFormed(content);
        } else if (DOUBLE_BYTE_CHARSET_IDS.contains(connectionCharsetId)) {
            checkDoubleByteBlobWellFormed(content);
        }
    }
    
    private void checkUtf8BlobWellFormed(final byte[] content) {
        CharsetDecoder decoder = wellFormedDecoders.computeIfAbsent(connectionCharset.get(), each -> createDecoder(each, CodingErrorAction.REPORT));
        decoder.reset();
        ByteBuffer input = ByteBuffer.wrap(content);
        CharBuffer chars = CharBuffer.allocate(Math.min(content.length, VALIDATION_BUFFER_SIZE));
        CoderResult result;
        do {
            result = decoder.decode(input, chars, true);
            chars.clear();
        } while (result.isOverflow());
        ShardingSpherePreconditions.checkState(result.isUnderflow(), MalformedStringException::new);
    }
    
    private void checkDoubleByteBlobWellFormed(final byte[] content) {
        int index = 0;
        while (index < content.length) {
            int first = content[index++] & 0xFF;
            if (isDoubleByteStart(first)) {
                ShardingSpherePreconditions.checkState(index < content.length, MalformedStringException::new);
                int second = content[index++] & 0xFF;
                if (CHARSET_GB2312 == connectionCharsetId) {
                    ShardingSpherePreconditions.checkState(first >= 0xA1 && first <= 0xFE && second >= 0xA1 && second <= 0xFE, MalformedStringException::new);
                }
            }
        }
    }
    
    private boolean isDoubleByteStart(final int value) {
        if (CHARSET_SJIS == connectionCharsetId) {
            return value >= 0x80 && value <= 0x9F || value >= 0xE0 && value <= 0xEF;
        }
        if (CHARSET_BIG5 == connectionCharsetId) {
            return value >= 0xA1 && value <= 0xFE;
        }
        return value >= 0x80;
    }
    
    private boolean isText(final FirebirdBinaryColumnType type) {
        return FirebirdBinaryColumnType.TEXT == type || FirebirdBinaryColumnType.VARYING == type || FirebirdBinaryColumnType.LEGACY_TEXT == type || FirebirdBinaryColumnType.LEGACY_VARYING == type;
    }
    
    private Object toText(final byte[] content, final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter) {
        byte[] text = readText(content, field, connectionCharset.isPresent() ? connectionCharsetId : ISCConstants.CS_NONE);
        Optional<Charset> textCharset = connectionCharset.isPresent() ? connectionCharset : findFieldCharset(field);
        if (!textCharset.isPresent()) {
            return text;
        }
        String result = decode(text, textCharset.get(), true);
        Optional<Integer> length = parameter.findColumnLength();
        return length.isPresent() ? truncate(result, length.get()) : result;
    }
    
    private Optional<Charset> findFieldCharset(final FirebirdBatchColumnDescriptor field) {
        return ISCConstants.BLOB_SUB_TYPE_TEXT == field.getSubType() ? FirebirdBlobParameterBufferUtils.findJavaCharset(getFieldCharsetId(field)) : Optional.empty();
    }
    
    private String truncate(final String text, final int length) {
        int textLength = text.codePointCount(0, text.length());
        if (textLength <= length) {
            return text;
        }
        int trimmedEnd = text.length();
        while (trimmedEnd > 0 && ' ' == text.charAt(trimmedEnd - 1)) {
            trimmedEnd--;
        }
        ShardingSpherePreconditions.checkState(textLength - (text.length() - trimmedEnd) <= length, () -> new StringTruncationException(length, textLength));
        return text.substring(0, text.offsetByCodePoints(0, length));
    }
    
    private boolean toBoolean(final String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isBooleanWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isBooleanWhitespace(value.charAt(end - 1))) {
            end--;
        }
        String literal = value.substring(start, end);
        if ("TRUE".equalsIgnoreCase(literal)) {
            return true;
        }
        ShardingSpherePreconditions.checkState("FALSE".equalsIgnoreCase(literal), () -> createConversionError(value));
        return false;
    }
    
    private boolean isBooleanWhitespace(final char value) {
        return ' ' == value || value >= '\t' && value <= '\r';
    }
    
    private boolean isInteger(final FirebirdBinaryColumnType type) {
        return FirebirdBinaryColumnType.SHORT == type || FirebirdBinaryColumnType.LONG == type || FirebirdBinaryColumnType.INT64 == type;
    }
    
    private Number toInteger(final String value, final FirebirdBinaryColumnType type) {
        if (FirebirdBinaryColumnType.SHORT == type) {
            return (short) toLong(value, Short.SIZE);
        }
        if (FirebirdBinaryColumnType.LONG == type) {
            return (int) toLong(value, Integer.SIZE);
        }
        return toLong(value, Long.SIZE);
    }
    
    private long toLong(final String value, final int bits) {
        int start = 0;
        while (start < value.length() && ' ' == value.charAt(start)) {
            start++;
        }
        if (start + 2 < value.length() && '0' == value.charAt(start) && 'X' == Character.toUpperCase(value.charAt(start + 1))) {
            return toHexLong(value, start + 2, bits);
        }
        return toDecimalLong(value, start, Long.MAX_VALUE >>> Long.SIZE - bits);
    }
    
    private long toHexLong(final String value, final int start, final int bits) {
        int digitsEnd = start;
        while (digitsEnd < value.length() && '\0' != value.charAt(digitsEnd) && ' ' != value.charAt(digitsEnd)) {
            digitsEnd++;
        }
        ShardingSpherePreconditions.checkState(isSpacesToEnd(value, digitsEnd) && (value.length() - start) * 4L <= bits, () -> createConversionError(value));
        long result = 0L;
        for (int i = start; i < digitsEnd; i++) {
            char each = value.charAt(i);
            ShardingSpherePreconditions.checkState(each < 128 && Character.digit(each, 16) >= 0, () -> createConversionError(value));
            result = result * 16 + Character.digit(each, 16);
        }
        return digitsEnd - start <= 8 ? result & 0xFFFFFFFFL : result;
    }
    
    private boolean isSpacesToEnd(final String value, final int start) {
        for (int i = start; i < value.length(); i++) {
            if (' ' != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private ConversionErrorException createConversionError(final String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char each = value.charAt(i);
            result.append(each < ' ' || each > Byte.MAX_VALUE ? String.format("#x%02x", (int) each) : String.valueOf(each));
        }
        return new ConversionErrorException(result.toString());
    }
    
    private long toDecimalLong(final String value, final int start, final long maxValue) {
        long limit = maxValue / 10;
        long result = 0L;
        int scale = 0;
        int sign = 0;
        boolean digitSeen = false;
        boolean fraction = false;
        int index = start;
        for (; index < value.length() && !isExponent(value.charAt(index)); index++) {
            char each = value.charAt(index);
            if (each >= '0' && each <= '9') {
                digitSeen = true;
                if (Long.compareUnsigned(result, limit) > 0) {
                    ShardingSpherePreconditions.checkState(fraction && isZerosToEnd(value, index), NumericOutOfRangeException::new);
                    index = value.length();
                    break;
                }
                ShardingSpherePreconditions.checkState(result != limit || each <= (-1 == sign ? '8' : '7'), NumericOutOfRangeException::new);
                result = result * 10 + each - '0';
                if (fraction) {
                    scale--;
                }
            } else if ('.' == each && !fraction) {
                fraction = true;
            } else if ('-' == each && !digitSeen && 0 == sign && !fraction) {
                sign = -1;
            } else if ('+' == each && !digitSeen && 0 == sign && !fraction) {
                sign = 1;
            } else {
                ShardingSpherePreconditions.checkState(' ' == each && isSpacesToEnd(value, index), () -> createConversionError(value));
                index = value.length();
                break;
            }
        }
        ShardingSpherePreconditions.checkState(digitSeen, () -> createConversionError(value));
        if (-1 == sign && result != -maxValue - 1) {
            result = -result;
        }
        if (index < value.length()) {
            scale += toExponent(value, index + 1);
        }
        return adjustForScale(result, -scale, limit);
    }
    
    private boolean isExponent(final char value) {
        return 'e' == value || 'E' == value;
    }
    
    private boolean isZerosToEnd(final String value, final int start) {
        for (int i = start; i < value.length(); i++) {
            if ('0' != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private int toExponent(final String value, final int start) {
        int result = 0;
        int sign = 0;
        boolean digitSeen = false;
        for (int index = start; index < value.length(); index++) {
            char each = value.charAt(index);
            if (each >= '0' && each <= '9') {
                digitSeen = true;
                result = result * 10 + each - '0';
                ShardingSpherePreconditions.checkState(result < EXPONENT_LIMIT, NumericOutOfRangeException::new);
            } else if ('-' == each && !digitSeen && 0 == sign) {
                sign = -1;
            } else if ('+' == each && !digitSeen && 0 == sign) {
                sign = 1;
            } else {
                ShardingSpherePreconditions.checkState(' ' == each && isSpacesToEnd(value, index), () -> createConversionError(value));
                break;
            }
        }
        ShardingSpherePreconditions.checkState(digitSeen, () -> createConversionError(value));
        return -1 == sign ? -result : result;
    }
    
    private long adjustForScale(final long value, final int scale, final long limit) {
        long result = value;
        if (scale > 0) {
            long fraction = 0L;
            for (int i = scale; i > 0; i--) {
                fraction = 1 == i ? result % 10 : fraction;
                result /= 10;
            }
            if (fraction > 4) {
                return result + 1;
            }
            return fraction < -4 ? result - 1 : result;
        }
        for (int i = scale; i < 0; i++) {
            ShardingSpherePreconditions.checkState(result <= limit && result >= -limit, NumericOutOfRangeException::new);
            result *= 10;
        }
        return result;
    }
    
    private byte[] readText(final byte[] content, final FirebirdBatchColumnDescriptor field, final int charsetId) {
        byte[] result = filter(content, field, charsetId);
        ShardingSpherePreconditions.checkState(result.length <= MAX_STRING_LENGTH, BlobTruncationException::new);
        return result;
    }
    
    private byte[] filter(final byte[] content, final FirebirdBatchColumnDescriptor field, final int charsetId) {
        int subType = field.getSubType();
        if (ISCConstants.BLOB_SUB_TYPE_TEXT == subType) {
            Optional<FirebirdBlobTransliterator> transliterator = FirebirdBlobParameterBufferUtils.createTextTransliterator(field.getScale(), charsetId, connectionCharsetId);
            return transliterator.isPresent() ? transliterator.get().read(content) : content;
        }
        if (ISCConstants.CS_BINARY == charsetId) {
            return content;
        }
        if (ISCConstants.BLOB_SUB_TYPE_BINARY == subType) {
            return filterText(content);
        }
        throw FirebirdBlobParameterBufferUtils.hasInternalTextFilter(subType) ? new UnsupportedBlobFilterException() : new BlobFilterNotFoundException(subType, ISCConstants.BLOB_SUB_TYPE_TEXT);
    }
    
    private byte[] filterText(final byte[] content) {
        ByteArrayOutputStream result = new ByteArrayOutputStream(content.length);
        for (byte each : content) {
            if ('\n' != each) {
                result.write(isPrintable(each) ? each : '.');
            }
        }
        return result.toByteArray();
    }
    
    private boolean isPrintable(final byte value) {
        return value >= '\b' && value <= '\r' || value >= ' ' && value < Byte.MAX_VALUE;
    }
    
    private String decode(final byte[] text, final Charset charset, final boolean wellFormed) {
        CharsetDecoder decoder = wellFormed
                ? wellFormedDecoders.computeIfAbsent(charset, each -> createDecoder(each, CodingErrorAction.REPORT))
                : replacingDecoders.computeIfAbsent(charset, each -> createDecoder(each, CodingErrorAction.REPLACE));
        try {
            return decoder.reset().decode(ByteBuffer.wrap(text)).toString();
        } catch (final CharacterCodingException ex) {
            throw new MalformedStringException();
        }
    }
    
    private CharsetDecoder createDecoder(final Charset charset, final CodingErrorAction malformedInputAction) {
        return charset.newDecoder().onMalformedInput(malformedInputAction).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(UNMAPPABLE_REPLACEMENT);
    }
}

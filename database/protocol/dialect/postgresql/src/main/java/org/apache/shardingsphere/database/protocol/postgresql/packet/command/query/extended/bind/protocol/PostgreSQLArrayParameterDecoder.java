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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * PostgreSQL array parameter decoder.
 */
public final class PostgreSQLArrayParameterDecoder {
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Decode int2 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public short[] decodeInt2Array(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        short[] result = new short[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Short.parseShort(each);
        }
        return result;
    }
    
    /**
     * Decode int4 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public int[] decodeInt4Array(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        int[] result = new int[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Integer.parseInt(each);
        }
        return result;
    }
    
    /**
     * Decode int8 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public long[] decodeInt8Array(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        long[] result = new long[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Long.parseLong(each);
        }
        return result;
    }
    
    /**
     * Decode float4 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public float[] decodeFloat4Array(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        float[] result = new float[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Float.parseFloat(each);
        }
        return result;
    }
    
    /**
     * Decode float8 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public double[] decodeFloat8Array(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        double[] result = new double[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Double.parseDouble(each);
        }
        return result;
    }
    
    /**
     * Decode bool array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public boolean[] decodeBoolArray(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        boolean[] result = new boolean[parameterElements.size()];
        int index = 0;
        for (String each : parameterElements) {
            result[index++] = Boolean.parseBoolean(each);
        }
        return result;
    }
    
    /**
     * Decode string array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public String[] decodeStringArray(final byte[] parameterBytes, final boolean isBinary) {
        ShardingSpherePreconditions.checkState(!isBinary, () -> new UnsupportedSQLOperationException("binary mode"));
        String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
        Collection<String> parameterElements = decodeText(parameterValue);
        return parameterElements.toArray(EMPTY_STRING_ARRAY);
    }
    
    /**
     * Decode parameter in text mode.
     *
     * @param value protocol parameter value
     * @return decoded parameter value elements
     */
    private Collection<String> decodeText(final String value) {
        Preconditions.checkArgument(value.length() >= 2, "value length less than 2");
        Preconditions.checkArgument('{' == value.charAt(0) && '}' == value.charAt(value.length() - 1), "value not start with '{' or not end with '}'");
        String[] elements = value.substring(1, value.length() - 1).split(",");
        return Arrays.stream(elements).map(each -> "NULL".equals(each) ? null : decodeElementText(each)).collect(Collectors.toList());
    }
    
    private static String decodeElementText(final String element) {
        String result = element;
        if ('"' == result.charAt(0) && '"' == result.charAt(result.length() - 1)) {
            result = result.substring(1, result.length() - 1);
        }
        while (result.contains("\\\"")) {
            result = result.replace("\\\"", "\"");
        }
        while (result.contains("\\\\")) {
            result = result.replace("\\\\", "\\");
        }
        return result;
    }
}

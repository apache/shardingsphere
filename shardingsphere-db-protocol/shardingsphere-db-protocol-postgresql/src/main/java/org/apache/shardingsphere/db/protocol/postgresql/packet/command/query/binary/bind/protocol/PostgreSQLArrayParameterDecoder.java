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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            short[] result = new short[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Short.parseShort(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode int4 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public int[] decodeInt4Array(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            int[] result = new int[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Integer.parseInt(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode int8 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public long[] decodeInt8Array(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            long[] result = new long[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Long.parseLong(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode float4 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public float[] decodeFloat4Array(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            float[] result = new float[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Float.parseFloat(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode float8 array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public double[] decodeFloat8Array(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            double[] result = new double[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Double.parseDouble(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode bool array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public boolean[] decodeBoolArray(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            boolean[] result = new boolean[parameterElements.size()];
            int index = 0;
            for (String element : parameterElements) {
                result[index++] = Boolean.parseBoolean(element);
            }
            return result;
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode string array parameter.
     *
     * @param parameterBytes protocol parameter value
     * @param isBinary whether parameter value is binary or not
     * @return int array
     */
    public String[] decodeStringArray(final byte[] parameterBytes, final boolean isBinary) {
        if (!isBinary) {
            String parameterValue = new String(parameterBytes, StandardCharsets.UTF_8);
            Collection<String> parameterElements = decodeText(parameterValue);
            return parameterElements.toArray(EMPTY_STRING_ARRAY);
        }
        throw new UnsupportedOperationException("binary mode");
    }
    
    /**
     * Decode parameter in text mode.
     *
     * @param value protocol parameter value
     * @return decoded parameter value elements
     */
    private Collection<String> decodeText(final String value) {
        if (value.length() < 2) {
            throw new IllegalArgumentException("value length less than 2");
        }
        if ('{' != value.charAt(0) || '}' != value.charAt(value.length() - 1)) {
            throw new IllegalArgumentException("value not start with '{' or not end with '}'");
        }
        String[] elements = value.substring(1, value.length() - 1).split(",");
        return Arrays.stream(elements).map(each -> {
            if ("NULL".equals(each)) {
                return null;
            }
            if ('"' == each.charAt(0) && '"' == each.charAt(each.length() - 1)) {
                each = each.substring(1, each.length() - 1);
            }
            while (each.contains("\\\"")) {
                each = each.replace("\\\"", "\"");
            }
            while (each.contains("\\\\")) {
                each = each.replace("\\\\", "\\");
            }
            return each;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
}

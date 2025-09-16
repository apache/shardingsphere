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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Parser;
import org.postgresql.util.internal.Nullness;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL array parameter decoder.
 */
public final class PostgreSQLArrayParameterDecoder {
    
    /**
     * decode numberArray String to a array.
     *
     * @param parameterValue arrayString
     * @return array
     */
    @SuppressWarnings("rawtypes")
    public Object decodeNumberArray(final String parameterValue) {
        char delim = ',';
        PgDimensionsArrayList list = decodeFromString(parameterValue, delim);
        int dims = list.dimensionsCount;
        int[] dimensionLengths = new int[dims];
        dimensionLengths[0] = list.size();
        List tmpList = (List) list.get(0);
        for (int i = 1; i < dims; i++) {
            dimensionLengths[i] = Nullness.castNonNull(tmpList, "first element of adjustedList is null").size();
            if (i != dims - 1) {
                tmpList = (List) tmpList.get(0);
            }
        }
        Object[] result = (Object[]) Array.newInstance(Number.class, dimensionLengths);
        if (result instanceof Number[]) {
            parserNumber((Number[]) result, list);
        } else {
            storeStringValues(result, list, dimensionLengths, 0);
        }
        return result;
    }
    
    /**
     * decode and copy to result.
     *
     * @param array result
     * @param list source String array
     * @param dimensionLengths dimensionLengths
     * @param dim dim
     */
    @SuppressWarnings("rawtypes")
    private static void storeStringValues(final Object[] array, final List list, final int[] dimensionLengths,
                                          final int dim) {
        for (int i = 0; i < dimensionLengths[dim]; i++) {
            Object element = Nullness.castNonNull(list.get(i), "list.get(i)");
            if (dim == dimensionLengths.length - 2) {
                parserNumber((Number[]) array[i], (List) element);
            } else {
                storeStringValues((Object[]) array[i], (List) element, dimensionLengths, dim + 1);
            }
        }
    }
    
    /**
     * parse a String list to Number[].
     *
     * @param target Number[]
     * @param source String list
     */
    @SuppressWarnings("rawtypes")
    private static void parserNumber(final Number[] target, final List source) {
        for (int i = 0; i < target.length; i++) {
            Object o = source.get(i);
            if (o == null) {
                continue;
            }
            target[i] = parseNumber(o.toString());
        }
    }
    
    /**
     * parse String to Number.
     *
     * @param stringValue String
     * @return BigDecimal or Double
     */
    private static Number parseNumber(final String stringValue) {
        String value = stringValue;
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 2) {
            value = stringValue.substring(1, value.length() - 1);
        }
        if (Double.toString(Double.NaN).equals(value)) {
            return Double.NaN;
        }
        if (Double.toString(Double.POSITIVE_INFINITY).equals(value)) {
            return Double.POSITIVE_INFINITY;
        }
        if (Double.toString(Double.NEGATIVE_INFINITY).equals(value)) {
            return Double.POSITIVE_INFINITY;
        }
        return new BigDecimal(value);
    }
    
    /**
     * decode String array.
     *
     * @param fieldString string result
     * @param delim delim
     * @return PgDimensionsArrayList
     */
    private PgDimensionsArrayList decodeFromString(final String fieldString, final char delim) {
        PgDimensionsArrayList result = new PgDimensionsArrayList();
        if (fieldString == null) {
            return result;
        }
        char[] chars = fieldString.toCharArray();
        StringBuilder buffer = null;
        boolean insideString = false;
        // needed for checking if NULL value occurred
        boolean wasInsideString = false;
        // array dimension arrays
        List<PgDimensionsArrayList> dims = new ArrayList<>(10);
        // currently processed array
        PgDimensionsArrayList curArray = result;
        // Starting with 8.0 non-standard (beginning index
        // isn't 1) bounds the dimensions are returned in the
        // data formatted like so "[0:3]={0,1,2,3,4}".
        // Older versions simply do not return the bounds.
        //
        // Right now we ignore these bounds, but we could
        // consider allowing these index values to be used
        // even though the JDBC spec says 1 is the first
        // index. I'm not sure what a client would like
        // to see, so we just retain the old behavior.
        int startOffset = 0;
        if (chars[0] == '[') {
            while (chars[startOffset] != '=') {
                startOffset++;
            }
            startOffset++;
        }
        int i = startOffset;
        while (i < chars.length) {
            // escape character that we need to skip
            if (chars[i] == '\\') {
                i++;
            } else if (!insideString && chars[i] == '{') {
                // subarray start
                if (dims.isEmpty()) {
                    dims.add(result);
                } else {
                    PgDimensionsArrayList array = new PgDimensionsArrayList();
                    PgDimensionsArrayList parent = dims.get(dims.size() - 1);
                    parent.add(array);
                    dims.add(array);
                }
                curArray = dims.get(dims.size() - 1);
                // number of dimensions
                for (int t = i + 1; t < chars.length; t++) {
                    if (Character.isWhitespace(chars[t])) {
                        continue;
                    } else if (chars[t] == '{') {
                        curArray.dimensionsCount++;
                    } else {
                        break;
                    }
                }
                buffer = new StringBuilder();
                i++;
                continue;
            } else if (chars[i] == '"') {
                // quoted element
                insideString = !insideString;
                wasInsideString = true;
                i++;
                continue;
            } else if (!insideString && Parser.isArrayWhiteSpace(chars[i])) {
                // white space
                i++;
                continue;
            } else if (!insideString && (chars[i] == delim || chars[i] == '}') || i == chars.length - 1) {
                // array end or element end
                // when character that is a part of array element
                if (chars[i] != '"' && chars[i] != '}' && chars[i] != delim && buffer != null) {
                    buffer.append(chars[i]);
                }
                String bufferString = buffer == null ? null : buffer.toString();
                // add element to current array
                if (bufferString != null && (!bufferString.isEmpty() || wasInsideString)) {
                    curArray.add(!wasInsideString && "NULL".equals(bufferString) ? null : bufferString);
                }
                wasInsideString = false;
                buffer = new StringBuilder();
                // when end of an array
                if (chars[i] != '}') {
                    i++;
                    continue;
                }
                dims.remove(dims.size() - 1);
                // when multi-dimension
                if (!dims.isEmpty()) {
                    curArray = dims.get(dims.size() - 1);
                }
                buffer = null;
                i++;
                continue;
            }
            if (buffer != null) {
                buffer.append(chars[i]);
            }
            i++;
        }
        return result;
    }
    
    private static final class PgDimensionsArrayList extends ArrayList<@Nullable Object> {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * How many dimensions.
         */
        private int dimensionsCount = 1;
        
    }
}

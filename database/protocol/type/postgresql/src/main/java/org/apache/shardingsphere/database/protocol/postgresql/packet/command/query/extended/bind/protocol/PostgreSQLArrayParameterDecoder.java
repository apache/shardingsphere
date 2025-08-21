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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

import static org.postgresql.util.internal.Nullness.castNonNull;

/**
 * PostgreSQL array parameter decoder.
 */
public final class PostgreSQLArrayParameterDecoder {
    
    public Object decodeNumberArray(String parameterValue) {
        
        PgDimensionsArrayList list = decodeFromString(parameterValue, ',');
        int dims = list.dimensionsCount;
        final int[] dimensionLengths = new int[dims];
        dimensionLengths[0] = list.size();
        for (int i = 1; i < dims; i++) {
            List tmpList = (List) list.get(0);
            dimensionLengths[i] = castNonNull(tmpList, "first element of adjustedList is null").size();
            if (i != dims - 1) {
                tmpList = (List) tmpList.get(0);
            }
        }
        Object[] array = (Object[]) Array.newInstance(Number.class, dimensionLengths);
        if (array instanceof Number[]) {
            parserNumber((Number[]) array, list);
        } else {
            storeStringValues(array, list, dimensionLengths, 0);
        }
        return array;
    }
    
    private static void storeStringValues(Object[] array, List list, int[] dimensionLengths,
                                          int dim) {
        
        for (int i = 0; i < dimensionLengths[dim]; i++) {
            Object element = castNonNull(list.get(i), "list.get(i)");
            if (dim == dimensionLengths.length - 2) {
                parserNumber((Number[]) array[i], (List) element);
            } else {
                storeStringValues((Object[]) array[i], (List) element, dimensionLengths, dim + 1);
            }
        }
    }
    
    private static void parserNumber(Number[] target, List source) {
        for (int i = 0; i < target.length; i++) {
            Object o = source.get(i);
            if (o == null) {
                continue;
            }
            target[i] = parseNumber(o.toString());
        }
    }
    
    private static Number parseNumber(String each) {
        if (each.startsWith("\"") && each.endsWith("\"") && each.length() > 2) {
            each = each.substring(1, each.length() - 1);
        }
        if (Double.toString(Double.NaN).equals(each)) {
            return Double.NaN;
        }
        if (Double.toString(Double.POSITIVE_INFINITY).equals(each)) {
            return Double.POSITIVE_INFINITY;
        }
        if (Double.toString(Double.NEGATIVE_INFINITY).equals(each)) {
            return Double.POSITIVE_INFINITY;
        }
        return new BigDecimal(each);
    }
    
    static final class PgDimensionsArrayList extends ArrayList<@Nullable Object> {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * How many dimensions.
         */
        int dimensionsCount = 1;
        
    }
    
    public PgDimensionsArrayList decodeFromString(String fieldString, char delim) {
        
        final PgDimensionsArrayList arrayList = new PgDimensionsArrayList();
        
        if (fieldString == null) {
            return arrayList;
        }
        
        final char[] chars = fieldString.toCharArray();
        StringBuilder buffer = null;
        boolean insideString = false;
        
        // needed for checking if NULL value occurred
        boolean wasInsideString = false;
        
        // array dimension arrays
        final List<PgDimensionsArrayList> dims = new ArrayList<>();
        
        // currently processed array
        PgDimensionsArrayList curArray = arrayList;
        
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
        {
            if (chars[0] == '[') {
                while (chars[startOffset] != '=') {
                    startOffset++;
                }
                startOffset++; // skip =
            }
        }
        
        for (int i = startOffset; i < chars.length; i++) {
            
            // escape character that we need to skip
            if (chars[i] == '\\') {
                i++;
            } else if (!insideString && chars[i] == '{') {
                // subarray start
                if (dims.isEmpty()) {
                    dims.add(arrayList);
                } else {
                    PgDimensionsArrayList a = new PgDimensionsArrayList();
                    PgDimensionsArrayList p = dims.get(dims.size() - 1);
                    p.add(a);
                    dims.add(a);
                }
                curArray = dims.get(dims.size() - 1);
                
                // number of dimensions
                {
                    for (int t = i + 1; t < chars.length; t++) {
                        if (Character.isWhitespace(chars[t])) {
                            continue;
                        } else if (chars[t] == '{') {
                            curArray.dimensionsCount++;
                        } else {
                            break;
                        }
                    }
                }
                
                buffer = new StringBuilder();
                continue;
            } else if (chars[i] == '"') {
                // quoted element
                insideString = !insideString;
                wasInsideString = true;
                continue;
            } else if (!insideString && Parser.isArrayWhiteSpace(chars[i])) {
                // white space
                continue;
            } else if ((!insideString && (chars[i] == delim || chars[i] == '}')) || i == chars.length - 1) {
                // array end or element end
                // when character that is a part of array element
                if (chars[i] != '"' && chars[i] != '}' && chars[i] != delim && buffer != null) {
                    buffer.append(chars[i]);
                }
                
                String b = buffer == null ? null : buffer.toString();
                
                // add element to current array
                if (b != null && (!b.isEmpty() || wasInsideString)) {
                    curArray.add(!wasInsideString && "NULL".equals(b) ? null : b);
                }
                
                wasInsideString = false;
                buffer = new StringBuilder();
                
                // when end of an array
                if (chars[i] == '}') {
                    dims.remove(dims.size() - 1);
                    
                    // when multi-dimension
                    if (!dims.isEmpty()) {
                        curArray = dims.get(dims.size() - 1);
                    }
                    
                    buffer = null;
                }
                
                continue;
            }
            
            if (buffer != null) {
                buffer.append(chars[i]);
            }
        }
        return arrayList;
    }
}

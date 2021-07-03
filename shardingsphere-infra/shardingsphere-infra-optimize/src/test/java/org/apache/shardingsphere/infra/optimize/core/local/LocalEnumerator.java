/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.optimize.core.local;

import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.avatica.util.DateTimeUtils;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Source;
import org.apache.commons.lang3.time.FastDateFormat;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enumerator that reads from a CSV file.
 */
class LocalEnumerator<E> implements Enumerator<E> {
    
    private static final FastDateFormat TIME_FORMAT_DATE;

    private static final FastDateFormat TIME_FORMAT_TIME;

    private static final FastDateFormat TIME_FORMAT_TIMESTAMP;
    
    private final CSVReader reader;
    
    private final String[] filterValues;
    
    private final AtomicBoolean cancelFlag;
    
    private final RowConverter<E> rowConverter;
    
    private E current;
  
    static {
        final TimeZone gmt = TimeZone.getTimeZone("GMT");
        TIME_FORMAT_DATE = FastDateFormat.getInstance("yyyy-MM-dd", gmt);
        TIME_FORMAT_TIME = FastDateFormat.getInstance("HH:mm:ss", gmt);
        TIME_FORMAT_TIMESTAMP =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", gmt);
    }
  
    LocalEnumerator(final Source source, final AtomicBoolean cancelFlag, final List<LocalFieldType> fieldTypes) {
        this(source, cancelFlag, fieldTypes, identityList(fieldTypes.size()));
    }
  
    LocalEnumerator(final Source source, final AtomicBoolean cancelFlag, final List<LocalFieldType> fieldTypes, final int[] fields) {
        this(source, cancelFlag, false, null, (RowConverter<E>) converter(fieldTypes, fields));
    }
  
    LocalEnumerator(final Source source, final AtomicBoolean cancelFlag, final boolean stream, final String[] filterValues, final RowConverter<E> rowConverter) {
        this.cancelFlag = cancelFlag;
        this.rowConverter = rowConverter;
        this.filterValues = filterValues;
        try {
            this.reader = openCsv(source);
            this.reader.readNext();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
  
    private static RowConverter<?> converter(final List<LocalFieldType> fieldTypes, final int[] fields) { 
        if (fields.length == 1) {
            final int field = fields[0];
            return new SingleColumnRowConverter(fieldTypes.get(field), field);
        } else {
            return new ArrayRowConverter(fieldTypes, fields);
        }
    }
  
    /** Deduces the names and types of a table's columns by reading the first line
     * of a CSV file. */
    static RelDataType deduceRowType(final JavaTypeFactory typeFactory, final Source source, final List<LocalFieldType> fieldTypes) {
        return deduceRowType(typeFactory, source, fieldTypes, false);
    }
  
    /** Deduces the names and types of a table's columns by reading the first line
    * of a CSV file. */
    static RelDataType deduceRowType(final JavaTypeFactory typeFactory, final Source source, final List<LocalFieldType> fieldTypes, final Boolean stream) {
        final List<RelDataType> types = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        try (CSVReader reader = openCsv(source)) {
            String[] strings = reader.readNext();
            if (strings == null) {
                strings = new String[]{"EmptyFileHasNoColumns:boolean"};
            }
            for (String string : strings) {
                final String name;
                final LocalFieldType fieldType;
                final int colon = string.indexOf(':');
                if (colon >= 0) {
                    name = string.substring(0, colon);
                    String typeString = string.substring(colon + 1);
                    fieldType = LocalFieldType.of(typeString);
                    if (fieldType == null) {
                        System.out.println("WARNING: Found unknown type: "
                            + typeString + " in file: " + source.path()
                            + " for column: " + name
                            + ". Will assume the type of column is string");
                    }
                } else {
                    name = string;
                    fieldType = null;
                }
                final RelDataType type;
                if (fieldType == null) {
                    type = typeFactory.createSqlType(SqlTypeName.VARCHAR);
                } else {
                    type = fieldType.toType(typeFactory);
                }
                names.add(name);
                types.add(type);
                if (fieldTypes != null) {
                    fieldTypes.add(fieldType);
                }
            }
        } catch (IOException e) {
          // ignore
        }
        if (names.isEmpty()) {
            names.add("line");
            types.add(typeFactory.createSqlType(SqlTypeName.VARCHAR));
        }
        return typeFactory.createStructType(Pair.zip(names, types));
    }
  
    public static CSVReader openCsv(final Source source) throws IOException {
        final Reader fileReader = source.reader();
        return new CSVReader(fileReader);
    }
  
    public E current() {
        return current;
    }
  
    public boolean moveNext() {
        try {
        outer:
            for (;;) {
                if (cancelFlag.get()) {
                    return false;
                }
                final String[] strings = reader.readNext();
                if (strings == null) {
                    current = null;
                    reader.close();
                    return false;
                }
                if (filterValues != null) {
                    for (int i = 0; i < strings.length; i++) {
                        String filterValue = filterValues[i];
                        if (filterValue != null && !filterValue.equals(strings[i])) {
                            continue outer;
                        }
                    }
                }
                current = rowConverter.convertRow(strings);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
  
    public void reset() {
        throw new UnsupportedOperationException();
    }
  
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing CSV reader", e);
        }
    }
  
    /** Returns an array of integers {0, ..., n - 1}. */
    static int[] identityList(final int n) {
        int[] integers = new int[n];
        for (int i = 0; i < n; i++) {
            integers[i] = i;
        }
        return integers;
    }
  
    /**
     * Row converter.
     * @param <E> element type
     */
    abstract static class RowConverter<E> {
        
        abstract E convertRow(String[] rows);
        
        protected Object convert(final LocalFieldType fieldType, final String string) {
            if (fieldType == null) {
                return string;
            }
            switch (fieldType) {
                case BOOLEAN:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Boolean.parseBoolean(string);
                case BYTE:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Byte.parseByte(string);
                case SHORT:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Short.parseShort(string);
                case INT:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Integer.parseInt(string);
                case LONG:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Long.parseLong(string);
                case FLOAT:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Float.parseFloat(string);
                case DOUBLE:
                    if (string.length() == 0) {
                        return null;
                    }
                    return Double.parseDouble(string);
                case DATE:
                    if (string.length() == 0) {
                        return null;
                    }
                    try {
                        Date date = TIME_FORMAT_DATE.parse(string);
                        return (int) (date.getTime() / DateTimeUtils.MILLIS_PER_DAY);
                    } catch (ParseException e) {
                        return null;
                    }
                case TIME:
                    if (string.length() == 0) {
                        return null;
                    }
                    try {
                        Date date = TIME_FORMAT_TIME.parse(string);
                        return (int) date.getTime();
                    } catch (ParseException e) {
                        return null;
                    }
                case TIMESTAMP:
                    if (string.length() == 0) {
                        return null;
                    }
                    try {
                        Date date = TIME_FORMAT_TIMESTAMP.parse(string);
                        return date.getTime();
                    } catch (ParseException e) {
                        return null;
                    }
                case STRING:
                default:
                    return string;
            }
        }
    }
  
    /** 
     * Array row converter.
     */
    private static class ArrayRowConverter extends RowConverter<Object[]> {
        
        private final LocalFieldType[] fieldTypes;
        
        private final int[] fields;

        /**
         * whether the row to convert is from a stream.
         */
        private final boolean stream;
    
        ArrayRowConverter(final List<LocalFieldType> fieldTypes, final int[] fields) {
            this.fieldTypes = fieldTypes.toArray(new LocalFieldType[0]);
            this.fields = fields;
            this.stream = false;
        }
    
        ArrayRowConverter(final List<LocalFieldType> fieldTypes, final int[] fields, final boolean stream) {
            this.fieldTypes = fieldTypes.toArray(new LocalFieldType[0]);
            this.fields = fields;
            this.stream = stream;
        }
    
        public Object[] convertRow(final String[] strings) {
            if (stream) {
                return convertStreamRow(strings);
            } else {
                return convertNormalRow(strings);
            }
        }
    
        public Object[] convertNormalRow(final String[] strings) {
            final Object[] objects = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                int field = fields[i];
                objects[i] = convert(fieldTypes[field], strings[field]);
            }
            return objects;
        }
    
        public Object[] convertStreamRow(final String[] strings) {
            final Object[] objects = new Object[fields.length + 1];
            objects[0] = System.currentTimeMillis();
            for (int i = 0; i < fields.length; i++) {
                int field = fields[i];
                objects[i + 1] = convert(fieldTypes[field], strings[field]);
            }
            return objects;
        }
    }
  
    /** 
     * Single column row converter.
     */
    private static final class SingleColumnRowConverter extends RowConverter {
        
        private final LocalFieldType fieldType;
        
        private final int fieldIndex;
    
        private SingleColumnRowConverter(final LocalFieldType fieldType, final int fieldIndex) {
            this.fieldType = fieldType;
            this.fieldIndex = fieldIndex;
        }
  
        public Object convertRow(final String[] strings) {
            return convert(fieldType, strings[fieldIndex]);
        }
    }
}

// End CsvEnumerator.java

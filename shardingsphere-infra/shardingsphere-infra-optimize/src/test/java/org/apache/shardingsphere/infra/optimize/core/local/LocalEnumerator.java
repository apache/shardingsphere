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
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Source;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Enumerator that reads from a CSV file.
 */
@Slf4j
class LocalEnumerator<E> implements Enumerator<E> {
    
    /**
     * Deduces the names and types of a table's columns by reading the first line of a CSV file. 
     *
     * @param typeFactory java type facotry
     * @param source file source
     * @param fieldTypes field types
     * @return rel data type
     */
    static RelDataType deduceRowType(final JavaTypeFactory typeFactory, final Source source, final List<LocalFieldType> fieldTypes) {
        final List<RelDataType> types = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        try (CSVReader reader = openCsv(source)) {
            String[] strings = reader.readNext();
            if (strings == null) {
                strings = new String[]{"EmptyFileHasNoColumns:boolean"};
            }
            while (strings[0].contains("#")) {
                strings = reader.readNext();
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
                        log.warn("WARNING: Found unknown type: "
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
        } catch (IOException ex) {
            log.error("Open csv file error", ex);
        }
        if (names.isEmpty()) {
            names.add("line");
            types.add(typeFactory.createSqlType(SqlTypeName.VARCHAR));
        }
        return typeFactory.createStructType(Pair.zip(names, types));
    }
    
    /**
     * Create a csv reader.
     * 
     * @param source file source
     * @return csv reader
     * @throws IOException io exception
     */
    public static CSVReader openCsv(final Source source) throws IOException {
        final Reader fileReader = source.reader();
        return new CSVReader(fileReader);
    }
    
    /**
     * Returns an array of integers {0, ..., n - 1}. 
     * 
     * @param n length of array
     * @return int array
     */
    public static int[] identityList(final int n) {
        int[] integers = new int[n];
        for (int i = 0; i < n; i++) {
            integers[i] = i;
        }
        return integers;
    }
    
    public E current() {
        throw new UnsupportedOperationException();
    }
    
    public boolean moveNext() {
        throw new UnsupportedOperationException();
    }
    
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    public void close() {
        throw new UnsupportedOperationException();
    }
}

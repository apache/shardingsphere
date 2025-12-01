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

package org.apache.shardingsphere.shadow.algorithm.shadow.column;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.exception.data.UnsupportedShadowColumnTypeException;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnValueMatchedShadowAlgorithmTest {
    
    private static final String SHADOW_TABLE = "foo_tbl";
    
    private static final String SHADOW_COLUMN = "shadow";
    
    @Test
    void assertIsShadow() {
        ColumnValueMatchedShadowAlgorithm shadowAlgorithm = (ColumnValueMatchedShadowAlgorithm) TypedSPILoader.getService(ShadowAlgorithm.class,
                "VALUE_MATCH", PropertiesBuilder.build(new Property("column", SHADOW_COLUMN), new Property("operation", "insert"), new Property("value", "1")));
        PreciseColumnShadowValueFixtureBuilder.createTrueCase(SHADOW_TABLE, SHADOW_COLUMN).forEach(each -> assertTrue(shadowAlgorithm.isShadow(each)));
        PreciseColumnShadowValueFixtureBuilder.createFalseCase(SHADOW_TABLE, SHADOW_COLUMN).forEach(each -> assertFalse(shadowAlgorithm.isShadow(each)));
    }
    
    @Test
    void assertExceptionCase() {
        ColumnValueMatchedShadowAlgorithm shadowAlgorithm = (ColumnValueMatchedShadowAlgorithm) TypedSPILoader.getService(ShadowAlgorithm.class,
                "VALUE_MATCH", PropertiesBuilder.build(new Property("column", SHADOW_COLUMN), new Property("operation", "insert"), new Property("value", "1")));
        assertThrows(UnsupportedShadowColumnTypeException.class,
                () -> PreciseColumnShadowValueFixtureBuilder.createExceptionCase(SHADOW_TABLE, SHADOW_COLUMN).forEach(each -> assertFalse(shadowAlgorithm.isShadow(each))));
    }
    
    @Test
    void assertPropertiesWithoutColumn() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShadowAlgorithm.class, "VALUE_MATCH", PropertiesBuilder.build(new Property("operation", "insert"), new Property("value", "1"))));
    }
    
    @Test
    void assertPropertiesWithWrongOperation() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShadowAlgorithm.class, "VALUE_MATCH",
                PropertiesBuilder.build(new Property("column", SHADOW_COLUMN), new Property("operation", "wrong"), new Property("value", "1"))));
    }
}

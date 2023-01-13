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

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.exception.data.UnsupportedShadowColumnTypeException;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ColumnRegexMatchShadowAlgorithmTest extends AbstractColumnShadowAlgorithmTest {
    
    @Test
    public void assertIsShadow() {
        Properties props = PropertiesBuilder.build(new Property("column", SHADOW_COLUMN), new Property("operation", "insert"), new Property("regex", "[1]"));
        ColumnRegexMatchedShadowAlgorithm shadowAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("REGEX_MATCH", props), ShadowAlgorithm.class);
        createPreciseColumnShadowValuesFalseCase().forEach(each -> assertFalse(shadowAlgorithm.isShadow(each)));
        createPreciseColumnShadowValuesTrueCase().forEach(each -> assertTrue(shadowAlgorithm.isShadow(each)));
    }
    
    @Test(expected = UnsupportedShadowColumnTypeException.class)
    public void assertExceptionCase() {
        Properties props = PropertiesBuilder.build(new Property("column", SHADOW_COLUMN), new Property("operation", "insert"), new Property("regex", "[1]"));
        ColumnRegexMatchedShadowAlgorithm shadowAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("REGEX_MATCH", props), ShadowAlgorithm.class);
        createPreciseColumnShadowValuesExceptionCase().forEach(each -> assertFalse(shadowAlgorithm.isShadow(each)));
    }
}

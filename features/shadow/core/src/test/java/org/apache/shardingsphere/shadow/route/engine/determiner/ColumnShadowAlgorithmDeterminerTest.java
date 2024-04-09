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

package org.apache.shardingsphere.shadow.route.engine.determiner;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnShadowAlgorithmDeterminerTest {
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertIsShadow() {
        Properties props = PropertiesBuilder.build(new Property("column", "user_id"), new Property("operation", "insert"), new Property("regex", "[1]"));
        assertTrue(ColumnShadowAlgorithmDeterminer.isShadow(
                (ColumnShadowAlgorithm) TypedSPILoader.getService(ShadowAlgorithm.class, "REGEX_MATCH", props), createShadowDetermineCondition()));
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        ShadowDetermineCondition result = new ShadowDetermineCondition("t_order", ShadowOperationType.INSERT);
        result.initShadowColumnCondition(new ShadowColumnCondition("t_order", "user_id", Collections.singleton(1)));
        return result;
    }
}

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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class ColumnShadowAlgorithmDeterminerTest {
    
    @Test
    public void assertIsShadow() {
        assertTrue(ColumnShadowAlgorithmDeterminer.isShadow(createColumnShadowAlgorithms(), createShadowDetermineCondition()));
    }
    
    @SuppressWarnings("unchecked")
    private ColumnShadowAlgorithm<Comparable<?>> createColumnShadowAlgorithms() {
        return (ColumnShadowAlgorithm<Comparable<?>>) ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("column", "user_id");
        result.setProperty("operation", "insert");
        result.setProperty("regex", "[1]");
        return result;
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        ShadowDetermineCondition result = new ShadowDetermineCondition("t_order", ShadowOperationType.INSERT);
        result.initShadowColumnCondition(new ShadowColumnCondition("t_order", "user_id", Collections.singleton(1)));
        return result;
    }
}

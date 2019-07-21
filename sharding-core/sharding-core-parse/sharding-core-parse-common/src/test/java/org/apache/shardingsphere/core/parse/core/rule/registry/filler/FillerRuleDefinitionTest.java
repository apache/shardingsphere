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

package org.apache.shardingsphere.core.parse.core.rule.registry.filler;

import org.apache.shardingsphere.core.parse.core.filler.impl.ddl.column.ColumnDefinitionFiller;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class FillerRuleDefinitionTest {
    
    @Test
    public void assertGetFiller() {
        FillerRuleDefinitionEntity fillerRuleDefinitionEntity = new FillerRuleDefinitionEntityLoader().load("META-INF/parsing-rule-definition/filler-rule-definition.xml");
        assertThat(new FillerRuleDefinition(fillerRuleDefinitionEntity).getFiller(ColumnDefinitionSegment.class), instanceOf(ColumnDefinitionFiller.class));
    }
}

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

package org.apache.shardingsphere.mask.distsql.handler.converter;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleStatementConverterTest {
    
    @Test
    void assertCovert() {
        MaskRuleConfiguration actual = MaskRuleStatementConverter.convert(Collections.singleton(new MaskRuleSegment("t_mask", createColumns())));
        assertThat(actual.getTables().iterator().next().getName(), is("t_mask"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getLogicColumn(), is("user_id"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getMaskAlgorithm(), is("t_mask_user_id_md5"));
        assertTrue(actual.getMaskAlgorithms().get("t_mask_user_id_md5").getType().contains("MD5"));
    }
    
    private Collection<MaskColumnSegment> createColumns() {
        return Collections.singleton(new MaskColumnSegment("user_id", new AlgorithmSegment("MD5", PropertiesBuilder.build(new Property("salt", "test_salt")))));
    }
}

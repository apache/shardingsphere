/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.rule.jaxb.segment;

import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.segment.SQLSegmentRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.segment.SQLSegmentRuleDefinitionEntityLoader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLSegmentRuleDefinitionEntityLoaderTest {
    
    @Test
    public void assertLoadForCommon() {
        SQLSegmentRuleDefinitionEntity actual = new SQLSegmentRuleDefinitionEntityLoader().load("parsing-rule-definition/common/extractor-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getExtractorBasePackage(), is("extractor.impl"));
        assertThat(actual.getFillerBasePackage(), is("filler.impl"));
        assertThat(actual.getRules().size(), is(15));
    }
    
    @Test
    public void assertLoadForMySQL() {
        SQLSegmentRuleDefinitionEntity actual = new SQLSegmentRuleDefinitionEntityLoader().load("parsing-rule-definition/mysql/extractor-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getExtractorBasePackage(), is("extractor.impl"));
        assertThat(actual.getFillerBasePackage(), is("filler.impl"));
        assertThat(actual.getRules().size(), is(6));
    }
    
    @Test
    public void assertLoadForPostgreSQL() {
        SQLSegmentRuleDefinitionEntity actual = new SQLSegmentRuleDefinitionEntityLoader().load("parsing-rule-definition/postgresql/extractor-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getExtractorBasePackage(), is("extractor.impl"));
        assertThat(actual.getFillerBasePackage(), is("filler.impl"));
        assertThat(actual.getRules().size(), is(3));
    }
    
    @Test
    public void assertLoadForOracle() {
        SQLSegmentRuleDefinitionEntity actual = new SQLSegmentRuleDefinitionEntityLoader().load("parsing-rule-definition/oracle/extractor-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getExtractorBasePackage(), is("extractor.impl"));
        assertThat(actual.getFillerBasePackage(), is("filler.impl"));
        assertThat(actual.getRules().size(), is(3));
    }
    
    @Test
    public void assertLoadSegmentRuleDefinitionForSQLServer() {
        SQLSegmentRuleDefinitionEntity actual = new SQLSegmentRuleDefinitionEntityLoader().load("parsing-rule-definition/sqlserver/extractor-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getExtractorBasePackage(), is("extractor.impl"));
        assertThat(actual.getFillerBasePackage(), is("filler.impl"));
        assertThat(actual.getRules().size(), is(5));
    }
}

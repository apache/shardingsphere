package org.apache.shardingsphere.infra.metadata.rule;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class ShardingSphereRuleMetaDataTest {

    private Collection<RuleConfiguration> configurations;

    private Collection<ShardingSphereRule> rules;

    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;

    @Before
    public void init() {
        rules = Arrays.asList(new ShardingSphereRuleFake());
        shardingSphereRuleMetaData = new ShardingSphereRuleMetaData(configurations, rules);
    }

    @Test
    public void assertFilterRulesReturnOneItem() {
        Collection<ShardingSphereRuleFake> clazzList = shardingSphereRuleMetaData.findRules(ShardingSphereRuleFake.class);
        assertThat(clazzList, hasSize(1));
    }

    @Test
    public void assertFindSingleRuleReturnsEmpty() {
        Optional<ClassTest1> clazzOptional = shardingSphereRuleMetaData.findSingleRule(ClassTest1.class);
        assertThat(clazzOptional.isEmpty(), is(true));
    }

    @Test
    public void assertFindSingleRuleHasValue() {
        Optional<ShardingSphereRuleFake> clazzOptional = shardingSphereRuleMetaData.findSingleRule(ShardingSphereRuleFake.class);
        assertThat(clazzOptional.isPresent(), is(true));
        assertThat(clazzOptional.get().getType(), equalTo("type"));
    }
}

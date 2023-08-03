package org.apache.shardingsphere.mask.checker;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MaskRuleConfigurationCheckerTest {

    private MaskRuleConfigurationChecker maskRuleConfigurationChecker;

    @BeforeEach
    public void setUp() {
        maskRuleConfigurationChecker = new MaskRuleConfigurationChecker();
    }

    @Test
    public void testCheck() {
        String databaseName = "testDb";
        MaskRuleConfiguration config = createMockConfig();
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        Collection<ShardingSphereRule> builtRules = Collections.emptyList();
        Assertions.assertDoesNotThrow(() -> maskRuleConfigurationChecker.check(databaseName, config, dataSourceMap, builtRules));
    }

    private MaskRuleConfiguration createMockConfig() {
        MaskTableRuleConfiguration tableConfig = new MaskTableRuleConfiguration("testTable", new ArrayList<>());
        tableConfig.getColumns().add(createMockColumnConfig());
        MaskRuleConfiguration config = new MaskRuleConfiguration(new ArrayList<>(), new HashMap<>());
        config.getTables().add(tableConfig);
        return config;
    }

    private MaskColumnRuleConfiguration createMockColumnConfig() {
        return new MaskColumnRuleConfiguration("testColumn", "MD5");
    }

    @Test
    public void testGetOrder() {
        Assertions.assertEquals(MaskOrder.ORDER, maskRuleConfigurationChecker.getOrder());
    }

    @Test
    public void testGetTypeClass() {
        Assertions.assertEquals(MaskRuleConfiguration.class, maskRuleConfigurationChecker.getTypeClass());
    }
}

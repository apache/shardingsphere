package org.apache.shardingsphere.mode.manager.fixture;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;

public class DefaultFixtureContextManagerBuilder implements ContextManagerBuilder {

    @Override
    public ContextManager build(final ModeConfiguration modeConfig, final Map<String, Map<String, DataSource>> dataSourcesMap, final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs,
                                final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) throws SQLException {
        return null;
    }

    @Override
    public String getType() {
        return "DefaultFixture";
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}

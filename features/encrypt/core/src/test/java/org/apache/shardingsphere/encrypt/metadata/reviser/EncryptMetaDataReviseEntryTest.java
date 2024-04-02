package org.apache.shardingsphere.encrypt.metadata.reviser;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.metadata.reviser.column.EncryptColumnExistedReviser;
import org.apache.shardingsphere.encrypt.metadata.reviser.column.EncryptColumnNameReviser;
import org.apache.shardingsphere.encrypt.metadata.reviser.index.EncryptIndexReviser;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptMetaDataReviseEntryTest {
    
    private final EncryptMetaDataReviseEntry reviseEntry = new EncryptMetaDataReviseEntry();
    private final EncryptRule rule = createEncryptRule();
    
    private EncryptRule createEncryptRule() {
        EncryptRuleConfiguration ruleConfig =
                new EncryptRuleConfiguration(Collections.singleton(new EncryptTableRuleConfiguration("test_table", Collections.emptyList())),new HashMap<>());
        
        return new EncryptRule("test_database", ruleConfig);
    }
    
    @Test
    void assertGetIndexReviser() {
        Optional<EncryptIndexReviser> indexReviser = reviseEntry.getIndexReviser(rule, "test_table");
        assertTrue(indexReviser.isPresent());
        assertThat(indexReviser.get().getClass(), is(EncryptIndexReviser.class));
    }
    
    @Test
    void assertGetColumnNameReviser() {
        Optional<EncryptColumnNameReviser> columnNameReviser = reviseEntry.getColumnNameReviser(rule, "test_table");
        assertTrue(columnNameReviser.isPresent());
        assertThat(columnNameReviser.get().getClass(), is(EncryptColumnNameReviser.class));
        
    }
    
    @Test
    void assertGetColumnExistedReviser() {
        Optional<EncryptColumnExistedReviser> columnExistedReviser = reviseEntry.getColumnExistedReviser(rule, "test_table");
        assertTrue(columnExistedReviser.isPresent());
        assertThat(columnExistedReviser.get().getClass(), is(EncryptColumnExistedReviser.class));
    }
}

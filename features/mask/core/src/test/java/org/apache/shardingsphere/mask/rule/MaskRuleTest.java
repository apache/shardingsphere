package org.apache.shardingsphere.mask.rule;

import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MaskRuleTest {

    @Mock
    private MaskRuleConfiguration mockConfiguration;

    @Mock
    private MaskAlgorithm<Object, Object> mockMaskAlgorithm;

    private MaskRule maskRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        maskRule = new MaskRule(mockConfiguration);
    }

    @Test
    void testFindMaskTableWithExistingTable() {
        // Mock the behavior of the configuration
        String tableName = "test_table";
        Map<String, MaskAlgorithm<Object, Object>> maskAlgorithms = new HashMap<>();
        maskAlgorithms.put("mockAlgorithm", mockMaskAlgorithm);

        when(mockConfiguration.getMaskAlgorithms()).thenReturn(maskAlgorithms);
        when(mockConfiguration.getTables()).thenReturn(Collections.singletonList(new MaskRuleConfiguration.TableRuleConfiguration(tableName, Collections.emptyList())));

        // Call the method to be tested
        Optional<MaskTable> result = maskRule.findMaskTable(tableName);

        // Verify and assert
        assertTrue(result.isPresent());
        assertEquals(tableName, result.get().getName());
        verify(mockConfiguration).getMaskAlgorithms();
        verify(mockConfiguration).getTables();
    }

}
package org.apache.shardingsphere.infra.metadata.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShardingSphereSchemaDataTest {

    private final static String TEST_TABLE_NAME = "TEST_TABLE_NAME";
    private final static String TEST_TABLE_2 = "TEST_TABLE_2";
    private final static String NON_EXISTENT_TABLE = "NON_EXISTENT_TABLE";

    @Test
    void assertGetTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));

        ShardingSphereTableData tableData = shardingSphereSchemaData.getTable(TEST_TABLE_NAME);
        assertTrue(TEST_TABLE_NAME.equalsIgnoreCase(tableData.getName()));
        ShardingSphereTableData nonExistentTableData = shardingSphereSchemaData.getTable(NON_EXISTENT_TABLE);
        assertNull(nonExistentTableData);
    }

    @Test
    void assertPutTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));

        assertEquals(1, shardingSphereSchemaData.getTableData().size());
        assertFalse(shardingSphereSchemaData.getTableData().containsKey(TEST_TABLE_2));
        ShardingSphereTableData newTable = new ShardingSphereTableData(TEST_TABLE_2);
        shardingSphereSchemaData.putTable(TEST_TABLE_2, newTable);
        assertEquals(2, shardingSphereSchemaData.getTableData().size());
        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_2));
    }

    @Test
    void assertRemoveTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));

        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_NAME));
        shardingSphereSchemaData.removeTable(TEST_TABLE_NAME);
        assertEquals(0, shardingSphereSchemaData.getTableData().size());
        assertFalse(shardingSphereSchemaData.getTableData().containsKey(TEST_TABLE_NAME));
    }

    @Test
    void assertContainsTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));

        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_NAME));
        assertFalse(shardingSphereSchemaData.containsTable(NON_EXISTENT_TABLE));
    }
}

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class TableNamesMapperTest {

    private TableNamesMapper tableNamesMapper;

    @BeforeEach
    void setUp() {
        tableNamesMapper = new TableNamesMapper();
    }

    @Test
    void assertContainsTable() {
        tableNamesMapper.put("foo_table");
        assertTrue(tableNamesMapper.contains("foo_table"));
    }

    @Test
    void assertGetTableNames() {
        tableNamesMapper.put("foo_table_1");
        tableNamesMapper.put("foo_table_2");
        assertThat(tableNamesMapper.getTableNames(), hasItems("foo_table_1", "foo_table_2"));
        assertThat(tableNamesMapper.getTableNames(), hasSize(2));
    }

    @Test
    void assertRemove() {
        tableNamesMapper.put("foo_table_1");
        tableNamesMapper.remove("foo_table_1");
        assertFalse(tableNamesMapper.contains("foo_table_1"));
        assertThat(tableNamesMapper.getTableNames(), hasSize(0));
    }

    @Test
    void assertPut() {
        tableNamesMapper.put("foo_table");
        assertThat(tableNamesMapper.getTableNames(), hasSize(1));
    }

}
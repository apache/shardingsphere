package org.apache.shardingsphere.infra.metadata.database.schema.reviser.table;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableMetadataReviseEngineTest<T extends ShardingSphereRule> {
    @Mock
    private T mockRule;

    @Mock
    private DatabaseType mockDatabaseType;

    @Mock
    private DataSource mockDataSource;

    @Mock
    private MetaDataReviseEntry<T> mockMetaDataReviseEntry;

    @InjectMocks
    private TableMetaDataReviseEngine<T> mockTableMetaDataReviseEngine;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReviseWithTableNameReviser() {
        // Arrange
        TableNameReviser<T> tableNameReviser = mock(TableNameReviser.class);

        // Create a sample original TableMetaData
        TableMetaData originalMetaData = new TableMetaData(
                "originalTableName",
                new ArrayList<>(),
                null,
                null
        );

        // Act
        doReturn(Optional.of(tableNameReviser)).when(mockMetaDataReviseEntry).getTableNameReviser();
        when(tableNameReviser.revise(anyString(), eq(mockRule))).thenReturn("revisedTableName");

        // Call the revise method
        TableMetaData revisedMetaData = mockTableMetaDataReviseEngine.revise(originalMetaData);

        // Assert
        // Verify that the revised table name is returned
        assertEquals("revisedTableName", revisedMetaData.getName());
    }

    @Test
    public void testReviseWithoutTableNameReviser() {
        // Arrange
        Collection<ColumnMetaData> columns = new ArrayList<>();
        columns.add(new ColumnMetaData(
                "column1",
                2,
                true,
                true,
                true,
                false,
                false,
                false
                )
        );

        Collection<IndexMetaData> indexes = new ArrayList<>();
        indexes.add(new IndexMetaData("index1"));

        // Create a sample TableMetaData
        TableMetaData tableMetaData = new TableMetaData(
                "originalTableName",
                columns,
                indexes,
                null
        );

        // Act
        // Mock TableNameReviser to return an empty Optional
        when(mockMetaDataReviseEntry.getTableNameReviser()).thenReturn(Optional.empty());

        // Call the revise method
        TableMetaData revisedMetaData = mockTableMetaDataReviseEngine.revise(tableMetaData);

        // Assert
        // Verify that the original table name is returned since there's no reviser
        assertEquals("originalTableName", revisedMetaData.getName());
    }
}

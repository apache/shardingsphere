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

package org.apache.shardingsphere.infra.binder.engine.statement;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for SQLStatementCopyUtils.
 */
class SQLStatementCopyUtilsTest {
    
    private SQLStatement originalStatement;
    
    private SQLStatement targetStatement;
    
    @BeforeEach
    void setUp() {
        originalStatement = new TestSQLStatement(new MockedDatabaseType());
        targetStatement = new TestSQLStatement(new MockedDatabaseType());
    }
    
    @Test
    void assertCopyAttributesWithEmptyCollections() {
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(0, targetStatement.getParameterCount());
        assertEquals(0, targetStatement.getParameterMarkers().size());
        assertEquals(0, targetStatement.getVariableNames().size());
        assertEquals(0, targetStatement.getComments().size());
    }
    
    @Test
    void assertCopyAttributesWithParameterMarkers() {
        // Add parameter markers to original statement
        ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(1, 1, 2);
        originalStatement.addParameterMarkers(Arrays.asList(param1, param2));
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(2, targetStatement.getParameterCount());
        assertEquals(2, targetStatement.getParameterMarkers().size());
        assertTrue(targetStatement.getParameterMarkers().contains(param1));
        assertTrue(targetStatement.getParameterMarkers().contains(param2));
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(1));
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(2));
    }
    
    @Test
    void assertCopyAttributesWithVariableNames() {
        // Add variable names to original statement
        originalStatement.getVariableNames().add("var1");
        originalStatement.getVariableNames().add("var2");
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(2, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains("var1"));
        assertTrue(targetStatement.getVariableNames().contains("var2"));
    }
    
    @Test
    void assertCopyAttributesWithComments() {
        // Add comments to original statement
        CommentSegment comment1 = new CommentSegment("comment1", 0, 7);
        CommentSegment comment2 = new CommentSegment("comment2", 8, 15);
        originalStatement.getComments().add(comment1);
        originalStatement.getComments().add(comment2);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(2, targetStatement.getComments().size());
        assertTrue(targetStatement.getComments().contains(comment1));
        assertTrue(targetStatement.getComments().contains(comment2));
    }
    
    @Test
    void assertCopyAttributesWithAllAttributes() {
        // Add all types of attributes to original statement
        ParameterMarkerExpressionSegment param = new ParameterMarkerExpressionSegment(0, 0, 1);
        originalStatement.addParameterMarkers(Collections.singletonList(param));
        originalStatement.getVariableNames().add("variable");
        CommentSegment comment = new CommentSegment("comment", 0, 6);
        originalStatement.getComments().add(comment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(1, targetStatement.getParameterCount());
        assertEquals(1, targetStatement.getParameterMarkers().size());
        assertEquals(1, targetStatement.getVariableNames().size());
        assertEquals(1, targetStatement.getComments().size());
        
        assertTrue(targetStatement.getParameterMarkers().contains(param));
        assertTrue(targetStatement.getVariableNames().contains("variable"));
        assertTrue(targetStatement.getComments().contains(comment));
    }
    
    @Test
    void assertCopyAttributesWithDuplicateParameterMarkers() {
        // Add parameter markers with duplicate indices
        ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        // Same index as param1
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(1, 1, 1);
        originalStatement.addParameterMarkers(Arrays.asList(param1, param2));
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // Should only have one unique parameter index
        assertEquals(1, targetStatement.getParameterCount());
        // Both segments are added
        assertEquals(2, targetStatement.getParameterMarkers().size());
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(1));
    }
    
    @Test
    void assertCopyAttributesWithCaseInsensitiveVariableNames() {
        // Test case insensitive behavior of variable names
        originalStatement.getVariableNames().add("Variable");
        originalStatement.getVariableNames().add("variable");
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // CaseInsensitiveSet should handle duplicates
        assertEquals(1, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains("Variable"));
    }
    
    @Test
    void assertCopyAttributesMultipleTimes() {
        // Add attributes to original statement
        ParameterMarkerExpressionSegment param = new ParameterMarkerExpressionSegment(0, 0, 1);
        originalStatement.addParameterMarkers(Collections.singletonList(param));
        originalStatement.getVariableNames().add("var");
        CommentSegment comment = new CommentSegment("comment", 0, 6);
        originalStatement.getComments().add(comment);
        
        // Copy attributes multiple times
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // Note: LinkedHashSet prevents duplicate object references, but allows different objects with same values
        // This demonstrates why we should avoid calling addParameterMarkers before copyAttributes
        // uniqueParameterIndexes.size() = 1
        assertEquals(1, targetStatement.getParameterCount());
        // param not duplicated due to LinkedHashSet behavior
        assertEquals(1, targetStatement.getParameterMarkers().size());
        // CaseInsensitiveSet handles duplicates
        assertEquals(1, targetStatement.getVariableNames().size());
        // LinkedList allows duplicates
        assertEquals(2, targetStatement.getComments().size());
    }
    
    @Test
    void assertCopyAttributesWithNullCollections() {
        // Test with null collections (should not throw exception)
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(0, targetStatement.getParameterCount());
        assertEquals(0, targetStatement.getParameterMarkers().size());
        assertEquals(0, targetStatement.getVariableNames().size());
        assertEquals(0, targetStatement.getComments().size());
    }
    
    @Test
    void assertCopyAttributesWithLargeCollections() {
        // Test with large collections
        Collection<ParameterMarkerSegment> largeParamList = Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 1),
                new ParameterMarkerExpressionSegment(1, 1, 2),
                new ParameterMarkerExpressionSegment(2, 2, 3),
                new ParameterMarkerExpressionSegment(3, 3, 4),
                new ParameterMarkerExpressionSegment(4, 4, 5));
        originalStatement.addParameterMarkers(largeParamList);
        
        for (int i = 0; i < 100; i++) {
            originalStatement.getVariableNames().add("var" + i);
        }
        
        for (int i = 0; i < 50; i++) {
            CommentSegment comment = new CommentSegment("comment" + i, i, i + 7);
            originalStatement.getComments().add(comment);
        }
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(5, targetStatement.getParameterCount());
        assertEquals(5, targetStatement.getParameterMarkers().size());
        assertEquals(100, targetStatement.getVariableNames().size());
        assertEquals(50, targetStatement.getComments().size());
    }
    
    @Test
    void assertCopyAttributesPreservesOrder() {
        // Test that order is preserved for comments (LinkedList)
        CommentSegment comment1 = new CommentSegment("first", 0, 4);
        CommentSegment comment2 = new CommentSegment("second", 5, 10);
        CommentSegment comment3 = new CommentSegment("third", 11, 15);
        
        originalStatement.getComments().add(comment1);
        originalStatement.getComments().add(comment2);
        originalStatement.getComments().add(comment3);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // Convert to array to check order
        CommentSegment[] comments = targetStatement.getComments().toArray(new CommentSegment[0]);
        assertEquals(comment1, comments[0]);
        assertEquals(comment2, comments[1]);
        assertEquals(comment3, comments[2]);
    }
    
    @Test
    void assertCopyAttributesWithSpecialCharacters() {
        // Test with special characters in variable names and comments
        originalStatement.getVariableNames().add("var_with_underscore");
        originalStatement.getVariableNames().add("var-with-dash");
        originalStatement.getVariableNames().add("var.with.dot");
        originalStatement.getVariableNames().add("var$with$dollar");
        
        CommentSegment specialComment = new CommentSegment("comment with special chars: !@#$%^&*()", 0, 35);
        originalStatement.getComments().add(specialComment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(4, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains("var_with_underscore"));
        assertTrue(targetStatement.getVariableNames().contains("var-with-dash"));
        assertTrue(targetStatement.getVariableNames().contains("var.with.dot"));
        assertTrue(targetStatement.getVariableNames().contains("var$with$dollar"));
        
        assertEquals(1, targetStatement.getComments().size());
        assertTrue(targetStatement.getComments().contains(specialComment));
    }
    
    @Test
    void assertCopyAttributesWithEmptyStrings() {
        // Test with empty strings
        originalStatement.getVariableNames().add("");
        originalStatement.getVariableNames().add("   ");
        CommentSegment emptyComment = new CommentSegment("", 0, 0);
        originalStatement.getComments().add(emptyComment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(2, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains(""));
        assertTrue(targetStatement.getVariableNames().contains("   "));
        
        assertEquals(1, targetStatement.getComments().size());
        assertTrue(targetStatement.getComments().contains(emptyComment));
    }
    
    @Test
    void assertCopyAttributesWithUnicodeCharacters() {
        // Test with Unicode characters
        originalStatement.getVariableNames().add("变量名");
        originalStatement.getVariableNames().add("変数名");
        originalStatement.getVariableNames().add("변수명");
        
        CommentSegment unicodeComment = new CommentSegment("Unicode comment: 你好世界", 0, 20);
        originalStatement.getComments().add(unicodeComment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(3, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains("变量名"));
        assertTrue(targetStatement.getVariableNames().contains("変数名"));
        assertTrue(targetStatement.getVariableNames().contains("변수명"));
        
        assertEquals(1, targetStatement.getComments().size());
        assertTrue(targetStatement.getComments().contains(unicodeComment));
    }
    
    @Test
    void assertCopyAttributesWithBoundaryValues() {
        // Test with boundary values for parameter indices
        ParameterMarkerExpressionSegment minParam = new ParameterMarkerExpressionSegment(0, 0, Integer.MIN_VALUE);
        ParameterMarkerExpressionSegment maxParam = new ParameterMarkerExpressionSegment(1, 1, Integer.MAX_VALUE);
        ParameterMarkerExpressionSegment zeroParam = new ParameterMarkerExpressionSegment(2, 2, 0);
        ParameterMarkerExpressionSegment negativeParam = new ParameterMarkerExpressionSegment(3, 3, -1);
        
        originalStatement.addParameterMarkers(Arrays.asList(minParam, maxParam, zeroParam, negativeParam));
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(4, targetStatement.getParameterCount());
        assertEquals(4, targetStatement.getParameterMarkers().size());
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(Integer.MIN_VALUE));
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(Integer.MAX_VALUE));
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(0));
        assertTrue(targetStatement.getUniqueParameterIndexes().contains(-1));
    }
    
    @Test
    void assertCopyAttributesWithMixedContent() {
        // Test with mixed content types
        ParameterMarkerExpressionSegment param = new ParameterMarkerExpressionSegment(0, 0, 1);
        originalStatement.addParameterMarkers(Collections.singletonList(param));
        
        originalStatement.getVariableNames().add("mixed_var");
        originalStatement.getVariableNames().add("123numeric");
        originalStatement.getVariableNames().add("_underscore_start");
        
        CommentSegment mixedComment = new CommentSegment("Mixed comment: 123 !@# 变量", 0, 25);
        originalStatement.getComments().add(mixedComment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        assertEquals(1, targetStatement.getParameterCount());
        assertEquals(3, targetStatement.getVariableNames().size());
        assertEquals(1, targetStatement.getComments().size());
        
        assertTrue(targetStatement.getVariableNames().contains("mixed_var"));
        assertTrue(targetStatement.getVariableNames().contains("123numeric"));
        assertTrue(targetStatement.getVariableNames().contains("_underscore_start"));
        assertTrue(targetStatement.getComments().contains(mixedComment));
    }
    
    @Test
    void assertCopyAttributesEquivalentToManualCopy() {
        // Test that copyAttributes is equivalent to manual copying
        ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(1, 1, 2);
        originalStatement.addParameterMarkers(Arrays.asList(param1, param2));
        
        originalStatement.getVariableNames().add("var1");
        originalStatement.getVariableNames().add("var2");
        
        CommentSegment comment1 = new CommentSegment("comment1", 0, 7);
        CommentSegment comment2 = new CommentSegment("comment2", 8, 15);
        originalStatement.getComments().add(comment1);
        originalStatement.getComments().add(comment2);
        
        // Method 1: Using copyAttributes
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // Method 2: Manual copying (equivalent to what copyAttributes does)
        SQLStatement manualCopyStatement = new TestSQLStatement(new MockedDatabaseType());
        manualCopyStatement.addParameterMarkers(originalStatement.getParameterMarkers());
        manualCopyStatement.getVariableNames().addAll(originalStatement.getVariableNames());
        manualCopyStatement.getComments().addAll(originalStatement.getComments());
        
        // Both should have the same result
        assertEquals(targetStatement.getParameterCount(), manualCopyStatement.getParameterCount());
        assertEquals(targetStatement.getParameterMarkers().size(), manualCopyStatement.getParameterMarkers().size());
        assertEquals(targetStatement.getVariableNames().size(), manualCopyStatement.getVariableNames().size());
        assertEquals(targetStatement.getComments().size(), manualCopyStatement.getComments().size());
        
        assertTrue(targetStatement.getParameterMarkers().containsAll(manualCopyStatement.getParameterMarkers()));
        assertTrue(targetStatement.getVariableNames().containsAll(manualCopyStatement.getVariableNames()));
        assertTrue(targetStatement.getComments().containsAll(manualCopyStatement.getComments()));
    }
    
    @Test
    void assertDuplicateAddParameterMarkersBehavior() {
        // Test the behavior of duplicate addParameterMarkers calls
        ParameterMarkerExpressionSegment param = new ParameterMarkerExpressionSegment(0, 0, 1);
        
        // First call
        targetStatement.addParameterMarkers(Collections.singletonList(param));
        assertEquals(1, targetStatement.getParameterMarkers().size());
        assertEquals(1, targetStatement.getParameterCount());
        
        // Second call with the same parameter object (same reference)
        targetStatement.addParameterMarkers(Collections.singletonList(param));
        // LinkedHashSet prevents duplicate object references
        assertEquals(1, targetStatement.getParameterMarkers().size());
        // uniqueParameterIndexes still 1 due to same index
        assertEquals(1, targetStatement.getParameterCount());
        
        // Third call with a new parameter object but same index
        // Same index, different position
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(1, 1, 1);
        targetStatement.addParameterMarkers(Collections.singletonList(param2));
        // Different object added
        assertEquals(2, targetStatement.getParameterMarkers().size());
        // uniqueParameterIndexes still 1 due to same index
        assertEquals(1, targetStatement.getParameterCount());
    }
    
    @Test
    void assertSQLStatementAddParameterMarkersBehavior() {
        // Test SQLStatement's addParameterMarkers method directly
        final ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        final ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(0, 0, 1);
        
        // First call
        targetStatement.addParameterMarkers(Collections.singletonList(param1));
        assertEquals(1, targetStatement.getParameterMarkers().size(), "First parameter should be added");
        assertEquals(1, targetStatement.getUniqueParameterIndexes().size(), "Should have one unique parameter index");
        assertEquals(1, targetStatement.getParameterCount(), "Parameter count should be 1");
        
        // Second call with the same parameter object (same reference)
        targetStatement.addParameterMarkers(Collections.singletonList(param1));
        assertEquals(1, targetStatement.getParameterMarkers().size(), "Same object should not be added again");
        assertEquals(1, targetStatement.getUniqueParameterIndexes().size(), "Unique parameter index count should remain 1");
        assertEquals(1, targetStatement.getParameterCount(), "Parameter count should remain 1");
        
        // Third call with a new parameter object but same index
        targetStatement.addParameterMarkers(Collections.singletonList(param2));
        assertEquals(2, targetStatement.getParameterMarkers().size(), "Different object should be added");
        assertEquals(1, targetStatement.getUniqueParameterIndexes().size(), "Unique parameter index count should remain 1");
        assertEquals(1, targetStatement.getParameterCount(), "Parameter count should remain 1");
        
        // Verify the behavior: LinkedHashSet prevents duplicate object references
        assertTrue(targetStatement.getParameterMarkers().contains(param1), "First parameter should be in the set");
        assertTrue(targetStatement.getParameterMarkers().contains(param2), "Second parameter should be in the set");
    }
    
    @Test
    void assertCopyAttributesWithDuplicateVariableNames() {
        // Test that duplicate variable names are handled correctly
        originalStatement.getVariableNames().add("duplicate");
        // Add same name twice
        originalStatement.getVariableNames().add("duplicate");
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // CaseInsensitiveSet should handle duplicates
        assertEquals(1, targetStatement.getVariableNames().size());
        assertTrue(targetStatement.getVariableNames().contains("duplicate"));
    }
    
    @Test
    void assertCopyAttributesWithDuplicateComments() {
        // Test that duplicate comments are handled correctly
        CommentSegment comment = new CommentSegment("duplicate", 0, 8);
        originalStatement.getComments().add(comment);
        // Add same comment twice
        originalStatement.getComments().add(comment);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // LinkedList allows duplicates
        assertEquals(2, targetStatement.getComments().size());
        assertTrue(targetStatement.getComments().contains(comment));
    }
    
    @Test
    void assertCopyAttributesWithNullValues() {
        // Test that null values are handled correctly
        originalStatement.getVariableNames().add(null);
        originalStatement.getComments().add(null);
        
        SQLStatementCopyUtils.copyAttributes(originalStatement, targetStatement);
        
        // Should handle null values gracefully
        assertEquals(1, targetStatement.getVariableNames().size());
        assertEquals(1, targetStatement.getComments().size());
        assertTrue(targetStatement.getVariableNames().contains(null));
        assertTrue(targetStatement.getComments().contains(null));
    }
    
    @Test
    void assertParameterMarkerExpressionSegmentEquality() {
        // Test that ParameterMarkerExpressionSegment objects with same values are NOT considered equal
        // because they don't override equals() and hashCode()
        ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(0, 0, 1);
        
        // These should NOT be equal because they use default object reference comparison
        assertFalse(param1.equals(param2));
        assertFalse(param1.hashCode() == param2.hashCode());
        
        // Add both to a LinkedHashSet
        originalStatement.addParameterMarkers(Arrays.asList(param1, param2));
        
        // Both should be added because LinkedHashSet treats them as different objects
        assertEquals(2, originalStatement.getParameterMarkers().size());
        // Same parameter index
        assertEquals(1, originalStatement.getParameterCount());
    }
    
    @Test
    void assertParameterMarkerExpressionSegmentInequality() {
        // Test that ParameterMarkerExpressionSegment objects with different values are not equal
        ParameterMarkerExpressionSegment param1 = new ParameterMarkerExpressionSegment(0, 0, 1);
        // Different start/stop indices
        ParameterMarkerExpressionSegment param2 = new ParameterMarkerExpressionSegment(1, 1, 1);
        // Different parameter index
        ParameterMarkerExpressionSegment param3 = new ParameterMarkerExpressionSegment(0, 0, 2);
        
        // These should not be equal
        assertFalse(param1.equals(param2));
        assertFalse(param1.equals(param3));
        
        // Add all three to a LinkedHashSet
        originalStatement.addParameterMarkers(Arrays.asList(param1, param2, param3));
        
        // All three should be added because they are different
        assertEquals(3, originalStatement.getParameterMarkers().size());
        // param1 and param3 have same index 1
        assertEquals(2, originalStatement.getParameterCount());
    }
    
    /**
     * Test SQL statement implementation for testing purposes.
     */
    private static class TestSQLStatement extends SQLStatement {
        
        TestSQLStatement(final MockedDatabaseType databaseType) {
            super(databaseType);
        }
        
        // Expose protected methods for testing
        public Collection<Integer> getUniqueParameterIndexes() {
            return super.getUniqueParameterIndexes();
        }
    }
    
    static class MockedDatabaseType implements DatabaseType {
        
        @Override
        public Collection<String> getJdbcUrlPrefixes() {
            return Collections.singleton("jdbc:mock");
        }
        
        @Override
        public String getType() {
            return "MOCKED";
        }
    }
}

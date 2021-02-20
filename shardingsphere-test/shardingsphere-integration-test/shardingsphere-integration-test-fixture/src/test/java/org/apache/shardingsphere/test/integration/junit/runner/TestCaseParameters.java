package org.apache.shardingsphere.test.integration.junit.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;

@Getter
@ToString
@RequiredArgsConstructor
public class TestCaseParameters {
    
    private final String name;
    
    private final String parentPath;
    
    private final String statement;
    
    private final SQLExecuteType executeType;
    
    private final Class<?> testClass;
    
    private final IntegrationTestCaseContext testCaseContext;
    
    private final IntegrationTestCaseAssertion assertion;
    
}
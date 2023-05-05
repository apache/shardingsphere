package org.apache.shardingsphere.test.e2e;

import org.apache.shardingsphere.test.loader.ExternalCaseSettings;

@ExternalCaseSettings(value = "MySQL", caseURL = ExternalMySQLTestCase.CASE_URL, resultURL = ExternalMySQLTestCase.RESULT_URL)
public class ExternalMySQLTestCase extends ExternalCompatibilityTestCase {
    
    static final String CASE_URL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t";
    
    static final String RESULT_URL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/r";
}

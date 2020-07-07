package org.apache.shardingsphere.shardingproxy.backend.privilege;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegePathTest {
    @Test
    public void assertPrivilegeConstructor(){
        // information (database / information)
        Privilege privilege = new Privilege();
        assertThat(privilege.getPrivilegeInformation(),is("*.*.*"));
        Privilege privilege1 = new Privilege("testDB");
        assertThat(privilege1.getPrivilegeInformation(),is("testDB.*.*"));
        Privilege privilege2 = new Privilege("testDB.testTable");
        assertThat(privilege2.getPrivilegeInformation(),is("testDB.testTable.*"));
        Privilege privilege3 = new Privilege("testDB.testTable.col1;col2");
        assertThat(privilege3.getPrivilegeInformation(),is("testDB.testTable.col1;col2"));
        Privilege privilege4 = new Privilege("testDB");
        assertThat(privilege4.getPrivilegeInformation(),is("testDB.*.*"));
        // database table
        Privilege privilege5 = new Privilege("testDB","testTable");
        assertThat(privilege5.getPrivilegeInformation(),is("testDB.testTable.*"));
        // database table cols
        List<String> cols = new LinkedList<>();cols.add("col1");cols.add("col2");
        Privilege privilege6 = new Privilege("testDB","testTable",cols);
        assertThat(privilege6.getPrivilegeInformation(),is("testDB.testTable.col1;col2"));
    }

    @Test
    public void assertPrivilegeEquals(){
        Privilege privilege1 = new Privilege("testDB.testTable")
                ,privilege2 = new Privilege("testDB.testTable")
                ,privilege3 = new Privilege("testDB.testTable_diff")
                ,privilege4 = new Privilege("testDB .testTable")
                ,privilege5 = new Privilege("testDB. testTable");
        assertThat(privilege1.equals(privilege2),is(true));
        assertThat(privilege1.equals(privilege3),is(false));
        assertThat(privilege1.equals(privilege4),is(true));
        assertThat(privilege1.equals(privilege5),is(true));
        assertThat(privilege4.equals(privilege5),is(true));
        assertThat(privilege1.hashCode() == privilege2.hashCode(),is(true));
        assertThat(privilege1.hashCode() == privilege3.hashCode(),is(false));
        assertThat(privilege1.hashCode() == privilege4.hashCode(),is(true));
        assertThat(privilege1.hashCode() == privilege5.hashCode(),is(true));
        assertThat(privilege4.hashCode() == privilege5.hashCode(),is(true));
    }

    @Test
    public void assertContainPlace(){
        Privilege privilege1, privilege2, privilege3, privilege4;
        // information (database)
        privilege1 = new Privilege();
        privilege2 = new Privilege("testDB");
        privilege3 = new Privilege("testDB.testTable");
        privilege4 = new Privilege("testDB.testTable.col1;col2");
        assertThat(privilege1.containsTargetPlace("testDB"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false"),is(false));
        // table
        assertThat(privilege3.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB.testTable_false"),is(false));

        assertThat(privilege1.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB","testTable_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable_false"),is(true));

        assertThat(privilege2.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB","testTable_false"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB.testTable_false"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB_false.testTable_false"),is(false));

        assertThat(privilege3.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB.testTable_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false.testTable_false"),is(false));

        assertThat(privilege4.containsTargetPlace("testDB","testTable"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB.testTable"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB.testTable_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false.testTable_false"),is(false));
        // column
        assertThat(privilege4.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilege4.containsTargetPlace("testDB","testTable","col1_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB.testTable.col1"),is(true));
        assertThat(privilege4.containsTargetPlace("testDB.testTable.col1_false"),is(false));

        assertThat(privilege1.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB","testTable_false","col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB","testTable_false","col1_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable","col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable","col1_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable_false","col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable.col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable.col_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable_false.col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB.testTable_false.col1_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable.col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable.col1_false"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable_false.col1"),is(true));
        assertThat(privilege1.containsTargetPlace("testDB_false.testTable_false.col1_false"),is(true));

        assertThat(privilege2.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB","testTable_false","col1"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB","testTable_false","col1_false"),is(true));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilege2.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));

        assertThat(privilege3.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilege3.containsTargetPlace("testDB","testTable_false","col1"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB","testTable_false","col1_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilege3.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));

        assertThat(privilege4.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilege4.containsTargetPlace("testDB","testTable","col_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB","testTable_false","col1"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB","testTable_false","col1_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilege4.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));
    }
}

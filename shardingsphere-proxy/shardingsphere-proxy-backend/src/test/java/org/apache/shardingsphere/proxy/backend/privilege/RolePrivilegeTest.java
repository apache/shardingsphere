package org.apache.shardingsphere.shardingproxy.backend.privilege;

import org.apache.shardingsphere.shardingproxy.backend.privilege.impl.RolePrivilege;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RolePrivilegeTest {
    @Test
    public void assertRolePrivilegeGenerator(){
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        assertThat(rolePrivilege, instanceOf(RolePrivilege.class));
    }

    @Test
    public void assertRolePrivilegeEquals(){
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        rolePrivilege.grant("select","testDB1");
        rolePrivilege.grant("select","testDB2 ");
        rolePrivilege.grant("select"," testDB3");
        rolePrivilege.grant("select"," testDB4 ");
        RolePrivilege rolePrivilege2 = new RolePrivilege("testRole");
        rolePrivilege2.grant("select","testDB4");
        rolePrivilege2.grant("select","testDB3");
        rolePrivilege2.grant("select","testDB2");
        assertThat(rolePrivilege.equals(rolePrivilege2),is(false));
        assertThat(rolePrivilege.hashCode()==rolePrivilege2.hashCode(),is(false));
        rolePrivilege2.grant("select","testDB1");
        assertThat(rolePrivilege.equals(rolePrivilege2),is(true));
        assertThat(rolePrivilege.hashCode()==rolePrivilege2.hashCode(),is(true));
    }

    @Test
    public void assertRolePrivilegeCheckExecutor(){
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        Privilege privilege1 = new Privilege("testDB1")
                ,privilege2 = new Privilege("testDB2.testTable")
                ,privilege3 = new Privilege("testDB3.testTable.col1");
        rolePrivilege.addPrivilege("select",privilege1);
        rolePrivilege.addPrivilege("select",privilege2);
        rolePrivilege.addPrivilege("select",privilege3);
        // information (database / information)
        assertThat(rolePrivilege.checkPrivilege("select","testDB_false"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB1"),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB2"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB2.testTable"),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB2. testTable"),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB3.testTable"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB3.testTable.col1"),is(true));
        // database table
        assertThat(rolePrivilege.checkPrivilege("select","testDB1","testTable"),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB2"," testTable"),is(true));
        // database table col
        assertThat(rolePrivilege.checkPrivilege("select","testDB1", "testTable", "col1"),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB1", "testTable", "col1 "),is(true));
        assertThat(rolePrivilege.checkPrivilege("select","testDB_false", "testTable", "col1"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB", "testTable_false", "col1"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB", "testTable", "col1_false"),is(false));
    }

    @Test
    public void assertRolePrivilegeGrantExecutor(){
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        RolePrivilege rolePrivilege2 = new RolePrivilege("testRole2");
        // information (database / information)
        rolePrivilege.grant("select", "*");
        assertThat(rolePrivilege.checkPrivilege("select","testDB.*"),is(true));
        rolePrivilege2.grant("select", "testDB");
        assertThat(rolePrivilege2.checkPrivilege("select","testDB"),is(true));
        assertThat(rolePrivilege2.checkPrivilege("select","testDB_false"),is(false));
        rolePrivilege2.grant("select", "testDB2.testTable");
        assertThat(rolePrivilege2.checkPrivilege("select","testDB2.testTable"),is(true));
        assertThat(rolePrivilege2.checkPrivilege("select","testDB2.testTable_false"),is(false));
        // database table
        rolePrivilege.grant("delete","testDB","testTable");
        assertThat(rolePrivilege.checkPrivilege("delete","testDB","testTable"),is(true));
        assertThat(rolePrivilege.checkPrivilege("delete","testDB","testTable_false"),is(false));
        // database table column
        List<String> cols = new LinkedList<>(); cols.add("col1");
        rolePrivilege.grant("update","testDB","testTable",cols);
        assertThat(rolePrivilege.checkPrivilege("update","testDB","testTable","col1"),is(true));
        assertThat(rolePrivilege.checkPrivilege("update","testDB","testTable","col_false"),is(false));
    }

    @Test
    public void assertRolePrivilegeRevokeExecutor(){
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        rolePrivilege.grant("select", "testDB2");
        rolePrivilege.grant("select", "testDB.testTable.col1");
        rolePrivilege.grant("select", "testDB.testTable2");
        rolePrivilege.grant("select", "testDB.testTable.col3");
        // information (database / information)
        rolePrivilege.revoke("select","testDB.testTable.col1");
        rolePrivilege.revoke("select","testDB2");
        assertThat(rolePrivilege.checkPrivilege("select","testDB2"),is(false));
        assertThat(rolePrivilege.checkPrivilege("select","testDB.testTable.col1"),is(false));
        // database table
        rolePrivilege.revoke("select","testDB", "testTable2");
        assertThat(rolePrivilege.checkPrivilege("select","testDB.testTable2"),is(false));
        // database table column
        List<String> cols = new LinkedList<>(); cols.add("col3");
        rolePrivilege.revoke("select","testDB", "testTable",cols);
        assertThat(rolePrivilege.checkPrivilege("select","testDB.testTable.col3"),is(false));
    }
}

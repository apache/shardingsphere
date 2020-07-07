package org.apache.shardingsphere.shardingproxy.backend.privilege;

import org.apache.shardingsphere.shardingproxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.shardingproxy.backend.privilege.impl.UserPrivilege;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class UserPrivilegeTest {
    @Test
    public void userPrivilegeConstruct(){
        UserPrivilege userPrivilege = new UserPrivilege("testUser","pw");
        assertThat(userPrivilege,instanceOf(UserPrivilege.class));
    }

    @Test
    public void userPrivilegeCheckNotInRole(){
        // information (database / information)
        UserPrivilege userPrivilege = new UserPrivilege("testUser","pw");
        Privilege privilege1 = new Privilege()
                ,privilege2 = new Privilege("testDB.testTable.col1;col2");
        userPrivilege.addPrivilege("select",privilege1); // select all privilege
        userPrivilege.addPrivilege("delete",privilege2); // delete testDB.testTable.col1;col2
        assertThat(userPrivilege.checkPrivilege("delete","testDB"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false"),is(true));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable.col1"),is(true));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable.col_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false.testTable_false"),is(true));
        // database table
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false", "testTable_false"),is(true));
        // database table column
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable", "col1"),is(true));
        assertThat(userPrivilege.checkPrivilege("delete","testDB_false", "testTable", "col_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false", "testTable_false", "col_false"),is(true));
    }

    @Test
    public void userPrivilegeGrant(){
        UserPrivilege userPrivilege1,userPrivilege2;
        // role
        userPrivilege1 = new UserPrivilege("testUser","pw");
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        Privilege privilege1 = new Privilege()
                ,privilege2 = new Privilege("testDB_role.testTable_role.col1;col2");
        rolePrivilege.addPrivilege("select",privilege1); // select all privilege
        rolePrivilege.addPrivilege("delete",privilege2); // delete testDB.testTable.col1;col2
        userPrivilege1.grant(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        userPrivilege1.grant(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        // information (database / information)
        userPrivilege1 = new UserPrivilege("testUser","pw");
        userPrivilege2 = new UserPrivilege("testUser2","pw");
        userPrivilege1.grant("select", "*");
        assertThat(userPrivilege1.checkPrivilege("select","*"),is(true));
        userPrivilege2.grant("select", "testDB");
        assertThat(userPrivilege2.checkPrivilege("select","testDB"),is(true));
        assertThat(userPrivilege2.checkPrivilege("select","testDB_false"),is(false));
        userPrivilege2.grant("select", "testDB2.testTable");
        assertThat(userPrivilege2.checkPrivilege("select","testDB2.testTable"),is(true));
        assertThat(userPrivilege2.checkPrivilege("select","testDB2.testTable_false"),is(false));
        // database table
        userPrivilege1.grant("delete","testDB","testTable");
        assertThat(userPrivilege1.checkPrivilege("delete","testDB","testTable"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB","testTable_false"),is(false));
        // database table column
        List<String> cols = new LinkedList<>(); cols.add("col1");
        userPrivilege1.grant("update","testDB","testTable",cols);
        assertThat(userPrivilege1.checkPrivilege("update","testDB","testTable","col1"),is(true));
        assertThat(userPrivilege1.checkPrivilege("update","testDB","testTable","col_false"),is(false));
    }

    @Test
    public void userPrivilegeRevoke(){
        UserPrivilege userPrivilege1,userPrivilege2;
        userPrivilege1 = new UserPrivilege("testUser","pw");
        userPrivilege1.grant("select", "testDB2");
        userPrivilege1.grant("select", "testDB.testTable.col1");
        userPrivilege1.grant("select", "testDB.testTable2");
        userPrivilege1.grant("select", "testDB.testTable.col3");
        //role
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        Privilege privilege1 = new Privilege()
                ,privilege2 = new Privilege("testDB_role.testTable_role.col1;col2");
        rolePrivilege.addPrivilege("select",privilege1); // select all privilege
        rolePrivilege.addPrivilege("delete",privilege2); // delete testDB.testTable.col1;col2
        userPrivilege1.revoke(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(0));
        userPrivilege1.revoke(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(0));
        // information (database / information)
        userPrivilege1.revoke("select","testDB.testTable.col1");
        userPrivilege1.revoke("select","testDB2");
        assertThat(userPrivilege1.checkPrivilege("select","testDB2"),is(false));
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable.col1"),is(false));
        // database table
        userPrivilege1.revoke("select","testDB", "testTable2");
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable2"),is(false));
        // database table column
        List<String> cols = new LinkedList<>(); cols.add("col3");
        userPrivilege1.revoke("select","testDB", "testTable",cols);
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable.col3"),is(false));
    }

    @Test
    public void userPrivilegeCheckInRole(){
        UserPrivilege userPrivilege1;
        userPrivilege1 = new UserPrivilege("testUser","pw");
        userPrivilege1.grant("select", "testDB2");
        userPrivilege1.grant("select", "testDB.testTable2");
        userPrivilege1.grant("select", "testDB.testTable.col1;col2");
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        Privilege privilege2 = new Privilege("testDB_role.testTable_role.col1;col2");
        rolePrivilege.addPrivilege("delete",privilege2); // delete testDB.testTable.col1;col2
        userPrivilege1.grant(rolePrivilege);
        // information (database / information)
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable2"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role.testTable_role.col1"),is(true));
        // database table
        assertThat(userPrivilege1.checkPrivilege("select","testDB","testTable2"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role","testTable_role"),is(false));
        // database table column
        assertThat(userPrivilege1.checkPrivilege("select","testDB","testTable","col1"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role","testTable_role","col1"),is(true));
    }

    @Test
    public void userPrivilegeEquals(){
        UserPrivilege userPrivilegeStdNoRole,userPrivilegeNoRole1,userPrivilegeNoRole2
                ,userPrivilegeStdRole,userPrivilegeRole1,userPrivilegeRole2;
        userPrivilegeStdNoRole = new UserPrivilege("testUser","pw");
        userPrivilegeNoRole1 = new UserPrivilege("testUser","pw");
        userPrivilegeNoRole2 = new UserPrivilege("testUser","pw");
        userPrivilegeStdRole = new UserPrivilege("testUser","pw");
        userPrivilegeRole1 = new UserPrivilege("testUser","pw");
        userPrivilegeRole2 = new UserPrivilege("testUser","pw");
        userPrivilegeStdNoRole.grant("select", "testDB2");
        userPrivilegeNoRole1.grant("select", "testDB2 ");
        userPrivilegeNoRole2.grant("select", "testDB3");
        // without role
        assertThat(userPrivilegeStdNoRole.equals(userPrivilegeNoRole1),is(true));
        assertThat(userPrivilegeStdNoRole.equals(userPrivilegeNoRole2),is(false));
        assertThat(userPrivilegeStdNoRole.hashCode()==userPrivilegeNoRole1.hashCode(),is(true));
        assertThat(userPrivilegeStdNoRole.hashCode()==userPrivilegeNoRole2.hashCode(),is(false));
        // role
        RolePrivilege rolePrivilege = new RolePrivilege("testRole")
                , rolePrivilege1 = new RolePrivilege("testRole")
                , rolePrivilege2 = new RolePrivilege("testRole");
        Privilege privilege = new Privilege("testDB_role.testTable_role.col1;col2")
                , privilege1 = new Privilege("testDB_role.testTable_role.col1 ;col2")
                , privilege2 = new Privilege("testDB_role.testTable_role.col1;col3");
        rolePrivilege.addPrivilege("select",privilege);
        rolePrivilege1.addPrivilege("select",privilege1);
        rolePrivilege2.addPrivilege("select",privilege2);
        assertThat(rolePrivilege.equals(rolePrivilege1),is(true));
        assertThat(rolePrivilege.equals(rolePrivilege2),is(false));
        assertThat(rolePrivilege.hashCode()==rolePrivilege1.hashCode(),is(true));
        assertThat(rolePrivilege.hashCode()==rolePrivilege2.hashCode(),is(false));
    }
}

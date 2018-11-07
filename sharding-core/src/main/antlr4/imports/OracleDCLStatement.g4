grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;
/**
 * each statement has a url, 
 * each base url : https://docs.oracle.com/database/121/SQLRF/.
 * no begin statement in oracle
 */
//statements_9014.htm#SQLRF01603
grant
    : GRANT
    (
    	(grantSystemPrivileges | grantObjectPrivilegeClause) (CONTAINER EQ_ (CURRENT | ALL))?
        | grantRolesToPrograms
    )
    ;
    
grantSystemPrivileges
    : systemObjects TO (granteeClause | granteeIdentifiedBy) (WITH (ADMIN | DELEGATE) OPTION)?
    ; 
    
systemObjects
    : systemObject(COMMA systemObject)*
    ;
          
systemObject
    : ALL PRIVILEGES
    | roleName
    | systemPrivilege
    ;
        
systemPrivilege
    : ID *?
    ;


granteeClause
    : grantee (COMMA grantee)*
    ;
    
grantee
    : userName 
    | roleName 
    | PUBLIC 
    ;
    
granteeIdentifiedBy
    : userName (COMMA userName)* IDENTIFIED BY STRING (COMMA STRING)*
    ;
    
grantObjectPrivilegeClause
    : grantObjectPrivilege (COMMA grantObjectPrivilege)* onObjectClause
    TO granteeClause(WITH HIERARCHY OPTION)?(WITH GRANT OPTION)?
    ;
    
grantObjectPrivilege
    : (objectPrivilege | ALL PRIVILEGES?)( LP_ columnName (COMMA columnName)* RIGHT_PAREN)? 
    ;

objectPrivilege
    : ID *?
    ;
    
onObjectClause
    : ON 
    (
    	schemaName? ID 
       | USER userName ( COMMA userName)*
       | (DIRECTORY | EDITION | MINING MODEL | JAVA (SOURCE | RESOURCE) | SQL TRANSLATION PROFILE) schemaName? ID 
    )
    ;
    
grantRolesToPrograms
    : roleName (COMMA roleName)* TO programUnit ( COMMA programUnit )*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
    ;
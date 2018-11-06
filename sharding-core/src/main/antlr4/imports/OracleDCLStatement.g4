grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

grant
    : GRANT
    (
    	(grantSystemPrivileges | grantObjectPrivilegeClause) (CONTAINER EQ_OR_ASSIGN (CURRENT | ALL))?
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
    : (objectPrivilege | ALL PRIVILEGES?)( LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN)? 
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
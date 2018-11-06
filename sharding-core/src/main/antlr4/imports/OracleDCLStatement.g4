grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;
granteeClause
    : grantee (COMMA grantee)
    ;
    
grantee
    : userName 
    | roleName 
    | PUBLIC 
    ;
    
granteeIdentifiedBy
    : userName (COMMA userName)* IDENTIFIED BY STRING (COMMA STRING)*
    ;
    
grantObjectPrivileges
    : objectPrivilege (COMMA objectPrivilege)*
    ;
    
grantObjectPrivilege
    : (objectPrivilege | ALL PRIVILEGES?)( LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN)?
    ;

objectPrivilege
    : ID *?
    ;
    
grantRolesToPrograms
    : roleName (COMMA roleName)* TO programUnit ( COMMA programUnit )*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
    ;
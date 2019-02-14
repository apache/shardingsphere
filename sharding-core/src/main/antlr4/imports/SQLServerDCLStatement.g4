grammar SQLServerDCLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, BaseRule, DataType;

grant
    : grantGeneral | grantDW
    ;
    
grantGeneral
    : GRANT generalPrisOn TO ids (WITH GRANT OPTION)? (AS ID)?
    ;
    
generalPrisOn
    : (ALL PRIVILEGES? | permissionOnColumns (COMMA_ permissionOnColumns)*) (ON (ID COLON_ COLON_)? tableName)?
    ;
    
permissionOnColumns
    : permission columnList?
    ;
    
permission
    : ID +?
    ;
    
grantDW
    : GRANT permission (COMMA_ permission)* (ON (classType COLON_ COLON_)? ID)? TO ids (WITH GRANT OPTION)?
    ;
    
classType
    : LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER
    ;
    
revoke
    : revokeGeneral | revokeDW
    ;
    
revokeGeneral
    : REVOKE (GRANT OPTION FOR)? ((ALL PRIVILEGES?)? | permissionOnColumns) (ON (ID COLON_ COLON_)? ID)? (TO | FROM) ids (CASCADE)? (AS ID)?
    ;
    
revokeDW
    : REVOKE permissionWithClass (FROM | TO)? ids CASCADE?
    ;
    
permissionWithClass
    : permission (COMMA_ permission)* (ON (classType COLON_ COLON_)? tableName)?
    ;
    
deny
    : DENY generalPrisOn TO ids CASCADE? (AS ID)?
    ;
    
createUser
    : CREATE USER 
    (
        userName (createUserBody1 | createUserBody4) 
        | createUserBody2 
        | createUserBody3 
    )?
    ;
    
createUserBody1
    : ((FOR | FROM) LOGIN ID)? (WITH optionsLists)?
    ;
    
createUserBody2
    : windowsPrincipal (WITH optionsLists)? | userName WITH PASSWORD EQ_ STRING_ (COMMA_ optionsList)* | ID FROM EXTERNAL PROVIDER
    ;
    
windowsPrincipal
    : ID BACKSLASH_ ID
    ;
     
createUserBody3
    : windowsPrincipal ((FOR | FROM) LOGIN ID)? | userName (FOR | FROM) LOGIN ID
    ;
    
createUserBody4
    : WITHOUT LOGIN (WITH optionsLists)? | (FOR | FROM) (CERTIFICATE ID | ASYMMETRIC KEY ID)
    ;
    
optionsLists
    : optionsList (COMMA_ optionsList)*
    ;
    
optionsList
    : ID EQ_ ID?
    ;
    
alterUser
    : ALTER USER userName WITH optionsLists
    ;
    
dropUser
    : DROP USER (IF EXISTS)? userName
    ;
    
createLogin
    : CREATE LOGIN (windowsPrincipal | ID) (WITH loginOptionList | FROM sources)
    ;
    
loginOptionList
    : PASSWORD EQ_ (STRING_ | ID HASHED) MUST_CHANGE? (COMMA_ optionsList)*
    ;
    
sources
    : WINDOWS (WITH optionsLists)? | CERTIFICATE ID | ASYMMETRIC KEY ID
    ;
    
alterLogin
    : ALTER LOGIN ID (ENABLE | DISABLE | WITH loginOption (COMMA_ loginOption)* | credentialOption)
    ;
    
loginOption
    : PASSWORD EQ_ (STRING_ | ID HASHED) (OLD_PASSWORD EQ_ STRING_ | passwordOption (passwordOption)?)?
    | DEFAULT_DATABASE EQ_ databaseName
    | optionsList
    | NO CREDENTIAL
    ;
    
passwordOption
    : MUST_CHANGE | UNLOCK
    ;
    
credentialOption
    : ADD CREDENTIAL ID | DROP CREDENTIAL
    ;
    
dropLogin
    : DROP LOGIN ID
    ;
    
createRole
    : CREATE ROLE roleName (AUTHORIZATION ID)
    ;
    
alterRole
    : ALTER ROLE roleName ((ADD | DROP) MEMBER ID | WITH NAME EQ_ ID)
    ;
    
dropRole
    : DROP ROLE (IF EXISTS)? roleName
    ;

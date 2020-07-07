package org.apache.shardingsphere.shardingproxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Getter
public class Privilege {
    // database.table.col1;col2;col3
    // * denote every db/table/cols
    private String privilegeInformation;

    public void setPrivilegeInformation(String privilegeInformation) {
        this.privilegeInformation = privilegeInformation.trim();
    }

    public Privilege(){
        this.setPrivilegeInformation("*.*.*");
    }

    public Privilege(String privilegeInformation){
        String candidatePrivilege = "*.*.*";
        if(privilegeInformation.length()==0) candidatePrivilege = "*.*.*";
        else {
            String[] splitTargets = privilegeInformation.split("\\.");
            switch (splitTargets.length){
                case 1:
                    candidatePrivilege = constructInformationString(splitTargets[0]);
                    break;
                case 2:
                    candidatePrivilege = constructInformationString(splitTargets[0],splitTargets[1]);
                    break;
                case 3:
                    candidatePrivilege = privilegeInformation;
                    break;
                default:
                    throw new ShardingSphereException("Invalid privilege format.");
            }
        }
        this.setPrivilegeInformation(candidatePrivilege);
    }

    public Privilege(String targetDatabase, String targetTable, List<String> targetColumns){
        String finalPrivilege = constructInformationString(targetDatabase,targetTable,targetColumns);
        this.setPrivilegeInformation(finalPrivilege);
    }

    public Privilege(String targetDatabase, String targetTable){
        this.setPrivilegeInformation(constructInformationString(targetDatabase,targetTable,null));
    }

    private String constructInformationString(String targetDatabase, String targetTable, List<String> targetColumns){
       StringBuilder informationStringBuilder = new StringBuilder();
       if(targetDatabase == null) informationStringBuilder.append("*.*.*");
       else {
           if(targetDatabase.trim().equals(""))
               throw new ShardingSphereException("Arg database arg can not be null.");
           informationStringBuilder.append(targetDatabase);
           if(targetTable == null) informationStringBuilder.append(".*.*");
           else{
               if(targetTable.trim().equals(""))
                   throw new ShardingSphereException("Arg table can not be null");
               informationStringBuilder.append(".");
               informationStringBuilder.append(targetTable);
               if(targetColumns == null) informationStringBuilder.append(".*");
               else {
                   if(targetColumns.isEmpty())
                       throw new ShardingSphereException("Arg columns size can not be null");
                   informationStringBuilder.append(".");
                   Iterator<String> iterator = targetColumns.iterator();
                   while (iterator.hasNext()){
                       String curCol = iterator.next();
                       if(iterator.hasNext()) informationStringBuilder.append(curCol + ";");
                       else informationStringBuilder.append(curCol);
                   }
               }
           }
       }
       return informationStringBuilder.toString();
    }

    private String constructInformationString(String targetDatabase, String targetTable){
        StringBuilder informationStringBuilder = new StringBuilder();
        if(targetDatabase == null) informationStringBuilder.append("*.*.*");
        else {
            if(targetDatabase.trim().equals(""))
                throw new ShardingSphereException("Arg database arg can not be null.");
            informationStringBuilder.append(targetDatabase);
            if(targetTable == null) informationStringBuilder.append(".*.*");
            else{
                if(targetTable.trim().equals(""))
                    throw new ShardingSphereException("Arg table can not be null");
                informationStringBuilder.append(".");
                informationStringBuilder.append(targetTable);
                informationStringBuilder.append(".*");
            }
        }
        return informationStringBuilder.toString();
    }

    private String constructInformationString(String targetDatabase){
        StringBuilder informationStringBuilder = new StringBuilder();
        if(targetDatabase == null) informationStringBuilder.append("*.*.*");
        else {
            if(targetDatabase.trim().equals(""))
                throw new ShardingSphereException("Arg database arg can not be null.");
            informationStringBuilder.append(targetDatabase);
            informationStringBuilder.append(".*.*");
        }
        return informationStringBuilder.toString();
    }

    public String getTargetDatabase(){
        String targetDatabase = this.getPrivilegeInformation().split("\\.")[0];
        return targetDatabase.trim();
    }

    public String getTargetTable(){
        String targetTable = this.getPrivilegeInformation().split("\\.")[1];
        return targetTable.trim();
    }

    public HashSet<String> getTargetColumns(){
        String[] columnList = this.getPrivilegeInformation().split("\\.")[2].split(";");
        if(columnList.length==0) return new HashSet<String>();
        else{
            HashSet<String> targetCols = new HashSet<>();
            for(int i=0;i<columnList.length;i++){
                targetCols.add(columnList[i].trim());
            }
            return targetCols;
        }
    }

    public boolean containsTargetPlace(String information){
        String[] splitTargets = information.split("\\.");
        switch (splitTargets.length){
            case 1:
                String targetDatabase = this.getTargetDatabase()
                        , targetTable = this.getTargetTable();
                if(targetDatabase.equals("*") ||
                        (splitTargets[0].trim().equals(targetDatabase) && targetTable.equals("*")))
                    return true;
                break;
            case 2:
                return containsTargetPlace(splitTargets[0], splitTargets[1]);
            case 3:
                return containsTargetPlace(splitTargets[0], splitTargets[1], splitTargets[2]);
            default:
                throw new ShardingSphereException("Invalid privilege format.");
        }
        return false;
    }

    public boolean containsTargetPlace(String database, String table){
        String targetDatabase = this.getTargetDatabase();
        if(targetDatabase.equals("*")) return true;
        else if(targetDatabase.equals(database.trim())){
            String targetTable = this.getTargetTable();
            HashSet<String> targetCols = this.getTargetColumns();;
            return (targetTable.equals("*") ||
                    (targetTable.equals(table.trim()) && targetCols.contains("*")));
        }
        return false;
    }

    public boolean containsTargetPlace(String database, String table, String column){
        String targetDatabase = this.getTargetDatabase();
        if(targetDatabase.equals("*")) return true;
        else if(targetDatabase.equals(database.trim())){
            String targetTable = this.getTargetTable();
            if(targetTable.equals("*")) return true;
            else if(targetTable.equals(table.trim())){
                HashSet<String> targetCols = this.getTargetColumns();
                return (targetCols.size()>0 && targetCols.contains("*") || targetCols.contains(column.trim()));
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Privilege privilege = (Privilege) o;
        return Objects.equals(this.getTargetDatabase(), privilege.getTargetDatabase()) &&
                Objects.equals(this.getTargetTable(), privilege.getTargetTable()) &&
                Objects.equals(this.getTargetColumns(), privilege.getTargetColumns());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTargetDatabase(),
                this.getTargetTable(),
                this.getTargetColumns());
    }
}

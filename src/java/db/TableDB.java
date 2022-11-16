package db;
import entity.Field;
import entity.ListField;
import entity.Table;
import entity.TableSave;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class TableDB extends BaseDB {

    public TableDB(HttpServletRequest request) {
        super(request);
    }
    
    public long createDescrTable(Table table) {
        long res = -1;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            String str = "INSERT INTO " + table.schema + "._tables_meta (name_table, title_table, fields_table) VALUES ('"
                    + table.name_table + "','" + table.title_table + "','" + table.fields_table + "');";
            int updateCount = statement.executeUpdate(str, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
              if (generatedKeys.next()) {
                res = generatedKeys.getLong("id_table");
              }
              else {
                  System.out.println("createTableId Creating failed, no ID obtained.");
              }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("createDescrTable error="+ex);
        }
        return res;
    }
    
    public String changeTable(Table tb) {
        boolean isData = false;
        String strUpd = "UPDATE " + tb.schema + "._tables_meta SET ";
        strUpd += "name_table ='" + tb.name_table +  "', title_table='" + tb.title_table +  "', fields_table='" + tb.fields_table
                + "' WHERE id_table = " + tb.id_table;
        String nameTableOld = "", titleTableOld = "", fieldsTable = "";
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            ResultSet res;
            String getTable = "SELECT * FROM " + tb.schema + "._tables_meta WHERE id_table=";
            res = statement.executeQuery(getTable + tb.id_table);
            if (res.next()) {
                nameTableOld = res.getString("name_table");
                titleTableOld = res.getString("title_table");
                fieldsTable = res.getString("fields_table");
                isData = true;
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("changeTable error="+ex);
        }
        if (isData) {
            ListField fieldsOld = gson.fromJson(fieldsTable, ListField.class);
            ListField fieldsNew = gson.fromJson(tb.fields_table, ListField.class);
            ListField listFieldNew = new ListField();
            ListField listFieldOld = new ListField();
            ListField listFieldDel = new ListField();
            boolean endNew, endOld;
            int iNew, iOld;
            int posNew = 0, posOld = 0;
            Field fNew = null, fOld = null;
            if (fieldsNew.size() == posNew) {
                endNew = true;
                iNew = Integer.MAX_VALUE;
            } else {
                fNew = fieldsNew.get(posNew);
                iNew = fNew.id_field;
                endNew = false;
            }
            if (fieldsOld.size() == posOld) {
                endOld = true;
                iOld = Integer.MAX_VALUE;
            } else {
                fOld = fieldsOld.get(posOld);
                iOld = fOld.id_field;
                endOld = false;
            }
            while ( ! (endNew && endOld)) {
                if (iOld == iNew) {
                    if (! fOld.name.equals(fNew.name)) {
                        listFieldOld.add(fNew);
                        listFieldOld.add(fOld);
                    }
                    posOld++;
                    if (fieldsOld.size() <= posOld) {
                        endOld = true;
                        iOld = Integer.MAX_VALUE;
                    } else {
                        fOld = fieldsOld.get(posOld);
                        iOld = fOld.id_field;
                    }
                    posNew++;
                    if (fieldsNew.size() <= posNew) {
                        endNew = true;
                        iNew = Integer.MAX_VALUE;
                    } else {
                        fNew = fieldsNew.get(posNew);
                        iNew = fNew.id_field;
                    }
                } else {
                    if (iOld < iNew) {
                        listFieldDel.add(fOld);
                        posOld++;
                        if (fieldsOld.size() <= posOld) {
                            endOld = true;
                            iOld = Integer.MAX_VALUE;
                        } else {
                            fOld = fieldsOld.get(posOld);
                            iOld = fOld.id_field;
                        }
                    } else {
                        listFieldNew.add(fNew);
                        posNew++;
                        if (fieldsNew.size() <= posNew) {
                            endNew = true;
                            iNew = Integer.MAX_VALUE;
                        } else {
                            fNew = fieldsNew.get(posNew);
                            iNew = fNew.id_field;
                        }
                    }
                }
            }
            
            try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
                String tableName = tb.schema + "." + nameTableOld;
                connection.setAutoCommit(false); 
                int ik = listFieldOld.size();
                for (int i = 0; i < ik; i = i + 2) {
                    fNew = listFieldOld.get(i);
                    fOld = listFieldOld.get(i + 1);
                    statement.executeUpdate("ALTER TABLE " + tableName + " RENAME COLUMN " + fOld.name + " TO " + fNew.name);
                }
                ik = listFieldDel.size();
                for (int i = 0; i < ik; i++) {
                    fNew = listFieldDel.get(i);
                    statement.executeUpdate("ALTER TABLE " + tableName + " DROP COLUMN " + fNew.name);
                }
                ik = listFieldNew.size();
                for (int i = 0; i < ik; i++) {
                    fNew = listFieldNew.get(i);
                    statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + setFieldParam(fNew));
                }
                if ( ! nameTableOld.equals(tb.name_table)) {
                    statement.executeUpdate("ALTER TABLE " + tableName + " RENAME COLUMN id_" + nameTableOld + " TO id_" + tb.name_table);
                    statement.executeUpdate("ALTER TABLE " + tableName + " RENAME TO " + tb.name_table);
                }
                statement.executeUpdate(strUpd);
                connection.commit();
            } catch (SQLException | ClassNotFoundException ex) {
                System.out.println("changeTable transaction error=" + ex);
                return "changeTable transaction error=" + ex;
            }
        } else {
            return "changeTable error=no table " + tb.name_table;
        }
        return "";
    }

    private String setFieldParam(Field f) {
        String sql = f.name;
        switch (f.type) {
            case "Gallery":
            case "Select":
            case "Text":
                if (f.length != null && f.length.length() > 0) {
                    sql += " VARCHAR(" + f.length + ")";
                } else {
                    sql += " TEXT";
                }
                break;
            case "Img":
                sql += " TEXT";
                break;
            case "Int":
                sql += " INTEGER";
                break;
            case "Date":
                sql += " DATE";
                break;
            case "Time":
                sql += " TIME";
                break;
            case "Timestamp":
                sql += " TIMESTAMP";
                break;
            case "TimestampZ":
                sql += " TIMESTAMP WITH TIME ZONE";
                break;
            case "Long":
                sql += " BIGINT";
                break;
            case "Float":
                sql += " REAL";
                break;
            case "Double":
                sql += " DOUBLE PRECISION";
                break;
            case "Serial":
                sql += " SERIAL";
                break;
            case "Bigserial":
                sql += " BIGSERIAL";
                break;
            case "Check":
            case "Switch":
            case "Boolean":
                sql += " BOOLEAN";
                break;
        }
        if (f.not_null != null && f.not_null) {
            sql += " NOT NULL";
        }
        if (f.def != null && f.def.length() > 0) {
            sql += " DEFAULT " + f.def;
        }
        if (f.unique) {
            sql += " UNIQUE";
        }
        return sql;
    }
    
    public String createTable(Table table) {
        String sql = "";
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            sql = "CREATE TABLE " + table.schema + "." + table.name_table + " (";
            ListField lf = gson.fromJson(table.fields_table, ListField.class);
            String sep = "";
            String pKey = "";
            String ind = "";
            for (Field f : lf) {
//System.out.println("createTable FFFF="+gson.toJson(f)+"<<");
                sql += sep + f.name;
                switch (f.type) {
                    case "Gallery":
                    case "Select":
                    case "Text":
                        if (f.length != null && f.length.length() > 0) {
                            sql += " VARCHAR(" + f.length + ")";
                        } else {
                            sql += " TEXT";
                        }
                        break;
                    case "Img":
                        sql += " TEXT";
                        break;
                    case "Int":
                        sql += " INTEGER";
                        break;
                    case "Date":
                        sql += " DATE";
                        break;
                    case "Time":
                        sql += " TIME";
                        break;
                    case "Timestamp":
                        sql += " TIMESTAMP";
                        break;
                    case "TimestampZ":
                        sql += " TIMESTAMP WITH TIME ZONE";
                        break;
                    case "Long":
                        sql += " BIGINT";
                        break;
                    case "Float":
                        sql += " REAL";
                        break;
                    case "Double":
                        sql += " DOUBLE PRECISION";
                        break;
                    case "Serial":
                        sql += " SERIAL";
                        break;
                    case "Bigserial":
                        sql += " BIGSERIAL";
                        break;
                    case "Check":
                    case "Switch":
                    case "Boolean":
                        sql += " BOOLEAN";
                        break;
                }
                if (f.def != null && f.def.length() > 0) {
                    sql += " DEFAULT " + f.def;
                }
                if (f.index != null && f.index) {
                    String un = "";
                    if (f.unique) {
                        un = " UNIQUE";
                    }
                    ind += "CREATE" + un + " INDEX IF NOT EXISTS " + f.name + " ON " + table.schema + "." + table.name_table + "(" + f.name + ");";
                }
                if (f.not_null != null && f.not_null) {
                    sql += " NOT NULL";
                }
                sep = ", ";
                if (f.key != null && f.key) {
                    pKey = ", PRIMARY KEY (" + f.name + ")";
                }
            }
            sql += pKey + ");" + ind;
//System.out.println("createTable SQL="+sql+"<<");
            statement.executeUpdate(sql);
            return "";
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("createTable error="+ex);
            return "createTable error="+ex + "<< SQL=" + sql +"<<";
        }
    }
    
    public List<Table> getListTables(String sql) {
        List<Table> lp = new ArrayList();
        ResultSet res;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            res = statement.executeQuery(sql);
            while (res.next()) {
                Table pm = new Table();
                pm.id_table = res.getLong("id_table");
                pm.name_table = res.getString("name_table");
                pm.title_table = res.getString("title_table");
                pm.fields_table = res.getString("fields_table");
                lp.add(pm);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("getListProject error="+ex);
        }
        return lp;
    }
    
    public String deleteTable(String schema, String name_table, long id_tab) {
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
// delete table            
            statement.executeUpdate("DROP TABLE IF EXISTS " + schema + "." + name_table);
// delete describe table
            statement.executeUpdate("DELETE FROM " + schema + "._tables_meta" + " WHERE id_table=" + id_tab);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("deleteTable error="+ex);
            return "deleteTable error="+ex;
        }
        return "";
    }
    
    public String exportTable(String name_table, String schema, String pathFile) {
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
//            String SQL = "SELECT * FROM " + schema + "." + name_table + " INTO OUTFILE " + pathFile + " FIELDS TERMINATED BY ',';";
            String copySQL = "COPY " + schema + "." + name_table + " TO '" + pathFile + "' csv header;";
//System.out.println("copySQL="+copySQL+"<<");
            statement.executeUpdate(copySQL);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("exportTable error="+ex);
            return "exportTable error="+ex;
        }
        return "";
    }
    
    public String saveData(String schema, TableSave ts) {
        long res = -1;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            int ik;
            if (ts.datNew.size() > 1) {
                String str = "INSERT INTO " + schema + "." + ts.name_table + " " + ts.datNew.get(0) + " VALUES ";
                String sep = "";
                for (int i = 1; i < ts.datNew.size(); i++) {
                    str += sep + ts.datNew.get(i);
                    sep = ", ";
                }
//System.out.println("saveData="+str+"<<");
                res = statement.executeUpdate(str);
                if (res < 0) {
                    return "saveData error= no insert Record";
                }
            }
            if (ts.dataDel.size() > 0) {
                String str = "DELETE FROM " + schema + "." + ts.name_table + " WHERE " + ts.name_primary + " IN (" + ts.dataDel.get(0);
                String sep = ", ";
                for (int i = 1; i < ts.dataDel.size(); i++) {
                    str += sep + ts.dataDel.get(i);
                }
                str += ")";
//System.out.println("saveData DEL="+str+"<<");
                res = statement.executeUpdate(str);
                if (res < 0) {
                    return "saveDeleteData error= no insert Record";
                }
            }
            if (ts.dataEdit.size() > 0) {
                ik = ts.dataEdit.size();
                String stEd = "UPDATE " + schema + "." + ts.name_table + " SET ";
                for (int i = 0; i < ik; i++) {
                    Timestamp timeS = new Timestamp(System.currentTimeMillis());
                    String dd = "__date_edit = '" + timeS.toInstant().toString() + "', ";
                    statement.executeUpdate(stEd + dd + ts.dataEdit.get(i));
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("saveData error="+ex);
            return "saveData error="+ex;
        }
        return "";
    }
}

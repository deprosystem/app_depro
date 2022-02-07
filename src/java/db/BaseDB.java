package db;

import com.google.gson.Gson;
import entity.ErrorSQL;
import entity.TokenUser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

public class BaseDB {
    public String urlDB;
/*
    public String userNameDB = "mobi";
    public String passwordDB = "mobi";
    public String nameDB = "ide_depro";
*/
    public Gson gson = new Gson();
    
    public HttpServletRequest request;
    
    public final String quote = "\"";
    public final String twoQuote = "\"\"";
    public final String quoteColon = "\":";
    public final String a = "\\";
    public String quoteEcran = a + quote;
    
    public BaseDB(HttpServletRequest request) {
        this.request = request;
    }

    public Connection getDBConnection() throws SQLException, ClassNotFoundException {
//        if (Constants.isLocale) {
        if (request.getServletContext().getRealPath("").indexOf(File.separator) != 0) {
            urlDB = "jdbc:postgresql://localhost:5001/";
        } else {
            urlDB = "jdbc:postgresql://localhost:5432/";
        }
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection (urlDB + "clients", "clients", "clients");
    }

    public String getQueryList(String sql) {
        StringBuilder result = new StringBuilder(2048);
        result.append("[");
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery(sql);
            ResultSetMetaData rsmd = res.getMetaData();
            int count = rsmd.getColumnCount();
            int count1 = count + 1;
            String[] names = new String[count1];
            int[] types = new int[count1];
            String selRec = "";
            String selField;
            for (int i = 1; i < count1; i++ ) {
                names[i] = rsmd.getColumnName(i);
                types[i] = rsmd.getColumnType(i);
            }
            while (res.next()) {
                result.append(selRec + "{");
                selField = "";
                for (int i = 1; i < count1; i++ ) {
                    result.append(selField + quote + names[i] + quoteColon);
//System.out.println("II="+i+" types[i]="+types[i]+" NNN="+names[i]+"<<");
                    switch(types[i]) {
                        case -5:
                            result.append(String.valueOf(res.getLong(i)));
                            break;
                        case 4:
                            result.append(String.valueOf(res.getInt(i)));
                            break;
                        case 7:
                            result.append(String.valueOf(res.getFloat(i)));
                            break;
                        case 8:
                            result.append(String.valueOf(res.getDouble(i)));
                            break;
                        case 12:
                            String sst = res.getString(i);
                            if (sst == null || sst.equals("null")) {
                                sst = "";
                            }
                            String stRes = escapingQuotes(sst);
                            result.append(quote + stRes + quote);                           
                            break;
                        case 91:
                            result.append(quote + res.getDate(i) + quote);
                            break;
                        case 93:
//System.out.print("DDD="+res.getTimestamp(i));
                            result.append(res.getTimestamp(i));
                            break;
                        case -7:
                            result.append(res.getBoolean(i));
                            break;
                    }
                    selField = ",";
                }
                result.append("}");
                selRec = ",";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("getQueryList error="+ex);
        }
        result.append("]");
        return result.toString();
    }
    
    public String getQueryRecord(String sql) {
        StringBuilder result = new StringBuilder(1024);
        result.append("{");
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery(sql);
            ResultSetMetaData rsmd = res.getMetaData();
            int count = rsmd.getColumnCount();
            int count1 = count + 1;
            String[] names = new String[count1];
            int[] types = new int[count1];
            String selField;
            for (int i = 1; i < count1; i++ ) {
                names[i] = rsmd.getColumnName(i);
                types[i] = rsmd.getColumnType(i);
            }
            if (res.next()) {
                selField = "";
                for (int i = 1; i < count1; i++ ) {
                    result.append(selField + quote + names[i] + quoteColon);
                    switch(types[i]) {
                        case -5:
                            result.append(String.valueOf(res.getLong(i)));
                            break;
                        case 4:
                            result.append(String.valueOf(res.getInt(i)));
                            break;
                        case 7:
                            result.append(String.valueOf(res.getFloat(i)));
                            break;
                        case 8:
                            result.append(String.valueOf(res.getDouble(i)));
                            break;
                        case 12:
                            String sst = res.getString(i);
                            if (sst == null || sst.equals("null")) {
                                sst = "";
                            }
                            String stRes = escapingQuotes(sst);
                            result.append(quote + stRes + quote);
                            break;
                        case -7:
                            result.append(res.getBoolean(i));
                            break;
                    }
                    selField = ",";
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("getQueryRecord error="+ex);
        }
        result.append("}");
        return result.toString();
    }
    
    public String copyTableInCSV(String sql, String pathOut) {
        StringBuilder result = new StringBuilder(2048);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(pathOut), "UTF8"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BaseDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery(sql);
            ResultSetMetaData rsmd = res.getMetaData();
            int count = rsmd.getColumnCount();
            int count1 = count + 1;
            String[] names = new String[count1];
            int[] types = new int[count1];
            String selField = "";
/*
            FileWriter writer;
System.out.println("pathOut-"+pathOut+"<<");
            writer = new FileWriter(pathOut, false);
*/
            for (int i = 1; i < count1; i++ ) {
                String nn = rsmd.getColumnName(i);
                names[i] = nn;
//                result.append(selField + quote + nn + quote);
                result.append(selField + nn);
                selField = ";";
                types[i] = rsmd.getColumnType(i);
            }
//System.out.println("head="+result+"<<");
            writer.write(result + "\n");
            result.setLength(0);
            while (res.next()) {
                selField = "";
                for (int i = 1; i < count1; i++ ) {
//                    result.append(selField + quote + names[i] + quoteColon);
//System.out.println("II="+i+" types[i]="+types[i]+" NNN="+names[i]+"<<");
                    switch(types[i]) {
                        case -5:
                            result.append(selField + String.valueOf(res.getLong(i)));
                            break;
                        case 4:
                            result.append(selField + String.valueOf(res.getInt(i)));
                            break;
                        case 7:
                            result.append(selField + String.valueOf(res.getFloat(i)));
                            break;
                        case 8:
                            result.append(selField + String.valueOf(res.getDouble(i)));
                            break;
                        case 12:
                            String sst = res.getString(i);
                            if (sst == null || sst.equals("null")) {
                                sst = "";
                            }
                            String stRes = escapingQuotesCSV(sst);
                            result.append(selField + quote + stRes + quote);                           
                            break;
                        case 91:
                            result.append(selField + quote + res.getDate(i) + quote);
                            break;
                        case 93:
//System.out.print("DDD="+res.getTimestamp(i));
                            result.append(selField + res.getTimestamp(i));
                            break;
                        case -7:
                            result.append(selField + res.getBoolean(i));
                            break;
                    }
                    selField = ";";
                }
//System.out.println("+++  "+result+"<<");
                writer.write(result + "\n");
                result.setLength(0);
            }
            writer.flush();
            writer.close();
        } catch (SQLException | IOException | ClassNotFoundException ex) {
            System.out.println("copyTableInCSV error="+ex);
            return "copyTableInCSV error="+ex;
        }
        return "";
    }
    
    public String inQuotes(String par) {
        return "'" + par + "'";
    }
    
    public String escapingQuotes(String sst) {
        if (sst == null) return "";
        StringBuilder stRes = new StringBuilder(1024);
        int iL = sst.length();
        if (iL > 0) {
            int j = 0;
            int k;
            do {
                k = sst.indexOf(quote, j);
                if (k > -1) {
                    stRes.append(sst.substring(j, k) + a + quote);
                    j = k + 1;
                } else {
                    stRes.append(sst.substring(j, iL));
                }
            } while (k > -1);
        }
        return stRes.toString();
    }
    
    public String escapingQuotesCSV(String sst) {
        if (sst == null) return "";
        StringBuilder stRes = new StringBuilder(1024);
        int iL = sst.length();
        if (iL > 0) {
            int j = 0;
            int k;
            do {
                k = sst.indexOf(quote, j);
                if (k > -1) {
                    stRes.append(sst.substring(j, k) + twoQuote);
                    j = k + 1;
                } else {
                    stRes.append(sst.substring(j, iL));
                }
            } while (k > -1);
        }
        return stRes.toString();
    }
    
    public TokenUser getUserByToken(String token, String schema){
        TokenUser tu = new TokenUser();
        tu.userId = -1;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SQL.getUserToken_1 + schema + SQL.getUserToken_3 + inQuotes(token) + ";");
            if (result.next()) {
                tu.userId = result.getLong("user_id");
                tu.userResurseInd = result.getString("user_resurse_ind");
                tu.token = result.getString("token");
                tu.dateCreate = result.getLong("date_create");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("getUserByToken " + ex);
        }
        return tu;
    }
    
    public ErrorSQL insertInTab(String sql, String id) {
        long res = -1;
        ErrorSQL erSql = new ErrorSQL();
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            int updateCount = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    res = generatedKeys.getLong(id);
                } else {
                    System.out.println("insertInTab Creating failed, no ID obtained");
                    erSql.errorMessage = "insertInTab Creating failed, no ID obtained";
                }
            } 
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("insertInTab error="+ex);
            erSql.errorMessage = ex.toString();
        }
        erSql.id = res;
        return erSql;
    }
    
    public ErrorSQL updateInTab(String sql) {
        ErrorSQL erSql = new ErrorSQL();
        erSql.errorMessage = "";
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("insertInTab error="+ex);
            erSql.errorMessage = ex.toString();
        }
        return erSql;
    }
}

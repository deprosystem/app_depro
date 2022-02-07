package db;

import entity.ResultGetUser;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

public class AutchDB extends BaseDB{
    
    public AutchDB(HttpServletRequest request) {
        super(request);
    }
    
    public int setToken(String token, long userId, String schema) {
        int size = -1;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            long dat = new Date().getTime();
            String str = "INSERT INTO " + schema + "._user_token (token, user_id, date_create) VALUES ('"+ token + "'," + userId + "," +dat + ");";
            size = statement.executeUpdate(str);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("setToken error="+ex);
        }
        return size;
    }
    
    public int removeOldTokens() {
        int size = -1;
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            long dat = new Date().getTime() - (60 * 60 * 24 * 1000);
            String str = "DELETE FROM token_user WHERE date_create < " + dat + ";";
            size = statement.executeUpdate(str);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("removeOldTokens error="+ex);
        }
        return size;
    }
    
    public ResultGetUser getUserByLogin(String sql) {
        StringBuilder result = new StringBuilder(1024);
        ResultGetUser resOut = new ResultGetUser();
        resOut.err = false;
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
                    String nameF = names[i];
                    if (nameF.equals("password")) {
                        resOut.password = res.getString(i);
                        continue;
                    }
                    if (nameF.equals("id_user")) {
                        resOut.id = res.getLong(i);
                        continue;
                    }
                    if (nameF.equals("login")) {
                        resOut.profile = res.getString(i);
                        continue;
                    }
                    result.append(selField + quote + nameF + quoteColon);
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
            } else {
                resOut.err = true;
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("getUserByLogin error="+ex);
            resOut.err = true;
        }
        result.append("}");
        resOut.profile = result.toString();
        return resOut;
    }
}

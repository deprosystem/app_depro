package db;

public class SQL {
    public static String getListTab = "";
    public static String getUserToken_1 = "SELECT * FROM ";
    public static String getUserToken_2 = ".token_user WHERE token=";
    public static String getUserToken_3 = "._user_token WHERE token=";
    public static String createTableTokenUser_1 = "CREATE TABLE ";
    public static String createTableTokenUser_2 = "._user_token(token VARCHAR(30) PRIMARY KEY, user_id BIGINT, date_create BIGINT);";
}

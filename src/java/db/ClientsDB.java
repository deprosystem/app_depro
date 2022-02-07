package db;

import entity.Query;
import entity.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.http.HttpServletRequest;

public class ClientsDB extends BaseDB {

    public ClientsDB(HttpServletRequest request) {
        super(request);
    }
    
    public String createSchema(String nameSchema) {
        TableDB tableDb = new TableDB(request);
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP SCHEMA IF EXISTS " + nameSchema + " CASCADE");
            statement.executeUpdate("CREATE SCHEMA " + nameSchema);
            statement.executeUpdate("CREATE TABLE " + nameSchema + "._tables_meta " 
                    + "(id_table SERIAL, name_table VARCHAR(50), title_table VARCHAR(100), fields_table TEXT, PRIMARY KEY ( id_table ))");
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS name_t ON " + nameSchema + "._tables_meta " + "(name_table)");
            statement.executeUpdate("CREATE TABLE " + nameSchema + "._querys_meta " 
                    + "(id_query SERIAL, name_query VARCHAR(100), type_query VARCHAR(12), origin_query TEXT, sql_query TEXT, param_query TEXT, err_1 TEXT, err_2 TEXT, PRIMARY KEY ( id_query ))"); 
// create table USER
            Table tb = new Table();
            tb.id_table = -1;
            tb.name_table = "user";
            tb.title_table = "User data table. You can add your own fields";
            tb.fields_table = "[{\"id_field\":0,\"name\":\"id_user\",\"title\":\"Primary key\",\"type\":\"Bigserial\",\"length\":\"\",\"def\":\"\",\"format\":\"\",\"not_null\":false,\"key\":true,\"index\":false,\"unique\":false,\"system\":\"primary\"},{\"id_field\":1,\"name\":\"login\",\"title\":\"Login\",\"type\":\"Text\",\"length\":40,\"def\":\"\",\"format\":\"\",\"not_null\":true,\"index\":true,\"unique\":true,\"system\":\"primary\"},{\"id_field\":2,\"name\":\"password\",\"title\":\"Password\",\"type\":\"Text\",\"length\":40,\"def\":\"\",\"format\":\"\",\"not_null\":false,\"index\":false,\"unique\":false,\"system\":\"primary\"}]";
            tb.schema = nameSchema;
            String res = "";
            long id = -1;
            id = tableDb.createDescrTable(tb);
            tb.id_table = id;
            res = tableDb.createTable(tb);
            
            statement.executeUpdate(SQL.createTableTokenUser_1 + nameSchema + SQL.createTableTokenUser_2);
            
            if (res.length() == 0) {
                QueryDB queryDB = new QueryDB(request);
                Query qu = new Query();
                qu.id_query = -1;
                qu.name_query = "sign_in";
                qu.type_query = "INSERT";
                qu.origin_query = "{\"fieldTable\":[{\"id_table\":" + id + ",\"name_table\":\"user\",\"fullness\":1,\"listFields\":[1,2]}],\"where\":null,\"order\":null}";
                qu.sql_query = "INSERT INTO " + nameSchema + ".user";
                qu.param_query = "user";
                qu.err_2 = "";
                qu.err_1 = "Incorrect login or password";
                id = queryDB.createQuery(qu, nameSchema);
                qu.name_query = "sign_up";
                qu.err_1 = "A user with this login already exists";
                id = queryDB.createQuery(qu, nameSchema);
                qu.name_query = "change_profile";
                qu.err_1 = "There is already an entry with such keys";
                qu.err_2 = "You need to log in";
                id = queryDB.createQuery(qu, nameSchema);
            }
            return res;
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("createSchema error="+ex.getMessage());
            return ex.getMessage();
        }
    }
    
    public String deleteSchema(String nameSchema) {
        try (Connection connection = getDBConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP SCHEMA IF EXISTS " + nameSchema + " CASCADE");
            return "";
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("createSchema error="+ex.getMessage());
            return ex.getMessage();
        }
    }
}

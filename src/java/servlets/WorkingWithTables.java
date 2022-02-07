package servlets;

import com.google.gson.JsonSyntaxException;
import db.SQL;
import db.TableDB;
import entity.DataServlet;
import entity.Table;
import entity.TableSave;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "WorkingWithTables", urlPatterns = {"/tables/*"})
public class WorkingWithTables extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
            TableDB tableDb = new TableDB(request);
            List<Table> listTables;
            Table tb;
            String schema;
            String res;
            String queryDat;
            switch (ds.query) {
                case "/tables/descr":
                    tb = null;
                    try {
                        String stDescr = getStringRequest(request);
                        tb = gson.fromJson(stDescr, Table.class);
                    } catch (JsonSyntaxException | IOException e) {
                        System.out.println(e);
                        sendError(response, "Tables create error " + e.toString());
                        break;
                    }
                    long id = -1;
                    if (tb != null) {
                        if (tb.id_table == -1) {
                            id = tableDb.createDescrTable(tb);
                            tb.id_table = id;
                            String st = tableDb.createTable(tb);
                            if (st.length() > 0) {
                                sendError(response, st);
                            } else {
                                sendResult(response, gson.toJson(tb));
                            }
                        } else {
                            res = tableDb.changeTable(tb);
                            if (res.length() == 0) {
                                sendResult(response, gson.toJson(tb));
                            } else {
                                sendError(response, res);
                            }
                        }
                    } else {
                        sendError(response, "Tables create error in initial data");
                    }
                    break;
                case "/tables/list":
                    schema = request.getHeader("schemDB");

                    listTables = tableDb.getListTables("SELECT * FROM " + schema + "._tables_meta");
                    res = gson.toJson(listTables);
                    sendResult(response, res);

                    break;
                case "/tables/save":
                    TableSave ts = null;
                    schema = request.getHeader("schemDB");
                    try {
                        queryDat = getStringRequest(request);
                        ts = gson.fromJson(queryDat, TableSave.class);
                        res = tableDb.saveData(schema, ts);
                        if (res.length() > 0) {
                            sendError(response, res);
                        } else {
                            sendResult(response, tableDb.getQueryList("SELECT * FROM " + schema + "." + ts.name_table));
                        }
                    } catch (IOException ex) {
                        sendError(response, "Tables save error: " + ex);
                    }
                    break;
                case "/tables/del_tab":
                    tb = null;
                    try {
                        String stDescr = getStringRequest(request);
                        tb = gson.fromJson(stDescr, Table.class);
                    } catch (JsonSyntaxException | IOException e) {
                        System.out.println(e);
                        sendError(response, "Tables delete error " + e.toString());
                        break;
                    }
                    if (tb != null) {
                        schema = request.getHeader("schemDB");
                        String result = tableDb.deleteTable(schema, tb.name_table, tb.id_table);
                        if (result.length() == 0) {
                            sendResultOk(response);
                        } else {
                            sendError(response, result);
                        }
                    } else {
                        sendError(response, "Tables delete error in param");
                    }
                    break;
                case "/tables/listdata":
                    schema = request.getHeader("schemDB");
                    String table = request.getParameter("name_table");
                    sendResult(response, tableDb.getQueryList("SELECT * FROM " + schema + "." + table));
                    break;
                case "/tables/export":
                    schema = request.getHeader("schemDB");
                    table = request.getParameter("name_table");
                    String appPath = ds.patchOutsideProject;
/*
                    if (appPath.indexOf(File.separator) == 0) {
                        appPath = "/usr/local/";
                    }
                    String pathExport = appPath + Constants.USERS_DATA + "export";
                    File ff = new File(appPath);
                    if ( ! ff.exists()) {
                        if( ! ff.mkdirs()) {                 
                            System.out.println("createDir Каталог " + ff.getAbsolutePath() + " создать не удалось.");
                            sendError(response, "createDir Каталог " + ff.getAbsolutePath() + " создать не удалось.");
                        }
                    }
*/
                    String result = tableDb.copyTableInCSV("SELECT * FROM " + schema + "." + table, appPath + "export/" + table + ".csv");
                    if (result.length() == 0) {
                        sendResult(response, "download/get_csv/" + table);
//                        sendResult(response, "export/" + table + ".csv");
                    } else {
                        sendError(response, result);
                    }
                    break;
            }
    }
    
    @Override
    public int needToLogin() {
        return 0;
    }
}

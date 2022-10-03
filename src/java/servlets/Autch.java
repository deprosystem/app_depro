package servlets;

import com.google.gson.JsonSyntaxException;
import db.AutchDB;
import db.QueryDB;
import entity.DataServlet;
import entity.ErrorSQL;
import entity.Query;
import entity.ResultGetUser;
import entity.TokenUser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import json_simple.FieldSimpl;
import json_simple.JsonSimple;
import json_simple.Record;

@WebServlet(name = "Autch", urlPatterns = {"/autch/*"})
@MultipartConfig
public class Autch extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
        String[] ar = (" " + ds.query).split("/");
        ds.schema = ar[2];
        String data = "";
        String sql;
        int count;
        String param_1;
        AutchDB autchDB = new AutchDB(request);
        QueryDB queryDB = new QueryDB(request);
        JsonSimple js = new JsonSimple();
        FieldSimpl fsimpl;
        Record rec;
        switch (ar[3]) {
            case "1":                   //  sign_in
                fsimpl = null;
                try {
                    data = getStringRequest(request);
                } catch (IOException e) {
                    sendError(response, ERR.PROF_ERR + e.toString());
                }
                try {
                    fsimpl = js.jsonToModel(data);
                } catch (json_simple.JsonSyntaxException ex) {
                    System.out.println("query SignIn JsonSyntaxException=" + ex);
                    sendError(response, "query SignIn JsonSyntaxException=" + ex.toString());
                }
                String sqlEx = "SELECT * FROM " + ds.schema + "._querys_meta WHERE id_query=1";
                Query resEx = queryDB.getQueryMobile(sqlEx);
                String stErrorQu = resEx.err_1;
                rec = (Record) fsimpl.value;
                String log = rec.getString("login");
                String pas = rec.getString("password");
                ResultGetUser resOut = autchDB.getUserByLogin("SELECT * FROM " + ds.schema + ".user" + " WHERE login='" + log + "'");
                if (resOut.err) {
                    sendError(response, stErrorQu);
                }
                if ( pas == null || ( ! pas.equals(resOut.password))) {
                    sendError(response, stErrorQu);
                } else {
                    count = 0;
                    do {
                        ds.token = createRandomStr(30);
                        count++;
                    } while (autchDB.setToken(ds.token, resOut.id, ds.schema) < 1 || count > 3);
                    if (count > 3) {
                        sendError(response, ERR.NO_USER);
                        break;
                    }
                    String res = "{\"token\":\"" + ds.token + "\",\"profile\":" + resOut.profile + "}";
                    sendResult(response, res);
                }
                break;
            case "2":                   //  sign_up
                if (request.getContentType() != null && 
                    request.getContentType().toLowerCase().indexOf("multipart") > -1 ) {
                    data = request.getParameter("data");
                } else {
                    try {
                        data = getStringRequest(request);
                    } catch (IOException e) {
                        sendError(response, ERR.INS_ERR + e.toString());
                    }
                }
                if (data.length() > 2) {
                    sqlEx = "SELECT * FROM " + ds.schema + "._querys_meta WHERE id_query=2";
                    resEx = queryDB.getQueryMobile(sqlEx);
                    stErrorQu = resEx.err_1;
                    String appPath = ds.patchOutsideProject;
                    if (appPath.indexOf(File.separator) == 0) {
                        appPath = "/usr/local/";
                    }

                    String fileName = "";
                    String pathImg = "img_app/" + ds.schema + "/";
                    String resultPath = appPath + pathImg;
//                    JsonSimple js = new JsonSimple();
                    fsimpl = null;
                    try {
                        fsimpl = js.jsonToModel(data);
                    } catch (json_simple.JsonSyntaxException ex) {
                        System.out.println("query SignUp JsonSyntaxException=" + ex);
                        sendError(response, "query SignUp JsonSyntaxException=" + ex.toString());
                    }
                    rec = (Record) fsimpl.value;

                    String tableName = "user";
                    String nameId = "id_" + tableName;
                    sql = "INSERT INTO " + ds.schema + "." + tableName;
                    String fieldName;
                    if (request.getContentType() != null && 
                        request.getContentType().toLowerCase().indexOf("multipart") > -1 ) {
                        try {
                            for (Part filePart : request.getParts()) {
                                fileName = filePart.getSubmittedFileName();
                                if (fileName == null) {
                                    continue;
                                }
                                fieldName = filePart.getName();
                                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                                InputStream inputStream = filePart.getInputStream();
                                byte[] buffer = new byte[1000];
                                fileName = tableName + "_" + fieldName + "_" + System.currentTimeMillis() + "." + fileExt;
                                createDir(resultPath);
                                FileOutputStream outputStream = new FileOutputStream(resultPath + fileName);
                                while (inputStream.available() > 0) {
                                    count = inputStream.read(buffer);
                                    outputStream.write(buffer, 0, count);
                                }
                                inputStream.close();
                                outputStream.close();
                                boolean noField = true;
                                for (FieldSimpl fv : rec) {
                                    if (fv.name.equals(fieldName)) {
                                        fv.value = pathImg + fileName;
                                        noField = false;
                                        break;
                                    }
                                }
                                if (noField) {
                                    FieldSimpl fv = new FieldSimpl();
                                    fv.name = fieldName;
                                    fv.type = FieldSimpl.TYPE_STRING;
                                    fv.value = pathImg + fileName;
                                    rec.add(fv);
                                }
                            }
                        } catch (IOException | ServletException ex) {
                            sendError(response, stErrorQu);
                        }
                    }

                    String ff = "", vv = "";
                    String sep = "";
                    for (FieldSimpl item : rec) {
                            ff += sep + item.name;
                            if (item.type == FieldSimpl.TYPE_STRING) {
                                vv += sep + "'" + item.value + "'";
                            } else {
                                vv += sep + item.value;
                            }
                            sep = ",";
                    }

                    sql += " (" + ff + ") VALUES (" + vv + ")";
                    ErrorSQL errSql = autchDB.insertInTab(sql, nameId);
                    if (errSql.id > -1) {
                        String result = "{\"" + nameId + "\":" + errSql.id;
                        for (FieldSimpl item : rec) {
                            if (item.name.equals("password")) {
                                continue;
                            }
                            if (item.type == FieldSimpl.TYPE_STRING) {
                                vv = "\"" + item.value + "\"";
                            } else {
                                vv = item.value.toString();
                            }
                            result += ",\""  + item.name + "\":" + vv;
                        }
                        result += "}";
                        count = 0;
                        do {
                            ds.token = createRandomStr(30);
                            count++;
                        } while (autchDB.setToken(ds.token, errSql.id, ds.schema) < 1 && count < 10);
                        if (count < 10) {
                            String res = "{\"token\":\"" + ds.token + "\",\"profile\":" + result + "}";
                            sendResult(response, res);
                        } else {
                            sendError(response, "Token creation error");
                        }
                    } else {
                        sendError(response, errSql.errorMessage);
                    }

                } else {
                    sendError(response, ERR.INS_ERR + " No data to register");
                }
                break;
            case "3":                   //  change profile
                sqlEx = "SELECT * FROM " + ds.schema + "._querys_meta WHERE id_query=3";
                resEx = queryDB.getQueryMobile(sqlEx);
                stErrorQu = resEx.err_1;
                if (ds.userId == -1) {
                    sendError(response, resEx.err_2);
                }
                if (request.getContentType() != null && 
                    request.getContentType().toLowerCase().indexOf("multipart") > -1 ) {
                    data = request.getParameter("data");
                } else {
                    try {
                        data = getStringRequest(request);
                    } catch (IOException e) {
                        sendError(response, ERR.INS_ERR + e.toString());
                    }
                }
                if (data.length() > 2) {
                    String appPath = ds.patchOutsideProject;
                    if (appPath.indexOf(File.separator) == 0) {
                        appPath = "/usr/local/";
                    }

                    String fileName = "";
                    String pathImg = "img_app/" + ds.schema + "/";
                    String resultPath = appPath + pathImg;
                    fsimpl = null;
                    try {
                        fsimpl = js.jsonToModel(data);
                    } catch (json_simple.JsonSyntaxException ex) {
                        System.out.println("query editProfile JsonSyntaxException=" + ex);
                    }
                    rec = (Record) fsimpl.value;

                    String tableName = "user";
                    String nameId = "id_" + tableName;
                    sql = "UPDATE " + ds.schema + "." + tableName + " SET ";
                    String fieldName;
                    if (request.getContentType() != null && 
                        request.getContentType().toLowerCase().indexOf("multipart") > -1 ) {
                        try {
                            for (Part filePart : request.getParts()) {
                                fileName = filePart.getSubmittedFileName();
                                if (fileName == null) {
                                    continue;
                                }
                                fieldName = filePart.getName();
                                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                                InputStream inputStream = filePart.getInputStream();
                                byte[] buffer = new byte[1000];
                                fileName = tableName + "_" + fieldName + "_" + System.currentTimeMillis() + "." + fileExt;
                                createDir(resultPath);
                                FileOutputStream outputStream = new FileOutputStream(resultPath + fileName);
                                while (inputStream.available() > 0) {
                                    count = inputStream.read(buffer);
                                    outputStream.write(buffer, 0, count);
                                }
                                inputStream.close();
                                outputStream.close();
                                boolean noField = true;
                                for (FieldSimpl fv : rec) {
                                    if (fv.name.equals(fieldName)) {
                                        fv.value = pathImg + fileName;
                                        noField = false;
                                        break;
                                    }
                                }
                                if (noField) {
                                    FieldSimpl fv = new FieldSimpl();
                                    fv.name = fieldName;
                                    fv.type = FieldSimpl.TYPE_STRING;
                                    fv.value = pathImg + fileName;
                                    rec.add(fv);
                                }
                            }
                        } catch (IOException | ServletException ex) {
                            sendError(response, ERR.INS_ERR + ex);
                        }
                    }

                    String fieldVal = "";
                    String sep = "";
                    String val;
                    for (FieldSimpl item : rec) {
                            if (item.type == FieldSimpl.TYPE_STRING) {
                                val = "'" + item.value + "'";
                            } else {
                                val = String.valueOf(item.value);
                            }
                            fieldVal += sep + item.name + "=" + val;
                            sep = ",";
                    }

                    sql += fieldVal + " WHERE id_user=" + ds.userId;
                    ErrorSQL er = autchDB.updateInTab(sql);
                    if (er.errorMessage.length() > 0) {
                        sendError(response, stErrorQu);
                    } else {
                        resOut = autchDB.getUserByLogin("SELECT * FROM " + ds.schema + ".user" + " WHERE id_user=" + ds.userId);
                        if (resOut.err) {
                            sendError(response, stErrorQu);
                        } else {
                            String res = "{\"token\":\"" + ds.token + "\",\"profile\":" + resOut.profile + "}";
                            sendResult(response, res);
                        }
                    }
                } else {
                    sendError(response, ERR.INS_ERR + " No data to edit profile");
                }
                break;
        }
    }

    @Override
    public int needToLogin() {
        return 1;
    }
}

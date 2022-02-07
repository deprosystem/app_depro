package servlets;

import com.google.gson.JsonSyntaxException;
import db.QueryDB;
import entity.DataServlet;
import entity.ErrorSQL;
import entity.Query;
import entity.SqlParam;
import entity.FieldValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import json_simple.FieldSimpl;
import json_simple.JsonSimple;
import json_simple.Record;

@WebServlet(name = "Querys", urlPatterns = {"/query/*"})
@MultipartConfig
public class Querys extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
            QueryDB queryDB = new QueryDB(request);
            Query qu;
            String schema = request.getHeader("schemDB");
            String res;
            String data = null;
            FieldSimpl fsimpl = null;
            JsonSimple js = new JsonSimple();
            Record rec = null;
            switch (ds.query) {
                case "/query/create":
                    qu = null;
                    String stDescr;
                    try {
                        stDescr = getStringRequest(request);
                        qu = gson.fromJson(stDescr, Query.class);
                    } catch (JsonSyntaxException | IOException e) {
                        System.out.println(e);
                        sendError(response, "Query create error " + e.toString());
                        break;
                    }
                    long id = -1;
                    if (qu != null) {
                        if (qu.id_query == -1) {
                            id = queryDB.createQuery(qu, schema);
                            qu.id_query = id;
                            sendResult(response, "{\"id_query\":" + id + "}");
                        } else {
                            String st = queryDB.changeQuery(qu, schema);
                            if (st.length() == 0) {
                                sendResultOk(response);
                            } else {
                                sendError(response, st);
                            }
                        }
                    } else {
                        sendError(response, "Tables create error in initial data");
                    }
                    break;
                case "/query/get":
                    String idQu = request.getParameter("id");
                    String sqlG = "SELECT * FROM " + schema + "._querys_meta WHERE id_query=" + idQu;
                    String resG = queryDB.getQueryRecord(sqlG);
                    sendResult(response, resG);
                    break;
                default:
                    String[] ar = (" " + ds.query).split("/");
                    ds.schema = ar[2];
                    String appPath = ds.patchOutsideProject;
                    if (appPath.indexOf(File.separator) == 0) {
                        appPath = "/usr/local/";
                    }

                    String fileName = "";
                    String pathImg = "img_app/" + ds.schema + "/";
                    String resultPath = appPath + pathImg;
                    switch (ar[3]) {
                        case "save_img":
                            if (request.getContentType() != null && 
                                request.getContentType().toLowerCase().indexOf("multipart") > -1 ) {
                                Record recRes = new Record();
                                try {
                                    for (Part filePart : request.getParts()) {
                                        fileName = filePart.getSubmittedFileName();
                                        if (fileName == null) {
                                            continue;
                                        }
                                        String fieldName = filePart.getName();
                                        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                                        InputStream inputStream = filePart.getInputStream();
                                        byte[] buffer = new byte[1000];
                                        fileName = fieldName + "_" + System.currentTimeMillis() + "." + fileExt;
                                        createDir(resultPath);
                                        FileOutputStream outputStream = new FileOutputStream(resultPath + fileName);
                                        while (inputStream.available() > 0) {
                                            int count = inputStream.read(buffer);
                                            outputStream.write(buffer, 0, count);
                                        }
                                        inputStream.close();
                                        outputStream.close();
                                        FieldSimpl fv = new FieldSimpl();
                                        fv.name = fieldName;
                                        fv.type = FieldSimpl.TYPE_STRING;
                                        fv.value = pathImg + fileName;
                                        recRes.add(fv);
                                    }
                                    
                                    String stRes = "{";
                                    String vv;
                                    for (FieldSimpl item : recRes) {
                                        if (item.type == FieldSimpl.TYPE_STRING) {
                                            vv = "\"" + item.value + "\"";
                                        } else {
                                            vv = item.value.toString();
                                        }
                                        stRes += ",\""  + item.name + "\":" + vv;
                                    }
                                    stRes += "}";
                                    sendResult(response, stRes);
                                    
                                } catch (IOException | ServletException ex) {
                                    sendError(response, ERR.INS_ERR + ex);
                                }
                            }
                            break;
                        case "del_img":
                            try {
                                data = getStringRequest(request);
                                fsimpl = js.jsonToModel(data);
                            } catch (IOException e) {
                                sendError(response, ERR.PROF_ERR + e.toString());
                            }
                            rec = (Record) fsimpl.value;
                            String nameFile = rec.getString("name");
                            deleteFile(appPath + nameFile);
                            sendResultOk(response);
                            break;
                        default:
                            String sqlEx = "SELECT * FROM " + ds.schema + "._querys_meta WHERE id_query=" + ar[3];
                            Query resEx = queryDB.getQueryMobile(sqlEx);
                            String sql;
                            String param_1;
//                            String data = "";
                            switch (resEx.type_query) {
                                case "SELECT":
                                    sql = resEx.sql_query;
                                    param_1 = resEx.param_query;
                                    if (param_1 != null && param_1.length() > 0) {
                                        String[] arPar = param_1.split(",");
                                        int ik = arPar.length;
                                        for (int i = 0; i < ik; i++) {
                                            String parI = request.getParameter(arPar[i]);
                                            if (parI == null) {
                                                sendError(response, "Query " + resEx.id_query + " " + resEx.name_query + " parameter not specified " + arPar[i]);
                                            }
                                            sql = sql.replace("%" + arPar[i] + "%", parI);
                                        }
                                    }
                                    String resMob = queryDB.getQueryList(sql);
                                    sendResult(response, resMob);
                                    break;
                                case "INSERT":
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
/*
                                        String appPath = ds.patchOutsideProject;
                                        if (appPath.indexOf(File.separator) == 0) {
                                            appPath = "/usr/local/";
                                        }

                                        String fileName = "";
                                        String pathImg = "img_app/" + ds.schema + "/";
                                        resultPath = appPath + pathImg;
*/
                                        fsimpl = null;
                                        try {
                                            fsimpl = js.jsonToModel(data);
                                        } catch (json_simple.JsonSyntaxException ex) {
                                            System.out.println("query INSERT JsonSyntaxException=" + ex);
                                        }
                                        rec = (Record) fsimpl.value;
                                        for (FieldSimpl fs : rec) {

                                        }
                                        String tableName = resEx.param_query;
                                        String nameId = "id_" + tableName;
                                        sql = resEx.sql_query;
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
                                                        int count = inputStream.read(buffer);
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
                                        ErrorSQL errSql = queryDB.insertInTab(sql, nameId);
                                        if (errSql.id > -1) {
                                            String result = "{\"" + nameId + "\":" + errSql.id;
                                            for (FieldSimpl item : rec) {
                                                if (item.type == FieldSimpl.TYPE_STRING) {
                                                    vv = "\"" + item.value + "\"";
                                                } else {
                                                    vv = item.value.toString();
                                                }
                                                result += ",\""  + item.name + "\":" + vv;
                                            }
                                            result += "}";
                                            sendResult(response, result);
                                        } else {
                                            sendError(response, errSql.errorMessage);
                                        }

                                    } else {
                                        sendError(response, ERR.INS_ERR + " No data to insert");
                                    }
                                    break;
                            }
                            break;
                    }
                    break;
            }
    }
    
}

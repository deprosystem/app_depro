package servlets;

import com.google.gson.JsonSyntaxException;
import db.QueryDB;
import entity.DataServlet;
import entity.ErrorSQL;
import entity.Field;
import entity.Query;
import entity.SqlParam;
import entity.FieldValue;
import entity.ListWhere;
import entity.NameVal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static javafx.scene.input.KeyCode.T;
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
            String schema = ds.schema;
            String res;
            String data = null;
            FieldSimpl fsimpl = null;
            JsonSimple js = new JsonSimple();
            Record rec = null;
//System.out.println("processRequest ds.userId="+ds.userId+"<<");
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
                            switch (resEx.type_query) {
                                case "SELECT":
                                    sql = resEx.sql_query;
                                    param_1 = resEx.param_query;
//System.out.println("processRequest SELECT ds.userId="+ds.userId+"<<");
                                    data = null;
                                    NameVal[] nameVal;
                                    try {
                                        data = getStringRequest(request);
                                    } catch (IOException ex) { }
                                    int pk;
                                    if (data != null && data.length() > 0) {
                                        try {
                                            fsimpl = js.jsonToModel(data);
                                        } catch (json_simple.JsonSyntaxException ex) {
                                            System.out.println("query SELECT JsonSyntaxException=" + ex);
                                            sendError(response, "query SELECT JsonSyntaxException=" + ex.toString());
                                        }
                                        if (fsimpl == null) {
                                            break;
                                        }
                                        rec = (Record) fsimpl.value;
                                        pk = rec.size();
                                        nameVal = new NameVal[pk];
                                        for (int p = 0; p < pk; p++) {
                                            FieldSimpl ff = rec.get(p);
                                            NameVal nv = new NameVal();
                                            nv.name = ff.name;
                                            nv.value = (String) ff.value;
                                            nameVal[p] = nv;
                                        }
                                    } else {
                                        Map<String, String[]> mapPar = request.getParameterMap();
                                        int i = 0;
                                        pk = mapPar.size();
                                        nameVal = new NameVal[pk];
                                        for(Map.Entry<String, String[]> entry: mapPar.entrySet()) {
                                            NameVal nv = new NameVal();
                                            nv.name = entry.getKey();
                                            nv.value = entry.getValue()[0];
                                            nameVal[i] = nv;
                                            i++;
                                        }
                                    }
                                    ListWhere arWhere = gson.fromJson(resEx.listWhere, ListWhere.class);
                                    int jkW = arWhere.size();
                                    if (param_1 != null && param_1.length() > 0) {
                                        String[] arPar = param_1.split(",");
                                        int ik = arPar.length;
                                        for (int i = 0; i < ik; i++) {
                                            String namePar = arPar[i];
                                            String parI = null;
                                            String nameParam_1 = namePar;
                                            int i_n = namePar.indexOf("=");
                                            if (i_n > -1) {
                                                nameParam_1 = namePar.substring(0, i_n);
                                            }
                                            for (int p = 0; p < pk; p++) {
                                                NameVal nv = nameVal[p];
                                                if (nv.name.equals(nameParam_1)) {
                                                    parI = nv.value;
                                                    break;
                                                }
                                            }
//System.out.println("processRequest 111 SELECT ds.userId="+ds.userId+"<< parI="+parI+"<< nameParam_1="+nameParam_1+"<<");
                                            if (parI != null && parI.equals(Constants.prefixProfileParam + "id_user")) {
                                                if (ds.userId < 0) {
                                                    sendError(response, Constants.ERR_NO_AUTCH);
                                                } else {
                                                    parI = String.valueOf(ds.userId);
                                                }
                                            }
//System.out.println("nameParam_1="+nameParam_1+"<< parI="+parI+"<<");
                                            String namePar5 = "%" + nameParam_1 + "%";
                                            for (int j = 0; j < jkW; j++) {
                                                String whereJ = arWhere.get(j);
                                                if (whereJ.indexOf(namePar5) > -1) {
                                                    if (parI == null) {
                                                        arWhere.set(j, "");
                                                    } else {
                                                        arWhere.set(j, whereJ.replace(namePar5, parI));
                                                    }
//                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    String sepW = " WHERE ";
                                    for (int j = 0; j < jkW; j++) {
                                        String whereJ = arWhere.get(j);
                                        if (whereJ.length() > 0) {
                                            sql += sepW + whereJ;
                                            sepW = " AND ";
                                        }
                                    }
                                        
                                    String ord = resEx.orderBy;
                                    if (ord != null && ord.length() > 0) {
                                        sql += " ORDER BY " + ord;
                                    }
                                    String resMob = queryDB.getQueryList(sql);
                                    if (resMob.indexOf("error") == 0) {
                                        sendError(response, resMob);
                                    } else {
                                        sendResult(response, resMob);
                                    }
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
                                        fsimpl = null;
                                        try {
                                            fsimpl = js.jsonToModel(data);
                                        } catch (json_simple.JsonSyntaxException ex) {
                                            System.out.println("query INSERT JsonSyntaxException=" + ex);
                                        }
                                        rec = (Record) fsimpl.value;

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
                                        String valSt;
                                        for (FieldSimpl item : rec) {
                                                ff += sep + item.name;
                                                if (item.type == FieldSimpl.TYPE_STRING) {
                                                    valSt = String.valueOf(item.value);
                                                    if (valSt.equals("\u0000id")) {
                                                        if (ds.userId < 0) {
                                                            sendError(response, Constants.ERR_NO_AUTCH);
                                                        } else {
//                                                            valSt = String.valueOf(ds.userId);
                                                            vv += sep + ds.userId;
                                                        }
                                                    } else {
                                                        vv += sep + "'" + valSt + "'";
                                                    }
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
                                                    vv = "\"" + escapingQuotes((String )item.value) + "\"";
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
                                case "FILTER":
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
                                    resMob = queryDB.getQueryList(sql);
                                    sendResult(response, resMob);
                                    break;
                                    
                                    
                                case "DELETE":
                                    sql = resEx.sql_query;
                                    param_1 = resEx.param_query;
//System.out.println("processRequest SELECT ds.userId="+ds.userId+"<<");
                                    data = null;
//                                    NameVal[] nameVal;
                                    try {
                                        data = getStringRequest(request);
                                    } catch (IOException ex) { }
//                                    int pk;
                                    if (data != null && data.length() > 0) {
                                        try {
                                            fsimpl = js.jsonToModel(data);
                                        } catch (json_simple.JsonSyntaxException ex) {
                                            System.out.println("query DELETE JsonSyntaxException=" + ex);
                                            sendError(response, "query DELETE JsonSyntaxException=" + ex.toString());
                                        }
                                        if (fsimpl == null) {
                                            break;
                                        }
                                        rec = (Record) fsimpl.value;
                                        pk = rec.size();
                                        nameVal = new NameVal[pk];
                                        for (int p = 0; p < pk; p++) {
                                            FieldSimpl ff = rec.get(p);
                                            NameVal nv = new NameVal();
                                            nv.name = ff.name;
                                            nv.value = ff.value.toString();
                                            nameVal[p] = nv;
                                        }
                                    } else {
                                        Map<String, String[]> mapPar = request.getParameterMap();
                                        int i = 0;
                                        pk = mapPar.size();
                                        nameVal = new NameVal[pk];
                                        for(Map.Entry<String, String[]> entry: mapPar.entrySet()) {
                                            NameVal nv = new NameVal();
                                            nv.name = entry.getKey();
                                            nv.value = entry.getValue()[0];
                                            nameVal[i] = nv;
                                            i++;
                                        }
                                    }
                                    
                                    arWhere = gson.fromJson(resEx.listWhere, ListWhere.class);
                                    jkW = arWhere.size();
                                    if (jkW > 0) {
                                        if (param_1 != null && param_1.length() > 0) {
                                            String[] arPar = param_1.split(",");
                                            int ik = arPar.length;
                                            for (int i = 0; i < ik; i++) {
                                                String namePar = arPar[i];
                                                String parI = null;
                                                String nameParam_1 = namePar;
                                                int i_n = namePar.indexOf("=");
                                                if (i_n > -1) {
                                                    nameParam_1 = namePar.substring(0, i_n);
                                                }
                                                for (int p = 0; p < pk; p++) {
                                                    NameVal nv = nameVal[p];
                                                    if (nv.name.equals(nameParam_1)) {
                                                        parI = nv.value;
                                                        break;
                                                    }
                                                }
    //System.out.println("processRequest 111 SELECT ds.userId="+ds.userId+"<< parI="+parI+"<< nameParam_1="+nameParam_1+"<<");
                                                if (parI != null && parI.equals(Constants.prefixProfileParam + "id_user")) {
                                                    if (ds.userId < 0) {
                                                        sendError(response, Constants.ERR_NO_AUTCH);
                                                    } else {
                                                        parI = String.valueOf(ds.userId);
                                                    }
                                                }
    //System.out.println("nameParam_1="+nameParam_1+"<< parI="+parI+"<<");
                                                String namePar5 = "%" + nameParam_1 + "%";
                                                for (int j = 0; j < jkW; j++) {
                                                    String whereJ = arWhere.get(j);
                                                    if (whereJ.indexOf(namePar5) > -1) {
                                                        if (parI == null) {
                                                            arWhere.set(j, "");
                                                        } else {
                                                            arWhere.set(j, whereJ.replace(namePar5, parI));
                                                        }
    //                                                    break;
                                                    }
                                                }
                                            }
                                        }
                                        sepW = " WHERE ";
                                        for (int j = 0; j < jkW; j++) {
                                            String whereJ = arWhere.get(j);
                                            if (whereJ.length() > 0) {
                                                sql += sepW + whereJ;
                                                sepW = " AND ";
                                            }
                                        }
    //System.out.println("DELETE SQL="+sql);
                                        int row = -1;
                                        try (Connection connection = queryDB.getDBConnection(); Statement statement = connection.createStatement()) {
                                            row = statement.executeUpdate(sql);
                                        } catch (SQLException | ClassNotFoundException ex) {
                                            System.out.println("DELETE error="+ex);
                                            sendError(response, "DELETE error="+ex);
                                        }
                                        sendResult(response, "{\"row\":" + row + "}");
                                    } else {
                                        sendError(response, "DELETE error: No deletion conditions");
                                    }
                                    break;
                            }
                            break;
                    }
                    break;
            }
    }
    
    public int needToLogin() {
        return 1;
    }
    
}

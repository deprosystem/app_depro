package servlets;
import db.ClientsDB;
import entity.DataServlet;
import entity.DescrHost;
import entity.ParamDelSchema;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "WorkingWithDB", urlPatterns = {"/db/*"})
public class WorkingWithDB extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
        ClientsDB clientsDB = new ClientsDB(request);
        switch (ds.query) {
            case "/db/create":
                try {
                    String stDescr = getStringRequest(request);
                    DescrHost dh = gson.fromJson(stDescr, DescrHost.class);
                    String res = clientsDB.createSchema(dh.res_ind);
                    if (res.length() > 0) {
                        sendError(response, "Host create error " + res);
                    } else {
                        sendResultOk(response);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                    sendError(response, "Host create error " + e.toString());
                }
                break;
            case "/db/del_schema":
                try {
                    String stDescr = getStringRequest(request);
                    ParamDelSchema dh = gson.fromJson(stDescr, ParamDelSchema.class);
                    String res = clientsDB.deleteSchema(dh.schema);
                    String appPath = ds.patchOutsideProject;
                    if (appPath.indexOf(File.separator) == 0) {
                        appPath = "/usr/local/";
                    }
                    String pathImg = "img_app/" + dh.schema + "/";
                    String resultPath = appPath + pathImg;
                    deleteDir(resultPath);
                    sendResultOk(response);
                } catch (IOException e) {
                    System.out.println(e);
                    sendError(response, "Schema delete error " + e.toString());
                }
                break;
            case "/db/addField":
                String schema = request.getHeader("schemDB");
                clientsDB.addField(schema);
                sendResultOk(response);
                break;
        }
    }

    @Override
    public int needToLogin() {
        return 0;
    }
}

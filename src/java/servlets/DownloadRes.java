package servlets;

import entity.DataServlet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DownloadRes", urlPatterns = {"/download/*"})
public class DownloadRes extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
            String[] ar = (" " + ds.query).split("/");
            String basePath = ds.patchOutsideProject;
            String userProjPath;
            String filename;
            File file;
            switch (ar[2]) {
                case "get_csv":
                        userProjPath = "export/" + ar[3] + ".csv";
                        filename = basePath + userProjPath;
                        file = new File(filename);
                        if (file.exists()) {
                            response.setHeader("Accept-Ranges", "bytes");
                            response.setContentType("application/txt");
                            response.setContentLength((int)file.length());
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                            try {
                                Files.copy(file.toPath(), response.getOutputStream());
                            } catch (IOException e) {
                                System.out.println(e);
                                sendError(response, "Get export csv error " + e.toString());
                                break;
                            }
                        } else {
                            sendError(response, "Export csv error");
                        }
                        String[] ff = filename.split("\\.");
//                        deleteDir(ff[0]);
                        deleteFile(filename);
                    break;
            }
    }
    
    @Override
    public int needToLogin() {
        return 0;
    }
}

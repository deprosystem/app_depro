package servlets;

import entity.DataServlet;
import entity.FileParam;
import entity.ResultCommander;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "WorkingWithImages", urlPatterns = {"/images/*"})
@MultipartConfig
public class WorkingWithImages extends BaseServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, DataServlet ds) {
        String appPath;
        String pathImg;
        String resultPath;
        switch (ds.query) {
            case "/images/save":
                String nameTable = "";
                String[] parArNameTable = request.getParameterValues("nameTable");
                if (parArNameTable != null) {
                    nameTable = parArNameTable[0];
                }
                String nameField = "";
                String[] parArNameField = request.getParameterValues("nameField");
                if (parArNameField != null) {
                    nameField = parArNameField[0];
                }
                
                String changeName = "";
                String[] parArChangeName = request.getParameterValues("changeName");
                if (parArChangeName != null) {
                    changeName = parArChangeName[0];
                }
                
                appPath = ds.patchOutsideProject;
                if (appPath.indexOf(File.separator) == 0) {
                    appPath = "/usr/local/";
                }
                
                String fileName = "";
                pathImg = "img_app/" + ds.schema + "/";
                resultPath = appPath + pathImg;
                try {
                    List<Part> fileParts;
                    fileParts = request.getParts().stream().filter(part -> part.getName().indexOf("file_") == 0).collect(Collectors.toList());
                    for (Part filePart : fileParts) {
                        fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                        InputStream inputStream = filePart.getInputStream();
                        if (fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase().equals("zip")) {
                            String res = unZip(inputStream, resultPath);
                            if (res.length() > 0) {
                                sendError(response, res);
                            } else {
                                sendResultOk(response);
                            }
                        } else {
                            byte[] buffer = new byte[1000];
                            if (changeName.equals("true")) {
                                fileName = nameTable + "_" + nameField + "_" + System.currentTimeMillis() + "." + fileExt;
                            }
                            createDir(resultPath);
                            FileOutputStream outputStream = new FileOutputStream(resultPath + fileName);
                            while (inputStream.available() > 0) {
                                int count = inputStream.read(buffer);
                                outputStream.write(buffer, 0, count);
                            }
                            inputStream.close();
                            outputStream.close();
                        }
                    }
                    String resR = "{\"img\":\"" + pathImg + fileName + "\"}";
                    sendResult(response, resR);
                } catch (IOException ex) {
                    System.out.println("UploadFile error: "+ex);
                } catch (ServletException ex) {
                    System.out.println("UploadFile error: "+ex);
                }
                break;
            case "/images/list":
                String nameDir;
                String[] parArFolder = request.getParameterValues("dir");
                if (parArFolder != null) {
                    nameDir = parArFolder[0];
                } else {
                    nameDir = "";
                }
                appPath = ds.patchOutsideProject;
                if (appPath.indexOf(File.separator) == 0) {
                    appPath = "/usr/local/";
                }
                pathImg = "img_app/" + ds.schema + "/" + nameDir;
                resultPath = appPath + pathImg;
                File myFolder = new File(resultPath);
                File[] files = myFolder.listFiles();
                if (files == null) {
                    sendResult(response, "[]");
                } else {
                    int ik = files.length;
                    ResultCommander rc = new ResultCommander();
                    rc.dir = appPath;
                    List<FileParam> lf = new ArrayList();
                    for (int i = 0; i < ik; i++) {
                        File f = files[i];
                        FileParam fp = new FileParam();
                        fp.name = f.getName();
                        if (f.isDirectory()) {
                            fp.type = 1;
                        } else {
                            fp.type = 0;
                        }
                        fp.size = f.length();
                        fp.date = timeCreateFile(f);
                        lf.add(fp);
                    }
                    Collections.sort(lf, new FileComparator());
                    rc.list = lf;
                    sendResult(response, gson.toJson(rc));
                }
                break;
        }
    }
    
    private long timeCreateFile(File f) {
        BasicFileAttributes attributes = null;
        try {
            attributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            System.out.println("Exception handled when trying to get file " +
                    "attributes: " + e.getMessage());
            return 0;
        }
        return attributes.creationTime().to(TimeUnit.MILLISECONDS);
    }

    private String unZip(InputStream is, String dir) {
        try(ZipInputStream zin = new ZipInputStream(is)) {
            ZipEntry entry;
            String name;
            while((entry=zin.getNextEntry())!=null){
                name = entry.getName();
                if (entry.isDirectory()) {
                    createDir(dir + name);
                } else {
                    FileOutputStream fout = new FileOutputStream(dir + name);
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }
                    fout.flush();
                    zin.closeEntry();
                    fout.close();
                }
            }
            return "";
        } catch(Exception ex){
            System.out.println("UploadFile unzip image error=" + ex.getMessage());
            return "Incorrect file names in the archive";
        } 
    }
    
    @Override
    public int needToLogin() {
        return 0;
    }
    
    private class FileComparator implements Comparator<FileParam> {
        public int compare(FileParam o1, FileParam o2) {
            if (o1.type == o2.type) {
                return o1.name.compareTo(o2.name);
            } else if (o1.type > o2.type) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}

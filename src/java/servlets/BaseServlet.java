package servlets;

import com.google.gson.Gson;
import db.BaseDB;
import entity.DataServlet;
import entity.ErrorMsg;
import entity.ItemChange;
import entity.ResultOk;
import entity.TokenUser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class BaseServlet extends HttpServlet{
    
    public Gson gson = new Gson();
    public BaseDB baseDb;
    public boolean isSerwer;
    
    protected abstract void processRequest(HttpServletRequest request, 
            HttpServletResponse response, DataServlet ds);

    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        baseDb = new BaseDB(request);
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        DataServlet ds = new DataServlet();
        TokenUser tu;
        ds.schema = request.getHeader("schemDB");
        ds.query = request.getRequestURI().substring(request.getContextPath().length());
        String[] ar = (" " + ds.query).split("/");
        ds.schema = ar[2];
        ds.patchOutsideProject = getPatchOutsideProject(request);
System.out.println("query="+ds.query+" schema="+ds.schema);
        int need = needToLogin();
        if (need == 0) {
            processRequest(request, response, ds);
        } else {
            ds.token = request.getHeader("Auth-token");
            if (ds.token == null) {
                String[] stPar = request.getParameterValues("Auth-token");
                if (stPar != null) {
                    ds.token = stPar[0];
                }
            }
            if (ds.token != null && ds.token.length() > 0) {
                tu = baseDb.getUserByToken(ds.token, ds.schema);
                ds.userId = tu.userId;
//                ds.userResurseInd = tu.userResurseInd;
                if (need == 1) {
                    processRequest(request, response, ds);
                } else {
                    if (ds.userId < 0) {
                        ErrorMsg err = new ErrorMsg();
                        err.message = "Need to log in";
                        sendResult(response, gson.toJson(err), HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        processRequest(request, response, ds);
                    }
                }
            } else {
                if (need == 1) {
                    ds.userId = -1;
                    processRequest(request, response, ds);
                } else {
                    ErrorMsg err = new ErrorMsg();
                    err.message = "Need to log in";
                    sendResult(response, gson.toJson(err), HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
    }

    public String getPatchOutsideProject(HttpServletRequest request) {
        String st = request.getServletContext().getRealPath("");
        if (st.indexOf(File.separator) != 0) {
            isSerwer = false;
            return st + File.separator;
        } else {
            isSerwer = true;
            int i = st.lastIndexOf(File.separator) + 1;
            return st.substring(0, i);
            
        }
    }
    
// 0 - не перевіряти, 
// 1 - якщо є token то визначити userId інакше userId = -1 дії на розсуд основного сервлету; 
// 2 - обовязкова реєстрація
    public int needToLogin() {
        return 0;
    }
    
    public String getParameter(HttpServletRequest request, String name) {
        String[] par = request.getParameterValues(name);
        if (par == null) {
            return null;
        } else {
            return par[0];
        }
    }
    
    public void sendResult(HttpServletResponse response, String result, int status) {
        response.setStatus (status);
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(result);
        } catch (IOException ex) {
            System.out.println("BaseServlet sendResult error" + ex);
        }
    }
    
    public void sendResult(HttpServletResponse response, String result) {
        sendResult(response, result, HttpServletResponse.SC_OK);
    }
    
    public void sendResultOk(HttpServletResponse response) {
        ResultOk resOk = new ResultOk();
        resOk.result = "oK";
        sendResult(response, gson.toJson(resOk), HttpServletResponse.SC_OK);
    }
    
    public void sendError(HttpServletResponse response, String message) {
        ErrorMsg err = new ErrorMsg();
        err.status = "error";
        err.title = "Ошибка";
        err.message = message;
        sendResult(response, gson.toJson(err), HttpServletResponse.SC_BAD_REQUEST);   // 404
    }
    
    public String getStringRequest(HttpServletRequest request) throws IOException {
        StringBuffer jb = new StringBuffer();
        String line = null;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            jb.append(line);
        }
        return jb.toString();
    }
    
    public void sendHTML(String nameFile, PrintWriter out) {
        try{
            FileInputStream fstream = new FileInputStream(getServletContext().getRealPath("/") + nameFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null){
                out.println(strLine);
            }
        }catch (IOException e){
           System.out.println("Ошибка вывода " + nameFile);
        }
    }
    
    public void sendHTML(String nameFile, PrintWriter out, String param) {
        try{
            FileInputStream fstream = new FileInputStream(getServletContext().getRealPath("/") + nameFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null){
                if (strLine.equals("//param")) {
                    out.println(param);
                } else {
                    out.println(strLine);
                }
            }
        }catch (IOException e){
           System.out.println("Ошибка вывода " + nameFile);
        }
    }
    
    public void setFileAndroid(String pathIn, String pathOut, ItemChange[] arCh) {
        try {
            FileReader reader = new FileReader(pathIn);
            FileWriter writer = new FileWriter(pathOut, false);
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                int i = line.indexOf("#");
                if (i < 0) {
                    writer.write(line + "\n");
                } else {
                    int i1 = line.indexOf("#", i + 1);
                    String par = line.substring(i, i1 + 1);
                    for (ItemChange ic : arCh) {
                        if (ic.name.equals(par)) {
                            writer.write(line.replace(ic.name, ic.value) + "\n");
                            break;
                        }
                    }
                }
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(BaseServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String createRandomStr(int len) {
        String alf = "ABCDEFGHIJKLMNOPQRSTUVWXYZqwertyuiopasdfghjklzxcvbnm1234567890_";
        Random random = new Random();
        String st = "" + alf.charAt(random.nextInt(50));
        for (int i = 0; i < len - 1; i++) {
            int j = random.nextInt(62);
            st+= alf.charAt(j);
        }
        return st;
    }
    
    public String lowerCaseRandom(int len) {
        String alf = "qwertyuiopasdfghjklzxcvbnm1234567890_";
        Random random = new Random();
        String st = "" + alf.charAt(random.nextInt(26));
        for (int i = 0; i < len - 1; i++) {
            int j = random.nextInt(37);
            st+= alf.charAt(j);
        }
        return st;
    }
    
    public String inDoubleQuotes(String par) {
        return "\"" + par + "\"";
    }
    
    public void formDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            clearFolder(file);
        } else {
            createDir(path);
        }
    }
    
    public void createDir(String dir) {
        File dirF = new File(dir);
        if( ! dirF.exists()) {             
            if( ! dirF.mkdirs()) {                 
                System.out.println("createDir Каталог " + dirF.getAbsolutePath() + " создвть не удалось.");
            }
        }
    }
    
    public void deleteDir(String dir) {
//System.out.println("AppDePro deleteDir dir="+dir+"<<");
        File dirF = new File(dir);
        if( dirF.exists()) {
            if(dirF.isDirectory()) {
                clearFolder(dirF);
            }
            dirF.delete();
        }
    }
    
    public void deleteFile(String nf) {
        File dirF = new File(nf);
        if( dirF.exists() && ! dirF.isDirectory()) {
            dirF.delete();
        }
    }
    
    public static void clearFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { 
            for(File f: files) {
                if(f.isDirectory()) {
                    clearFolder(f);
                    f.delete();
                } else {
                    f.delete();
                }
            }
        }
    }
    
    public static void copyDir(String srcFolder, String destFolder) {
        File src = new File(srcFolder);
        File dest = new File(destFolder);
        if(!src.exists()){
           System.out.println("Does not exist directory " + srcFolder);
           return;
        }
        try {
            copyFolder(src, dest);
        } catch (IOException ex) {
            System.out.println("createBaseProject copyFolder: " + ex);
        }
    }
    
    public static void copyFolder(File src, File dest) throws IOException{
        if(src.isDirectory()){
           //if directory not exists, create it
            if(!dest.exists()){
               dest.mkdir();
//               System.out.println("Directory copied from " + src + "  to " + dest);
            }
           //list all the directory contents
            String files[]= src.list();
            for (String file : files) {
              //construct the src and dest file structure
               File srcFile = new File(src, file);
               File destFile = new File(dest, file);
              //recursive copy
               copyFolder(srcFile,destFile);
            }
        }else{
           //if file, then copy it
           //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[]buffer = new byte[1024];

            int length;
           //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
               out.write(buffer, 0, length);
            }
            in.close();
            out.close();
//            System.out.println("File copied from " + src + " to " + dest);
        }
    }
    
    public void copyFile(String srcPuth, String destPuth) {
        File src = new File(srcPuth);
        File dest = new File(destPuth);
        if(!src.exists()){
           System.out.println("Does not exist directory " + srcPuth);
           return;
        }
        InputStream in;
        try {
            in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[]buffer = new byte[1024];

            int length;
                //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            System.out.println("copyFile: " + ex);
        } catch (IOException ex) {
            System.out.println("copyFile: " + ex);
        }
    }
    
    public String escapingQuotes(String stQ) {
        int i;
        String quote = "\"";
        String stRes = "";
        int iL = stQ.length();
        int j = 0;
        String a = "\\";
        do {
            i = stQ.indexOf(quote, j);
            if (i > -1) {
                stRes += stQ.substring(j, i) + a + quote;
                j = i + 1;
            } else {
                stRes += stQ.substring(j, iL);
            }
        } while (i > 0);
        return stRes;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
//System.out.println("QQ Access-Control-Allow-Origin 0000000000000000000000000");
        response.setStatus (HttpServletResponse.SC_OK);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        response.setHeader("Access-Control-Allow-Headers", "schemDB, auth-token, Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        try (PrintWriter out = response.getWriter()) {
            out.println("ok");
        } catch (IOException ex) {
            System.out.println("BaseServlet sendResult error" + ex);
        }
    }
}

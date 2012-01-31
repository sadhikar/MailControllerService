/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailcontroller.serverapp;


import com.tcs.igrid.universal.helper.DBConnectionManager;
import com.tcs.igrid.universal.helper.DBHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;





public class UploadFiles extends HttpServlet implements javax.servlet.Servlet {

    String directoryPath = null;

    @Override
    public void init(){
        try {
            Properties storageProperty = new Properties();
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("logfile.properties");
            storageProperty.load(in);
            directoryPath = storageProperty.getProperty("attachments_directory_path");
        } catch (IOException ex) {
            Logger.getLogger(UploadFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            Connection connection =null;
            int fileId =0;
        try {

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = new ArrayList();

            String createFileIdQuery = getInitParameter("createFileIdQuery");           
            
            items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            while (iter.hasNext()) {

                FileItem item = (FileItem) iter.next();
                System.out.println(item.getName());

                String originalFileName = item.getName().substring(item.getName().lastIndexOf("/")+1);

                connection =  DBConnectionManager.getConnection();
                DBHandler dbhandler = new DBHandler(connection);

                System.out.println("query="+createFileIdQuery);
                System.out.println("filename="+originalFileName);                
                ResultSet result = dbhandler.executeAndGetKeys(createFileIdQuery,originalFileName);
                result.next();
                fileId = result.getInt(1);

                if (!item.isFormField()) {
                    int i = 0;
                    byte[] input = new byte[256];
                    
                    String fileName = directoryPath+fileId;
                    File output = new File(fileName);
                    FileOutputStream fout = new FileOutputStream(output);

                    InputStream in = item.getInputStream();
                    while ((i = in.read(input, 0, 255)) != -1) {
                        fout.write(input, 0, i);
                    }
                    
                    fout.close();
                    output = null;

                }
            }
            System.out.println("file uploaded");

            /* If upload is success then message returned is of the format :- "Success|<file_id>"
             **/
            out.write("Success|"+fileId);
            
        } catch (SQLException ex) {
            Logger.getLogger(UploadFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileUploadException ex) {
            System.out.println("again");
            Logger.getLogger(UploadFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}

package servise.ftp;
/**
 * Created by 1 on 27.02.2018.
 */

import org.apache.commons.net.ftp.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ClientFtp {

    public static void ftpConn(String hostAddress, String log, String password) throws FileNotFoundException {

        FTPClientConfig config = new FTPClientConfig ();
        int reply;
        FTPClient fClient = new FTPClient();
        FileInputStream fInput = new FileInputStream("c:\\ftconfig.ini");
        String fs = "data/sm.txt";
        try {
            fClient.connect(hostAddress);
            fClient.enterLocalPassiveMode();
            fClient.login(log, password);
            reply=fClient.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                fClient.disconnect();
                System.err.println("FTP server refused connection.");
                //System.exit(1);
            }


            String dir=fClient.printWorkingDirectory();
            System.out.println(dir);

           boolean success = fClient.changeWorkingDirectory("data");


            if(success){

                System.out.println(fClient.printWorkingDirectory());
                fClient.setFileType(FTP.BINARY_FILE_TYPE);



                FTPFile[] files = fClient.listFiles();

                System.out.print(files.toString());
                fClient.appendFile("sdsdsdsd",fInput);
                fClient.logout();
                fClient.disconnect();

            }

           // FTPFile[] files = fClient.listFiles();

           //  files.toString();


            fClient.appendFile(fs,fInput);
                fClient.logout();
                fClient.disconnect();


        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public void loadFile(String mame) {

    }

    public void writeFile(String mame) {

    }


}

package servise;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *  Вся инфа по системе
 * Created by 1 on 07.09.2017.
 */
public class InfoSystems {


public String getInfoSystems(){
    String resuil="";


    String cmd = "cmd /c systeminfo";
    Process pr = null;
    try {

        pr = Runtime.getRuntime().exec(cmd);



        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        in.readLine();//pro4itaet pustuju stoku ,no ne vivedet ejo
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }



    return  resuil;


}


}

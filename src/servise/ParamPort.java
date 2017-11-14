package servise;

import java.util.HashMap;

/**
 * Created by 1 on 30.09.2016.
 */
public class ParamPort {
    private String namePort;
    private Integer baundRate;
    private Integer dataBits;
    private Integer stopBits;
    private String parity;
    private  HashMap<String, Object> hmPoint;




    ParamPort(HashMap<String, Object> hmPoint, HashMap<String, Object> hmLocal, String portShedule) {

        Boolean bLocal = false;
           this.hmPoint=hmPoint;

        if (hmLocal != null) {
            bLocal = (Boolean) hmLocal.get("gsm_local_on");
        }

        if (bLocal) {

            namePort = (String) hmLocal.get("comm_port");
            baundRate = (Integer) hmLocal.get("baud_rate");
            dataBits = (Integer) hmLocal.get("byte_size");
            stopBits = (Integer) hmLocal.get("stop_bits");
            parity = (String) hmLocal.get("parity");

            if (portShedule != null) {
                namePort = portShedule;
            }



        } else {

            namePort = hmPoint.get("comm_port").toString();
            baundRate = (Integer) hmPoint.get("baud_rate");
            dataBits = (Integer) hmPoint.get("byte_size");
            stopBits = (Integer) hmPoint.get("stop_bits");
            parity = hmPoint.get("parity").toString();

        }


    }
    public HashMap<String, Object> getHmPoint() {
        return hmPoint;
    }

    public Integer getDataBits() {
        return dataBits;
    }

    public void setDataBits(Integer dataBits) {
        this.dataBits = dataBits;
    }

    public Integer getStopBits() {
        return stopBits;
    }

    public void setStopBits(Integer stopBits) {
        stopBits = stopBits;
    }

    public String getParity() {
        return parity;
    }

    public void setParity(String parity) {
        parity = parity;
    }


    public String getNamePort() {
        return namePort;
    }

    public void setNamePort(String namePort) {
        this.namePort = namePort;
    }

    public Integer getBaundRate() {
        return baundRate;
    }

    public void setBaundRate(Integer baundRate) {
        baundRate = baundRate;
    }


}

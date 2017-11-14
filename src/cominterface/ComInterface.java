/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cominterface;

import servise.CommandGet;
import servise.ParamPort;

import java.util.List;

/**
 *
 * @author 1
 */
public interface ComInterface {


    public String getPortName();

    void closePort() throws Exception;

    void  purgePort() throws Exception;

    boolean isDSR() throws Exception;

    boolean isSTS() throws Exception;

    void setDTR(boolean flag) throws Exception;

    void setRTS(boolean flag) throws Exception;

    public void writePack(List<Integer> list) throws Exception ;

    public void write(CommandGet cmd) throws Exception;

    boolean sendBreak(int duration) throws Exception;

     public  String[] getPortNames();

    boolean openPort(ParamPort paramPort) throws Exception;

    boolean isOpened();

    boolean isRLSD() throws Exception;

}

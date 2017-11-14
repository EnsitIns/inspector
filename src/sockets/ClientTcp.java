/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sockets;

import net.sf.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 1
 */
public class ClientTcp implements Runnable {

    public static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger("LogServer");
    public static int PORT;
    public static InetAddress IA;
    Socket socket;
    // Тип команды
    int kommand;
    //Отправляемые данные
    byte[] msg;
    // Номер отправленого пакета
    int numPack;
    InputStream is = null;
    OutputStream os = null;
    ArrayList<Object> alAnsver;
    ArrayList<ParMsg> alSend;
    private final ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);

    public ClientTcp(InetAddress ia, int port) throws IOException {

        socket = new Socket(ia, port);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        alSend = new ArrayList<ParMsg>();

    }

    public ClientTcp(int timeout) throws IOException {

        socket = new Socket(IA, PORT);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        alSend = new ArrayList<ParMsg>();
        socket.setSoTimeout(timeout);
    }

    public ClientTcp(InetAddress ia, int port, int timeout) throws IOException {
        socket = new Socket(ia, port);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        socket.setSoTimeout(timeout);

    }

    public void setSoTimeout(int i) throws SocketException {

        socket.setSoTimeout(i);

    }

    public Integer getTypeCmd() {

        Integer typ = (Integer) alAnsver.get(2);

        return typ;
    }

    public Integer getNumPacket() {

        Integer num = (Integer) alAnsver.get(1);

        return num;
    }

    public String getResultString() {

        byte[] bs = (byte[]) alAnsver.get(0);

        if (bs != null) {
            return new String(bs);
        } else {

            return "";
        }
    }

    public byte[] getResult() {

        return (byte[]) alAnsver.get(0);

    }

    public void addMsg(int num, int typ, byte[] msg) {

        ParMsg parMsg = new ParMsg(num, typ, msg);

        alSend.add(parMsg);

    }

    @SuppressWarnings("null")
    public Object sendScript(int idScript, HashMap<String, Object> param, int wait) throws Exception {

        Object result = null;

        JSONObject jSONObject;

  
        param.put("#ID", idScript);
        
        jSONObject = JSONObject.fromObject(param);

        String jSon = jSONObject.toString();

        socket.setSoTimeout(wait);

        byte[] bs = jSon.getBytes();
        buffer.put(bs);
        buffer.flip();
        os.write(buffer.array(), 0, buffer.limit());

        buffer.clear();
        long time = System.currentTimeMillis();

        boolean bread = true;

        String sAnswer = "";

        while (time + wait > System.currentTimeMillis() || bread) {

            int len = is.available();

            if (len > 0) {

              //  System.out.println("Размер данных-" + len);

//buffer.clear();
                byte[] ansver = new byte[len];

                is.read(ansver);

                sAnswer = sAnswer + new String(ansver);

//is.read(buffer.array());
                //  buffer.flip();
                //  byte[] ba = buffer.array();
                //if (ba.length > 0) {
                //  try {
                //String sAnswer = new String(ba);
                JSONObject jsono = null;

                try {

                    jsono = JSONObject.fromObject(sAnswer);
                    bread = false;

                } catch (Exception ex) {

                    continue;
                }

             
                HashMap<String, Object> hmParam;
                hmParam = (HashMap<String, Object>) JSONObject.toBean((JSONObject) jsono, Map.class);
           
                
                  if (hmParam.containsKey("error")) {
                    String sError = (String) hmParam.get("error");
                    JOptionPane.showMessageDialog(null, sError, "Сообщение сервера", JOptionPane.ERROR_MESSAGE);
                    throw new Exception(sError);
                }

                
                
                if (hmParam != null && hmParam.containsKey("message")) {

                    String sMessage = (String) hmParam.get("message");
                    JOptionPane.showMessageDialog(null, sMessage, "Сообщение сервера", JOptionPane.INFORMATION_MESSAGE);

                }

                os.close();
                is.close();
                socket.close();

                return hmParam;

//  } finally {
                //    os.close();
                //  is.close();
                // socket.close();
                // }
                // }
            }
        }

        os.close();
        is.close();
        socket.close();

        throw new Exception("Закончилось время ожидания ответа сервера !");

    }

   
    public Object sendMsg() {

        boolean done = false;

        ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);

        int pozNext = 0;

        for (ParMsg parMsg : alSend) {

            //  ArrayList<byte[]> bsSend = MathTrans.createSendMsg(parMsg.getNum(), parMsg.getTyp(), parMsg.getMsg());
            byte[] bs = null; 
                    
                    //= MathTrans.getAllMsg(parMsg.getNum(), parMsg.getTyp(), parMsg.getMsg());

            try {

                buffer.put(bs);

                //  pozNext=pozNext+bs.length;
                //  buffer.position(pozNext);
                buffer.flip();

                try {
                    // for (byte[] bs : bsSend) {
                    os.write(buffer.array(), 0, buffer.limit());
                    // }

                } catch (IOException ex) {
                    Logger.getLogger(ClientTcp.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (Exception e) {
                System.err.println(e);

            }

            //    pozNext = pozNext + buffer.position();
            //  buffer.flip();
        }

        buffer.flip();

        try {
            // for (byte[] bs : bsSend) {
            os.write(buffer.array(), 0, buffer.limit());
            // }

        } catch (IOException ex) {
            Logger.getLogger(ClientTcp.class.getName()).log(Level.SEVERE, null, ex);
        }

        //   PrintWriter writer = new PrintWriter(os, true);
        // String msg = new String(crc) + new String(cap);
        // writer.print(msg);
        Scanner scanner = new Scanner(is);

        String answer = "";

        while (!done && scanner.hasNextLine()) {
            String line = scanner.nextLine();
            answer = answer + line;
            if (answer.endsWith(ServerTcp.END_OK)) {
                done = true;

                break;
            }
        }
        return answer;
    }

    class Answer extends Observable implements Runnable {

        private void start() {
        }

        @Override
        public void run() {
        }
    }

    class ParMsg {

        private int typ;
        private int num;
        private byte[] msg;

        public ParMsg(int num, int typ, byte[] msg) {

            this.typ = typ;
            this.num = num;
            this.msg = msg;

        }

        public byte[] getMsg() {
            return msg;
        }

        public int getNum() {
            return num;
        }

        public int getTyp() {
            return typ;
        }
    }

    public Object sendMsg(String msg) {

        String answer = "";

        ArrayList<Byte> alByte;

        try {

            PrintWriter writer = new PrintWriter(os, true);
            Scanner scanner = new Scanner(is);

            writer.println(msg + ServerTcp.END_OK);

            boolean done = false;

            try {

                if (msg.contains("caesar")) {

                    alByte = new ArrayList<Byte>();

                    int i;
                    while ((i = is.read()) != -1) {

                        alByte.add((byte) i);
                    }

                    Byte[] b;

                    b = alByte.toArray(new Byte[alByte.size()]);

                    return b;

                } else {

                    while (!done && scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        answer = answer + line;
                        if (answer.endsWith(ServerTcp.END_OK)) {
                            done = true;

                            return answer;
                        }

                    }

                }

            } finally {
                is.close();
                os.close();
            }

        } catch (IOException ex) {

            LOGGER.error(ex.getMessage(), ex);
        }

        return answer;
    }

    @Override
    public void run() {
        sendMsg();
    }
}

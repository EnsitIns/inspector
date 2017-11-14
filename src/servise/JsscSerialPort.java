/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 1
 */
public class JsscSerialPort extends Port implements cominterface.ComInterface {

    private jssc.SerialPort serialPort;
    private SerialPortReader portReader;


    public JsscSerialPort(BitSet bitSetFlags, Observer observer) {
        this.bitSetFlags = bitSetFlags;
        this.addObserver(observer);
        hmPack = new LinkedHashMap<>();
        currentPack = 0;
        alSend = new LinkedList<>();
        capacity = 4000;
        threadBlock = new ThreadBlock();
        lbqResult = new LinkedBlockingQueue<>();

    }

    public JsscSerialPort() {
    }


    @Override
    public boolean openPort(ParamPort paramPort) throws Exception {

        Integer baundRate;
        Integer dataBits;
        Integer stopBits;
        Integer parity;
        bStop = false;

        //  if (serialPort != null && serialPort.isOpened()) {

//            serialPort.setDTR(false);
        //          serialPort.closePort();
        //    }


        this.hmPoint = paramPort.getHmPoint();

        // ParamPort paramPort = getParamPort(hmPoint, hmLocal, portShedule);

        String sParity;

        sParity = paramPort.getParity();
        parity = jssc.SerialPort.PARITY_NONE;

        if (sParity.equals("Нет")) {
            parity = jssc.SerialPort.PARITY_NONE;
        } else if (sParity.equals("Чет")) {
            parity = jssc.SerialPort.PARITY_EVEN;
        } else if (sParity.equals("Нечет")) {
            parity = jssc.SerialPort.PARITY_ODD;
        } else if (sParity.equals("Маркет(1)")) {
            parity = jssc.SerialPort.PARITY_MARK;
        } else if (sParity.equals("Пробел(0)")) {
            parity = jssc.SerialPort.PARITY_SPACE;
        }


        if (serialPort != null && serialPort.isOpened()) {

            serialPort.setDTR(false);
            serialPort.setRTS(false);
            serialPort.closePort();
        }

        commPortName = paramPort.getNamePort();

        serialPort = new jssc.SerialPort(commPortName);

        baundRate = paramPort.getBaundRate();
        dataBits = paramPort.getDataBits();
        stopBits = paramPort.getStopBits();


        if (!serialPort.isOpened()) {


           try {
               serialPort.openPort();
           }catch (SerialPortException e){

               return false;

           }

            serialPort.setParams(baundRate, dataBits, stopBits, parity);

            serialPort.setDTR(false);

            if (oldNamePort != null && !oldNamePort.equals(serialPort.getPortName())) {

                bitSetFlags.clear(ValuesByChannel.BSF_DTR_NO);
                bitSetFlags.clear(ValuesByChannel.BSF_DTR_YES);
            }

            //if (hmPack != null) {
            //  hmPack.clear();
            // }

            setEvents();

            return true;

        }
        return true;
    }


    public void setEvents() throws SerialPortException {

        //Готовим маску, на основании неё мы будем получать сообщения о событиях, которые произошли. Ну например,
//нам необходимо знать что пришли какие-то данные, т.о. в маске должна присутствовать следующая величина: MASK_RXCHAR.
// Если нам, например, ещё нужно знать об изменении состояния линий CTS и DSR, то маска уже будет
//выглядеть так: SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR
        int mask = SerialPort.MASK_RING + SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_BREAK;//Prepare mask
        //           serialPort.setEventsMask(mask);//Set mask

        //     int mask = SerialPort.MASK_RXCHAR;
        //Выставляем подготовленную маску
        serialPort.setEventsMask(mask);
//Добавляем собственно интерфейс через который мы и будем узнавать о нужных нам событиях

        serialPort.purgePort(SerialPort.MASK_RXCHAR);

        portReader = new SerialPortReader();

        serialPort.addEventListener(portReader);

    }


    @Override
    public void writePack(List<Integer> list) throws Exception {

        byte[] bs = new byte[list.size()];
        for (int i = 0; i < bs.length; i++) {

            int jy = list.get(i);
            bs[i] = (byte) jy;
        }

        serialPort.writeBytes(bs);

    }

    @Override
    public void write(CommandGet commandGet) throws Exception {

        List<Integer> list = commandGet.alSend;
        int[] bs = new int[list.size()];
        for (int i = 0; i < bs.length; i++) {

            int jy = list.get(i);
            bs[i] = jy;
        }
        try {
            Thread.sleep(commandGet.sleepTime);
        } catch (InterruptedException ex) {
            setNotifyObservers(ex);
        }
        serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);


        serialPort.writeIntArray(bs);


    }

    @Override
    public String getPortName() {
        return serialPort.getPortName();
    }


    @Override
    public boolean isSTS() throws SerialPortException {
        return serialPort.isCTS();
    }

    @Override
    public void setDTR(boolean flag) throws SerialPortException {
        serialPort.setDTR(flag);
    }

    @Override
    public void closePort() throws SerialPortException {

        if (serialPort.isOpened()) {

            serialPort.closePort();
        }

    }

    @Override
    public boolean isOpened() {
        return serialPort.isOpened();
    }


    @Override
    public boolean isRLSD() throws SerialPortException {
        return serialPort.isRLSD();
    }

    // public boolean isEmpty() {
    //   return hmPack.isEmpty();
    // }


    @Override
    public void setRTS(boolean flag) throws Exception {
        serialPort.setRTS(flag);
    }

    @Override
    public String[] getPortNames() {

        return SerialPortList.getPortNames();
    }

    @Override
    public boolean sendBreak(int duration) throws Exception {
        return serialPort.sendBreak(duration);
    }


    @Override
    public boolean isDSR() throws Exception {
        return serialPort.isDSR();
    }

    @Override
    public boolean isStop() {
        return bStop;
    }

    @Override
    public void setComPack(CommandGet comPack) {
        this.comPack = comPack;
        //hmPack = new LinkedHashMap<>();

        if (this.comPack != null) {
            this.comPack.alSend = new LinkedList<Integer>();
        }
    }

    @Override
    public void purgePort() throws Exception {
        serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);

    }


    public class SerialWriter {

        private List<Integer> alSend;
        private int sleepTime;
        private ArrayList<List<Integer>> listWrite;

        public SerialWriter() {
        }

        public SerialWriter(List<Integer> listSend, int sleepTime) {

            this.sleepTime = sleepTime;
            this.alSend = listSend;

        }

        //Пакетная передача
        public SerialWriter(ArrayList<List<Integer>> listWrite, int sleepTime) {

            this.sleepTime = sleepTime;
            this.listWrite = listWrite;

        }

        public void begin() throws SerialPortException {

            if (listWrite != null) {

                for (List<Integer> is : listWrite) {

                    alSend = is;

                    write();
                }

            } else {

                write();

            }

        }

        private void write() throws SerialPortException {

            int[] bs = new int[alSend.size()];
            for (int i = 0; i < bs.length; i++) {

                int jy = alSend.get(i);
                bs[i] = jy;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                setNotifyObservers(ex);
            }
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);


            serialPort.writeIntArray(bs);

        }


        private void write(List<Integer> list) throws SerialPortException {

            int[] bs = new int[list.size()];
            for (int i = 0; i < bs.length; i++) {

                int jy = alSend.get(i);
                bs[i] = jy;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                setNotifyObservers(ex);
            }
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXABORT);


            serialPort.writeIntArray(bs);

        }


        public void writePack(List<Integer> list) throws SerialPortException {

            byte[] bs = new byte[list.size()];
            for (int i = 0; i < bs.length; i++) {

                int jy = list.get(i);
                bs[i] = (byte) jy;
            }

            serialPort.writeBytes(bs);

        }

    }

    class SerialPortReader implements SerialPortEventListener {

        //    private LinkedList<Integer> alByte;
        //  protected  LinkedBlockingQueue<Integer> lbqResult;

        //  public  LinkedHashMap<Integer, CommandGet> hmPack;
        // private LinkedList<Integer> alByte;
        //    private Thread thread;
        //  private List<Integer> lCheck;
        //   private int delay; // Время ожидания пакета
        private List<Integer> alAnswer;
        private boolean OnlyOn; // Одиночная команда при буферизации
        private String typPack;

        public SerialPortReader() {

            //    alByte = new LinkedList<>();
            //  lbqResult = new LinkedBlockingQueue<>();
            //   hmPack = new LinkedHashMap<>();


        }


        public List<Integer> getAnswer() {
            return alAnswer;
        }


        @Override
        public void serialEvent(jssc.SerialPortEvent event) {

            int iTyp = event.getEventType();

            if (iTyp != 1) {
                System.out.println(iTyp);
            }
            if (event.isCTS()) {

                setNotifyObservers(iTyp);
            }

            if (event.isDSR()) {

                setNotifyObservers(iTyp);
            }

            if (event.isBREAK()) {
                //Остановка запроса

                setNotifyObservers(iTyp);
                //this.typRegime = REJIM_GET_DIAL;
                // executeProcess();

            }

            if (iTyp == jssc.SerialPortEvent.RING) {
                //Дозвон контроллера

                setNotifyObservers(iTyp);
                //this.typRegime = REJIM_GET_DIAL;
                // executeProcess();

            }

            if (event.isRXCHAR()) {
                try {
                    int intbuffer[] = serialPort.readIntArray(event.getEventValue());

                    //  setNotifyObservers(buffer);
                    for (int i = 0; i < intbuffer.length; i++) {

                        int b = intbuffer[i];
                        // Integer s = (Integer) ((b < 0) ? b + 256 : b);
                        //  alByte.add(b);

                        lbqResult.add(b);
                    }

                    if (bitSetFlags.get(ValuesByChannel.BSF_GPRS_GSM_DIAL)) {

                        // Дозвон контроллера

                        setNotifyObservers(new LinkedList(lbqResult));
                    }

                    // Если не пакетный режим
                    if (!bitSetFlags.get(ValuesByChannel.BSF_BUFER_PACK)) {
                        checkOnly();
                    }

                } catch (Exception ex) {
                    channel.setLoggerInfo("Ответ", ex);

                    // channel.checkEnd();
                }

            }

        }


    }
}

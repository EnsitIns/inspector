package servise;

import cominterface.ComInterface;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static constatant_static.SettingActions.esStatus.*;
import static servise.ValuesByChannel.BLACK_COUNT;
import static servise.ValuesByChannel.BSF_TRANS_PROTOKOL;
import static servise.ValuesByChannel.B_LOG_TXD;

/**
 * Родительский класс ком порта
 */
public class Port extends Observable {
    public CommandGet commandGet;
    String script;
    // ValuesByChannel channel;
    HashMap<String, Object> mapValid;
    private boolean pack;

    // буфер посланых байт
    public LinkedList<Integer> alSend;

    public BitSet bitSetFlags;
    public String commPortName;
    public String oldNamePort;
    public HashMap<Integer, Integer> hmRepeat;

    public HashMap<String, Object> hmPoint;
    public ValuesByChannel channel;
    String sok;
    int minLen;
    public boolean Pack;

    Integer currentIdPribor; // текущий id

    //Команда со скриптами пакетного режима
    public CommandGet comPack;
    public ThreadBlock threadBlock;
    // команды посланные в пакетном режиме
    public LinkedHashMap<Integer, CommandGet> hmPack;
    public String typPack;
    public LinkedBlockingQueue<Integer> lbqResult;

    // Номер текущего пакета
    public Integer currentPack;
    public int capacity;

    public CommandGet currentCmd;
    public boolean bStop;
    //   public ValidResult valid;
    public boolean bOk; // если true то все команды посланы в порт
    public boolean bAdd; // есть команды  на посылку в порт
    // private WatchTask watchTask;
    private int delay; // Время ожидания пакета
    private List<Integer> alAnswer;


    public class CheckBuffer implements Runnable {

        @Override
        public void run() {

            try {
                checkBuffer();
            } catch (Exception ex) {
                channel.setLoggerInfo(ex.getMessage(), ex);
            }
        }

    }


    public void stop() {
        bStop = true;

    }

    public void start() {
        bStop = false;
    }


    public boolean doSendPack(CommandGet cmd, boolean bRepeat) throws Exception {


        currentCmd = cmd;

        setPack(true);


        cmd.alHelp = cmd.alSend;

        // Заворачиваем в транспортный протокол Меркурий 228

        //Если не повтор
        if (!bRepeat) {
            cmd.alSend = toProtocol(cmd);
        }

        if (B_LOG_TXD) {

            channel.setLoggerInfo("Заворачиваем в транспортный протокол Меркурий 228", null);
            channel.setLoggerInfo("TxD [" + MathTrans.getNexStrByList(cmd.alSend, " ") + "]", null);
        }

//        }

        /**
         * режим буферизации для неодиночных команд
         */
        //   if (bitSetFlags.get(ValuesByChannel.BSF_BUFER_PACK) && !cmd.onlyOne) {

        //   setPack(true);
        //   alCommands.add(cmd);

        if (B_LOG_TXD) {

            channel.setLoggerInfo("Задействан режим буферизации", null);
        }

        // Пока не послали ни одной команды
        // if (hmPack.isEmpty()) {

        //   portReader.clear();
        // }


        if (addList(cmd.alSend)) {
            hmPack.put(cmd.number, cmd);
            //SerialWriter writer = new SerialWriter();

            ((ComInterface) this).writePack(cmd.alSend);

            //writePack(cmd.alSend);


            return true;
        }
        return true;


        // }

    }


    public boolean doSend(CommandGet cmd) throws Exception {

        boolean result = false;

        doPrepareSend(cmd);

        int currTry = cmd.countTry;

        //
        //
        //    portReader.clear();
        //   portReader.setThread(Thread.currentThread());

        //   portReader.setParam(cmd.Ok), cmd.lenCrc);
        while (currTry > 0) {

            if (bStop) {


                ((ComInterface) this).sendBreak(500);

                //  serialPort.sendBreak(500);
                return true;
                // break;
            }

            clearAnswer();
            cmd.errorCmd = null;
            //  SerialWriter writer = new SerialWriter(cmd.alSend, cmd.sleepTime);

//            writer.begin();

            ((ComInterface) this).write(cmd);


            // текущий поток ждет ответа...
            if (wait(cmd.waitTime)) {

                bitSetFlags.set(ValuesByChannel.BSF_GPRS_SERVER);

                // Пришел ответ
                //  cmd.alResult = portReader.getAnswer();
            } else {

                // Закончилось время ожидания
                cmd.errorCmd = "Закончилось время ожидания ответа !";

            }

            // clear();

            if (bStop) {


                result = true;

                break;
            }

            // EMPTY -для команд без ответа
            if (cmd.errorCmd != null && !cmd.Ok.equals("EMPTY")) {
                // Если ошибка...
                result = false;
                currTry--;

            } else // все хорошо !
            {
                result = true;
                break;
            }
        }

        return result;

    }


    public void setHmPoint(HashMap<String, Object> hmPoint) {
        this.hmPoint = hmPoint;
    }


    public void setNotifyObservers(Object notyfy) {
        setChanged();
        notifyObservers(notyfy);

    }

    public boolean addList(List<Integer> list) throws InterruptedException {

        bAdd = false;
        //  заполнен буфер и неодиночная команда

        while (alSend.size() + list.size() >= capacity) {

            Thread.sleep(100);

        }

        //  if (alSend.size() + list.size() >= capacity) {
        // Буфер заполнен,ждем очистки
        //Thread.currentThread().setName("bufferFull");
        // sendCmd = true; // есть команда на добавление
        //    System.out.println("Буфер заполнен,ждем очистки..." + comPack.alSend.size());
        //  bAdd = true;
        //  suspend();
        System.out.println("Можно добавлять " + alSend.size());

        //}
        for (Integer i : list) {
            alSend.add(i);

        }

        return true;

    }

    public boolean isStop() {

        return bStop;
    }

    public void setComPack(CommandGet cmdPack) {
        this.comPack = cmdPack;
    }


    /**
     * Предварительная обработка команд перед отправкой
     *
     * @param cmd
     * @return
     * @throws Exception
     */
    public void doPrepareSend(CommandGet cmd) throws Exception {
        currentCmd = cmd;
        boolean result = false;
        //  String nameCom = getAllName(cmd);
        int currTry = cmd.countTry;

        if (currTry == 0) {
            currTry = 1;
        }

        // Дозвон по GSM (набор номера);
        if (cmd.name.equals("DialGsm")) {
            cmd.waitTime = (Integer) hmPoint.get("time_pause") * 1000;
        }

        setPack(false);

        // portReader.setThread(Thread.currentThread());
        // portReader.setOnlyOn(cmd.onlyOne);

        cmd.alHelp = cmd.alSend;

        if (channel != null && channel.typContoller != null) {

            // Заворачиваем в транспортный протокол текущего контроллера,если он есть
            channel.runScriptByController(channel.typContoller, ValuesByChannel.TS_FIND, cmd);

            if (B_LOG_TXD) {

                channel.setLoggerInfo("Текущий контроллер:" + channel.typContoller, null);
                channel.setLoggerInfo("TxD [" + MathTrans.getNexStrByList(cmd.alSend, " ") + "]", null);
            }

        }

        if (bitSetFlags.get(BSF_TRANS_PROTOKOL)) {

            // Заворачиваем в транспортный протокол Меркурий 228

            cmd.alSend = toProtocol(cmd);


            if (B_LOG_TXD) {

                channel.setLoggerInfo("Заворачиваем в транспортный протокол Меркурий 228", null);
                channel.setLoggerInfo("TxD [" + MathTrans.getNexStrByList(cmd.alSend, " ") + "]", null);
            }

        }


        if (cmd.lenCrc < 3) {
            cmd.lenCrc = 3;
        }

        String script = null;

        setCommandGet(cmd);


    }


    public void checkOnly() throws Exception {

        if (bStop) {

            ((ComInterface) this).purgePort();
            setNotifyObservers(esCheckEnd);
            return;
        }

        //протокол м228
        if (bitSetFlags.get(ValuesByChannel.BSF_TRANS_PROTOKOL)) {
            // Разворачиваем

            if (!isUndoProtocol(comPack)) {

                // не удалось развернуть
                return;

            }

            alAnswer = comPack.alResult;

            // Ждем,вдруг в буфере чтото осталось...
            Thread.sleep(100);
            clearAnswer();

        } else {

            // без протокола
            alAnswer = new LinkedList<>(lbqResult);

        }

        //  if (B_LOG_TXD) {
        //    channel.setLoggerInfo("Меркурий 228" + ": RxD [" + channel.getNextString(alAnswer) + "]", null);
        // }
        //  currentCmd.alResult = alAnswer;
        // Разворачиваем из  транспортного протокола текущего контроллера,если он есть
        if (channel != null && channel.typContoller != null) {

            //  if (B_LOG_TXD) {
            // channel.setLoggerInfo(channel.typContoller + ": RxD [" + channel.getNextString(alAnswer) + "]", null);
            // }
            //channel.runScriptByController(channel.typContoller, ValuesByChannel.TS_RESULT, currentCmd);
        }

        // Для текущего контроллера
        if (comPack != null && isAnswerInPack(alAnswer)) {

            alAnswer = comPack.alResult;
        }
        if (isValid(alAnswer)) {
            currentCmd.alResult = alAnswer;


            resume();
        }

    }


    public void setChannel(ValuesByChannel channel) {
        this.channel = channel;
    }

    public void clearAnswer() {
        lbqResult.clear();
    }


    public void resume() {

        threadBlock.resume();
    }


    public boolean wait(int millisec) {
        return threadBlock.wait(millisec);

    }


    public void startCheckBuffer(Integer idPribor) {

        bOk = true;
        currentIdPribor = idPribor;
        clearAnswer();
        //  Режим буфферизации
        CheckBuffer buffer = new CheckBuffer();
        new Thread(buffer).start();


    }


    /**
     * Удаляет байты результата
     *
     * @throws Exception
     */

    public void removeResult() throws Exception {

        int len = (int) comPack.result;
        for (int i = 0; i < len; i++) {
            lbqResult.take();
        }

        int isend = currentCmd.alSend.size();

        // удаляем для увеличения буфера посылки

        for (int i = 0; i < isend; i++) {
            alSend.removeFirst();
        }


    }

    protected boolean putRepeat(int number) {

        boolean result = false;

        Integer countRepeat = hmRepeat.get(number);

        // повторно посылаем команду 3 раза
        if (countRepeat == null || countRepeat <= BLACK_COUNT) {

            countRepeat = (countRepeat == null ? 1 : countRepeat + 1);

            hmRepeat.put(number, countRepeat);
            // doSendPack(currentCmd, true);

            result = true;

        }
        return result;

    }


    public void checkBuffer() throws Exception {

        Long starTime = System.currentTimeMillis();
        Long endTime;
        Boolean bTime = false;
        int currentSize = lbqResult.size();
        int endSize;


        Integer countRepeat;
        if (hmRepeat == null) {

            hmRepeat = new HashMap<>();
        }


        while (bOk) {


            if (bTime) {
//Запускаем ожидание ответа
                starTime = System.currentTimeMillis();
                bTime = false;
                currentSize = lbqResult.size();

            }


            endTime = System.currentTimeMillis();

            Long timeWait = endTime - starTime;

            if (timeWait >= 7000) {
                //Превышано время ожидания
                break;

            }


            if (lbqResult.isEmpty()) {
                continue;
            }

            endSize = lbqResult.size();

            if (currentSize != endSize) {

                bTime = true;
            }


            if (bStop) {

                break;
            }

            boolean bundo = isUndoProtocol(comPack);


            if (!bundo) {
                continue;
            }


            try {

                //   Если удалось вытащить из  транспортного протокола

                Thread.sleep(10);
// Сбрасываем таймер


                alAnswer = comPack.alResult;

                // Номер пакета
                int number = comPack.number;

                if (!hmPack.isEmpty() && hmPack.containsKey(number)) {

                    int lenDat = alAnswer.size();

                    currentCmd = hmPack.get(number);

                    if (lenDat == 0) {

                        // повторно посылаем команду 3 раза
                        if (putRepeat(number)) {

                            doSendPack(currentCmd, true);


                        } else {
                            // ставим как пройденную

                            //удаляем результат
                            // removeResult();
                            currentCmd = hmPack.remove(number);

                            //любая команда послана больше 3 раз, выходим...

                            if (currentCmd.criticalError) {
                                // нет смысла опрашивать

                                // явно добавляем в черный список
                                channel.addCmdInBlackList(currentIdPribor, "Прибор не доступен.", 7);

                                bOk = false;
                                setNotifyObservers(esEndGoPack);


                            }


                            // bOk = false;
                            // setNotifyObservers(esEndGoPack);
                            // break;
                        }


                    } else {
//  ================ЕСТЬ ДАННЫЕ =======================

                        // currentCmd = hmPack.get(number);
//                        currentCmd = hmPack.remove(number);
                        ArrayList<Integer> al = new ArrayList<Integer>(alAnswer);
                        currentCmd.alResult = al;

                        //   removeResult();

                        //  int len = (int) comPack.result;
                        //for (int i = 0; i < len; i++) {
                        //  lbqResult.take();
                        //}

                        //   int isend = currentCmd.alSend.size();

                        // удаляем для увеличения буфера посылки

                        // for (int i = 0; i < isend; i++) {
                        //   alSend.removeFirst();
                        // }

                        // Для текущего контроллера
                        if (channel != null && channel.typContoller != null) {

                            // Разворачиваем из  транспортного протокола текущего контроллера,если он есть
                            //   channel.runScriptByController(channel.typContoller, ValuesByChannel.TS_RESULT, currentCmd);
                            if (B_LOG_TXD) {

                                channel.setLoggerInfo("Разворачиваем...", null);
                                channel.setLoggerInfo("Текущий контроллер:" + channel.typContoller, null);
                                channel.setLoggerInfo("RxD [" + MathTrans.getNexStrByList(currentCmd.alResult, " ") + "]", null);


                            }

                        }

                        // Для текущего контроллера
                        if (comPack != null && isAnswerInPack(al)) {

                            currentCmd.alResult = comPack.alResult;

                            alAnswer = comPack.alResult;
                        }

                        setCommandGet(currentCmd);

                        if (isValid(alAnswer)) {

                            currentCmd = hmPack.remove(number);

                            channel.createResultAndRemove(currentIdPribor, currentCmd);

                            setNotifyObservers(currentCmd);

                        } else {

                            //  добавляем в черный список
                            channel.addCmdInBlackList(currentIdPribor, "Не совпадает контрольная сумма !", 7);

                            if (putRepeat(number)) {

                                doSendPack(currentCmd, true);


                            } else {

                                currentCmd = hmPack.remove(number);
                                currentCmd.sResult = "Не совпадает контрольная сумма !";
                                channel.setLoggerInfo("Не совпадает контрольная сумма !", null);

                            }

                        }

                        //Данные


                    }

                    removeResult();
                }


                if (hmPack.isEmpty()) {

                    alSend.clear();
                    bOk = false;
                    hmRepeat.clear();
                    setNotifyObservers(esEndGoPack);
                    return;

                    //Выходим...


                }


            } catch (Exception e) {
                bOk = false;
                channel.setLoggerInfo("Ответ", e);
                //  timer.cancel();
                setNotifyObservers(esEndGoPack);
                break;
            }


        }

        hmRepeat.clear();

        setNotifyObservers(esEndGoPack);

    }

    /**
     * Развернуть из транспртного протокола
     *
     * @param cmd
     */
    public Boolean isUndoProtocol(CommandGet cmd) throws Exception {

        Boolean result = false;

        if (lbqResult.size() < 9) {

            return result;
        }

        LinkedList<Integer> values = new LinkedList<>(lbqResult);
        // Проверяем  размер данных
        List l = values.subList(5, 7);
        int iLen = MathTrans.getIntByList(l, MathTrans.B_LITTLE_ENDIAN, false);

        if (iLen + 9 > values.size()) {
            return result;
        }

        // Номер пакета
        List np = values.subList(3, 5);
        int inp = MathTrans.getIntByList(np, MathTrans.B_LITTLE_ENDIAN, false);

        cmd.number = inp;

        // Проверка контрольной суммы пакета
        List<Integer> lcrc = values.subList(3, 8);

        int[] ses = null;

        ses = MathTrans.getCRC24Tab(lcrc);

        int a0 = (int) values.get(0);
        int a1 = (int) values.get(1);
        int a2 = (int) values.get(2);

        int b0 = ses[2];
        int b1 = ses[1];
        int b2 = ses[0];

        if (a0 == b0 & a1 == b1 & a2 == b2) {

            if (iLen + 9 > values.size()) {
                return result;
            }

            //Полная длина пакета
            cmd.result = iLen + 9;

            // Вытаскиваем данные
            LinkedList r = new LinkedList<Integer>();
            r.addAll(values.subList(8, 8 + iLen));

            cmd.alResult = r;

            return true;

        } else {

            return result;

        }

    }

    protected boolean isPack() {
        return Pack;
    }

    protected void setPack(boolean bPack) {
        this.Pack = bPack;
    }


    protected void setCommandGet(CommandGet commandGet) {
        this.commandGet = commandGet;
        sok = commandGet.Ok;
        minLen = commandGet.lenCrc;
        script = (String) commandGet.getProperty("c_measure");
    }


    /**
     * Завернуть в транспортный протокол
     *
     * @param cmd
     * @return завернутый в массив протокол
     */
    public LinkedList<Integer> toProtocol(CommandGet cmd) {

// Заворот в транспортный протокол
        List<Integer> send = cmd.alSend;
        int port = 1;
        // для контроллера порт=0
        if (cmd.name.equals("SetParamPort228")) {
            port = 0;
        }

        int num = cmd.number;

        int[] crc = new int[3];
        int[] cap = new int[5];
        int[] sum = new int[1];
        int checksum = 0;

        LinkedList<Integer> listResult = new LinkedList<>();

        if (send != null) {
            checksum = MathTrans.getIntCheckSum(send);
            sum[0] = checksum;
        } else {
            //send = new int[0];
            sum = null;
        }

        int[] bNum = MathTrans.intToByteArray(num, 2);
        cap[0] = bNum[1];
        cap[1] = bNum[0];
        int[] bLen = MathTrans.intToByteArray(send.size(), 2);
        cap[2] = bLen[1];
        cap[3] = bLen[0];
        cap[4] = port;
        long l = MathTrans.getCRC24Tab(cap);
        int[] crcr = MathTrans.longToIntArray(l);
        crc[0] = crcr[2];
        crc[1] = crcr[1];
        crc[2] = crcr[0];

        //crc24
        listResult.add(crc[0]);
        listResult.add(crc[1]);
        listResult.add(crc[2]);

        // заголовок
        listResult.add(cap[0]);
        listResult.add(cap[1]);
        listResult.add(cap[2]);
        listResult.add(cap[3]);
        listResult.add(cap[4]);

        int sendLen = send.size();

        // Не пустой пакет
        if (sendLen > 0) {
            for (int i = 0; i < sendLen; i++) {

                listResult.add(send.get(i));

            }

            listResult.add(checksum);

        }

        return listResult;

    }


    public boolean isAnswerInPack(List list) {

        if (list.isEmpty()) {

            return false;
        }
        // comPack.alSend = new LinkedList(list);

        comPack.alSend = list;
        Object object = null;
        try {
            object = channel.evalScript(ValuesByChannel.TS_RESULT, comPack);
        } catch (Exception ex) {
            comPack.errorCmd = ex.getMessage();
            channel.setLoggerInfo("Проверка ответа", ex);
            return false;
        }

        if (object == null) {
            return true;
        } else {

            return (Boolean) object;

        }

    }


    boolean isValid(List<Integer> al) throws Exception {

        if (al.size() < minLen) {

            return false;
        }

        boolean result = false;
        String s;

        if (sok.equals("@BCC")) {

            int len = al.size();

            int sum = MathTrans.getSumM228(al.subList(0, len - 1));

            if (al.get(len - 1) == sum) {

                return true;
            }

        } else if (sok.equals("@CRLF")) {

            int len = al.size();

            if (al.get(len - 1) == 10 && al.get(len - 2) == 13) {

                return true;
            }

        } else if (sok.equals("@PCRC16"))//Контродьная сумма регистратора пульсар
        {

            int r;
            int r1;

            r = (int) MathTrans.getCRC_PULSAR(al, 0);
            r1 = (int) MathTrans.getCRC_PULSAR(al, al.size() - 2);

            return (r == r1);

        } else if (sok.equals("@CRC16")) //  Двухбайтная контрольная сумма
        {

            int r;
            int r1;

            r = MathTrans.getCRC16(al, 0);
            r1 = MathTrans.getCRC16(al,
                    al.size() - 2);

            //  r = MathTrans.getCRC16(al.toArray(new Integer[al.size()]), 0);
            //r1 = MathTrans.getCRC16(al.toArray(new Integer[al.size()]),
            //      al.size() - 2);
            return (r == r1);

        } else if (sok.startsWith("@CRC303")) {

            s = MathTrans.getStringByList(al, null, false);

            String ok = "\r\n\u0003";

            if (s.contains(ok) || s.contains("ERR")) {

                commandGet.sResult = s;
                return true;
            }

        } else if (sok.startsWith("@CRC228")) {

            List<Integer> lcrc = al.subList(3, 8);

            int[] ses = null;
            ses = MathTrans.getCRC24(lcrc);

            int a0 = al.get(0);
            int a1 = al.get(1);
            int a2 = al.get(2);

            int b0 = ses[2];
            int b1 = ses[1];
            int b2 = ses[0];

            if (a0 == b0 & a1 == b1 & a2 == b2) {

                return true;
// Проверяем  размер данных

            }

            return false;

        } else if (sok.startsWith("@CRC225")) {

            if (al.size() < minLen) {
                return false;
            }

            List<Integer> lcrc = al.subList(3, 8);

            int[] ses = null;
            ses = MathTrans.getCRC24(lcrc);

            int a0 = al.get(0);
            int a1 = al.get(1);
            int a2 = al.get(2);

            int b0 = ses[2];
            int b1 = ses[1];
            int b2 = ses[0];

            if (a0 == b0 & a1 == b1 & a2 == b2) {

                // Длина нагрузки
                int l255 = al.get(7);

                if (al.size() >= 7 + l255) {

                    return true;
                }

                return false;
// Проверяем  размер данных

            }

            return false;

        } else if (sok.equals("@OK")) {

            s = MathTrans.getStringByList(al, null, false);

            result = (s.contains("OK"));

        } else if (sok.contains("@BETWEEN")) {

            s = MathTrans.getStringByList(al, null, false);

            int id = s.indexOf("\r\n", minLen);
            int idl = s.lastIndexOf("\r\n");

            boolean b = (id > 0 && idl > 0) && (id < idl);

            if (b) {

                commandGet.sResult = s;
                return b;

            }
            // Ответ- последовательность символов
        } else if (sok.contains("%")) {


            s = MathTrans.getStringByList(al, null, false);
            sok = sok.replaceAll("%", "");

            if (s.contains(sok)) {
                return true;
            }
            // Ответ- последовательность символов
        } else if (sok.contains(";")) {

            s = MathTrans.getStringByList(al, null, false);

            String[] asOk = sok.split(";");


            for (String answer : asOk) {

                if (s.contains(answer)) {

                    commandGet.sResult = answer;

                    return true;
                }
            }

        } else if (sok.equals("@NO")) {

            // Контрольная сумма не проверяется
            return true;
        } else {
            // Скрипт

            commandGet.alResult = al;

            result = (boolean) channel.evalScript(script, commandGet);

            return result;

        }

        return result;

    }


}

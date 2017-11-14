/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author 1
 */
public class BlockingBuffer extends ArrayBlockingQueue {

    private LinkedList<CommandGet> alCom;
    private HashMap<Integer, CommandGet> hmCmd;
    private int capacity;
    private boolean sendCmd; //Есть ли команда на добавление в буфер
    private ValuesByChannel channel;
    //Команда со скриптами пакетного режима
    private CommandGet comPack;
    
    public BlockingBuffer(int capacity, HashMap<Integer, CommandGet> hmCmd,ValuesByChannel channel) {
        super(capacity);

        this.channel=channel;
        this.capacity = capacity;
        alCom = new LinkedList<CommandGet>();
        this.hmCmd = hmCmd;
        sendCmd = false;
    }

    public void setChannel(ValuesByChannel channel) {
        this.channel = channel;
    
    
    }

    public boolean isAnswerInPack(List list) {

        if (comPack == null || list.isEmpty()) {

            return false;
        }
       // comPack.alSend = new LinkedList(list);

        comPack.alSend = list;
        Object object = null;
        try {
            object = channel.evalScript(ValuesByChannel.TS_RESULT, comPack);
        } catch (Exception ex) {
            comPack.errorCmd = ex.getMessage();
            MainWorker.deffLoger.fatal(ex.getMessage());
            return false;
        }

        if (object == null) {
            return false;
        } else {

            return (Boolean) object;

        }

    }

    
    
    public boolean isCommandEmpty() {

        return hmCmd.isEmpty() && !sendCmd;
    }

    public boolean addList(List<Integer> list) {

        System.out.println("БуферДО -" + this.size());

        if (this.size() + list.size() >= capacity) {
            // Буфер заполнен,ждем очистки

            sendCmd = true; // есть команда на добавление

            System.out.println("Буфер заполнен,ждем очистки..." + this.size());

            while (!hmCmd.isEmpty()) {

                try {
                    //  Thread.currentThread().join(capacity);

                    Thread.sleep(3000);
                } catch (InterruptedException ex) {

                    System.out.println(ex.getMessage());

                }

            }

            System.out.println("Буфер чист. " + this.size());
            sendCmd = false;

        }

        for (Integer i : list) {
            this.add(i);

        }

        System.out.println("БуферПосле -" + this.size());

        return true;

    }

    public LinkedList<CommandGet> getCmmandsList() {

        return alCom;
    }

    /**
     * Наличие всех данных а обертке
     *
     * @param values
     * @return
     */
    public boolean isFullValues(LinkedList<Integer> values) {

        // Проверяем  размер данных
        List l = values.subList(5, 7);
        int iLen = MathTrans.getIntByList(l, MathTrans.B_LITTLE_ENDIAN,false);

        if (iLen + 9 > values.size()) {
            return false;
        }

        if (!isCheckSum(values, iLen)) {

            return false;
        }

        return true;

    }

    /**
     * Проверка контрольной суммы ответа
     *
     * @param values
     * @return
     */
    public boolean isCheckSum(LinkedList<Integer> values, int len) {

        List<Integer> list = values.subList(8, 8 + len);

        int sum = 0;
        int checksum = values.get(8 + len);
        int checksum1 = 0;

        for (Integer i : list) {
            sum = sum + i;

        }

        checksum1 = (sum + 255) & 255;

        return (checksum == checksum1);

    }

    /**
     * Проверка контрольной суммы пакета
     *
     * @param values
     * @return
     */
    public boolean isCheck(LinkedList<Integer> values) {

        if (values.size() < 9) {

            return false;
        }

        List<Integer> lcrc = values.subList(3, 8);

        int[] ses = null;

        ses = MathTrans.getCRC24Tab(lcrc);

        int a0 = values.get(0);
        int a1 = values.get(1);
        int a2 = values.get(2);

        int b0 = ses[2];
        int b1 = ses[1];
        int b2 = ses[0];

        if (a0 == b0 & a1 == b1 & a2 == b2) {

            return true;

        }
        return false;
    }

    /**
     *
     * @param values обертка
     * @return номер пакета
     */
    public int getNumberPack(LinkedList<Integer> values) {

        // Номер пакета 
        List np = values.subList(3, 5);
        int inp = MathTrans.getIntByList(np, MathTrans.B_LITTLE_ENDIAN,false);

        return inp;

    }

    /**
     *
     * @param values обертка
     * @return результат без обертки
     */
    public List<Integer> getAnswer(LinkedList<Integer> values) {

        if (!isCheck(values)) {

            return values;
        }

        // Проверяем  размер данных
        List l = values.subList(5, 7);
        int iLen = MathTrans.getIntByList(l, MathTrans.B_LITTLE_ENDIAN,false);

        if (iLen + 9 > values.size()) {
            return values;
        }

        // Вытаскиваем данные
        return values.subList(8, 8 + iLen);

    }

    public void createAnswer(LinkedList<Integer> values) {

        // Номер пакета 
        int inp = getNumberPack(values);

        CommandGet cg = hmCmd.remove(inp);

        System.out.println("Команд в буфере-" + hmCmd.size());

        alCom.add(cg);

        LinkedList<Integer> answer = new LinkedList<>();

        // Добавляем данные
        answer.addAll(getAnswer(values));

        cg.alResult = answer;

        for (int i = 0; i < 9 + answer.size(); i++) {
            values.removeFirst();
        }

        int letAns = cg.alSend.size();

        for (int i = 0; i < letAns; i++) {
            this.remove();
            System.out.println(this.size());

        }

    }

}

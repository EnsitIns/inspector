/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author 1
 */
public class ValidResult {

    CommandGet commandGet;
    String script;
    ValuesByChannel channel;
    HashMap<String, Object> mapValid;
    boolean Pack;
    String sok;
    int minLen;

    public boolean isPack() {
        return Pack;
    }

    public void setPack(boolean bPack) {
        this.Pack = bPack;
    }

    public void setCommandGet(CommandGet commandGet) {
        this.commandGet = commandGet;
        sok = commandGet.Ok;
        minLen = commandGet.lenCrc;
        script = (String) commandGet.getProperty("c_measure");
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
        } else if (sok.contains(";")) {

            s = MathTrans.getStringByList(al, null, false);

            String[] asOk = sok.split(";");

           // if (asOk.length > 1) {
             //   String sEnd = "";
            for (String answer : asOk) {

                if (s.contains(answer)) {

                    commandGet.sResult = answer;

                    return true;
                }
            }
             //       int is = MathTrans.getIntByString(asOk[i]);
            //     sEnd = sEnd + (char) is;

                //}
              //  for (int i = 0; i
            //        < asOk.length; i++) {
                  //  int is = MathTrans.getIntByString(asOk[i]);
            // sEnd = sEnd + (char) is;
            //  }
                //if (s.contains(sEnd)) {
                  //  commandGet.sResult = s;
                  //  return true;
               // }
            //  } else {
            //    if (s.contains(sok)) {
              //      return true;
              //  }
           // }
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

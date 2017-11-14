/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import connectdbf.StatementEx;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Визуализация табличного запроса Данных
 *
 * @author 1
 */
public class MapMessageProcess {

    public static final String DATA_BASE = "Данные есть в базе.";
    public static final String DATA_GET = "Данные получены.";
    public static final String DATA_GO = "Запрос данных...";
    public static final String DATA_BLACK = "Объект недоступен.";
    
    HashMap<Integer, Integer> hmPozicion;
    HashMap<Integer, String> hmMsg;
    private int idColInfo;

    public MapMessageProcess() {
        hmPozicion = new HashMap<>();
        hmMsg = new HashMap<>();
    }

    public HashMap<Integer, String> getInfoRows() {
        return hmMsg;
    }

    public void clearProcess() {
        hmMsg.clear();
      hmPozicion.clear();
    }

    public void clearErrorRows() {

        for (Integer id : hmPozicion.keySet()) {

            int poz = hmPozicion.get(id);

            if (hmMsg.containsKey(poz)) {

                String msg = hmMsg.get(poz);

             //  if (!msg.equals(MapMessageProcess.DATA_BASE) && !msg.equals(MapMessageProcess.DATA_GET)) {
               //     hmMsg.remove(poz);
              // }

            }

        }

    }

    public void setIdColInfo(int idColInfo) {
        this.idColInfo = idColInfo;
    }

    public boolean isEmpty() {

        return hmMsg.isEmpty();
    }

    public int getIdColInfo() {
        return idColInfo;
    }

    public void setRow(int idObject, int nRow) {

        hmPozicion.put(idObject, nRow);

    }

    public void saveMessagesProcess() throws SQLException {

        HashMap<String, Object> hmPam = new HashMap<String, Object>();

        String sKey = SqlTask.getPrimaryKeyTable(null, "objects");

        hmPam.put(sKey, null);
        hmPam.put("value_info", null);

        StatementEx statementEx = new StatementEx(null, "objects", hmPam);

        try {
            String msg = null;

            for (Integer id : hmPozicion.keySet()) {

                int poz = hmPozicion.get(id);

                if (hmMsg.containsKey(poz)) {

                    msg = hmMsg.get(poz);
                }

                if (msg != null) {
                    hmPam.put(sKey, id);
                    hmPam.put("value_info", msg);
                    statementEx.replaceRecInTable(hmPam, false);
                }
            }
        } finally {
            statementEx.close();
        }
    }

    public void setInfoProcess(int idObject, String msg) {

        if (hmPozicion.containsKey(idObject)) {
            hmMsg.put(hmPozicion.get(idObject), msg);

        }

    }

}

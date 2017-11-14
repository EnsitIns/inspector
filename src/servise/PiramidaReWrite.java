/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.CM_PIRAMIDA_COMMERC;

/**
 *
 * @author 1
 */
public class PiramidaReWrite extends MainWorker {

    private DateTime dtFirst;
    private DateTime dtLast;

    public PiramidaReWrite(ExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public void update(Observable o, Object arg) {


        if (arg instanceof Object[]) {

            Object[] objects = (Object[]) arg;

            String cmnd = (String) objects[0];


            if (cmnd.equals(CM_PIRAMIDA_COMMERC)) {


                dtFirst = (DateTime) objects[1];
                dtLast = (DateTime) objects[2];

                executeProcess();

            }
        }



    }

    private void RefreshCommercData() throws Exception {


        String s = "Обнуление флага передачи данных...";
        newProcess(s);
        // Запрашиваем экспортируемые
        Timestamp tLast;
        Timestamp tFirst = new Timestamp(dtFirst.getMillis());

        DateTime dateTime = new DateTime(dtLast.getMillis());
        dateTime = dateTime.plusDays(1);



        tLast = new Timestamp(dateTime.getMillis());

        ArrayList<String> al = Work.getAllNameTablesByTyp(15);
        setMinMaxValue(0, al.size());
        String sql;
        for (String nameTable : al) {
            String capS = (String) Work.getParamTableByName(nameTable, Work.TABLE_CAPTION);
            int row = al.indexOf(nameTable);
            refreshBarValue(row);
            sql = "UPDATE " + nameTable + " SET flag0=? WHERE value_date>? AND value_date<=?";

            blinkText("Обнуление флага в таблице '" + capS + "'...");

            //  if(nameTable.equals("enegry_data")){
            int count = SqlTask.executeUpdateSQL(null, sql, new Object[]{0, tFirst, tLast}, null);

            //}
            stopBlinkText();
            refreshBarValue(capS + "(" + count + ")");
        }
    }

    @Override
    public void doProcess() {
        try {
            RefreshCommercData();
        } catch (Exception ex) {
            setLoggerInfo("", ex);
        }

    }
}

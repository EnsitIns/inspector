package servise;

import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import xmldom.XmlTask;

import javax.swing.*;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.esStatus.esAddArxive;
import static constatant_static.SettingActions.esStatus.isSetStatus;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Перевод данных в архив по истечению актуальности
 *
 * @author 1
 */
public class Arxive extends MainWorker {

    private static Integer MAX_COUNT_MOVED;
    private Integer idSchedule; // id расписания перевода данных в архив
    private Integer countMonth; // количество месяцев актуальности
    private String nameSchedule;// Имя расписания
    private ScheduleClass schedClass; // класс расписаний
    private String pathArxiv; // Путь к папке с архивами

    HashMap valeus;

    public Arxive(ExecutorService pool) {
        this.pool = pool;
        try {
            setParameters();
        } catch (Exception ex) {
            setLoggerInfo("Параметры", ex);
        }
    }

    @Override
    public void doProcess() {
        try {

            errorString = null;
            Boolean arxive = (Boolean) getProperty("arxive");

            newProcess("Перевод данных в архив...");

            String sarx = (String) valeus.get("size_arxiv");

            Integer iarx = Integer.parseInt(sarx);

            iarx = (iarx == null ? 1 : iarx);

            if (arxive != null && arxive) {

                iarx = (iarx < 12 ? 12 : iarx);
            }

            DateTime dateTime = new DateTime();

            DateTime dateTimeCurr;

            for (int i = iarx; i >= 0; i--) {
                dateTimeCurr = dateTime.minusMonths(i);
                goMove(dateTimeCurr);

            }

            //Усекаем таблицу
            if (arxive != null && arxive) {

                String sactu = (String) valeus.get("count_arxiv");

                Integer month = Integer.parseInt(sactu);

                month = (month < 4 ? 4 : month);

                String[] s = {"Да", "Нет"};

                // Удаление всех объектов
                if (JOptionPane.showOptionDialog(null, "Подтвердите перевод(рекомендуется сделать архивную копию базы!) ",
                        "Перевод в архив", JOptionPane.WARNING_MESSAGE,
                        JOptionPane.WARNING_MESSAGE, null, s, s[1]) == 0) {
                    try {
                        blinkText("Удаление данных из базы...");
                        deleteRows(month);
                    } finally {
                        stopBlinkText();

                    }
                }

            }

        } catch (Exception ex) {
            errorString = "Ошибка";
            setLoggerInfo(nameSchedule, ex);
            return;
        }

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer) {
            try {
                setParameters();
            } catch (Exception ex) {
                setLoggerInfo("Установка параметров", ex);
            }

            if (idSchedule == (Integer) arg && o instanceof ScheduleClass && !isSetStatus(esAddArxive)) {

                schedClass = (ScheduleClass) o;

                nameSchedule = schedClass.getNameSchedule();
                // Выполняем если количество актуальных месяцев больше или равно 1

                if (countMonth != null && countMonth >= 1) {

                    executeProcess();
                }
            }
        }

        if (arg instanceof Boolean && o instanceof ScheduleClass) {
            MainWorker.isStop = true;
            setLoggerInfo("Расписание перевод данных в архив остановлено", null);

        }
    }

    /**
     * Удаление Данных из таблиц
     */
    private void deleteRows(int month) throws SQLException {

        String sql = "";

        DateTime dateTime = new DateTime();

        dateTime = dateTime.millisOfDay().setCopy(0);

// Начальная дата перевода
        dateTime = dateTime.minusMonths(month);

        Timestamp ts = new Timestamp(dateTime.getMillis());

        Map<String, String> map = SqlTask.getNameAndCaptionTablesByTyp(null, 15);

        for (String name : map.keySet()) {
            sql = "DELETE FROM " + name + " WHERE value_date< ?";
            SqlTask.executeSql(null, sql, new Object[]{ts});

        }

    }

    private void setParameters() throws Exception {

        Document docData = null;
        docData = Work.getXmlDocFromConst("arxive");

        valeus = XmlTask.getMapAttrubuteByName(docData.getDocumentElement(), "name", "value", "cell");

        String countMov = (String) valeus.get("count_arxiv");

        if (countMov != null && !countMov.isEmpty()) {
            try {

                MAX_COUNT_MOVED = Integer.decode(countMov);
            } catch (NumberFormatException e) {
                setLoggerInfo("Не указано количество переводимых данных!", null);

            }

        } else {
            MAX_COUNT_MOVED = 10000;
        }

        String id_sched = (String) valeus.get("shedule_arxiv");

        pathArxiv = (String) valeus.get("folder_arxiv");

        String count_Month = (String) valeus.get("count_arxiv");

        try {

            countMonth = Integer.parseInt(count_Month);
        } catch (NumberFormatException e) {

            setLoggerInfo("Не указано количество месяцев актуальности!", null);

        }
        if (id_sched != null && !id_sched.isEmpty()) {
            try {
                idSchedule = Integer.parseInt(id_sched);

            } catch (NumberFormatException ex) {

                idSchedule = -1;
                setLoggerInfo("", ex);

            }
        }
    }

    private ResultSet getResultSetMove(String nameTable, DateTime dateTime) {

        String sql;
        ResultSet rs = null;

        Timestamp timestamp = new Timestamp(dateTime.getMillis());

        //Вытаскиваем значение
        try {

            sql = "SELECT * FROM " + nameTable + "  WHERE  value_date<? ";

            rs = SqlTask.getResultSet(null, sql, new Object[]{timestamp}, MAX_COUNT_MOVED, ResultSet.CONCUR_UPDATABLE);

        } catch (SQLException ex) {

            setLoggerInfo("Запрос данных для перемещения", ex);
        }

        return rs;

    }

    /**
     * Перевод данных в архив.
     */
    private void goMove(DateTime dateTime) throws SQLException, Exception {

        dateTime = dateTime.millisOfDay().setCopy(0);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM_yyyy");

        DateTime dtFirst;
        DateTime dtLast;

        int countRow = 0;

// Начальная дата перевода
        dateTime = dateTime.minusMonths(1);

        dtFirst = dateTime.dayOfMonth().setCopy(1);

        dtLast = dtFirst.plusMonths(1);

        Timestamp tFirst = new Timestamp(dtFirst.getMillis());

        Timestamp tLast = new Timestamp(dtLast.getMillis());

        int year = dateTime.getYearOfEra();

        String name = dateTime.toString(dtf);

        // все таблицы с данными
        ArrayList<String> alTables = Work.getAllNameTablesByTyp(15);

        String sql = "SELECT id_object FROM objects";

        String path = (String) valeus.get("folder_arxiv");

        File file;

        String pathSep = File.separator;

        path = path + pathSep + "" + year;

        // Проходим все объекты
        ResultSet rsObjects = SqlTask.getResultSet(null, sql);

        try {

       
            rsObjects.last();

            countRow = rsObjects.getRow();
            setMinMaxValue(0, countRow);
            rsObjects.beforeFirst();

            int row = 0;

            while (rsObjects.next()) {

                
                if (MainWorker.isStop) {

                    return;
                }
                
                
                int id = rsObjects.getInt(1);

                String path1 = path + pathSep + id;

                refreshBarValue(name + "_" + id);

                //name=name+
                // формируем название файла
                for (String stable : alTables) {

                    String name1 = name + "_" + stable + "_" + id + ".xml";

                    // File fPath = new File(path1);
                    // fPath.mkdirs();
                    file = new File(path1, name1);

                    if (!file.exists()) {
                    // нет такого файла

                        //     file.mkdir();
                        sql = "SELECT * FROM " + stable + " WHERE id_object=? AND value_date>=? AND value_date<=?";

                        ResultSet rsData = SqlTask.getResultSet(null, sql, new Object[]{id, tFirst, tLast});

                        try {
                            DiffTask.saveTableInXml(rsData, file);

                        } finally {
                            rsData.close();
                        }

                    }

                }

                row++;

                refreshBarValue(row);
            }
        } finally {
            rsObjects.close();
        }

    }
}

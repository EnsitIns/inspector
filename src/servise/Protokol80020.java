/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import dbf.Work;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xmldom.XmlTask;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.CM_PIRAMIDA_80020;
import static constatant_static.SettingActions.esStatus.esPiramida80020On;
import static constatant_static.SettingActions.esStatus.isSetStatus;

/**
 * @author 1
 */
public class Protokol80020 extends MainWorker {

    public static final int PERIOD_LAST = 0;
    public static String TASK_CREATE_80020 = "Создание макета 80020 XML";

    private DateTime dtFirst;
    private DateTime dtLast;
    private int idSchedule;   //Макет ОАО "АТС" XML 80020
    private String mail_addres; // Электронный адрес
    private String currentSQL; // текущий запрос;
    private static int count_file = 1; // номер файла
    private int count_80020 = 1; // номер файла протокола 80020
    private Document document;
    private HashMap<Integer, HashMap<String, Object>> hmParameters; // Параметры объектов
    private HashMap<Integer, HashMap<Integer, String>> hmCodes;
    private HashMap<Integer, String> hmCodesPiramida;
    private ArrayList<ResultSet> alWorking;  // список обрабатываемых параметров
    private String nameSchedule;// Имя расписания
    private HashMap<Integer, Double> hmRemains1;  // Остаток1 предыдущего дня по каждой точке
    private HashMap<Integer, Double> hmRemains2;  // Остаток2 предыдущего дня по каждой точке
    private HashMap<Integer, Double> hmRemains3;  // Остаток3 предыдущего дня по каждой точке
    private HashMap<Integer, Double> hmRemains4;  // Остаток4 предыдущего дня по каждой точке
    private HashMap<Integer, Integer> hmSum1;  // Cумма за весь период A+
    private HashMap<Integer, Integer> hmSum2;  // Cумма за весь период A-
    private HashMap<Integer, Integer> hmSum3;  // Cумма за весь период R+
    private HashMap<Integer, Integer> hmSum4;  // Cумма за весь период R-
    private ZipCreator zipCreator;
    private ArrayList<String> report;
    private HashMap<Integer, String> hmCaptions;
    private ArrayList<String> errors;
    private Integer groupSub;

    public Protokol80020(ExecutorService pool) {
        this.pool = pool;
        currentSQL = "SELECT  * FROM objects";
        zipCreator = null;
        hmRemains1 = new HashMap<Integer, Double>();
        hmRemains2 = new HashMap<Integer, Double>();
        hmRemains3 = new HashMap<Integer, Double>();
        hmRemains4 = new HashMap<Integer, Double>();
        hmSum1 = new HashMap<Integer, Integer>();
        hmSum2 = new HashMap<Integer, Integer>();
        hmSum3 = new HashMap<Integer, Integer>();
        hmSum4 = new HashMap<Integer, Integer>();

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Object[]) {

            Object[] objects = (Object[]) arg;

            String cmnd = (String) objects[0];

            if (cmnd.equals(CM_PIRAMIDA_80020)) {
                // Обновление протокола 80020

                dtFirst = (DateTime) objects[1];
                dtLast = (DateTime) objects[2];

                executeProcess();
            }

        } else if (arg instanceof Integer) {

            if (isSetStatus(esPiramida80020On)) {
                return;
            }
            try {
                setParameters();
            } catch (Exception ex) {
                setLoggerInfo("Установка параметров", ex);
            }

            // Сработало расписание
            if (idSchedule == (Integer) arg) {

                ScheduleClass sc = (ScheduleClass) o;

                nameSchedule = sc.getNameSchedule();

                // Передаем данные за предыдущий день
                DateTime dateTime = new DateTime();

                dtFirst = dateTime.minusDays(1);
                dtLast = dtFirst;

                executeProcess();

            }
        } else if (arg instanceof JButton) {

            try {
                setParameters();

                JButton button = (JButton) arg;

                Document docXml = (Document) button.getClientProperty("docConfig");

                mapProperties = XmlTask.getMapAttrubuteByName(docXml.getDocumentElement(), "name", "value", "cell");

                HashMap<String, Object> hmParam = Work.getParametersFromConst("piramida");

                mapProperties.putAll(hmParam);

            } catch (Exception ex) {
                setLoggerInfo("Параметры экспорта", ex);

            }

            String dFirst = (String) mapProperties.get("date_first");
            String dLast = (String) mapProperties.get("date_last");
            DateTimeFormatter dtf;
            dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");

            String sql_object = (String) mapProperties.get("sql_object");

            Integer idSql = Integer.decode(sql_object);
            try {
                HashMap<String, Object> hmSql = Work.getParametersRow(idSql, null, "object6", false, false);

                currentSQL = (String) hmSql.get("sql_string");

            } catch (SQLException ex) {
                setLoggerInfo("Данные запроса", ex);
            }

            dtFirst = dtf.parseDateTime(dFirst);
            dtLast = dtf.parseDateTime(dLast);

            executeProcess();

        }
    }

    public void setParameters() throws Exception {

        mapProperties = Work.getParametersFromConst("piramida");

        if (mapProperties.isEmpty()) {

            return;

        }

        Integer id_schedAtc = (Integer) mapProperties.get("shedule_atc");

        if (id_schedAtc != null) {
            idSchedule = id_schedAtc;

            // hmCodes = new HashMap<Integer, HashMap<Integer, String>>();
            // createMapCodes();
        }

        mail_addres = (String) mapProperties.get("mail_piram");

    }

    /**
     * Создает XML файл для отсылки на почту по протоколу 80020
     *
     * @param rs
     */
    private void createSendFileProtokol(ResultSet rsObjects, Element elArea, DateTime timeSend) {
        try {
            rsObjects.last();
            int countRow = rsObjects.getRow();
            // Вытаскивае кфц для   протокола 80020
            // HashMap<String,String> hmKfc=Parameter.getKfcByBytSet(0);
            // Профиль за предыдущий день
            // Начальная дата
            DateTime dateFirst;
            // Конечная дата
            DateTime dateLast;
            dateFirst = timeSend;
            dateLast = timeSend.plusDays(1);
            dateFirst = dateFirst.millisOfDay().setCopy(0);
            dateLast = dateLast.millisOfDay().setCopy(0);
            rsObjects.beforeFirst();
            setMinMaxValue(0, countRow);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");
            refreshBarValue("Формируем XML файл на: " + dateFirst.toString(dtf));
            String sql;
            String nameTable = "profil_power";
            dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
            HashMap<String, Object> hmParamObject = null;
            try {
                while (rsObjects.next()) {
                    int row = rsObjects.getRow();
                    refreshBarValue(row);
                    int idObj = rsObjects.getInt("id_object");
                    //   String inn_80200 = rsObjects.getString("inn_80200");
                    // String sNameObj = rsObjects.getString("name3");
                    // String sNameObj = "-";
                    //   Integer timezone_80200 = rsObjects.getInt("timezone_80200");
                    String code_80200 = rsObjects.getString("code_80200");
                    // String name_80200 = rsObjects.getString("name_80200");

                    try {

                        hmParamObject = Work.getParametersRow(idObj, rsObjects, "objects", true, true);
                    } catch (SQLException ex) {
                        setNotifyObservers("Параметры объекта № " + idObj);
                        setNotifyObservers(ex);
                        continue;

                    }

                    String name = (String) hmParamObject.get("caption");
                    hmCaptions.put(idObj, name);
                    Integer kt = (Integer) hmParamObject.get("kt");
                    Integer kn = (Integer) hmParamObject.get("kn");
                    // Выбираем данные по текущему объекту за предыдущий день
                    sql = "SELECT * FROM profil_power WHERE Id_object=? AND (value_date>? AND value_date<=?)";
                    Timestamp tFirst = new Timestamp(dateFirst.getMillis());
                    Timestamp tLast = new Timestamp(dateLast.getMillis());
                    Object[] objects = new Object[]{idObj, tFirst, tLast};
                    ResultSet rsData = SqlTask.getResultSet(null, sql, objects);
                    rsData.last();
                    // Количество записей должно быть 48
                    int cRow = rsData.getRow();
                    if (cRow < 48) {

                        DateTimeFormatter dtff = DateTimeFormat.forPattern("dd.MM.yyyy");
                        String ddd = dateFirst.toString(dtff);
                        String s = name + ": Количество записей -" + cRow + " Дата: " + ddd;
                        errors.add(s);

                    }

                    Element el_measuringpoint = document.createElement("measuringpoint");
                    elArea.appendChild(el_measuringpoint);
                    el_measuringpoint.setAttribute("code", code_80200);
                    el_measuringpoint.setAttribute("name", name);
                    //*************measuringchannel************
                    // Каналы
                    Element el_channel_1 = document.createElement("measuringchannel");
                    el_measuringpoint.appendChild(el_channel_1);
                    el_channel_1.setAttribute("code", "01");
                    el_channel_1.setAttribute("desc", "Счетчик-активная, прием");
                    Element el_channel_2 = document.createElement("measuringchannel");
                    //    el_measuringpoint.appendChild(el_channel_2);
                    el_channel_2.setAttribute("code", "02");
                    el_channel_2.setAttribute("desc", "Счетчик-активная, отдача");
                    Element el_channel_3 = document.createElement("measuringchannel");
                    el_measuringpoint.appendChild(el_channel_3);
                    el_channel_3.setAttribute("code", "03");
                    el_channel_3.setAttribute("desc", "Счетчик-реактивная, прием");
                    Element el_channel_4 = document.createElement("measuringchannel");
                    //  el_measuringpoint.appendChild(el_channel_4);
                    el_channel_4.setAttribute("code", "04");
                    el_channel_4.setAttribute("desc", "Счетчик-реактивная, отдача");
                    rsData.beforeFirst();
                    // Значения каналов
                    Double d1;
                    Double d2;
                    Double d3;
                    Double d4;
                    // округленые значения
                    int ocr1 = 0;
                    int ocr2 = 0;
                    int ocr3 = 0;
                    int ocr4 = 0;
                    // Остаток c предыдущего дня
                    Double ost1 = 0.0;
                    Double ost2 = 0.0;
                    Double ost3 = 0.0;
                    Double ost4 = 0.0;

                    if (hmRemains1.containsKey(idObj)) {
                        ost1 = hmRemains1.get(idObj);
                    }
                    if (hmRemains2.containsKey(idObj)) {
                        ost2 = hmRemains1.get(idObj);
                    }
                    if (hmRemains3.containsKey(idObj)) {
                        ost3 = hmRemains1.get(idObj);
                    }
                    if (hmRemains4.containsKey(idObj)) {
                        ost4 = hmRemains1.get(idObj);
                    }

                    dtf = DateTimeFormat.forPattern("HHmm");
                    DateTime dateTimeStart;
                    DateTime dateTimeEnd;
                    String sStart = null;
                    String sEnd = null;
                    String sValue = "";
                    while (rsData.next()) {
                        Timestamp t = rsData.getTimestamp("value_date");
                        dateTimeEnd = new DateTime(t);

                        int day = dateTimeEnd.getDayOfMonth();

                        dateTimeStart = dateTimeEnd.minusMinutes(30);
                        sStart = dateTimeStart.toString(dtf);
                        sEnd = dateTimeEnd.toString(dtf);
                        d1 = rsData.getDouble("power_pa");
                        d1 = d1 * kt * kn + ost1;
                        ocr1 = (int) Math.round(d1);
                        ost1 = d1 - ocr1;
                        sValue = "" + ocr1;
                        Element el_period_1 = document.createElement("period");
                        el_channel_1.appendChild(el_period_1);
                        el_period_1.setAttribute("start", sStart);
                        el_period_1.setAttribute("end", sEnd);

                        /**
                         * if (day == 26 && sStart.equals("0000") &&
                         * sEnd.equals("0030")) {
                         * el_period_1.setAttribute("summer", "1"); } if (day ==
                         * 26 && sStart.equals("0030") && sEnd.equals("0100")) {
                         * el_period_1.setAttribute("summer", "1"); }
                         *
                         * if (day == 26 && sStart.equals("0100") &&
                         * sEnd.equals("0130")) {
                         * el_period_1.setAttribute("summer", "1"); } if (day ==
                         * 26 && sStart.equals("0130") && sEnd.equals("0200")) {
                         * el_period_1.setAttribute("summer", "1"); }
                         *
                         */
                        Element el_value_1 = document.createElement("value");
                        el_value_1.setAttribute("status", "0");
                        el_value_1.setTextContent(sValue);
                        el_period_1.appendChild(el_value_1);
                        d2 = rsData.getDouble("power_pr") * kt * kn + ost2;
                        ocr2 = (int) Math.round(d2);
                        ost2 = d2 - ocr2;
                        sValue = "" + ocr2;
                        Element el_period_2 = document.createElement("period");
                        el_channel_2.appendChild(el_period_2);
                        el_period_2.setAttribute("start", sStart);
                        el_period_2.setAttribute("end", sEnd);
                        Element el_value_2 = document.createElement("value");
                        el_value_2.setAttribute("status", "0");
                        el_value_2.setTextContent(sValue);
                        el_period_2.appendChild(el_value_2);
                        d3 = rsData.getDouble("power_qa") * kt * kn + ost3;
                        ocr3 = (int) Math.round(d3);
                        ost3 = d3 - ocr3;
                        sValue = "" + ocr3;
                        Element el_period_3 = document.createElement("period");
                        el_channel_3.appendChild(el_period_3);
                        el_period_3.setAttribute("start", sStart);
                        el_period_3.setAttribute("end", sEnd);
                        Element el_value_3 = document.createElement("value");
                        el_value_3.setAttribute("status", "0");
                        el_value_3.setTextContent(sValue);
                        el_period_3.appendChild(el_value_3);
                        d4 = rsData.getDouble("power_qr") * kt * kn + ost4;
                        ocr4 = (int) Math.round(d4);
                        ost3 = d4 - ocr4;
                        sValue = "" + ocr4;
                        Element el_period_4 = document.createElement("period");
                        el_channel_4.appendChild(el_period_4);
                        el_period_4.setAttribute("start", sStart);
                        el_period_3.setAttribute("end", sEnd);
                        Element el_value_4 = document.createElement("value");
                        el_value_4.setAttribute("status", "0");
                        el_value_4.setTextContent(sValue);
                        el_period_4.appendChild(el_value_4);

                        // суммируем...
                        // Сумма за весь период
                        if (hmSum1.containsKey(idObj)) {
                            Integer val = hmSum1.get(idObj);
                            hmSum1.put(idObj, val + ocr1);
                        } else {
                            hmSum1.put(idObj, ocr1);
                        }

                        if (hmSum2.containsKey(idObj)) {
                            Integer val = hmSum2.get(idObj);
                            hmSum2.put(idObj, val + ocr2);
                        } else {
                            hmSum2.put(idObj, ocr2);
                        }

                        if (hmSum3.containsKey(idObj)) {
                            Integer val = hmSum3.get(idObj);
                            hmSum3.put(idObj, val + ocr3);
                        } else {
                            hmSum3.put(idObj, ocr3);
                        }

                        if (hmSum4.containsKey(idObj)) {
                            Integer val = hmSum4.get(idObj);
                            hmSum4.put(idObj, val + ocr4);
                        } else {
                            hmSum4.put(idObj, ocr4);
                        }

                    } //measuringpoint

                    // Остатки на следующий день
                    hmRemains1.put(idObj, ost1);
                    hmRemains2.put(idObj, ost2);
                    hmRemains3.put(idObj, ost3);
                    hmRemains4.put(idObj, ost4);

                    //measuringpoint
                }
            } finally {
                rsObjects.close();
            }
        } catch (SQLException ex) {
            setNotifyObservers(ex);
        }
    }

    /**
     * Создает XML файл для отсылки на почту по протоколу 80020
     *
     * @param rs
     */
    private void createValuesProtokol(ResultSet rsObjects, Element elArea, DateTime timeSend) throws Exception {

        rsObjects.last();
        int countRow = rsObjects.getRow();
        // Вытаскивае кфц для   протокола 80020
        // HashMap<String,String> hmKfc=Parameter.getKfcByBytSet(0);
        // Профиль за предыдущий день
        // Начальная дата
        DateTime dateFirst;
        // Конечная дата
        DateTime dateLast;
        dateFirst = timeSend;
        dateLast = timeSend.plusDays(1);
        dateFirst = dateFirst.millisOfDay().setCopy(0);
        dateLast = dateLast.millisOfDay().setCopy(0);
        rsObjects.beforeFirst();
        setMinMaxValue(0, countRow);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");
        refreshBarValue("Формируем XML файл на: " + dateFirst.toString(dtf));
        String sql;
        String nameTable = "profil_power";
        dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        HashMap<String, Object> hmParamObject = new HashMap<String, Object>();

        while (rsObjects.next()) {

            hmParamObject.clear();

            int row = rsObjects.getRow();
            refreshBarValue(row);
            int idObj = rsObjects.getInt("id_object");
            //   String inn_80200 = rsObjects.getString("inn_80200");
            // String sNameObj = rsObjects.getString("name3");
            // String sNameObj = "-";
            //   Integer timezone_80200 = rsObjects.getInt("timezone_80200");
            String code_80200 = rsObjects.getString("code_80200");
            // String name_80200 = rsObjects.getString("name_80200");

            try {

                SqlTask.addParamToMap(rsObjects, hmParamObject);

                //      hmParamObject = Work.getParametersRow(idObj, rsObjects, "objects", true, true);
            } catch (SQLException ex) {
                setNotifyObservers("Параметры объекта № " + idObj);
                setNotifyObservers(ex);
                continue;

            }

            String name = (String) hmParamObject.get("dis_nummer");
            hmCaptions.put(idObj, name);
            Integer kt = (Integer) hmParamObject.get("kt");
            Integer kn = (Integer) hmParamObject.get("kn");
            // Выбираем данные по текущему объекту за предыдущий день
            sql = "SELECT * FROM profil_power WHERE Id_object=? AND (value_date>? AND value_date<=?)";
            Timestamp tFirst = new Timestamp(dateFirst.getMillis());
            Timestamp tLast = new Timestamp(dateLast.getMillis());
            Object[] objects = new Object[]{idObj, tFirst, tLast};
            ResultSet rsData = SqlTask.getResultSet(null, sql, objects);
            rsData.last();
            // Количество записей должно быть 48
            int cRow = rsData.getRow();
            if (cRow < 48) {

                DateTimeFormatter dtff = DateTimeFormat.forPattern("dd.MM.yyyy");
                String ddd = dateFirst.toString(dtff);
                String s = name + ": Количество записей -" + cRow + " Дата: " + ddd;
                errors.add(s);

            }

            Element el_measuringpoint = document.createElement("measuringpoint");
            elArea.appendChild(el_measuringpoint);
            el_measuringpoint.setAttribute("code", code_80200);
            el_measuringpoint.setAttribute("name", name);
            //*************measuringchannel************
            // Каналы
            Element el_channel_1 = document.createElement("measuringchannel");
            el_measuringpoint.appendChild(el_channel_1);
            el_channel_1.setAttribute("code", "01");
            el_channel_1.setAttribute("desc", "Счетчик-активная, прием");
            Element el_channel_2 = document.createElement("measuringchannel");
            //    el_measuringpoint.appendChild(el_channel_2);
            el_channel_2.setAttribute("code", "02");
            el_channel_2.setAttribute("desc", "Счетчик-активная, отдача");
            Element el_channel_3 = document.createElement("measuringchannel");
            el_measuringpoint.appendChild(el_channel_3);
            el_channel_3.setAttribute("code", "03");
            el_channel_3.setAttribute("desc", "Счетчик-реактивная, прием");
            Element el_channel_4 = document.createElement("measuringchannel");
            //  el_measuringpoint.appendChild(el_channel_4);
            el_channel_4.setAttribute("code", "04");
            el_channel_4.setAttribute("desc", "Счетчик-реактивная, отдача");
            rsData.beforeFirst();
            // Значения каналов
            Double d1;
            Double d2;
            Double d3;
            Double d4;
            // округленые значения
            int ocr1 = 0;
            int ocr2 = 0;
            int ocr3 = 0;
            int ocr4 = 0;
            // Остаток c предыдущего дня
            Double ost1 = 0.0;
            Double ost2 = 0.0;
            Double ost3 = 0.0;
            Double ost4 = 0.0;

            if (hmRemains1.containsKey(idObj)) {
                ost1 = hmRemains1.get(idObj);
            }
            if (hmRemains2.containsKey(idObj)) {
                ost2 = hmRemains1.get(idObj);
            }
            if (hmRemains3.containsKey(idObj)) {
                ost3 = hmRemains1.get(idObj);
            }
            if (hmRemains4.containsKey(idObj)) {
                ost4 = hmRemains1.get(idObj);
            }

            dtf = DateTimeFormat.forPattern("HHmm");
            DateTime dateTimeStart;
            DateTime dateTimeEnd;
            String sStart = null;
            String sEnd = null;
            String sValue = "";
            while (rsData.next()) {
                Timestamp t = rsData.getTimestamp("value_date");
                //  int season=rsData.getInt("season");

                dateTimeEnd = new DateTime(t);

                int day = dateTimeEnd.getDayOfMonth();

                dateTimeStart = dateTimeEnd.minusMinutes(30);
                sStart = dateTimeStart.toString(dtf);
                sEnd = dateTimeEnd.toString(dtf);
                d1 = rsData.getDouble("power_pa");
                d1 = d1 * kt * kn + ost1;
                ocr1 = (int) Math.round(d1);
                ost1 = d1 - ocr1;
                sValue = "" + ocr1;
                Element el_period_1 = document.createElement("period");
                el_channel_1.appendChild(el_period_1);
                el_period_1.setAttribute("start", sStart);
                el_period_1.setAttribute("end", sEnd);

                /*     if (day == 26 &&  sEnd.equals("0030")) {
                 el_period_1.setAttribute("summer", "1");
                 }
                 if (day == 26 &&  sEnd.equals("0100")) {
                 el_period_1.setAttribute("summer", "1");
                 }

                 if (day == 26 &&  sEnd.equals("0130") && season==13) {
                 el_period_1.setAttribute("summer", "1");
                 }
                 if (day == 26 &&  sEnd.equals("0200") && season==13 ) {
                 el_period_1.setAttribute("summer", "1");
                 }

                 **/
                Element el_value_1 = document.createElement("value");
                el_value_1.setAttribute("status", "0");
                el_value_1.setTextContent(sValue);
                el_period_1.appendChild(el_value_1);
                d2 = rsData.getDouble("power_pr") * kt * kn + ost2;
                ocr2 = (int) Math.round(d2);
                ost2 = d2 - ocr2;
                sValue = "" + ocr2;
                Element el_period_2 = document.createElement("period");
                el_channel_2.appendChild(el_period_2);
                el_period_2.setAttribute("start", sStart);
                el_period_2.setAttribute("end", sEnd);
                Element el_value_2 = document.createElement("value");
                el_value_2.setAttribute("status", "0");
                el_value_2.setTextContent(sValue);
                el_period_2.appendChild(el_value_2);
                d3 = rsData.getDouble("power_qa") * kt * kn + ost3;
                ocr3 = (int) Math.round(d3);
                ost3 = d3 - ocr3;
                sValue = "" + ocr3;
                Element el_period_3 = document.createElement("period");
                el_channel_3.appendChild(el_period_3);
                el_period_3.setAttribute("start", sStart);
                el_period_3.setAttribute("end", sEnd);
                Element el_value_3 = document.createElement("value");
                el_value_3.setAttribute("status", "0");
                el_value_3.setTextContent(sValue);
                el_period_3.appendChild(el_value_3);
                d4 = rsData.getDouble("power_qr") * kt * kn + ost4;
                ocr4 = (int) Math.round(d4);
                ost3 = d4 - ocr4;
                sValue = "" + ocr4;
                Element el_period_4 = document.createElement("period");
                el_channel_4.appendChild(el_period_4);
                el_period_4.setAttribute("start", sStart);
                el_period_3.setAttribute("end", sEnd);
                Element el_value_4 = document.createElement("value");
                el_value_4.setAttribute("status", "0");
                el_value_4.setTextContent(sValue);
                el_period_4.appendChild(el_value_4);

                // суммируем...
                // Сумма за весь период
                if (hmSum1.containsKey(idObj)) {
                    Integer val = hmSum1.get(idObj);
                    hmSum1.put(idObj, val + ocr1);
                } else {
                    hmSum1.put(idObj, ocr1);
                }

                if (hmSum2.containsKey(idObj)) {
                    Integer val = hmSum2.get(idObj);
                    hmSum2.put(idObj, val + ocr2);
                } else {
                    hmSum2.put(idObj, ocr2);
                }

                if (hmSum3.containsKey(idObj)) {
                    Integer val = hmSum3.get(idObj);
                    hmSum3.put(idObj, val + ocr3);
                } else {
                    hmSum3.put(idObj, ocr3);
                }

                if (hmSum4.containsKey(idObj)) {
                    Integer val = hmSum4.get(idObj);
                    hmSum4.put(idObj, val + ocr4);
                } else {
                    hmSum4.put(idObj, ocr4);
                }

            } //measuringpoint

            // Остатки на следующий день
            hmRemains1.put(idObj, ost1);
            hmRemains2.put(idObj, ost2);
            hmRemains3.put(idObj, ost3);
            hmRemains4.put(idObj, ost4);

            //measuringpoint
        }

    }

    /**
     * Создает карту кодов Пирамиды по объектам для протокола 80020 Код
     * котроллера должен быть тип СТРОКА а код точки учета тип ЧИСЛО коды
     * Силеста должны быть типом ЧИСЛО
     */
    private void createPiramidaCodes() {

        try {
            Workbook wb = Piramida.OpenWb();

            hmCodesPiramida = new HashMap<Integer, String>();

            String sql = "";
            Name nameCode = wb.getName("kodSilesta");

            AreaReference ar = new AreaReference(nameCode.getRefersToFormula());
            CellReference cf = ar.getFirstCell();

            // Столбец  кодов Силесты
            int colCod = cf.getCol();

            Name nameCodePiramid = wb.getName("kodPiramida");

            ar = new AreaReference(nameCodePiramid.getRefersToFormula());
            cf = ar.getFirstCell();

            int rowParam = cf.getRow();

            // Столбец кодов Пирамиды
            int colPiram = cf.getCol();

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            for (int sheetNum = 0; sheetNum
                    < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);

                refreshBarValue("Создаем карту кодов 'Пирамиды'");

                setMinMaxValue(0, sheet.getLastRowNum());

                //  Текущщий код пирамиды
                String CurrenCodPiramida = null;

                // текущий код объекта
                String CurrentCodObject = null;

                // Код объекта
                Double kodObj = 0.0;
                /* Колонка кодов  объектов Пирамиды
                 если тип строка то текущий код
                 */

                // Проходим строчки
                for (Row row : sheet) {

                    int ir = row.getRowNum();

                    refreshBarValue(ir);

                    // Колонка кодов  объектов Силесты должно быть число
                    Cell cell = row.getCell(colCod);

                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK
                            || cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {

                        continue;

                    }

                    Cell cellPiram = row.getCell(colPiram);

                    if (cellPiram == null || cellPiram.getCellType() == Cell.CELL_TYPE_BLANK) {
                        continue;

                    } // Проверяем тип

                    if (cellPiram.getCellType() == Cell.CELL_TYPE_STRING) {
                        // Код контроллера
                        CurrenCodPiramida = cellPiram.getStringCellValue();

                    }

                    if (cellPiram.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        // Код Объекта
                        kodObj = cellPiram.getNumericCellValue();

                        Integer i = kodObj.intValue();

                        CurrentCodObject = CurrenCodPiramida + ".98." + i;

                    }

                    try {
                        int iCod = (int) cell.getNumericCellValue();

                        hmCodesPiramida.put(iCod, CurrentCodObject);

                    } catch (NumberFormatException e) {
                    }

                }
            }
        } catch (IOException ex) {

            setNotifyObservers(ex);

        }
    }

    private void sendMailZip() {

        File file;

        try {

            if (zipCreator == null) {
                return;
            }
            try {
                file = zipCreator.getZipFile();
            } catch (IOException ex) {
                setLoggerInfo("", ex);
                return;
            }

            String is_mail = (String) mapProperties.get("is_mail");

            if (is_mail.equals("1")) {

                String s = "Отправка данных на адрес " + mail_addres + "...";

                blinkText(s);
                setNotifyObservers(s);
                try {
                    file = zipCreator.getZipFile();
                    MailClass.goMailApathe(mail_addres, "80020", "Инспектор", file, null);
                    setNotifyObservers(file.getName() + " на адрес " + mail_addres + " отправлен.");

                } catch (Exception ex) {
                    setLoggerInfo(s, ex);
                }

            }

        } finally {
            stopBlinkText();
        }

    }

    /**
     * Экспорт в пирамиду по протоколу 80020 только профиль мощности за день
     */
    private void createProtocol(DateTime timeSend, ResultSet rsObjects, File fileDir) throws Exception {
        // Перебираем все Объекты
        String value = null;
        //  if (hmCodesPiramida == null) {
        //    createPiramidaCodes();
        // }
        document = XmlTask.getNewDocument();
        refreshBarValue("Разбор файла...");

        // ГГГГММДД  yyyyMMddHHmmss
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        DateTime dateTime = new DateTime();
        // dateTimeSend = dateTime.minusDays(1);
        String sd = timeSend.toString(dtf);
        String innOrg = (String) mapProperties.get("inn_subconto");

        String nameFile = "80020_" + sd + "_" + innOrg + "_" + count_80020 + ".xml";

        File file = new File(fileDir, nameFile);
        Element elMessage = document.createElement("message");
        elMessage.setAttribute("class", "80020");
        elMessage.setAttribute("version", "2");
        elMessage.setAttribute("number", "" + count_80020);
        document.appendChild(elMessage);
        Element elDatetime = document.createElement("datetime");
        Element el_timestamp = document.createElement("timestamp");
        dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        sd = dateTime.toString(dtf);
        el_timestamp.setTextContent(sd);
        Element el_daylightsavingtime = document.createElement("daylightsavingtime");
        el_daylightsavingtime.setTextContent("1");
        Element el_day = document.createElement("day");
        dtf = DateTimeFormat.forPattern("yyyyMMdd");
        sd = timeSend.toString(dtf);
        el_day.setTextContent(sd);
        elDatetime.appendChild(el_timestamp);
        elDatetime.appendChild(el_daylightsavingtime);
        elDatetime.appendChild(el_day);
        //*************comment************
        //  Element elComment = document.createElement("comment");
        //*************sender************
        Element elSender = document.createElement("sender");
        Element el_inn = document.createElement("inn");
        value = (String) mapProperties.get("inn_subconto");
        el_inn.setTextContent(value);
        Element el_name = document.createElement("name");
        value = (String) mapProperties.get("name_subconto");
        el_name.setTextContent(value);
        elSender.appendChild(el_inn);
        elSender.appendChild(el_name);
        elMessage.appendChild(elDatetime);
        elMessage.appendChild(elSender);

        Integer timezone_80200 = (Integer) mapProperties.get("zone_subconto");

        String inn_80200 = (String) mapProperties.get("inn_subconto");

        String name_80200 = (String) mapProperties.get("name_subconto");

        if (timezone_80200 == null) {
            timezone_80200 = 1;
        }

        Element elArea = document.createElement("area");
        elArea.setAttribute("timezone", timezone_80200.toString());

        Element el_innArea = document.createElement("inn");
        el_innArea.setTextContent(inn_80200);
        Element el_nameArea = document.createElement("name");
        el_nameArea.setTextContent(name_80200);
        elArea.appendChild(el_innArea);
        elArea.appendChild(el_nameArea);

        elMessage.appendChild(elArea);

        //     HashMap<String, Integer> hmColNames;
        //     try {
        //         hmColNames = Work.getNameColValueByNameTable(nameTable);
        // Запрашиваем все объекты  экспортируемые
        refreshBarValue("Запрос значений...");
        // rsObjects = Work.getResSetByNameSql(groupSub);
        createValuesProtokol(rsObjects, elArea, timeSend);
        //   } catch (SQLException ex) {
        //   setNotifyObservers(ex);
        // } //   String nameXml = null;
        //   XmlParsing.saveXmlDocument(document, nameXml);
        //   String nameXml = null;
        //   XmlParsing.saveXmlDocument(document, nameXml);
        refreshBarValue("...");

        count_80020++;
        try {

            try {

                Boolean is_zip = true;

                ByteArrayOutputStream byteStream = MailClass.getByteArrayOutputStream(document);

                file = MailClass.createFile(file, byteStream);

                if (is_zip) {

                    if (zipCreator == null) {

                        String nameZip = "";

                        String nInn = (String) mapProperties.get("inn_subconto");
                        String nDog = (String) mapProperties.get("ens_subconto");

                        dtf = DateTimeFormat.forPattern("yyyy");
                        String sg = timeSend.toString(dtf);

                        dtf = DateTimeFormat.forPattern("MM");
                        String sm = timeSend.toString(dtf);

                        if (nDog == null || nDog.isEmpty()) {
                            nDog = "80020";
                        }

                        nameZip = nInn + "_" + nDog + "_" + sm + "_" + sg + ".zip";

                        zipCreator = new ZipCreator(file.getParent(), nameZip);

                        zipCreator.addFileToZip(file);
                    } else {

                        zipCreator.addFileToZip(file);

                    }

                }

                byteStream.close();

                //    Boolean is_mail = (Boolean) mapProperties.get("is_mail");
                //  if (is_mail && zipCreator == null) {
                //    String s = "Отправка данных на адрес " + mail_addres + "...";
                //  blinkText(s);
                //  setNotifyObservers(s);
                //  MailClass.goMailApathe(mail_addres, "80020", "Инспектор", file, null, alErrors);
                // setNotifyObservers("Файл 'P80020.zip' на адрес " + mail_addres + " отправлен.");
                //   }//
            } finally {
                stopBlinkText();
            }

        } catch (Exception e) {
            setNotifyObservers(e);
        }
    }

    /**
     * Экспорт в пирамиду по протоколу 80020 только профиль мощности за день
     */
    private void goMoveProtocol(DateTime timeSend) throws Exception {
        ResultSet rsObjects = null;
        // Перебираем все Объекты
        String value = null;
        //  if (hmCodesPiramida == null) {
        //    createPiramidaCodes();
        // }
        document = XmlTask.getNewDocument();
        refreshBarValue("Разбор файла...");

        // ГГГГММДД  yyyyMMddHHmmss
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        DateTime dateTime = new DateTime();
        // dateTimeSend = dateTime.minusDays(1);
        String sd = timeSend.toString(dtf);
        String innOrg = (String) mapProperties.get("inn_organiz");
        String nameFile = "80020_" + sd + "_" + innOrg + "_" + count_80020 + ".xml";
        String path = null;
        String dir = (String) mapProperties.get("folder_80020");
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        file = new File(file, nameFile);
        Element elMessage = document.createElement("message");
        elMessage.setAttribute("class", "80020");
        elMessage.setAttribute("version", "2");
        elMessage.setAttribute("number", "" + count_80020);
        document.appendChild(elMessage);
        Element elDatetime = document.createElement("datetime");
        Element el_timestamp = document.createElement("timestamp");
        dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        sd = dateTime.toString(dtf);
        el_timestamp.setTextContent(sd);
        Element el_daylightsavingtime = document.createElement("daylightsavingtime");
        el_daylightsavingtime.setTextContent("1");
        Element el_day = document.createElement("day");
        dtf = DateTimeFormat.forPattern("yyyyMMdd");
        sd = timeSend.toString(dtf);
        el_day.setTextContent(sd);
        elDatetime.appendChild(el_timestamp);
        elDatetime.appendChild(el_daylightsavingtime);
        elDatetime.appendChild(el_day);
        //*************comment************
        //  Element elComment = document.createElement("comment");
        //*************sender************
        Element elSender = document.createElement("sender");
        Element el_inn = document.createElement("inn");
        value = (String) mapProperties.get("inn_80020");
        el_inn.setTextContent(value);
        Element el_name = document.createElement("name");
        value = (String) mapProperties.get("name_80020");
        el_name.setTextContent(value);
        elSender.appendChild(el_inn);
        elSender.appendChild(el_name);
        elMessage.appendChild(elDatetime);
        elMessage.appendChild(elSender);

        String timezone_80200 = (String) mapProperties.get("timezone");

        String inn_80200 = (String) mapProperties.get("inn_80020");

        String name_80200 = (String) mapProperties.get("name_80020");

        String dog_80200 = (String) mapProperties.get("dog_80020");

        if (timezone_80200 == null || timezone_80200.isEmpty()) {
            timezone_80200 = "1";
        }

        Element elArea = document.createElement("area");
        elArea.setAttribute("timezone", timezone_80200);

        Element el_innArea = document.createElement("inn");
        el_innArea.setTextContent(inn_80200);
        Element el_nameArea = document.createElement("name");
        el_nameArea.setTextContent(name_80200);
        elArea.appendChild(el_innArea);
        elArea.appendChild(el_nameArea);

        elMessage.appendChild(elArea);

        //     HashMap<String, Integer> hmColNames;
        try {
            //         hmColNames = Work.getNameColValueByNameTable(nameTable);
            // Запрашиваем все объекты  экспортируемые

            refreshBarValue("Запрос значений...");
            rsObjects = SqlTask.getResultSet(null, currentSQL);

            createSendFileProtokol(rsObjects, elArea, timeSend);

        } catch (SQLException ex) {
            setNotifyObservers(ex);
        } //   String nameXml = null;
        //   XmlParsing.saveXmlDocument(document, nameXml);
        //   String nameXml = null;
        //   XmlParsing.saveXmlDocument(document, nameXml);
        refreshBarValue("...");

        count_80020++;
        try {

            try {

                //  Boolean is_zip = (Boolean) mapProperties.get("zip_file");
                ByteArrayOutputStream byteStream = MailClass.getByteArrayOutputStream(document);

                file = MailClass.createFile(file, byteStream);

                //  if (is_zip) {
                if (zipCreator == null) {

                    String nameZip = "";

                    dtf = DateTimeFormat.forPattern("yyyy");
                    String sg = timeSend.toString(dtf);

                    dtf = DateTimeFormat.forPattern("MM");
                    String sm = timeSend.toString(dtf);

                    if (dog_80200 == null || dog_80200.isEmpty()) {
                        dog_80200 = "80020";
                    }

                    nameZip = inn_80200 + "_" + dog_80200 + "_" + sm + "_" + sg + ".zip";

                    zipCreator = new ZipCreator(file.getParent(), nameZip);

                    zipCreator.addFileToZip(file);
                } else {

                    zipCreator.addFileToZip(file);

                }

                //    }
                byteStream.close();

                //    Boolean is_mail = (Boolean) mapProperties.get("is_mail");
                //  if (is_mail && zipCreator == null) {
                //    String s = "Отправка данных на адрес " + mail_addres + "...";
                //  blinkText(s);
                //setNotifyObservers(s);
                //MailClass.goMailApathe(mail_addres, "80020", "Инспектор", file, null, alErrors);
                //setNotifyObservers("Файл 'P80020.zip' на адрес " + mail_addres + " отправлен.");
                // }
            } finally {
                stopBlinkText();
            }

        } catch (Exception e) {
            setNotifyObservers(e);
        }
    }

    /**
     * удаляем все файлы из папки
     *
     * @param path путь к папке
     */
    public static void deleteAllFilesFolder(String path) {
        for (File myFile : new File(path).listFiles()) {
            if (myFile.isFile()) {
                myFile.delete();
            }
        }
    }

    private void closeResultSets() throws SQLException {

        if (alWorking != null) {
            for (ResultSet rs : alWorking) {
                rs.getStatement().close();
            }
        }

    }

    private void createReport() {

        String dir = (String) mapProperties.get("folder_80020");
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        file = new File(file, "report.html");

        for (Integer i : hmCaptions.keySet()) {

            String name = hmCaptions.get(i);

            Integer ap = hmSum1.get(i);
            Integer am = hmSum2.get(i);
            Integer rp = hmSum3.get(i);
            Integer rm = hmSum4.get(i);

            String val = name + ";" + ap + ";" + am + ";" + rp + ";" + rm;
            report.add(val);

        }

        ArrayList alCap = new ArrayList();

        alCap.add("Наименование");
        alCap.add("A+");
        alCap.add("A-");
        alCap.add("R+");
        alCap.add("R-");

        DateTimeFormatter dtff = DateTimeFormat.forPattern("dd.MM.yyyy");
        String df = dtFirst.toString(dtff);
        String dl = dtLast.toString(dtff);

        String ctab = "Суммарные показатели профиля мощности с " + df + " по " + dl;

        //  GroovyTestClass.
        // GroovyTestClass.createHtmlByBilder(errors, ctab, alCap, report, file);
    }

    public File getMaket(Map hmParam) throws Exception {

        mapProperties = hmParam;

        File file = null;

        groupSub = (Integer) hmParam.get("group_subconto");

        Integer iPeriod = (Integer) hmParam.get("period");

        if (iPeriod == null) {
            iPeriod = -1;// по умолчанию за предыдущий месяц
        }


        String dir;
        dir = (String) mapProperties.get("folder_80020");

        File fileDir;


        if (dir == null || dir.isEmpty()) {
            dir = System.getProperty("user.dir");
            fileDir = new File(dir, "80020");
            fileDir.mkdir();


        } else {

            fileDir = new File(dir);
        }


        // Предварительно очищаем  

        if (fileDir.toString().contains("80020")) {
            deleteAllFilesFolder(fileDir.getPath());
        }

        ArrayList<DateTime> alDates = MathTrans.getDatesByMonth(iPeriod);

        DateTime dt = new DateTime();

        dtFirst = alDates.get(0);

        dtLast = alDates.get(1);

        Days days = Days.daysBetween(dtFirst, dtLast.minusDays(1));
        int countDays = days.getDays();
        DateTime dtCurrent;
        report = new ArrayList();
        hmCaptions = new HashMap();
        errors = new ArrayList();
        hmRemains1.clear();
        hmRemains2.clear();
        hmRemains3.clear();
        hmRemains4.clear();
        hmSum1.clear();
        hmSum2.clear();
        hmSum3.clear();
        hmSum4.clear();

        ResultSet resultSet = Work.getResSetByNameSql(groupSub);

        int row = 0;
        zipCreator = null;
        try {

            resultSet.last();
            int rows = resultSet.getRow();
            setMinMaxValue(0, rows);

            for (int i = 0; i <= countDays; i++) {
                dtCurrent = dtFirst.plusDays(i);
                try {
                    createProtocol(dtCurrent, resultSet, fileDir);
                    refreshBarValue(row);
                    row++;
                } catch (Exception ex) {
                    setLoggerInfo("", ex);
                }
            }

        } finally {
            resultSet.close();
        }

        file = zipCreator.getZipFile();

        return file;

    }

    private void Refresh80020Data() {
        Days days = Days.daysBetween(dtFirst, dtLast);
        int countDays = days.getDays();
        DateTime dtCurrent;
        report = new ArrayList<String>();
        hmCaptions = new HashMap<Integer, String>();
        errors = new ArrayList<String>();
        hmRemains1.clear();
        hmRemains2.clear();
        hmRemains3.clear();
        hmRemains4.clear();
        hmSum1.clear();
        hmSum2.clear();
        hmSum3.clear();
        hmSum4.clear();

        for (int i = 0; i <= countDays; i++) {
            dtCurrent = dtFirst.plusDays(i);
            try {
                goMoveProtocol(dtCurrent);
            } catch (Exception ex) {
                setLoggerInfo("", ex);
            }
        }
// Отправляем ZIP файл
        sendMailZip();

        // Создаем отчет
        createReport();

    }

    @Override
    public void doProcess() {

        errorString = null;

        newProcess(currentTask);
        try {

            if (currentTask.equals(TASK_CREATE_80020)) {

                getMaket(mapProperties);

            }
        } catch (Exception ex) {
            MainWorker.setLogInfo("Ошибка", ex);
            errorString = "Ошибка !";
        }

    }
}

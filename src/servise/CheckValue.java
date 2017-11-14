/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xmldom.XmlTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.esStatus.esCheckValue;
import static constatant_static.SettingActions.esStatus.isSetStatus;

/**
 * Проверка превышений поступивших параметров
 *
 * @author 1
 */
public class CheckValue extends MainWorker {

    private Integer idSchedule;
    private String nameSchedule;
    private HashMap<Integer, Document> hmUsers; // Карта всех пользователей
    private ResultSet rsOver;// Превышения расписания
    public boolean OverOk;

    public CheckValue(ExecutorService pool) {

        this.pool = pool;
        hmUsers = new HashMap<Integer, Document>();
    }

    @Override
    public void update(Observable o, Object arg) {




        if (o instanceof ScheduleClass && !isSetStatus(esCheckValue) && arg instanceof Integer) {
            // Сработало расписание
            idSchedule = (Integer) arg;

            ScheduleClass sc = (ScheduleClass) o;

            nameSchedule = sc.getNameSchedule();
            try {
                if (findEvents(idSchedule)) {

                    executeProcess();
                }
            } catch (SQLException ex) {
                setNotifyObservers(ex);
            }




        }

    }

    @Override
    public void doProcess() {
        try {

            try {

                String ss = nameSchedule;
                newProcess(ss);
                setNotifyObservers(ss);

                hmUsers.clear();
                try {
                    createEventsObject(rsOver);
                } catch (Exception ex) {
                    setLoggerInfo("Создание уставок", ex);
                }
                sendOver();
                System.gc();
                setNotifyObservers("Проверка уставок закончена.");


            } finally {
                rsOver.getStatement().close();
            }



        } catch (SQLException ex) {
            setNotifyObservers(ex);
        }


    }

    /**
     * посылка превышений
     *
     */
    public synchronized void sendOver() {


        Document docUser;

        blinkText("Посылка сообщений по превышениям...");

        Map<String, String> hmprop;

        for (Integer idUser : hmUsers.keySet()) {
            try {

                try {
                    docUser = hmUsers.get(idUser);


                    hmprop = XmlTask.getMapAttrubuteByName(docUser.getDocumentElement(),
                            "name", "value", "column");
                    String mailUser = hmprop.get("email");
                    // Имя пользователя
                    String user = hmprop.get("name_user");

                    setNotifyObservers("Превышения:отправка уведомлений(" + user + ", " + mailUser + ")");


                 //   OverJurnal.addOver(docUser, alErrors);
                   // MailClass.sendMail(docUser, alErrors);

                } finally {

                    stopBlinkText();


                }


            } catch (Exception ex) {

               MainWorker.setLogInfo("", ex); 
            }


        }


    }

// фиксируем статус даных по проверяемому объекту
    private void doFix(boolean bStart, Integer idObject) {
        try {
            ArrayList<String> alNameTables = Work.getAllNameTablesByTyp(15);
            HashMap<String, Object> hmUpdate = new HashMap<String, Object>();
            String sql = "";
            Integer check = 0;
            if (bStart) {
                // меняем статус не проверенных  данных(0)   на проверяемые(2)
                // Update enegry_data SET is_check=(is_check | 4) WHERE (is_check & 1)=0 AND object_id=11
                sql = "WHERE is_check=0 AND id_object=" + idObject;
                check = 2;
            } else {
                // Update enegry_data SET is_check=(is_check | 1) WHERE (is_check & 4)=4 AND object_id=11
                // меняем статус  проверяемых   данных(2)   на проверенные(1)
                sql = "WHERE is_check=2 AND id_object=" + idObject;
                check = 1;
            }
            hmUpdate.put("is_check", check);
            for (String nameTable : alNameTables) {
                try {
                    SqlTask.updateRecInTable(null, nameTable, sql, hmUpdate);
                } catch (Exception ex) {
                    setNotifyObservers(ex);
                }
            }
        } catch (SQLException ex) {
            setNotifyObservers(ex);
        }
    }

    private void addEventsInDocUser(Document doc_user, HashMap<String, HashMap<String, Object>> hmEvents, String evnts, Integer notise) {
        try {

            ResultSet rsEvents;


            for (String nameValue : hmEvents.keySet()) {

                HashMap<String, Object> hmValue = hmEvents.get(nameValue);
                rsEvents = (ResultSet) hmValue.get("rs");
                rsEvents.last();

                int count = rsEvents.getRow();

                if (count <= 0) {
                    continue;
                }

                rsEvents.beforeFirst();

                //  Объект  Дата  Параметр  Значение Превышение


                while (rsEvents.next()) {
                    Element elm = doc_user.createElement("events");
                    elm.setAttribute("notise", notise.toString());


                    doc_user.getDocumentElement().appendChild(elm);

                    // Объект

                    int idObject = rsEvents.getInt("id_object");


                    // id
                    elm.setAttribute("id_object", "" + idObject);


                    // Название



                    String ObjName = (String) Work.getParametersRow(idObject, null, "objects", false, true).get("caption");
                    elm.setAttribute("object_name", ObjName);


                    //  DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");



                    // Дата

                    DateTime d_value;

                    Object obj = rsEvents.getObject("value_date");

                    d_value = new DateTime(obj);
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
                    String dateValue = d_value.toString(dtf);


                    //   SimpleDateFormat formatter2 = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                    // String dateValue = formatter2.format(obj);
                    elm.setAttribute("c_value_date", dateValue);

                    // Название параметра


                    String captionPar = (String) hmValue.get("caption");
                    elm.setAttribute("c_partype_name", captionPar);




                    // Значение


                    String namePar = (String) hmValue.get("name");
                    Double dValue = rsEvents.getDouble(namePar);

                    elm.setAttribute("value", dValue.toString());


                    // Превышение

                    elm.setAttribute("over_value", evnts);


                }


                rsEvents.getStatement().close();
            }



        } catch (SQLException ex) {

            setNotifyObservers(ex);
            return;
        }



    }

    private HashMap getRsEvents(int obj, String evn) {


        HashMap<String, HashMap<String, Object>> hmResult = new HashMap<String, HashMap<String, Object>>();
        ResultSet result = null;
        StringBuilder sbSql;
        String evnRepl = "";

        HashMap<String, HashMap<String, String>> hmOver = new HashMap<String, HashMap<String, String>>();

        HashMap<String, String> hmParStr;


        try {
            String sql = "";
            ResultSet rsPar = null;

            sql = "SELECT c_partype_id, c_partype_name, over_string,name_table,prm_name_tbl FROM c_parnumber WHERE over_string<>''";
            rsPar = SqlTask.getResultSet(null, sql);


            try {

                while (rsPar.next()) {

                    String parOver = rsPar.getString("over_string").trim();
                    Integer parId = rsPar.getInt("c_partype_id");
                    String nameTable = rsPar.getString("name_table");
                    String nameValue = rsPar.getString("prm_name_tbl");
                    String captionValue = rsPar.getString("c_partype_name");





                    if (!Work.isNumber(parOver) && evn.indexOf(parOver) != -1) {



                        if (hmOver.containsKey(nameTable)) {

                            hmParStr = hmOver.get(nameTable);



                        } else {

                            hmParStr = new HashMap<String, String>();

                            hmOver.put(nameTable, hmParStr);
                        }

                        evnRepl = evn.replace(parOver, nameValue);
                        // Меняем запятые на точки

                        evnRepl = evnRepl.replaceAll(",", ".");

                        hmParStr.put(evnRepl, captionValue + "//" + nameValue);

                    }
                }

            } finally {
                rsPar.getStatement().close();
            }

// Формируем запрос

            for (String nTab : hmOver.keySet()) {

                hmParStr = hmOver.get(nTab);
                sbSql = new StringBuilder();

                sbSql.append("SELECT * FROM " + nTab + " WHERE  id_object=" + obj);
                sbSql.append(" AND ");
                sbSql.append("is_check=2");

                sbSql.append(" AND ");


                int poz = sbSql.length();

                for (String sOwer : hmParStr.keySet()) {

                    String[] names = hmParStr.get(sOwer).split("//");


                    sbSql.append(sOwer);

                    sql = sbSql.toString();


                    result = SqlTask.getResultSet(null, sql);


                    if (result.next()) {

                        HashMap<String, Object> hmValue = new HashMap<String, Object>();
                        hmValue.put("name", names[1]);
                        hmValue.put("caption", names[0]);
                        hmValue.put("table", nTab);
                        hmValue.put("rs", result);
                        hmResult.put(names[1], hmValue);
                    }

                    sbSql.delete(poz, sbSql.length());

                }
            }
        } catch (SQLException ex) {

            setNotifyObservers(ex);

        }
        return hmResult;

    }

    /**
     * Создает листинг Объектов по отслеживанию событий rs-расписание
     */
    public void createEventsObject(ResultSet rsThis) throws Exception {

        Document docUser;

        ResultSet rs = null;
        Integer idUser; //Пользователь
        Integer idObject;// Объект контроля
        String prObject;// параметры контроля
        Integer notise;// Куда посылать превышения

        String evnReplace = "";

        hmUsers.clear();

        rsThis.last();

        int countEve = rsThis.getRow();


        setMinMaxValue(0, countEve);

        refreshBarValue("Обработка уставок...");


        rsThis.beforeFirst();





        while (rsThis.next()) {
            //  пользователь

            refreshBarValue(rsThis.getRow());


            idUser = rsThis.getInt("id_parent");
            idObject = rsThis.getInt("id_object");
            prObject = rsThis.getString("pr_object");
            notise = rsThis.getInt("notise");


            if (idObject == null || prObject == null) {

               // listError.add("По пользователю " + idUser + " не заполнены параметры контроля объектов !");
                continue;
            }


            // Фиксируем данные
            doFix(true, idObject);

            if (!hmUsers.containsKey(idUser)) {

                // Создаем новый Документ-пользователь
                docUser = Work.getXMLDocByIdObject(idUser);

                hmUsers.put(idUser, docUser);

            } else {
                // Уже есть
                docUser = hmUsers.get(idUser);

            }


            HashMap<String, Object> hmObjectProp = Work.getParametersRow(idObject, null, "objects", true, true);


            if (hmObjectProp == null) {
                continue;
            }



            String[] par = prObject.split(";");

            for (int x = 0; x < par.length; x++) {

                String s_events = par[x].trim();
                evnReplace = s_events;


                // Проверяем коэффициенты

                for (String sColum : hmObjectProp.keySet()) {

                    if (s_events.indexOf(sColum) != -1) {

                        Object valCol = hmObjectProp.get(sColum);

                        String sVal = "1";

                        if (valCol instanceof Integer) {

                            Integer iVal = (Integer) valCol;
                            sVal = iVal.toString();
                        }
                        evnReplace = evnReplace.replace(sColum, sVal);


                    }


                }





                HashMap hm = getRsEvents(idObject, evnReplace);

                // Есть превышения
                if (hm != null && hm.size() > 0) {

                    // Добавляем
                    addEventsInDocUser(docUser, hm, evnReplace, notise);


                }


            }

            // конец фиксации
            doFix(false, idObject);

        }

        refreshBarValue("Конец проверк объектов.");



    }

// Ищем события , у которых  тип контроля объект или группа объектов
    public boolean findEvents(Integer idSchedule) throws SQLException {




        String sql = "SELECT * FROM events WHERE shedule=" + idSchedule;


        rsOver = SqlTask.getResultSet(null, sql);


        if (rsOver.next()) {

            return true;
        } else {

            rsOver.getStatement().close();
            return false;
        }


    }
}

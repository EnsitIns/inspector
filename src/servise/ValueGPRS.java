package servise;

import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xmldom.XmlTask;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.esStatus.*;

public class ValueGPRS extends MainWorker {

    private Integer id_schedule; // id Расписания
    private File f_folder; // Папка с файлами
    private String nameSchedule;// Имя расписания
    private HashMap<Integer, HashMap<Integer, Integer>> hmKeysObject;
    private HashMap<String, ArrayList<String>> hmColTables;

    private void setParameters() throws Exception {

        id_schedule = -1;

        Document docData = null;
        docData = Work.getXmlDocFromConst("server");

        String id_sched = null; // Папка с данными

        if (docData == null) {
            return;
        }

        Element e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "shedule_gprs", "cell");
        id_sched = e.getAttribute("value");

        try {
            id_schedule = Integer.parseInt(id_sched);

        } catch (NumberFormatException ex) {

            setNotifyObservers(ex);

            id_schedule = -1;
        }

        e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "folder_gprs", "cell");

        String folder = e.getAttribute("value");

        f_folder = new File(folder);

    }

    public ValueGPRS(ExecutorService pool) {

        this.pool = pool;
        hmColTables = new HashMap<String, ArrayList<String>>();

    }

    /**
     * Проверка поступления данных по GPRS
     *
     */
    public void CheckDataGprs() {
        String folder = null; // Папка с данными

        if (f_folder == null || !f_folder.exists()) {

            setLoggerInfo("Папки " + folder + " не существует !", null);
            excludeStatus(esGPRSOn);
            return;
        }

        File[] files = f_folder.listFiles();

        blinkText("Запрос кодов объектов...");

        if (files.length > 0) {

            hmKeysObject = createMap();

        } else {
            // папка пустая
            excludeStatus(esGPRSOn);
            return;
        }

        stopBlinkText();

        if (hmKeysObject != null) {
            // Ошибок нет
            String s = "Загрузка данных :" + f_folder.getAbsolutePath() + "(" + files.length + ")";

            refreshBarValue(s);
            setNotifyObservers(s);

            for (File f : files) {

                if (f.isFile()) {

                    if (addGprsData(f)) {
                        f.delete();
                    }
                }
            }
        }

    }

    public boolean addGprsData(File f) {

        boolean result = false;
        Integer idObj = null;
        String w_device_code; // Адрес контроллера
        String w_data_sub_device_addr; // Номер счетчика
        String w_data_data_type_id; // Тип данных
        String strValue;  // Массив данных

        ArrayList<HashMap<String, String>> alAtt = new ArrayList<HashMap<String, String>>();

        try {

            XmlTask.getValuesNodeBySax(f, alAtt, "value");

            if (alAtt.isEmpty()) {

                return true;
            }

            setMinMaxValue(0, alAtt.size());
            refreshBarValue(f.getAbsolutePath());

            for (HashMap<String, String> hm : alAtt) {

                refreshBarValue(alAtt.indexOf(hm));

                // id контроллера
                w_device_code = hm.get("w_device_code");

                // адрес счетчика
                w_data_sub_device_addr = hm.get("w_data_sub_device_addr");

                // тип параметра
                w_data_data_type_id = hm.get("w_data_data_type_id");

                // Строковое значение
                strValue = hm.get("strValue");

                try {
                    Integer id_cnt = Integer.parseInt(w_device_code);
                    Integer id_count = Integer.parseInt(w_data_sub_device_addr);

                    HashMap<Integer, Integer> hmKeys = hmKeysObject.get(id_cnt);

                    if (hmKeys != null) {

                        idObj = hmKeys.get(id_count);

                        if (idObj != null) {

                            addDataByXml(idObj, w_data_data_type_id, strValue);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    setNotifyObservers("id контроллера=" + w_device_code);
                    setNotifyObservers("id счетчика=" + w_data_sub_device_addr);

                    continue;
                }

            }

            result = true;

        } catch (Exception e) {
            result = false;

            setLoggerInfo("", e);

        }

        return result;

    }

    public static HashMap<Integer, HashMap<Integer, Integer>> createMap() {

        int i = 0;
        int j = 0;
        int x = 0;

        //ArrayList<HashMap<String, Object>> alError = null;
        HashMap<Integer, HashMap<Integer, Integer>> result = new HashMap<Integer, HashMap<Integer, Integer>>();

        HashMap<Integer, Integer> hmId;

        //точка потключения
        HashMap<String, Object> hmPoint = null; // точки подключения

        String namePoint;

        // точки
        ArrayList<HashMap<String, Object>> alPoint = null;

        // Object object = Work.getParametersTableByType(4);
        // if (object instanceof HashMap) {
        //   alPoint = new ArrayList<HashMap<String, Object>>();
        // alPoint.add((HashMap<String, Object>) object);
        // }
        // Объекты
        HashMap<String, Object> hmObject = null; // точки подключения

        String nameObject;
        ArrayList<HashMap<String, Object>> alObject = null;
        //    object = Work.getParametersTableByType(1);

        //  if (object instanceof HashMap) {
        //    alObject = new ArrayList<HashMap<String, Object>>();
        //  alObject.add((HashMap<String, Object>) object);
        // }
        Integer idPoint = null;
        Integer idController = null;
        try {

            // Обрабатываем контроллеры
            try {

                //   hmPoint = hm;
                // namePoint = (String) hmPoint.get(Work.TABLE_NAME);
                String sql = "SELECT c_tree_id,sub_type4  FROM controllers";

                ResultSet rsPoint = SqlTask.getResultSet(null, sql);

                try {
                    while (rsPoint.next()) {

                        idPoint = rsPoint.getInt("sub_type4");
                        idController = rsPoint.getInt("c_tree_id");

                        // Добавляем контроллер
                        if (result.containsKey(idController)) {
                            // Уже есть

                            hmId = result.get(idController);

                        } else {
                            // еще нет
                            hmId = new HashMap<Integer, Integer>();

                            if (idController != null && idController > -1) {
                                result.put(idController, hmId);
                            }
                        }

                        // Обработка объектов
                        j++;
                        //  hmObject = hmObj;
                        nameObject = "objects";

                        //   Integer idTable = (Integer) hmObject.get(Work.TABLE_ID);
                        sql = "SELECT c_tree_id,sub_type6  FROM " + nameObject + " WHERE sub_type4=" + idPoint;
                        ResultSet rsObject = SqlTask.getResultSet(null, sql);

                        String caption;

                        try {
                            while (rsObject.next()) {

                                x++;
                                Integer idObject = rsObject.getInt("c_tree_id");
                                Integer idCounter = rsObject.getInt("sub_type6");

                                if (idCounter == null || idCounter == -1) {

                                    MainWorker.setLogInfo("Для объекта [" + idObject + "] нет прибора учета !", null);

                                    continue;
                                }

                                HashMap<String, Object> hmCounters = Work.getParametersRow(idObject, null, "objects", true, false);

                                Integer counter = null;
                                if (hmCounters != null) {
                                    counter = (Integer) hmCounters.get("counter_addres");
                                }

                                if (counter != null) {

                                    if (hmId.containsKey(counter)) {

                                        int dblId = hmId.get(counter);
                                        MainWorker.setLogInfo("" + idObject + ": дублируется сет. адрес прибора учета !(" + dblId + ")", null);
                                        continue;

                                    } else {
                                        hmId.put((Integer) counter, idObject);
                                    }

                                } else {

                                    MainWorker.setLogInfo("Для объекта [" + idObject + "] нет сетевого адреса !", null);
                                    continue;
                                }
                                x++;
                            }

                        } finally {
                            rsObject.close();
                        }
                    }

                } finally {
                    rsPoint.close();
                }
                i++;
            } catch (SQLException ex) {

                MainWorker.setLogInfo("", ex);
            }
        } catch (Exception e) {

            MainWorker.setLogInfo("", e);
        }
        return result;
    }

    /**
     * Возвращает id Объекта по номеру контоллера и номеру счетчика
     *
     */
    public Integer getIdObject(int idController, int nCounter) {

        String namePoint = "points";

        String nameObject = "objects";

        StringBuffer sb = new StringBuffer();

        sb.append("SELECT a2.c_tree_id  FROM " + namePoint + " as a1," + nameObject + " as a2   WHERE   a1.sub_type5=");
        sb.append(idController);
        sb.append(" AND a2.sub_type4=a1.c_tree_id AND a2.counter_addres=");
        sb.append(nCounter);

        String sql = sb.toString();
        ResultSet rs;
        Integer result = null;

        try {

            rs = SqlTask.getResultSet(null, sql);

            // PreparedStatement statement=Work.conn.prepareStatement(sql);
            try {

                if (rs.next()) {
                    result = rs.getInt(1);
                }
            } finally {
                rs.close();
            }

        } catch (SQLException ex) {

            setNotifyObservers(ex);

        }

        return result;
    }

    /**
     * Добавляет данные поступившие по GPRS
     *
     */
    public void addDataByXml(Integer idObject, String TypData, String sValues) {

        String sql = "";
        ResultSet rs;
        HashMap<String, HashMap<String, Object>> hmInsInTab = new HashMap<String, HashMap<String, Object>>();
        Double dv;

        ArrayList<String> alCol;
        HashMap<String, Integer> hmColTable = null;

        String fullName;

        HashMap<String, Object> hmInsert;

        String[] Val = sValues.split(";");

        String sDate = Val[0];
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        DateTime d_value;

        try {

            d_value = dtf.parseDateTime(sDate);

        } catch (Exception e) {

            setNotifyObservers(e);

            return;
        }

        sql = "SELECT prm_name_tbl,name_table,c_partype_id,prm_tarif   FROM c_parnumber WHERE  c_w_type=? AND c_w_number=?";
        try {
            PreparedStatement statement = Work.CONNECT_DBF.prepareStatement(sql);

            try {

                int typ = 0;
                int num = 0;
                String nameVal;
                String nameTab;
                Integer typPar;
                Byte tarifPar;
                for (int i = 1; i < Val.length; i++) {

                    try {
                        typ = Integer.parseInt(TypData);

                        statement.setInt(1, typ);
                        statement.setInt(2, i);

                    } catch (NumberFormatException e) {

                        setNotifyObservers("Объект[" + idObject + "] не корректный тип параметра !(" + TypData + ")");
                        continue;
                    }

                    rs = statement.executeQuery();
                    if (rs.next()) {

                        nameVal = rs.getString("prm_name_tbl");
                        nameTab = rs.getString("name_table");
                        typPar = rs.getInt("c_partype_id");
                        tarifPar = rs.getByte("prm_tarif");

                        String sV = Val[i];
                        sV = sV.replaceAll(",", ".");

                        if (!hmColTables.containsKey(nameTab)) {

                            alCol = Work.getNamesCol(nameTab);
                            hmColTables.put(nameTab, alCol);
                        }

                        try {

                            dv = Double.parseDouble(sV);

                        } catch (NumberFormatException e) {

                            setNotifyObservers(e);

                            continue;

                        }

                        fullName = nameTab + ":" + typPar + ":" + tarifPar;

                        if (hmInsInTab.containsKey(fullName)) {

                            hmInsert = hmInsInTab.get(fullName);
                            hmInsert.put(nameVal, dv);

                        } else {

                            hmInsert = new HashMap<String, Object>();
                            hmInsert.put(nameVal, dv);

                            hmInsInTab.put(fullName, hmInsert);

                        }

                    }
                    rs.close();

                }

                for (String nTab : hmInsInTab.keySet()) {

                    String[] names = nTab.split(":");
                    String tabNam = names[0];

                    hmInsert = hmInsInTab.get(nTab);

                    alCol = hmColTables.get(tabNam);

                    if (alCol.contains("parnumber_id")) {

                        typPar = Integer.parseInt(names[1]);
                        hmInsert.put("parnumber_id", typPar);
                    }

                    if (alCol.contains("tarif")) {

                        tarifPar = Byte.parseByte(names[2]);
                        hmInsert.put("tarif", tarifPar);
                    }

                    hmInsert.put("Id_object", idObject);

                    String caption = (String) Work.getParametersRow(idObject, null, "objects", false, true).get("caption");

                    hmInsert.put("object_caption", caption);

                    Timestamp ts = new Timestamp(d_value.getMillis());
                    hmInsert.put("value_date", ts);

                    Timestamp timestamp = new Timestamp(new DateTime().getMillis());
                    hmInsert.put("modify_date", timestamp);
                    hmInsert.put("is_check", 0);
                    hmInsert.put("flag0", 0);
                    hmInsert.put("flag1", 0);

                    Work.insertRecInTable(tabNam, hmInsert);

                }
            } finally {
                statement.close();
            }

        } catch (SQLException ex) {

            setNotifyObservers(ex);

        }

    }

    @Override
    public void doProcess() {

        try {
            includeStatus(esGPRSOn);

            long l = Thread.currentThread().getId();
            Thread.currentThread().setName("GPRS(" + l + ")");

            String s = nameSchedule;

            setNotifyObservers(s);
            newProcess(s);

            CheckDataGprs();

            setNotifyObservers("Данные GPRS загружены.");

        } finally {

            excludeStatus(esGPRSOn);

        }

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer) {
            try {
                setParameters();
            } catch (Exception ex) {
                setLoggerInfo(nameSchedule, ex);
            }

            if (id_schedule == (Integer) arg && o instanceof ScheduleClass && !isSetStatus(esGPRSOn)) {
                ScheduleClass sc = (ScheduleClass) o;
                nameSchedule = sc.getNameSchedule();
                executeProcess();
            }
        }

    }
}

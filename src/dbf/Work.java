/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbf;

import connectdbf.SqlTask;
import connectdbf.StatementEx;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.*;
import servise.BitSetEx;
import servise.MainWorker;
import servise.MathTrans;
import xmldom.XmlTask;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 1
 */
public class Work {

    public static final int B_LITTLE_ENDIAN = 0;  // от Младшего к старшему
    public static final int B_BIG_ENDIAN = 1;  // от старшего  к младшему
    public static Connection CONNECT_DBF;// Текущее соединение с базой
    public static final String CONN_JDBC = "jdbc"; // драйвер подключения
    public static final String CONN_NAME_BASE = "databaseURL"; // путь к вазе
    public static final String CONN_NEW_BASE = "newBase"; // новая база
    public static final String TABLE_ID = "id";
    public static final String TABLE_NAME = "name";
    public static final String TABLE_TYPE = "type";
    public static final String TABLE_CAPTION = "caption";
    public static final String TABLE_PROPERTY = "property";
    public static final String TABLE_PROPERTY_XML = "property_xml";
    public static final String TABLE_LEVEL = "level";
    public static final String TABLE_ORDER_NAME = "names_cap";
    public static final String TABLE_TYPE_COL = "type_col";
    public static final String TABLE_CAPTION_COL = "name_col";
    public static final String TABLE_PRIMARY_KEY = "primary_key";
    public static final String TABLE_INDEX = "index_table";
    public static final String TABLE_AUTO_INCREMENT = "auto_increment";
    public static final String TABLE_LINK_OBJECT = "link_object";
    public static Integer ID_CUR_USER; // id текущего пользователя

    public static final int TYP_OBJ_COUNT = 0;
    public static final int TYP_OBJ_CTRL = 1;
    public static final int TYP_OBJ_MODEM = 2;
    public static final int TYP_OBJ_KA = 3;
    public static final int TYP_OBJ_DISCRET = 4;
    public static final int TYP_OBJ_CONNECT = 5;

    //Названия столбцов
    public static HashMap<String, LinkedHashMap<String, String>> HM_VALUE_CAPTION = null;

    //Названия коэффициентов
    public static HashMap<String, LinkedHashMap<String, String>> HM_VALUE_KFC = null;

    //Названия столбцов
    public static final HashMap<String, String[]> HM_COL_CAPTION = new HashMap<String, String[]>();

    /**
     * Возвращает Файл конфигурации по названию таблицы
     *
     */
    public static Document getDocConfigObject(String nameTable) throws SQLException {

        String sql = "SELECT c_base_property     FROM  c_obj_spec   WHERE  c_name_table='" + nameTable + "'";

        ResultSet rsThis = SqlTask.getResultSet(null, sql);

        try {
            if (rsThis.next()) {

                String sxml = rsThis.getString(1);
                try {
                    return XmlTask.stringToXmlDoc(sxml);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }

            } else {
                return null;
            }

        } finally {

            rsThis.close();
        }
    }

    public static HashMap<Integer, String> getUsersMap() throws Exception {

        String sql = "SELECT * FROM users ORDER BY name_user";

        ResultSet rs = SqlTask.getResultSet(null, sql);

        HashMap<Integer, String> hmUser = new HashMap<>();

        try {

            while (rs.next()) {

                Integer id_user = rs.getInt("id_user");
                String name_user = rs.getString("name_user");

                hmUser.put(id_user, name_user);

            }

        } finally {
            rs.close();
        }

        return hmUser;
    }

    public static synchronized void setUserParameter(String param, Object value) throws SQLException {

        int idUser = MainWorker.ID_CUR_USER;

        String sql = "SELECT json_info FROM users WHERE id_user=" + idUser;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("json_info");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            }
        } finally {
            rs.close();
        }

        jSONObject.put(param, value);

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<String, Object>();

        hmParam.put("json_info", json);

        sql = " WHERE id_user=" + idUser;
        SqlTask.updateRecInTable(null, "users", sql, hmParam);

    }

    public static synchronized Object getUserParameter(String nameparam) throws SQLException {
        Object result = null;

        int idUser = MainWorker.ID_CUR_USER;

        String sql = "SELECT json_info FROM users WHERE id_user=" + idUser;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("json_info");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);
                    result = jSONObject.get(nameparam);

                }
            }
        } finally {
            rs.close();
        }

        return result;

    }

    public static synchronized void setJsonParameters(int idObject, Map<String, Object> parameters) throws SQLException {

        String sql = "SELECT dopinfo_json FROM dop_info WHERE id_dopinfo=" + idObject;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("dopinfo_json");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            } else {

                jSONObject = new JSONObject();

            }
        } finally {
            rs.close();
        }

        for (String name : parameters.keySet()) {
            Object value = parameters.get(name);

            jSONObject.put(name, value);

        }

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<>();

        Timestamp t = new Timestamp(new Date().getTime());

        hmParam.put("dopinfo_json", json);
        hmParam.put("id_dopinfo", idObject);
        hmParam.put("value_date", t);

        StatementEx statementEx = new StatementEx(CONNECT_DBF, "dop_info", hmParam);

        statementEx.replaceRecInTable(hmParam, true);

    }

    public static synchronized void setJsonParameter(int idObject, String param, Object value) throws SQLException {

        String sql = "SELECT dopinfo_json FROM dop_info WHERE id_dopinfo=" + idObject;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("dopinfo_json");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            } else {

                jSONObject = new JSONObject();

            }
        } finally {
            rs.close();
        }

        jSONObject.put(param, value);

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<>();

        Timestamp t = new Timestamp(new Date().getTime());

        hmParam.put("dopinfo_json", json);
        hmParam.put("id_dopinfo", idObject);
        hmParam.put("value_date", t);
        StatementEx statementEx = new StatementEx(CONNECT_DBF, "dop_info", hmParam);

        statementEx.replaceRecInTable(hmParam, true);

    }

    /**
     * Формирует строку в формате JSON
     *
     * @param param карта параметров
     * @return
     */
    public static String getJsonStringByMap(Map param) {

        String result;

        JSONObject jSONObject = new JSONObject();

        jSONObject.putAll(param);

        result = jSONObject.toString();

        return result;
    }

    /**
     * Параметры из строки в формате JSON
     *
     * @param json -строка в формате JSON
     * @return
     */
    public static HashMap<Object, Object> getMapByJsonString(String json) {

        HashMap<Object, Object> result = new HashMap();

        JSONObject jSONObject;

        jSONObject = JSONObject.fromObject(json);

        Set set = jSONObject.entrySet();
        Iterator iter = set.iterator();

        while (iter.hasNext()) {
            Map.Entry pet = (Map.Entry) iter.next();
            result.put((String) pet.getKey(), pet.getValue());
        }

        return result;
    }

    public static synchronized HashMap<String, Object> getJsonParameter(int idObject) throws SQLException {

        HashMap<String, Object> result = new HashMap();

        String sql = "SELECT dopinfo_json FROM dop_info WHERE id_dopinfo=" + idObject;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("dopinfo_json");

                if (json != null && !json.isEmpty()) {

                    jSONObject = JSONObject.fromObject(json);

                    Set set = jSONObject.entrySet();
                    Iterator iter = set.iterator();

                    while (iter.hasNext()) {
                        Map.Entry pet = (Map.Entry) iter.next();
                        result.put((String) pet.getKey(), pet.getValue());
                    }

                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    public static synchronized Object getJsonParameter(int idObject, String nameparam) throws SQLException {

        Object result = null;

        String sql = "SELECT dopinfo_json FROM dop_info WHERE id_dopinfo=" + idObject;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("dopinfo_json");

                if (json != null && !json.isEmpty()) {

                    jSONObject = JSONObject.fromObject(json);
                    result = jSONObject.get(nameparam);

                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    public static Map getLocaleParametres() throws Exception {

        HashMap<String, Object> hmResult;

        File fileChek;

        String user_dir = System.getProperty("user.dir");
        fileChek = new File(user_dir, "config_local.xml");
        Document document;
        document = xmldom.XmlTask.getDocument(fileChek);
        hmResult = xmldom.XmlTask.getMapValuesByXML(fileChek, "name", "value", "cell");

        String version = document.getDocumentElement().getAttribute("version");
        String build = document.getDocumentElement().getAttribute("build");
        String name = document.getDocumentElement().getAttribute("name");

        hmResult.put("version", version);
        hmResult.put("build", build);
        hmResult.put("name", name);

        return hmResult;

    }

    /**
     * Добавляет новый объект в базу.
     *
     * @param nameTable -тип добавляемого объекта
     * @return
     * @throws java.lang.Exception
     */
    public static HashMap<String, Object> createNewObject(String nameTable) throws Exception {

        HashMap<String, Object> hmCol;

        String typCounter = "Меркурий 230";// Счетчик
        String typObject = "Объект ЭС"; // Объект
        String typContrioller = "TC-65"; // Контроллер

        hmCol = SqlTask.getMapNamesCol(null, nameTable, 5);

        HashMap<String, Object> hmValues;

        StatementEx statementEx = new StatementEx(null, nameTable, null);

        hmValues = statementEx.getHmCol();

        for (String ncol : hmValues.keySet()) {

            hmValues.put(ncol, null);

        }

        String key = SqlTask.getPrimaryKeyTable(null, nameTable);
        int maxKey = SqlTask.getMaxKeyByNameTable(null, nameTable);

        String typ = "";

        Timestamp t = new Timestamp(new Date().getTime());

        if (hmValues.containsKey(key)) {

            hmValues.put(key, maxKey);

        }

        if (nameTable.equals("objects")) {

            typ = typObject;
            hmValues.put("date_update_obj", t);
            hmValues.put("typ_object", typObject);

        } else if (nameTable.equals("points")) {

        } else if (nameTable.equals("counters")) {

            typ = typCounter;
            hmValues.put("date_update_count", t);
            hmValues.put("typ_counter", typCounter);

        } else if (nameTable.equals("controllers")) {

            typ = typContrioller;
        }

        Document dPage = Work.getXmlDocFromConst(typ);

        Map<String, String> hmDeff;

        hmDeff = XmlTask.getMapAttrubuteByName(dPage.getDocumentElement(), "name", "default_value", "cell");

        Integer sqltyp;

        for (String nameCol : hmValues.keySet()) {

            sqltyp = (Integer) hmCol.get(nameCol);

            if (hmDeff.containsKey(nameCol)) {

                sqltyp = (Integer) hmCol.get(nameCol);
                String value = hmDeff.get(nameCol);

                if (value == null || value.isEmpty()) {
                    continue;
                }

                if (sqltyp == java.sql.Types.INTEGER) {

                    int vi = Integer.parseInt(value);
                    hmValues.put(nameCol, vi);

                } else if (sqltyp == java.sql.Types.BOOLEAN || sqltyp == java.sql.Types.TINYINT) {

                    boolean vb;

                    if (value.equals("1") || value.equals("true")) {

                        vb = true;

                    } else {

                        vb = false;

                    }

                    hmValues.put(nameCol, vb);

                } else {

                    hmValues.put(nameCol, value);

                }

            }

        }

        statementEx.insertRecInTable(hmValues);

        statementEx.close();

        return hmValues;
    }

    public static Object getLocaleParam(String nameParam) throws Exception {

        Object result = null;

        HashMap<String, Object> hmParam = getMapLocaleParam();

        if (hmParam != null && hmParam.containsKey(nameParam)) {

            result = hmParam.get(nameParam);
        }
        return result;
    }

    public static HashMap<String, Object> getMapLocaleParam() throws Exception {

        String dir;

        dir = System.getProperty("user.dir");
        File fileChek;
        fileChek = new File(dir, "config_local.xml");
        HashMap<String, Object> hmParam;
        hmParam = XmlTask.getMapValuesByXML(fileChek, "name", "value", "cell");

        return hmParam;
    }

    private static void addPage(Document docConfig, String typPage, String deffPage) throws Exception {

        if (typPage == null || typPage.isEmpty()) {

            typPage = deffPage;
        }

        Node root = docConfig.getDocumentElement();

        Document dPage = Work.getXmlDocFromConst(typPage);

        if (dPage == null) {

            dPage = Work.getXmlDocFromConst(deffPage);

        }

        NodeList list = XmlTask.getNodeListByXpath(dPage, "descendant::page");

        Node nFirst = root.getFirstChild();

        for (int i = 0; i < list.getLength(); i++) {

            Node node = list.item(i);

            Node nAdd = docConfig.importNode(node, true);

            root.insertBefore(nAdd, nFirst);

        }

    }

    public static Document getDocConfigByName(String nameTable, HashMap<String, Object> hmParam) throws Exception {

        Document result = null;

        String typCounter = "Меркурий 230";// Счетчик электрический
        String typColdWater = "Нет прибора учета";// Счетчик холодной воды
        String typHotWater = "Нет прибора учета";// Счетчик горячей воды
        String typHeating = "Нет прибора учета";// Счетчик отопления
        String typGas = "Нет прибора учета";// Счетчик газа

        String typObject = "Объект ЭС"; // Объект
        String typContrioller = "TC-65"; // Контроллер

        String namePage = null;

        result = getDocConfigObject(nameTable);

        if (hmParam.containsKey("typ_counter")) {

            namePage = (String) hmParam.get("typ_counter");
            addPage(result, namePage, typCounter);

        }

        if (hmParam.containsKey("typ_cold_water")) {

            namePage = (String) hmParam.get("typ_cold_water");
            addPage(result, namePage, typColdWater);

        }

        if (hmParam.containsKey("typ_hot_water")) {

            namePage = (String) hmParam.get("typ_hot_water");
            addPage(result, namePage, typHotWater);

        }

        if (hmParam.containsKey("typ_heating")) {

            namePage = (String) hmParam.get("typ_heating");
            addPage(result, namePage, typHeating);

        }

        if (hmParam.containsKey("typ_gas")) {

            namePage = (String) hmParam.get("typ_gas");
            addPage(result, namePage, typGas);

        }

        if (hmParam.containsKey("typ_object")) {

            namePage = (String) hmParam.get("typ_object");
            addPage(result, namePage, typObject);

        }

        if (hmParam.containsKey("typ_controller")) {

            namePage = (String) hmParam.get("typ_controller");
            addPage(result, namePage, typContrioller);

        }

        if (result != null) {
            result.getDocumentElement().setAttribute("name_form", nameTable);
        }

        return result;

    }

    public static HashMap<String, Integer> getValueKfcById(Integer id, HashMap<String, String> mapNamesKfc) throws SQLException {

        HashMap<String, Integer> hmKgcVal = new HashMap<>();

        Map map = getParametersObject(id, null, false, false, false);

        for (String skfc : mapNamesKfc.values()) {

            String[] kfc_arr = skfc.split(";");

            for (String nKfc : kfc_arr) {

                Integer val = (Integer) map.get(nKfc);

                hmKgcVal.put(nKfc, val);

            }

        }

        return hmKgcVal;
    }

    /**
     * Для WEB Inspectora постраничный вывод информаци если страница 1 то
     * выводится название оббекта "caption"
     *
     * @param objects листинг объектов если число то id субконто
     * @param nametable имя таблицы
     * @param dtFirst начальная дата
     * @param dtLast конечная дата
     * @param step - количество записей на странице
     * @param pozicion номер страницы для получения начиная с 1 Для первой
     * страницы возврвщаются имена и название полей таблицы данных;
     * @param kfc если true то с учетом коэффициентов
     * @return
     * @throws Exception
     */
    public static HashMap<String, Object> getLimitValues(Object objects, String nametable, String dateFirst, String dateLast, Integer step, Integer page, Boolean kfc) throws Exception {

        HashMap<String, Object> hmResult = new HashMap<>();
        DateTime dtFirst;
        DateTime dtLast;

        // Значения коэффициентов по id
        HashMap<Integer, HashMap<String, Integer>> hmValueKfcById = new HashMap<>();

        HashMap<String, Integer> hmkfcValues = null;

        String nameObject = "Все объекты";
        Integer idObject = null;

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy");

        dtFirst = dtf.parseDateTime(dateFirst);
        dtLast = dtf.parseDateTime(dateLast);

        Timestamp tFirst = new Timestamp(dtFirst.getMillis());
        Timestamp tLast = new Timestamp(dtLast.getMillis());;

        ArrayList<HashMap<String, Object>> alResult = new ArrayList<>();

        String sql;

        List listObjects;

        if (objects instanceof Integer) {

            Integer idSubconto = (Integer) objects;

            sql = "SELECT id_object FROM objects WHERE id_subconto=?";

            listObjects = SqlTask.getListBySQL(null, sql, new Object[]{idSubconto});

        } else {

            listObjects = (List) objects;

            if (listObjects.size() == 1) {

                idObject = (Integer) listObjects.get(0);
            }

        }

        StringBuilder builder = new StringBuilder("SELECT * FROM " + nametable + " WHERE ");

        builder.append("(");

        for (Object id : listObjects) {

            builder.append("id_object=? OR ");

        }

        builder.delete(builder.length() - 3, builder.length());

        builder.append(")");

        builder.append(" AND (value_date>=? AND value_date<=?) ORDER BY value_date");

        sql = builder.toString();

        listObjects.add(tFirst);
        listObjects.add(tLast);

        LinkedHashMap<String, Object> hmValues;

        //Названия и имена столбцов
        LinkedHashMap<String, String> mapCaption = getCaptionTableValue(nametable);

        // Коэффициенты
        LinkedHashMap<String, String> hmKfc = getKfcTableValue(nametable);

        ResultSet rs = SqlTask.getResultSet(null, sql, listObjects);

        try {

            if (rs.last()) {
                int size = rs.getRow();

                // Всего страниц
                int allPages = size / step;

                int ost = size % step;

                if (ost > 0) {

                    allPages = allPages + 1;

                }

                rs.first();

                if (page == 1) {

                    rs.beforeFirst();

                    // Запрошена 1я страница
                    if (idObject != null) {
                        // по одному объекту

                        Map map = getParametersObject(idObject, null, false, false, true);

                        nameObject = (String) map.get("caption");

                    }

                } else {

                    rs.relative(step * (page - 1) - 1);

                }

                hmResult.put("pages", allPages);
                hmResult.put("values", alResult);
                hmResult.put("sql", sql);
                hmResult.put("columns", mapCaption);
                hmResult.put("caption", nameObject);

                dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

                int count = 0;

                Double dVal;

                while (rs.next() && count < step) {
                    hmValues = new LinkedHashMap<>();

                    for (String name : mapCaption.keySet()) {
                        Object value = rs.getObject(name);

                        if (name.equals("value_date")) {

                            Timestamp t = (Timestamp) value;
                            DateTime dateTime = new DateTime(t.getTime());

                            String sdate = dateTime.toString(dtf);
                            hmValues.put(name, sdate);

                        } else if (name.equals("id_object")) {

                            idObject = (Integer) value;

                            hmValues.put(name, idObject);
                            if (hmValueKfcById.containsKey(idObject)) {

                                hmkfcValues = hmValueKfcById.get(idObject);

                            } else {

                                hmkfcValues = getValueKfcById(idObject, hmKfc);
                                hmValueKfcById.put(idObject, hmkfcValues);

                            }

                        } else {

                            if (kfc && hmKfc.containsKey(name)) {

                                String kfcs = hmKfc.get(name);
                                dVal = rs.getDouble(name);

                                for (String nameKfc : hmkfcValues.keySet()) {

                                    if (kfcs.contains(nameKfc)) {
                                        Integer vKfc = hmkfcValues.get(nameKfc);

                                        dVal = dVal * vKfc;

                                    }

                                }

                                String sval = String.format("%9.3f", dVal);
                                hmValues.put(name, sval);

                            } else {

                                //Object value = rs.getObject(name);
                                hmValues.put(name, value);
                            }

                        }
                    }

                    alResult.add(hmValues);
                    count++;
                }

            }

        } finally {

            rs.close();

        }

        return hmResult;

    }

    /**
     * Названия и коэффициенты столбцов таблицы данных для представлений
     * пользователям
     *
     * @return карту имя-коэффициенты
     * @throws Exception
     */
    public static LinkedHashMap<String, String> getKfcTableValue(String nameTable) throws Exception {

        LinkedHashMap<String, String> mapKfc = null;

        if (HM_VALUE_KFC != null && HM_VALUE_KFC.containsKey(nameTable)) {
            mapKfc = HM_VALUE_KFC.get(nameTable);

        } else {

            Document docConfig = getDocConfigByTable(nameTable);

            if (docConfig != null) {

                LinkedHashMap<String, String> hmKey = XmlTask.getLinkedMapAttrubuteByName(docConfig.getDocumentElement(), "name", "kfc", "cell");

                mapKfc = new LinkedHashMap<>();

                for (String name : hmKey.keySet()) {

                    String kfc = hmKey.get(name);

                    mapKfc.put(name, kfc);
                }

            }

            if (HM_VALUE_KFC == null) {

                HM_VALUE_KFC = new LinkedHashMap<>();
            }

            HM_VALUE_KFC.put(nameTable, mapKfc);
        }

        return mapKfc;
    }

    /**
     * Названия и имена столбцов таблицы данных для представлений пользователям
     *
     * @return карту имя-название
     * @throws Exception
     */
    public static LinkedHashMap<String, String> getCaptionTableValue(String nameTable) throws Exception {

        LinkedHashMap<String, String> mapCaption = null;

        if (HM_VALUE_CAPTION != null && HM_VALUE_CAPTION.containsKey(nameTable)) {
            mapCaption = HM_VALUE_CAPTION.get(nameTable);

        } else {

            Document docConfig = getDocConfigByTable(nameTable);

            String caption;

            if (docConfig != null) {
                caption = docConfig.getDocumentElement().getAttribute("column");

                LinkedHashMap<String, String> hmKey = XmlTask.getLinkedMapAttrubuteByName(docConfig.getDocumentElement(), "name", "caption", "cell");

                mapCaption = new LinkedHashMap<>();

                mapCaption.put("Id_object", "ID");
                mapCaption.put("value_date", "Дата");

                for (String name : hmKey.keySet()) {

                    if (caption.contains(name)) {
                        String cap = hmKey.get(name);

                        mapCaption.put(name, cap);
                    }

                }

                if (HM_VALUE_CAPTION == null) {

                    HM_VALUE_CAPTION = new LinkedHashMap<>();
                }

                HM_VALUE_CAPTION.put(nameTable, mapCaption);
            }

        }

        return mapCaption;
    }

    /**
     * Названия столбцов таблиц points, objects, counters
     *
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, String> getCaptionCols() throws Exception {

        ArrayList<String> alNames = new ArrayList<>();

        alNames.add("objects");
        alNames.add("points");
        alNames.add("counters");

        LinkedHashMap<String, Object> hmTables;

        LinkedHashMap<String, String> hmNames = new LinkedHashMap<>();
        Document docConfig;

        for (String nameTable : alNames) {

            hmTables = SqlTask.getMapNamesCol(null, nameTable, 5);

            docConfig = getDocConfigByTable(nameTable);

            String nameCol;

            if (docConfig != null) {
                LinkedHashMap<String, String> hmKey = XmlTask.getLinkedMapAttrubuteByName(docConfig.getDocumentElement(), "name", "caption", "cell");

                for (String key : hmTables.keySet()) {

                    if (hmKey.containsKey(key)) {

                        nameCol = hmKey.get(key);
                    } else {

                        nameCol = "Нет названия в конфигурации";

                    }

                    hmNames.put(key, nameCol);

                }
            }

        }

        return hmNames;
    }

    public static HashMap<String, String> getNamesColByResultSet(ResultSet rs, String sqlObj) throws Exception {

        ArrayList<String> alNames;

        alNames = SqlTask.getNamesTabBySQL(null, sqlObj);

        HashMap<String, String> hmNames = new HashMap<>();
        Document docConfig;

        HashSet<String> tables = new HashSet<>();

        HashMap<String, Object> hmParam = new HashMap<>();

        if (rs.first()) {
            SqlTask.addParamToMap(rs, hmParam);
        }

        for (String nameTable : alNames) {

            docConfig = getDocConfigByName(nameTable, hmParam);

            if (docConfig != null) {
                HashMap<String, String> hmKey = XmlTask.getMapAttrubuteByName(docConfig.getDocumentElement(), "name", "caption", "cell");
                hmNames.putAll(hmKey);
            }

        }

        return hmNames;
    }

    /**
     * Название объектов по типу
     *
     * @param тип
     * @return
     */
    public static TreeSet<String> getObjectNameByTyp(int typ) throws SQLException {

        ResultSet rs;

        TreeSet<String> treeSet = new TreeSet<String>();

        String sql = sql = "SELECT c_obj_list_name FROM c_obj_spec WHERE c_obj_type=?";

        rs = SqlTask.getResultSet(null, sql, new Object[]{typ});

        try {

            while (rs.next()) {

                String names = rs.getString(1);

                treeSet.add(names);

            }

        } finally {

            rs.close();
        }

        sql = sql = "SELECT c_const_name FROM c_const WHERE type_const=?";

        rs = SqlTask.getResultSet(null, sql, new Object[]{typ});

        try {

            while (rs.next()) {

                String names = rs.getString(1);

                treeSet.add(names);

            }

        } finally {

            rs.close();
        }
        return treeSet;
    }

    /**
     * Название прибров учета или контроллеров по типу 0-приборы учета
     * 1-контроллеры ,2-Тип присоединения(связи)
     *
     * @param typ
     * @return
     * @throws java.sql.SQLException
     */
    public static TreeSet<String> getPriborsNameByTyp(int typ) throws SQLException {

        ResultSet rs;

        TreeSet<String> treeSet = new TreeSet();

        String sql = "SELECT c_instrument FROM commands WHERE c_grup=?";

        String group = null;

        if (typ == TYP_OBJ_COUNT) {
            group = "Счетчик";
        } else if (typ == TYP_OBJ_CTRL) {
            group = "Контроллер";
        } else if (typ == TYP_OBJ_MODEM) {
            group = "Модем";
        } else if (typ == TYP_OBJ_KA) {
            group = "КА";
        } else if (typ == TYP_OBJ_DISCRET) {
            group = "Контакт";
        } else if (typ == TYP_OBJ_CONNECT) {
            group = "Связь";
        }

        rs = SqlTask.getResultSet(null, sql, new Object[]{group});

        try {

            while (rs.next()) {

                String names = rs.getString(1);

                if (names == null || names.isEmpty()) {
                    continue;
                }

                String[] namePrib = names.split("/");

                for (String name : namePrib) {

                    if (!name.trim().isEmpty()) {

                        treeSet.add(name);
                    }
                }
            }

        } finally {

            rs.close();
        }

        return treeSet;
    }

    public static int getLevelTable(String nameTable) throws SQLException {

        int result = 0;
        ResultSet rs;

        String sql = "SELECT c_level FROM   c_obj_spec WHERE  c_name_table=?";

        rs = SqlTask.getResultSet(null, sql, new Object[]{nameTable});

        try {
            if (rs.next()) {

                result = rs.getInt(1);
            }

        } finally {
            rs.close();
        }

        return result;
    }

    /**
     * Проверяет может ли быть числом строка
     *
     * @param value
     * @return
     */
    public static boolean isNumber(String value) {

        boolean result = true;

        try {
            Double d = Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            result = false;
        }
        return result;
    }

    // Добавляем в общую таблицу
    public static Integer addInTree(int idTable, int typObject) throws SQLException {

        String sql;
        Integer result = null;

        HashMap<String, Object> hmAdd = new HashMap<String, Object>();

        Date currentDate = new Date();
        Timestamp ts = new Timestamp(currentDate.getTime());

        hmAdd.put("c_obj_list_id", idTable);
        hmAdd.put("date_modify", ts);
        hmAdd.put("obj_typ_id", typObject);

        result = insertRecInTable("c_tree", hmAdd);

        return result;
    }

    /**
     * Подгонка профиля при несовпадении и добавление получасовок на 26 октября
     *
     * @param idObject
     */
    public static String normProfil(int idObject) {

        String result = null;

        // Энергия на начало текущего  месяца
        DateTime dateTimeFirst = MathTrans.getDateReport(-1);
        // Энергия на начало текущих суток
        DateTime dateTimeLast = MathTrans.getDateReport(0);

        String sql = "";

        Double ergValFirst = null;
        Double ergValLast = null;

        Double enrgRasx = null;
        Double profRasx = 0.0;

        Timestamp tsFirst = new Timestamp(dateTimeFirst.getMillis());
        Timestamp tsLast = new Timestamp(dateTimeLast.getMillis());

        sql = "SELECT * FROM enegry_data WHERE id_object=? AND  (value_date=? OR value_date=?)  AND tarif=?";

        Object[] param = new Object[]{idObject, tsFirst, tsLast, 0};
        try {

            ResultSet resultSet = SqlTask.getResultSet(null, sql, param);

            StatementEx ex = null;

            try {

                while (resultSet.next()) {

                    Timestamp timestamp = resultSet.getTimestamp("value_date");

                    if (timestamp.getTime() == tsFirst.getTime()) {
                        ergValFirst = resultSet.getDouble("energy_down_0_0");
                    } else {

                        ergValLast = resultSet.getDouble("energy_down_0_0");
                    }
                }

            } finally {
                resultSet.close();
            }

            if (ergValFirst != null && ergValLast != null) {

                enrgRasx = ergValLast - ergValFirst;

            } else {

                result = "недостаточно данных для выполнения скрипта !";

                return result;
            }

            // Профиль мощности
            sql = "SELECT * FROM profil_power WHERE id_object=? AND  value_date>? AND value_date<=?";

            param = new Object[]{idObject, tsFirst, tsLast};

            // количество нулевых
            int countZero = 0;

            try {

                resultSet = SqlTask.getResultSet(null, sql, param, null, ResultSet.CONCUR_UPDATABLE);

                while (resultSet.next()) {

                    Double pa = resultSet.getDouble("power_pa");

                    profRasx = pa + profRasx;

                    if (pa == 0.0) {

                        countZero = countZero + 1;
                    }

                }

                // Разница расходов
                Double razRasx = enrgRasx - profRasx;

                HashMap<String, Object> hmCol = new HashMap<>();

                ex = new StatementEx(null, "profil_power", hmCol);

                DateTime dt = new DateTime();
                dt = dt.millisOfDay().setCopy(0);
                dt = dt.dayOfMonth().setCopy(26);
                dt = dt.monthOfYear().setCopy(10);
                dt = dt.minuteOfHour().setCopy(30);
                dt = dt.hourOfDay().setCopy(1);

                hmCol.put("id_object", idObject);
                hmCol.put("object_caption", "доп.получасовка");
                hmCol.put("value_date", new Timestamp(dt.getMillis()));
                hmCol.put("season", 13);
                hmCol.put("power_pa", razRasx / 2.0);
                hmCol.put("power_pr", 0.0);
                hmCol.put("power_qa", 0.0);
                hmCol.put("power_qr", 0.0);
                hmCol.put("tangens_f", 0.0);
                hmCol.put("modify_date", new Timestamp(dt.getMillis()));
                hmCol.put("is_check", 0);
                hmCol.put("flag0", 0);
                hmCol.put("flag1", 0);

                ex.insertRecInTable(hmCol);

                dt = dt.minuteOfHour().setCopy(0);
                dt = dt.hourOfDay().setCopy(2);

                hmCol.put("value_date", new Timestamp(dt.getMillis()));
                ex.insertRecInTable(hmCol);

                ex.close();

// Добавляем 2 получасовки
            } finally {

                resultSet.close();

            }

        } catch (SQLException ex) {
            MainWorker.deffLoger.error(ex);
            result = ex.getMessage();

        }

        return result;
    }

    /**
     * Подгонка профиля при несовпадении
     *
     * @param idObject
     */
    public static String updateProfil(int idObject) {

        String result = null;

        // Энергия на начало предыдущего месяца
        DateTime dateTimeFirst = MathTrans.getDateReport(-1);
        // Энергия на начало текущего месяца
        DateTime dateTimeLast = MathTrans.getDateReport(0);

        String sql = "";

        Double ergValFirst = null;
        Double ergValLast = null;

        Double enrgRasx = null;
        Double profRasx = 0.0;
        Double profRasxNew = 0.0;

        Timestamp tsFirst = new Timestamp(dateTimeFirst.getMillis());
        Timestamp tsLast = new Timestamp(dateTimeLast.getMillis());

        sql = "SELECT * FROM enegry_data WHERE id_object=? AND  (value_date=? OR value_date=?)  AND tarif=?";

        Object[] param = new Object[]{idObject, tsFirst, tsLast, 0};
        try {

            ResultSet resultSet = SqlTask.getResultSet(null, sql, param);

            try {

                while (resultSet.next()) {

                    Timestamp timestamp = resultSet.getTimestamp("value_date");

                    if (timestamp.getTime() == tsFirst.getTime()) {
                        ergValFirst = resultSet.getDouble("energy_down_0_0");
                    } else {

                        ergValLast = resultSet.getDouble("energy_down_0_0");
                    }
                }

            } finally {
                resultSet.close();
            }

            if (ergValFirst != null && ergValLast != null) {

                enrgRasx = ergValLast - ergValFirst;

            } else {

                result = "недостаточно данных для выполнения скрипта !";

                return result;
            }

            // Профиль мощности
            sql = "SELECT * FROM profil_power WHERE id_object=? AND  value_date>? AND value_date<=?";

            param = new Object[]{idObject, tsFirst, tsLast};

            // количество нулевых
            int countZero = 0;

            try {

                resultSet = SqlTask.getResultSet(null, sql, param, null, ResultSet.CONCUR_UPDATABLE);

                while (resultSet.next()) {

                    Double pa = resultSet.getDouble("power_pa");

                    profRasx = pa + profRasx;

                    if (pa == 0.0) {

                        countZero = countZero + 1;
                    }

                }

                // Разница расходов
                Double razRasx = enrgRasx - profRasx;

                resultSet.last();

                // Всего ненулевых получасовок
                int countRow = resultSet.getRow() - countZero;

                Double kfc = razRasx / countRow;

                resultSet.beforeFirst();

                while (resultSet.next()) {

                    Double pa = resultSet.getDouble("power_pa");

                    if (pa != 0.0) {

                        pa = pa + kfc;
                        resultSet.updateString("object_caption", "ОК");
                        resultSet.updateDouble("power_pa", pa);
                        resultSet.updateRow();
                    }

                    profRasxNew = profRasxNew + pa;

                }

            } finally {

                resultSet.close();

            }

        } catch (SQLException ex) {
            MainWorker.deffLoger.error(ex);
            result = ex.getMessage();

        }

        return result;
    }

    public static String getNameTableByView(String nameView) throws SQLException {

        String result = nameView;

        TreeSet<String> ts = SqlTask.getNamesDb(null, false);

        for (String s : ts) {

            if (nameView.equals(s)) {
                result = s;
                break;
            }
        }
        return result;
    }

    public static String getSQLbyId(int id) throws SQLException {
        String result = null;
        String sql = "SELECT sql_string FROM sql_make WHERE id_sql=" + id;

        ResultSet rs;

        rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                result = rs.getString("sql_string");
            }

        } finally {
            rs.close();
        }
        return result;
    }

    public static String getCaptionObject(Map hmParam, String nameTable) throws SQLException {

        String result = null;

        String[] names;

        if (HM_COL_CAPTION.containsKey(nameTable)) {

            names = HM_COL_CAPTION.get(nameTable);
        } else {

            names = getCaptionByTable(nameTable);
            HM_COL_CAPTION.put(nameTable, names);

        }

        if (names == null) {
            return result;
        }

        StringBuilder builder = new StringBuilder("");

        for (String name : names) {

            Object cap = hmParam.get(name);

            if (cap != null) {
                builder.append(cap);
                builder.append(", ");
            }
        }

        if (builder.length() > 2) {
            builder.deleteCharAt(builder.length() - 2);
        }

        result = builder.toString();

        return result;
    }

    /**
     * Название строки по текущему столбцу
     *
     * @param rs
     * @param iCol
     * @return
     * @throws SQLException
     */
    public static String getCaptionObjectByCol(ResultSet rs, int iCol) throws SQLException {

        String result = null;

        if (iCol <= 0) {
            return result;
        }

        String nameTable = rs.getMetaData().getTableName(iCol).toLowerCase();

        // nameTable = SqlTask.getNameTableByView(nameTable);
        ArrayList<String> alNames;

        alNames = SqlTask.getNamesTabBySQL(null, nameTable);
        nameTable = alNames.get(0);

        String[] names;

        if (HM_COL_CAPTION.containsKey(nameTable)) {

            names = HM_COL_CAPTION.get(nameTable);
        } else {

            names = getCaptionByTable(nameTable);
            HM_COL_CAPTION.put(nameTable, names);

        }

        if (names == null) {
            return result;
        }

        StringBuilder builder = new StringBuilder("");

        for (String name : names) {

            String cap = rs.getString(name);

            if (cap != null && !cap.isEmpty()) {
                builder.append(cap);
                builder.append(", ");
            }
        }

        if (builder.length() > 2) {
            builder.deleteCharAt(builder.length() - 2);
        }

        result = builder.toString();
        return result;
    }

    /**
     * Возвращает названия полей учавстующих в названии объекта
     *
     * @param nameTable Название таблицы
     */
    public static String[] getCaptionByTable(String nameTable) throws SQLException {

        String[] result = null;

        Document document = getDocConfigByTable(nameTable);

        String caption = document.getDocumentElement().getAttribute("caption");

        if (caption != null && !caption.isEmpty()) {

            result = caption.split(";");

        }
        return result;
    }

    /**
     * Возвращает Файл конфигурации по названию таблицы
     *
     * @param nameTable Название таблицы
     */
    public static Document getDocConfigByTable(String nameTable) throws SQLException {

        Document result = null;
        String sql = "SELECT c_base_property     FROM  c_obj_spec   WHERE  c_name_table='" + nameTable + "'";

        ResultSet rsThis = SqlTask.getResultSet(null, sql);

        try {

            if (rsThis.next()) {
                String sxml = rsThis.getString(1);
                try {
                    result = XmlTask.stringToXmlDoc(sxml);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }
            }
        } finally {
            rsThis.close();
        }
        return result;
    }

    /**
     * Возвращает строку между двумя символами
     *
     */
    public static String getDelimitedString(String from, char start, char end) {

        int startPos = from.indexOf(start);
        int endPos = from.lastIndexOf(end);

        if (startPos > endPos) {
            return null;
        } else if (startPos == -1) {
            return null;
        } else if (endPos == -1) {
            return from.substring(startPos);
        } else {
            return from.substring(startPos + 1, endPos);
        }

    }

    public static ArrayList<String> getStringNames(String format) {

        ArrayList<String> result = new ArrayList<String>();
        String SetChar = "1234567890@&abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";

        boolean bYes = false;

        String name = "";

        for (int i = 0; i < format.length(); i++) {

            char c = format.charAt(i);

            if (SetChar.indexOf(c) != -1) {

                name = name + c;

                bYes = true;

            } else {

                if (bYes) {

                    result.add(name);
                    name = "";
                    bYes = false;
                }
            }
        }

        if (!name.isEmpty()) {
            result.add(name);
        }

        return result;
    }

    /**
     * Разбор строки типа А=0; B=1; c=2
     *
     * @param nameCmd
     * @return
     */
    public static HashMap<String, String> getMapNameCmd(String nameCmd) {

        HashMap<String, String> result = new HashMap<String, String>();
        String[] ses = nameCmd.split(";");
        for (String ss : ses) {
            String[] ses1 = ss.split("=");
            result.put(ses1[0], ses1[1]);
        }
        return result;
    }

    /**
     * Разбор строки типа А=0; B=1; c=2
     *
     * @param nameCmd
     * @param typGet 0-key 1-value
     * @return
     */
    public static ArrayList<String> getListNameCmd(String nameCmd, int typGet) {

        ArrayList<String> result = new ArrayList<String>();
        String[] ses = nameCmd.split(";");
        for (String ss : ses) {
            String[] ses1 = ss.split("=");

            if (typGet == 0) {
                result.add(ses1[0]);

            } else {
                result.add(ses1[1]);
            }

        }
        return result;
    }

    public static String getCountersByPointJson(int idPoint) throws SQLException {

        String result = "";

        HashMap map = new HashMap();

        String sql = "SELECT *  FROM objects WHERE id_point=" + idPoint;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            while (rs.next()) {
                int id = rs.getInt("id_object");

                Map param = Work.getParametersRow(id, rs, "objects", true, true);
                String caption = (String) param.get("caption");
                Integer addres = (Integer) param.get("counter_addres");

                map.put(addres, caption);

            }

            result = getJsonStringByMap(map);

        } finally {
            rs.getStatement().close();
        }

        return result;

    }

    /**
     * Список счетчиков контроллера в виде номер счетчика=caption
     *
     * @param idPoint -id точки присоединения
     * @return
     */
    public static String getCountersByPoint(int idPoint) throws SQLException {

        String result = "";

        StringBuilder builder = new StringBuilder();

        String sql = "SELECT *  FROM objects WHERE id_point=" + idPoint;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            while (rs.next()) {
                int id = rs.getInt("id_object");

                Map param = Work.getParametersRow(id, rs, "objects", true, true);
                String caption = (String) param.get("caption");
                Integer addres = (Integer) param.get("counter_addres");

                builder.append(addres);
                builder.append("=");
                builder.append(caption);
                builder.append(";");
            }

            builder.deleteCharAt(builder.lastIndexOf(";"));

        } finally {
            rs.getStatement().close();
        }

        return builder.toString();

    }

    /**
     * Карта отсортированного столбца nameName и столбца nameValue в виде
     * nameName=nameValue
     *
     * @param nameTable -название таблицы
     * @param nameName ключ
     * @param nameValue значение
     * @return карту в виде nameName=nameValue
     * @throws SQLException
     */
    public static TreeMap<String, Object> getSortMapCaption(String nameTable, String nameName, String nameValue) throws SQLException {

        TreeMap<String, Object> result = new TreeMap<String, Object>();
        String sql = "SELECT " + nameName + "," + nameValue + " FROM " + nameTable;

        ResultSet rsCaption = null;

        String name;
        Object value;

        rsCaption = SqlTask.getResultSet(null, sql);

        try {

            while (rsCaption.next()) {
                name = rsCaption.getString(nameName);
                value = rsCaption.getObject(nameValue);
                result.put(name, value);

            }

        } finally {
            rsCaption.close();

        }

        return result;
    }

    /**
     * Возвращает строки по разделителю
     *
     * @param delim -символы раэделения строк
     */
    public static ArrayList<String> getListByDelim(String string, String delim) {
        ArrayList<String> arrayList = new ArrayList<String>();

        String a[] = string.split(delim);
        for (String ss : a) {
            arrayList.add(ss.trim());
        }
        return arrayList;
    }

    /**
     * Возвращает строки только те, у которых первые символы starts
     *
     * @param delim -символы раэделения строк
     */
    public static ArrayList<String> getListByStartsWith(String string, String starts, String delim) {

        ArrayList<String> arrayList = new ArrayList<String>();

        String a[] = string.split(delim);

        for (String ss : a) {

            if (ss.trim().startsWith(starts)) {

                String sval = ss.trim().substring(starts.length(), ss.trim().length());

                arrayList.add(sval);

            }

        }

        return arrayList;

    }

    /**
     * список задейственных полей в SQL запросе
     *
     * @param sql
     * @param nameTable
     * @return список задейственных полей в SQL запросе
     */
    public static ArrayList<String> getStatementList(String sql, String nameTable) throws SQLException {

        ArrayList<String> alCol = new ArrayList<String>();

        StringTokenizer tokenizer = new StringTokenizer(sql);

        ArrayList<String> al = SqlTask.getNamesCol(null, nameTable);

        while (tokenizer.hasMoreTokens()) {
            String val = tokenizer.nextToken();

            int idx = val.indexOf("#");

            if (idx != -1) {
                for (String nameCol : al) {

                    if (val.contains(nameCol)) {
                        alCol.add(nameCol);
                        break;
                    }
                }
            }
        }

        return alCol;
    }

    /**
     * Заменяет записи в таблице
     *
     * @param nameTable Имя таблицы
     * @param hmCol карта записываемых столбцов в виде имя-значение
     */
    public static void replaceRecInTable(String nameTable, HashMap<String, Object> hmKeys, HashMap<String, Object> hmCol) throws Exception {

        StringBuilder sbValues = new StringBuilder();
        PreparedStatement ps = null;
        String sql = "";

        if (hmCol == null || hmCol.isEmpty()) {
            return;
        }

        StringBuilder sbWhere = new StringBuilder();

        sbWhere.append(" WHERE ");

        for (String k : hmKeys.keySet()) {
            sbWhere.append(k);
            sbWhere.append("=?");
            sbWhere.append(" AND ");
        }

        int idx = sbWhere.lastIndexOf("AND");

        sbWhere.delete(idx, idx + 3);

        sbValues.append("UPDATE ");
        sbValues.append(nameTable);
        sbValues.append(" ");
        sbValues.append("SET ");

        for (String ncol : hmCol.keySet()) {

            sbValues.append(ncol);
            sbValues.append("=?,");

        }
        sbValues.deleteCharAt(sbValues.lastIndexOf(","));

        sbValues.append(" ");
        sbValues.append(sbWhere.toString());

        sql = sbValues.toString();

        ps = CONNECT_DBF.prepareStatement(sql);

        int i = 1;

        for (String ncol : hmCol.keySet()) {
            Object val = hmCol.get(ncol);

            if (val instanceof File) {
                File file = (File) val;

                FileInputStream fis = new FileInputStream(file);
                ps.setBinaryStream(i, fis, file.length());
                //  fis.close();

            } else {
                ps.setObject(i, val);
            }
            i++;
        }

        // добавляем условие
        for (Object v : hmKeys.values()) {
            ps.setObject(i, v);
            i++;
        }

        try {

            int iReplace = ps.executeUpdate();

            // Если не обновил, значит новая запись
            if (iReplace < 1) {

                insertRecInTable(nameTable, hmCol);

            }

        } finally {

            ps.close();
        }

    }

    /**
     * Заменяет записи в таблице
     *
     * @param nameTable Имя таблицы
     * @param bInsert если true то можно вставлять новые false -только обновлять
     * старые
     * @param hmCol карта записываемых столбцов в виде имя-значение
     */
    public static void replaceRecInTable(String nameTable, HashMap<String, Object> hmCol, boolean bInsert) throws Exception {

        StringBuilder sbValues = new StringBuilder();
        PreparedStatement ps = null;
        ArrayList<String> alKeys;

        String sql = "";

        if (hmCol == null || hmCol.isEmpty()) {
            return;
        }

        alKeys = SqlTask.getPrimaryKey(null, nameTable);

        if (alKeys.isEmpty()) {

            throw new NullPointerException(nameTable + "-Нет главного ключа!");
        }

        for (String s : alKeys) {

            if (!hmCol.containsKey(s)) {

                throw new NullPointerException(nameTable + "- нет ключа " + s);
            }

        }

        StringBuilder sbWhere = new StringBuilder();

        sbWhere.append(" WHERE ");

        for (String k : alKeys) {
            sbWhere.append(k);
            sbWhere.append("=?");
            sbWhere.append(" AND ");
        }

        int idx = sbWhere.lastIndexOf("AND");

        sbWhere.delete(idx, idx + 3);

        sbValues.append("UPDATE ");
        sbValues.append(nameTable);
        sbValues.append(" ");
        sbValues.append("SET ");

        for (String ncol : hmCol.keySet()) {

            sbValues.append(ncol);
            sbValues.append("=?,");

        }
        sbValues.deleteCharAt(sbValues.lastIndexOf(","));

        sbValues.append(" ");
        sbValues.append(sbWhere.toString());

        sql = sbValues.toString();

        ps = CONNECT_DBF.prepareStatement(sql);

        int i = 1;

        for (String ncol : hmCol.keySet()) {
            Object val = hmCol.get(ncol);

            if (val instanceof File) {
                File file = (File) val;

                FileInputStream fis = new FileInputStream(file);
                ps.setBinaryStream(i, fis, file.length());
                fis.close();

            } else {
                ps.setObject(i, val);
            }
            i++;
        }

        // добавляем условие
        for (String k : alKeys) {

            Object whe = hmCol.get(k);

            ps.setObject(i, whe);
            i++;
        }

        try {

            int iReplace = ps.executeUpdate();

            // Если не обновил, значит новая запись
            if (iReplace < 1 && bInsert) {

                insertRecInTable(nameTable, hmCol);

            }

        } finally {

            ps.close();
        }

    }

    /**
     * Проверка формальных требований к конфигурациям таблиц.
     *
     * @return
     * @throws SQLException
     */
    public static void checkDemand(List listErrors) throws SQLException {

    }

    public static void ShowError(String error) {

        JOptionPane.showMessageDialog(null, error, "Ошибка", JOptionPane.ERROR_MESSAGE);

    }

    /**
     * Список объектов по логину и паролю
     *
     * @param login
     * @param password
     * @return
     */
    public static List isOkPassword(String login, String password) throws SQLException {

        List result = null;

        String sql = "SELECT id_subconto FROM subconto WHERE login_subconto=? AND password_subconto=?";

        ResultSet resultSet = SqlTask.getResultSet(null, sql, new Object[]{login, password});

        int id = -1;

        try {

            if (resultSet.next()) {

                id = resultSet.getInt("id_subconto");
            }

        } finally {
            resultSet.close();
        }

        if (id == -1) {

            return result;
        }

        sql = "SELECT * FROM objects,points,counters WHERE  objects.id_point=points.id_point AND objects.id_counter=counters.id_counter  AND objects.id_subconto=?";

        result = SqlTask.getArrayMapBySQL(null, sql, new Object[]{id});

        return result;
    }

//HashMap<Integer,HashMap<String,Object>>
    public static Double convert132byte(int b0, int b1, int b2, int shcale) {

        Double result;

        result = (b0 * Math.pow(256, 2))
                + (b2 * 256) + b1;

        if (b0 == 0 && b1 == 0 && b2 == 0) {

            result = 0.0;
        } else {

            result = result / shcale;
        }

        return result;
    }

    static void updateProfil(int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // Загрузка обновлений
    public void setUpdate() {
    }

    /**
     *
     * @param val ,байт 1 2 3 4 5 6 7 8
     * @param poz позиция байта ( с единицы, слева направо) 7 6 5 4 3 2 1 0
     * @param len количество байт
     * @return число из выделенных байт
     */
    public static int getIntbyBits(int val, int poz, int len) {

        int result = val;

        int v = 256 | val;

        String s = Integer.toBinaryString(v);

        String d = s.substring(poz, poz + len);

        result = Integer.parseInt(d, 2);

        return result;
    }

    /**
     * Конверируем байты в число
     *
     * @param al - массив вайт
     * @param start -позиция первого байта
     * @param count-количество байт
     * @param Direction тип конвертации (от младшего к старшему от старшего к
     * младшему)
     * @return
     */
    public static long convertByte(List<Short> al, int start, int count, int Direction) {

        long result = 0;

        if (Direction == B_LITTLE_ENDIAN) {

            for (int i = 0; i < count; i++) {

                result = (long) (result + al.get(i + start) * Math.pow(256, count - i - 1));

            }

        } else {

            for (int i = count - 1; i > -1; i--) {

                result = (long) (result + al.get(i + start) * Math.pow(256, i));
            }

        }
        //    res := (arr[0] * Intpower(256, 3)) + (arr[1] * Intpower(256, 2)) +
        //   (arr[2] * 256) + arr[3];

        return result;
    }

    /**
     * Возвращает имена столбцов таблицы
     *
     * @param nameTable -имя таблицы
     * @return имя
     */
    public static ArrayList<String> getNamesCol(String nameTable) throws SQLException {

        ArrayList<String> alCol = new ArrayList<String>();

        DatabaseMetaData data;
        data = CONNECT_DBF.getMetaData();

        ResultSet resultSet = data.getColumns(null, null, nameTable, null);

        try {
            while (resultSet.next()) {

                String nameCol = resultSet.getString(4);

                alCol.add(nameCol);
            }
        } finally {
            resultSet.close();
        }

        return alCol;
    }

    /**
     * Карту атрибутов элемента
     *
     * @param element
     * @return
     * @throws SQLException
     */
    public static HashMap<String, String> getMapAttributes(Element element) {

        HashMap<String, String> result = new HashMap<>();

        NamedNodeMap map = element.getAttributes();

        for (int j = 0; j < map.getLength(); j++) {

            Node item = map.item(j);

            String name = item.getNodeName();

            String value = item.getNodeValue();

            result.put(name, value);
        }

        return result;

    }

    /**
     * Возвращает имена и любой другой параметр столбца по номеру
     *
     * @param nameTable -имя таблицы
     * @return имя=тип
     */
    public static HashMap<String, Object> getMapNamesCol(String nameTable, int nParam) throws SQLException {

        HashMap<String, Object> hmValues = new HashMap<String, Object>();

        DatabaseMetaData data;
        data = CONNECT_DBF.getMetaData();

        ResultSet resultSet = data.getColumns(null, null, nameTable, null);

        try {
            while (resultSet.next()) {

                String nameCol = resultSet.getString(4);

                Object Param = resultSet.getObject(nParam);

                hmValues.put(nameCol, Param);
            }
        } finally {
            resultSet.close();
        }

        return hmValues;
    }

    /**
     * Возвращает имена и типы столбцов таблицы
     *
     * @param nameTable -имя таблицы
     * @return имя=тип
     */
    public static HashMap<String, Integer> getNameAndTypeCol(String nameTable) throws SQLException {

        HashMap<String, Integer> hmValues = new HashMap<String, Integer>();

        DatabaseMetaData data;
        data = CONNECT_DBF.getMetaData();

        ResultSet resultSet = data.getColumns(null, null, nameTable, null);

        try {
            while (resultSet.next()) {

                String nameCol = resultSet.getString(4);

                int typCol = resultSet.getInt(5);

                hmValues.put(nameCol, typCol);
            }
        } finally {
            resultSet.close();
        }

        return hmValues;
    }

    /**
     * Возвращает первичный ключ таблицы в базе данных
     *
     * @return Название первичного ключа
     */
    public static String getPrimaryKeyTable(String nameTable) throws SQLException {
        ResultSet rs = null;
        String name = null;

        StringBuilder builder = new StringBuilder();

        try {
            DatabaseMetaData meta = CONNECT_DBF.getMetaData();

            rs = meta.getPrimaryKeys(null, null, nameTable);

            while (rs.next()) {
                name = rs.getString(4);

                builder.append(name);
                builder.append(";");

            }
        } finally {
            rs.close();
        }

        int lidx = builder.lastIndexOf(";");

        if (lidx != -1) {
            builder.deleteCharAt(lidx);
        }
        return builder.toString();
    }

    /**
     * Возвращает названия всех существующих таблиц и представлений в базе
     * данных
     *
     * @return HashMap названий таблиц=typ "TABLE", "VIEW" если bAll false то
     * только таблицы иначе все
     */
    public static HashMap<String, String> getNameAndTypeTables(boolean bAll) throws SQLException {
        HashMap<String, String> tables = null;
        ResultSet rs = null;
        try {
            DatabaseMetaData meta = CONNECT_DBF.getMetaData();
            tables = new HashMap<String, String>();

            if (bAll) {
                rs = meta.getTables(null, null, null, new String[]{"TABLE", "VIEW"});
            } else {
                rs = meta.getTables(null, null, null, new String[]{"TABLE"});

            }

            while (rs.next()) {
                String name = rs.getString(3).toLowerCase();
                String type = rs.getString(4).toUpperCase();

                tables.put(name, type);

            }
        } finally {
            rs.close();
        }
        return tables;
    }

    /**
     * Возвращает названия всех существующих таблиц и представлений в базе
     * данных
     *
     * @return TreeSet названий таблиц
     */
    public static TreeSet<String> getNameTables() throws SQLException {
        TreeSet<String> tables = null;
        ResultSet rs = null;
        try {
            DatabaseMetaData meta = CONNECT_DBF.getMetaData();
            tables = new TreeSet<String>();
            rs = meta.getTables(null, null, null, new String[]{"TABLE", "VIEW"});

            while (rs.next()) {
                String name = rs.getString(3).toLowerCase();
                tables.add(name);

            }
        } finally {
            rs.close();
        }
        return tables;
    }

    public static String getNameTableByTypObject(int iTyp) throws SQLException {

        String result = "";
        String sql = "SELECT c_name_table FROM c_obj_spec WHERE c_obj_type=" + iTyp;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                result = rs.getString("c_name_table");
            }

        } finally {
            rs.close();
        }
        return result;
    }

    /**
     * Возвращает RS по имени или id sql запроса
     *
     * @param idSQL- id или Имя
     * @throws SQLException
     */
    public static ResultSet getResSetByNameSql(Object idSQL) throws SQLException {

        ResultSet rs = null;

        ResultSet result = null;
        String sql;

        if (idSQL instanceof String) {
            sql = "SELECT * FROM sql_make WHERE name_sql=?";
        } else {
            sql = "SELECT * FROM sql_make WHERE id_sql=?";
        }

        rs = SqlTask.getResultSet(null, sql, new Object[]{idSQL});

        try {
            if (rs.next()) {

                sql = rs.getString("sql_string");

                result = SqlTask.getResultSet(null, sql);

            }
        } finally {

            rs.close();
        }
        return result;
    }

    /**
     *
     * @param id
     * @param rsObjects
     * @return
     */
    public static HashMap<String, Object> getParametersObject(Integer id, ResultSet rsObjects, boolean all, boolean json, boolean caption) throws SQLException {

        ResultSet rs;
        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {

            rs = rsObjects;

            SqlTask.addParamToMap(rs, hm);

        } else {

            sql = "SELECT * FROM  objects WHERE  id_object=" + id;

            rs = SqlTask.getResultSet(null, sql);

            try {

                if (rs.next()) {
                    SqlTask.addParamToMap(rs, hm);
                }

            } finally {
                rs.close();
            }

        }

        if (all) {

            Integer id_point = (Integer) hm.get("id_point");
            Integer id_subconto = (Integer) hm.get("id_subconto");
            Integer id_counter = (Integer) hm.get("id_counter");

            if (id_point != null) {
                sql = "SELECT * FROM  points WHERE  id_point=" + id_point;

                ResultSet rsPoint = SqlTask.getResultSet(null, sql);

                try {
                    if (rsPoint.next()) {
                        SqlTask.addParamToMap(rsPoint, hm);

                    }

                } finally {
                    rsPoint.close();
                }

            }

            if (id_subconto != null) {
                sql = "SELECT * FROM  subconto WHERE  id_subconto=" + id_subconto;

                ResultSet rsSub = SqlTask.getResultSet(null, sql);

                try {
                    if (rsSub.next()) {
                        SqlTask.addParamToMap(rsSub, hm);

                    }

                } finally {
                    rsSub.close();
                }

            }

            if (id_counter != null) {
                sql = "SELECT * FROM  counters WHERE  id_counter=" + id_counter;

                ResultSet rsCount = SqlTask.getResultSet(null, sql);

                try {
                    if (rsCount.next()) {
                        SqlTask.addParamToMap(rsCount, hm);

                    }

                } finally {
                    rsCount.close();
                }
            }

        }

        if (json) {

            Integer id_object = (Integer) hm.get("id_object");

            Map m = Work.getJsonParameter(id_object);

            hm.putAll(m);

        }

        if (caption) {

            String n1 = (String) hm.get("dis_number");
            String n2 = (String) hm.get("name1");
            n1 = ((n1 != null && !n1.isEmpty()) ? n1 : "");
            n2 = ((n2 != null && !n2.isEmpty()) ? n2 : "");
            String name;

            name = n1 + ", " + n2;

            if (n1.isEmpty()) {
                name = n2;

            }
            if (n2.isEmpty()) {
                name = n1;

            }

            hm.put("caption", name);
        }

        return hm;

    }

    /**
     *
     * @param id
     * @return
     */
    public static HashMap<String, Object> getParametersObject(Integer id, ResultSet rsObjects) throws Exception {

        ResultSet rs;
        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {

            rs = rsObjects;

        } else {

            sql = "SELECT * FROM  objects WHERE  id_object=" + id;

            rs = SqlTask.getResultSet(null, sql);
        }

        if (rsObjects == null) {

            if (!rs.next()) {
                return hm;
            }

        }

        SqlTask.addParamToMap(rs, hm);

        Document docConfig = getDocConfigByName(sql, hm);

// тип объекта
        String typObject = rs.getString("typ_object");

        return hm;

    }

    /**
     * Возвращает карту параметров по его id
     *
     *
     */
    public static HashMap<String, Object> getParametersRow(Integer id, ResultSet rsObjects, String nameTable, boolean bSubType, boolean bCaption) throws SQLException {

        ResultSet rs;
        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {
            SqlTask.addParamToMap(rsObjects, hm);

        } else {

            String nameKey = SqlTask.getPrimaryKeyTable(null, nameTable);

            sql = "SELECT * FROM " + nameTable + " WHERE  " + nameKey + "=" + id;

            rs = SqlTask.getResultSet(null, sql);

            try {

                if (rs.next()) {
                    SqlTask.addParamToMap(rs, hm);
                }
            } finally {

                rs.close();

            }

        }

        if (bCaption) {

            String cap = getCaptionObject(hm, nameTable);
            hm.put("caption", cap);
        }

        if (bSubType) {

            HashMap<String, Object> hmSub = new HashMap<String, Object>();

            String subTable = "";
            Integer subId = -1;

            for (String nameCol : hm.keySet()) {

                subId = -1;

                if (nameCol.equals("id_point")) {

                    subTable = "points";
                    subId = (Integer) hm.get(nameCol);
                } else if (nameCol.equals("id_counter")) {

                    subTable = "counters";
                    subId = (Integer) hm.get(nameCol);
                } else if (nameCol.equals("id_subconto")) {
                    subId = (Integer) hm.get(nameCol);
                    subTable = "subconto";
                }

                if (subId == null || subId == -1) {
                    continue;
                }

                sql = "SELECT * FROM " + subTable + " WHERE  " + nameCol + "=" + subId;
                ResultSet rsSub = SqlTask.getResultSet(null, sql);

                try {

                    if (rsSub.next()) {

                        SqlTask.addParamToMap(rsSub, hmSub);
                    }

                } finally {
                    rsSub.close();
                }

            }

            hm.putAll(hmSub);
        }

        return hm;
    }

    /**
     * Возвращает карту параметров по его id
     *
     *
     */
    public static HashMap<String, Object> getParamById(Integer id, ResultSet rsObjects, String nameTable, boolean bSubType, boolean bCaption) throws SQLException {

        ResultSet rs;
        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {

            rs = rsObjects;

        } else {

            String nameKey = SqlTask.getPrimaryKeyTable(null, nameTable);

            sql = "SELECT * FROM " + nameTable + " WHERE  " + nameKey + "=" + id;

            rs = SqlTask.getResultSet(null, sql);
        }

        try {

            if (rsObjects == null) {

                if (!rs.next()) {
                    return hm;
                }

            }

            SqlTask.addParamToMap(rs, hm);

            if (bCaption) {

                String cap = getCaptionObjectByCol(rs, 1);
                hm.put("caption", cap);
            }

            if (bSubType) {

                HashMap<String, Object> hmSub = new HashMap<String, Object>();

                for (String nameCol : hm.keySet()) {

                    if (nameCol.startsWith("sub_type")) {

                        Integer subId = (Integer) hm.get(nameCol);

                        if (subId == null || subId == -1) {
                            continue;
                        }

                        String sType = nameCol.replaceFirst("sub_type", "");
                        int iTyp = Integer.decode(sType);
                        String nameSub = "";

                        sql = "SELECT c_name_table FROM c_obj_spec WHERE c_obj_type=" + iTyp;

                        ResultSet rs1 = SqlTask.getResultSet(null, sql);

                        try {
                            if (rs1.next()) {
                                nameSub = rs1.getString("c_name_table");
                                String nameKey = SqlTask.getPrimaryKeyTable(null, nameSub);
                                sql = "SELECT * FROM " + nameSub + " WHERE  " + nameKey + "=" + subId;
                                ResultSet rs2 = SqlTask.getResultSet(null, sql);

                                try {

                                    if (rs2.next()) {

                                        hmSub.clear();
                                        SqlTask.addParamToMap(rs2, hmSub);
                                        hmSub.remove("c_tree_id");
                                        hm.putAll(hmSub);
                                    }

                                } finally {
                                    rs2.close();
                                }

                            }

                        } finally {
                            rs1.close();
                        }
                    }
                }

            }

        } finally {

            if (rsObjects == null) {
                rs.close();
            }
        }

        return hm;
    }

    /**
     * Возвращает карту параметров Отчета по его id
     *
     *
     */
    public static HashMap<String, Object> getParametersSxem(Integer id, ResultSet rsObjects) throws SQLException {

        ResultSet rs;

        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {

            rs = rsObjects;

        } else {

            sql = "SELECT * FROM object10 WHERE  c_tree_id=" + id;

            rs = SqlTask.getResultSet(null, sql);
        }

        try {

            if (rsObjects == null) {

                if (!rs.next()) {
                    return hm;
                }

            }

            SqlTask.addParamToMap(rs, hm);

            String caption = null;

            String c1 = (String) hm.get("name_sxem");
            caption = c1;

            hm.put("caption", caption);

        } finally {

            if (rsObjects == null) {
                rs.close();
            }
        }

        return hm;
    }

    /**
     * Возвращает карту параметров Запроса к группе объектов по его id
     *
     *
     */
    public static HashMap<String, Object> getParametersGroupGet(Integer id, ResultSet rsObjects) throws SQLException {

        ResultSet rs;

        String sql = "";

        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (rsObjects != null) {

            rs = rsObjects;

        } else {

            sql = "SELECT * FROM val_group_get WHERE  id_group_get=" + id;

            rs = SqlTask.getResultSet(null, sql);
        }

        try {

            if (rsObjects == null) {

                if (!rs.next()) {
                    return hm;
                }

            }

            SqlTask.addParamToMap(rs, hm);

            String caption = null;

            String c1 = (String) hm.get("name_report");
            caption = c1;

            hm.put("caption", caption);

        } finally {

            if (rsObjects == null) {
                rs.close();
            }
        }

        return hm;
    }

    /**
     * Возвращает строковое представление значения в зависимости от его типа
     *
     * @param DeffValue
     * @param typCol
     * @return
     */
    public static String getStringValueByTyp(Object DeffValue) {

        String result = null;

        if (DeffValue instanceof Timestamp) {

            DateTime dateTime;
            Timestamp timestamp = (Timestamp) DeffValue;

            dateTime = new DateTime(timestamp);

            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
            result = dateTime.toString(dtf);

        } else {
            result = DeffValue.toString();
        }

        return result;
    }

    public static HashMap<String, Object> getDeffValueByNameConst(String nameConst) throws Exception {

        HashMap<String, Object> hmResult = new HashMap<>();

        Document document = null;

        String sql = "SELECT c_const_value  FROM c_const   WHERE  c_const_name=?";

        ResultSet rsThis = SqlTask.getResultSet(null, sql, new Object[]{nameConst});

        try {

            if (rsThis.next()) {
                String sxml = rsThis.getString(1);
                try {
                    document = XmlTask.stringToXmlDoc(sxml);
                } catch (Exception ex) {

                    throw new SQLException(ex);
                }

            }

        } finally {
            rsThis.close();
        }

        if (document != null) {
            HashMap<String, String> map = XmlTask.getMapAttrubuteByName(document.getDocumentElement(), "name", "default_value", "cell");

            for (String s : map.keySet()) {
                hmResult.put(s, map.get(s));

            }

        }

        return hmResult;

    }

    public static Object getValueByTyp(String DeffValue, String typCol) throws NullPointerException {

        Object result = null;

        if (typCol.equalsIgnoreCase(Integer.class
                .getSimpleName())) {

            try {
                result = new Integer(DeffValue.trim());

            } catch (NumberFormatException nfe) {

                result = new Integer(0);
            }

        } else if (typCol.equalsIgnoreCase(Float.class
                .getSimpleName())) {

            try {
                result = new Float(DeffValue.trim());

            } catch (NumberFormatException nfe) {
                result = new Float(0);
            }

        } else if (typCol.equalsIgnoreCase(Double.class
                .getSimpleName())) {

            try {
                result = new Double(DeffValue);
            } catch (NumberFormatException nfe) {
                result = new Double(0);
            }

        } else if (typCol.equalsIgnoreCase(Boolean.class
                .getSimpleName())) {

            Boolean.getBoolean(DeffValue.trim());

        } else if (typCol.equalsIgnoreCase(Short.class
                .getSimpleName())) {

            try {
                result = new Short(DeffValue.trim());
            } catch (NumberFormatException nfe) {
                short s = 0;
                result = new Short(s);
            }
        } else if (typCol.equalsIgnoreCase(Byte.class
                .getSimpleName())) {

            try {
                result = new Byte(DeffValue.trim());
            } catch (NumberFormatException nfe) {
                byte s = 0;
                result = new Byte(s);
            }
        } else if (typCol.equalsIgnoreCase(Timestamp.class
                .getSimpleName())) {

            DateTime dateTime;
            Timestamp timestamp;

            try {

                DateTimeFormatter dtf;

                if (DeffValue == null || DeffValue.isEmpty()) {

                    dateTime = new DateTime();
                    timestamp = new Timestamp(dateTime.getMillis());

                } else {

                    if (DeffValue.indexOf("-") == -1) {

                        dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

                    } else {

                        dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");

                    }

                    dateTime = dtf.parseDateTime(DeffValue.trim());
                    timestamp = new Timestamp(dateTime.getMillis());

                }
                result = timestamp;

            } catch (Exception e) {

                //   DbfClass.setLog("Проверте ввод даты !", e);
                dateTime = new DateTime();
                timestamp = new Timestamp(dateTime.getMillis());

                result = timestamp;

            }

        } else if (typCol.equalsIgnoreCase(String.class
                .getSimpleName())) {
            result = DeffValue.trim();

        } else {
            // Такого типа нет !

            result = null;

            // throw new NullPointerException("Тип " + typCol + " в базе данных не предусмотрен!");
        }

        return result;
    }

    public static Object getDeffValueByTypCol111(String nameValue, String DeffValue, String nameTable) throws SQLException, NullPointerException {

        Object result = null;

        HashMap<String, Object> hmTypes = getMapNamesCol(nameTable, 5);

        Integer iTyp = (Integer) hmTypes.get(nameValue);

        if (iTyp == Types.VARCHAR || iTyp == Types.CHAR) {

            result = DeffValue;

        } else if (iTyp == Types.BIT) {
            try {
                result = Byte.parseByte(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.SMALLINT) {
            try {
                result = Short.parseShort(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.TINYINT) {
            try {
                result = Byte.parseByte(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.INTEGER) {
            try {
                result = Integer.parseInt(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.BIGINT) {
            try {
                result = Long.parseLong(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.BOOLEAN) {
            try {
                result = Boolean.parseBoolean(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.DECIMAL) {
            try {
                result = Double.parseDouble(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.DOUBLE) {
            try {
                result = Double.parseDouble(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.FLOAT) {
            try {
                result = Float.parseFloat(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.NUMERIC) {
            try {
                result = Double.parseDouble(DeffValue);

            } catch (NumberFormatException nfe) {

                result = 0;
            }

        } else if (iTyp == Types.DATE || iTyp == Types.TIMESTAMP) {

            DateTimeFormatter dtf;
            Timestamp timestamp;
            DateTime dateTime;

            if (DeffValue == null || DeffValue.isEmpty()) {

                dateTime = new DateTime();
                timestamp = new Timestamp(dateTime.getMillis());

            } else {
                if (DeffValue.indexOf("-") == -1) {

                    dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

                } else {

                    dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");

                }

                dateTime = dtf.parseDateTime(DeffValue.trim());
                timestamp = new Timestamp(dateTime.getMillis());

            }
            result = timestamp;
        } else {

            throw (new NullPointerException("Не распознанный формат данных !"));
        }
        return result;
    }

    public static Object getDeffValue(String nameValue, String DeffValue, Object nameTableORmap) throws Exception {
        Object result = null;
        Map hmType = null;
        String nameTable;

        if (DeffValue == null) {
            return DeffValue;
        }

        if (nameTableORmap == null) {
            return DeffValue;

        }

        if (nameTableORmap instanceof String) {

            nameTable = (String) nameTableORmap;

            if (nameTable == null || nameTable.isEmpty()) {
                return DeffValue;

            }

            hmType = SqlTask.getNameAndTypeCol(null, nameTable);

            // hmType = (HashMap<String, String>) Work.getParamTableByName(nameTable, Work.TABLE_TYPE_COL);
        }

        // для новой базы
        if (nameTableORmap instanceof Map) {

            hmType = (HashMap<String, String>) nameTableORmap;

        }

        String typCol = null;
        if (hmType.containsKey(nameValue)) {

            result = DeffValue;
        } else {

            throw new NullPointerException("Столбца " + nameValue + "в xml Конфигурации нет !");
        }

        return result;
    }

    /**
     * Название таблицы и код хранимого параметра по представлению
     *
     * @param nameView -имя представления
     * @return
     */
    public static Object[] getNameTabAndKodByView(String nameView) throws SQLException {

        Object[] objects = new Object[2];

        Integer iKod = null;
        String nameTable;
        ResultSet rs = null;
        String sql = "SELECT * FROM " + nameView;

        try {
            rs = SqlTask.getResultSet(null, sql, 1, ResultSet.CONCUR_READ_ONLY);

            nameTable = rs.getMetaData().getTableName(1).toLowerCase();

            if (rs.next() && !nameTable.equals(nameView)) {
                iKod = rs.getInt("parnumber_id");

            }

        } finally {
            rs.close();
        }

        objects[0] = nameTable;
        objects[1] = iKod;

        return objects;
    }

    /**
     * Карта в виде позиция=имя
     *
     * @param idPar -тип параметра
     * @return
     */
    public static HashMap<Integer, String> getMapMamesByIdParam(int idPar) throws SQLException {

        HashMap<Integer, String> result = new HashMap<Integer, String>();

        String sql = "SELECT prm_name_tbl,prm_poz_cmd   FROM c_parnumber WHERE c_partype_id=" + idPar;

        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            while (rs.next()) {
                int poz = rs.getInt("prm_poz_cmd");
                String name = rs.getString("prm_name_tbl");
                result.put(poz, name);

            }
        } finally {
            rs.close();
        }

        return result;
    }

    /**
     * Параметры контроллера по id точке присоединения.
     *
     * @param idPoint -id присоединения.
     * @return карта параметров контроллера
     */
    public static HashMap<String, Object> getMapController(Integer idPoint) throws SQLException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String sql = "SELECT * FROM controllers WHERE id_point=?";

        ResultSet rs;

        rs = SqlTask.getResultSet(null, sql, new Object[]{idPoint});
        try {

            if (rs.next()) {
                SqlTask.addParamToMap(rs, result);
            }

        } finally {

            rs.close();
        }

        return result;
    }

    /**
     * Ищет id таблицы по id Объекта
     *
     * @param id id Объекта
     * @return id Таблицы
     */
    public static Integer getIdTableByObjectId(int id) throws SQLException {
        Integer result = null;
        String sql = "SELECT c_obj_list_id FROM c_tree WHERE c_tree_id=" + id;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {
                result = rs.getInt("c_obj_list_id");
                return result;
            } else {

                return result;

            }

        } finally {
            rs.getStatement().close();
        }

    }

    /**
     * Ищет Название таблицы по id Объекта
     *
     * @param id id Объекта
     * @return id Таблицы
     */
    public static String getNameTableByObjectId(int id) throws SQLException {
        String result = null;
        String sql = "SELECT c_obj_list_id FROM c_tree WHERE c_tree_id=" + id;

        ResultSet rs = SqlTask.getResultSet(null, sql);
        ResultSet rs1 = null;

        try {

            if (rs.next()) {
                int id_tab = rs.getInt("c_obj_list_id");

                sql = "SELECT c_name_table FROM c_obj_spec  WHERE c_obj_list_id=" + id_tab;

                try {

                    rs1 = SqlTask.getResultSet(null, sql);
                    if (rs1.next()) {
                        result = rs1.getString("c_name_table");
                    }
                } finally {

                    rs1.getStatement().close();
                }
                return result;
            } else {
                throw new SQLException("Нет таблицы для объекта " + id);
            }

        } finally {
            rs.getStatement().close();
        }

    }

    /**
     * Возвращает имя столбца -первичного ключа по названию таблицы о
     */
    public static HashMap<String, Object> getMapPrimaryKey(String nameTable) throws SQLException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        DatabaseMetaData databaseMetaData = CONNECT_DBF.getMetaData();

        ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, nameTable.toUpperCase());
        try {
            while (resultSet.next()) {

                String key = resultSet.getString(4).toLowerCase();

                result.put(key, null);
            }

        } finally {
            resultSet.close();
        }
        return result;

    }

    /**
     * Возвращает имена полей данных по имени таблицы и биту в байте =1 в поле
     * prm_flag (если они есть в таблице c_parnumber и их коды
     *
     * @param nameTable -Таблица данных
     * @return
     */
    public static HashMap<String, Object> getNameColValueByNameTable(String nameTable, int bytSet, String nameValue) throws SQLException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        Object[] objects = Work.getNameTabAndKodByView(nameTable);

        Integer kodParam = (Integer) objects[1];

        nameTable = (String) objects[0];

        String sql;

        if (kodParam != null) {
            sql = "SELECT * FROM c_parnumber WHERE c_partype_id=" + kodParam + " AND  name_table='" + nameTable + "'";

        } else {

            sql = "SELECT * FROM c_parnumber WHERE name_table='" + nameTable + "'";

        }

        ResultSet rsNames = null;

        rsNames = SqlTask.getResultSet(null, sql);

        try {
            while (rsNames.next()) {

                int flag = rsNames.getInt("prm_flag");

                Integer tarif = 0;

                tarif = rsNames.getInt("prm_tarif");

                if (tarif == null) {
                    tarif = 0;
                }

                if (BitSetEx.isBitSet(flag, bytSet)) {
                    String s = rsNames.getString("prm_name_tbl") + "#" + tarif;

                    // String s = rsNames.getString("prm_name_tbl");
                    Object t = rsNames.getObject(nameValue);
                    result.put(s, t);
                }

            }

        } finally {
            rsNames.getStatement().close();
        }

        return result;
    }

    /**
     * Возвращает карту параметров из константы
     *
     * @param consName -имя сонстанты
     * @return
     */
    public static HashMap<String, Object> getParametersFromConst(String consName) throws Exception {

        HashMap<String, Object> hmParam = new HashMap<String, Object>();

        String sql = "SELECT c_const_value FROM c_const WHERE c_const_name='" + consName.trim() + "'";
        ResultSet rs;
        rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {

                String xml = rs.getString(1);
                hmParam = XmlTask.getMapValuesByXML(xml, "name", "value", "cell");
            }
        } finally {
            rs.getStatement().close();
        }

        return hmParam;
    }

    /**
     * Возвращает имена таблиц
     *
     * @param typTable тип таблицы если Null то по всем
     * @return
     */
    public static ArrayList<String> getAllNameTablesByTyp(Integer typTable) throws SQLException {

        String sql;

        if (typTable != null) {
            sql = "SELECT c_name_table FROM c_obj_spec WHERE c_obj_type=" + typTable;
        } else {

            sql = "SELECT c_name_table FROM c_obj_spec";

        }

        ArrayList<String> result = new ArrayList<String>();

        ResultSet resultSet = SqlTask.getResultSet(null, sql);

        try {
            while (resultSet.next()) {
                String name = resultSet.getString("c_name_table");
                result.add(name);
            }

        } finally {
            resultSet.getStatement().close();
        }

        return result;
    }

    /**
     * Возврат индексов таблицы
     *
     * @param nameTable
     * @return
     */
    public static ArrayList<String> getArrayIndexTable(String nameTable) throws SQLException {

        ArrayList<String> result = new ArrayList<String>();
        DatabaseMetaData databaseMetaData = CONNECT_DBF.getMetaData();
        ResultSet resultSet = databaseMetaData.getIndexInfo(null, null, nameTable.toUpperCase(), false, false);

        try {

            while (resultSet.next()) {

                String nameIndex = resultSet.getString(9);

                if (nameIndex != null) {
                    nameIndex = nameIndex.toLowerCase();
                    result.add(nameIndex);

                }

            }

        } finally {
            resultSet.close();
        }

        return result;

    }

   

    public static Integer getIdTableByNameTable(String nameTable) throws SQLException {

        Integer result = null;

        String sql = "SELECT c_obj_list_id FROM c_obj_spec WHERE c_name_table='" + nameTable + "'";
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            while (rs.next()) {

                result = rs.getInt("c_obj_list_id");

            }
        } finally {
            rs.close();
        }

        return result;

    }

    public static Object getParTable(ResultSet rowSet, String nameParam) throws Exception {

        Object result = null;
        String sql;

        while (rowSet.next()) {

            if (nameParam.equals(TABLE_ID)) {

                Integer id = rowSet.getInt("c_obj_list_id");

                result = id;

            } else if (nameParam.equals(TABLE_NAME)) {

                String name = rowSet.getString("c_name_table");

                result = name;

            } else if (nameParam.equals(TABLE_PRIMARY_KEY)) {

                String name = rowSet.getString("c_name_table");

                ArrayList primaryKey = SqlTask.getPrimaryKey(null, name);

                result = primaryKey;

            } else if (nameParam.equals(TABLE_INDEX)) {

                String name = rowSet.getString("c_name_table");

                ArrayList<String> alIndex = SqlTask.getArrayIndexTable(null, name);

                result = alIndex;

            } else if (nameParam.equals(TABLE_PROPERTY)) {

                String prop = rowSet.getString("c_base_property");

                result = prop;

            } else if (nameParam.equals(TABLE_CAPTION_COL)) {

                String prop = rowSet.getString("c_base_property");

                HashMap<String, String> hmKey = XmlTask.getMapAttrByXML(prop, "name", "caption", "cell");
                result = hmKey;

            } else if (nameParam.equals(TABLE_CAPTION)) {

                String captionTab = rowSet.getString("c_obj_list_name");

                result = captionTab;
            } else if (nameParam.equals(TABLE_TYPE_COL)) {

                String prop = rowSet.getString("c_base_property");

                HashMap<String, String> hmType = XmlTask.getMapAttrByXML(prop, "name", "type", "cell");
                result = hmType;

            } else if (nameParam.equals(TABLE_TYPE)) {

                Integer iTyp = rowSet.getInt("c_obj_type");

                result = iTyp;

            } else if (nameParam.equals(TABLE_LEVEL)) {

                Integer iLevel = rowSet.getInt("c_level");

                result = iLevel;

            } else if (nameParam.equals(TABLE_AUTO_INCREMENT)) {

                return false;

            } else if (nameParam.equals(TABLE_PROPERTY_XML)) {

                String prop = rowSet.getString("c_base_property");

                Document d;
                try {
                    d = XmlTask.stringToXmlDoc(prop);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }

                result = d;
            } else if (nameParam.equals(TABLE_ORDER_NAME)) {

                //Составное название объекта
                // Проверяем, есть ли в конфигурации
                String prop = rowSet.getString("c_base_property");

                Document d;
                try {
                    d = XmlTask.stringToXmlDoc(prop);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }

                String caption = d.getDocumentElement().getAttribute("caption");

                if (caption != null && !caption.isEmpty()) {

                    String[] ses = caption.split(";");

                    return ses;
                }

                Integer idTyp = rowSet.getInt("c_obj_type");

                sql = "SELECT name_caption FROM c_obj_types WHERE obj_types_id=" + idTyp;

                ResultSet rs = SqlTask.getResultSet(null, sql);

                try {

                    if (rs.next()) {

                        String col_names = rs.getString("name_caption");

                        String[] ses = col_names.split(";");

                        return ses;
                    }
                } finally {
                    rs.getStatement().close();
                }
            } else if (nameParam.equals(TABLE_LINK_OBJECT)) {

                String prop = rowSet.getString("c_base_property");

                Document d;
                try {
                    d = XmlTask.stringToXmlDoc(prop);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }

                HashMap<String, Integer> hmLink = new HashMap<String, Integer>();

                sql = "descendant::cell";
                NodeList list = XmlTask.getNodeListByXpath(d, sql);

                if (list.getLength() > 1) {

                    // ссылки на объекты
                    String t = null;
                    String nameCol;

                    for (int i = 0; i < list.getLength(); i++) {

                        Element e = (Element) list.item(i);

                        nameCol = e.getAttribute("name");

                        sql = "descendant::grid";
                        NodeList listGrid = XmlTask.getNodeListByXpath(e, sql);

                        for (int j = 0; j < listGrid.getLength(); j++) {

                            Element eGrid = (Element) listGrid.item(j);

                            String key = eGrid.getAttribute("type");

                            if (key == null) {
                                continue;
                            }

                            if (key.indexOf("type") != -1) {

                                // ссылка на объект
                                t = getDelimitedString(key, '(', ')');

                                try {
                                    Integer ti = Integer.parseInt(t);
                                    hmLink.put(nameCol, ti);
                                    break;
                                } catch (NumberFormatException nfe) {
                                }
                            }
                        }
                    }
                }

                result = hmLink;
            }

        }

        return result;

    }

    /**
     * Возвращает параметры таблица из ее XML описания
     *
     * @param idTable id таблицы
     * @param nameParam -имя запрашиваемого параметра
     * @return
     */
    public static Object getParamTableById(int idTable, String nameParam) throws Exception {

        ResultSet rowSet = null;

        try {
            String sql = "SELECT * FROM c_obj_spec  WHERE c_obj_list_id=?";
            rowSet = SqlTask.getResultSet(null, sql, new Object[]{idTable});
            return getParTable(rowSet, nameParam);
        } finally {
            rowSet.getStatement().close();
        }

    }

    /**
     * Возвращает листинг всех поддерживаемых типов текущей базой данных
     *
     */
    public static ArrayList<String> getTypInfo() throws SQLException {

        ArrayList result = new ArrayList();

        DatabaseMetaData dbmt;

        dbmt = Work.CONNECT_DBF.getMetaData();
        ResultSet rs;
        rs = dbmt.getTypeInfo();

        try {

            while (rs.next()) {

                String types = rs.getString("TYPE_NAME");

                result.add(types.toUpperCase());
            }
        } finally {
            rs.close();
        }

        return result;

    }

    public static HashMap<String, String> getTypColumns(String nameTable) throws SQLException {

        HashMap<String, String> result = new HashMap<String, String>();

        DatabaseMetaData data;
        data = CONNECT_DBF.getMetaData();

        ResultSet resultSet = data.getColumns(null, null, nameTable, null);
        try {
            while (resultSet.next()) {

                String nameCol = resultSet.getString(4);

                String typCol = resultSet.getString(6);

                result.put(nameCol, typCol);
            }
        } finally {
            resultSet.close();
        }

        return result;
    }

    /**
     * Возвращает параметры таблица из ее XML описания
     *
     * @param nameTable имя таблицы
     * @param nameParam -имя запрашиваемого параметра
     * @return
     */
    public static Object getParamTableByName(String nameTable, String nameParam) throws Exception {

        ResultSet rowSet = null;

        try {
            String sql = "SELECT * FROM c_obj_spec WHERE c_name_table=?";
            rowSet = SqlTask.getResultSet(null, sql, new Object[]{nameTable});
            return getParTable(rowSet, nameParam);
        } finally {
            rowSet.getStatement().close();
        }
    }

    /**
     * Создает новый XML документ с параметрами записи по id объекта
     *
     * @param id Идентификатор глобального объекта
     * @return XML Документ с параметрами текущей записи
     */
    public static Document getXMLDocByIdObject(int id) throws Exception {

        Document result = XmlTask.getNewDocument();
        result.appendChild(result.createElement("root"));

        Element elm;
        ResultSet rs = null;

        String nameTable = getNameTableByObjectId(id);

        String sql = "SELECT * FROM " + nameTable + " WHERE c_tree_id=" + id;

        try {

            rs = SqlTask.getResultSet(null, sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rs.next()) {

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    elm = result.createElement("column");
                    String name = rsmd.getColumnName(i);
                    String value = rs.getString(name);
                    elm.setAttribute("name", name);
                    elm.setAttribute("value", value);
                    result.getDocumentElement().appendChild(elm);
                }
            }

        } finally {
            rs.getStatement().close();

        }

        return result;
    }

    /**
     * Возвращает IP адрес и порт регистрации конкретного пользователя
     *
     * @param idUser
     * @return
     */
    public static Object[] getIPaddresUser(int idUser) throws SQLException {

        Object[] result = new Object[2];

        String sql = "SELECT * FROM register WHERE id_user=" + idUser;

        ResultSet rsReg = SqlTask.getResultSet(null, sql);

        try {
            if (rsReg.next()) {

                String ipAddres = rsReg.getString("ip_addres");

                int ipPort = rsReg.getInt("id_port");

                result[0] = ipAddres;
                result[1] = ipPort;
            }

        } finally {
            rsReg.getStatement().close();
        }

        return result;
    }

    /**
     * Сохранение выборочных записей таблицы в XML
     *
     * @param sql-строка запроса
     * @param file файл для сохранения
     * @return
     * @throws Exception
     */
    public static String saveSQLinXML(String sql, File file) throws Exception {
        String result = null;

        Document document = XmlTask.getNewDocument();

        Element root = document.createElement("root");

        document.appendChild(root);

        if (file == null) {
            return "Не указан путь к файлу";
        }

        String path = file.getParent();
        String name = file.getName();

        if (!name.endsWith(".xml")) {

            name = name + ".xml";

            file = new File(path, name);

        }

        ResultSet rsTable;

        rsTable = SqlTask.getResultSet(null, sql);

        rsTable.first();

        ResultSetMetaData metaData = rsTable.getMetaData();

        String nameT = metaData.getTableName(1).toLowerCase();
        root.setAttribute("name_table", nameT);

        String sKey = SqlTask.getPrimaryKeyTable(null, nameT);
        root.setAttribute("name_key", sKey);

        HashMap<String, Object> hmParamCol = SqlTask.getMapNamesCol(null, nameT, new Integer[]{6, 7});

        for (String nameCol : hmParamCol.keySet()) {

            Object[] oPar = (Object[]) hmParamCol.get(nameCol);

            String collTyp = "";
            String colSize = "";

            if (oPar[0] != null) {
                collTyp = oPar[0].toString().toUpperCase();
            }

            if (oPar[1] != null) {
                colSize = oPar[1].toString();
            }

            Element elType = document.createElement("type_col");

            elType.setAttribute("name", nameCol);
            elType.setAttribute("type", collTyp);
            elType.setAttribute("size", colSize);

            root.appendChild(elType);

        }

        String value;
        do {

            Element element = document.createElement("row");

            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                String nameCol = metaData.getColumnName(i).toLowerCase();

                int typCol = metaData.getColumnType(i);

                if (typCol == java.sql.Types.LONGVARBINARY) {

                    byte[] bs = rsTable.getBytes(nameCol);
                    value = javax.xml.bind.DatatypeConverter.printBase64Binary(bs);
                    root.setAttribute("Base64", nameCol);

                } else {
                    value = rsTable.getString(nameCol);
                }
                element.setAttribute(nameCol, value);
            }
            root.appendChild(element);
        } while (rsTable.next());

        XmlTask.saveXmlDocument(document, file);

        return result;

    }

    public static ArrayList<Double> getEnergyByIdObject(Integer idObject, DateTime dateTime, int tarif) throws SQLException {

        Timestamp t = new Timestamp(dateTime.getMillis());

        ArrayList<Double> result = new ArrayList<>();
        String sql = "SELECT *  FROM enegry_data WHERE id_object=? AND value_date=? AND tarif=?";

        ResultSet rs = SqlTask.getResultSet(null, sql, new Object[]{idObject, t, tarif});

        try {
            if (rs.next()) {

                Double value = rs.getDouble("energy_down_0_0");
                result.add(value);
                value = rs.getDouble("energy_down_0_1");
                result.add(value);
                value = rs.getDouble("energy_down_0_2");
                result.add(value);
                value = rs.getDouble("energy_down_0_3");
                result.add(value);

            }
        } finally {
            rs.close();
        }

        return result;
    }

    /**
     * Возвращает значение Энергии по номеру счетчика
     *
     * @param numCount -номер счетчика
     * @param tarif -тариф
     * @param dateTime -Дата значения
     * @return
     */
    public static ArrayList<Double> getEnergyByNameCount(String numCount, DateTime dateTime, int tarif) throws SQLException {

        ArrayList<Double> result = new ArrayList<>();
        String sql = "SELECT id_counter FROM counters  WHERE serial_number=?";

        Integer id_counter = null;
        Integer id_Object = null;

        ResultSet rs = SqlTask.getResultSet(null, sql, new Object[]{numCount});

        try {
            if (rs.next()) {
                id_counter = rs.getInt("id_counter");
            }
        } finally {
            rs.close();
        }

        if (id_counter == null) {
            return result;
        }

        sql = "SELECT id_object FROM objects  WHERE id_counter=?";

        rs = SqlTask.getResultSet(null, sql, new Object[]{id_counter});

        try {
            if (rs.next()) {
                id_Object = rs.getInt("id_object");
            }
        } finally {
            rs.close();
        }

        if (id_Object == null) {
            return result;
        }

        result = getEnergyByIdObject(id_Object, dateTime, tarif);

        return result;
    }

    /**
     * Вставляет новую запись в таблицу возвращает номер вставленой записи если
     * -1 то проблемы с вставкой файла
     *
     * @param nameTable Имя таблицы
     * @param hmCol карта записываемых столбцов в виде имя-значение
     */
    public static Integer insertRecInTable(String nameTable, HashMap<String, Object> hmCol) throws SQLException {

        Integer id_key = null;

        PreparedStatement ps = null;

        StringBuilder sbName = new StringBuilder();
        StringBuilder sbValue = new StringBuilder();

        if (hmCol == null || hmCol.isEmpty()) {

            return null;
        }

        String sql;

        sbName.append("INSERT INTO ");

        sbName.append(nameTable);
        sbName.append(" (");
        sbValue.append(" VALUES (");

        for (String ncol : hmCol.keySet()) {
            sbName.append(ncol + ",");
            sbValue.append("?,");

        }

        sbName.deleteCharAt(sbName.lastIndexOf(","));
        sbName.append(")");
        sbValue.deleteCharAt(sbValue.lastIndexOf(","));
        sbValue.append(")");

        sql = sbName.toString() + sbValue.toString();

        try {

            ps = CONNECT_DBF.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int i = 1;

            for (String ncol : hmCol.keySet()) {
                Object val = hmCol.get(ncol);

                if (val instanceof File) {
                    try {
                        File file = (File) val;

                        FileInputStream fis = new FileInputStream(file);
                        ps.setBinaryStream(i, fis, file.length());

                    } catch (FileNotFoundException ex) {

                        return -1;

                    }

                } else {
                    ps.setObject(i, val);
                }

                i++;
            }

            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs != null) {

                try {
                    if (rs.next()) {
                        id_key = rs.getInt(1);
                    }

                } finally {
                    rs.close();
                }
            }

        } finally {
            ps.close();
        }

        return id_key;
    }

    /**
     * XML документ по названию константы
     *
     * @param consName - имя константы
     * @return XML Документ
     * @throws SQLException
     */
    public static Document getXmlDocFromConst(String consName) throws SQLException {

        Document result = null;

        String sql = "SELECT c_const_value FROM c_const WHERE c_const_name=?";

        Object[] objects = new Object[]{consName.trim()};

        ResultSet rs = null;

        rs = SqlTask.getResultSet(null, sql, objects);

        try {

            if (rs.next()) {

                String xml = rs.getString(1);
                try {
                    result = XmlTask.stringToXmlDoc(xml);
                } catch (Exception ex) {
                    throw new SQLException(ex);
                }

            }

        } finally {
            rs.close();
        }
        return result;
    }

    public static boolean setConnect(String driver, String url, String port, String basename, String dopInfo, String username, String password, Locale locale) {

        boolean result = false;

        ResourceBundle bundle = ResourceBundle.getBundle("DbfRes", locale);

        String jdbc = bundle.getString(CONN_JDBC);
        String nameBaseURL = bundle.getString(CONN_NAME_BASE);
        String newBase = bundle.getString(CONN_NEW_BASE);

        //   jdbc:sqlserver://serg:1433;databaseName=c_level;user=sa;password=silesta;
        // StringBuffer sb = new StringBuffer("jdbc:sqlserver://");
        StringBuilder sb = new StringBuilder(jdbc);

        if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
            // локальная база

            File file = new File(dopInfo, basename);

            sb.append(file.getAbsolutePath());

            // если не найдена
            if (!file.exists()) {

                return false;

            }

        } else {
            // серверная база
            sb.append(url);
            sb.append(":");
            sb.append(port);
        }
        try {
            try {

                String uri_con = sb.toString();

                if (uri_con.indexOf("localhost") != -1) {
                }

                if (basename == null || basename.isEmpty()) {

                    return false;

                }

                Class.forName(driver).newInstance(); //загрузка драйвера, который должен быть в CLASSPATH
                System.out.println("Connect from :" + uri_con + "...");
                try {
                    CONNECT_DBF = DriverManager.getConnection(uri_con, username, password);

                    CONNECT_DBF.setCatalog(basename);

                    System.out.println("Connect from :" + uri_con + "...OK.");

                    return true;

                } catch (SQLException ex) {

                    Logger.getLogger(Work.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

            } catch (InstantiationException ex) {
                Logger.getLogger(Work.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Work.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Work.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}

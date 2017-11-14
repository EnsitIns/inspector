  /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import connectdbf.StatementEx;
import dbf.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xmldom.XmlTask;

import javax.xml.soap.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author 1
 */
public class SOAPServis extends MainWorker {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int SOCKET_TIMEOUT = 30000;
    private Integer id_schedule; // id Расписания
    private String nameSchedule;// Имя расписания
    private String soap_user; //Пользователь
    private String soap_password; //Пользователь
    private String soap_group; //Номер группы
    private String soap_url; //URL сервера
    private HashMap<Integer, HashMap<Integer, Integer>> hmKeysObject;
    private HashMap<String, StatementEx> hmColTables;
    private int objCount;//Количество объектов запроса
    static public HashMap<Integer, String> hmNamesObj = new HashMap<Integer, String>();//Имена объектов

    public SOAPServis(ExecutorService pool) {

        this.pool = pool;
        objCount = 0;
        hmColTables = new HashMap<String, StatementEx>();

        setLogger(org.apache.log4j.Logger.getLogger("LogSoap"));
        setLoggerInfo("Модуль SOAP подключен.", null);

    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof ScheduleClass && arg instanceof Integer) {
            try {
                // Сработало расписание
                if (!isParameters()) {
                    setLoggerInfo("Параметры SOAP запроса не установлены !", null);
                    return;
                }
            } catch (Exception ex) {
                setLoggerInfo("", ex);
            }

            if (id_schedule == (Integer) arg) {

                ScheduleClass sc = (ScheduleClass) o;
                nameSchedule = sc.getNameSchedule();
                executeProcess();

            }
        } else if (arg instanceof Boolean && o instanceof ScheduleClass) {
            
            setLoggerInfo("Расписание  SOAP остановлено", null);
            MainWorker.isStop = true;

        }
    }

    private boolean isErrorSoap(SOAPMessage reply) {

        boolean result = false;

        if (reply != null) {

            SOAPBody pBody = null;
            try {
                pBody = reply.getSOAPBody();
            } catch (SOAPException ex) {
                setLoggerInfo("", ex);

                return true;
            }

            // Проверяем на ошибки
            NodeList list = pBody.getElementsByTagName("soap:Fault");

            if (list.getLength() > 0) {
                // Есть ошибки

                String sErr = list.item(0).getTextContent();

                setLoggerInfo(sErr, null);

                return true;
            }

        }

        return result;
    }

    @Override
    public void doProcess() {

        if (isGoProcess()) {

            setLoggerInfo("Модуль SOAP уже выполняется.", null);
            return;
        }

        hmColTables.clear();

        errorString = null;
        currentTask = nameSchedule;

        newProcess(currentTask);

        try {

            try {
                setGoProcess(true);
                CheckDataGprs();

            } finally {

                setGoProcess(false);

                setLoggerInfo("Загрузка данных с IP адреса:'" + soap_url + "' Закончена.", null);
                for (StatementEx se : hmColTables.values()) {
                    se.close();

                }

            }

        } catch (Exception ex) {
            setLoggerInfo(currentTask, ex);
            errorString = "Ошибка загрузки";

        }

    }

    public static HashMap<String, Object> postHTTP(String sUrl, String sPost, HashMap<String, String> property) throws Exception {

        StringBuilder responseBuf = new StringBuilder();

        HashMap<String, Object> hmResult = new HashMap<String, Object>();

        String result = null;

        URL url = new URL(sUrl);
        URLConnection connectionUrl = url.openConnection();

        HttpURLConnection httpConn = (HttpURLConnection) connectionUrl;

        byte[] b = sPost.getBytes("UTF-8");

        httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

        httpConn.setConnectTimeout(CONNECT_TIMEOUT);
        httpConn.setReadTimeout(SOCKET_TIMEOUT);

        for (String key : property.keySet()) {
            String var = property.get(key);
            httpConn.setRequestProperty(key, var);
        }

        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        // Send the the request
        OutputStream out = httpConn.getOutputStream();
        out.write(b);
        out.close();

        // Read the response and write it to the response buffer.
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        String line;
        do {
            line = in.readLine();
            if (line != null) {
                responseBuf.append(line);
            }
        } while (line != null);

        in.close();
        result = responseBuf.toString();

        hmResult.put("result", result);
        hmResult.put("HeaderFields", connectionUrl.getHeaderFields());

        return hmResult;
    }

    public static HashMap<String, Object> getHTTP(String sUrl, HashMap<String, String> property) throws Exception {

        StringBuilder responseBuf = new StringBuilder();

        HashMap<String, Object> hmResult = new HashMap<String, Object>();

        String result = null;

        URL url = new URL(sUrl);
        URLConnection connectionUrl = url.openConnection();

        HttpURLConnection httpConn = (HttpURLConnection) connectionUrl;

        //   byte[] b = sPost.getBytes("UTF-8");
        // httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
        // httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setConnectTimeout(10000);
        httpConn.setReadTimeout(10000);

        for (String key : property.keySet()) {
            String var = property.get(key);
            httpConn.setRequestProperty(key, var);
        }

        httpConn.setRequestMethod("GET");
        //   httpConn.setDoOutput(true);
        // httpConn.setDoInput(true);

        int responseCode = httpConn.getResponseCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        hmResult.put("code", responseCode);

        //  hmResult.putAll(httpConn.getRequestProperties());
        hmResult.putAll(httpConn.getHeaderFields());

        //     BufferedReader in = new BufferedReader(
        //           new InputStreamReader(httpConn.getInputStream()));
        //  String inputLine;
        //   StringBuffer response = new StringBuffer();
        // while ((inputLine = in.readLine()) != null) {
        //   response.append(inputLine);
        //  }
        //in.close();
        //print result
        //   System.out.println(response.toString());
        // hmResult.put("result", response.toString());
        // Send the the request
        //    OutputStream out = httpConn.getOutputStream();
        //  out.write(b);
        //  out.close();
        // Read the response and write it to the response buffer.
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        String line;
        do {
            line = in.readLine();
            if (line != null) {
                responseBuf.append(line);
            }
        } while (line != null);

        in.close();
        result = responseBuf.toString();

        hmResult.put("result", result);
        hmResult.put("HeaderFields", connectionUrl.getHeaderFields());

        return hmResult;
    }

    public void CheckDataGprs() {

        int countRec = 0; // Число записей

        // Вытаскиваем номер группы
        //Сначала создаем соединение
        try {

            blinkText("Запрос кодов объектов...");

            Object o = createMapEx();

            if (o != null) {
                hmKeysObject = (HashMap<Integer, HashMap<Integer, Integer>>) o;
            }

            stopBlinkText();

            HashMap<String, Object> hmHttp;
            HashMap<String, String> hmProp = new HashMap<String, String>();

            String sMsg = GetAvailableGroupsMsg(soap_user, soap_password);
            hmProp.put("SOAPAction", "http://www.npk-silesta.ru/" + "GetAvailableGroups");
            hmHttp = postHTTP(soap_url, sMsg, hmProp);

            String resHttp = (String) hmHttp.get("result");

            if (!resHttp.contains("w_group_id")) {

                setLoggerInfo("Нет соединения с сервером SOAP.  Проверте параметры сервера !", null);
                return;
            }

            Document document = XmlTask.stringToXmlDoc(resHttp);

            NodeList list;

            list = document.getElementsByTagName("w_group_id");
            Node n = list.item(0);

            if (n == null) {
                setLoggerInfo("Не определена группа данных !."
                        + " Проверте параметры сервера ! ", null);
                return;
            }

            soap_group = n.getTextContent();
            list = document.getElementsByTagName("w_group_name");
            n = list.item(0);

            String name = n.getTextContent();

            setLoggerInfo("Запрос данных по группе: " + name + "...", null);

            setLoggerInfo("User-" + soap_user + "; Password-" + soap_password + "; Group-" + soap_group, null);
            setLoggerInfo("URL-" + soap_url, null);

            NodeList nlValues = null;

            Document docValues;

            List<String> cookieKey = null;

            Map<String, List<String>> mapHeader;

            String keySession = null;

            do {

                if (MainWorker.isStop) {

                    MainWorker.setLogInfo("Процесс запроса данных с сервера  остановлен", null);
                    return;
                }

                hmProp.clear();
                String sMsgIn = GetDataForGroupMsg(soap_user, soap_password, soap_group);
                hmProp.put("SOAPAction", "http://www.npk-silesta.ru/" + "GetDataForGroup");

                if (keySession != null) {
                    hmProp.put("Cookie", keySession);
                }

                refreshBarValue("Запрос данных...");

                hmHttp = postHTTP(soap_url, sMsgIn, hmProp);
                resHttp = (String) hmHttp.get("result");

                mapHeader = (Map<String, List<String>>) hmHttp.get("HeaderFields");

                docValues = XmlTask.stringToXmlDoc(resHttp);

                if (keySession == null) {
                    cookieKey = mapHeader.get("Set-Cookie");
                    String[] ss = cookieKey.get(0).split(";");
                    keySession = ss[0];
                }

                nlValues = docValues.getElementsByTagName("value");

                if (nlValues.getLength() > 0) {

                    countRec = countRec + nlValues.getLength();

                    String idDataKey = nlValues.item(0).getParentNode().getNodeName();

                    //        setLoggerInfo("ID=" + idDataKey + "(" + nlValues.getLength() + ")", null);
                    try {
                        insertValue(nlValues);

                    } catch (SQLException e) {
                        setLoggerInfo(sMsg, e);
                        continue;
                    }

                    //Отмечаем как прочитаные и записаные
                    // Помечаем на сервере
                    String sMsgOut = SetDataAsReadedMsg(idDataKey);

                    hmProp.clear();
                    hmProp.put("SOAPAction", "http://www.npk-silesta.ru/" + "SetDataAsReaded");

                    if (keySession != null) {
                        hmProp.put("Cookie", keySession);
                    }

                    hmHttp = postHTTP(soap_url, sMsgOut, hmProp);

                    resHttp = (String) hmHttp.get("result");

                    setLoggerInfo("Загружено- " + countRec, null);

                }

            } while (nlValues != null && nlValues.getLength() > 0);

            //      setLoggerInfo("User-" + soap_user + "; Password-" + soap_password + "; Group-" + soap_group, null);
            //    setLoggerInfo("URL-" + soap_url, null);
            setLoggerInfo("Всего загружено- " + countRec, null);

        } catch (Exception e) {

            setLoggerInfo(soap_user, e);
        }
    }

    public boolean isParameters() throws Exception {

        boolean result;
        id_schedule = -1;

        mapProperties = Work.getParametersFromConst("gprs_server");

        id_schedule = (Integer) mapProperties.get("shedule_gprs");
        soap_user = (String) mapProperties.get("soap_user");
        soap_password = (String) mapProperties.get("soap_password");
        soap_url = (String) mapProperties.get("soap_url");

        result = (id_schedule == null || soap_user == null
                || soap_password == null || soap_url == null);

        return !result;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> createMapEx() throws SQLException {

        HashMap<Integer, HashMap<Integer, Integer>> result = new HashMap<Integer, HashMap<Integer, Integer>>();

        HashMap<Integer, Integer> hmId = null;

        HashMap<String, Object> hmObject = null; // точки подключения

        ResultSet rsObjects;

        String sql = "SELECT objects.*,controllers.id_controller"
                + " FROM objects,points,controllers WHERE objects.id_point=points.id_point "
                + "  AND objects.id_point=controllers.id_point";

        rsObjects = SqlTask.getResultSet(null, sql);
        Integer idObject;
        Integer idController;

        try {

            rsObjects.last();

            int count = rsObjects.getRow();

            if (objCount == count) {

                return null;
            } else {

                objCount = count;
            }

            rsObjects.beforeFirst();

            while (rsObjects.next()) {
                idObject = rsObjects.getInt("id_object");
                idController = rsObjects.getInt("id_controller");

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

                hmObject = Work.getParametersRow(idObject, rsObjects, "objects", true, false);

                Integer idCounter = (Integer) hmObject.get("id_counter");

                if (idCounter == null || idCounter == -1) {

                    setLoggerInfo("Для объекта " + idObject + " нет прибора учета !", null);

                    continue;
                }

                Integer counter = null;
                if (hmObject != null) {
                    counter = (Integer) hmObject.get("counter_addres");
                }

                if (counter != null) {

                    if (hmId.containsKey(counter)) {

                        int dblId = hmId.get(counter);

                        setLoggerInfo(idObject + ": дублируется сет. адрес прибора учета !(" + dblId + ")", null);
                        continue;

                    } else {
                        hmId.put((Integer) counter, idObject);
                    }

                } else {

                    setLoggerInfo("Для объекта " + idObject + " нет сетевого адреса !", null);
                    continue;
                }
            }

        } finally {

            rsObjects.close();
        }

        return result;
    }

    /**
     * Добавляет данные поступившие по GPRS
     *
     */
    public void addDataByXml(Integer idObject, String TypData, String sValues, StatementEx statementEx) throws SQLException {

        String sql = "";
        ResultSet rs;
        HashMap<String, HashMap<String, Object>> hmInsInTab = new HashMap<String, HashMap<String, Object>>();
        Double dv;

        ArrayList<String> alCol;
        HashMap<String, Object> hmColTable = null;

        String fullName;

        HashMap<String, Object> hmInsert;

        String[] Val = sValues.split(";");

        String sDate = Val[0];
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        DateTime d_value;

        try {

            d_value = dtf.parseDateTime(sDate);

        } catch (Exception e) {

            setLoggerInfo("Парсинг даты", e);

            return;
        }

        int typ = 0;
        int num = 0;
        String nameVal;
        String nameTab;
        Integer typPar;
        Byte tarifPar;
        boolean bTyp = false;

        Object[] objects = new Object[2];

        for (int i = 1; i < Val.length; i++) {

            try {
                typ = Integer.parseInt(TypData);

                if (typ == 13) {

                    typ = 3;
                    bTyp = true;

                }

                objects[0] = typ;
                objects[1] = i;

                //  statement.setInt(1, typ);
                //    statement.setInt(2, i);
            } catch (NumberFormatException e) {

                setLoggerInfo("Объект[" + idObject + "] не корректный тип параметра !(" + TypData + ")", e);
                continue;
            }

            rs = statementEx.getResultSet(objects);
            if (rs.next()) {

                nameVal = rs.getString("prm_name_tbl");
                nameTab = rs.getString("name_table");
                typPar = rs.getInt("c_partype_id");
                tarifPar = rs.getByte("prm_tarif");

                if (bTyp) {

                    tarifPar = 13;

                }

                String sV = Val[i];
                sV = sV.replaceAll(",", ".");

                if (!hmColTables.containsKey(nameTab)) {

                    //  alCol = Work.getNamesCol(nameTab);
                    StatementEx seInsert = new StatementEx(null, nameTab, null);

                    hmColTables.put(nameTab, seInsert);
                }

                try {

                    dv = Double.parseDouble(sV);

                } catch (NumberFormatException e) {

                    setLoggerInfo(sV, e);

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

            StatementEx seTable = hmColTables.get(tabNam);

            hmColTable = seTable.getHmCol();

            if (hmColTable.containsKey("parnumber_id")) {

                typPar = Integer.parseInt(names[1]);
                hmInsert.put("parnumber_id", typPar);
            }

            if (hmColTable.containsKey("tarif")) {

                tarifPar = Byte.parseByte(names[2]);
                hmInsert.put("tarif", tarifPar);
            }

            if (hmColTable.containsKey("season")) {

                tarifPar = Byte.parseByte(names[2]);
                hmInsert.put("season", tarifPar);
            }

            hmInsert.put("id_object", idObject);

            String caption;

            if (hmNamesObj.containsKey(idObject)) {

                caption = hmNamesObj.get(idObject);
            } else {

                caption = (String) Work.getParametersRow(idObject, null, "objects", false, true).get("caption");
                hmNamesObj.put(idObject, caption);
            }

            hmInsert.put("object_caption", caption);

            Timestamp ts = new Timestamp(d_value.getMillis());
            hmInsert.put("value_date", ts);

            Timestamp timestamp = new Timestamp(new DateTime().getMillis());
            hmInsert.put("modify_date", timestamp);
            hmInsert.put("is_check", 0);
            hmInsert.put("flag0", 0);
            hmInsert.put("flag1", 0);
            try {
                seTable.replaceRecInTable(hmInsert, true);

            } catch (Exception ex) {
                setLoggerInfo(sql, ex);
            }

        }
    }

    private void insertValue(NodeList nlValues) throws SQLException {

        boolean result = false;
        Integer idObj = null;
        String w_device_code = null; // Адрес контроллера
        String w_data_sub_device_addr = null; // Номер счетчика
        String w_data_data_type_id = null; // Тип данных
        String strValue = null; // Массив данных
        NodeList list = nlValues;
        String nName;
        String nValue;

        StatementEx statementEx = null;
        String sql = "SELECT prm_name_tbl,name_table,c_partype_id,prm_tarif   FROM c_parnumber WHERE  c_w_type=? AND c_w_number=?";
        statementEx = new StatementEx(null, sql);

        setMinMaxValue(0, nlValues.getLength());
        refreshBarValue(soap_url + "[" + nlValues.getLength() + "]");

        for (int i = 0; i < list.getLength(); i++) {
            NodeList nl = list.item(i).getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                Node n = nl.item(j);
                nName = n.getNodeName();
                nValue = n.getTextContent();
                if (nName.equals("w_device_code")) {
                    // id контроллера
                    w_device_code = nValue;
                } else if (nName.equals("w_data_sub_device_addr")) {
                    // адрес счетчика
                    w_data_sub_device_addr = nValue;
                } else if (nName.equals("w_data_data_type_id")) {
                    // тип параметра
                    w_data_data_type_id = nValue;
                } else if (nName.equals("strValue")) {
                    // Строковое значение
                    strValue = nValue;
                }
            }
            Integer id_cnt;
            Integer id_count;
            try {
                id_cnt = Integer.parseInt(w_device_code);
                id_count = Integer.parseInt(w_data_sub_device_addr);
            } catch (NumberFormatException nfe) {
                setLoggerInfo("id контроллера=" + w_device_code + " Проверить !", null);
                setLoggerInfo("id счетчика=" + w_data_sub_device_addr + " Проверить !", null);
                continue;
            }
            HashMap<Integer, Integer> hmKeys = hmKeysObject.get(id_cnt);
            if (hmKeys != null) {
                idObj = hmKeys.get(id_count);
                if (idObj != null && strValue != null) {
                    addDataByXml(idObj, w_data_data_type_id, strValue, statementEx);
                }
            }
            result = true;

            refreshBarValue(i);

        }
    }

    private String GetAvailableGroupsMsg(String userName, String password) throws Exception {

        String msg;

        Document document = XmlTask.getNewDocument();
        Element eEnvelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
        document.appendChild(eEnvelope);
        Element eBody = document.createElementNS("", "soap:Body");
        eEnvelope.appendChild(eBody);
        Element eGetAvailableGroups = document.createElementNS("http://www.npk-silesta.ru/", "GetAvailableGroups");
        eBody.appendChild(eGetAvailableGroups);
        Element eUset = document.createElement("userName");
        eUset.setTextContent(userName);
        eGetAvailableGroups.appendChild(eUset);
        Element ePass = document.createElement("password");
        ePass.setTextContent(password);
        eGetAvailableGroups.appendChild(ePass);
        msg = XmlTask.xmlDocToString(document);

        return msg;
    }

    private String SetDataAsReadedMsg(String idMsg) throws Exception {

        String msg;

        Document document = XmlTask.getNewDocument();
        Element eEnvelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
        document.appendChild(eEnvelope);
        Element eBody = document.createElementNS("", "soap:Body");
        eEnvelope.appendChild(eBody);
        Element eGetAvailableGroups = document.createElementNS("http://www.npk-silesta.ru/", "SetDataAsReaded");
        eBody.appendChild(eGetAvailableGroups);
        Element eDataKey = document.createElement("dataKey");
        eDataKey.setTextContent(idMsg);
        eGetAvailableGroups.appendChild(eDataKey);
        msg = XmlTask.xmlDocToString(document);

        return msg;
    }

    private String GetDataForGroupMsg(String userName, String password, String groupId) throws Exception {

        String msg;

        Document document = XmlTask.getNewDocument();
        Element eEnvelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
        document.appendChild(eEnvelope);
        Element eBody = document.createElementNS("", "soap:Body");
        eEnvelope.appendChild(eBody);
        Element eGetAvailableGroups = document.createElementNS("http://www.npk-silesta.ru/", "GetDataForGroup");
        eBody.appendChild(eGetAvailableGroups);
        Element eUset = document.createElement("userName");
        eUset.setTextContent(userName);
        eGetAvailableGroups.appendChild(eUset);
        Element ePass = document.createElement("password");
        ePass.setTextContent(password);
        eGetAvailableGroups.appendChild(ePass);
        Element eGroupId = document.createElement("groupId");
        eGroupId.setTextContent(groupId);
        eGetAvailableGroups.appendChild(eGroupId);

        msg = XmlTask.xmlDocToString(document);

        return msg;
    }

    private SOAPMessage getSOAPMessage(String msg, String soapAction) throws Exception {

        SOAPMessage soapm = null;
        //Затем создаем сообщение
        MessageFactory messageFactory = MessageFactory.newInstance();

        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml; charset=UTF-8");
        mimeHeaders.addHeader("SOAPAction", "http://www.npk-silesta.ru/" + soapAction);

        ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBytes());

        soapm = messageFactory.createMessage(mimeHeaders, bais);

        return soapm;
    }
}

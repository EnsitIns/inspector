/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import dbf.Work;
import forms.ValueWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Технологическая проверка всех параметров программы
 *
 * @author 1
 */
public class TechnologyCheck extends MainWorker {

    static String ANALIZ_DBF = "База данных:";
    static String ANALIZ_COUNTER = "Приборы учета:";
    static String ANALIZ_CONTROLLERS = "Контроллеры:";
    static String ANALIZ_REPORT = "Отчеты:";
    static String ANALIZ_REPORT_USER = "Параметры контроля отчетов по пользователям:";
    static String ANALIZ_OVER_USER = "Параметры контроля объектов по пользователям:";
    static String ANALIZ_REFERENCE = "Глобальный идентификатор:";
    private ArrayList<String> alCheck;
    private StringBuilder builder;

    /**
     * Проверка наличия или дублирования приборов учета(счетчиков)
     *
     * @return
     * @throws SQLException
     */
    public static void checkCounters(ArrayList<String> alCheck) throws SQLException {



        // Объекты учета
        String sql = "SELECT c_tree_id, sub_type6 FROM objects";

        ResultSet rs = SqlTask.getResultSet(null, sql);
        HashMap<Integer, Integer> hmCounters = new HashMap<Integer, Integer>();

        try {
            while (rs.next()) {


                int id_object = rs.getInt("c_tree_id");
                Integer id_count = rs.getInt("sub_type6");

                if (id_count == null || id_count == -1) {

                    alCheck.add("У объекта № " + id_object + " нет прибора учета !");

                } else {

                    if (hmCounters.containsKey(id_count)) {
                        // Дублируется

                        int obj = hmCounters.get(id_count);

                        alCheck.add("У объ-та № " + id_object + " и объ-та № " + obj + " один и тот же прибор учета(" + id_count + ")");
                    } else {

                        hmCounters.put(id_count, id_object);
                    }
                }
            }
        } finally {

            rs.close();
        }

    }

    /**
     * проверка ссылочной целостности таблиц
     *
     */
    public void checkReference(ArrayList<String> alCheck) {
        try {
            HashMap<String, String> hmNames = SqlTask.getNameAndTypeTables(null, false);

            String sql;

            ArrayList<String> alKeys;

            for (String nameTable : hmNames.keySet()) {

                alKeys = SqlTask.getPrimaryKey(null, nameTable);

                if (alKeys.size() == 1 && alKeys.contains("c_tree_id")) {

                    sql = "SELECT * FROM " + nameTable;

                    ResultSet rsRowsTab = SqlTask.getResultSet(null, sql);

                    while (rsRowsTab.next()) {

                        int id = rsRowsTab.getInt("c_tree_id");

                        Integer typ = Work.getIdTableByObjectId(id);

                        if (typ == null) {

                            alCheck.add("Объект " + id + " таблица '" + nameTable + "' нет глобального идентификатора !");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            setLoggerInfo("", ex);
        }

    }

    /**
     * Проверка загружаемой конфигурации таблицы на соответствие конфигурации
     *
     * @param docConfigTable
     * @param alCheck
     */
    public static void checkTable(Element node, ArrayList<String> alCheck, String nameTable) throws SQLException {


        HashMap<String, Object> hmCol;

        hmCol = SqlTask.getMapNamesCol(null, nameTable, new Integer[]{6, 7});

        org.w3c.dom.NodeList nlCol = node.getElementsByTagName("type_col");

        String nameCol;
        String typCol;
        String sizeCol;


        for (int j = 0; j < nlCol.getLength(); j++) {

            Element eCol = (Element) nlCol.item(j);

            nameCol = eCol.getAttribute("name");
            typCol = eCol.getAttribute("type");
            sizeCol = eCol.getAttribute("size");


            if (hmCol.containsKey(nameCol)) {

                Object[] param = (Object[]) hmCol.get(nameCol);

                String tabType = (String) param[0];
                Integer tabSize = (Integer) param[1];

                // Не совпадают типы
                if (!typCol.contains(tabType)) {
                    alCheck.add("Столбец '" + nameCol + "' не совподают типы столбцов '" + tabType + "'(таблица) '" + typCol + "'(конфиг.)' в таблице '" + nameTable + "' БД!");
                }

                // не совпадает размер полей (для строковых полей)

                if (tabType.contains("VARCHAR")) {

                    Integer cnfSize = Integer.decode(sizeCol);

                    if (tabSize < cnfSize) {

                        alCheck.add("Столбец '" + nameCol + "' не совпадают размеры столбцов '" + tabSize + "'(таблица) '" + cnfSize + "'(конфиг.) в таблице '" + nameTable + "' БД!");

                    }

                }

            } else {

                // нет такого столбца

                alCheck.add("Нет столбца '" + nameCol + "' в таблице '" + nameTable + "' БД!");

            }

        }




    }

    /**
     * Проверка соответствия ХМL Конфигурации и ,базы данных
     *
     * @param nameTable
     * @return
     * @throws SQLException
     */
    public static void checkKonfigTables(ArrayList<String> alCheck) throws Exception {



        Document document = Work.getXmlDocFromConst("check");


        if (document == null) {

            DiffTask diffTask = new DiffTask();

            File file = null;

            diffTask.setConfigObject(file);

            document = Work.getXmlDocFromConst("check");

        }



        if (document == null) {

            String s = "Модуль проверки не загружен.";
            alCheck.add(s);
            return;
        }


        org.w3c.dom.NodeList nodeList = document.getElementsByTagName("table");

        Element node;

        HashMap<String, Object> hmCol;

        for (int i = 0; i < nodeList.getLength(); i++) {
            node = (Element) nodeList.item(i);

            //Имя таблицы
            String nameTable = node.getAttribute("name");


            // Таблицы данных
            if (nameTable.equals("data_value")) {

                HashMap<String, String> hmTables = SqlTask.getNameAndCaptionTablesByTyp(null, 15);


                for (String ntab : hmTables.keySet()) {

                    checkTable(node, alCheck, ntab);
                }

            } else {

                checkTable(node, alCheck, nameTable);
            }
        }



        // таблицы в базе
        HashMap<String, String> map = SqlTask.getNameAndTypeTables(null, false);

        // Таблицы Кофигураций
        HashMap<String, Integer> hmlNameTable = SqlTask.getNamesAndTypeTablesXML(null);

        // Проверяем на соответствие количества таблиц в XML  и базе

        if (map.size() != hmlNameTable.size()) {

            alCheck.add("Количеcтво таблиц в конфигурации XML и Таблице БД разное!");

        }



        // Проверяем есть ли все таблицы  базы  в конфигурации

        for (String nametab : map.keySet()) {

            // Проверяем наличие первичного ключа во всех таблицах

            String sKey = SqlTask.getPrimaryKeyTable(null, nametab);


            // Только журналы  без первичного ключа !

            if (hmlNameTable.containsKey(nametab) && (sKey == null || sKey.isEmpty())) {

                Integer iTyp = hmlNameTable.get(nametab);

                if (iTyp != 18) {
                    //   alCheck.add("Для таблицы '" + nametab + "' нет первичного ключа!");
                }
            }




            if (!hmlNameTable.containsKey(nametab)) {
                alCheck.add("Нет таблицы '" + nametab + "' в конфигурации XML!");
            }

        }

        // Проверяем есть ли все таблицы конфигурации в базе

        for (String nametab : hmlNameTable.keySet()) {

            if (!map.containsKey(nametab)) {
                alCheck.add("Нет таблицы '" + nametab + "' в базе данных!");
            }

        }

        HashMap<String, Integer> hmColTable;
        HashMap<String, String> hmType;

        for (String nameTable : map.keySet()) {

            hmColTable = SqlTask.getNameAndTypeCol(null, nameTable);

            hmType = (HashMap<String, String>) Work.getParamTableByName(nameTable, Work.TABLE_TYPE_COL);

            if (hmType == null) {
                continue;
            }




            // Проверяем на соответствие количества полей в XML  и базе

            if (hmColTable.size() != hmType.size()) {

                alCheck.add("Количетво столбцов в конфигурации XML и Таблице '" + nameTable + "' БД разное!");
            }


            // Проверяем есть ли все поля конфигурации в базе

            for (String nameCol : hmType.keySet()) {

                if (!hmColTable.containsKey(nameCol)) {
                    alCheck.add("Нет столбца '" + nameCol + "' в таблице '" + nameTable + "' БД!");
                }

            }

            // Проверяем есть ли все поля  базы  в конфигурации

            for (String nameCol : hmColTable.keySet()) {

                if (!hmType.containsKey(nameCol)) {
                    alCheck.add("Нет столбца '" + nameCol + "' в конфигурации '" + nameTable + "' XML!");
                }

            }

        }

    }

    /**
     * Проверка одинаковых адресов контроллера
     */
    private void checkController() throws SQLException {

        String sql = "SELECT * FROM c_obj_types";

        ResultSet rsTypes = SqlTask.getResultSet(null, sql);




    }

    /**
     * Соответствие таблиц формальным требованиям по типам
     */
    private void objectType() throws SQLException {

        String sql = "SELECT * FROM c_obj_types";

        ResultSet rsTypes = SqlTask.getResultSet(null, sql);




    }

    /**
     * отчеты Excel
     */
    private void report() {
    }

    private void analiz(String nameAnaliz) throws Exception {

        builder.append("<H3 ALIGN=LEFT>");
        builder.append("<FONT COLOR=000080>");
        builder.append(nameAnaliz);
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("<H3>");

        if (nameAnaliz.equals(ANALIZ_DBF)) {
            checkKonfigTables(alCheck);
        } else if (nameAnaliz.equals(ANALIZ_COUNTER)) {
            checkCounters(alCheck);

        } else if (nameAnaliz.equals(ANALIZ_OVER_USER)) {
        } else if (nameAnaliz.equals(ANALIZ_REPORT)) {
        } else if (nameAnaliz.equals(ANALIZ_REPORT_USER)) {
        } else if (nameAnaliz.equals(ANALIZ_REFERENCE)) {
            // checkReference(alCheck);
        }

        addError();
        builder.append("</FONT>");
        builder.append("</H3><hr>");

    }

    private void addError() {

        if (alCheck.isEmpty()) {

            builder.append("<FONT COLOR=008000>");
            builder.append("Ошибок не обнаружено.");


        } else {
            builder.append("<FONT COLOR=ff0000>");

            for (String s : alCheck) {
                builder.append("<P ALIGN=LEFT>");
                builder.append(s);
                builder.append("</P>");

            }

        }
        alCheck.clear();

    }

    private void startAnaliz(String nameAnaliz) {

        builder = new StringBuilder();
        alCheck = new ArrayList<String>();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("ТЕХНОЛОГИЧЕСКИЙ АНАЛИЗ ПАРАМЕТРОВ");
        builder.append("</H3>");


    }

    private void stopAnaliz() {

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("АНАЛИЗ ЗАВЕРШЕН.");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");


    }

    public void check() {
        try {

            setMinMaxValue(0, 5);

            startAnaliz("ТЕХНОЛОГИЧЕСКИЙ АНАЛИЗ ПАРАМЕТРОВ");
            blinkText(ANALIZ_DBF);
            analiz(ANALIZ_DBF);

            analiz(ANALIZ_COUNTER);

            analiz(ANALIZ_OVER_USER);

            setNotifyObservers(3);

            analiz(ANALIZ_REPORT);

            setNotifyObservers(4);

            analiz(ANALIZ_REPORT_USER);

            analiz(ANALIZ_CONTROLLERS);

            analiz(ANALIZ_REFERENCE);

            stopAnaliz();

        } catch (Exception ex) {
            setNotifyObservers(ex);
            setLoggerInfo("", ex);
            

        }



    }

    @Override
    public void doProcess() {
        super.doProcess();

        newProcess("Технологический анализ параметров программы...");

        check();
    }

    @Override
    public void endProcess() {
        super.endProcess();

        String msg = builder.toString();


        forms.ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }
}

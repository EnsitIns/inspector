/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import connectdbf.StatementEx;
import dbf.Work;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xmldom.XmlTask;

import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.CM_EXCEL_EXPORT;
import static constatant_static.SettingActions.esStatus.*;

/**
 * Вывод в Excel табличных данных
 *
 * @author 1
 */
public class ExcelTable extends MainWorker {

    private File fileChek;
    private ResultSet crsThis;
    private String sqlTable;
    private Document document;
    private String caption;
    private int typShow;
    ArrayList<Integer> alKfc; //Коэффициенты
    public static final String CT_SHOW_TABLE = "Экспорт таблицы";// экспорт таблицы
    public static final String CT_SHOW_TREE = "Экспорт дерева";// экспорт  дерева
    public static final String CT_EXPORT_IN_EXCEL = "Экспорт в 'Excel'"; // экспорт  в excel
    public static final String CT_IMPORT_EXCEL = "Импорт из 'Excel'";// экспорт  дерева
    public static final String CT_IMPORT_ASUSE = "Импорт объектов 'АСУСЭ'";// экспорт  дерева
    public static final String CT_EXPORT_ASUSE = "Экспорт Данных в 'АСУСЭ'";// экспорт  дерева

    public static int SHOW_TABLE = 0;// экспорт таблицы
    public static int SHOW_TREE = 1;// экспорт  дерева

    public ExcelTable(ExecutorService pool) {

        this.pool = pool;

    }

    public void toExcel(String sql, ArrayList<Integer> alKfc, String caption) {

        this.sqlTable = sql;
        this.alKfc = alKfc;
        this.caption = caption;

        currentTask = CT_SHOW_TABLE;
        try {

            //  в отдельном потоке
            executeProcess();

        } catch (Exception ex) {

            setNotifyObservers(ex);
        }
    }

    public void showTableinExcel(String sql, Object[] values) throws Exception {

        crsThis = SqlTask.getResultSet(null, sql, values);

        reportExcel();
    }

    private boolean createResultSet() throws SQLException {
        boolean result = false;

        crsThis = SqlTask.getResultSetBySaveSql(null, sqlTable, ResultSet.CONCUR_READ_ONLY);
        result = true;

        return result;
    }

    @Override
    public void doProcess() {

        errorString = null;

        try {

            newProcess(currentTask);

            if (currentTask.equals(CT_IMPORT_ASUSE)) {

                importFromASUSE();
            }
            if (currentTask.equals(CT_EXPORT_ASUSE)) {
                exportToASUSE();
            }

            if (currentTask.equals(CT_SHOW_TABLE)) {

                try {

                    if (createResultSet()) {
                        reportExcel();
                    }

                } finally {

                    if (crsThis != null) {
                        crsThis.close();
                    }
                }
            } else if (currentTask.equals(CT_SHOW_TREE)) {

                // Экспорт дерева
                exportTreeInExcel();
            } else if (currentTask.equals(CT_IMPORT_EXCEL)) {

                importFromExcel();

            }

        } catch (Exception ex) {

            setNotifyObservers(ex);
            setLoggerInfo("Экспорт дерева", ex);

            errorString = "Ошибка";
        }

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Object[]) {

            Object[] objects = (Object[]) arg;

            String cmnd = (String) objects[0];

            if (cmnd.equals(CM_EXCEL_EXPORT)) {

                this.sqlTable = (String) objects[1];

                currentTask = CT_SHOW_TABLE;
                try {

                    //  в отдельном потоке
                    executeProcess();

                } catch (Exception ex) {

                    setNotifyObservers(ex);
                }

            }

        }

    }

    /**
     * Экспорт показаний в асусэ
     */
    public File exportToASUSE() throws Exception {

        // Объекты
        Map<String, Object> hmAsuse = SqlTask.getMapNamesCol(null, "asuse", 7);

        Map map = Work.getParametersFromConst("asuse");

        String folder_asuse = (String) map.get("folder_asuse");
        String date_first = (String) map.get("date_first");

        HashMap<Integer, String> hmTyps = new HashMap<>();

        Boolean ap = (Boolean) map.get("ap");
        Boolean am = (Boolean) map.get("am");
        Boolean rp = (Boolean) map.get("rp");
        Boolean rm = (Boolean) map.get("rm");

        if (ap) {
            hmTyps.put(0, "А+");
        }

        if (am) {
            hmTyps.put(1, "А-");
        }
        if (rp) {
            hmTyps.put(2, "R+");
        }
        if (rm) {
            hmTyps.put(3, "R-");
        }

        DateTimeFormatter dtf;
        dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");
        // "2013-12-01 00:00:00.0"
        DateTime dateTime = dtf.parseDateTime(date_first);

        Timestamp t = new Timestamp(dateTime.getMillis());

        dtf = DateTimeFormat.forPattern("dd_MM-yyyy");

        File f = new File(folder_asuse, dateTime.toString(dtf) + ".psk");

        Integer idsql = (Integer) map.get("sql_object");

        if (idsql == null) {
            return null;
        }

        String sql = Work.getSQLbyId(idsql);

        FileWriter writer = new FileWriter(f, false);
        ResultSet rs = SqlTask.getResultSet(null, sql);

        dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

        try {

            for (int etyp : hmTyps.keySet()) {

                String enrgName = hmTyps.get(etyp);

                refreshBarValue(currentTask+":"+enrgName);
                
                
                rs.last();
                int max = rs.getRow();

                // все объекты
                setMinMaxValue(0, max);

                rs.beforeFirst();
                String row = "";

                boolean bWrite = false;

                int i = 0;
                while (rs.next()) {

                    row = "";
                    bWrite = false;
                    for (String name : hmAsuse.keySet()) {

                        int len = (int) hmAsuse.get(name);

                        String value = rs.getString(name);

                        if (name.equals("value_typ")) {

                            value = "eng";
                        } else if (name.equals("value_date")) {

                            value = dateTime.toString(dtf);

                        } else if (name.equals("value_counter")) {

                            String serial_number = rs.getString("serial_number").trim();

                            List list = Work.getEnergyByNameCount(serial_number, dateTime, 0);

                            if (list.isEmpty()) {

                                break;
                            }

                            bWrite = true;
                            Double dval = (Double) list.get(etyp);

                            value = dval.toString();

                        } else if (name.equals("value_info")) {
                            value = enrgName;
                        }

                        if (value.length() != len) {

                            value = MathTrans.addSpace(value, len);

                        }

                        row = row + value + " ";

                    }

                    if (bWrite) {
                        writer.write(row);
                        writer.append('\r');
                        writer.append('\n');
                    }
                    refreshBarValue(i);

                    i++;
                }

            }

        } finally {
            writer.close();

            rs.close();
        }

        return f;
    }

    /**
     * Импорт объектов асусэ из Excel
     */
    public void importFromASUSE() throws Exception {

        File fUpdate = XmlTask.openFile("xlsx", "Выбор файла импорта", null);

        Workbook wb = null;
        HashMap<String, String> hmDefValues = new HashMap<String, String>(); // Значения по умолчанию

        Document docDeff = Work.getDocConfigByTable("asuse");
        Map<String, String> hmDef = XmlTask.getMapAttrubuteByName(docDeff, "name", "default_value", "cell");
        hmDefValues.putAll(hmDef);

        HashMap<String, Object> hmAsuse = null;

        HashMap<String, Object> hmAsuseRow = null;

        HashMap<String, Integer> hmPosition = new HashMap<>();

        hmPosition.put("number_dog", 10);
        hmPosition.put("kod_dog", 9);
        hmPosition.put("number_object", 14);
        hmPosition.put("kod_object", 13);
        hmPosition.put("kod_point", 17);
        hmPosition.put("serial_number", 23);
        hmPosition.put("zone", 42);
        hmPosition.put("first_date", 45);
        hmPosition.put("last_date", 46);
        hmPosition.put("name_counter", 40);
        hmPosition.put("kod_point_tar", 44);

        // Объекты
        hmAsuse = SqlTask.getMapNamesCol(null, "asuse", 7);

        // Названия и текущие значения  столбцов
        HashMap<String, Object> hmNamecol = new HashMap<String, Object>();
        FileInputStream fileOut = null;
        fileOut = new FileInputStream(fUpdate);
        wb = new XSSFWorkbook(fileOut);

        fileOut.close();

        Sheet sheet = wb.getSheetAt(0);

        hmAsuseRow = new HashMap<>();

        // создаем статемент
        StatementEx seAsuse = new StatementEx(null, "asuse", hmAsuse);

        try {
            // все объекты
            setMinMaxValue(5, sheet.getLastRowNum());

            for (int i = 5; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                for (String name : hmAsuse.keySet()) {

                    if (!hmPosition.containsKey(name)) {

                        hmAsuseRow.put(name, "");
                        continue;
                    }

                    int pozPow = hmPosition.get(name);

                    Cell cell = row.getCell(pozPow);

                    String sValue = "";

                    if (cell == null) {

                        hmAsuseRow.put(name, "");
                        continue;
                    }

                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

                        sValue = cell.getStringCellValue().trim();

                        if (sValue == null || sValue.isEmpty()) {

                            sValue = "";
                        }

                    } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                        Double nValue = cell.getNumericCellValue();

                        if (name.equals("first_date") || name.equals("last_date")) {

                            Date date = cell.getDateCellValue();

                            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");

                            DateTime dateTime = new DateTime(date.getTime());

                            sValue = dateTime.toString(dtf);

                        } else {

                            Integer i1 = nValue.intValue();
                            sValue = i1.toString();
                        }
                    } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {

                        sValue = "";

                    }

                    if (sValue == null || sValue.isEmpty()) {

                        sValue = "";
                    }

                    int size = (int) hmAsuse.get(name);

                    if (sValue.length() > size) {

                        sValue = sValue.substring(0, size);
                    }

                    hmAsuseRow.put(name, sValue);

                }

                seAsuse.replaceRecInTable(hmAsuseRow, true);
                refreshBarValue(i);
            }
        } finally {
            seAsuse.close();

        }
    }

    /**
     * Импорт структуры объектов из таблицы Excel
     */
    public void importFromExcel() throws Exception {

        File fUpdate = XmlTask.openFile("xls", "Выбор файла импорта", null);

        HashMap<String, Object> hmCols = new HashMap<String, Object>();

        HashMap<String, String> hmDefValues = new HashMap<String, String>(); // Значения по умолчанию

        Document docDeff = Work.getDocConfigByTable("objects");
        Map<String, String> hmDef = XmlTask.getMapAttrubuteByName(docDeff, "name", "default_value", "cell");
        hmDefValues.putAll(hmDef);

        docDeff = Work.getDocConfigByTable("points");
        hmDef = XmlTask.getMapAttrubuteByName(docDeff, "name", "default_value", "cell");
        hmDefValues.putAll(hmDef);

        HashMap<String, StatementEx> hmStatements = new HashMap<String, StatementEx>();

        Workbook wb = null;

        HashMap<String, Object> hmPoint = null;
        HashMap<String, Object> hmObject = null;
        HashMap<String, Object> hmCounters = null;
        HashMap<String, Object> hmSubconto = null;

        try {

            // Объекты
            hmObject = SqlTask.getMapNamesCol(null, "objects", 5);
            hmCols.putAll(hmObject);

            // точки подключения
            hmPoint = SqlTask.getMapNamesCol(null, "points", 5);
            hmCols.putAll(hmPoint);

            // приборы учета
            hmCounters = SqlTask.getMapNamesCol(null, "counters", 5);
            hmCols.putAll(hmCounters);

            // контрагенты
            hmSubconto = SqlTask.getMapNamesCol(null, "subconto", 5);
            hmCols.putAll(hmSubconto);

        } catch (SQLException ex) {
            setLoggerInfo("", ex);
        }

        // Названия и текущие значения  столбцов
        HashMap<String, Object> hmNamecol = new HashMap<String, Object>();
        FileInputStream fileOut = null;
        try {
            fileOut = new FileInputStream(fUpdate);
        } catch (FileNotFoundException ex) {
            setLoggerInfo("", ex);
        }
        try {
            wb = new HSSFWorkbook(fileOut);

            fileOut.close();

        } catch (IOException ex) {
            setLoggerInfo("", ex);
        }

        Sheet sheet = wb.getSheetAt(0);

        // Названия всех столбцов таблицы и их позиции
        String nameCol;

        int startRow = 0;
        for (int i = 0; i < 5; i++) {

            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            for (Cell cell : row) {

                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

                    nameCol = cell.getStringCellValue();

                    if (hmCols.containsKey(nameCol)) {

                        hmCols.put(nameCol, cell.getColumnIndex());

                        hmNamecol.put(nameCol, null);

                        startRow = cell.getRowIndex();
                    }
                }

            }
        }

        // создаем статемент
        StatementEx sePoint = new StatementEx(null, "points", hmPoint);
        StatementEx seObject = new StatementEx(null, "objects", hmObject);
        StatementEx seCounters = new StatementEx(null, "counters", hmCounters);
        StatementEx seSubconto = new StatementEx(null, "subconto", hmSubconto);

        try {

            hmStatements.put("stPoint", sePoint);
            hmStatements.put("stObject", seObject);
            hmStatements.put("stCounters", seCounters);
            hmStatements.put("stSubconto", seSubconto);

// Проходим все объекты
            setMinMaxValue(startRow + 1, sheet.getLastRowNum());

            for (int i = startRow + 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                refreshBarValue(i);

                for (String name : hmNamecol.keySet()) {
                    Integer pozRow = (Integer) hmCols.get(name);
                    Cell cell = row.getCell(pozRow);

                    if (cell != null) {

                        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

                            String sValue = cell.getStringCellValue().trim();

                            if (sValue == null || sValue.isEmpty()) {

                                sValue = hmDefValues.get(name);
                            }

                            hmNamecol.put(name, sValue);

                        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                            Object sValue = cell.getNumericCellValue();

                            hmNamecol.put(name, sValue);

                        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {

                            Object sValue = cell.getNumericCellValue();

                            hmNamecol.put(name, sValue);

                        }

                    } else {
                        hmNamecol.put(name, hmDefValues.get(name));
                    }

                }

                try {

                    String idObj = (String) hmNamecol.get("id_object");

                    System.out.println(idObj);

                    createRow(hmNamecol, sePoint, hmDefValues, (HashMap<String, Object>) hmPoint.clone());
                    createRow(hmNamecol, seObject, hmDefValues, (HashMap<String, Object>) hmObject.clone());
                    createRow(hmNamecol, seCounters, hmDefValues, (HashMap<String, Object>) hmCounters.clone());
                    createRow(hmNamecol, seSubconto, hmDefValues, (HashMap<String, Object>) hmSubconto.clone());

                } catch (Exception e) {

                    throw new Exception("Проблемы с импортом", e);
                }

                // Обработка объектов
                // exportObject(hmNamecol, hmStatements, hmDefValues);
            }

        } finally {

            sePoint.close();
            seObject.close();

        }

        hmNamecol.clear();

    }

    private void createRow(HashMap<String, Object> hmValues, StatementEx statem, HashMap<String, String> hmDefValues, HashMap<String, Object> hmColumns) throws Exception {

        for (String nameCol : hmValues.keySet()) {

            if (hmColumns.containsKey(nameCol)) {

                int typ = (int) hmColumns.get(nameCol);

                Object val = hmValues.get(nameCol);

                if (typ == java.sql.Types.TIMESTAMP) {

                    if (val == null || val.equals("NULL")) {

                        Timestamp t = new Timestamp(new DateTime().getMillis());

                        val = t;
                    }

                } else if (typ == java.sql.Types.INTEGER || typ == java.sql.Types.SMALLINT) {

                    if (val == null || val.equals("NULL")) {

                        val = -1;
                    }

                } else if (typ == java.sql.Types.CHAR || typ == java.sql.Types.VARCHAR) {

                    if (val == null || val.equals("NULL")) {

                        val = "";
                    }

                } else if (typ == java.sql.Types.BOOLEAN || typ == java.sql.Types.BIT) {

                    if (val == null || val.equals("NULL")) {

                        val = 0;
                    }

                } else {

                    throw new Exception("Не поддерживаемый тип !");

                }

                hmColumns.put(nameCol, val);

            }

        }

        try {
            statem.replaceRecInTable(hmColumns, true);
        } catch (SQLException ex) {
            MainWorker.deffLoger.error(ex.getMessage());
        }

    }

    private void createObject(HashMap<String, String> hmDefValues, HashMap<String, StatementEx> hmStatements, HashMap<String, Object> hmValues, Integer idObject, Integer idPoint, Integer idCounter) throws SQLException, Exception {

        StatementEx statementEx;
        Integer iType = null;
        Object sub_type4;
        Object sub_type6;

        //Объекты
        HashMap<String, Object> hmCol = SqlTask.getValuesByKey(null, "objects", new Object[]{idObject});

        if (hmCol.isEmpty()) {

            hmCol = SqlTask.getMapNamesCol(null, "objects", 5);
        }

//java.sql.Types.TIMESTAMP
        for (String nameCol : hmCol.keySet()) {

            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmCol.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmCol.put(nameCol, hmDefValues.get(nameCol));
            }

        }

        Object dateInput = hmCol.get("date_input");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("date_input", new Timestamp(new DateTime().getMillis()));
        }

        dateInput = hmCol.get("date_update_obj");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("date_update_obj", new Timestamp(new DateTime().getMillis()));
        }

        dateInput = hmCol.get("sub_type4");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("sub_type4", -1);
        }

        dateInput = hmCol.get("sub_type6");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("sub_type6", -1);
        }

        dateInput = hmCol.get("date_update_poi");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("date_update_poi", new Timestamp(new DateTime().getMillis()));
        }

        dateInput = hmCol.get("date_update_count");

        if (dateInput == null || dateInput.toString().isEmpty()) {

            hmCol.put("date_update_count", new Timestamp(new DateTime().getMillis()));
        }

        // setLoggerInfo(hmCol.toString(), null);
        statementEx = hmStatements.get("stObject");
        statementEx.replaceRecInTable(hmCol, true);

        sub_type4 = hmCol.get("sub_type4");
        sub_type6 = hmCol.get("sub_type6");

        //Точки подключения
        hmCol = SqlTask.getValuesByKey(null, "points", new Object[]{idPoint});

        if (hmCol.isEmpty()) {

            hmCol = SqlTask.getMapNamesCol(null, "points", 1);
        }

        for (String nameCol : hmCol.keySet()) {
            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmCol.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmCol.put(nameCol, hmDefValues.get(nameCol));
            }
        }

        dateInput = hmCol.get("date_update_poi");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("date_update_poi", new Timestamp(new DateTime().getMillis()));
        }

        dateInput = hmCol.get("sub_type11");

        if (dateInput == null || dateInput.toString().isEmpty() || dateInput.equals("NULL")) {

            hmCol.put("sub_type11", -1);
        }

        hmCol.put("c_tree_id", sub_type4);

        statementEx = hmStatements.get("stPoint");
        statementEx.replaceRecInTable(hmCol, true);

        //Приборы учета
        hmCol = SqlTask.getValuesByKey(null, "object5", new Object[]{idCounter});

        if (hmCol.isEmpty()) {

            hmCol = SqlTask.getMapNamesCol(null, "object5", 1);
        }

        for (String nameCol : hmCol.keySet()) {
            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmCol.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmCol.put(nameCol, hmDefValues.get(nameCol));
            }
        }

        dateInput = hmCol.get("date_update_count");

        if (dateInput == null || dateInput.toString().isEmpty()) {

            hmCol.put("date_update_count", new Timestamp(new DateTime().getMillis()));
        }

        hmCol.put("c_tree_id", sub_type6);
        statementEx = hmStatements.get("stCounter");
        statementEx.replaceRecInTable(hmCol, true);

        //  statementEx.replaceRecInTable(hmCol, true);
    }

    private void createObject(HashMap<String, String> hmDefValues, HashMap<String, StatementEx> hmStatements, HashMap<String, Object> hmValues, String namePoint, String nameObject, String modelCounter, Integer addressCounter) throws SQLException {

        ResultSet rs;
        StatementEx statementEx;
        Integer idCounter = null;
        Integer idPoint = null;
        Integer idObject = null;

        //  HashMap<String,Integer> hmNamesPoint=new HashMap<String, Integer>();
        HashMap<String, Object> hmColObject;
        HashMap<String, Object> hmColPoint;
        HashMap<String, Object> hmColCounter = null;

        String sql = "SELECT * FROM objects WHERE dis_number=?";

        //Объект
        rs = SqlTask.getResultSet(null, sql, new Object[]{nameObject});

        if (rs.next()) {

            hmColObject = new HashMap<String, Object>();
            SqlTask.addParamToMap(rs, hmColObject);
            idCounter = (Integer) hmColObject.get("sub_type6");
            idPoint = (Integer) hmColObject.get("sub_type4");
            idObject = (Integer) hmColObject.get("c_tree_id");

        } else {
            hmColObject = SqlTask.getMapNamesCol(null, "objects", 5);
            idObject = SqlTask.getMaxKeyByNameTable(null, "objects");
        }

        for (String nameCol : hmColObject.keySet()) {

            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmColObject.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmColObject.put(nameCol, hmDefValues.get(nameCol));
            }

        }

        Object dateInput = hmColObject.get("date_input");

        if (dateInput.toString().isEmpty()) {

            hmColObject.put("date_input", new Timestamp(new DateTime().getMillis()));
        }

        hmColObject.put("c_tree_id", idObject);

        setLoggerInfo(hmColObject.toString(), null);

        //    statementEx = hmStatements.get("stObject");
        //  statementEx.replaceRecInTable(hmColObject, true);
        // Прибор учета
        if (idCounter == null) {

            hmColCounter = SqlTask.getMapNamesCol(null, "object5", 5);
            idCounter = SqlTask.getMaxKeyByNameTable(null, "object5");

        } else {

            //Приборы учета
            hmColCounter = SqlTask.getValuesByKey(null, "object5", new Object[]{idCounter});

        }

        for (String nameCol : hmColCounter.keySet()) {

            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmColCounter.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmColCounter.put(nameCol, hmDefValues.get(nameCol));
            }

        }

        hmColCounter.put("c_tree_id", idCounter);
        statementEx = hmStatements.get("stCounter");
        statementEx.replaceRecInTable(hmColCounter, true);

        // Подключение
        sql = "SELECT * FROM points WHERE name_point=?";

        rs = SqlTask.getResultSet(null, sql, new Object[]{namePoint});

        if (rs.next()) {

            hmColPoint = new HashMap<String, Object>();
            SqlTask.addParamToMap(rs, hmColPoint);
            idPoint = (Integer) hmColPoint.get("c_tree_id");

        } else {
            hmColPoint = SqlTask.getMapNamesCol(null, "points", 5);
            idPoint = SqlTask.getMaxKeyByNameTable(null, "points");
        }

        for (String nameCol : hmColPoint.keySet()) {

            if (hmValues.containsKey(nameCol)) {
                // Значения из тавлицы
                hmColPoint.put(nameCol, hmValues.get(nameCol));
            } else if (hmDefValues.containsKey(nameCol)) {
                // Значения по умолчанию
                hmColPoint.put(nameCol, hmDefValues.get(nameCol));
            }

        }

        hmColPoint.put("c_tree_id", idPoint);

        statementEx = hmStatements.get("stPoint");
        statementEx.replaceRecInTable(hmColPoint, true);

        statementEx = hmStatements.get("stObject");

        hmColObject.put("sub_type4", idPoint);
        hmColObject.put("sub_type6", idCounter);

        statementEx.replaceRecInTable(hmColObject, true);

    }

    private void exportObject(HashMap<String, Object> hmValues, HashMap<String, StatementEx> hmStatements, HashMap<String, String> hmDefValues) throws Exception {

        Integer idObject;  //c_tree_id
        Integer idPoint;    //sub_type4
        Integer idCounter;// sub_type6

        if (hmValues.containsKey("c_tree_id") && hmValues.containsKey("sub_type4") && hmValues.containsKey("sub_type6")) {

            String sid = (String) hmValues.get("sub_type4");
            idPoint = Integer.decode(sid);
            sid = (String) hmValues.get("sub_type6");
            idCounter = Integer.decode(sid);
            sid = (String) hmValues.get("c_tree_id");
            idObject = Integer.decode(sid);

            createObject(hmDefValues, hmStatements, hmValues, idObject, idPoint, idCounter);

        } else if (hmValues.containsKey("name_point") && hmValues.containsKey("dis_number") && hmValues.containsKey("model_counter")
                && hmValues.containsKey("counter_addres")) {

            String namePoint;
            String nameObject;
            String modelCounter;
            Integer addressCounter;

            namePoint = (String) hmValues.get("name_point");
            nameObject = (String) hmValues.get("dis_number");
            modelCounter = (String) hmValues.get("model_counter");
            String sid = (String) hmValues.get("counter_addres");
            addressCounter = Integer.decode(sid);

            createObject(hmDefValues, hmStatements, hmValues, namePoint, nameObject, modelCounter, addressCounter);

        } else {

            throw new Exception("Файл не соответствует параметрам импорта !");
        }

    }

    private void createRow(Row titleRow, String caption, CellStyle style) {
        Cell titleCell;

        String[] sc = caption.split("/");

        Sheet sheet = titleRow.getSheet();

        // titleRow.getSheet().setColumnWidth(500, 1);
        titleCell = titleRow.createCell(1);
        titleCell.setCellValue(new HSSFRichTextString(sc[0]));
        titleCell.setCellStyle(style);
        sheet.autoSizeColumn(1, true);

        if (sc.length == 2) {
            String[] st = sc[1].split(";");

            for (int i = 0; i < st.length; i++) {
                String ss = st[i];

                titleCell = titleRow.createCell(i + 2);
                titleCell.setCellValue(new HSSFRichTextString(ss));
                titleCell.setCellStyle(style);
                sheet.autoSizeColumn(titleCell.getColumnIndex(), true);

            }
        } else {
        }

    }

    /**
     * Названия допустимых параметров отчета
     */
    public void showListNameParameters() throws Exception {

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Отчет");
        Cell titleCell;
        Row titleRow;
        wb.setActiveSheet(0);
        int rowSheet = 0;
        try {
            HashMap<String, String> hmParam = (HashMap<String, String>) Work.getParamTableByName("objects", Work.TABLE_CAPTION_COL);

            HashMap<String, String> hmParam1 = (HashMap<String, String>) Work.getParamTableByName("points", Work.TABLE_CAPTION_COL);

            hmParam.putAll(hmParam1);

            hmParam1 = (HashMap<String, String>) Work.getParamTableByName("object5", Work.TABLE_CAPTION_COL);

            hmParam.putAll(hmParam1);

            hmParam1 = (HashMap<String, String>) Work.getParamTableByName("controllers", Work.TABLE_CAPTION_COL);

            hmParam.putAll(hmParam1);

            // Заголовок
            titleRow = sheet.createRow(0);
            titleCell = titleRow.createCell(1);
            CellStyle style;
            style = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFont(titleFont);
            style.setBorderBottom(CellStyle.BORDER_MEDIUM);
            titleCell.setCellStyle(style);
            titleCell.setCellValue("Название ");
            titleCell = titleRow.createCell(2);
            titleCell.setCellStyle(style);

            titleCell.setCellValue("Имя ");

            int poz = 1;
            for (String name : hmParam.keySet()) {

                String caption = hmParam.get(name);

                Row row = sheet.createRow(poz);
                Cell cCaption = row.createCell(1);
                Cell cName = row.createCell(2);
                cCaption.setCellValue(caption);
                cName.setCellValue(name);

                sheet.autoSizeColumn(1, true);
                sheet.autoSizeColumn(2, true);

                poz++;
            }

            sheet.createFreezePane(0, 1, 0, 1);

            String dir = System.getProperty("user.dir");
            // }
            fileChek = new File(dir, "tmp.xls");

            //saveWorkbook(wb, fileChek);
            //showReport(fileChek);
            ExcelReport.saveWorkbook(wb, fileChek);
            ExcelReport.showReport(fileChek);

        } catch (SQLException ex) {

            setLoggerInfo("Имена столбцов", ex);
        }

    }

    private void exportTreeInExcel() throws Exception {

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Отчет");
        Cell titleCell;
        Row titleRow;
        wb.setActiveSheet(0);
        int rowSheet = 0;

        TreeMap<String, ArrayList<String>> hmRows;

        hmRows = new TreeMap<String, ArrayList<String>>();

        String sql = "descendant::*";

        String captionObj = document.getDocumentElement().getAttribute("caption_object");

        // Заголовок
        titleRow = sheet.createRow(1);
        titleCell = titleRow.createCell(1);
        CellStyle style;
        style = wb.createCellStyle();

        // style.setBorderTop(CellStyle.BORDER_THIN);
        // style.setBorderLeft(CellStyle.BORDER_THIN);
        // style.setBorderRight(CellStyle.BORDER_THIN);
        Font titleFont = wb.createFont();
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(titleFont);
        titleCell.setCellStyle(style);
        titleCell.setCellValue(captionObj);

        //Время
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

        String sDate = dt.toString(dtf);

        titleRow = sheet.createRow(2);
        titleCell = titleRow.createCell(1);
        style = wb.createCellStyle();
        // style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        // style.setBorderLeft(CellStyle.BORDER_THIN);
        // style.setBorderRight(CellStyle.BORDER_THIN);

        titleFont = wb.createFont();
        // titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(titleFont);
        titleCell.setCellStyle(style);
        titleCell.setCellValue(sDate);

        NodeList nodeList = XmlTask.getNodeListByXpath(document.getDocumentElement(), sql);

        //  NodeList nodeList = document.getElementsByTagName("row");
        for (int i = 0; i < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);
            String sel = element.getAttribute("select");

            if (sel.equals("1")) {

                String caption = element.getAttribute("caption");
                String name = element.getAttribute("name");
                String value = element.getAttribute("value");

                ArrayList<String> alVlues;

                if (!name.isEmpty()) {

                    Element elemParent = (Element) element.getParentNode();
                    String captionParent = elemParent.getAttribute("caption");
                    String headingParent = elemParent.getAttribute("heading");

                    // Заголовок
                    String heading = captionParent + "/" + headingParent;

                    if (hmRows.containsKey(heading)) {

                        alVlues = hmRows.get(heading);

                        alVlues.add(caption + "/" + value);

                    } else {

                        alVlues = new ArrayList<String>();

                        alVlues.add(caption + "/" + value);
                        hmRows.put(heading, alVlues);

                    }

                }

            }
        }

        // Формируем файл
        rowSheet = 4;

        ArrayList<String> alVlues;
        for (String heading : hmRows.keySet()) {

            // Заголовок
            titleRow = sheet.createRow(rowSheet);

            style = wb.createCellStyle();

            titleFont = wb.createFont();
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFont(titleFont);

            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);

            createRow(titleRow, heading, style);

            rowSheet++;

            alVlues = hmRows.get(heading);

            for (String v : alVlues) {

                titleRow = sheet.createRow(rowSheet);

                style = wb.createCellStyle();
                style.setAlignment(CellStyle.ALIGN_RIGHT);
                style.setBorderBottom(CellStyle.BORDER_NONE);
                style.setBorderLeft(CellStyle.BORDER_NONE);
                style.setBorderTop(CellStyle.BORDER_NONE);
                style.setBorderRight(CellStyle.BORDER_NONE);

                createRow(titleRow, v, style);

                rowSheet++;

            }

            rowSheet++;

        }

        //   String dir = System.getProperty("user.dir");
        // }
        File file = File.createTempFile("gsm_", ".xls");

        // fileChek = new File(dir, "tmp.xls");
        //saveWorkbook(wb, fileChek);
        //showReport(fileChek);
        ExcelReport.saveWorkbook(wb, file);
        ExcelReport.showReport(file);

    }

    public void exportTree(Document d) {

        this.document = d;
        currentTask = CT_SHOW_TREE;
        executeProcess();

    }

    private void reportExcel() throws Exception {

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Отчет");
        Cell titleCell;
        Cell nameCell;
        Row captionRow;
        Row titleRow;
        Row nameRow;

        wb.setActiveSheet(0);
        int rowSheet = 0;
        crsThis.last();
        int max = crsThis.getRow();

        setMinMaxValue(0, max);

        refreshBarValue("Всего строк: " + max);

        if (caption != null) {

            captionRow = sheet.createRow(rowSheet);
            rowSheet = 1;
            captionRow.createCell(1).setCellValue(caption);

        }

        titleRow = sheet.createRow(rowSheet);
        nameRow = sheet.createRow(rowSheet + 1);
        ResultSetMetaData metaData = crsThis.getMetaData();

        String nameTable = metaData.getTableName(1).toLowerCase();

        HashMap<String, String> hmCol = (HashMap<String, String>) Work.getNamesColByResultSet(crsThis, nameTable);

        String caption = null;

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = metaData.getColumnName(i).toLowerCase();

            if (hmCol != null) {

                caption = hmCol.get(name);
            } else {
                caption = name;
            }

            titleCell = titleRow.createCell(i);
            titleCell.setCellValue(caption);
            nameCell = nameRow.createCell(i);
            nameCell.setCellValue(name);

            nameRow.setZeroHeight(true);
            sheet.autoSizeColumn(i, true);

        }
        rowSheet = 0;

        crsThis.beforeFirst();

        while (crsThis.next()) {

            rowSheet = crsThis.getRow() + 1;
            refreshBarValue(rowSheet);
            if (isSetStatus(esStop)) {
                excludeStatus(esStop);
                break;
            }
            titleRow = sheet.createRow(rowSheet);

            String name = null;

            Object object = null;

            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                try {

                    name = metaData.getColumnName(i).toLowerCase();
                    object = crsThis.getObject(name);

                    if (object == null) {

                        object = "NULL";
                    }

                    String value = "";

                    if (object instanceof BigDecimal && !name.equals("tarif")) {

                        BigDecimal decimal = (BigDecimal) object;

                        Double d = decimal.doubleValue();

                        if (alKfc != null) {

                            for (Integer kf : alKfc) {

                                d = d * kf;
                            }

                        }

                        titleCell = titleRow.createCell(i);
                        titleCell.setCellValue(d);

                    } else {
                        // Строка

                        if (name.indexOf("password") != -1 && !isSetStatus(esLevelAccess0)) {
                            value = "****";
                        } else {
                            value = object.toString();
                            titleCell = titleRow.createCell(i);
                            titleCell.setCellValue(value);
                        }
                    }

                    rowSheet++;

                } catch (Exception e) {

                    int erow = crsThis.getRow();
                    setLoggerInfo(object.toString() + "[" + erow + "]" + name + "[" + i + "]", e);
                    throw new Exception(e);

                }

            }
        }

        // Стоп только одна строка
        sheet.createFreezePane(0, 1, 0, 1);
        // for (int i=2;i<20;i++){
        // sheet.shiftRows(i,i,1);
        // }
        //  String dir = System.getProperty("user.dir");
        // Document doc = clsConfigXml.GetDocument(clsConfigXml.getFile("config"));
        //String dir = doc.getDocumentElement().getAttribute("path_tmp");
        //if (dir == null || dir.isEmpty()) {
        //  dir = System.getProperty("user.dir");
        // }

        File file = File.createTempFile(nameTable + "_", ".xls");

        //  fileChek = new File(dir, "tmp.xls");
        ExcelReport.saveWorkbook(wb, file);
        ExcelReport.showReport(file);

    }
}

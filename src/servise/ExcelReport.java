/*
 * Работа с отчетами в Excel
 * 
 */
package servise;

import connectdbf.SqlTask;
import connectdbf.StatementEx;
import dbf.Work;
import files.FileFinder;
import forms.DialogSelectDate;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xmldom.XmlTask;

import java.io.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static constatant_static.SettingActions.CM_EXCEL_GO_REPORT;
import static servise.DiffTask.write;

/**
 * @author 1
 */
public class ExcelReport extends MainWorker {

    private static String excel_url; // Путь к Excel
    public static HashMap<String, Object> hm_buff = new HashMap<String, Object>();
    public static String TASK_WEB_REPORT = "createWebReport";
    public boolean jurnal_up; // отчет для журнала превышений
    private static ArrayList<String> alReportGo;
    // private ResultSet rsTable;
    private DateTime d_first; // Дата начала отчета
    private DateTime d_last;// Дата конца отчета
    private boolean bAvto;// Автоматический расчет
    private HashMap<Row, Sheet> hmDinamik;
    private ResultSet rsObject;// если есть обрабатываемые объекты
    private String sqlCurrent; // Текущий запрос
    private Integer idObject;// Объект контроля
    private String nameReport;// Имя отчета
    private Integer idUser; //Пользователь
    private ArrayList<CompareClass> alCompare;//Сравнение величин
    private Byte notise;// Куда посылать превышения
    private String prObject;// параметры контроля
    private String nameSchedule; //название расписания

    private HashMap mapProp;
    private Integer idSchedule;// id Расписания
    private Integer idSelect;// id Выбраного объекта
    private HashMap<Integer, Document> hmUsers; // Карта всех пользователей
    private Workbook wb;
    private boolean stopCreateReport; // Остановка формирования отчета
    private ResultSet rsUsers; // Отчеты пользователей
    private HashMap<String, Element> mapTree;
    private String nameList; // Названия Листов отчета
    private HashMap<String, Object[]> hmParamCol;
    private String ext_report; // расширение отчета;

    private HashMap<Integer, String> hmCollVal; // Для контроля столбцов
    private HashMap<String, HashMap<Integer, Object>> hmValuesColl; // Значения контролируемых столбцов


    private ArrayList<Cell> listSum;
    private ScriptGroovy script;
    private int percent;  // Процент выполнения отчета от 0 до 100
    private File fileCurrent; // созданый файл отчета

    public ExcelReport(ExecutorService pool) {

        this.pool = pool;
        this.ext_report = "xls";

        if (alReportGo == null) {

            alReportGo = new ArrayList<>();
        }

        setLogger(org.apache.log4j.Logger.getLogger("LogExcel"));
        setLoggerInfo("Модуль EXCEL подключен.", null);

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Object[]) {

            Object[] objects = (Object[]) arg;

            String cmnd = (String) objects[0];

            // Отчет в ручном режиме
            if (cmnd.equals(CM_EXCEL_GO_REPORT)) {

                this.sqlCurrent = (String) objects[1];
                Date dateFirst = (Date) objects[2];
                Date dateLast = (Date) objects[3];
                this.nameReport = (String) objects[4];
                this.idSelect = (Integer) objects[5];

                d_first = new DateTime(dateFirst);
                d_last = new DateTime(dateLast);
                hmDinamik = new HashMap<Row, Sheet>();
                hmUsers = new HashMap<Integer, Document>();
                bAvto = false;

                //    if (alReportGo.contains(nameReport)) {
                //      stopProcess = true;
                //    alReportGo.remove(nameReport);
                //  return;
                //  } else {
                //    alReportGo.add(nameReport);
                // }

                try {

                    //  в отдельном потоке
                    executeProcess();
                } catch (Exception ex) {

                    setNotifyObservers(ex);
                }

            }
        }

        if (o instanceof ScheduleClass && arg instanceof Integer) {
            int id = (Integer) arg;
            // Сработало расписание
            ScheduleClass sc = (ScheduleClass) o;
            String nSchedule = sc.getNameSchedule();

            this.idSchedule = id;
            try {
                // расчет по расписанию

                /**
                 * if (FindEvents()) {
                 *
                 * this.nameSchedule = nSchedule; alCompare = new
                 * ArrayList<CompareClass>(); this.bAvto = true; hmDinamik = new
                 * HashMap<Row, Sheet>(); hmUsers = new HashMap<Integer,
                 * Document>();
                 *
                 * // Запускаем в отдельном потоке executeProcess();
                 *
                 * }
                 *
                 *
                 */
            } catch (Exception ex) {
                setNotifyObservers(ex);
            }
        }
    }

    public File getFileCurrent() {
        return fileCurrent;
    }

    public void setFileCurrent(File fileCurrent) {
        this.fileCurrent = fileCurrent;
    }

    public int getPercent() {
        return percent;
    }

    private void clearPercent() {

        percent = 0;
    }

    public void setPercent(int value, int maxValue) {
        this.percent = value * 100 / maxValue;
    }

    public void createWebReport(String nameReport, Integer idSelect, String dateFirst, String dateLast, String sqlReport) throws Exception {

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy");

        this.sqlCurrent = sqlReport;
        this.nameReport = nameReport;
        this.idSelect = idSelect;

        d_first = dtf.parseDateTime(dateFirst);
        d_last = dtf.parseDateTime(dateLast);
        hmDinamik = new HashMap<Row, Sheet>();
        hmUsers = new HashMap<Integer, Document>();
        bAvto = false;

        getReport(nameReport.trim(), sqlCurrent);

    }

    /**
     * @param idReport      -Идентификатор отчета или его название
     * @param idSQL-Объекты отчета
     * @return файл отчета
     * @throws Exception
     */
    public HashMap<String, Object> getReport(Object idReport, Object idSQL) throws Exception {

        File file;
        hmDinamik = new HashMap<>();
        setFileCurrent(null);
        ResultSet rs;
        String sql;

        script = new ScriptGroovy();

        if (idReport instanceof Integer) {
            sql = "SELECT * FROM report WHERE id_report=" + idReport;
            rs = SqlTask.getResultSet(null, sql);

        } else {

            sql = "SELECT * FROM report WHERE name_report=?";
            rs = SqlTask.getResultSet(null, sql, new Object[]{nameReport.trim()});

        }

        try {

            while (rs.next()) {

                byte[] bs = rs.getBytes("file_report");
                nameReport = rs.getString("name_report");
                ext_report = rs.getString("ext_report");

                if (idSQL == null) {
                    idSQL = rs.getInt("id_sql");
                }
                InputStream is = new ByteArrayInputStream(bs);
                wb = WorkbookFactory.create(is);

                is.close();
            }

            if (idSQL instanceof Integer) {

                rsObject = Work.getResSetByNameSql(idSQL);
            } else {

                sql = (String) idSQL;
                rsObject = SqlTask.getResultSet(null, sql);
            }

            nameReport = nameReport.replaceAll(" ", "_");

        } finally {

            rs.close();
        }

        HashMap<String, Object> map = executeReport();


        if (map == null) {
            return map;
        }

        if (hmValuesColl!=null){

            for (String mapName:hmValuesColl.keySet()){

                Map mapVal=hmValuesColl.get(mapName);

                map.put(mapName, mapVal);

            }



        }

        file = File.createTempFile("report_", ".xls");

        saveWorkbook(wb, file);

        setFileCurrent(file);
        map.put("#file", file);

        return map;
    }

    public void createReport(String sql, Date dpFirst, Date dpLast, HashMap<String, Element> mapTree, HashMap<String, Object> mapProp, String nameReport) {

        this.sqlCurrent = sql;
        this.nameReport = nameReport;
        d_first = new DateTime(dpFirst);
        d_last = new DateTime(dpLast);

        hmDinamik = new HashMap<Row, Sheet>();
        hmUsers = new HashMap<Integer, Document>();

        this.mapTree = mapTree;
        //  this.mapProp = mapProp;

        bAvto = false;

    }

    public static void showReport(File file) throws Exception {
        RunExcel(file.getPath());
    }

    public static String findExcel() {

        String result = null;

        FileFinder finder = new FileFinder();

        String arh = System.getProperty("os.arch");

        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        String pf = arh.contains("64") ? "Program Files (x86)" : "Program Files";

        map.put("EXCEL.EXE", "C:\\" + pf + "\\Microsoft Office\\");
        map.put("XLVIEW.EXE", "C:\\" + pf + "\\Microsoft Office\\");
        map.put("scalc.exe", "C:\\" + pf + "\\");

        List list = null;

        for (String excel : map.keySet()) {

            String path = map.get(excel);

            try {

                list = finder.findFiles(path, excel);

            } catch (Exception ex) {
                MainWorker.setLogInfo("Поиск Excel", ex);
            }

            if (list != null && !list.isEmpty()) {

                File file = (File) list.get(0);

                result = file.getAbsolutePath();
                break;
            }

        }

        return result;
    }

    public static void RunExcel(String url) throws Exception {

        Runtime run = Runtime.getRuntime();
        Process p = null;

        if (excel_url == null || excel_url.isEmpty()) {
            excel_url = (String) Work.getLocaleParam("excel_url");
        }

        if (excel_url != null && !excel_url.isEmpty() && !new File(excel_url).exists()) {

            excel_url = findExcel();

            if (excel_url != null && !excel_url.isEmpty() && !new File(excel_url).exists()) {
                throw new Exception("Не прописан путь к программе Excel !");
            }
        }

        String os = System.getProperty("os.name");

        if (os.toLowerCase().contains("linux")) {
            url = excel_url + " " + url;

        } else {
            // Windows
            url = "\"" + excel_url + "\"" + " " + url;
        }

        try {
            p = run.exec(url);

// p.waitFor();
        } finally {
            // p.destroy();
            //  System.exit(1);
        }
    }

    public static void setExcel_url(String excel_url1) {

        excel_url = excel_url1;
    }

    // Инициализация контролируемых ячеек
    private void setSetting() {

        if (prObject == null) {
            return;
        }

        alCompare = new ArrayList<CompareClass>();

        String sett[] = prObject.split(";");

        CompareClass compare;

        for (String s : sett) {
            compare = new CompareClass(s);
            alCompare.add(compare);

        }
    }

    // Для конкретного пользователя
    private void goReportForUser() throws Exception {

        Document docUser;
        ResultSet rsReport = null;

        setSetting();

        if (idObject == null || prObject == null || notise == null) {
            //  alErrors.add("По пользователю " + idUser + " не заполнены ВСЕ параметры контроля объектов !");
            return;
        }

        if (!hmUsers.containsKey(idUser)) {
            // Создаем новый
            docUser = Work.getXMLDocByIdObject(idUser);
            hmUsers.put(idUser, docUser);
        } else {
            // Уже есть
            docUser = hmUsers.get(idUser);
        }

        // Тип объекта
        Integer idTable = Work.getIdTableByObjectId(idObject);
        if (idTable == null) {
            //alErrors.add("(Пользователь " + idUser + ") Для объекта " + idObject + " не определен тип !");
            return;
        }

        String sql = "SELECT * FROM report WHERE id_report=" + idObject;
        rsReport = SqlTask.getResultSet(null, sql);

        try {
            if (rsReport.next()) {
                nameReport = rsReport.getString("name_report");
                InputStream is = rsReport.getBinaryStream("file_report");
                if (is != null) {
                    try {
                        try {
                            wb = WorkbookFactory.create(is);

                            is.close();
                        } catch (IOException ex) {
                            //           alErrors.add(ex);
                        }

                    } catch (InvalidFormatException ex) {

                        //     alErrors.add(ex);
                    }

                } else {
                    // alErrors.add("(Пользователь " + idUser + ") объект " + idObject + ". Проверте наличие файла отчета!");
                    return;
                }
                createReport();
            } else {
                // alErrors.add("(Пользователь " + idUser + ") объект " + idObject + " не найден !");
                return;
            }
        } finally {
            rsReport.getStatement().close();
        }

    }

    private HashMap<String, Object> getValueByName(Workbook wb) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        //FormulaEvaluator evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
        Object r = null;

        // Проверям поддиапазоны
        int inames = wb.getNumberOfNames();
        for (int i = 0; i < inames; i++) {
            Name sheetName = wb.getNameAt(i);

            String sName = sheetName.getNameName();

            Name fName = wb.getNameAt(i);
            String sn = fName.getSheetName();
            Sheet sheet_result = wb.getSheet(sn);

            // FormulaEvaluator evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
            String reference = fName.getRefersToFormula();

            try {

                CellReference cellReference = new CellReference(reference);

                int rcell = cellReference.getRow();
                int ccell = cellReference.getCol();

                Row row_res = sheet_result.getRow(rcell);
                Cell sres = row_res.getCell(ccell);

                ///String svalue = cell.getRichStringCellValue().getString();
                //  AreaReference ar = new AreaReferenceference(Name.getNameName());
                // HSSFRow row_res = sheet_result.getRow(rr);
                //  CellReference[] cells = ar.getAllReferencedCells();
                // CellReference cr = ar.getFirstCell();
                //  int rc = ar.getFirstCell().getCol();
                //int rr = ar.getFirstCell().getRow();
                //    HSSFRow row_res = sheet_result.getRow(rr);
                //  HSSFCell sres = row_res.getCell(rc);
                if (sres != null) {

                    int type = sres.getCellType();

                    if (type == Cell.CELL_TYPE_FORMULA) {

                        CellValue cellValue = evaluator.evaluate(sres);

                        int ft = cellValue.getCellType();

                        if (ft == Cell.CELL_TYPE_NUMERIC) {

                            r = sres.getNumericCellValue();
                        }

                        if (ft == Cell.CELL_TYPE_STRING) {

                            r = sres.getRichStringCellValue().getString();
                        }

                        if (ft == Cell.CELL_TYPE_ERROR) {

                            r = null;
                        }

                    }

                    if (type == Cell.CELL_TYPE_NUMERIC) {
                        r = sres.getNumericCellValue();
                    }

                    if (type == Cell.CELL_TYPE_STRING) {
                        r = sres.getRichStringCellValue().getString();
                    }

                }

            } catch (Exception ex) {

                setLoggerInfo("Значения именованых ячеек", ex);

            }

            result.put(sName, r);

        }

        return result;

    }

    private Object getValue(Workbook wb, String nameResult) {

        Object r = null;

        FormulaEvaluator evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) wb);

        int idx = wb.getNameIndex(nameResult);

        if (idx == -1) {
            return null;
        }

        Name fName = wb.getNameAt(idx);
        String sn = fName.getSheetName();
        Sheet sheet_result = wb.getSheet(sn);

        String reference = fName.getRefersToFormula();

        try {

            CellReference cellReference = new CellReference(reference);

            int rcell = cellReference.getRow();
            int ccell = cellReference.getCol();

            Row row_res = sheet_result.getRow(rcell);
            Cell sres = row_res.getCell(ccell);

            ///String svalue = cell.getRichStringCellValue().getString();
            //  AreaReference ar = new AreaReferenceference(Name.getNameName());
            // HSSFRow row_res = sheet_result.getRow(rr);
            //  CellReference[] cells = ar.getAllReferencedCells();
            // CellReference cr = ar.getFirstCell();
            //  int rc = ar.getFirstCell().getCol();
            //int rr = ar.getFirstCell().getRow();
            //    HSSFRow row_res = sheet_result.getRow(rr);
            //  HSSFCell sres = row_res.getCell(rc);
            if (sres != null) {

                int type = sres.getCellType();

                if (type == Cell.CELL_TYPE_FORMULA) {

                    CellValue cellValue = evaluator.evaluate(sres);

                    int ft = cellValue.getCellType();

                    if (ft == Cell.CELL_TYPE_NUMERIC) {

                        r = sres.getNumericCellValue();
                    }

                    if (ft == Cell.CELL_TYPE_STRING) {

                        r = sres.getRichStringCellValue().getString();
                    }

                    if (ft == Cell.CELL_TYPE_ERROR) {

                        r = null;
                    }

                }

                if (type == Cell.CELL_TYPE_NUMERIC) {
                    r = sres.getNumericCellValue();
                }

                if (type == Cell.CELL_TYPE_STRING) {
                    r = sres.getRichStringCellValue().getString();
                }

            }

        } catch (Exception ex) {

            setLoggerInfo(nameResult, ex);

        }

        return r;
    }

    public static int getParamByNumber(String sCell, int num) {

        int result = -1;

        int b = sCell.indexOf("(");
        int e = sCell.indexOf(")");

        if (b < 0 || e < 0) {
            return result;
        }

        String par = sCell.substring(b + 1, e);

        String p[] = par.split(",");

        if (p.length > num) {
            par = p[num];
        } else {

            return result;
        }

        try {

            result = Integer.parseInt(par.trim());

        } catch (NumberFormatException ex) {

            //   Work.logger.error("", ex);
            result = -1;

        }

        return result;
    }

    public static int getParametersId(String sCell) {

        int result = -1;

        String par = Work.getDelimitedString(sCell, '(', ')');

        if (par == null) {
            return -1;
        }

        String p[] = par.split(",");

        if (p.length == 2 || p.length == 3) {
            par = p[1];
        }

        try {

            result = Integer.parseInt(par.trim());

        } catch (NumberFormatException ex) {

            result = -1;

        }

        return result;
    }

    public static int getObjectId(String sCell) {

        int result = -1;

        String par = Work.getDelimitedString(sCell, '(', ')');

        if (par == null) {
            return result;
        }

        String p[] = par.split(",");

        if (p.length == 2 || p.length == 3) {
            par = p[0];
        } else {

            return result;

        }

        try {

            result = Integer.parseInt(par.trim());

        } catch (NumberFormatException ex) {

            //   Work.logger.error("", ex);
            result = -1;

        }

        return result;
    }

    public static Object getValueByCell(ResultSet rsThis, String sCell, Integer typId) throws SQLException {
        Object result = null;
        String[] v;

        String par = "";

        par = Work.getDelimitedString(sCell, '(', ')').trim();

        HashMap<String, Object> hmValues;

        if (par == null) {
            return result;
        }
        try {

            result = rsThis.getObject(par);

        } catch (SQLException ex) {

            try {
                // ищем и в подчиненных

                if (typId != null) {

                    int id;

                    if (typId == 0) {
                        id = rsThis.getInt("c_tree_id");
                        hmValues = Work.getParametersRow(id, rsThis, "objects", true, false);

                    } else {
                        id = rsThis.getInt("Id_object");
                        hmValues = Work.getParametersRow(id, null, "objects", true, false);

                    }

                    if (hmValues.containsKey(par)) {

                        result = hmValues.get(par);
                    } else {
                        result = null;
                    }

                }

            } catch (SQLException sqle) {

                result = sqle.getMessage();

            }
        }
        return result;
    }

    public static void saveWorkbook(Workbook wbs, File filename) throws Exception {

        FileOutputStream out = null;
        out = new FileOutputStream(filename);
        wbs.write(out);
        out.close();

    }

    private static void showReport(Workbook wb, String nameRep) throws Exception {
        if (nameRep.equals("xsl")) {
            nameRep = "xls";
        }

        File file = File.createTempFile("tmp_", "." + nameRep);
        saveWorkbook(wb, file);
        RunExcel(file.getPath());

    }

    public void createDinamicReport(Workbook wb, Row rowGo, Sheet sheetGo) {

        Row newRow;
        Cell newCell;
        Integer typId;// тип идентификатора

        String sDay;
        DateTime dateTime;
        String sql = null;
        HashMap<String, Object> hmCurParam;//

        // HSSFName Name = wb.createName();
        // Name.setNameName("Названия");
        // Name.setReference("'Данные'!$A$1:$A$11");
        int countr = 0;
        Integer idObj;
        int istart;
        int iEnd;

        ArrayList<Name> alNames;

        Name sheetName;
        // Динамический расчет
        try {

            // Создаем запрос
            if (sqlCurrent != null && !sqlCurrent.isEmpty()) {
                try {
                    rsObject = SqlTask.getResultSetBySaveSql(null, sqlCurrent, ResultSet.CONCUR_READ_ONLY);

                    if (!rsObject.next()) {
                        return;
                    }

                } catch (Exception ex) {
                    setNotifyObservers(ex);
                    setLoggerInfo("Создание отчета", ex);

                }
            }

            String keyTable = SqlTask.getPrimaryKeyTable(null, "objects");

            alNames = new ArrayList<Name>();

            // Проверяем наличие поля идентификатора
            try {

                // Для объектов
                idObj = rsObject.getInt("id_object");

                typId = 0;

            } catch (SQLException e) {

                try {

                    // для данных
                    idObj = rsObject.getInt("Id_object");

                    typId = 1;
                } catch (SQLException ex) {

                    typId = null;

                }

            }

            // Проверяем на динамическое расширение
            // Проверяем, есть ли имена для столбцов
            int r = rowGo.getRowNum();

            // Проверям поддиапазоны
            int inames = wb.getNumberOfNames();
            for (int i = 0; i < inames; i++) {
                sheetName = wb.getNameAt(i);
                AreaReference ar = new AreaReference(sheetName.getRefersToFormula());
                CellReference cf = ar.getFirstCell();
                int rsh = cf.getRow();

                // Имена в динамическом шаблоне
                if (r == rsh) {

                    alNames.add(sheetName);
                }
            }
            rsObject.last();
            countr = rsObject.getRow();

            setMinMaxValue(0, countr);
            refreshBarValue(nameReport);

            // setNotify(new Point(0, countr));
            // Вставляем строки
            istart = rowGo.getRowNum();
            iEnd = sheetGo.getLastRowNum();

            // Стоп только одна строка
            sheetGo.createFreezePane(0, istart);

            // Вставляем строки
            if (istart + 1 < iEnd) {
                sheetGo.shiftRows(istart + 1, iEnd, countr - 1, false, false);
            }

            int curRow = istart - 1;

            rsObject.beforeFirst();

            while (rsObject.next()) {

                // Добавляем строку
                if (stopCreateReport) {

                    stopCreateReport = false;
                    return;
                }

                curRow++;

                refreshBarValue(curRow);

                newRow = sheetGo.createRow(curRow);

                String svalue;

                // Проходим все столбцы
                for (int i = 0; i < rowGo.getLastCellNum(); i++) {
                    Cell cell = rowGo.getCell(i);
                    if (cell == null) {
                        continue;
                    }

                    CellStyle style = cell.getCellStyle();

                    int type = cell.getCellType();

                    // Динамическое расширение
                    if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().startsWith("##COUNT")) {
                    }

                    newCell = newRow.createCell(i);

                    newCell.setCellStyle(style);

                    if (type == Cell.CELL_TYPE_STRING) {
                        svalue = cell.getStringCellValue();

                        if (svalue.startsWith("#CDN")) {

                            DateTimeFormatter dtf1 = DateTimeFormat.forPattern("dd.MM.yyyy");
                            newCell.setCellType(Cell.CELL_TYPE_STRING);
                            newCell.setCellValue(dtf1.print(d_first));

                        }

                        if (svalue.startsWith("#CDK")) {

                            DateTimeFormatter dtf1 = DateTimeFormat.forPattern("dd.MM.yyyy");
                            newCell.setCellType(Cell.CELL_TYPE_STRING);
                            newCell.setCellValue(dtf1.print(d_last));

                        }

                        if (svalue.startsWith("#COUNT")) {

                            PreparedStatement statement = null;
                            int valCount = 0;

                            Timestamp timestampFirst = null;
                            Timestamp timestampLast = null;

                            if (hmParamCol.containsKey(svalue)) {
                                statement = (PreparedStatement) hmParamCol.get(svalue)[0];
                                timestampFirst = (Timestamp) hmParamCol.get(svalue)[1];
                                timestampLast = (Timestamp) hmParamCol.get(svalue)[2];

                            } else {

                                int par = getParametersId(svalue);
                                hmCurParam = SqlTask.getValuesByKey(null, "c_parnumber", new Object[]{par});
                                String nameTab = (String) hmCurParam.get("name_table");
                                int poz = svalue.indexOf("(");
                                sDay = svalue.substring(6, poz);
                                dateTime = new DateTime(d_last).minusDays(Integer.decode(sDay));
                                timestampFirst = new Timestamp(dateTime.getMillis());
                                dateTime = dateTime.plusDays(1);
                                timestampLast = new Timestamp(dateTime.getMillis());
                                sql = "SELECT COUNT(*) FROM " + nameTab + " WHERE value_date>=? AND value_date<? AND Id_object=?";

                            }

                            idObject = rsObject.getInt(keyTable);

                            ResultSet rs = SqlTask.getResultSet(null, sql, new Object[]{timestampFirst, timestampLast, idObject}, statement);

                            if (rs.next()) {

                                valCount = rs.getInt(1);

                            }

                            if (statement == null) {

                                statement = (PreparedStatement) rs.getStatement();
                                hmParamCol.put(svalue, new Object[]{statement, timestampFirst, timestampLast});
                            }

                            newCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            newCell.setCellValue(valCount);

                        }

                        if (svalue.startsWith("#COL")) {

                            Object value = getValueByCell(rsObject, svalue, typId);

                            if (value instanceof String) {

                                newCell.setCellType(Cell.CELL_TYPE_STRING);
                                newCell.setCellValue(String.valueOf(value));

                            } else if (value instanceof Number) {

                                if (value instanceof Integer) {
                                    newCell.setCellValue((Integer) value);

                                } else if (value instanceof BigDecimal) {

                                    BigDecimal bd = (BigDecimal) value;

                                    double d = bd.doubleValue();

                                    newCell.setCellValue(d);

                                }

                                newCell.setCellType(Cell.CELL_TYPE_NUMERIC);

                            } else if (value instanceof Timestamp) {

                                Timestamp t = (Timestamp) value;

                                DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

                                newCell.setCellType(Cell.CELL_TYPE_STRING);

                                String dd = dtf.print(t.getTime());
                                newCell.setCellValue(dd);

                            }

                        } else if (svalue.startsWith("#SN") || svalue.startsWith("#SK")) {

                            DateTime d = null;

                            if (svalue.startsWith("#SN")) {
                                d = d_first;
                            } else {
                                d = d_last;
                            }

                            int par = getParametersId(svalue);

                            if (par == -1) {

                                newCell.setCellValue("ERROR");

                                continue;
                            }

                            if (typId == 0) {

                                idObj = rsObject.getInt("id_object");

                            } else {
                                idObj = rsObject.getInt("Id_object");

                            }

                            Parameter parameter = new Parameter(idObj, par, d, false);
                            Double val = parameter.getValuePar();

                            if (val != null) {
                                newCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                newCell.setCellValue(val);
                            } else {
                                newCell.setCellType(Cell.CELL_TYPE_STRING);
                                newCell.setCellValue("N/D");

                            }

                            //   String sv=String.format("", val);
                        }
                    } else if (type == Cell.CELL_TYPE_FORMULA) {

                        svalue = cell.getCellFormula();

                        //   CellReference reference=new CellReference(cell);
                        //    FormulaEvaluator evaluator=wb.getCreationHelper().createFormulaEvaluator();
                        //cellReference = new  CellReference ("B3");
                        //Строка строка = sheet.getRow (cellReference.getRow ());
                        //Сотовые ячейки = row.getCell (cellReference.getCol ());
                        //  evaluator.evaluate(cell);
                        HSSFEvaluationWorkbook hssfew = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);

                        Ptg[] parse = FormulaParser.parse(svalue, hssfew, FormulaType.NAMEDRANGE, 0);

                        String formula = "";

                        Ptg[] parseNew = new Ptg[parse.length];

                        for (int j = 0; j < parse.length; j++) {

                            Ptg rec = parse[j];

                            if (rec instanceof RefPtg) {

                                RefPtg refPtg = (RefPtg) rec;

                                // String s = rec.toFormulaString();
                                //CellReference reference = new CellReference(s);
                                //     int row = refPtg.getRow();
                                int col = refPtg.getColumn();

                                //   row++;
                                refPtg = new RefPtg(curRow, col, true, true);

                                parseNew[j] = refPtg;

                            } else {

                                parseNew[j] = rec;

                            }
                        }

                        formula = FormulaRenderer.toFormulaString(hssfew, parseNew);
                        newCell.setCellType(Cell.CELL_TYPE_FORMULA);
                        newCell.setCellFormula(formula);

                    }
                }

            }

// Перевыводим имена диапазонов;
            HSSFEvaluationWorkbook hssfew = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);

            for (Name n : alNames) {

                String sv = n.getRefersToFormula();

//sv="расход!$H$5:$H$21";
                Ptg[] parse = FormulaParser.parse(sv, hssfew, FormulaType.NAMEDRANGE, 0);

                Ptg[] parseNew = new Ptg[parse.length];

                for (int j = 0; j < parse.length; j++) {

                    Ptg rec = parse[j];

                    if (rec instanceof Ref3DPtg) {

                        Ref3DPtg refPtg = (Ref3DPtg) rec;

                        int row = refPtg.getRow();
                        int col = refPtg.getColumn();
                        int sIdx = refPtg.getExternSheetIndex();

                        Area3DPtg area3DPtg;

                        area3DPtg = new Area3DPtg(row, curRow, col, col, true, true, true, true, sIdx);

                        String form = area3DPtg.toFormulaString(hssfew);

                        n.setRefersToFormula(form);

                    } else {
                    }
                }

            }

            //   sheetGo.removeRow(rowGo);
        } catch (SQLException ex) {
            setLoggerInfo(sql, ex);
        }

    }

    private void setValueInCell(Object value, Cell valCell) {

        if (value instanceof String) {

            valCell.setCellType(Cell.CELL_TYPE_STRING);
            valCell.setCellValue(String.valueOf(value));

        } else if (value instanceof Number) {

            if (value instanceof Integer) {
                valCell.setCellValue((Integer) value);

            } else if (value instanceof BigDecimal) {

                BigDecimal bd = (BigDecimal) value;

                double d = bd.doubleValue();

                valCell.setCellValue(d);

            } else if (value instanceof Double) {

                Double d = (Double) value;
                valCell.setCellValue(d);

            }
            valCell.setCellType(Cell.CELL_TYPE_NUMERIC);

        } else {

            valCell.setCellType(Cell.CELL_TYPE_STRING);
            valCell.setCellValue(String.valueOf(value));

        }

    }

    private void evalSheet(Sheet sheet, HashMap<String, Object> mapParam) {

        for (Row row : sheet) {

            refreshBarValue(row.getRowNum());

            // if (stopCreateReport) {
            //return;
            // }
            for (Cell cell : row) {

//                    if (stopCreateReport) {
                //                      return;
                //                }
                int type = cell.getCellType();

                if (type == Cell.CELL_TYPE_BLANK) {
                }

                if (type == Cell.CELL_TYPE_STRING) {

                    String svalue = cell.getStringCellValue();

                    if (svalue.startsWith("#")) {
                        // Проверяем не скрипт ли...

                        Object value;
                        try {


                            value = script.runScript(svalue, mapParam);

                        } catch (Exception ex) {

                            value = "ERROR";
                            setLoggerInfo("", ex);
                        }

                        setValueInCell(value, cell);
                    }
                }
            }
        }

    }

    // Одиночный отчет 
    public void executeSingleReport(Workbook wb) throws SQLException {

        HashMap<String, Object> hmCurParam;//

        int countr = 0;

        if (!rsObject.next()) {
            return;
        }

        rsObject.last();
        countr = rsObject.getRow();

        setMinMaxValue(0, countr);
        refreshBarValue(nameReport);

        rsObject.beforeFirst();

        Sheet sheetGo;

        sheetGo = wb.getSheetAt(0);

        int row = 0;

        while (rsObject.next()) {

            hmCurParam = Work.getParametersObject(null, rsObject, true, true, true);

            sheetGo = wb.cloneSheet(0);
            int idx = wb.getSheetIndex(sheetGo);

            String lName = "Лист - " + idx;
            if (nameList != null) {
                lName = hmCurParam.get(nameList).toString();

            }

            wb.setSheetName(idx, lName);

            evalSheet(sheetGo, hmCurParam);

            row++;
            refreshBarValue(row);

//Обрабатываем листы
        }

        wb.removeSheetAt(0);

    }

    public void executeDinamicReport(Workbook wb, Row rowGo, Sheet sheetGo) throws SQLException {

        Row newRow;
        Cell newCell;
        Integer typId;// тип идентификатора

        String sDay;
        DateTime dateTime;
        String sql = null;
        HashMap<String, Object> hmCurParam;//

        hmCurParam = new HashMap<String, Object>();

        // HSSFName Name = wb.createName();
        // Name.setNameName("Названия");
        // Name.setReference("'Данные'!$A$1:$A$11");
        int countr = 0;
        Integer idObj;
        int istart;
        int iEnd;

        ArrayList<Name> alNames;

        Name sheetName;
        // Динамический расчет
        try {


            // Создаем запрос
            if (!rsObject.next()) {
                return;
            }

            //  } catch (Exception ex) {
            //    setNotifyObservers(ex);
            //  alErrors.add(ex);
            // }
            //String keyTable = SqlTask.getPrimaryKeyTable(null, "objects");
            alNames = new ArrayList<Name>();

            // Проверяем наличие поля идентификатора
            // Проверяем на динамическое расширение
            // Проверяем, есть ли имена для столбцов
            int r = rowGo.getRowNum();

            // Проверям поддиапазоны
            int inames = wb.getNumberOfNames();
            for (int i = 0; i < inames; i++) {
                sheetName = wb.getNameAt(i);
                AreaReference ar = new AreaReference(sheetName.getRefersToFormula());
                CellReference cf = ar.getFirstCell();
                int rsh = cf.getRow();

                // Имена в динамическом шаблоне
                if (r == rsh) {

                    alNames.add(sheetName);
                }
            }
            rsObject.last();
            countr = rsObject.getRow();


            clearPercent();
            setMinMaxValue(0, countr);
            refreshBarValue(nameReport);

            // setNotify(new Point(0, countr));
            if (countr == 1) {
                countr = countr + 1;
            }

            // Вставляем строки
            istart = rowGo.getRowNum();
            iEnd = sheetGo.getLastRowNum();

            // Стоп только одна строка
            sheetGo.createFreezePane(0, istart);

            // Вставляем строки
            if (istart + 1 < iEnd) {
                sheetGo.shiftRows(istart + 1, iEnd, countr - 1, false, false);
            }

            int curRow = istart - 1;

            rsObject.beforeFirst();

            while (rsObject.next()) {

                // Добавляем строку
                if (stopCreateReport) {

                    stopCreateReport = false;
                    return;
                }

                curRow++;

                // Процент выполнения
                setPercent(curRow, countr);
                refreshBarValue(curRow);

                newRow = sheetGo.createRow(curRow);

                String svalue;

                hmCurParam.clear();


                if (jurnal_up) {


                    HashMap<String, Object> hmJurParam = Work.getParametersObject(null, rsObject, true, true, true);
                    ;
                    int id = rsObject.getInt("id_object");


                    hmCurParam = Work.getParametersObject(id, null, false, false, false);
                    hmCurParam.putAll(hmJurParam);


                    Object o = hmCurParam.get("inn_object");

                    String ustavka = "-";
                    if (o != null) {

                        String inn_object = o.toString();

                        String[] prev = inn_object.split(";");

                        if (prev.length == 2) {

                            String min = prev[0];
                            String max = prev[1];

                            String event = hmJurParam.get("event").toString();

                            ustavka = (event.equals("больше") ? max : min);

                        }


                    }

                    hmCurParam.put("ustavka", ustavka);

                } else {

                    hmCurParam = Work.getParametersObject(null, rsObject, true, true, true);
                }


                // Проходим все столбцы
                for (int i = 0; i < rowGo.getLastCellNum(); i++) {
                    Cell cell = rowGo.getCell(i);
                    if (cell == null) {
                        continue;
                    }

                    CellStyle style = cell.getCellStyle();

                    int type = cell.getCellType();

                    // Динамическое расширение
                    newCell = newRow.createCell(i);

                    newCell.setCellStyle(style);

                    if (type == Cell.CELL_TYPE_STRING) {
                        svalue = cell.getStringCellValue();

                        // Скрипт 
                        if (svalue.startsWith("#")) {

                            Object value;
                            try {

                                value = script.runScript(svalue, hmCurParam);

                            } catch (Exception ex) {

                                value = "ERROR";
                                setLoggerInfo("", ex);
                            }

                            setValueInCell(value, newCell);

                        }
                    } else if (type == Cell.CELL_TYPE_FORMULA) {

                        svalue = cell.getCellFormula();


                        //   CellReference reference=new CellReference(cell);
                        //   FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                        //cellReference = new  CellReference ("B3");
                        //Строка строка = sheet.getRow (cellReference.getRow ());
                        //Сотовые ячейки = row.getCell (cellReference.getCol ());


                        // CellValue value = evaluator.evaluate(cell);

                        // System.out.print(value.toString());


                        HSSFEvaluationWorkbook hssfew = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);

                        Ptg[] parse = FormulaParser.parse(svalue, hssfew, FormulaType.NAMEDRANGE, 0);

                        String formula = "";

                        Ptg[] parseNew = new Ptg[parse.length];

                        for (int j = 0; j < parse.length; j++) {

                            Ptg rec = parse[j];

                            if (rec instanceof RefPtg) {

                                RefPtg refPtg = (RefPtg) rec;

                                // String s = rec.toFormulaString();
                                //CellReference reference = new CellReference(s);
                                //     int row = refPtg.getRow();
                                int col = refPtg.getColumn();

                                //   row++;
                                refPtg = new RefPtg(curRow, col, true, true);

                                parseNew[j] = refPtg;

                            } else {

                                parseNew[j] = rec;

                            }
                        }

                        formula = FormulaRenderer.toFormulaString(hssfew, parseNew);
                        newCell.setCellType(Cell.CELL_TYPE_FORMULA);
                        newCell.setCellFormula(formula);

                    }
                    // Данные для контролируемых столбцов


                    Integer id_object;

                    String nameCol;

                    if (hmCollVal != null) {
                        for (Integer col : hmCollVal.keySet()) {

                            if (newCell.getColumnIndex() == col) {
                                nameCol = hmCollVal.get(col);
                                id_object = (Integer) hmCurParam.get("id_object");
                                addValue(newCell, id_object, nameCol);
                            }


                        }
                    }

                }

            }

            if (listSum != null && !listSum.isEmpty()) {


                for (Cell cellSum : listSum) {

                    int lastRow = cellSum.getRowIndex();
                    String formula = cellSum.getCellFormula();
                    formula = formula.replaceFirst("" + (lastRow + 1), "" + lastRow);
                    cellSum.setCellFormula(formula);
                }

            }


// Перевыводим имена диапазонов;
            HSSFEvaluationWorkbook hssfew = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);

            for (Name n : alNames) {

                String sv = n.getRefersToFormula();

//sv="расход!$H$5:$H$21";
                Ptg[] parse = FormulaParser.parse(sv, hssfew, FormulaType.NAMEDRANGE, 0);

                Ptg[] parseNew = new Ptg[parse.length];

                for (int j = 0; j < parse.length; j++) {

                    Ptg rec = parse[j];

                    if (rec instanceof Ref3DPtg) {

                        Ref3DPtg refPtg = (Ref3DPtg) rec;

                        int row = refPtg.getRow();
                        int col = refPtg.getColumn();
                        int sIdx = refPtg.getExternSheetIndex();

                        Area3DPtg area3DPtg;

                        area3DPtg = new Area3DPtg(row, curRow, col, col, true, true, true, true, sIdx);

                        String form = area3DPtg.toFormulaString(hssfew);

                        n.setRefersToFormula(form);

                    } else {
                    }
                }

            }

        } finally {
            rsObject.close();
        }
    }

    private void addValue(Cell cell, Integer id, String nameCol) {
        CellValue value = null;
        Object svalue;


        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            value = evaluator.evaluate(cell);


            if (value.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                svalue = value.getNumberValue();
            } else if (value.getCellType() == Cell.CELL_TYPE_STRING) {

                svalue = value.getStringValue();
            } else if (value.getCellType() == Cell.CELL_TYPE_BOOLEAN) {

                svalue = value.getBooleanValue();
            } else {
                svalue = value.formatAsString();
            }


        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

            svalue = cell.getNumericCellValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

            svalue = value.getStringValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {

            svalue = value.getBooleanValue();
        } else {
            svalue = value.formatAsString();
        }


        if (hmValuesColl.containsKey(nameCol)) {

            Map mValues = hmValuesColl.get(nameCol);
            mValues.put(id, svalue);




        } else {

            HashMap<Integer, Object> mValues = new HashMap<>();
            mValues.put(id, svalue);
            hmValuesColl.put(nameCol,mValues);

        }


    }


    //bAvto-в автоматическом режиме
    public HashMap<String, Object> executeReport() throws SQLException {

        hm_buff.clear();
        jurnal_up = false;
        boolean single = false;

        boolean select = false;
        HashMap<Integer, String> hmColor = new HashMap<Integer, String>();
        HashMap<Integer, String> hmFlags = new HashMap<Integer, String>();

        HashMap<String, Object> hmCurParam = null;

        HashMap<String, Object> result = new HashMap<String, Object>();

        String sval;
        Sheet sheet = null;

        //  for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
        // sheet = wb.getSheetAt(sheetNum);
        // int sheetNum = 0;
        sheet = wb.getSheetAt(0);

        //  if (stopCreateReport) {
        //    return;
        // }
        // if (!bAvto) {

        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            sheet = wb.getSheetAt(sheetNum);

            String nameSheet = sheet.getSheetName();


            setMinMaxValue(0, sheet.getLastRowNum());

            String rName = nameReport + "..." + nameSheet;
            refreshBarValue(rName);
            setNotifyObservers(rName);

            for (Row row : sheet) {

                if (stopProcess) {

                    refreshBarValue("Остановка создания отчета...");
                    return null;
                }

                refreshBarValue(row.getRowNum());

                // if (stopCreateReport) {
                //return;
                // }
                for (Cell cell : row) {

//                    if (stopCreateReport) {
                    //                      return;
                    //                }
                    int type = cell.getCellType();


                    if (type == Cell.CELL_TYPE_BLANK) {
                    }

                    if (type == Cell.CELL_TYPE_FORMULA) {

                        sval = cell.getCellFormula();

                        if (sval.contains("SUM")) {

                            if (listSum == null) {

                                listSum = new ArrayList<>();
                            }

                            listSum.add(cell);


                        }


                    }

                    if (type == Cell.CELL_TYPE_ERROR) {

                        sval = cell.getStringCellValue();

                    }

                    if (type == Cell.CELL_TYPE_STRING) {

                        String svalue = cell.getStringCellValue();

                        if (svalue.startsWith("&COLOR")) {

                            hmColor.put(cell.getColumnIndex(), svalue);
                        }

                        if (svalue.startsWith("&FLAG")) {

                            hmFlags.put(cell.getColumnIndex(), svalue);
                        }

                        if (svalue.startsWith("&VALUE")) {

                            if (hmCollVal == null) {

                                hmCollVal = new HashMap<>();
                                hmValuesColl = new HashMap<>();

                            }
                            int poz = cell.getColumnIndex();
                            hmCollVal.put(poz, svalue);

                        }



                        if (svalue.contains("${")) {

                            String newValue = svalue;

                            String evalString;

                            String repValue;

                            while (newValue.contains("${")) {

                                int pozLast = newValue.lastIndexOf("$");

                                int pozFirst = newValue.lastIndexOf("}");

                                evalString = newValue.substring(pozLast + 2, pozFirst);
                                repValue = newValue.substring(pozLast, pozFirst + 1);
                                Object value;
                                try {
                                    value = script.runScript(evalString, hmCurParam);

                                    newValue = newValue.replace(repValue, value.toString());


                                } catch (Exception ex) {

                                    newValue = "ERROR";
                                    setLoggerInfo("", ex);
                                }


                            }

                            setValueInCell(newValue, cell);


                        }


                        if (svalue.startsWith("&JURNAL_UP")) {

                            Timestamp tFirst = new Timestamp(d_first.getMillis());
                            Timestamp tLast = new Timestamp(d_last.getMillis());


                            String sql = "SELECT * FROM jurnal_over WHERE  value_date > ? AND value_date < ? ORDER BY  value_date";
                            jurnal_up = true;

                            rsObject = SqlTask.getResultSet(null, sql, new Object[]{tFirst, tLast});

                            //отчет по журналам превышений


                        }


                        if (svalue.startsWith("&SINGLE")) {

                            //  расчет для каждого объекта на отдельной страницы
                            single = true;
                        }

                        if (svalue.startsWith("name&")) {

                            //  названия листов в многостраничном отчете
                            nameList = svalue.replaceAll("name&", "");


                        }


                        if (svalue.startsWith("&SELECT")) {

                            if (idSelect == null) {

                                Work.ShowError("Не выбран объект для формирования отчета !");
                                return result;
                            }

                            hmCurParam = Work.getParametersObject(idSelect, null, true, true, true);

                            //  расчет только для выделенного объекта
                            select = true;
                        }

                        if (svalue.startsWith("#")) {
                            // Проверяем не скрипт ли...

                            if (svalue.indexOf("(-1") != -1) {

                                if (!hmDinamik.containsKey(row)) {
                                    hmDinamik.put(row, sheet);
                                }

                                if (!select) {
                                    continue;
                                }
                            }

                            Object value;
                            try {
                                value = script.runScript(svalue, hmCurParam);

                            } catch (Exception ex) {

                                value = "ERROR";
                                setLoggerInfo("", ex);
                            }

                            setValueInCell(value, cell);
                        }
                    }
                }
            }

            //конец простого отчета

        }


        if (single) {

            executeSingleReport(wb);

        } else {

            if (!hmDinamik.isEmpty() && !select) {

                for (Row row : hmDinamik.keySet()) {
                    Sheet s = hmDinamik.get(row);

                    executeDinamicReport(wb, row, s);

                }

            }
        }
// Пересчитываем ячейки и проверяем уставки ...
        HashMap<String, Object> hmf = recalcDinReport(hmColor, hmFlags);

        if (hmf != null && !hmf.isEmpty()) {
            result.putAll(hmf);
        }

// Вытаскиваем значения именованых ячеек   
        hmf = getValueByName(wb);

        if (hmf != null && !hmf.isEmpty()) {
            result.putAll(hmf);
        }

        return result;

    }

    public void setDataFromZip() {

        File f = XmlTask.openFile("zip", "Выбор архива обновлений", null);

        File fileXsl;

        DateTime dateTime1 = null;
        DateTime dateTime2 = null;

        DateTime dateCurr1 = null;
        DateTime dateCurr2;

        ArrayList<HashMap<String, Object>> alValues = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> hmValues = null;

        int pozVal1;
        int pozVal2;

        int id1 = 131;
        int id2 = 130;

        StatementEx statementEx = null;

        String cap1 = "Ввод1";
        String cap2 = "Ввод2";
        try {

            ZipFile zf = new ZipFile(f);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();

                fileXsl = File.createTempFile("xls", ".xls");

                FileOutputStream fos = new FileOutputStream(fileXsl);

                // Workbook workbook=WorkbookFactory.create(fos);
                write((int) ze.getSize(), zf.getInputStream(ze), fos);

                pozVal1 = -1;
                pozVal2 = 0;

                //wb=new XSSFWorkbook(fileXsl.getAbsolutePath());
                wb = WorkbookFactory.create(zf.getInputStream(ze));

                DateTime dateTime = new DateTime();

                String sval;
                Sheet sheet = null;

                String sValue = "";
                Double dValue1 = null;
                Double dValue2 = null;

                // String sDate = null;
                for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
                    sheet = wb.getSheetAt(sheetNum);

                    for (Row row : sheet) {

                        for (Cell cell : row) {
                            sValue = "";

                            int c = cell.getColumnIndex();
                            int r = cell.getRowIndex();
                            int type = cell.getCellType();

                            if (type == Cell.CELL_TYPE_STRING) {

                                sValue = cell.getStringCellValue();

                                if (c == 2 && r == 1) {
                                    // sDate = sValue;
                                    dateTime1 = new DateTime();

                                    sValue = sValue.replace(".", ";");

                                    String[] sdate = sValue.split(";");
                                    dateTime1 = dateTime1.millisOfDay().setCopy(0);

                                    sdate[0] = (sdate[0].startsWith("0") ? sdate[0].replace("0", "") : sdate[0]);
                                    sdate[1] = (sdate[1].startsWith("0") ? sdate[1].replace("0", "") : sdate[1]);

                                    dateTime1 = dateTime1.monthOfYear().setCopy(sdate[1]);
                                    dateTime1 = dateTime1.dayOfMonth().setCopy(sdate[0]);
                                    dateTime2 = new DateTime(dateTime1);

                                }

                                System.out.print(sValue);

                            }

                            //   if(type==Cell.CELL_TYPE_NUMERIC){
                            //  dValue1=cell.getNumericCellValue();
                            // }    
                            if (sValue.contains(":") && sValue.contains("-")) {

                                pozVal1 = pozVal1 + 2;
                                pozVal2 = pozVal2 + 2;

                                System.out.print(sValue);

                                Cell cellVal1 = row.getCell(cell.getColumnIndex() + 1);
                                Cell cellVal2 = row.getCell(cell.getColumnIndex() + 2);

                                type = cellVal1.getCellType();

                                if (type == Cell.CELL_TYPE_STRING) {

                                    dValue1 = Double.parseDouble(cellVal1.getStringCellValue());
                                    dValue2 = Double.parseDouble(cellVal2.getStringCellValue());

                                } else if (type == Cell.CELL_TYPE_NUMERIC) {

                                    dValue1 = cellVal1.getNumericCellValue();
                                    dValue2 = cellVal2.getNumericCellValue();
                                }

                                dateCurr1 = dateTime1.plusMinutes(pozVal1 * 30);
                                dateCurr2 = dateTime2.plusMinutes(pozVal2 * 30);

                                System.out.println(dateCurr1.toString());
                                System.out.println(dateCurr1.toString());

                                double d11 = dValue1 / 2;
                                double d22 = dValue2 / 2;

                                hmValues = getValuesMap(id1, cap1, dateCurr1, d11);

                                alValues.add(hmValues);
                                hmValues = getValuesMap(id1, cap1, dateCurr2, d11);
                                alValues.add(hmValues);

                                hmValues = getValuesMap(id2, cap2, dateCurr1, d22);
                                alValues.add(hmValues);

                                hmValues = getValuesMap(id2, cap2, dateCurr2, d22);
                                alValues.add(hmValues);

                            }

                        }
                    }
                }

                statementEx = new StatementEx(null, "profil_power", null);

                for (HashMap<String, Object> hmVal : alValues) {

                    statementEx.replaceRecInTable(hmVal, true);
                }

                //  statementEx.close();
                // hmZip.put(ze.getName(), file);
                // htSizes.put(ze.getName(), ze.getSize());
                // workbook = WorkbookFactory.create(fileXsl);
            }

            statementEx.close();

            zf.close();

            //    if (!tsTables.contains("update_tbl")) {
            //createTableUpdate(null);
            //  }
            //  runAnt(hmZip.get("build.xml"));
        } catch (Exception ex) {
            MainWorker.deffLoger.error(ex);

        }

    }

    private HashMap<String, Object> getValuesMap(int id, String caption, DateTime dateTime, Double dVal) {
        HashMap<String, Object> hmValue = new HashMap<String, Object>();

        hmValue.put("Id_object", id);

        Timestamp timestamp = new Timestamp(dateTime.getMillis());

        hmValue.put("value_date", timestamp);
        hmValue.put("object_caption", caption);
        hmValue.put("modify_date", timestamp);
        hmValue.put("flag0", 0);
        hmValue.put("flag1", 0);
        hmValue.put("is_check", 0);
        hmValue.put("tangens_f", 0);
        hmValue.put("power_pa", dVal);
        hmValue.put("power_pr", 0);
        hmValue.put("power_qa", 0);
        hmValue.put("power_qr", 0);

        return hmValue;

    }

    //bAvto-в автоматическом режиме
    public void createReport() {

        // Делаем статические вычисления (Для конкретных объектов)
        // Динамические отчеты
        //     HashMap<HSSFRow,HSSFSheet> hmDinamik=new HashMap<HSSFRow, HSSFSheet>();
        hmParamCol = new HashMap<String, Object[]>();

        Sheet sheet = null;
        String pathfile = "";
        //   HSSFWorkbook wb = null;
        String sval = "";
        String[] v = null;

        int obj = 0;
        int par = 0;

        //   pathfile = report.getNameReport();
//objectInputStream = new java.io.ObjectInputStream(fileInputStream); rs = (ResultSet) objectInputStream.readObject();
//file_report
        //wb =wb;
        // HSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        // HSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        //    SimpleDateFormat formatter2 = new SimpleDateFormat("dd.MM.yyyy");
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy");
// String str = fmt.print(dt);

        //   HSSFSheet sheet = wb.getSheetAt(1);
        //   int count = wb.getNumberOfSheets();
        //  prbProcess.setMinimum(0);
        //  prbProcess.setString("Создаем отчет...");
        //   prbProcess.setStringPainted(true);
        //   prbProcess.setVisible(true);
        // Ищем дату начала отчета
        if (bAvto) {

            // Автоматический режим расчета
            Object DN = getValue(wb, "НачалоОтчета");

            if (DN != null && DN instanceof Double) {

                int days = 0;
                try {

                    //  Double ddd = Double.parseDouble(DN);
                    days = ((Double) DN).intValue();

                } catch (NumberFormatException e) {
                    setLoggerInfo("Создание отчета", e);
                }

                d_last = new DateTime();
                d_first = new DateTime().plusDays(days);

                d_last = d_last.millisOfDay().setCopy(0);
                d_first = d_first.millisOfDay().setCopy(0);

            } else {

                d_last = new DateTime();
                return;

            }

        }

        //  HSSFCell cell = wb.getSheetAt(idx).getRow(idx).cit.next();
        //      publish(new clsEvnMsg(-1, sres.toString(), MSG_ERROR, STR_NEW));
        // Name.getReference();
        // Name.setReference("'Данные'!$A$1:$A$11");
        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            sheet = wb.getSheetAt(sheetNum);

            if (stopCreateReport) {
                return;
            }

            // if (!bAvto) {
            setMinMaxValue(0, sheet.getLastRowNum());

            String rName = nameReport + "... Лист № " + (sheetNum + 1);
            refreshBarValue(rName);
            setNotifyObservers(rName);

            for (Row row : sheet) {

                refreshBarValue(row.getRowNum());

                if (stopCreateReport) {
                    return;
                }

                for (Cell cell : row) {

                    if (stopCreateReport) {
                        return;
                    }

                    int type = cell.getCellType();

                    if (type == Cell.CELL_TYPE_BLANK) {
                    }

                    if (type == Cell.CELL_TYPE_FORMULA) {

                        sval = cell.getCellFormula();

                    }

                    if (type == Cell.CELL_TYPE_ERROR) {

                        sval = cell.getRichStringCellValue().getString();

                    }

                    if (type == Cell.CELL_TYPE_STRING) {

                        String svalue = cell.getStringCellValue();

                        if (svalue.startsWith("#")) {
                            // Проверяем не скрипт ли...
                        }

                        // Период  дат отчета отчета
                        if (svalue.startsWith("#DBETWEEN")) {

                            CellStyle style = cell.getCellStyle();

                            Duration duration = new Duration(d_first.getMillis(), d_last.getMillis());

                            int countDays = (int) duration.getStandardSeconds();

                            DateTime dateTime = new DateTime(d_first);
                            sval = fmt.print(dateTime);
                            cell.setCellValue(sval);

                            int iCol = cell.getColumnIndex();
                            int width = cell.getSheet().getColumnWidth(iCol);

                            for (int iDay = 1; iDay <= countDays; iDay++) {
                                int iColNew = iDay + iCol;
                                cell.getSheet().setColumnWidth(iColNew, width);
                                DateTime dateTimeNew = new DateTime(d_first).plusDays(iDay);
                                Cell cDay = row.createCell(iColNew);
                                cDay.setCellStyle(style);

                                sval = fmt.print(dateTimeNew);
                                cDay.setCellValue(sval);

                            }

                        }

                        // Период  дат отчета отчета
                        if (svalue.startsWith("#PERIOD")) {

                            DialogSelectDate selectDate = new DialogSelectDate(null, true);
                            selectDate.setVisible(true);

                            d_first = new DateTime(selectDate.getDpFirst().getDate());
                            d_last = new DateTime(selectDate.getDpLast().getDate());

                        }

                        // insert 10 rows starting at row 9
                        //	sheet.shiftRows(8, sheet.getLastRowNum(), 10, true,
                        if (svalue.startsWith("#COL")) {

                            // Если нет запятой, то динамический
                            if (svalue.indexOf(",") == -1) {

                                if (!hmDinamik.containsKey(row)) {
                                    hmDinamik.put(row, sheet);
                                }
                                continue;

                            }

                        }

                        if (svalue.startsWith("#PAR")) {

                            // Параметры текущей записи
                            String sPar = Work.getDelimitedString(svalue, '(', ')');

                            if (mapProp != null) {

                                cell.setCellValue((String) mapProp.get(sPar.trim()));

                            }

                        }

                        if (svalue.startsWith("#TRE")) {

                            // Параметры текущего дерева запроса
                            String sPar = Work.getDelimitedString(svalue, '(', ')');

                            String[] sTree = sPar.split(",");

                            int iPoz = -1;
                            if (sTree.length == 2) {
                                sPar = sTree[0];

                                try {
                                    iPoz = Integer.parseInt(sTree[1]);

                                } catch (NumberFormatException e) {

                                    setLoggerInfo(sPar, e);
                                }

                            }

                            if (mapTree != null) {

                                Element element = mapTree.get(sPar);

                                String sV = element.getAttribute("value");

                                String[] ses = sV.split(";");

                                if (ses.length >= iPoz && iPoz > -1) {

                                    sV = ses[iPoz];
                                }

                                cell.setCellValue(sV);

                            }

                        }

                        if (svalue.indexOf("#DN") != -1) {

                            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");
                            int x = getParamByNumber(svalue, 0);

                            if (x != -1) {

                                //DateTime datetime = new DateTime(dateFirst).plusDays(x);
                                DateTime datetime = d_first.plusDays(x);

                                svalue = dtf.print(datetime);
                                cell.setCellValue(svalue);
                            } else {

                                // DateTime datetime = new DateTime(dateFirst);
                                svalue = dtf.print(d_first);
                                cell.setCellValue(svalue);

                            }

                        }

                        if (svalue.indexOf("#DK") != -1) {

                            //sval = formatter2.format(dateLast);
                            sval = fmt.print(d_last);

                            // svalue = svalue.replaceAll("#DK", sval);
                            cell.setCellValue(sval);

                        }

                        if (svalue.indexOf("#DMINUS") != -1) {

                            String sDay = svalue.substring(7, svalue.length());
                            int iDay = Integer.decode(sDay);

                            DateTime dateTime = new DateTime(d_last).minusDays(iDay);

                            sval = fmt.print(dateTime);
                            cell.setCellValue(sval);

                        }

                        //  Количество дней
                        if (svalue.indexOf("#DP") != -1) {

                            Days days = Days.daysBetween(d_first, d_last);

                            //     Days days = Days.daysBetween(new DateTime(dateFirst), new DateTime(dateLast));
                            int cd = days.getDays();

                            cell.setCellType(Cell.CELL_TYPE_BLANK);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(cd);

                        }

                        // Название параметра
                        if (svalue.startsWith("#NV")) {

                            par = getParametersId(svalue);

                            if (par == -1) {

                                cell.setCellValue("ERROR");

                                continue;
                            }

                            Parameter parameter;
                            String caption;

                            try {
                                parameter = new Parameter(null, par, false);
                                caption = parameter.getCaptionPar();

                                cell.setCellValue(caption);

                            } catch (SQLException ex) {

                                setNotifyObservers(ex);
                            }

                        }

                        if (svalue.startsWith("#TSN")) {

                            if (svalue.indexOf(",") == -1) {

                                if (!hmDinamik.containsKey(row)) {
                                    hmDinamik.put(row, sheet);
                                }
                                continue;
                            }

                            String pozs = Work.getDelimitedString(svalue, 'N', '(');

                            int pozt;

                            try {
                                pozt = Integer.parseInt(pozs);
                                DateTime dt = d_first.plusMinutes(pozt * 30);

                            } catch (NumberFormatException e) {

                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue("ERROR");

                            }

                        }

                        if (svalue.startsWith("#SN") || svalue.startsWith("#SK")
                                || svalue.startsWith("#VEND") || svalue.startsWith("#DEND")) {

                            // Если нет запятой, то динамический
                            if (svalue.indexOf(",") == -1) {

                                if (!hmDinamik.containsKey(row)) {
                                    hmDinamik.put(row, sheet);
                                }
                                continue;

                            }

                            DateTime d = null;

                            if (svalue.startsWith("#SN")) {
                                d = d_first;

                                int x = getParamByNumber(svalue, 2);

                                if (x != -1) {

                                    //  DateTime datetime = new DateTime(dateFirst).plusDays(x);
                                    DateTime datetime = d_first.plusDays(x);
                                    d = datetime;

                                }

                            }

                            if (svalue.startsWith("#SK")) {
                                d = d_last;

                            }

                            if (svalue.startsWith("#VEND")) {
                                d = null;

                            }

                            if (svalue.startsWith("#DEND")) {
                                d = null;

                            }

                            obj = getObjectId(svalue);
                            par = getParametersId(svalue);

                            if (obj == -1 || par == -1) {

                                cell.setCellValue("ERROR");

                                continue;
                            }

                            Object val = null;
                            Parameter parameter;
                            try {
                                parameter = new Parameter(obj, par, d, false);

                                if (svalue.startsWith("#DEND")) {

                                    val = parameter.getDateValue();

                                } else {

                                    val = parameter.getValuePar();

                                }

                            } catch (SQLException ex) {
                                setNotifyObservers(ex);
                            }

                            if (val == null) {

                                cell.setCellValue("N/D");

                            } else {
                                cell.setCellType(Cell.CELL_TYPE_BLANK);

                                if (val instanceof Double) {

                                    Double d1 = (Double) val;
                                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell.setCellValue(d1);

                                } else {

                                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");
                                    DateTime dt = (DateTime) val;

                                    cell.setCellType(Cell.CELL_TYPE_STRING);
                                    cell.setCellValue(dt.toString(dtf));

                                }

                            }
                        }
                    }
                }
            }

            if (!hmDinamik.isEmpty()) {

                for (Row row : hmDinamik.keySet()) {
                    Sheet s = hmDinamik.get(row);

                    createDinamicReport(wb, row, s);

                }

            }

// Пересчитываем ячейки...
            recalcDinReport(null, null);

            // Проверяем контролируемые объекты
            checkValueObject(wb);

        }

        //Показываем расчет
        if (!bAvto) {

            setNotifyObservers(true);
            try {
                showReport(wb, this.ext_report);
            } catch (Exception ex) {

                setLoggerInfo("Создание отчета", ex);
                setNotifyObservers(ex);
            }

        } else {

            // Проверяем, нужно ли отправлять файл пользователю
            createTmpReport(wb);
        }

    }

    private void checkValueObject(Workbook wb) {

        if (alCompare == null) {
            return;
        }

        ArrayList<CompareClass> alOver = new ArrayList<CompareClass>();

        for (CompareClass compare : alCompare) {
            String name = compare.getName();

            Object value = getValue(wb, name);

            if (value instanceof Double) {

                compare.setValue((Double) value);

                Boolean bc = compare.isCompare();

                if (bc != null && bc) {

                    compare.setTs(new Timestamp(d_last.getMillis()));

                    alOver.add(compare);
                }

            } else // Ошибка или нет данных
            {

                String oname = compare.getName();
                compare.setName(oname + "-Ошибка или нет данных !");

                compare.setValue((Double) 0.0);

                alOver.add(compare);
                compare.setTs(new Timestamp(d_last.getMillis()));

            }

        }

        if (bAvto) {

            Document doc = hmUsers.get(idUser);
            addEventsInDoc(doc, alOver, notise, nameReport, idObject);
        }

    }

    public void addEventsInDoc(Document doc, ArrayList<CompareClass> alOver, Byte notise, String nameReport, int idReport) {

        Element el;

        for (CompareClass over_obj : alOver) {

            el = doc.createElement("events");

            // Объект
            // id
            el.setAttribute("id_object", "" + idReport);
            el.setAttribute("notise", notise.toString());

            // Название
            el.setAttribute("object_name", nameReport);

            Timestamp ts = over_obj.getTs();
            SimpleDateFormat formatter2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String dateValue = formatter2.format(ts);

            el.setAttribute("c_value_date", dateValue);
            el.setAttribute("c_partype_name", over_obj.getName());
            el.setAttribute("value", over_obj.getValue().toString());
            String znak = over_obj.getName() + over_obj.getZnak() + "" + over_obj.getValueOver();
            el.setAttribute("over_value", znak);
            doc.getDocumentElement().appendChild(el);

        }

    }

    private void setFlagsCell(Cell cell, int idx, HashMap<String, Object> result, HashMap<Integer, String> flags) throws Exception {

        Double val = cell.getNumericCellValue();
        String scomp = flags.get(idx);
        scomp = MathTrans.getBetweenVal(scomp, '(', ')');

        String[] ustavki = scomp.split(";");

        for (String sval : ustavki) {

            String[] comp = sval.split("#");

            boolean compare = MathTrans.compareValue(val, comp[0]);

            if (compare) {

                flags.remove(idx);
                result.put(comp[1], Boolean.TRUE);

            }

        }

    }

    private void setColorCell(Cell cell, String sColor) throws Exception {

        String[] ustavki = sColor.split(";");

        String[] param;

        //  if (cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
        //    return;
        // }
        Double val = cell.getNumericCellValue();

        for (String sval : ustavki) {

            String[] comp = sval.split("#");

            boolean compare = MathTrans.compareValue(val, comp[0]);

            CellStyle styleOld = cell.getCellStyle();
            // short color = style.getFillForegroundColor();
            // short pattern = style.getFillPattern();
            if (compare) {

                CellStyle style = wb.createCellStyle();
                style.cloneStyleFrom(styleOld);

                Short short1 = Short.parseShort(comp[1]);
                //short s = HSSFColor.LIGHT_GREEN.index;
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                style.setFillForegroundColor(short1);
                cell.setCellStyle(style);
                break;
            }

        }

    }

    private HashMap<String, Object> recalcDinReport(HashMap<Integer, String> hmColor, HashMap<Integer, String> hmFlags) {

// Прерсчет всех ячеек
        HashMap<String, Object> result = new HashMap<String, Object>();
        String comp;
        //Re-calculating all formulas in a Workbook

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

//            FormulaEvaluator evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = wb.getSheetAt(sheetNum);

            for (Row row : sheet) {

                for (Cell cell : row) {

                    int typ = cell.getCellType();

                    if (typ == Cell.CELL_TYPE_FORMULA) {

                        evaluator.evaluateFormulaCell(cell);

                        //   CellValue value= evaluator.evaluate(cell);
                        //  int typ= value.getCellType();
                        //if (typ==Cell.CELL_TYPE_NUMERIC){
                        // }
                    } else if (typ == Cell.CELL_TYPE_NUMERIC) {
                    } else {

                        continue;
                    }

                    int idx = cell.getColumnIndex();

                    if (hmFlags != null && hmFlags.containsKey(idx)) {

                        try {
                            setFlagsCell(cell, idx, result, hmFlags);
                        } catch (Exception ex) {
                            cell.setCellValue(ex.getMessage());
                        }

                    }

                    if (hmColor != null && hmColor.containsKey(idx)) {

                        if (hmColor != null && hmColor.containsKey(idx)) {

                            comp = hmColor.get(idx);

                            comp = MathTrans.getBetweenVal(comp, '(', ')');

                            try {

                                setColorCell(cell, comp);

                            } catch (Exception e) {
                                cell.setCellValue(e.getMessage());
                            }

                        }

                    }
                }
            }
        }

        return result;

    }
    // Создаем отчет для каждого пользователя

    private void createReportBySchedule(ResultSet rs) {
        try {
            rsObject = null;
            ResultSet rsReport = null;
            Document docUser;
            idUser = null; //Пользователь
            idObject = null; // Объект контроля(Отчет)
            prObject = null; // параметры контроля
            notise = null; // Куда посылать превышения
            wb = null;
            nameReport = null;
            String obj;
            hmUsers.clear();
            rs.beforeFirst();
            // Проходим всех пользователей
            while (rs.next()) {
                idUser = rs.getInt("id_parent");
                idObject = rs.getInt("id_object");
                prObject = rs.getString("pr_object");
                notise = rs.getByte("notise");

                goReportForUser();

            }
        } catch (Exception ex) {
            setLoggerInfo("Создание отчета", ex);
        }
    }

    @Override
    public void doProcess() {
        try {

            setFileCurrent(null);

            if (currentTask != null && currentTask.equals(TASK_WEB_REPORT)) {

                String nameReport = (String) mapProperties.get("nameReport");
                Integer idSelect = (Integer) mapProperties.get("idSelect");
                String dateFirst = (String) mapProperties.get("dateFirst");
                String dateLast = (String) mapProperties.get("dateLast");
                String sqlReport = (String) mapProperties.get("sqlReport");
                createWebReport(nameReport, idSelect, dateFirst, dateLast, sqlReport);

            } else {

                if (bAvto) {

                    // По расписанию
                    newProcess(nameSchedule);
                    setNotifyObservers(nameSchedule);

                    hmUsers.clear();
                    // Производим расчеты для всех пользователей
                    createReportBySchedule(rsUsers);
                    // Посылаем сообщения
                    sendOver();
                } else {
                    // в ручном режиме

                    newProcess("Создание отчета...");

                    showReport();

                    //prepareReport();
                    // createReport();
                }
            }
        } catch (Exception ex) {
            answerProcess("ошибка.", ProcLogs.MSG_ERROR);

            setLoggerInfo("Отчет Excel", ex);
            answerProcess("Ошибка!", ProcLogs.MSG_ERROR);
            return;
        }
        answerProcess("успешно.", ProcLogs.MSG_OK);

    }

    private void showReport() throws Exception {

        HashMap<String, Object> hmReport = getReport(nameReport.trim(), sqlCurrent);

        if (hmReport == null) {
            return;
        }


        File file = (File) hmReport.get("#file");

        RunExcel(file.getAbsolutePath());

    }

    //Подготовка данных для ручного отчета
    private void prepareReport() throws Exception {

        String sql = "SELECT * FROM object4 WHERE name_report=?";

        ResultSet rs;
        rs = SqlTask.getResultSet(null, sql, new Object[]{nameReport.trim()});

        try {

            while (rs.next()) {

                byte[] bs = rs.getBytes("file_report");
                this.ext_report = rs.getString("ext_report");
                InputStream is = new ByteArrayInputStream(bs);
                //wb = WorkbookFactory.create(rs.getBinaryStream("file_report"));
                wb = WorkbookFactory.create(is);

                is.close();
            }

            nameReport = nameReport.replaceAll(" ", "_");

            this.d_last = d_last.millisOfDay().setCopy(0);
            this.d_first = d_first.millisOfDay().setCopy(0);

        } finally {

            rs.close();
        }

    }

    // Ищем события , у которых есть активное расписание и тип контроля расчет или  расчет группы объектов
    private boolean FindEvents() throws Exception {

        String sql = "SELECT * FROM report WHERE  shedule=" + idSchedule;

        rsUsers = SqlTask.getResultSet(null, sql);

        if (rsUsers.next()) {

            return true;

        } else {
            rsUsers.getStatement().close();
            return false;
        }

    }

    /**
     * посылка превышений по каждому пользователю
     */
    public void sendOver() throws Exception {

        Document docUser;

        try {

            blinkText("Отправка уведомлений...");

            sleepProcess(50);

            Map<String, String> hmprop;

            for (Integer idU : hmUsers.keySet()) {

                docUser = hmUsers.get(idU);

                hmprop = XmlTask.getMapAttrubuteByName(docUser.getDocumentElement(),
                        "name", "value", "column");
                String mailUser = hmprop.get("email");
                // Имя пользователя
                String user = hmprop.get("name_user");

                setNotifyObservers("Отчеты:отправка уведомлений(" + user + ", " + mailUser + ")");

                try {
                    // OverJurnal.addOver(docUser, alErrors);
                    // MailClass.sendMail(docUser, alErrors);

                } catch (Exception ex) {
                    setLoggerInfo("Создание отчета", ex);
                }

            }

        } finally {
            stopBlinkText();
        }

    }

    private void createTmpReport(Workbook wb) {

        try {

            if (!BitSetEx.isBitSet(notise, (byte) 1)) {
                return;
            }

            //  String dir = System.getProperty("java.io.tmpdir");
            File file = File.createTempFile("report", ".xls");
            // File file = new File(dir, nameRep + ".xls");
            ExcelReport.saveWorkbook(wb, file);

            Document docUser = hmUsers.get(idUser);

            Element element = docUser.createElement("report");
            element.setAttribute("path", file.getAbsolutePath());

            docUser.getDocumentElement().appendChild(element);

        } catch (Exception ex) {

            setNotifyObservers(ex);
        }

    }
}

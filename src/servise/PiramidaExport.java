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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xmldom.XmlTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.esStatus;
import static constatant_static.SettingActions.esStatus.*;

/**
 * @author 1
 */
public class PiramidaExport extends MainWorker {

    private int idSchedule;// Расписание сбора Оперативно-комерческих данных
    private String mail_addres; // Электронный адрес
    //в  виде ZIP файла
    private Boolean bZip;
    private Boolean isMail; // Отправлять по электронке
    private String folderFile;//Папка для хранения файлов
    private static int count_file = 1; // номер файла
    private static int ROW_LIMIT = 6000; // Максимальное количество обрабатываемых строк
    private Document document;
    private HashMap<Integer, HashMap<String, Object>> hmParameters; // Параметры объектов
    private HashMap<Integer, HashMap<Integer, ArrayList<String>>> hmCodes;
    private HashMap<Integer, String> hmCodesPiramida;
    private HashMap<ResultSet, String> hmWorking;  // список обрабатываемых таблиц
    ArrayList<String> alTables;
    private HashMap<String, Element> hmElements;
    private String nameSchedule;// Имя расписания
    private HashMap<String, Object> hmProp; // параметры
    private Savepoint savepoint;

    public PiramidaExport(ExecutorService pool) {

        hmWorking = new HashMap<ResultSet, String>();
        this.pool = pool;
        hmParameters = new HashMap<Integer, HashMap<String, Object>>();
        hmElements = new HashMap<String, Element>();

        alTables = new ArrayList<String>();
        bZip = true;
        setLogger(org.apache.log4j.Logger.getLogger("LogPiramida"));
        setLoggerInfo("Модуль PIRAMIDA подключен.", null);
    }

    private void createSendFile(ResultSet rs, HashMap<String, Object> hmColNames, Element elMeasuringData) throws Exception {

        String kodPiramida;

        //TreeSet<String> set = new TreeSet<String>();
        HashMap<String, Element> hmMeasuring = new HashMap<String, Element>();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        HashMap<String, Object> hmCurParam = null;

        rs.last();
        int countRow = rs.getRow();
        String nameTable = rs.getMetaData().getTableName(1).toLowerCase();

        ArrayList<String> alNamesCol = SqlTask.getNamesCol(null, nameTable);

        String caption = (String) Work.getParamTableByName(nameTable, Work.TABLE_CAPTION);
        rs.beforeFirst();
        setMinMaxValue(0, countRow);
        refreshBarValue(caption + "(" + countRow + ")");
        // Вытаскивае кфц для  комерческого протокола  (первый бит=1)
        HashMap<String, String> hmKfc = Parameter.getKfcByBytSet(1);

        while (rs.next()) {

            countRow = rs.getRow();

            refreshBarValue(countRow);

            int idObj = rs.getInt("Id_object");

            Integer iTarif = -1;

            // Для энергиии  только по сумме
            if (alNamesCol.contains("tarif")) {

                iTarif = rs.getInt("tarif");

                //  if (iTarif != 0) {
                //    continue;
                // }
            }

            // if (hmCodes.containsKey(idObj)) {
            // setLoggerInfo("ID-" +idObj, null);
            //   hmCod = hmCodes.get(idObj);
            if (hmParameters.containsKey(idObj)) {
                hmCurParam = hmParameters.get(idObj);
            } else {
                try {
                    hmCurParam = Work.getParametersRow(idObj, null, "objects", true, false);
                    hmParameters.put(idObj, hmCurParam);
                } catch (SQLException ex) {
                    setLoggerInfo("Объект " + idObj + "-нет текущих параметров!", ex);
                    continue;
                }
            }

            kodPiramida = (String) hmCurParam.get("kod_piramida");

            if (kodPiramida == null || kodPiramida.isEmpty()) {

                continue;

            }

            Timestamp timestamp = rs.getTimestamp("value_date");
            DateTime dateTime = new DateTime(timestamp);

            /**
             * int min = dateTime.getMinuteOfHour(); int hour =
             * dateTime.getHourOfDay(); int season = rs.getInt("season");
             *
             * if (((hour == 1 && min == 30) || (hour == 2 && min == 0)) &&
             * season != 13) { par112 = true;
             *
             * }
             *
             * if (((hour == 1 && min == 30) || (hour == 2 && min == 0)) &&
             * season == 13) { par113 = true;
             *
             * }
             *
             */
            int Month = dateTime.getMonthOfYear();
            // Вытаскивае кфц для  комерческого протокола
            String curKfc = null;
            String sCod = ""; //Канал пирамиды
            Double d = null;

            String kodAll;

            for (String nCol : hmColNames.keySet()) {
                try {
                    sCod = (String) hmColNames.get(nCol);

                    kodAll = getCodPiramid(kodPiramida, sCod);
                    //   String allName = "ID-" + idObj + " KOD-" + kodAll + " COL-" + nCol;
                    String[] coltab = nCol.split("#");
                    // имя столбца
                    nCol = coltab[0];
                    //тариф
                    String valTarif = coltab[1];

                    // для данных с тарифом
                    if (iTarif != -1 && !iTarif.toString().equals(valTarif)) {
                        continue;
                    }
                    d = rs.getDouble(nCol);
                    curKfc = hmKfc.get(nCol);
                    d = Parameter.getValueKfc(idObj, nCol, d, hmCurParam, curKfc);
                } catch (Exception ex) {
                    setLoggerInfo("", ex);
                    setLoggerInfo(nCol + ":об." + idObj + "-нет текущих коэф-ов!", null);
                    continue;
                }

                String sCode = null;

                String[] sk = sCod.split(";");

                sCode = sk[1].trim();

                Element element = null;
                Element elMeasuring = null;

                if (hmElements.containsKey(kodAll)) {
                    element = hmElements.get(kodAll);
                } else {
                    element = document.createElement("element");
                    elMeasuringData.appendChild(element);
                    element.setAttribute("code", kodAll);
                    hmElements.put(kodAll, element);
                }

                //Элементы  measuring
                hmMeasuring.clear();

                NodeList list = element.getChildNodes();

                for (int i = 0; i < list.getLength(); i++) {

                    Element e = (Element) list.item(i);
                    String mesKos = e.getAttribute("code");
                    hmMeasuring.put(mesKos, e);

                }

                //    if(hmMeasuring.size()>1){
                //  setLoggerInfo(nCol, null);
                // }
                if (hmMeasuring.containsKey(sCode)) {
                    elMeasuring = hmMeasuring.get(sCode);
                } else {
                    elMeasuring = document.createElement("measuring");
                    elMeasuring.setAttribute("code", sCode);
                    element.appendChild(elMeasuring);
                    //       hmMeasuring.put(sCode, elMeasuring);
                }

                Element elValue = document.createElement("value");
                String time = dtf.print(timestamp.getTime());
                elValue.setAttribute("time", time);
                elValue.setAttribute("svalue", "");

                // Летнее время
                if (Month > 3 && Month < 10) {
                    elValue.setAttribute("season", "1");
                } else {
                    elValue.setAttribute("season", "0");
                }
                elValue.setAttribute("isvalue", "1");
                elValue.setAttribute("status", "1000000");
                elValue.setTextContent(String.valueOf(d));
                elMeasuring.appendChild(elValue);

                //}
            }

            // устанавливаем флаг
            //    } else {
            // Нет кода для Объекта
            // String caption =Work.getCaptionByIdObject(idObj);
            // setNotifyObservers("Для "+caption+"("+idObj+") нет кода ИИС Пирамиды !");
            //  }
        }
    }

    private String getCodPiramid(String kod, String sKanal) {

        String dopKod;
        Integer iKanal = null;
        kod = kod.replace('.', ';');

        String[] kods = kod.split(";");

        if (kods.length != 2) {
            return null;
        }

        String kPir = kods[0];
        String kObj = kods[1].trim();

        int ikObj = Integer.parseInt(kObj);

        String[] pars = sKanal.split(";");

        if (pars.length != 3) {
            return null;
        }

        dopKod = pars[2];

        String sKl = pars[0].trim();

        iKanal = Integer.parseInt(sKl);

        if (dopKod.equals("0")) {

            ikObj = (ikObj - 1) * 4 + iKanal;

        } else if (dopKod.equals("98")) {

            ikObj = (ikObj - 1) + iKanal;

        } else {

            ikObj = (ikObj * 255 + ikObj) + iKanal;

        }

        String kodPiram = "";

        kodPiram = kPir + "." + dopKod + "." + ikObj;

        return kodPiram;
    }

    /**
     * Создает карту кодов Пирамиды по объектам
     */
    private void createMapCodes() {

        try {
            Workbook wb = Piramida.OpenWb();
            Row rowCodesSilesta;
            Row rowCodesPiramida;
            HashMap<Integer, String> hmPozic = null;

            //prm_write
            HashMap<String, Object> hmValues = new HashMap<String, Object>();

            hmCodes = new HashMap<Integer, HashMap<Integer, ArrayList<String>>>();

            String sql = "";
            Name nameCode = wb.getName("kodSilesta");

            AreaReference ar = new AreaReference(nameCode.getRefersToFormula());
            CellReference cf = ar.getFirstCell();

            // Столбец кода силесты
            int colCod = cf.getCol();

            Name namePoints = wb.getName("ParamSilesta");

            ar = new AreaReference(namePoints.getRefersToFormula());
            cf = ar.getFirstCell();

            // Параметры силесты
            int rowParam = cf.getRow();

            int colParam = cf.getCol();

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            for (int sheetNum = 0; sheetNum
                    < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);

                rowCodesSilesta = sheet.getRow(rowParam);
                rowCodesPiramida = sheet.getRow(rowParam + 1);

                refreshBarValue("Создаем карту кодов 'Пирамиды'");

                setMinMaxValue(0, sheet.getLastRowNum());

                // Проходим строчки
                String kodController = "";

                for (Row row : sheet) {

                    int ir = row.getRowNum();

                    refreshBarValue(ir);

                    hmValues.clear();

                    // Колонка кодов  объектов
                    Cell cell = row.getCell(colCod);

                    Cell cellPir = row.getCell(colCod + 1);

                    String kodPiramida = "";

                    if (cellPir != null && cellPir.getCellType() == Cell.CELL_TYPE_STRING) {

                        kodController = cellPir.getStringCellValue();

                    }

                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK
                            || cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {

                        continue;

                    }

                    //  String sCod = cell.getStringCellValue();
                    try {
                        int iCod = (int) cell.getNumericCellValue();

                        int iColPir = -1;

                        if (cellPir != null && cellPir.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                            iColPir = (int) cellPir.getNumericCellValue();

                        }

                        kodPiramida = kodController + "." + iColPir;

                        if (iColPir != -1) {
                            hmValues.put("kod_piramida", kodPiramida);
                            //   try {
                            //     Work.updateRecInTable("objects", "WHERE c_tree_id=" + iCod, hmValues);
                            // } catch (Exception ex) {
                            //   setLoggerInfo("", ex);
                            // }
                        }

                        //hmCodes
                        HashMap<Integer, ArrayList<String>> hmColCod = new HashMap<Integer, ArrayList<String>>();

                        hmCodes.put(iCod, hmColCod);

                        // Проходим столбцы
                        int poz = 0;

                        for (int cc = colParam; cc
                                < row.getLastCellNum(); cc++) {

                            Cell cObj = row.getCell(cc);
                            Cell cSilesta = rowCodesSilesta.getCell(cc);
                            Cell cPiramida = rowCodesPiramida.getCell(cc);

                            if (cObj == null || cSilesta == null || cPiramida == null) {

                                continue;

                            }

                            String codObj;
                            String codSilesta;
                            String codPiramida;

                            int iSilesta = 0;

                            int iPiramida = 0;

                            if (cObj.getCellType() == Cell.CELL_TYPE_FORMULA) {

                                CellValue cv = evaluator.evaluate(cObj);

                                codObj = cv.getStringValue();

                            } else if (cObj.getCellType() == Cell.CELL_TYPE_STRING) {

                                codObj = cObj.getStringCellValue();

                            } else {
                                continue;

                            }

                            try {

                                if (cSilesta.getCellType() == Cell.CELL_TYPE_STRING) {

                                    codSilesta = cSilesta.getStringCellValue();

                                } else if (cSilesta.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                                    iSilesta = (int) cSilesta.getNumericCellValue();

                                } else {
                                    continue;

                                }

                                if (cPiramida.getCellType() == Cell.CELL_TYPE_STRING) {

                                    codPiramida = cPiramida.getStringCellValue();

                                } else if (cPiramida.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                                    iPiramida = (int) cPiramida.getNumericCellValue();

                                } else {
                                    continue;

                                }

                                if (iSilesta == 29) {

                                    iPiramida = 12;

                                } else if (iSilesta == 8) {
                                    iPiramida = 101;

                                } else {

                                    iPiramida = 1100;

                                }

                                ArrayList<String> alKodes;
                                String nameCol;
                                String sKode;
                                if (hmColCod.containsKey(iSilesta)) {

                                    poz++;
                                    alKodes = hmColCod.get(iSilesta);

                                    nameCol = hmPozic.get(poz);
                                    sKode = nameCol + "@@" + codObj + "@@" + iPiramida;
                                    alKodes.add(sKode);
                                    hmColCod.put(iSilesta, alKodes);

                                } else {

                                    poz = 0;
                                    alKodes = new ArrayList<String>();
                                    try {
                                        hmPozic = Work.getMapMamesByIdParam(iSilesta);
                                    } catch (SQLException ex) {
                                        setLoggerInfo("Позиция", ex);
                                    }

                                    nameCol = hmPozic.get(poz);
                                    sKode = nameCol + "@@" + codObj + "@@" + iPiramida;
                                    alKodes.add(sKode);
                                    hmColCod.put(iSilesta, alKodes);

                                }

                            } catch (NumberFormatException exception) {
                                continue;

                            }
                        }
                    } catch (NumberFormatException e) {
                    }

                }
            }
        } catch (IOException ex) {

            setNotifyObservers(ex);

        }
    }

    /**
     * Экспорт оперативно-комерческих данных в пирамиду
     */
    private void goMove() throws Exception {
        ResultSet rsValues = null;
        // Перебираем все таблицы с данными
        hmWorking.clear();

        hmElements.clear();
        if (hmCodes == null) {
            //createMapCodes();
        }
        document = XmlTask.getNewDocument();

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        DateTime dateTime = new DateTime();
        String sd = dateTime.toString(dtf);
        String nameFile = "PIRDATA_" + sd + "_" + count_file + ".xml";
        String path = null;

        // String dir = System.getProperty("user.dir");
        // File file = new File(dir, "piramida");
        // if (!file.exists() || !file.isDirectory()) {
        //   file.mkdir();
        // }
        File fileDir;
        File file;

        Element elBody = document.createElement("body");
        elBody.setAttribute("version", "0");
        elBody.setAttribute("timezone", "3");
        document.appendChild(elBody);
        Element elMeasuringData = document.createElement("measuringdata");
        elBody.appendChild(elMeasuringData);
        HashMap<String, Object> hmColNames;
        try {

            // Все таблицы с данными
            alTables = (ArrayList) Work.getAllNameTablesByTyp(15);
        } catch (SQLException ex) {
            setNotifyObservers(ex);
        }

        for (String nameTable : alTables) {

            if (MainWorker.isStop) {
                MainWorker.setLogInfo("Процесс экспорта данных  в пирамиду остановлен", null);
                return;
            }

            hmColNames = Work.getNameColValueByNameTable(nameTable, 1, "kod_piramida");

            if (hmColNames.isEmpty()) {
                continue;
            }

            //  String sqlUp = "UPDATE " + nameTable + " SET flag0=?  WHERE  flag0=? LIMIT " + ROW_LIMIT;
            //   int upCount = SqlTask.executeUpdateSQL(null, sqlUp, new Object[]{2, 0}, null);
            String caption = (String) Work.getParamTableByName(nameTable, Work.TABLE_CAPTION);
            // Запрашиваем экспортируемые

            // ТОЛЬКО для базы  MySQL !!!
            // Устанавливаем в 0 флаги 2(чтобы не накапливались)
            String sql = "UPDATE " + nameTable + " SET flag0=0  WHERE flag0=? ";

            SqlTask.executeUpdateSQL(null, sql, new Object[]{2}, null);

            int typBase = SqlTask.getTypeBase(null);


            /**
             * Устанавливаем флаг равным 2 для ROW_LIMIT записей;
             */
            if (typBase == 0) {
                sql = "UPDATE " + nameTable + " SET flag0=2  WHERE flag0=? LIMIT " + ROW_LIMIT;
            } else {
                sql = "UPDATE " + nameTable + " SET flag0=2  WHERE flag0=? ";
            }
            // для Derby
            // SELECT NAME, SCORE FROM RESULTS ORDER BY SCORE DESC
            // > FETCH FIRST 3 ROWS ONLY;


            SqlTask.executeUpdateSQL(null, sql, new Object[]{0}, null);

            sql = "SELECT * FROM " + nameTable + " WHERE flag0=?";

            blinkText("Запрос таблицы '" + caption + "'...");

            // Запрашиваем данные c флагом=2
            rsValues = SqlTask.getResultSet(null, sql, new Object[]{2});

            try {
                try {
                    stopBlinkText();

                    hmWorking.put(rsValues, caption);

                    setLoggerInfo(caption + "...", null);

                    // обрабатываем...
                    createSendFile(rsValues, hmColNames, elMeasuringData);

                } finally {

                    rsValues.close();

                }

            } catch (SQLException ex) {
                setLoggerInfo(caption + "...", ex);
            }

        }

        if (document.getElementsByTagName("value").getLength() == 0) {

            setLoggerInfo("Нет данных для отправки.", null);

            return;
        }

        count_file++;

        try {

            // отправляем...
            blinkText("Отправка файла на адрес: " + mail_addres);

            String nameZip = count_file + "_piramida.zip";

            ByteArrayOutputStream byteStream = MailClass.getByteArrayOutputStream(document);

            if (isMail) {

                String dir = System.getProperty("user.dir");
                fileDir = new File(dir, "piramida");
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }

                file = new File(fileDir, nameFile);

            } else {

                file = new File(folderFile, nameFile);
            }

            if (bZip) {

                file = MailClass.createZipFile(file, byteStream, nameZip);
            } else {

                file = MailClass.createFile(file, byteStream);

            }
            byteStream.close();

            // MailClass.goMail(mail_addres, "Экспорт в пирамиду", "", file, null, alErrors);
            if (isMail) {
                MailClass.goMailApathe(mail_addres, "Экспорт в пирамиду", "Инспектор", file, null);
            }

            setLoggerInfo("файл '" + nameZip + "' на адрес " + mail_addres + " отправлен.", null);

            stopBlinkText();
            updateMovedFlag();

        } finally {

            stopBlinkText();

        }

    }

    private void updateMovedFlag() throws SQLException {

        String msg;

        for (String nameTable : alTables) {

            // Если все хорошо обновляем флаг отправки
            String sql = "UPDATE " + nameTable + " SET flag0=1  WHERE flag0=? ";

            msg = nameTable + ": Обновление флага передачи данных...";
            refreshBarValue(msg);
            setLoggerInfo(msg, null);

            SqlTask.executeUpdateSQL(null, sql, new Object[]{2}, null);
        }

    }

    private void setParameters() throws Exception {
        hmProp = Work.getParametersFromConst("piramida_export");

        if (hmProp.isEmpty()) {

            return;

        }

        Integer id_sched = (Integer) hmProp.get("shedule_piram");

        bZip = (Boolean) hmProp.get("zip_file");

        isMail = (Boolean) hmProp.get("is_mail");

        if (id_sched != null) {
            idSchedule = id_sched;

        }

        folderFile = (String) hmProp.get("folder_file");

        mail_addres = (String) hmProp.get("mail_piram");

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer && o instanceof ScheduleClass) {
            try {
                setParameters();
            } catch (Exception ex) {
                setLoggerInfo("", ex);
            }

            if ((idSchedule == (Integer) arg) && !isSetStatus(esPiramidaCommerc)) {

                ScheduleClass sc = (ScheduleClass) o;

                nameSchedule = sc.getNameSchedule();

                executeProcess();

            }
        } else if (arg instanceof Boolean && o instanceof ScheduleClass) {
            setLoggerInfo("Расписание  ПИРАМИДА остановлено", null);
            MainWorker.isStop = true;

        }
    }

    @Override
    public void doProcess() {

        errorString = null;

        currentTask = "Экспорт в ПО 'Пирамида 2000'";
        long l = Thread.currentThread().getId();

        try {
            try {

                setParameters();
                newProcess(currentTask);
                setLoggerInfo(currentTask, null);

                Thread.currentThread().setName("P.cmrc(" + l + ")");
                includeStatus(esStatus.esPiramidaCommerc);

                goMove();

            } finally {

                excludeStatus(esStatus.esPiramidaCommerc);
            }
        } catch (Exception e) {
            errorString = "Ошибка !";
            setLoggerInfo(currentTask, e);
        }

    }
}

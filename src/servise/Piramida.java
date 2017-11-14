package servise;

import connectdbf.SqlTask;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Экспорт данных в Пирамиду
 *
 * @author 1
 */
public class Piramida {

    private int idScheduleCommerc;// Расписание сбора Оперативно-комерческих данных
    private int idScheduleATC;   //Макет ОАО "АТС" XML 80020
    private String mail_addres; // Электронный адрес
    private static int count_file = 1; // номер файла
    private int count_80020 = 1; // номер файла протокола 80020
    private static int ROW_LIMIT = 6000; // Максимальное количество обрабатываемых строк
    private Document document;
    private HashMap<Integer, HashMap<String, Object>> hmParameters; // Параметры объектов
    private HashMap<Integer, HashMap<Integer, String>> hmCodes;
    private HashMap<Integer, String> hmCodesPiramida;
    private ArrayList<ResultSet> alWorking;  // список обрабатываемых параметров
    private ExecutorService pool; // пул потоков
    private String nameSchedule;// Имя расписания
    private HashMap<String, String> hmProp; // параметры
    private int iTyp;  //0-комерческие данные 1-протокол 80020

    public Piramida(ExecutorService pool) {

        this.pool = pool;
        hmParameters = new HashMap<Integer, HashMap<String, Object>>();
        alWorking = new ArrayList<ResultSet>();
    }

    class GoCheck implements Runnable {

        @Override
        public void run() {
// Запускаем экспорт данных
            // insertCodes();
            // goMove();
        }
    }

    public static Workbook OpenWb() throws IOException {

        Workbook wb = null;

        FileInputStream fis = null;
        File file = xmldom.XmlTask.getFile("piramida");




        if (file.exists()) {

            try {

                fis = new FileInputStream(file);
                wb = new HSSFWorkbook(fis);


            } finally {
                fis.close();


            }

        }

        return wb;


    }

    private void insertCodes() {
        try {
            Workbook wb = OpenWb();
            ResultSet rsPoint = null;
            ResultSet rsObject = null;
            HashMap<String, Integer> hmNames = new HashMap<String, Integer>();

            String sql = "";
            Name nameCode = wb.getName("kodSilesta");


            AreaReference ar = new AreaReference(nameCode.getRefersToFormula());
            CellReference cf = ar.getFirstCell();


            int colCod = cf.getCol();




            Name namePoints = wb.getName("Points");

            ar = new AreaReference(namePoints.getRefersToFormula());
            cf = ar.getFirstCell();


            int colPoint = cf.getCol();






            for (int sheetNum = 0; sheetNum
                    < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);

                // Проходим строчки


                for (Row row : sheet) {

                    // Колонка названий объектов
                    Cell cell = row.getCell(colPoint);



                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

                        String nameObj = cell.getStringCellValue();



                        if (nameObj.indexOf("Контроллер") != -1) {

                            hmNames.clear();
                            String namePoint = nameObj.substring(10);

                            sql = "SELECT c_tree_id FROM points  WHERE name_point LIKE '%" + namePoint.trim() + "%'";




                            try {
                                rsPoint = SqlTask.getResultSet(null, sql);



                                if (rsPoint.next()) {
                                    int idx = rsPoint.getInt("c_tree_id");
                                    sql = "SELECT * FROM objects  WHERE sub_type4=" + idx;



                                    try {
                                        rsObject = SqlTask.getResultSet(null, sql);



                                        while (rsObject.next()) {

                                            int id = rsObject.getInt("c_tree_id");
                                            nameObj = rsObject.getString("name1");

                                            hmNames.put(nameObj, id);



                                        }

                                    } finally {
                                        rsObject.close();


                                    }

                                }
                            } finally {
                                rsPoint.close();


                            }
                        }

                        if (hmNames.containsKey(nameObj)) {

                            int idx = hmNames.get(nameObj);

                            cell = row.getCell(colCod);

                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(idx);




                        }




                    }


                }

            }


// Записываем изменения

            //  ExcelClass.saveWorkbook(wb, XmlParsing.getFile("piramida"));


        } catch (SQLException ex) {
        } catch (IOException ex) {
        }
    }

    /**
     * Создает карту кодов Пирамиды по объектам для протокола 80020 Код
     * котроллера должен быть тип СТРОКА а код точки учета тип ЧИСЛО коды
     * Силеста должны быть типом ЧИСЛО
     */
    private void createPiramidaCodes(ProcessInterface pi) {


        try {
            Workbook wb = OpenWb();

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


                pi.refreshBarValue("Создаем карту кодов 'Пирамиды'");


                pi.setMinMaxValue(0, sheet.getLastRowNum());


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

                    pi.refreshBarValue(ir);



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
        }
    }

    /**
     * Создает карту кодов Пирамиды по объектам
     */
    private void createMapCodes(ProcessInterface pi) {


        try {
            Workbook wb = OpenWb();
            Row rowCodesSilesta;
            Row rowCodesPiramida;

            hmCodes = new HashMap<Integer, HashMap<Integer, String>>();

            String sql = "";
            Name nameCode = wb.getName("kodSilesta");


            AreaReference ar = new AreaReference(nameCode.getRefersToFormula());
            CellReference cf = ar.getFirstCell();


            int colCod = cf.getCol();




            Name namePoints = wb.getName("ParamSilesta");

            ar = new AreaReference(namePoints.getRefersToFormula());
            cf = ar.getFirstCell();


            int rowParam = cf.getRow();


            int colParam = cf.getCol();


            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();






            for (int sheetNum = 0; sheetNum
                    < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);

                rowCodesSilesta = sheet.getRow(rowParam);
                rowCodesPiramida = sheet.getRow(rowParam + 1);



                pi.refreshBarValue("Создаем карту кодов 'Пирамиды'");


                pi.setMinMaxValue(0, sheet.getLastRowNum());

                // Проходим строчки


                for (Row row : sheet) {

                    int ir = row.getRowNum();

                    pi.refreshBarValue(ir);

                    //       try {
                    //         Thread.sleep(1);
                    //   } catch (InterruptedException ex) {

                    //     DbfClass.logger.error("", ex);
                    // }




                    // Колонка кодов  объектов
                    Cell cell = row.getCell(colCod);



                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK
                            || cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {

                        continue;


                    }


                    //  String sCod = cell.getStringCellValue();

                    try {
                        int iCod = (int) cell.getNumericCellValue();

                        //hmCodes

                        HashMap<Integer, String> hmColCod = new HashMap<Integer, String>();

                        hmCodes.put(iCod, hmColCod);

                        // Проходим столбцы



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



                                if (iSilesta == 29 || iSilesta == 33 || iSilesta == 37 || iSilesta == 41) {

                                    iPiramida = 12;


                                } else if (iSilesta == 8 || iSilesta == 9 || iSilesta == 10 || iSilesta == 11) {
                                    iPiramida = 101;


                                } else {

                                    iPiramida = 1100;


                                }



                                hmColCod.put(iSilesta, codObj + "@@" + iPiramida);





                            } catch (NumberFormatException exception) {
                                continue;


                            }
                        }
                    } catch (NumberFormatException e) {
                    }


                }
            }
        } catch (IOException ex) {
        }
    }
}

package servise;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import dbf.Work;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Класс проверки расчетов и отчетов
 *
 * @author 1
 */
public class Excel {

    private static Connection cnct;
    private static String excel_url;
    private HashMap<String, String> hmCaption;
    private String nameReport; // Название отчета
    private boolean stopCreateReport; // Остановка формирования отчета
    private ExecutorService pool; // пул потоков
    private javax.swing.Timer activityMonitor;

    public Excel(ExecutorService pool) {

        this.pool = pool;
        stopCreateReport = false;
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

    public static int getObjectId(ResultSet rsThis) {

        int result = -1;
        try {
            result = rsThis.getInt("c_tree_id");



        } catch (SQLException ex) {

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
                    } else {
                        id = rsThis.getInt("Id_object");
                    }

                    hmValues = Work.getParametersRow(id, null, "object2", true, true);

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

    public static void registerReport(java.sql.Connection conn, String url) {

        //  http://poi.apache.org/spreadsheet/quick-guide.html

        HSSFWorkbook wb = new HSSFWorkbook();
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(url);
            try {
                wb.write(fileOut);

                HSSFSheet sheet = wb.createSheet("Данные ПО Inspector");

                fileOut.close();

            } catch (IOException ex) {
            }


        } catch (FileNotFoundException ex) {
            //Work.logger.error("", ex);
        }


    }

    public static void saveWorkbook(byte[] bs, File filename) throws Exception {


        FileOutputStream out = null;
        out = new FileOutputStream(filename);
        out.write(bs);
        out.close();

    }

    public static void saveWorkbook(Workbook wbs, File filename) throws Exception {

        //  String file = "E:/loan-calculator.xls";

        // file = newname;


        FileOutputStream out = null;
        out = new FileOutputStream(filename);
        wbs.write(out);
        out.close();

    }

    private static HashMap<String, HSSFCellStyle> createStyles(HSSFWorkbook wb) {



        Map<String, HSSFCellStyle> styles = new HashMap<String, HSSFCellStyle>();

        HSSFCellStyle style;
        style = wb.createCellStyle();

        HSSFFont titleFont = wb.createFont();
        titleFont.setFontHeight((short) 14);
        titleFont.setFontName("Trebuchet MS");
        style = wb.createCellStyle();
        style.setFont(titleFont);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        styles.put("title", style);

        HSSFFont itemFont = wb.createFont();
        itemFont.setFontHeightInPoints((short) 9);
        itemFont.setFontName("Trebuchet MS");
        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setFont(itemFont);
        styles.put("item_left", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        styles.put("item_right", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setDataFormat(wb.createDataFormat().getFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"));
        styles.put("input_$", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setDataFormat(wb.createDataFormat().getFormat("0.000%"));
        styles.put("input_%", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setDataFormat(wb.createDataFormat().getFormat("0"));
        styles.put("input_i", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setFont(itemFont);
        style.setDataFormat(wb.createDataFormat().getFormat("m/d/yy"));
        styles.put("input_d", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        styles.put("formula_$", style);

        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
        style.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setDataFormat(wb.createDataFormat().getFormat("0"));
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        styles.put("formula_i", style);

        return (HashMap<String, HSSFCellStyle>) styles;
    }
}

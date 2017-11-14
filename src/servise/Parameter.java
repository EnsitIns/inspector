package servise;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author 1
 */
public class Parameter {

    private DateTime dateValue;// Дата параметра
    private Double valuePar; // Значение параметра
    private String nameObject1; // Название объекта1
    private String nameObject2; // Название объекта2
    private String namePar; // Имя параметра
    private String kfcPar;// коэффициенты
    private String captionPar;// Название параметра
    private String nameTable;// таблица хранения
    private ArrayList<Integer> alIdPar; //листинг ID параметров
    private HashMap<String, Object> hmParameters;
    private HashMap<Integer, HashMap<String, Object>> hmResult; //карта результатов запроса параметров
    private HashMap<Integer, String> hmPozic; //карта позиций результата
    private String unitOfMeas;// Еденица измерения
    private String overString;// Символ для проверки превышений
    boolean bkfc;
    private ArrayList<Double> alValues;
    private Integer idObject;// id Объекта
    private Integer idPar;// id Параметра
    private int size;

    public Parameter(Integer id_object, Integer id_par, boolean kfc) throws SQLException {
        this.idObject = id_object;
        this.idPar = id_par;
        this.bkfc = kfc;
        hmParameters = new HashMap<String, Object>();
        createParameter();

    }

    /**
     * Формирует значение параметров по ID для конкретного объекта на конкретную
     * дату если значение даты равно NULL то берется последняя запись текущей
     * даты
     *
     * @param id_object
     * @param alIdPar листинг ID параметров
     * @param dataValue
     * @param kfc -true значит надо учитывать коэффициенты
     */
    public Parameter(int id_object, ArrayList<Integer> alIdPar, DateTime dataValue, boolean kfc) throws SQLException {
        this.idObject = id_object;
        this.dateValue = dataValue;
        this.alIdPar = alIdPar;
        this.bkfc = kfc;
        hmParameters = new HashMap<String, Object>();
        createParameter();
    }

    /**
     * Формирует значение параметра по его ID для конкретного объекта на
     * конкретную дату если значение даты равно NULL то берется последняя запись
     * текущей даты
     *
     * @param id_object -идентификатор объекта
     * @param id_par
     * @param dataValue
     * @param kfc -true значит надо учитывать коэффициенты
     */
    public Parameter(int id_object, int id_par, DateTime dataValue, boolean kfc) throws SQLException {
        this.idObject = id_object;
        this.idPar = id_par;
        this.dateValue = dataValue;
        this.bkfc = kfc;
        hmParameters = new HashMap<String, Object>();
        createParameter();
    }

    public DateTime getDateValue() throws SQLException {

        return dateValue;
    }

    public Integer getIdObject() {
        return idObject;
    }

    public Integer getIdPar() {
        return idPar;
    }

    public String getKfcPar() {
        return kfcPar;
    }

    public String getCaptionPar() {

        return captionPar;


    }

    public String getNamePar() throws SQLException {
        return namePar;
    }

    //Единица измерения
    public String getUnitOfMeas() {

        return unitOfMeas;


    }

    public Double getValuePar(int poz) {
        if (!hmResult.isEmpty()) {
            return (Double) hmResult.get(poz).get("value");

        } else {
            return null;
        }
    }

    public Object getParameter(String nameParameter) {
        return hmParameters.get(nameParameter);

    }

    public Double getValuePar() {
        return valuePar;
    }

    public String getNameObject1() {
        return nameObject1;
    }

    public String getNameObject2() {
        return nameObject2;
    }

    public int getSize() {
        return size;
    }

    public String getCaptionPar(int poz) {


        if (!hmResult.isEmpty()) {
            return (String) hmResult.get(poz).get("c_partype_name");

        } else {
            return null;
        }

    }

    private void createParameter() throws SQLException {

        String sql = null;
        String dataVal = null;
        Integer tarif = null;
        size = 0;
        //  Integer prm_poz_cmd;

        alValues = new ArrayList<Double>();

        sql = " SELECT * FROM c_parnumber WHERE id_parameter=" + idPar;

        ResultSet rsPar = SqlTask.getResultSet(null, sql);

        try {
            if (rsPar.next()) {

                SqlTask.addParamToMap(rsPar, hmParameters);
            } else {

                throw new SQLException("нет такого параметра !");

            }

        } finally {
            rsPar.close();
        }

        if (idObject == null) {

            captionPar = (String) hmParameters.get("c_partype_name");

            return;
        }


        namePar = (String) hmParameters.get("prm_name_tbl");
        captionPar = (String) hmParameters.get("c_partype_name");
        unitOfMeas = (String) hmParameters.get("c_unit_caption");
        nameTable = (String) hmParameters.get("name_table");
        kfcPar = (String) hmParameters.get("c_kfc");
        overString = (String) hmParameters.get("over_string");
        tarif = (Integer) hmParameters.get("prm_tarif");


        ArrayList<String> alCols = SqlTask.getNamesCol(null, nameTable);

        String sTarif = "";

        if (alCols.contains("tarif")) {

            sTarif = " AND tarif=? ";


        }

        ArrayList<Object> alValue = new ArrayList<Object>();

        Timestamp timestamp;

        //Вытаскиваем значение


        Timestamp tLast;


        if (dateValue != null) {

            sql = "SELECT * FROM " + nameTable + " WHERE Id_object=? AND value_date=?" + sTarif;

            timestamp = new Timestamp(dateValue.getMillis());
            alValue.add(idObject);
            alValue.add(timestamp);

        } else {

            sql = "SELECT *  FROM " + nameTable + " WHERE Id_object=? AND (value_date>=? AND value_date<?) " + sTarif
                    + "ORDER BY value_date";
            DateTime d = new DateTime();

            d = d.millisOfDay().setCopy(0);
            timestamp = new Timestamp(d.getMillis());

            DateTime dateTime = d.plusDays(1);

            tLast = new Timestamp(dateTime.getMillis());

            alValue.add(idObject);
            alValue.add(timestamp);
            alValue.add(tLast);



        }

        if (!sTarif.isEmpty()) {

            alValue.add(tarif);
        }



        ResultSet rsValue = SqlTask.getResultSet(null, sql, alValue.toArray());



        try {


            if (rsValue.last()) {

                valuePar = rsValue.getDouble(namePar);

                // Учитываем коэффициэнты

                if (bkfc) {
                    checkKfc();
                }

                Timestamp ts = rsValue.getTimestamp("value_date");
                dateValue = new DateTime(ts);


            }
        } finally {

            rsValue.close();
        }

    }

    /**
     * Возвращает карту в виде название поля = кфц для расчета
     *
     * @param bytSet номер установленного бита в поле prm_flag таблицы
     * c_parnumber
     * @return
     * @throws SQLException
     */
    public static HashMap<String, String> getKfcByBytSet(int bytSet) throws SQLException {

        HashMap<String, String> result = new HashMap<String, String>();
        String sql = "SELECT prm_name_tbl,c_kfc,prm_flag  FROM c_parnumber";
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            while (rs.next()) {
                int flag = rs.getInt("prm_flag");

                if (BitSetEx.isBitSet(flag, bytSet)) {

                    String nameCol = rs.getString("prm_name_tbl");
                    String kfc = rs.getString("c_kfc");

                    result.put(nameCol, kfc);
                }


            }
        } finally {
            rs.getStatement().close();
        }
        return result;
    }

    /**
     *
     * @param idObj- id объекта
     * @param nameValue название поля
     * @param value значение поля без кфц
     * @param hmParam параметры объекта
     * @param kfcPar кфц для расчета
     * @return
     * @throws Exception
     */
    public static Double getValueKfc(int idObj, String nameValue, Double value, HashMap<String, Object> hmParam, String kfcPar) throws Exception {
        Double result = value;
        HashMap<String, Object> hmObject;

        if (hmParam == null) {
            hmObject = Work.getParametersRow(idObj, null, "objects", true, false);
        } else {
            hmObject = hmParam;
        }


        String sKfc = null;

        if (kfcPar == null) {

            String sql = "SELECT c_kfc FROM c_parnumber WHERE prm_name_tbl=?";
            ResultSet rsKfc = null;


            try {
                rsKfc = SqlTask.getResultSet(null, sql, new Object[]{nameValue});

                if (rsKfc.next()) {
                    sKfc = rsKfc.getString("c_kfc");
                }

            } finally {
                rsKfc.getStatement().close();
            }


        } else {

            sKfc = kfcPar;

        }



        if (sKfc == null || sKfc.isEmpty()) {
            return result;
        }

        if (Calculater.CreatePZ(sKfc, true)) {


            Calculater.hmVar.put("this", value);


            for (String name : Calculater.hmVar.keySet()) {

                if (hmObject.containsKey(name)) {
                    Double kfc = null;

                    Object okfc = hmObject.get(name);

                    if (okfc instanceof Integer) {

                        Integer i = (Integer) okfc;

                        kfc = i * 1.0;

                    } else {

                        kfc = (Double) okfc;
                    }
                    Calculater.hmVar.put(name, kfc);
                } else if (!name.equals("this")) {
                    return result;
                }
            }

            result = Calculater.evaluateExp();
        }


        return result;
    }

    private void checkKfc() throws SQLException {


        if (kfcPar == null || kfcPar.isEmpty()) {
            return;
        }
        HashMap<String, Object> hmObject = Work.getParametersRow(idObject, null, "objects", true, false);

        if (Calculater.CreatePZ(kfcPar, true)) {


            Calculater.hmVar.put("this", valuePar);


            for (String name : Calculater.hmVar.keySet()) {

                if (hmObject.containsKey(name)) {




                    Double kfc = null;

                    Object okfc = hmObject.get(name);

                    if (okfc instanceof Integer) {

                        Integer i = (Integer) okfc;

                        kfc = i * 1.0;

                    } else {

                        kfc = (Double) okfc;
                    }




                    Calculater.hmVar.put(name, kfc);


                } else if (!name.equals("this")) {

                    throw new SQLException(name);

                }
            }

            valuePar = Calculater.evaluateExp();


        }

    }
}

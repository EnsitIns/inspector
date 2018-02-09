/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jdesktop.swingx.JXDatePicker;
import script_test.ScriptTest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

/**
 * Выполнение скриптов Groovy
 *
 * @author 1
 */
public class ScriptGroovy extends MainWorker {


    public static HashMap<String, Script> hmScripts = new HashMap<>();
    public static JXDatePicker dpFirst;
    public static JXDatePicker dpLast;
    private Integer id_schedule;
    private Integer id_sql; // Объекты запроса
    private String parameters; // Параметры скрипта
    private boolean isGroup; //true -выполнять для всей группы объектов
    private boolean isSMS; //true -отправлять SMS при запуске
    private boolean isMail; //true -отправлять уведомление на почту при запуске


    public static Object helpval; // Промежуточный объект для скриптов
    public static HashMap<String, Object> hmHelp;// Для хронения промежуточных значений

    private String nameSchedule;
    private String nameScript;  //Имя скрипта


    public ScriptGroovy() {
        hmHelp = new HashMap<>();

    }

    public Object runScript(String nameScript, HashMap<String, Object> hmPapam) throws Exception {
        Script script;
        Object result = null;
        String codScript = null;
        String[] param;
        String betweenPar;
        // Есть параметры
        int poz1;
        int poz2;

        poz1 = nameScript.indexOf("(");
        poz2 = nameScript.indexOf(")");

        if (poz1 > 0 && poz2 > 0) {

            if (hmPapam == null) {
                hmPapam = new HashMap<>();
            }

            betweenPar = nameScript.substring(poz1 + 1, poz2);

            param = betweenPar.split(",");

            nameScript = nameScript.substring(0, poz1).toUpperCase();

            for (int i = 0; i < param.length; i++) {

                String s = param[i];

                try {

                    Integer i1 = Integer.parseInt(s);

                    hmPapam.put("#par" + (i + 1), i1);

                } catch (NumberFormatException e) {

                    s = s.replaceAll("'", "");
                    hmPapam.put("#par" + (i + 1), s);
                }
            }
        }


        if (hmScripts.containsKey(nameScript)) {

            script = hmScripts.get(nameScript);
        } else {

            String sql = "SELECT *  FROM groovy_scripts  WHERE name_script=?";

            ResultSet resultSet = SqlTask.getResultSet(null, sql, new Object[]{nameScript});

            try {
                if (resultSet.next()) {
                    codScript = resultSet.getString("groovy_cod");


                    if (codScript.startsWith("test")) {

                        Object valhelp = hmHelp.get(nameScript);

                        ScriptTest test = new ScriptTest();
                        result = test.evalScript(hmPapam, valhelp);

                        return result;
                    }


                    GroovyShell shell = new GroovyShell(this.getClass().getClassLoader());
                    script = shell.parse(codScript);
                    hmScripts.put(nameScript, script);
                    script.setBinding(new Binding());

                } else {

                    throw new Exception("Скрипта " + nameScript + " не существует !");

                }

            } finally {
                resultSet.close();
            }
        }

        if (script == null) {

            return null;
        }
        Object valhelp = hmHelp.get(nameScript);

        Binding binding = script.getBinding();
        binding.setVariable("values", hmPapam);
        //  binding.setVariable("maphelp", hmHelp);
        binding.setVariable("valhelp", valhelp);
        result = script.run();

        return result;
    }


    public ScriptGroovy(ExecutorService pool) {

        this.pool = pool;

        this.pool = pool;

        setLogger(org.apache.log4j.Logger.getLogger("LogGroovy"));
        setLoggerInfo("Модуль Groovy подключен.", null);

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ScheduleClass && arg instanceof Integer) {
            try {

                id_schedule = (Integer) arg;

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
        }
    }

    public static Object executeScript(int idScript, HashMap<String, Object> hmPapam) throws Exception {

        Object result = null;
        String nameScript = null;

        String sql = "SELECT *  FROM groovy_scripts  WHERE id_script=?";

        ResultSet resultSet = SqlTask.getResultSet(null, sql, new Object[]{idScript});

        try {
            if (resultSet.next()) {
                nameScript = resultSet.getString("name_script");
                ScriptGroovy script = new ScriptGroovy();

                result = script.runScript(nameScript, hmPapam);


                return result;

            } else {

                throw new Exception("Скрипта " + idScript + " не существует !");

            }

        } finally {
            resultSet.close();
        }


    }

    public static String getScriptName(Integer idScript) throws SQLException {

        String result = null;

        String sql = "SELECT *  FROM groovy_scripts  WHERE id_script=" + idScript;

        ResultSet resultSet = SqlTask.getResultSet(null, sql);

        try {

            if (resultSet.next()) {
                result = resultSet.getString("name_script");

            } else {
                return result;
            }

        } finally {
            resultSet.close();
        }

        return result;
    }

    private boolean isParameters() throws SQLException {

        String sql = "SELECT * FROM groovy_schedule  WHERE id_schedule=" + id_schedule;

        ResultSet resultSet = SqlTask.getResultSet(null, sql);

        try {

            if (resultSet.next()) {
                Integer idScript = resultSet.getInt("id_script");
                nameScript = getScriptName(idScript);
                id_sql = resultSet.getInt("id_sql");
                String sGroup = resultSet.getString("group_groovy");
                isGroup = (sGroup.equals("Выполнять для всей группы") ? true : false);
                parameters = resultSet.getString("param_groovy");
                if (!sGroup.isEmpty()) {
                    parameters = (parameters.isEmpty() ? sGroup : parameters + ";" + sGroup);

                }


                isSMS = (resultSet.getInt("is_sms") > 0 ? true : false);

                isMail = (resultSet.getInt("is_email") > 0 ? true : false);


                if (nameScript == null) {
                    return false;
                }

            } else {
                return false;
            }

        } finally {
            resultSet.close();
        }

        return true;

    }

    private void goScript(HashMap<String, Object> hmProp) throws Exception {


        ScriptGroovy scriptGroovy = new ScriptGroovy();
        scriptGroovy.runScript(nameScript, hmProp);

    }

    @Override
    public void doProcess() {
// Выполнение скрипта

        try {

            HashMap<String, Object> hmParam = new HashMap<String, Object>();

            // Выполняем для каждого объекта
            if (!isGroup) {

                String sql = "SELECT * FROM sql_make  WHERE id_sql=" + id_sql;
                ResultSet resultSet = SqlTask.getResultSet(null, sql);


                try {

                    if (resultSet.next()) {

                        sql = resultSet.getString("sql_string");
                    }

                } finally {
                    resultSet.close();
                }

                ResultSet set = SqlTask.getResultSet(null, sql, ResultSet.CONCUR_UPDATABLE);

                try {

                    ArrayList<String> list = new ArrayList<>();

                    if (isSMS) {
                        // Отправляем sms при запуске...

                        Map<String, String> prms = MathTrans.stringToMap(parameters, ";");

                        //String phone

                        String phone = prms.get("телефон");
                        String port = prms.get("порт");

                        if (phone != null && port != null) {

                            ValuesByChannel channel = new ValuesByChannel(null, null);
                            channel.sendSMS(nameSchedule, phone, port);
                        }

                    }


                    if (isMail) {
                        // Отправляем email при запуске...


                        list.add("ooo.ensit@gmail.com");

                        set.last();
                        Integer rowCount = set.getRow();

                        set.beforeFirst();

                        String msg = "Расписание: " + nameSchedule + ". Запуск скрипта :" + nameScript + ". Объектов -" + rowCount;

                        MailClass.goMail(list, "Уведомление ЭНСИТ", msg, nameSchedule, null, null);

                    }


                    while (set.next()) {

                        hmParam.clear();
                        SqlTask.addParamToMap(set, hmParam);

                        hmParam.put("#rs", set);
                        hmParam.put("#parameters", parameters);

                        goScript(hmParam);


                    }
                } finally {

                    set.close();

                }

            } else {
                // для всей группы объектов
                hmParam.put("#parameters", parameters);
                hmParam.put("#id_sql", id_sql);
                goScript(hmParam);
            }

        } catch (Exception e) {

            setLoggerInfo(nameScript, e);

        }

    }
}

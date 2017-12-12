/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package servise;

//import jssc.SerialPort;

import cominterface.ComInterface;
import connectdbf.SqlTask;
import connectdbf.StatementEx;
import constatant_static.CursorToolkitOne;
import constatant_static.SettingActions;
import dbf.Work;
import forms.ValueWindow;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import jssc.SerialPortException;
import org.jdesktop.swingx.JXDatePicker;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.*;
import script_test.ScriptTest;
import winform.RecordWindow;
import xmldom.XmlTask;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.createIcon;
import static constatant_static.SettingActions.esStatus.*;
import static xmldom.XmlTask.getNodeListByXpath;

// import com.engidea.comm.SerialPort;

//import cmdtest.EvalGroovy;

/**
 * Класс сбора данных по физическим или GSM каналам связи
 *
 * @author 1
 */
public class ValuesByChannel extends MainWorker {


    // до выполнения команды(команд) единожды
    public static final String GO_BEGIN = "begin@";

    // перед выполнением команды
    public static final String GO_BEFORE = "before@";
    // после выполнения команды
    public static final String GO_AFTER = "after@";  // после выполнения команды
    // после выполнения ВСЕХ команды и закрытия порта
    public static final String GO_END = "end@";
    public static final String GO_ERROR = "error@";  // после выполнения команды при ошибке
    private static HashMap<String, Object> HM_LOCALE;


    // public static final int GROOVY_RESULT = 0;// Для формирования результата
    // public static final int GROOVY_COMMAND = 1;// Для формирования Команды
    // public static final int GROOVY_SCRIPT = 2;// Для формирования Команды
    public static final String SERVER_GSM = "SERVER_GSM"; // Дозвон по GSM
    public static final String SERVER_GPRS = "SERVER_GPRS"; // Дозвон по GPRS
    // возврат каретки
    //   public static final int CR = 0x0D;
    //   public static final int LF = 0x0A;// перевод строки
    //   public static final int STX = 0x02;// Символ начала блока текста
    //   public static final int ETX = 0x03;// Символ конца блока текста
    //   public static final String CHECK_BALANCE = "\u002c\u0034\u0038";
    //   public static final String CHECK_OK = "\r\n";

    public static final int BSF_GSM_YES = 0;//   Модем ответил
    public static final int BSF_GSM_GO = 1;// Номер занят
    public static final int BSF_DTR_YES = 2;// Модем поддерживает DTR
    public static final int BSF_GPRS_GSM_DIAL = 3;// Режим Дозвон (ком порт не закрываем)
    public static final int BSF_DTR_NO = 4;// Модем  не поддерживает DTR
    public static final int BSF_GSM_ERROR = 5;// Критическая ошибка (Номер не отвечяет,внутреняя ошибка


    //Флаги с 9 по 18 испльзуются для текущего присоединения  и очищаются для следующего
    // Флаги  с 0 по 8  глобальные  для текущего обращения к модему
    public static final int BSF_BUFER_PACK = 10;//Буферизация пакетов (например Меркурий 228)
    public static final int BSF_TRANS_PROTOKOL = 9;//Оборачивать  в транспортный протокол
    //( например Меркурий 228/НТС) сброс после выполнения любой команды
    public static final int BSF_NO_CARRIER = 13;// Номер недоступен
    public static final int BSF_BUSY = 14;// Номер эанят
    public static final int BSF_ERROR = 15;// Ошибка критическая


    public static final int BSF_MEMORY_EX = 16;// Расширеная  память (Меркурий 234 и выше)

    public static final int BSF_BYT17_YES = 17;// Устанавливаем в 1 byt 17 (для профиля)
    public static final int BSF_BUTTON_ON = 18;// Режим записи/ сброшен чтения
    public static final int BSF_GPRS_SERVER = 11;// Режим Сервера/ сброшен- Клиента
    public static final int BSF_REPEAT_ON = 12;// Использовать повторяющиеся команды для профиля


    //Тип операции чтения данных
    public static final int REJIM_GET_HAND = 0;//Ручной
    public static final int REJIM_GET_SCHEDULE = 1;//По расписанию
    public static final int REJIM_GET_SQL = 2;//По  запросу текущей таблицы
    public static final int REJIM_GET_DIAL = 3;//По  дозвону контроллера
    public static final int REJIM_GET_BUTTON = 4;//По  Кнопке дерева
    public static final int REJIM_SET_VALUES = 5;//Запись данных
    public static final int REJIM_GET_SCRIPT = 8;//По  Скрипту
    public static final int REJIM_SET_SERVER = 9;//Инициализация сервера
    public static final int CMD_GET = 0;//Операция чтения
    public static final int CMD_SET = 1;//Операция записи
    public static final int CMD_Q = 0;//Запрос
    public static final int CMD_A = 1;//Ответ
    public static final int TF_ALL = 0;// Искать с полным соответствием
    public static final int TF_PART = 1;// Искать с частичным соответствием
    public static final String TM_JSSC = "WINDOWS/LINUX";//тип модуля работы с портом
    public static final String TM_WINPORT = "WINDOWS";// тип модуля работы с портом
    public static final int BS_GSM_GO = 1;// Послана команда на дозвон модема
    public static final int BS_GET_HAND = 2;//Ручной(визуальный режим)
    public static Color colorBorder = new Color(0x70, 0x80, 0x90);
    public static Color colorFont = new Color(0xf0, 0xff, 0xff);
    public static String TYP_COMPORT;

    // Скрипты 
    public static final String TS_GREATE = "G";
    public static final String TS_FIND = "F";
    public static final String TS_RESULT = "R";
    public static final String TS_SAVE = "S";
    public static final String TS_CHECK = "C";
    // Для хранения выполняемых скриптов
    public static HashMap<String, Script> hmScripts = new HashMap<>();
    public static String TG_MODEM = "Модем";
    public static String TG_COUNTER = "Счетчик";
    public static String TG_CONTRLLER = "Контроллер";
    public static HashMap<String, ValuesByChannel> hmOpen; // Запущеные процессы (ключ - номер ком порта)
    public static HashMap<String, Document> hmdocTree; // Запущеные процессы (ключ - номер ком порта)
    public static HashMap<Integer, Object> blackHash = new HashMap<>();//черный список неотвечающих объетов для каналов

    public static ArrayList<String> listFull;// для счетчиков  с битом 17

    public static int nSchedule; //количество  запущеных расписаний
    public static int idSave; // id текущего сохраняемого объекта

    public static int BLACK_COUNT = 2; // при не ответе N раз попадает в черный список и не опрашивается в течении суток
    // или до перезагрузки
    public static boolean B_LOG_TXD; // лог приема -передачи
    //    public static JButton buttonGo; //кнопка запуска процесса
    private TreeMap<String, CommandGet> hmModem; // Команды GSM модема  и типа связи
    private ResultSet rsGet; //Запросы
    private ResultSet rsObjects; //Объекты запроса;
    private HashMap<Integer, ArrayList<Integer>> hmChannels;  // Объекты сгруппированые по каналу связи
    public HashMap<String, Object> hmProperty; // Текущие параметры опрашиваемого  объекта
    public HashMap<String, Object> hmWrite; // Текущие  записываемые команды
    //   public HashMap<Integer, Object> blackHash;  //  черный список неотвечающих объетов

    public DmPower power;

    public HashMap<Element, String> hmAddButtons; // Динамически добавленные кнопки

    // Дополнительные параметры
    public HashMap<Integer, Map<String, Object>> hmParJSon;
    // Команды по типам приборов учета
    public HashMap<String, TreeMap<String, CommandGet>> hmAllCommands;
    public HashMap<Integer, String> hmCaptions;// Названия объектов;
    public HashMap<String, CommandGet> hmRepeat; // Повторяющиеся команды
    private String portSchesule; // Порт опроса по расписанию
    // Команды текущего прибора
    public TreeMap<String, CommandGet> hmCommands;
    public Set<String> pribors; // Листинг всех приборов
    //  выполняемые команды
    private ArrayList<CommandGet> alRun;
    //   команды для конкретного типа связи
    private ArrayList<CommandGet> alGroup;
    private ArrayList<CommandGet> alMake;//Список созданых команд
    //   имена  выбраных  команд для проборов
    public ArrayList<String> alSelect;
    public LinkedList<String> alSelectPribor;
    //Имена  добавленных команд по каждому объекту
    // private HashMap<Integer, String> hmSelects;
    private LinkedList<String> alAddition; // Добавочные команды
    public TreeSet<String> tsBegin; // Выполненые функции(имена)   до начала запроса
    public ArrayList<CommandGet> alEnd; //Имена  Функции, которые надо выполнить после запроса
    // выбраные команды из дерева запроса
    public HashMap<String, Element> hmCommansTree; //Элементы дерева
    private Document docTree;
    HashMap<String, Object> hmParamGet; // Параметры запроса по расписанию
    //Значения для выбраных команд из базы данных
    private HashMap<Integer, HashMap<String, Object>> hmBaseValue;

    // Первичная доступность объектов
    private Set<Integer> setController;


    //Groovy сктипты команд
    // private HashMap<String,Script> hmScripts;

    // Для записи данных
    HashMap<String, StatementEx> hmStatements;
    // Данные , которые записываются в поле /answer/
    private HashMap<Integer, String> hmInfoObject;

    // Предварительно сораненные значения
    private HashMap<Integer, ArrayList<Map>> hmFirst;

    // Текущий порт запроса
    // private InputStream isRead;
    //  private OutputStream osWrite;
    //  private com.engidea.comm.SerialPort serport;
    //  private Object serialPortObj;
    private Integer time_aut;
    private Integer sys_time;
    private Integer count_sql;
    private String modelPribor; //текущая модель опрашиваемого прибора
    private String typConnect;// текущий тип связи
    public String typContoller; //текущая модель контроллера
    public String typPack; //Имя пакетного режима

    private int currIdObject; //текущий id опрашиваемого объекта
    private int currOperation; //текущая операция (запись или чтение)
    private String currentCommands;//список команд запроса для авто режима
    private String sqlTable;//sql обрабатываемой таблицы
    private boolean bController; //true если выбранный  объект контроллер    
    // Тип сервера
    private String typServer;
    private boolean bSetBaseValue; // добавлены ли значения из базы
    private boolean bOpenChannel;//Открыт ли канал связи
    private int typRegime; // тип  режима запроса
    private HashMap<String, Object> hmPoint;// текущие парамтры точки подключения
    private HashMap<String, Object> hmController;// текущие парамтры контроллера
    private String nameTable; // Таблица выбраного объекта(контроллер или прибор учета(объект)
    private boolean bComPortYes; // ком порт  открыт
    private boolean bModemYes; // модем ответил
    private boolean bTelephoneYes; // Дозвонились по телефону
    private boolean bPriborYes; // прибор ответил
    private boolean bCheckValueFromBase; // Флаг проверки наличия данных в базе
    private boolean bRing; // Флаг дозвона по GSM
    //private HashSet<Integer> hsOkey; // Удачно опрошенные каналы
    private ThreadBlock block;
    // Подчиненные команды
    private HashMap<Integer, LinkedHashMap<Timestamp, CommandGet>> hmChilds;
    private LinkedHashMap<Integer, ArrayList<String>> hmCmdSend;// Команды запроса посылаемые в порт

    /*
     ID объектов, по которым собраны ВСЕ данные в текущем сеансе связи
     */

    /*Битовые флаги   с 0-8  глобальные для всего сеанса
     с 9-18  флаги текущего присоединения
     * при  новом соединениии сбрасываются
     */
    private BitSet bitSetFlags; // Текущие флаги модуля c 0 по  8 глобальные  с 9  по 18 текущая сесия
    private ArrayList<Integer> alComScript; // Идентификаторы команд скрипта
    //JTextPane paneNex;
    private MapMessageProcess mapMessageProcess;
    private int countIter; // Количество циклов запроса

    CommandGet cmdError; //команда в которой есть  критическая ошибка

    //+++++++++++++++ Для контроллера для уставок в счетчик +++++++++++++++++++++++
    private Integer currentAddres;  // текущий  адрес счетчика 

    // Выполнение скриптов по кнопке
    //private HashMap<String, CommandGet> hmButton;
    //Команды выполняемые перед командой
    private HashMap<CommandGet, CommandGet> hmBefore;
    // если модем поддерживает DTR  то TRUE
    private Boolean isDTR;
    // Порт запроса
    private cominterface.ComInterface serialPort;
    private SelectProcessInterface processInterface; // Кнопка группового запроса

    public ValuesByChannel(ExecutorService pool, HashMap<String, Object> hmParamGet) {

        this.pool = pool;
        typRegime = REJIM_GET_HAND; // по умолчанию ручной
        setListPribors();
        currOperation = CMD_GET;// считывание данных
        alEnd = new ArrayList();
        isDTR = null;
        alRun = new ArrayList();
        bCheckValueFromBase = true;
        hmProperty = new HashMap();
        alGroup = new ArrayList();
        //   hsOkey = new HashSet();
        //   blackHash = new HashMap<>(); //черный список
        setController = new HashSet<>();
        block = new ThreadBlock();
        hmFirst = new HashMap<>();
        hmCmdSend = new LinkedHashMap<>();

        hmStatements = new HashMap();

        // Признак обертки в транспортный пакет  по умолчанию нет
        time_aut = 7000;
        sys_time = 20;
        count_sql = 3;
        hmCommansTree = new HashMap(); // Текущие команды
        hmCommands = new TreeMap();
        hmAllCommands = new HashMap();
        alSelect = new ArrayList();
        currOperation = CMD_GET;
        bRing = false;
        hmCaptions = new HashMap();
        alAddition = new LinkedList<>();
        tsBegin = new TreeSet();
        alSelectPribor = new LinkedList<>();
        alMake = new ArrayList();
        hmWrite = new HashMap();
        TYP_COMPORT = TM_WINPORT;
        portSchesule = null;
        bitSetFlags = new BitSet(18);
        this.hmParamGet = hmParamGet;
        mapMessageProcess = new MapMessageProcess();
        countIter = 1;
        try {
            setLocalParameters();
        } catch (Exception ex) {
            setLoggerInfo("Установка локальных параметров", ex);
        }


        //Создаем ком порт
        createComPort();

        // Режим сбора по расписанию
        if (this.hmParamGet != null && !this.hmParamGet.isEmpty()) {
            this.portSchesule = (String) this.hmParamGet.get("port_get");
            typRegime = REJIM_GET_SCHEDULE;

            nameTable = "objects";

            countIter = 1;

            if (hmParamGet.containsKey("count_iter")) {

                Integer count_iter = (Integer) hmParamGet.get("count_iter");

                if (count_iter != null) {
                    countIter = (count_iter < 1 ? 1 : count_iter);
                }
            }

        }

        createModemCommands();

        setLogger(org.apache.log4j.Logger.getLogger("LogChannel"));

        setLoggerInfo("Модуль CHANNEL загружен", null);

    }

    public Integer getCurrentAddres() {
        return currentAddres;
    }

    public void setCurrentAddres(Integer currentAddres) {
        this.currentAddres = currentAddres;
    }

    public void setTypRegime(int typRegime) {
        this.typRegime = typRegime;
    }


    public int getTypRegime() {
        return this.typRegime;
    }


    public void setValues() {
        typRegime = REJIM_SET_VALUES;
        executeProcess();
    }

    /**
     * Для усреднения показаний от датчиков мощности
     *
     * @return
     */
    public DmPower getDmPower() {


        DmPower power;
        power = new DmPower();
        return power;

    }

    public void setbLogTxD(boolean bLogTxD) {
        this.B_LOG_TXD = bLogTxD;
    }

    /**
     * @param typController
     * @param nameScript
     * @param cg
     * @throws Exception
     */
    public void runScriptByController(String typController, String nameScript, CommandGet cg) throws Exception {

        CommandGet comControler = hmModem.get(typController);

        if (comControler != null) {
            comControler.alSend = cg.alSend;
            comControler.alResult = cg.alResult;
            comControler.number = cg.number;
            Object val = evalScript(nameScript, comControler);

            if (val == null) {

                return;
            }

            if (nameScript.contains(TS_FIND)) {

                cg.alSend = (List<Integer>) val;

            } else if (nameScript.contains(TS_RESULT)) {

                cg.alResult = (List<Integer>) val;

            }

        }

    }

    public CommandGet getCommandByName(String nameCmd) {

        CommandGet result = null;

        result = findCommand(nameCmd);

        if (result != null) {
            return result;
        }


        if (hmCommands.containsKey(nameCmd)) {

            return hmCommands.get(nameCmd);
        } else if (hmModem.containsKey(nameCmd)) {
            return hmModem.get(nameCmd);
        }

        return result;
    }

    /**
     * Выполнение команд по скрипту
     *
     * @param typScript  -название типа связи или контроллера
     * @param nameScript
     * @throws Exception
     */
    public void runCommandByScript(String typScript, String nameScript) throws Exception {

        CommandGet comScript = hmModem.get(typScript);

        if (comScript == null) {

            comScript = hmCommands.get(typScript);
        }

        Object object = null;

        alGroup.clear();

        if (comScript != null) {
            object = evalScript(nameScript, comScript);

        } else {

            return;
        }

        if (object instanceof List) {

            List<String> alNames = (List<String>) object;
            addCmdbyScript(alNames);
            alRun = new ArrayList<CommandGet>(alGroup);

            question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, 0);
        } else if (object instanceof Map) {

            Map<String, Object> map = (Map<String, Object>) object;

            for (String name : map.keySet()) {

                if ("run".equals(name)) {

                    //Запустить в отдельном потоке
                    this.currentCommands = (String) map.get(name);
                    this.setTypRegime(ValuesByChannel.REJIM_GET_BUTTON);
                    this.executeProcess();

                    // Добавить команды и запустить в текущем потоке
                } else if ("add".equals(name)) {
                    // Добавить команды

                    List<String> alNames = (List<String>) map.get(name);
                    addCmdbyScript(alNames);
                    alRun = new ArrayList<CommandGet>(alGroup);

                    question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, 0);

                }

            }

        }

    }

    /**
     * Установка GPRS/GSM сервера
     */
    public void setAsServer() {

        //  try {
        // Открываем порт
        //    openPortRingJssc(null);
        // } catch (Exception ex) {
        //   setLoggerInfo("", ex);

        // return;
        // }

        hmPoint.put("inspSerialPort", serialPort);

        hmPoint.put("module_work", this);

        hmProperty = hmPoint;

// Режим контроля
        bitSetFlags.set(BSF_GPRS_GSM_DIAL);
        try {
            // Выполняем скрипт для инициализации сервера или отлова событий
            runCommandByScript(typServer, TS_GREATE);

            ImageIcon iconBell = (ImageIcon) getProperty("bell_on");
            JLabel label = (JLabel) getProperty("lblRing");
            if (label != null) {
                label.setIcon(iconBell);
            }

        } catch (Exception ex) {

            setLoggerInfo("Сервер не установлен .", ex);
        }

        //stopBlinkText();
        setLoggerInfo("Сервер установлен .", null);

    }

    public void setbRing(boolean bRing) {
        this.bRing = bRing;
    }

    /**
     * Скрипт Groovy по названию команды
     *
     * @param nameCmd
     * @return
     */
    private String getScriptByNameCmd(String nameCmd, String tscript) throws SQLException {

        String result = null;

        String nameScript = "set_script";

        if (tscript.equals(TS_GREATE)) {
            nameScript = "set_script";

        } else if (tscript.equals(TS_RESULT)) {

            nameScript = "script_groovy";

        } else if (tscript.equals(TS_FIND)) {

            nameScript = "find_script";

        } else if (tscript.equals(TS_SAVE)) {

            nameScript = "save_script";
        }

        String sql = " SELECT " + nameScript + " FROM commands WHERE c_name='" + nameCmd + "'";

        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            while (rs.next()) {
                result = rs.getString(1);
            }

        } finally {
            rs.close();
        }

        return result;

    }

    // Выполняется для формирования результата
    private Object evaluateGroovyTest(CommandGet commandGet, Integer nTest, Integer pozCol, Integer pozRow) throws Exception {

        Object result = null;
        ScriptTest evalGroovy = new ScriptTest();

        if (nTest == 0) {
            result = evalGroovy.evalCmdScript0(hmProperty, hmPoint, pozCol, pozRow, bitSetFlags, hmCommands, commandGet, this);
        } else if (nTest == 1) {
            result = evalGroovy.evalCmdScript1(hmProperty, hmPoint, pozCol, pozRow, bitSetFlags, hmCommands, commandGet, this);

        } else if (nTest == 2) {
            result = evalGroovy.evalCmdScript2(hmProperty, hmPoint, pozCol, pozRow, bitSetFlags, hmCommands, commandGet, this);

        } else if (nTest == 3) {
            result = evalGroovy.evalCmdScript3(hmProperty, hmPoint, pozCol, pozRow, bitSetFlags, hmCommands, commandGet, this);

        }

        return result;
    }

    /**
     * для выбора скрипта по условию if в скрипте должен начинаться с '//if#'
     *
     * @param script
     * @param commandGet
     * @param pozcol
     * @param pozrow
     * @return
     * @throws Exception
     */
    public Object getIfScript(String script, CommandGet commandGet, Integer pozcol, Integer pozrow) throws Exception {

        Object result;

        Binding binding = new Binding();

        binding.setVariable("property", hmProperty);
        binding.setVariable("point", hmPoint);
        binding.setVariable("command", commandGet.getHmPar());
        binding.setVariable("name", commandGet.name);
        binding.setVariable("bitset", bitSetFlags);
        binding.setVariable("pozcol", pozcol);
        binding.setVariable("pozrow", pozrow);
        binding.setVariable("values", commandGet.result);
        binding.setVariable("mapcommands", hmCommands);
        binding.setVariable("cmd", commandGet);
        binding.setVariable("result", commandGet.alResult);
        binding.setVariable("answer", commandGet.alAnsRes);
        binding.setVariable("send", commandGet.alSend);
        binding.setVariable("set", commandGet.alSet);
        binding.setVariable("channel", this);

        GroovyShell shell = new GroovyShell(binding);

        try {
            result = shell.evaluate(script);

        } catch (Exception exception) {

            throw new Exception(script, exception);

        }

        return result;

    }

    // Выполняется для формирования результата или команды
    public Object evalScript(String typScript, CommandGet commandGet) throws Exception {

        Object result = null;

        Script script;

        String scriptCod = null;
        //if (script.startsWith("//if#")) {
        //  script = (String) getIfScript(script, commandGet, pozcol, pozrow);
        // }

//        if (script.startsWith("#")) {
        Integer pozcol = (Integer) commandGet.getProperty("idnCol");

        Integer pozrow = (Integer) commandGet.getProperty("idnRow");


        String key = "" + commandGet.id + "_" + typScript;

        try {

            GroovyShell shell = new GroovyShell(this.getClass().getClassLoader());


            if (hmScripts.containsKey(key)) {

                script = hmScripts.get(key);
            } else {


                if (typScript.equals(TS_GREATE)) {
                    scriptCod = (String) commandGet.getProperty("set_script");

                } else if (typScript.equals(TS_RESULT)) {
                    scriptCod = (String) commandGet.getProperty("script_groovy");

                } else if (typScript.equals(TS_FIND)) {
                    scriptCod = (String) commandGet.getProperty("find_script");

                } else if (typScript.equals(TS_SAVE)) {
                    scriptCod = (String) commandGet.getProperty("save_script");

                } else if (typScript.equals(TS_CHECK)) {
                    scriptCod = (String) commandGet.getProperty("check_script");

                } else {

                    scriptCod = getScriptById(typScript);
                }


                if (scriptCod == null || scriptCod.isEmpty()) {

                    return null;
                }


                if (scriptCod.startsWith("#")) {

                    scriptCod = getScriptById(scriptCod);
                }


                if (scriptCod.startsWith("test")) {

                    int nTest = 0;

                    if (scriptCod.startsWith("test0")) {
                        nTest = 0;
                    } else if (scriptCod.startsWith("test1")) {
                        nTest = 1;
                    } else if (scriptCod.startsWith("test2")) {
                        nTest = 2;
                    } else if (scriptCod.startsWith("test3")) {
                        nTest = 3;
                    }

                    result = evaluateGroovyTest(commandGet, nTest, pozcol, pozrow);

                    return result;
                }

                script = shell.parse(scriptCod);
                hmScripts.put(key, script);


            }


            // GroovyShell  shell = new GroovyShell();


            // script= shell.parse(scriptCod);

            // GroovyShell shell = new GroovyShell(this.getClass().getClassLoader());

            //script = shell.parse(scriptCod);
            //hmScripts.put(key, script);
            script.setBinding(new Binding());
            Binding binding = script.getBinding();


            binding.setVariable("cmd", commandGet);
            binding.setVariable("property", hmProperty);
            binding.setVariable("point", hmPoint);
            binding.setVariable("bitset", bitSetFlags);
            binding.setVariable("pozcol", pozcol);
            binding.setVariable("pozrow", pozrow);
            binding.setVariable("mapcommands", hmCommands);
            binding.setVariable("channel", this);


            // try {
            result = script.run();

        } catch (Exception exception) {

            throw new Exception("Выполнение скрипта:" + key, exception);

        }


        return result;
    }

    private void createWriteCmd() throws Exception {

        if (docTree == null) {
            return;
        }

        bCheckValueFromBase = false;
        alSelect.clear();

        currentCommands = null;
        CommandGet command;

        String sql = "descendant::*";

        NodeList nodeList = XmlTask.getNodeListByXpath(docTree.getDocumentElement(), sql);

        //  NodeList nodeList = document.getElementsByTagName("row");
        for (int i = 0; i < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);
            String sel = element.getAttribute("update");

            if (!sel.isEmpty()) {

                String name = element.getAttribute("name");

                element.setAttribute("select", "1");

                if (hmCommands.containsKey(name)) {
                    command = hmCommands.get(name);

                    hmWrite.put("set@" + name, sel);

                    command.clearCommand();
                    alSelect.add(command.name);
                    //  }

                }
            }

        }

    }

    // Запись данных в архив
    private void writeGsmValues() {

        if (docTree == null) {
            return;
        }

        currOperation = CMD_SET;

        bCheckValueFromBase = false;
        alSelect.clear();
        tsBegin.clear();

        currentCommands = null;
        CommandGet command;

        hmWrite.clear();

        try {

            int idPoint;
            int idObject;
            ((Port) serialPort).stop();
            //serialPort.stop();

            String idPnt = docTree.getDocumentElement().getAttribute("id_point");
            String idObj = docTree.getDocumentElement().getAttribute("id_object");

            try {

                idPoint = Integer.parseInt(idPnt);

                idObject = Integer.parseInt(idObj);
            } catch (NumberFormatException e) {

                setLoggerInfo("", e);
                return;
            }

            ArrayList<Integer> al = new ArrayList<Integer>();

            al.add(idObject);

            currentCommands = null;

            // alEnd.clear();
            // tsBegin.clear();
            // clearYes();
            //clearSelectCommands();
            // alAddition.clear();
            //  setSelectCommands(currentCommands);
            // runAndAddCmd(alSelect);
            // addAdditionCommands(alSelect);
            // setDeffCommand();
            getValueFromGroupEx(idPoint, al);

        } finally {


            setNotifyObservers(esStopCommands);
        }

    }

    // TODO: Запрос данных по кнопке 
    private void startGetValueByButton(Observable o) {
        SelectProcessInterface process = (SelectProcessInterface) o;

        processInterface = process;

        ImageIcon icon = createIcon("log_gsm.png");
        MainWorker.buttonError.setIcon(icon);

        int rejim = process.getRejim();
        countIter = process.getCountIter();
        //  serialPort.stop();

        bCheckValueFromBase = process.isCheckValue();
        countIter = (countIter < 1 ? 1 : countIter);

        if (rejim == REJIM_GET_SQL) {
            currentCommands = process.getValuesName();

            sqlTable = process.getSqlTable();

            if (sqlTable == null) {

                sqlTable = "SELECT * FROM objects";
            }

            try {
                rsObjects = SqlTask.getResultSetBySaveSql(null, sqlTable, ResultSet.CONCUR_READ_ONLY);
            } catch (Exception ex) {
                setLoggerInfo(sqlTable, ex);

                return;
            }

        } else {
            currentCommands = null;
        }

        typRegime = rejim;//Запрос по текущей таблицы
        executeProcess();


    }


    @Override
    public void update(Observable o, Object arg) {


        if (arg instanceof SettingActions.esStatus) {

            SettingActions.esStatus status = (SettingActions.esStatus) arg;

            switch (status) {

                case esEndGoPack:
                    block.resume(); //После окончания пакетного запроса

                    break;

                case esStopCommands:// Остановить запрос данных

                    //serialPort.stop();

                    ((Port) serialPort).stop();

                    answerProcess("Остановка запроса данных.", ProcLogs.MSG_LOG);

                    break;

                case esCheckEnd:// Происходит после остановкизапроса

                    try {
                        checkEnd();
                    } catch (Exception e) {

                    }


                    break;
                case esGoCommands:// Haчало запроса данных по кнопке


                    startGetValueByButton(o);

                    break;

                case esEndTime:// Закончилось время ожидания ответа  впакетном режиме


                    answerProcess("Закончилось время ожидания ответа  в пакетном режиме !", ProcLogs.MSG_ERROR);

                    try {
                        checkEnd();
                    } catch (Exception e) {

                    }


                    // startGetValueByButton(o);

                    break;


            }
        }


        // Проверяем расписания
        if (o instanceof ScheduleClass) {


            if (arg instanceof Integer) {


                Integer i = (Integer) arg;

                //   int nSchedule = 0;

                typRegime = REJIM_GET_SCHEDULE;// автоматический режим по расписанию
                if (checkSchedule(i)) {
                    try {
                        rsGet.beforeFirst();

                        while (rsGet.next()) {

                            int isActive = rsGet.getByte("is_active");

                            if (isActive == 0) {
                                continue;
                            }

                            if (hmOpen == null) {

                                hmOpen = new HashMap<>();
                            }

                            HashMap<String, Object> hmParam = new HashMap<String, Object>();

                            SqlTask.addParamToMap(rsGet, hmParam);

                            String port = (String) hmParam.get("port_get");

                            if (hmOpen.containsKey(port)) {

                                continue;
                            }

                            nSchedule++;

                            ValuesByChannel byChannel = new ValuesByChannel(pool, hmParam);

                            byChannel.putProperty("port", port);

                            byChannel.setLogger(org.apache.log4j.Logger.getLogger("LogChannel" + nSchedule));

                            hmOpen.put(port, byChannel);

                            Object observer = this.getProperty("SwingLogger");

                            if (observer != null) {
                                byChannel.addObserver((Observer) observer);
                            }

                            byChannel.setLoggerInfo("Сбор данных по расписанию... [" + i + "]", null);
                            byChannel.executeProcess();

                        }

                        rsGet.close();

                    } catch (SQLException ex) {
                        setLoggerInfo("Запрос по расписанию", ex);
                    }

                }
            } else if (arg instanceof Boolean) {

                ((Port) serialPort).stop();
                //serialPort.stop();
                MainWorker.isStop = true;
                setLoggerInfo("Расписание  сбора данных остановлено", null);


            }


        } else if (o instanceof ValuesByChannel) {


            if (serialPort != null) {

                String namePort = serialPort.getPortName();
                String s = (String) arg;

                if (namePort.equals(s)) {
                    // closePortEngidea();
                    //    ImageIcon iconBell = (ImageIcon) getProperty("bell_off");
                    //  JLabel label = (JLabel) getProperty("lblRing");
                    // if (label != null) {
                    //   label.setIcon(iconBell);
                    // }
                }

            } else {

                if ("openPortRing".equals(arg)) {
                    //openPortRing();
                }
            }

        } else if (o instanceof ComInterface) {
            // Уведомления от ком порта

            if (arg instanceof List) {

            } else if (arg instanceof Exception) {

                Exception exception = (Exception) arg;

                setLoggerInfo("Пакетные данные", exception);

            } else if (arg instanceof CommandGet) {
                try {
                    CommandGet cg = (CommandGet) arg;

                    formResultByCommand(cg);
                } catch (Exception ex) {
                    setLoggerInfo("Результат пакетного режима", ex);
                }
            } else if (arg instanceof byte[]) {

                byte[] bs = (byte[]) arg;
                String s = new String(bs);

                if (s.contains("REMOTE IP:")) {
                    setNotifyObservers("@ring");
                }


            }


        }


    }

    // Сброс критичных параметров сбора данных
    private void clearYes() {

        bModemYes = false; // модем ответил
        bPriborYes = false; // прибор ответил

    }

    /**
     * Проверка на наличие комманд в текущем списке.
     *
     * @return
     */
    private boolean isNoNamesCmd() {
        boolean result = true;

        for (String cgName : alSelect) {

            CommandGet cg = hmCommands.get(cgName);

            if (cg == null) {

                setLoggerInfo("Команды '" + cgName + "' нет в списке команд !", null);
                result = false;
            }
        }

        return result;
    }

    /**
     * Проверяем наличие запрашиваемых значений в базе данных по группе
     * подчиненных команд (например профиль)
     *
     * @param bCheckValnBa
     * @return
     */
    private boolean checkValueFromGroup(CommandGet cmdParent, int idObject, boolean bCheckValnBa) {
        Object result = null;

        Map<Timestamp, Object> hmValue;

        Map<Timestamp, CommandGet> mapChilds;

        mapChilds = cmdParent.tmChilds;

        //  CommandGet cmdChils=mapChilds.values().iterator().next();
        // String nameChild=cmdChils.name;    
        if (!cmdParent.bSave) {

            return Boolean.TRUE;

        }

        if (bitSetFlags.get(BSF_BUTTON_ON)) {

            return Boolean.TRUE;

        }

        // Не проверяем в базе
        if (!bCheckValnBa) {

            return true;

        }

        String find_script = (String) cmdParent.getProperty("find_script");

        if (find_script != null && !find_script.isEmpty()) {

            try {

                result = evalScript(TS_FIND, cmdParent);

            } catch (Exception ex) {
                setLoggerInfo(ex.getMessage(), ex);
            }

        } else {
            // Данные в базе не хронятся

            return Boolean.TRUE;

        }

        hmValue = (Map<Timestamp, Object>) result;

        // Удаляем где есть данные
        for (Timestamp timestamp : hmValue.keySet()) {

            if (mapChilds.containsKey(timestamp)) {

                mapChilds.remove(timestamp);

            }

        }
        return false;

    }

    /**
     * Очистка команд перед запросом
     */
    private void clearCommandsModel() {

        for (CommandGet cg : hmCommands.values()) {
            cg.result = null;
            cg.sResult = null;
            cg.errorCmd = null;
            //cg.tmChilds = null;

        }
    }

    /**
     * Очистка команд перед запросом
     *
     * @param al
     */
    private void clearCommandsGet(ArrayList<CommandGet> al) {

        LinkedHashMap<DateTime, CommandGet> tmChilds;
        for (CommandGet cg : al) {
            cg.result = null;

            tmChilds = cg.tmChilds;

            if (tmChilds != null) {
                for (CommandGet commandGet : tmChilds.values()) {
                    commandGet.result = null;
                }
            }
        }
    }

    // Очищаем результаты
    private void clearResultChilds(CommandGet cg) {

        LinkedHashMap<Object, Object> tmChilds;
        tmChilds = cg.tmChilds;

        for (Object object : tmChilds.values()) {

            if (object instanceof CommandGet) {

                ((CommandGet) object).result = null;
            }
        }
    }

    /**
     * Проверяем наличие запрашиваемых значений в базе данных по группе объектов
     * одного присоединения
     *
     * @param listPribor -список опрашиваемых приборов
     * @return список id приборов с по которым есть ВСЕ данные
     */
    public ArrayList<Integer> checkValueInBase(List<Integer> listPribor) {

        boolean result = true;
        Object objectAdd = null;
        CommandGet cg = null;
        ArrayList<Integer> alObjOk = new ArrayList<Integer>();

        blinkText("Проверка наличия данных в базе...");
        try {

            for (Integer idPribor : listPribor) {

                try {

                    if (!hmCmdSend.containsKey(idPribor)) {
                        // Данные по этому объкту собраны
                        continue;
                    }


                    alSelect = (ArrayList<String>) hmCmdSend.get(idPribor).clone();

                    //  setMinMaxValue(0, alSelect.size() - 1);
                    hmProperty = Work.getParametersRow(idPribor, null, nameTable, true, false);

                    // Устанавливаем  модель прибора
                    String model = getModelPribor(nameTable);

                    if (model == null || model.isEmpty()) {

                        String msg = "Объект '" + hmCaptions.get(idPribor) + "' Прибор учета не обнаружен !";
                        setLoggerInfo(msg, null);
                        mapMessageProcess.setInfoProcess(idPribor, msg);
                        continue;
                    }

                    setCommandsByModel(model);

                    for (String cgName : alSelect) {

                        cg = hmCommands.get(cgName);

                        if (cg == null) {

                            String msg = "Команды '" + cgName + "' нет в списке команд !";
                            mapMessageProcess.setInfoProcess(idPribor, msg);
                            setLoggerInfo("Объект '" + hmCaptions.get(idPribor) + "' Прибор учета:'" + model + "'", null);
                            setLoggerInfo(msg, null);

                            continue;
                        }

                        //Выполняем процедуры перед запросом
                        runBeforeCommands(cgName);

                        if (cg.name.equals("SetProfilPower")) {

                            createFirstCmdPP(cg, idPribor);

                            if (hmChilds.containsKey(idPribor)) {
                                cg.tmChilds = hmChilds.get(idPribor);
                            }
                        }

                        if (cg.tmChilds != null && !cg.tmChilds.isEmpty()) {
                            // Проверяем наличие значений для всей группы

                            clearResultChilds(cg);

                            objectAdd = checkValueFromGroup(cg, idPribor, bCheckValueFromBase);

                            if (cg.tmChilds.isEmpty()) {

                                objectAdd = null;

                            }

                        } else {
                            // Простая команда (не профиль)

                            cg.result = null;

                            objectAdd = getValueByCommand(cg, idPribor, bCheckValueFromBase);

                        }

                        if (objectAdd == null) {

                            if (hmCmdSend.containsKey(idPribor)) {

                                Integer idOk = removeCommandInGroup(idPribor, cg.name);

                                if (idOk != null) {

                                    alObjOk.add(idOk);
                                }

                            }
                        }

//   refreshBarValue(alSelect.indexOf(cgName));
                    }
                } catch (Exception e) {

                    setLoggerInfo("Команда '" + cg.name + "'", e);

                    mapMessageProcess.setInfoProcess(idPribor, e.getMessage());

                }

            }

        } finally {
            stopBlinkText();
            setNotifyObservers(mapMessageProcess);

        }

        return alObjOk;

    }

    /**
     * Проверяет наличие расписания запроса данных
     *
     * @param id -id сработавшего расписания
     * @return
     */
    private boolean checkSchedule(int id) {

        boolean result = false;

        String nTable = "val_group_get";
        try {
            nTable = SqlTask.getNameTableByTypBase(null, nTable);
        } catch (SQLException ex) {
            setLoggerInfo("", ex);
        }

        String sql = "SELECT * FROM " + nTable + " WHERE id_schedule=" + id;
        try {

            rsGet = SqlTask.getResultSet(null, sql);

            if (rsGet.next()) {

                result = true;
            }

        } catch (SQLException ex) {

            setLoggerInfo(sql, ex);
        }

        return result;

    }

    // Текущий тип связи
    public String getTypConnect() {
        return typConnect;
    }

    // Установка ручного режима запроса
    public void setHandRegime(int regime, boolean bCheckValue, int countIter) {

        if (countIter < 1) {
            this.countIter = 1;
        } else {
            this.countIter = countIter;
        }
        typRegime = regime;

        // проверка наличия данных в базе
        bCheckValueFromBase = bCheckValue;
    }

    @Override
    public void doProcess() {

        //    if (isGoProcess()) {

        //      setLoggerInfo("Режим запроса уже запущен.", null);
        //    return;

        // }

        // setGoProcess(true);

        errorString = null;

        try {

            if (typRegime == REJIM_GET_DIAL) {
                // Дозвон контроллера

                //     newProcess("Дозвон контроллера...");
                try {

                    createEventsController();

                } catch (Exception ex) {
                    setLoggerInfo("Дозвон контроллера", ex);
                }

            } else if (typRegime == REJIM_GET_HAND || typRegime == REJIM_GET_BUTTON) {
                // Запрос в ручном режиме

                if (typRegime == REJIM_GET_HAND) {
                    newProcess("Запрос данных по каналу связи...");
                } else {
                    //      setEnabledButtonGo(0);
                    newProcess("Обработка данных...");
                }

                // Очищаем флаги текущего присоединения
                bitSetFlags.clear(9, 18);
                //  hsOkey.clear();
                hmCmdSend.clear();

                hmChannels = null;

                try {
                    try {


                        if (setBeforeCommandsSelect()) {

                            getValueHand();// в ручном режиме

                        }


                    } finally {

                        if (typRegime == REJIM_GET_BUTTON) {
                            //        clearButtonsAndAnswer();
                        }
                        //   processInterface.setImageStart();

                    }
                } catch (Exception e) {
                    setLoggerInfo("Установка команд", e);
                }

                return;
            } else if (typRegime == REJIM_GET_SCHEDULE) {

                // Очищаем флаги текущего присоединения
                bitSetFlags.clear(9, 18);

                bCheckValueFromBase = true;
                newProcess("Запрос данных по расписанию...");

                nameTable = "objects";

                getValueByShedule();

                return;
            } // По текущей таблице
            else if (typRegime == REJIM_GET_SQL) {

                // Очищаем флаги текущего присоединения
                bitSetFlags.clear(9, 18);

                //  hsOkey.clear();

                newProcess("Запрос данных по текущей таблице...");

                nameTable = "objects";
                try {
                    setBeforeCommandsSelect();
                } catch (Exception ex) {
                    setLoggerInfo("Установка команд", ex);
                }
                try {
                    createMapChannel();
                } catch (SQLException ex) {
                    setLoggerInfo("Создание каналов", ex);
                }

                try {
                    getValueByTable();
                } finally {

                    setNotifyObservers(esStopCommands);

                }

                return;
            } else if (typRegime == REJIM_SET_SERVER) {

                newProcess("Инициализация контроля событий...");

                //  setAsServer();
            }

        } finally {

            setGoProcess(false);

        }

    }

    @Override
    public void endProcess() {

        String port = (String) this.getProperty("port");

        if (hmOpen != null && hmOpen.containsKey(port)) {

            hmOpen.remove(port);

        }

        //  setEnabledButtonGo(1);

        super.endProcess();

        alMake.clear();

        //   setNotifyObservers("openPortRing");
    }

    /**
     * Поиск по частичному соответствию
     *
     * @param NameComm
     * @param mapFrom
     * @param TypFind
     * @return
     */
    private static CommandGet findCommandByName(String NameComm, Map<String, CommandGet> mapFrom, int TypFind) {

//command0l2
        if (TypFind == TF_ALL) {

            return mapFrom.get(NameComm);

        }

        if (TypFind == TF_PART) {// Частично

            for (CommandGet cmd : mapFrom.values()) {
                if (cmd.name.indexOf(NameComm) != -1) {
                    return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Преобразование байтов в строку
     *
     * @param list
     * @param delim -разделитель байтов
     * @return
     */
    private static String getStringByList(List<Integer> list, String delim, boolean bShort) {

        String s = null;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            int c = list.get(i);

            if (bShort) {
                sb.append(c);

            } else {
                sb.append((char) c);
            }

            if (delim != null && !delim.isEmpty()) {
                sb.append(delim);
            }

        }

        if (delim != null) {

            sb.deleteCharAt(sb.lastIndexOf(delim));
        }

        s = sb.toString();

        return s;

    }

    private int maskBit(int res, int len) {

        // Маскируем  биты
        int ir = (int) (res & 0x00ff);

        int s = 0;

        if (len == 1) {
            s = 0x7f;
        }

        if (len == 2) {
            s = 0x3f;
        }

        if (len == 3) {
            s = 0x1f;
        }

        ir = (int) (ir & s);

        return ir;

    }

    public static int getByte(int val, int poz, int count) {

        int r = 0;
        int l = 0;

        // l = (int) (val << (7 + 8 - poz));
        l = (int) ((int) Math.pow(2, poz + 1) - 1);

        r = (int) (val & l);

        r = (int) (r >>> poz - count + 1);

        // String s=Integer.toString(val, 2);
        // String v=s.substring(7-poz,7-poz+count);
        // r=  (int) Integer.parseInt(v, 2);
        return r;
    }

    public static int getIntByString(String s) throws NumberFormatException {
        int r = 0;

        if (s.indexOf("0x") != -1) {
            String a = s.substring(2);
            r = Integer.parseInt(a.trim(), 16);
        } else {
            r = Integer.parseInt(s.trim());
        }

        return r;
    }

    // Установка чтения или записи
    public static void setWorR(CommandGet cm, int typ) {

        String sql = "";
        String format = "";

// по умолчанию чтение
        sql = (String) cm.getProperty("c_read");
        format = (String) cm.getProperty("c_format");
        cm.calcResult = format;

        if (typ == CMD_SET) {
            // запись
            String sqlSet = (String) cm.getProperty("c_write");

            if (sqlSet != null && !sqlSet.isEmpty()) {

                sql = sqlSet;
            }

        }

        String[] arex = sql.split("//");

        if (arex.length == 3) {
            cm.query = arex[0].trim();
            cm.answer = arex[1].trim();
            cm.Ok = arex[2].trim();

            // Количество байт для проверки суммы
            String s = cm.answer.split(";")[0];

            cm.lenCrc = getIntByString(s);

        }

    }

    public String setPodstav(String spod) {

        String val = "";
        String s = "";
        int poz = 0;
        String res = null;

        if (spod.indexOf("[") == -1) {

            return spod;
        }

        String[] as = spod.split("]");
        s = new String(spod);

        for (int i = 0; i < as.length; i++) {

            val = as[i];
            poz = val.indexOf("[");

            if (poz == -1) {
                continue;
            }
            val = val.substring(poz + 1);

            Object o = hmProperty.get(val);

            if (o == null) {
                o = "";

            }

            try {

                res = o.toString().trim();

                s = s.replace(val, res);
            } catch (NullPointerException e) {

                setLoggerInfo("Параметр замены", e);

            }

        }

        res = s.replace("[", "");
        res = res.replace("]", "");

        return res;

    }

    public static String getAllName(CommandGet cm) {

        String row = (String) cm.getProperty("c_name_row");
        String col = (String) cm.getProperty("c_name_col");

        String sr = (col == null ? row : row + ":" + col);

        return sr;

    }

    public static byte[] setLongInByteArray(long l) {

        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(l);
        return bb.array();

    }

    //  Грубо проверяем выражение на расчет
    public static boolean isCalc(String eval) {

        boolean result = false;

        for (int i = 0; i < eval.length(); i++) {

            char c = eval.charAt(i);

            if (c == '+' || c == '-' || c == '*' || c == '^') {

                result = true;
                break;
            }

        }

        return result;

    }

    private ArrayList<Integer> stringValueToShortArray(String si, int kfc) {

        ArrayList<Integer> list = new ArrayList<Integer>();

        Double d = Double.parseDouble(si) * kfc;

        int addr = (int) Math.round(d);

        int t = (int) ((addr & 0xFF000000) >>> 24);
        list.add(t);

        t = (int) ((addr & 0xFF0000) >>> 16);
        list.add(t);

        t = (int) ((addr & 0xFF00) >>> 8);
        list.add(t);

        t = (int) (addr & 0xFF);
        list.add(t);

        return list;

    }

    public boolean createSendCommand(CommandGet cmdsend) throws Exception {

        boolean result = false;

        String[] slCmd = null;
        String cmd = null;
        String s = null;

        String sSend = (String) cmdsend.getProperty("c_read");
        String[] arex = sSend.split("//");


        if (arex.length == 3) {
            cmdsend.query = arex[0].trim();
            cmdsend.answer = arex[1].trim();
            cmdsend.Ok = arex[2].trim();

            // Количество байт для проверки суммы
            String sb = cmdsend.answer.split(";")[0];

            cmdsend.lenCrc = getIntByString(sb);

        }


        return result;
    }


    /**
     * Формируем команды для посылки в ком порт
     */
    public boolean createCommandExt(int ityp, CommandGet cmdsend) throws Exception {
        String[] slCmd = null;
        String cmd = null;
        String s = null;

        String group = (String) cmdsend.getHmPar().get("c_grup");

        ArrayList<String> alSubCol = new ArrayList<String>();
        alSubCol.add("1");

        Long prevValue; // Расчитаное значение

        int poz = 0;
        LinkedList<Integer> llByte = null;
        //     ArrayList<String> alClon = null;

        //   if (cmdsend.llXXX != null && cmdsend.llXXX.size() != 0) {
        //     alClon = cmdsend.llXXX.getFirst();
        // }
        // Или читаем или записываем
        //   clsCommand.SetWorR(cmdsend, ityp);
        // Делаем для запроса или для шаблона ответа
        String sSend = (String) cmdsend.getProperty("c_read");
        String[] arex = sSend.split("//");

        if (arex.length == 3) {
            cmdsend.query = arex[0].trim();
            cmdsend.answer = arex[1].trim();
            cmdsend.Ok = arex[2].trim();

            // Количество байт для проверки суммы
            String sb = cmdsend.answer.split(";")[0];

            cmdsend.lenCrc = getIntByString(sb);

        }

// format = (String) cm.getProperty("c_format");
        //  cm.calcResult = format;
        // if (ityp == CMD_Q) {  // Запрос
        slCmd = cmdsend.query.split(";");
        cmdsend.alSend = new LinkedList<Integer>();
        llByte = (LinkedList<Integer>) cmdsend.alSend;

        // }// else // Ответ
        // {
        //   slCmd = cmdsend.answer.split(";");
        //   if (cmdsend.alHelp != null) {
        //  cmdsend.alHelp.clear();
        // }
        // if (cmdsend.answer.indexOf("SUBC") != -1) {
        //   String sSubName = (String) cmdsend.getProperty("c_name_subcol");
        // alSubCol = Work.getListNameCmd(sSubName, 1);
        // }
        //   }
        for (String sPoz : alSubCol) {

            if (ityp == CMD_A) {
                llByte = new LinkedList<Integer>();
            }

            for (int i = 0; i < slCmd.length; i++) {
                cmd = slCmd[i].trim();

                if (ityp == CMD_A) {
                    cmd = cmd.replace("[SUBC]", sPoz);
                }

                if (cmd.indexOf("set@name") != -1) {

                    cmd = cmd.replace("name", cmdsend.name);
                }

                cmd = setPodstav(cmd);

// Если есть что считать
                if (isCalc(cmd) && Calculater.CreatePZ(cmd, true)) {

                    Double result = Calculater.evaluateExp();

                    prevValue = Math.round(result);

                    cmdsend.putProperty("prevValue", prevValue);

                    cmd = String.valueOf(prevValue);

                }

                if (cmd.indexOf("#EMPTY") != -1) { // Ждущая команда (Ничего не выполняется) ждем только ответ
                    // llByte.add((int) 100);
                    cmdsend.bEmpty = true;
                } else if (cmd.startsWith("#LF")) {

                    llByte.add((int) 10);

                } else if (cmd.startsWith("#CR")) {

                    llByte.add((int) 13);

                } else if (cmd.startsWith("#SOH")) {
                    llByte.add((int) 1);
                } else if (cmd.startsWith("#NAK")) {

                    llByte.add((int) 0x15);
                } else if (cmd.startsWith("#STX")) {

                    llByte.add((int) 2);
                } else if (cmd.startsWith("#EOT")) {

                    llByte.add((int) 2);
                } else if (cmd.startsWith("#ETX")) {

                    llByte.add((int) 3);
                } else if (cmd.startsWith("#LFCR")) {

                    llByte.add((int) 10);
                    llByte.add((int) 13);
                } else if (cmd.startsWith("#ACK")) {

                    llByte.add((int) 6);
                } else if (cmd.equals("@ID")) {
                    // Идентификатор команды

                    int id = cmdsend.number;

                    int b = (int) (id >> 8);
                    llByte.addLast(b);

                    b = (int) (id & 0xFF);
                    llByte.addLast(b);

                } else if (cmd.indexOf("2BYTE") != -1) {
                    // Предыдущее выражение разбить на 2 байта

                    // Предыдущее значение берем из переменной  prevValue
                    prevValue = (Long) cmdsend.getProperty("prevValue");

                    // т.к. оно может быть больше int
                    llByte.removeLast();

                    int b = 0;

                    b = (int) (prevValue >> 8);
                    llByte.addLast(b);

                    b = (int) (prevValue & 0xFF);
                    llByte.addLast(b);

                } else if (cmd.equals("@BCC")) {

                    int sum = MathTrans.getBBCSum(llByte.subList(1, llByte.size()));

                    llByte.addLast(sum);

                } else if (cmd.startsWith("@CRLIT")) {

                    /**
                     * Простая контрольная сумма из двух байт младшим байтом
                     * вперед если
                     *
                     * @CRLIT2 то складываем все байты с позиции 2 номер позиции
                     * с нуля если
                     * @CRLIT0 то с 0 позиции
                     *
                     *
                     */
                    String spoz = cmd.substring(6, cmd.length());
                    int ipoz = Integer.decode(spoz);
                    Integer ks = 0;

                    for (int ic = ipoz; ic < llByte.size() - 1; ic++) {
                        Integer k1 = llByte.get(ic);
                        ks = ks + k1;
                    }

                    // Младший
                    int t = (int) (ks & 0xFF);
                    llByte.add(t);

                    //Старший
                    t = (int) ((ks & 0xFF00) >>> 8);
                    llByte.add(t);
                } else if (cmd.equals("@SUM228")) {

                    List<Integer> list = llByte.subList(8, llByte.size());

                    Integer[] ses = (list.toArray(new Integer[list.size()]));

                    int sadd = MathTrans.getSumM228(ses);

                    llByte.add(sadd);

                } else if (cmd.equals("@CRC228")) {
                    // Контрольная сумма  для Меркурий 228 (GSM шлюз)

                    //Short[] ints = llByte.toArray(new Short[llByte.size()]);
                    int[] ses = MathTrans.getCRC24(llByte);

                    for (int iy = 0; iy < 3; iy++) {

                        Integer s1 = ses[iy];

                        llByte.addFirst(s1);

                    }

                } else if (cmd.equals("@CRC8")) {

                    // Например восток - скай
                    int crc = MathTrans.getCRC8(llByte);

                    llByte.add(crc);

                } else if (cmd.equals("@CRC16")) {  // Контрольная сумма

                    // cmdsend.alSend.toArray(new Short[cmdsend.alSend.size()]);
                    // int crc = getCRC16by1(cmdsend.alSend.toArray(new Short[cmdsend.alSend.size()])
                    //                         ,cmdsend.alSend.size());
                    // int crc = MathTrans.getCRC16(cmdsend.alSend.toArray(new Integer[cmdsend.alSend.size()]), cmdsend.alSend.size());
                    //  System.out.print(crc);
                    int crc = MathTrans.getCRC16(llByte, llByte.size());

                    int x = (int) (crc >> 8);
                    llByte.add(x);
                    x = (int) (crc & 0xFF);
                    llByte.add(x);

                    // Контольная сумма для регистратора ПУЛЬСАР
                } else if (cmd.equals("@PCRC16")) {

                    int crc = (int) MathTrans.getCRC_PULSAR(llByte, llByte.size());

                    int x = (int) (crc & 0xFF);
                    llByte.add(x);

                    x = (int) (crc >> 8);
                    llByte.add(x);

                } else if (cmd.equals("xxx")) {  // Произвольные команды из листинга

                    if (cmdsend.alSet != null) {
                        for (int as : cmdsend.alSet) {
                            llByte.add(as);
                        }

                    }

                    // Установить условия равно нулю  в контроллер
                } else if (cmd.startsWith("USLOVZERO$")) {

                    cmd = cmd.replace("USLOVZERO$", "");

                    String[] sExp = cmd.split(";");

                    //Ток
                    String usl = sExp[0];

                    if (usl.equals("Фиксировать")) {

                        llByte.add((int) 1);
                    } else {
                        llByte.add((int) 0);
                    }

                    //Напряжение
                    usl = sExp[1];

                    if (usl.equals("Фиксировать")) {

                        llByte.add((int) 1);
                    } else {
                        llByte.add((int) 0);
                    }

                } else if (cmd.startsWith("USLOV$")) {

                    cmd = cmd.replace("USLOV$", "");

                    String[] sExp = cmd.split(";");

                    //Ток
                    String sV = sExp[0].trim().replace(",", ".");

                    ArrayList<Integer> al = stringValueToShortArray(sV, 1000);

                    llByte.add(al.get(1));
                    llByte.add(al.get(3));
                    llByte.add(al.get(2));
                    //Напряжение

                    sV = sExp[1].trim().replace(",", ".");

                    al = stringValueToShortArray(sV, 100);

                    llByte.add(al.get(1));
                    llByte.add(al.get(3));
                    llByte.add(al.get(2));

                } else if (cmd.startsWith("$")) { // из базы в цифру

                    cmd = cmd.replace("$", "");
                    llByte.add(Integer.parseInt(cmd));

                } else if (cmd.startsWith("DELIM$")) {

                    // из строки  вида '12;233;33'  в массив цифр
                    cmd = cmd.replace("DELIM$", "");

                    String[] ses = cmd.split(";");

                    for (String s1 : ses) {

                        llByte.add(Integer.parseInt(s1.trim()));

                    }
                } else if (cmd.startsWith("FLEFT$")) {
                    // по правому выражению левое значение в цифру

                    cmd = cmd.replace("FLEFT$", "");

                    String sv = null;

                    String[] sExp = cmdsend.calcResult.split(";");

                    for (String sVar : sExp) {

                        String[] ss = sVar.split("=");

                        String s1 = ss[1];

                        if (cmd.equals(s1)) {
                            sv = ss[0];
                            break;
                        }
                    }

                    llByte.add(Integer.parseInt(sv));

                } else if (cmd.startsWith("FRIGHT$")) {
                    // по левому выражению правое значение в цифру
                } else if (cmd.startsWith("?")) { // из Базы    побайтно цифра

                    for (int j = 1; j < cmd.length(); j++) {
                        char c = cmd.charAt(j);
                        String a = String.valueOf(c);
                        int w = Integer.parseInt(a);
                        llByte.add((int) w);
                    }

                } else if (cmd.startsWith("BCDADDR")) //сетевой адрес устройства (4байта) в формате BCD, старшим байтом вперёд
                {

                    cmd = cmd.replace("BCDADDR", "");

                    Integer icmd = Integer.parseInt(cmd);

                    // дополнянм адрес  ведущими нулями
                    cmd = String.format("%08d", icmd);

                    String sb = cmd.substring(0, 2);
                    int ib = Integer.parseInt(sb, 16);
                    llByte.add(ib);

                    sb = cmd.substring(2, 4);
                    ib = Integer.parseInt(sb, 16);
                    llByte.add(ib);

                    sb = cmd.substring(4, 6);
                    ib = Integer.parseInt(sb, 16);
                    llByte.add(ib);

                    sb = cmd.substring(6, 8);
                    ib = Integer.parseInt(sb, 16);
                    llByte.add(ib);

                } else if (cmd.startsWith("4$")) { // в 4 байта

                    cmd = cmd.replace("4$", "");

                    Integer addr;
                    try {
                        addr = Integer.parseInt(cmd);
                    } catch (NumberFormatException e) {

                        setLoggerInfo("Создание команды", e);
                        return false;
                    }

                    int t = (int) ((addr & 0xFF000000) >>> 24);
                    llByte.add(t);

                    t = (int) ((addr & 0xFF0000) >>> 16);
                    llByte.add(t);

                    t = (int) ((addr & 0xFF00) >>> 8);
                    llByte.add(t);

                    t = (int) (addr & 0xFF);
                    llByte.add(t);

                    // i and $FF000000 shr 24
                    // i and $FF0000 shr 16
                    // i and $FF00 shr 8
                    // i and $FF
                } else if (cmd.startsWith("2LIT$")) { // в 2 байта  от младшего к старшему

                    cmd = cmd.replace("2LIT$", "");

                    Integer addr;
                    try {
                        addr = Integer.parseInt(cmd);
                    } catch (NumberFormatException e) {

                        throw new Exception("Формирование команды", e);
                    }

                    // Младший
                    int t = (int) (addr & 0xFF);
                    llByte.add(t);

                    //Старший
                    t = (int) ((addr & 0xFF00) >>> 8);
                    llByte.add(t);

                } else if (cmd.startsWith("MASK_CH")) {

                    // битовая маска для каналов для ПУЛЬСАРА
                    cmd = cmd.replace("MASK_CH", "");

                    Integer mask;
                    try {
                        mask = Integer.parseInt(cmd);
                    } catch (NumberFormatException e) {

                        throw new Exception("Формирование команды", e);
                    }

                    BitSetEx bitSetEx = new BitSetEx(0);

                    bitSetEx.set(mask - 1);

                    int bitmask = bitSetEx.getInt();

                    int[] ib = MathTrans.intToByteArray(bitmask, 4);

                    llByte.add(ib[3]);
                    llByte.add(ib[2]);
                    llByte.add(ib[1]);
                    llByte.add(ib[0]);

                    // Малдшим байтом вперед
                } else if (cmd.startsWith("2BIG$")) { // в 2 байта  от старшего  к младшему

                    cmd = cmd.replace("2BIG$", "");

                    Integer addr;
                    try {
                        addr = Integer.parseInt(cmd);
                    } catch (NumberFormatException e) {

                        throw new Exception("Формирование команды", e);
                    }

                    //Старший
                    int t = (int) ((addr & 0xFF00) >>> 8);
                    llByte.add(t);

                    // Младший
                    t = (int) (addr & 0xFF);
                    llByte.add(t);

                } else if (cmd.startsWith("#PASSWORD")) { // Откыть канал связи  

                    // Текущий пользователь
                    String password;

                    Integer revers = (Integer) hmProperty.get("revers");

                    revers = (revers != null ? revers : 1);

                    if (isSetStatus(esLevelAccess0)) {
                        // Администратор
                        password = (String) hmProperty.get("password_2");

                        llByte.add(2);

                    } else {

                        password = (String) hmProperty.get("password_1");
                        llByte.add(1);

                    }

                    int w;

                    if (revers == 0) {
                        // Пароль в asc коде
                        byte[] bs = password.getBytes("US-ASCII");

                        for (int j = 0; j < bs.length; j++) {

                            byte b = bs[j];
                            llByte.add((int) b);

                        }

                    } else {
                        // Цифра

                        for (int j = 0; j < password.length(); j++) {
                            char c = password.charAt(j);
                            w = Character.digit(c, 10);
                            llByte.add((int) w);
                        }

                    }

                } // Формирование команды  в скрипте
                else if (cmd.startsWith("SCRIPT") || cmd.startsWith("#SCRIPT")) {

                    //#SCRIPT_G,#SCRIPT_R,#SCRIPT_F,#SCRIPT_S
                    Object objArray = null;

                    if (cmd.contains("(")) {

                        ArrayList<Integer> alPar = new ArrayList<Integer>();

                        cmd = MathTrans.getBetweenVal(cmd, '(', ')');

                        String[] param = cmd.split(",");

                        for (String p : param) {
                            poz = getIntByString(p);
                            alPar.add(poz);
                        }

                        cmdsend.alSet = alPar;

                    }

                    String scriptName = "set_script";

                    //   if (cmd.endsWith(TS_GREATE)) {
                    //     scriptName = "set_script";
                    // } else if (cmd.endsWith(TS_RESULT)) {
                    //   scriptName = "script_groovy";
                    //   } else if (cmd.endsWith(TS_FIND)) {
                    //     scriptName = "find_script";
                    // } else if (cmd.endsWith(TS_SAVE)) {
                    //   scriptName = "save_script";
                    //   } else if (cmd.endsWith(TS_CHECK)) {
                    //     scriptName = "check_script";
                    // }

                    //  String script_groovy;

                    //  script_groovy = (String) cmdsend.getProperty(scriptName);

                    // if (script_groovy != null && !script_groovy.isEmpty()) {

                    // Integer pcol = (Integer) cmdsend.getProperty("idnCol");
                    // Integer prow = (Integer) cmdsend.getProperty("idnRow");

                    objArray = evalScript(TS_GREATE, cmdsend);

                    // }

                    if (objArray instanceof List) {

                        List<Integer> al = (List<Integer>) objArray;

                        for (int t : al) {
                            llByte.add(t);
                        }
                    } else if (objArray instanceof Map) {
                        /* Для формирования подчиненных команд

                         имя подчиненой комады читаем из свойств родительской
                         название параметра "nameCildCmd"

                         */
                    }

                } else if (cmd.startsWith("&")) { // ASC код

                    for (int j = 1; j < cmd.length(); j++) {
                        char c = cmd.charAt(j);
                        llByte.add((int) c);
                    }

                } else // чисто цыфры
                {

                    poz = getIntByString(cmd);
                    llByte.add((int) poz);

                }

            }

            //  if (typContoller != null) {
            //Добавляем или выполняем команды и флаги для текущего типа контроллера
            //    runScriptByController(typContoller, TS_FIND, cmdsend);
            // }
            if (ityp == CMD_A) {

                //  cmdsend.alHelp.add(llByte);
            }

        }

        // Создаем массив запроса
        //   CreateSendBox(llByte, ityp,cmdcom);
        return true;

    }

    //Формирование результата запроса сразу всех параметров ответа
    public void createResultByName(CommandGet cmd) throws Exception {

        String nameCmd = cmd.name;
        Object[] values;
        DateTime dateTime = null;
        int idx;

        int iHour;
        int iMinute;
        int iSec;

        int iDay;
        int iMonth;
        int iYear;
        // Часы

        Double z0 = 0.0;
        //   Short ss = command.alResult.get(2);
        // String sHour = Integer.toString(ss, 16);
        // dt = dt.hourOfDay().setCopy(sHour);

// Дискретные каналы
        if (nameCmd.equals("OpenRecordDiscret")) {

            values = new Object[5];

            // Дата события
            iYear = cmd.alAnsRes.get(0);
            iMonth = cmd.alAnsRes.get(1);
            iDay = cmd.alAnsRes.get(2);

            iHour = cmd.alAnsRes.get(3);
            iMinute = cmd.alAnsRes.get(4);
            iSec = cmd.alAnsRes.get(5);

            dateTime = new DateTime();

            dateTime = dateTime.secondOfDay().setCopy(iSec);
            dateTime = dateTime.minuteOfHour().setCopy(iMinute);
            dateTime = dateTime.hourOfDay().setCopy(iHour);

            dateTime = dateTime.dayOfMonth().setCopy(iDay);
            dateTime = dateTime.monthOfYear().setCopy(iMonth);
            dateTime = dateTime.yearOfCentury().setCopy(iYear);

            values[0] = dateTime;

// канал
            idx = cmd.alAnsRes.get(6);

            values[1] = idx;

            String nameVal = null;

            // Событие
            idx = cmd.alAnsRes.get(7);

            if (idx == 1) {
                nameVal = "Замыкание";

            } else {
                nameVal = "Размыкание";
            }

            values[2] = nameVal;

// Предыдущее состояние
            idx = cmd.alAnsRes.get(8);

            if (idx == 1) {
                nameVal = "Замкнуто";
            } else {
                nameVal = "Разомкнуто";
            }

            values[3] = nameVal;

            // Новое положение
            idx = cmd.alAnsRes.get(9);

            if (idx == 1) {
                nameVal = "Замкнуто";
            } else {
                nameVal = "Разомкнуто";
            }

            values[4] = nameVal;

            cmd.result = values;

        }

// Превышения параметров
        if (nameCmd.equals("OpenRecordOver")) {

            values = new Object[5];

            // адрес счетчика
            idx = cmd.alAnsRes.get(0);

            values[0] = idx;

            // Дата события
            iYear = cmd.alAnsRes.get(1);
            iMonth = cmd.alAnsRes.get(2);
            iDay = cmd.alAnsRes.get(3);

            iHour = cmd.alAnsRes.get(4);
            iMinute = cmd.alAnsRes.get(5);
            iSec = cmd.alAnsRes.get(6);

            dateTime = new DateTime();

            dateTime = dateTime.secondOfDay().setCopy(iSec);
            dateTime = dateTime.minuteOfHour().setCopy(iMinute);
            dateTime = dateTime.hourOfDay().setCopy(iHour);

            dateTime = dateTime.dayOfMonth().setCopy(iDay);
            dateTime = dateTime.monthOfYear().setCopy(iMonth);
            dateTime = dateTime.yearOfCentury().setCopy(iYear);

            values[1] = dateTime;

// Номер параметра
            idx = cmd.alAnsRes.get(7);
            String nameVal = null;

            if (idx == 1) {
                nameVal = "Ток:Фаза 'A'";
                z0 = Work.convert132byte(cmd.alAnsRes.get(9), cmd.alAnsRes.get(10), cmd.alAnsRes.get(11), 100);

            } else if (idx == 2) {
                nameVal = "Ток:Фаза 'B'";
                z0 = Work.convert132byte(cmd.alAnsRes.get(12), cmd.alAnsRes.get(13), cmd.alAnsRes.get(14), 100);

            } else if (idx == 3) {
                nameVal = "Ток:Фаза 'C'";

                z0 = Work.convert132byte(cmd.alAnsRes.get(15), cmd.alAnsRes.get(16), cmd.alAnsRes.get(17), 100);

            } else if (idx == 4) {
                nameVal = "Напряжение:Фаза 'A'";

                z0 = Work.convert132byte(cmd.alAnsRes.get(9), cmd.alAnsRes.get(10), cmd.alAnsRes.get(11), 100);

            } else if (idx == 5) {

                z0 = Work.convert132byte(cmd.alAnsRes.get(12), cmd.alAnsRes.get(13), cmd.alAnsRes.get(14), 100);

                nameVal = "Напряжение:Фаза 'В'";
            } else if (idx == 6) {

                nameVal = "Напряжение:Фаза 'С'";
                z0 = Work.convert132byte(cmd.alAnsRes.get(15), cmd.alAnsRes.get(16), cmd.alAnsRes.get(17), 100);

            }

            values[2] = nameVal;

// Событие
            idx = cmd.alAnsRes.get(8);

            if (idx == 1) {
                nameVal = "больше";
            } else if (idx == 2) {
                nameVal = "меньше";
            } else if (idx == 3) {
                nameVal = "равно нулю";
            }

            values[3] = nameVal;

            values[4] = z0;

            cmd.result = values;

        }

        //Профиль тока и напряжения
        if (nameCmd.equals("GetArxiveCutN")) {

            values = new Object[8];

            // адрес счетчика
            idx = cmd.alAnsRes.get(0);

            if (idx == 255) {

                return;
            }

            values[0] = idx;

            // Время среза
            iHour = cmd.alAnsRes.get(1);
            iMinute = cmd.alAnsRes.get(2);
            iSec = cmd.alAnsRes.get(3);

            dateTime = new DateTime();

            dateTime = dateTime.secondOfDay().setCopy(iSec);
            dateTime = dateTime.minuteOfHour().setCopy(iMinute);
            dateTime = dateTime.hourOfDay().setCopy(iHour);

            values[7] = dateTime;

            //Параметры
            //Ток
            z0 = Work.convert132byte(cmd.alAnsRes.get(4), cmd.alAnsRes.get(5), cmd.alAnsRes.get(6), 100);
            values[1] = z0;
            z0 = Work.convert132byte(cmd.alAnsRes.get(7), cmd.alAnsRes.get(8), cmd.alAnsRes.get(9), 100);
            values[2] = z0;
            z0 = Work.convert132byte(cmd.alAnsRes.get(10), cmd.alAnsRes.get(11), cmd.alAnsRes.get(12), 100);
            values[3] = z0;

            //напряжение
            //Ток
            z0 = Work.convert132byte(cmd.alAnsRes.get(13), cmd.alAnsRes.get(14), cmd.alAnsRes.get(15), 100);
            values[4] = z0;

            z0 = Work.convert132byte(cmd.alAnsRes.get(16), cmd.alAnsRes.get(17), cmd.alAnsRes.get(18), 100);
            values[5] = z0;

            z0 = Work.convert132byte(cmd.alAnsRes.get(19), cmd.alAnsRes.get(20), cmd.alAnsRes.get(21), 100);
            values[6] = z0;

            cmd.result = values;

        }

    }

    // Удаляет все узлы-ответы
    private void removeNodeAnswer() throws Exception {

        if (docTree != null) {

            NodeList nl = getNodeListByXpath(docTree.getDocumentElement(), "descendant::answer");

            HashMap<Node, Node> hm = new HashMap<>();

            for (int i = 0; i < nl.getLength(); i++) {

                Node n = nl.item(i);

                Node parent = n.getParentNode();

                hm.put(n, parent);

            }

            for (Node n : hm.keySet()) {

                Element e = (Element) n;

                String name = e.getAttribute("name");

                if (hmCommansTree.containsKey(name)) {
                    hmCommansTree.remove(name);

                }

                Node parent = hm.get(n);

                parent.removeChild(n);

            }

            checkVisibleCellTree();

        }

    }

    private void createTreeDomResult(CommandGet cmd, List result) {

        Element element = hmCommansTree.get(cmd.name);

        if (element == null) {

            return;
        }

        CommandGet cmdAdd = null;

        while (element.hasChildNodes()) {

            Node n = element.getFirstChild();

            if (n != null) {

                element.removeChild(n);

            }

        }

        for (Object m : result) {

            Map<String, String> map = (Map) m;

            Element eChild = docTree.createElement("answer");

            for (String name : map.keySet()) {

                String value = map.get(name);
                eChild.setAttribute(name, value);

            }

            String name = map.get("name");

            hmCommansTree.put(name, eChild);

            cmdAdd = hmCommands.get(name);

            String button1 = map.get("button1");
            String button2 = map.get("button2");

            if (button1 != null && !button1.isEmpty()) {
                if (cmdAdd != null) {
                    addButton(5, button1, cmdAdd);
                }

            }

            if (button2 != null && !button2.isEmpty()) {
                if (cmdAdd != null) {
                    addButton(6, button2, cmdAdd);
                }

            }

            element.appendChild(eChild);
        }

        setNotifyObservers(element);

        checkVisibleCellTree();

    }

    public void createResult(CommandGet cmd) throws Exception {

        if (cmd.alResult == null || cmd.alResult.isEmpty()) {
            return;
        }

        Object result;

        // Скрипт
        //  Integer ipozRow = (Integer) cmd.getProperty("idnCol");
        Object[] objects;

        //  String script_groovy = (String) cmd.getProperty("script_groovy");

        //   if (script_groovy != null && !script_groovy.isEmpty()) {

        // Integer pcol = (Integer) cmd.getProperty("idnCol");
        // Integer prow = (Integer) cmd.getProperty("idnRow");

        result = evalScript(TS_RESULT, cmd);


        if (result instanceof List) {

            createTreeDomResult(cmd, (List) result);
            return;
        }

        objects = (Object[]) result;

        if (objects != null) {

            cmd.result = objects[0];
            cmd.sResult = objects[1];

            if (objects.length == 3 && objects[2] != null) {
                // Критическая ошибка
                String error = (String) objects[2];

                cmd.errorCmd = error;

                //  throw new ErrorCommandException(cmd);

            }

            if (!(cmd.result instanceof List)) {

                Integer id_object = (Integer) hmProperty.get("id_object");

                if (id_object != null) {

                    putInfoObject(id_object, (String) cmd.sResult);
                }
            }

        }

        // }

    }

    public void formResultByCommand(CommandGet cmd) throws Exception {

        if (cmd.errorCmd == null) {

            // createCommandExt(CMD_A, cmd);
            try {

                if (cmd.alResult == null || cmd.alResult.isEmpty()) {

                    // Ищем в повторах
                    String sRepeat = cmd.alHelp.toString();

                    if (hmRepeat.containsKey(sRepeat)) {

                        CommandGet get = hmRepeat.get(sRepeat);

                        cmd.alResult = get.alResult;

                    }

                }

                createResult(cmd);

                String s = getAllName(cmd);
                refreshBarValue(s);
                refreshBarValue(cmd.number);

                if (cmd.tmChilds == null || cmd.tmChilds.isEmpty()) {
                    // Удаляем команду как пройденную
                    removeCommandInGroup(currIdObject, cmd.name);

                    firstSaveValue(currIdObject, cmd);

                }

                if (B_LOG_TXD) {

                    setLoggerInfo("Параметр: " + s, null);
                    setLoggerInfo("RxD [" + MathTrans.getNexStrByList(cmd.alResult, " ") + "]", null);
                    setLoggerInfo("Результат:" + cmd.sResult, null);

                }

// createResultOld(cmd);
            } catch (Exception ex) {

                // Если ошибка
                invokeMetod(cmd, GO_ERROR);

                if (ex instanceof ErrorCommandException) {

                    throw new ErrorCommandException(cmd);
                } else {
                    throw new Exception(ex);
                }

            }

            // Вывод на экран
            setNotifyObservers(cmd);

        }

    }

    // Листинг всех приборов
    private void setListPribors() {

        if (pribors == null) {

            pribors = new HashSet<String>();
        } else {

            pribors.clear();
        }

        ResultSet rs = null;

        String sql = "SELECT c_instrument FROM commands";

        try {

            TreeSet<String> tsTables = SqlTask.getNameTables(null);

            if (!tsTables.contains("commands")) {

                setLoggerInfo("В базе нет таблицы 'commands'!", null);
                return;
            }

            rs = SqlTask.getResultSet(null, sql);

            try {

                pribors = new HashSet<String>();
                while (rs.next()) {

                    String name = rs.getString(1);

                    if (name == null) {
                        continue;
                    }

                    String[] prib = name.split("/");

                    for (int i = 0; i < prib.length; i++) {
                        String pribb = prib[i];
                        pribors.add(pribb.trim());

                    }
                }
            } finally {

                rs.close();
            }

        } catch (SQLException ex) {

            setLoggerInfo(sql, ex);
        }

    }

    /**
     * Установка модели опрашиваемого прибора
     */
    private String getModelPribor(String nameTab) {

        String model = null;

        if (nameTab.equals("objects")) {

            model = (String) hmProperty.get("typ_counter");
        } else if (nameTab.equals("controllers")) {

            model = (String) hmProperty.get("typ_controller");

        } else if (nameTab.equals("t_swith")) {
            model = (String) hmProperty.get("typ_switch");
        } else if (nameTab.equals("t_discret")) {
            model = (String) hmProperty.get("typ_discret");
        } else if (nameTab.equals("points")) {
            model = (String) hmProperty.get("typ_connect");
        }

        if (model == null) {

            return model;
        }

        for (String s : pribors) {

            if (!s.isEmpty() && model.equals(s.trim())) {

                model = s;
                break;
            }
        }

        for (String s : pribors) {

            if (!s.isEmpty() && !s.equals(model) && model.contains(s)) {

                hmProperty.put("global_name", s);
                break;
            }
        }

        return model;
    }


    /**
     * Добавление клонированных команд
     *
     * @param commandGet команда клон
     */
    public void addCommandClon(String name, CommandGet commandGet) {

        if (hmAllCommands.containsKey("clon")) {

            Map map = hmAllCommands.get("clon");

            map.put(name, commandGet);

        } else {

            TreeMap<String, CommandGet> tmClon = new TreeMap<>();

            tmClon.put(name, commandGet);

            hmAllCommands.put("clon", tmClon);


        }


    }


    /**
     * Добавление добавочных команд из скрипта
     *
     * @param alSel
     */
    public void addCommands(List<String> alSel) {

        for (String cmd : alSel) {

            alAddition.add(cmd);
        }

    }

    /**
     * Добавление дополнительных команд,учавствующих в формировании запроса или
     * результата формат : cmd&+НазваниеКоманды
     */
    private void addAdditionCommands(List<String> alSel) {

        ArrayList<CommandGet> alAdd = new ArrayList<CommandGet>();

        // alAddition.clear();
        String format;

        ArrayList<String> names = new ArrayList<String>();

        CommandGet commandGet;
        for (String commandName : alSel) {

            commandGet = getCommandByName(commandName);

            format = (String) commandGet.getProperty("c_format");

            if (format == null || format.isEmpty()) {
                continue;
            }

            names = Work.getStringNames(format);

            String cmd;

            for (String nameCmd : names) {

                if (nameCmd.startsWith("cmd&")) {

                    cmd = nameCmd.replaceFirst("cmd&", "");

                    if (hmCommands.containsKey(cmd)) {

                        commandGet = getCommandByName(cmd);

                        if (!alAdd.contains(commandGet)) {

                            commandGet.clearCommand();
                            alAdd.add(commandGet);
                        }
                    }
                }
            }
        }

        int idx = 0;

        for (int i = 0; i < alAdd.size(); i++) {

            commandGet = alAdd.get(i);
            if (!alAddition.contains(commandGet.name)) {

                alAddition.add(idx, commandGet.name);

                idx++;
            }

        }

    }

    /**
     * Добавление предопределенных команд Предопределенные команды должны
     * называтся command+Номер команды добавляются в начало списка по принципу
     * command0,command1, command2 и т.д. до 10 команд в bitSetFlags содержится
     * условие добавления
     */
    public void setDeffCommand() {

        String s = null;
        CommandGet cmd = null;

        for (int i = 10; i >= 0; i--) {
            s = "command" + String.valueOf(i);

            cmd = findCommandByName(s, hmCommands, TF_PART);
            if (cmd != null) {
                // Если загрузка по уровню доступа
                if (cmd.name.indexOf("l") != -1) {
                    if (isSetStatus(esLevelAccess0)) {
                        s = "command" + String.valueOf(i) + "l0";
                        cmd = findCommandByName(s, hmCommands, TF_ALL);
                    } else if (isSetStatus(esLevelAccess1)) {
                        s = "command" + String.valueOf(i) + "l1";
                        cmd = findCommandByName(s, hmCommands, TF_ALL);
                    } else if (isSetStatus(esLevelAccess2)) {
                        s = "command" + String.valueOf(i) + "l2";
                        cmd = findCommandByName(s, hmCommands, TF_ALL);
                    } else if (isSetStatus(esLevelAccess3)) {
                        s = "command" + String.valueOf(i) + "l3";
                        cmd = findCommandByName(s, hmCommands, TF_ALL);
                    }

                }

                if (cmd != null) {

                    // Проверяем условие добавления
                    String sUslov = (String) cmd.getProperty("check_script");

                    if (sUslov != null && !sUslov.isEmpty()) {
                        try {
                            Object result = evalScript(TS_CHECK, cmd);

                            if (result != null && result instanceof Boolean) {

                                Boolean b = (Boolean) result;

                                if (!b) {
                                    continue;
                                }

                            }

                        } catch (Exception ex) {
                            MainWorker.deffLoger.error(ex.getMessage());
                        }

                    }

                    cmd.clearCommand();

                    // Добавляем в начало
                    if (!alAddition.contains(cmd.name)) {

                        alAddition.add(0, cmd.name);
                    }
                }
            }
        }

    }

    // Создаем команды для GSM Модема
    private void createModemCommands() {
        try {

            if (hmModem == null) {

                hmModem = new TreeMap<>();
            }

            if (!hmModem.isEmpty()) {
                return;
            }
            String sql = "SELECT  * FROM  commands WHERE c_grup='Модем' OR c_grup='Связь' ";

            createCommands(sql, hmModem);
            hmAllCommands.put("modem", hmModem);

        } catch (Exception ex) {

            setLogInfo("Создание модемных команд", ex);
        }

    }

    public void setModelPribor(String modelPribor) {
        this.modelPribor = modelPribor;
    }

    /**
     * @return возвращает команды текущей модели прибора + команды контроллера
     * если он есть
     */
    public TreeMap<String, CommandGet> getCommandsByModel(String model) throws Exception {

        TreeMap<String, CommandGet> hmResult = new TreeMap();

        //  hmScripts=new HashMap<>();

        // Команды для объекта
        String sql;

        String global_name = (String) hmProperty.get("global_name");

        if (global_name != null && !global_name.isEmpty()) {

            sql = "SELECT * FROM commands  WHERE  c_instrument  LIKE '%/" + model + "/%' OR c_instrument  LIKE '%/" + global_name + "/%'";

        } else {
            // Команды для объекта
            sql = "SELECT * FROM commands  WHERE  c_instrument  LIKE '%/" + model + "/%'";
        }

        createCommands(sql, hmResult);

        return hmResult;
    }

    private static String[] getArrayByString(String arStr) {
        String[] ses;

        String[] sparam = arStr.split(";");

        // Тип
        String sType = sparam[0];

        // Шаг
        String sStep = sparam[1];

        // Значения
        String[] sValues = sparam[2].split("==");

        String sStart = sValues[0];

        String sStop = sValues[1];

        Integer iStart = Integer.parseInt(sStart);
        Integer iStop = Integer.parseInt(sStop);
        ses = new String[iStop - iStart + 1];

        if (sType.equals("Time")) {

            DateTime dateTime = new DateTime(0);

            dateTime = dateTime.hourOfDay().setCopy(0);

            // шаг в минутах
            Integer iStep = Integer.parseInt(sStep);

            //   DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
            DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm:ss");

            for (int i = iStart; i <= iStop; i++) {

                dateTime = dateTime.plusMinutes(iStep);

                String rend = dtf.print(dateTime);

                ses[i - 1] = rend + "=" + i;
            }

        }

        return ses;
    }

    /**
     * Разделяем составные команды на простые
     */
    private void createCommandbyStringExp(CommandGet cmnd, ArrayList<CommandGet> alCom) throws Exception {

        // String aRow[] = new String("1").split(";");
        // String aCol[] = new String("1").split(";");
        // String aSubCol[] = new String("1").split(";");
        String aRow[] = new String[]{"1"};
        String aCol[] = new String[]{"1"};
        String aSubCol[] = new String[]{"1"};

        String row, col, subCol;
        StringBuffer sb = new StringBuffer();

        CommandGet oComm = null;
        int p0, p1;

        String[] acap = {"", "", ""};

        String aa;

        row = (String) cmnd.getProperty("c_name_row");
        col = (String) cmnd.getProperty("c_name_col");
        subCol = (String) cmnd.getProperty("c_name_subcol");

        row = (row == null ? "" : row);
        col = (col == null ? "" : col);
        subCol = (subCol == null ? "" : subCol);

        if ((row.indexOf("[") != -1)) {
            sb = new StringBuffer(row);
            p0 = sb.indexOf("[");
            p1 = sb.indexOf("]");
            acap[0] = sb.substring(0, p0);//Название
            aa = sb.substring(p0 + 1, p1);

            if (aa.indexOf("==") != -1) {

                aRow = getArrayByString(aa);

            } else {

                aRow = aa.split(";");

            }

        }

        if ((col.indexOf("[") != -1)) {
            sb = new StringBuffer(col);
            p0 = sb.indexOf("[");
            p1 = sb.indexOf("]");
            acap[1] = sb.substring(0, p0);//Название
            aa = sb.substring(p0 + 1, p1);
            aCol = aa.split(";");

        }

        if ((subCol.indexOf("[") != -1)) {

            aa = Work.getDelimitedString(subCol, '[', ']');
            cmnd.putProperty("c_name_subcol", aa);

            sb = new StringBuffer(subCol);
            p0 = sb.indexOf("[");
            p1 = sb.indexOf("]");
            acap[2] = sb.substring(0, p0);//Название

            aa = sb.substring(p0 + 1, p1);
            aSubCol = aa.split(";");

        }

        for (int iRow = 0; iRow < aRow.length; iRow++) {

            String[] rowValue = aRow[iRow].split("=");

            String rv = "";
            String rn = row;

            if (rowValue.length == 2) {
                rv = rowValue[1];
                rn = rowValue[0];

            }

            for (int iCol = 0; iCol < aCol.length; iCol++) {

                String[] colValue = aCol[iCol].split("=");

                String cv = "";
                String cn = col;

                if (colValue.length == 2) {
                    cv = colValue[1];
                    cn = colValue[0];

                }

                oComm = new CommandGet();
                oComm = (CommandGet) cmnd.clone();

                String sRead = (String) oComm.getProperty("c_read");

                sRead = (sRead == null ? "" : sRead);

                sRead = sRead.replace("[ROW]", rv);
                sRead = sRead.replace("[COL]", cv);
                //    sRead = sRead.replace("[SUBC]", subv);
                oComm.putProperty("c_read", sRead);

                String sFormat = (String) oComm.getProperty("c_format");

                sFormat = (sFormat == null ? "" : sFormat);

                sFormat = sFormat.replace("[ROW]", rv);
                sFormat = sFormat.replace("[COL]", cv);
                //  sFormat = sFormat.replace("[SUBC]", subv);
                oComm.putProperty("c_format", sFormat);

                String sName = (String) oComm.getProperty("c_name");

                sName = (sName == null ? "" : sName);

                sName = sName.replace("[ROW]", rv);

                if (!rv.isEmpty()) {
                    Integer idnRow = Integer.parseInt(rv);
                    oComm.putProperty("idnRow", idnRow);
                }

                sName = sName.replace("[COL]", cv);

                if (!cv.isEmpty()) {
                    Integer idnCol = Integer.parseInt(cv);
                    oComm.putProperty("idnCol", idnCol);
                }

                // sName = sName.replace("[SUBC]", subv);
                //  if (!subv.isEmpty()) {
                //    Integer idnSubCol = Integer.parseInt(subv);
                //  oComm.put("idnSubCol", idnSubCol);
                // }
                oComm.putProperty("c_name", sName);
                oComm.putProperty("c_name_row", acap[0] + " " + rn);
                oComm.putProperty("c_name_col", acap[1] + " " + cn);
                //  oComm.put("c_name_subcol", acap[2] + " " + subn);

                alCom.add(oComm);

                for (int iSubCol = 0; iSubCol < aSubCol.length; iSubCol++) {

                    String[] subColValue = aSubCol[iSubCol].split("=");

                    String subv = "";
                    String subn = subCol;

                    if (subColValue.length == 2) {
                        subv = subColValue[1];
                        subn = subColValue[0];
                    }

                }

            }

        }
    }

    /**
     * Создает полный список команд по файлу команд
     */
    public void createCommands(String sql, Map<String, CommandGet> hmComm) throws Exception {

        CommandGet oComm = null;
        String Fld = "";
        //   String s = "";
        String se = "";
        String sn = "";
        String[] a = null;
        int y = 0;
        int x = 0;
        int i = 0;
        int pb = 0;
        int pe = 0;
        char s1 = ' ';
        ResultSet rs;
        ArrayList<CommandGet> alClon = new ArrayList<CommandGet>();
        ArrayList<CommandGet> alDel = new ArrayList<CommandGet>();
        ArrayList<CommandGet> lFrom = new ArrayList<CommandGet>();

        getCommandByCroup(sql, lFrom);

        String s = "";
        String v = "";
        String g = "";
        for (CommandGet cmd : lFrom) {

            s = (String) cmd.getProperty("c_name_row");
            v = (String) cmd.getProperty("c_name_col");
            g = (String) cmd.getProperty("c_name_subcol");

            s = (s == null ? "" : s);
            v = (v == null ? "" : v);
            g = (g == null ? "" : g);

            if ((s != null && s.indexOf("[") != -1) || (v != null && v.indexOf("[") != -1) || (g != null && g.indexOf("[") != -1)) {

                try {
                    createCommandbyStringExp(cmd, alClon);
                } catch (Exception e) {

                    throw new Exception(e);

                }

                alDel.add(cmd);
            }
        }
        // Удаляем сложные команды
        for (CommandGet cmddel : alDel) {
            lFrom.remove(cmddel);
        }
        for (CommandGet cmd : alClon) {
            lFrom.add(cmd);
        }
        //    Формируем имена команд
        String sl = "";
        String sr = "";
        for (CommandGet cm : lFrom) {
            sl = (String) cm.getProperty("c_name");
            if (!sl.equals("")) {
                //    sl = setPodstav(sl, cm.getHmPar());
                cm.putProperty("c_name", sl);
            }
            cm.name = (String) cm.getProperty("c_name");
            sr = cm.getProperty("c_name_row") + " " + cm.getProperty("c_name_col") + " " + cm.getProperty("c_name_subcol");

            Integer iOne = (Integer) cm.getProperty("c_off");

            // true -при ошибке дальше не опрашивать
            if (iOne != null && iOne == 1) {
                cm.criticalError = true;

            } else {

                cm.criticalError = false;
            }

            //Скрипты

            Integer idCmd = (Integer) cm.getProperty("c_id");

            cm.id = idCmd;


            //  sl = (String) cm.getProperty("c_read");

            /* Формируем флаг записи данных команды в базу
             *Если данные команды не хронятся в базе,  то скрипт  'save' должен быть пустым !
             */
            String ssave = (String) cm.getProperty("save_script");

            if (ssave != null && !ssave.isEmpty()) {
                cm.bSave = true;
            }

            hmComm.put(cm.name.trim(), cm);

            //    DbfClass.SendMessage(sl, MSG_WARNING, STR_NEW);
            //  DbfClass.SendMessage(cm.name + "/ " + sr, MSG_WARNING, STR_ANSWER);
        }

    }

    public void closePort() throws Exception {
        try {

            if (!bitSetFlags.get(BSF_GPRS_GSM_DIAL)) {
                serialPort.closePort();
            }

        } catch (SerialPortException ex) {
            setLoggerInfo("Закрытие порта", ex);
        }

        //JsscSerialPort  jsscSerialPort  =new JsscSerialPort();

        // serialPort=jsscSerialPort;

        // serialPort.

        /*
         if (typModule.equals(TM_WINPORT)) {
         try {
         closePortEngidea();
         } catch (IOException ex) {

         setLoggerInfo("Закрытие порта", ex);
         }
         } else {
         try {
         closePortJssc();
         } catch (SerialPortException ex) {
         setLoggerInfo("Закрытие порта", ex);
         }
         }
         */
    }

    /**
     * Добавляем превышения в журнал
     *
     * @param commandGet
     */
    public void addEventsInJurnalOver(CommandGet commandGet) {

        DateTime dateTime;
        Timestamp timestamp;

        Integer idPoint = (Integer) hmPoint.get("id_point");

        HashMap<String, Object> hmValues = new HashMap<String, Object>();

        LinkedHashMap<Integer, CommandGet> tmOver;
        tmOver = commandGet.tmChilds;

        if (tmOver == null || tmOver.isEmpty()) {

            return;
        }

        CommandGet cmdOver = tmOver.get(0);

        Integer addres = cmdOver.alResult.get(1);

        String sql = "SELECT * FROM objects WHERE id_point=?";

        ResultSet resultSet;
        Map parObject;
        String caption = "";
        Integer idCount = null;

        HashMap<Integer, Object> hmCounters = new HashMap<>();


        try {

            resultSet = SqlTask.getResultSet(null, sql, new Object[]{idPoint});
            try {

                while (resultSet.next()) {

                    parObject = Work.getParametersRow(null, resultSet, "objects", true, true);
                    addres = (Integer) parObject.get("counter_addres");

                    hmCounters.put(addres, parObject);

                }

            } finally {
                resultSet.close();
            }

        } catch (SQLException ex) {
            setLoggerInfo("Журнал превышений", ex);
        }

        DateTime dt = new DateTime();

        int year1 = dt.getYearOfEra();
        year1 = year1 - dt.getYearOfCentury();

        for (CommandGet cmd : tmOver.values()) {

            int year = year1 + cmd.alResult.get(2);
            int month = cmd.alResult.get(3);
            int day = cmd.alResult.get(4);
            int hour = cmd.alResult.get(5);
            int min = cmd.alResult.get(6);
            int sec = cmd.alResult.get(7);

            int npar = cmd.alResult.get(8);
            String sParam = "";

            if (npar == 1) {
                sParam = "Ток,Фаза А";
            } else if (npar == 2) {
                sParam = "Ток,Фаза B";
            } else if (npar == 3) {
                sParam = "Ток,Фаза C";
            } else if (npar == 4) {

                sParam = "Напряжение,Фаза А";
            } else if (npar == 5) {
                sParam = "Напряжение,Фаза В";
            } else if (npar == 6) {
                sParam = "Напряжение,Фаза С";

            }

            String sEvent = "";

            int events = cmd.alResult.get(9);

            if (events == 1) {
                sEvent = "больше";
            } else if (events == 2) {
                sEvent = "меньше";
            } else if (events == 3) {
                sEvent = "равно нулю";
            }

            int value1 = cmd.alResult.get(10);
            int value2 = cmd.alResult.get(11);
            int value3 = cmd.alResult.get(12);

            Integer ival1 = MathTrans.getIntByBits(new int[]{value1, value3, value2}, MathTrans.B_BIG_ENDIAN);
            Double dval1;
            dval1 = (double) ival1 / 100;

            value1 = cmd.alResult.get(13);
            value2 = cmd.alResult.get(14);
            value3 = cmd.alResult.get(15);

            ival1 = MathTrans.getIntByBits(new int[]{value1, value3, value2}, MathTrans.B_BIG_ENDIAN);
            Double dval2;
            dval2 = (double) ival1 / 100;

            value1 = cmd.alResult.get(16);
            value2 = cmd.alResult.get(17);
            value3 = cmd.alResult.get(18);

            ival1 = MathTrans.getIntByBits(new int[]{value1, value3, value2}, MathTrans.B_BIG_ENDIAN);
            Double dval3;
            dval3 = (double) ival1 / 100;

            // DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute) 
            DateTime dt1 = new DateTime(year, month, day, hour, min, sec);

            Timestamp t = new Timestamp(dt1.getMillis());


            addres = cmd.alResult.get(1);

            parObject = (Map) hmCounters.get(addres);

            caption = (String) parObject.get("caption");
            idCount = (Integer) parObject.get("id_object");


            // Счетчик
            hmValues.put("id_object", idCount);
            hmValues.put("name_object", caption);
            hmValues.put("value_date", t);
            hmValues.put("name_event", sParam);
            hmValues.put("event", sEvent);
            hmValues.put("phase_a", dval1);
            hmValues.put("phase_b", dval2);
            hmValues.put("phase_c", dval3);

            try {
                Work.replaceRecInTable("jurnal_over", hmValues, true);

            } catch (Exception ex) {
                setLoggerInfo("Журнал превышений", ex);
            }
        }

    }

    /**
     * Добавляем события в журнал дискрет.
     *
     * @param commandGet
     */
    private void addEventsInJurnalDiscret(CommandGet commandGet) {

        DateTime dateTime;
        Timestamp timestamp;

        String[] parUser = (String[]) this.getProperty("parUser");
        String userName = parUser[1];

        HashMap<Integer, Integer> hmControls = new HashMap<Integer, Integer>();

        CommandGet cg = hmModem.get("Ping");
        Integer address = (Integer) cg.result;

        // Контроллер
        String sql = "SELECT * FROM controllers WHERE control_addres=" + address;

        //t_discret
        ResultSet rs;
        try {
            rs = SqlTask.getResultSet(null, sql);

            try {

                if (rs.next()) {

                    int idController = rs.getInt("c_tree_id");

                    sql = "SELECT c_tree_id FROM t_discret WHERE sub_type5=" + idController;

                    ResultSet rsControls = SqlTask.getResultSet(null, sql);

                    HashMap<String, Object> hmProperty = new HashMap<String, Object>();

                    try {
                        while (rsControls.next()) {

                            int id = rsControls.getInt("c_tree_id");

                            hmProperty = Work.getParametersRow(id, null, "objects", true, true);

                            Integer countAdd = (Integer) hmProperty.get("counter_addres");

                            hmControls.put(countAdd, id);

                        }
                    } finally {
                        rsControls.close();
                    }

                } else {
                    return;
                }
            } finally {
                rs.close();
            }

        } catch (SQLException ex) {
            setLoggerInfo(sql, ex);
        }

        HashMap<String, Object> hmValues = new HashMap<String, Object>();

        LinkedHashMap<Integer, CommandGet> tmDiscret;
        tmDiscret = commandGet.tmChilds;

        for (CommandGet get : tmDiscret.values()) {

            Object[] objects = (Object[]) get.result;

            // Дата
            dateTime = (DateTime) objects[0];
            timestamp = new Timestamp(dateTime.getMillis());

            // Номер канала
            Integer idChannel = (Integer) objects[1];

            //Название канала
            String nameChannel = "-";

            // Событие
            String sEvents = (String) objects[2];

            //Предыдущее состояние
            String oldStatus = (String) objects[3];

            //Новое состояние
            String newStatus = (String) objects[4];

            hmValues.put("Id_object", 1);
            hmValues.put("user_par", userName);
            hmValues.put("value_date", timestamp);
            hmValues.put("id_discret", idChannel);
            hmValues.put("name_discret", nameChannel);
            hmValues.put("event_discret", sEvents);
            hmValues.put("old_status", oldStatus);
            hmValues.put("current_status", newStatus);

            try {
                Work.insertRecInTable("jurnal_discret", hmValues);
            } catch (SQLException ex) {
                setLoggerInfo("Журнал превышений", ex);
            }

        }

    }

    /**
     * Проверка журналов превышениий и дискретников
     */
    private void checkEventsByGsm() {

        CommandGet command2 = hmCommands.get("JurnalOwerUstav");

        // События превышений
        if (command2.tmChilds != null && !command2.tmChilds.isEmpty()) {

            setNotifyObservers("@Дозвон");

            addEventsInJurnalOver(command2);

        }

        command2 = hmCommands.get("JurnalDiscretEvends");

        // Дискретные сигналы
        if (command2.tmChilds != null && !command2.tmChilds.isEmpty()) {

            setNotifyObservers("Discret@");

            addEventsInJurnalDiscret(command2);
        }

    }

    private void setByt17(Integer id, List<CommandGet> childs) {

        Timestamp timestamp;

        for (CommandGet commandGet : childs) {

            Object o = commandGet.getProperty("value_date");

            if (o != null && o instanceof Timestamp) {

                String full = id + "_" + o.toString();

                if (listFull.contains(full)) {

                    commandGet.putProperty("bit17", true);
                }

            }


        }


    }


    /**
     * Добавляем команды журнала превышений и дискретников
     */
    private void addCommandOver(int ita, int ist, int isql) {

        String modelPr = "RTU";

        TreeMap<String, CommandGet> hmResult = new TreeMap<String, CommandGet>();

        // Команды для объекта
        String sql = null;
        alRun.clear();

        // Команды для объекта
        sql = "SELECT * FROM commands  WHERE  c_instrument  LIKE '%" + modelPr + "%'";
        try {
            createCommands(sql, hmResult);
        } catch (Exception ex) {

            MainWorker.deffLoger.error(ex.getMessage());
        }

        CommandGet cg = hmResult.get("command0");
        alRun.add(cg);

        cg = hmResult.get("JurnalOwerUstav");
        hmCommands.put("JurnalOwerUstav", cg);
        alRun.add(cg);

        cg = hmResult.get("JurnalDiscretEvends");
        hmCommands.put("JurnalDiscretEvends", cg);

        alRun.add(cg);

        CommandGet command2 = hmResult.get("OpenRecordOver");
        hmCommands.put("OpenRecordOver", command2);

        command2 = hmResult.get("OpenRecordDiscret");
        hmCommands.put("OpenRecordDiscret", command2);

        // Очищаем журналы
        cg = hmResult.get("ClearDisJurnal");
        alRun.add(cg);

        cg = hmResult.get("ClearJurnalEvents");
        alRun.add(cg);
        try {
            // Считываем события журнала
            question(CMD_GET, alRun, ita, ist, isql, null, 0);
            // Обрабатываем журнал превышений

            checkEventsByGsm();

        } catch (Exception ex) {

            setLoggerInfo("Журнал", ex);
        }

    }

    /**
     * создает команды для ответа контроллера при дозвоне
     *
     * @throws Exception
     */
    private void createEventsController() throws Exception {

        try {

            bTelephoneYes = true;
            HashMap<String, Object> hmParam = null;

            //   blinkText("Запрос сетевого адреса...");
            hmParam = Work.getParametersFromConst("dialing");

            typConnect = "GSM";

            // Ожидание ответа
            Integer sta = (Integer) hmParam.get("time_aut");
            // Пауза между посылкой команд
            Integer sst = (Integer) hmParam.get("sys_time");
            // попыток дозвона
            Integer ssql = (Integer) hmParam.get("count_sql");

            int ita = time_aut;
            int ist = sys_time;
            int isql = count_sql;

            try {
                ita = sta;
                ist = sst;
                isql = ssql;
            } catch (NumberFormatException e) {

                setLoggerInfo("Дозвон контроллера", e);

                return;
            }

            setLoggerInfo("Дозвон контроллера...", null);

            CommandGet cgPing = hmCommands.get("Ping");

            alRun.clear();

            alRun.add(cgPing);

            // Прочитать жупнал превышений
            CommandGet cgJurnal = hmCommands.get("JurnalEvents");

            CommandGet cgOpen = hmCommands.get("command0");

            //  alRun.add(cgJurnal);
            // очистить журнал событий
            CommandGet cgClear = hmCommands.get("ClearJurnalEvents");

            //  setMinMaxValue(0, alRun.size());
            //  alRun.add(cgClear);
            try {

                question(CMD_GET, alRun, ita, ist, isql, null, 0);

                //Сетевой адрес контроллера
                Integer iAdress;

                iAdress = cgPing.alResult.get(1);
                //  iAdress = 99;

                String sql = "SELECT * FROM controllers WHERE control_addres=?";

                LinkedHashMap<String, Object> hmControl = SqlTask.getRowValues(null, sql, new Object[]{iAdress});

                Integer idPoint = (Integer) hmControl.get("id_point");

                hmPoint.put("id_point", idPoint);

                String password1_ctrl = (String) hmControl.get("password1_ctrl");

                // добавляем команды журнала контроллера
                hmProperty.put("control_addres", iAdress);
                hmProperty.put("password1_ctrl", password1_ctrl);

                alRun.clear();
                alRun.add(cgOpen);
                alRun.add(cgJurnal);
                alRun.add(cgClear);

                //    setMinMaxValue(0, alRun.size());
                question(CMD_GET, alRun, ita, ist, isql, null, 0);

                setLoggerInfo("Дозвонился контроллер № " + iAdress, null);

                // stopBlinkText();
                addEventsInJurnalOver(cgJurnal);

                setNotifyObservers("@Дозвон");

            } catch (Exception ex) {
                setLoggerInfo("", ex);
            }

        } finally {

            checkEnd();

        }

    }


    /**
     * Возвращает id объекта по котрому собраны все данные
     *
     * @param idObject
     * @param nameCmd
     * @return
     */
    private Integer removeCommandInGroup(Integer idObject, String nameCmd) {

        if (hmCmdSend.containsKey(idObject)) {

            ArrayList<String> al = hmCmdSend.get(idObject);

            if (al.contains(nameCmd)) {

                al.remove(nameCmd);
            }

            if (al.isEmpty()) {


                hmCmdSend.remove(idObject);
                mapMessageProcess.setInfoProcess(idObject, MapMessageProcess.DATA_GET);
                setNotifyObservers(mapMessageProcess);

                //Объект ответил, удаляем из черного списка
                if (blackHash.containsKey(idObject)) {
                    blackHash.remove(idObject);
                }

                return idObject;
            }

        }
        return null;
    }

    private void setCommandsInGroup(ArrayList<Integer> alGroup) {

        //hmCmdSend.clear();

        for (Integer id : alGroup) {

            ArrayList al = new ArrayList(alSelect);

            hmCmdSend.put(id, al);

        }

    }

    // Предварительное сохранение данных
    private void firstSaveValue(Integer idPribor, CommandGet cg) throws Exception {

        ArrayList<Map> alValues;


        if (hmFirst.containsKey(idPribor)) {

            alValues = hmFirst.get(idPribor);

        } else {

            alValues = new ArrayList<>();
            hmFirst.put(idPribor, alValues);

        }

        if (cg.result == null) {
            return;
        }
        Timestamp ts = (Timestamp) cg.getProperty("value_date");

        if (ts == null) {

            return;
        }

        Object value;

        //Integer pcol = (Integer) cg.getProperty("idnCol");
        //Integer prow = (Integer) cg.getProperty("idnRow");

        value = evalScript(TS_SAVE, cg);

        if (value instanceof Map) {

            alValues.add((Map) value);
        }

    }

    /**
     * Создание порта в зависимости от системы
     */
    private void createComPort() {


        // serialPort = new Pi4jSerialPort(bitSetFlags, this);

        serialPort = new JsscSerialPort(bitSetFlags, this);


    }

    public static void setLocalParameters() throws Exception {

        InputStream is;
        try {

            String dir = System.getProperty("user.dir");
            File file = new File(dir, "config_local.xml");

            is = new FileInputStream(file);

            HM_LOCALE = XmlTask.getMapValuesByXML(is, "name", "value", "cell");

            String typ_port = (String) HM_LOCALE.get("typ_port");

            if (!typ_port.isEmpty() && typ_port.equals(TM_WINPORT)) {

                TYP_COMPORT = TM_WINPORT;
            } else {

                TYP_COMPORT = TM_JSSC;
            }

            try {
                is.close();
            } catch (IOException ex) {
                Work.ShowError(ex.getMessage());
            }

        } catch (FileNotFoundException ex) {
            Work.ShowError(ex.getMessage());

        }

        //typ_port
    }

    private String getCurrentCommands() throws SQLException {
        String curCon = null;

        Integer idCmd = (Integer) hmParamGet.get("id_make");

        if (idCmd == null && idCmd == -1) {

            throw new SQLException("Не выбраны команды запроса!");

        }

        String sql = "SELECT * FROM cmd_make WHERE id_make=" + idCmd;

        ResultSet setCmd = SqlTask.getResultSet(null, sql);

        try {

            if (setCmd.next()) {

                curCon = setCmd.getString("make_string");

            } else {

                throw new SQLException("Не выбраны команды запроса!");
            }

        } finally {
            setCmd.close();
        }

        return curCon;
    }

    private void getValueByShedule() {

        try {

            Integer idSql = (Integer) hmParamGet.get("id_sql");

            // тип запроса (команды или скрипт)
            //текущие команды
            currOperation = CMD_GET;

            currentCommands = getCurrentCommands();

            // если начинается  с "@" то Скрипт Groovy
            if (currentCommands.startsWith("@")) {
                // Выполняем скрипт
            }

            setLoggerInfo("Команды запроса: " + currentCommands, null);

            String sql = null;

            if (idSql == null || idSql == -1) {
                // по всем объектам

                String nameView = SqlTask.getViewByTable("objects");

                sql = "SELECT * FROM " + nameView;

                rsObjects = SqlTask.getResultSet(null, sql);

            } else {

                sql = "SELECT * FROM sql_make WHERE id_sql=" + idSql;

                ResultSet set = SqlTask.getResultSet(null, sql);

                String sqlObj = null;

                try {
                    if (set.next()) {

                        sqlObj = set.getString("sql_string");

                        setLoggerInfo("SQL запроса: " + sqlObj, null);

                        rsObjects = SqlTask.getResultSetBySaveSql(null, sqlObj, ResultSet.CONCUR_READ_ONLY);
                    }
                } finally {
                    set.close();
                }
            }
            //hsOkey.clear();

            try {
                setBeforeCommandsSelect();
            } catch (Exception ex) {
                setLoggerInfo("Установка команд", ex);
            }

            setLoggerInfo("Количество циклов запроса: " + countIter, null);

            createMapChannel();
            getValueByTable();

        } catch (SQLException ex) {
            setLoggerInfo("", ex);
        }

    }

    /**
     * Возвращает дату по типу записи в базу
     *
     * @param typSave
     * @return
     */
    private Timestamp getDateByTypSave(int typSave, CommandGet commandGet) {

        DateTime dt = new DateTime();

        Timestamp timestamp;

        if (typSave == 14) {

            DateTime time;

            time = (DateTime) commandGet.getProperty("value_date");

            if (time != null) {
                dt = time;
            }

        } else if (typSave == 13) {

            dt = dt.millisOfDay().setCopy(0);

        }

        timestamp = new Timestamp(dt.getMillis());

        return timestamp;
    }

    public Object getValueByCommandEx(String tab_save, Integer idPribor, String nameCmd, boolean isCheckBase) {

        Object result = null;

        return result;
    }

    /**
     * Возвращает значение команды
     *
     * @param commandGet
     * @param isCheckBase -true проверять в базе данных
     * @return Значение команды если Boolean , то значит данные в базе не
     * хронятся
     */
    public Object getValueByCommand(CommandGet commandGet, Integer idPribor, boolean isCheckBase) {

        String nameCmd = commandGet.name;
        commandGet.result = null;

        Object result = null;

        if (bitSetFlags.get(BSF_BUTTON_ON)) {

            return Boolean.TRUE;

        }

        if (!commandGet.bSave) {

            return Boolean.TRUE;

        }


//        Integer pcol = (Integer) commandGet.getProperty("idnCol");
        //  Integer prow = (Integer) commandGet.getProperty("idnRow");

        //Хронятся но не проверяем
        if (!isCheckBase) {


            return Boolean.TRUE;

            //    pcol = -1;

        }

        Element element;

        try {

            /**
             * Любой результат возвращается в ввиде карты [timeStamp:Object]
             * если есть данные то результат должен возвращатся в виде Списка
             * [timeStamp:ArrayList] иначе данных нет
             */
            result = evalScript(TS_FIND, commandGet);

            if (result == null) {

                return Boolean.TRUE;

            }


        } catch (Exception ex) {
            setLoggerInfo(ex.getMessage(), ex);

        }

        Map<Timestamp, Object> hmVal = (Map<Timestamp, Object>) result;

        if (hmVal == null || hmVal.isEmpty()) {

            return true;
        }

        Object val = hmVal.values().iterator().next();

        if (val != null && val instanceof ArrayList) {

            if (hmCommansTree != null && !hmCommansTree.isEmpty()) {

                element = hmCommansTree.get(nameCmd);

                if (element != null) {

                    element.setAttribute("base", "1");

                    setValueInTree(element, val);
                }
            }

            return null;

        } else {
            // Данных нет

            return Boolean.TRUE;

        }
    }

    /**
     * Установка адреса текущего счетчика
     *
     * @param command
     */
    public void setAddresCounters(CommandGet command) {

        int address = getAddressCounter();

        if (address == -1) {

            JOptionPane.showMessageDialog(null, "Не выбран текущий счетчик !");

            ((Port) serialPort).stop();
            //serialPort.stop();
            return;
        }

        this.setCurrentAddres(address);

        //command.addByteInSet(address);
        // hmWrite.put("current_address", address);
    }

    private void saveInfoObjects() throws SQLException {

        if (hmInfoObject != null) {

            HashMap<String, Object> hashMap = new HashMap<>();

            //    String sql="UPDATE  objects  SET answer=? WHERE id_object=?";
            hashMap.put("id_object", 0);
            hashMap.put("answer", "");

            StatementEx statementEx = new StatementEx(null, "objects", hashMap);

            for (int id : hmInfoObject.keySet()) {

                String value = hmInfoObject.get(id);

                hashMap.put("id_object", id);
                hashMap.put("answer", value);

                statementEx.replaceRecInTable(hashMap, false);

            }

        }
    }

    /**
     * Добавление значений в поле answer таблицы объектов
     *
     * @param id
     * @param value
     */
    private void putInfoObject(int id, String value) {

        if (hmInfoObject == null) {

            hmInfoObject = new HashMap<>();

        }

        hmInfoObject.put(id, value);

    }

    private void saveJsonValues() throws SQLException {

        if (hmParJSon == null || hmParJSon.isEmpty()) {

            return;
        }

        for (Integer id : hmParJSon.keySet()) {

            Map<String, Object> map = hmParJSon.get(id);

            Work.setJsonParameters(id, map);

        }

    }

    private void saveValues() throws SQLException {

        ArrayList<Map> alValues;

        blinkText("запись в базу данных...");

        try {

            for (Integer idObject : hmFirst.keySet()) {
                alValues = hmFirst.get(idObject);

                for (Map map : alValues) {

                    String nameTabValue = (String) map.get("name_table");

                    map.remove("name_table");

                    map.put("id_object", idObject);

                    idSave = idObject;

                    if (!map.containsKey("object_caption")) {

                        String caption = "-";
                        map.put("object_caption", caption);
                    }

                    map.put("modify_date", new Timestamp(new Date().getTime()));
                    map.put("is_check", 0);
                    map.put("flag0", 0);
                    map.put("flag1", 0);

                    StatementEx statementEx;


                    String nameStatement = map.keySet().toString();

                    if (hmStatements.containsKey(nameStatement)) {
                        statementEx = hmStatements.get(nameStatement);

                    } else {

                        statementEx = new StatementEx(null, nameTabValue, (HashMap) map);
                        hmStatements.put(nameStatement, statementEx);
                    }

                    try {

                        statementEx.replaceRecInTable(map, true);
                    } catch (SQLException exception) {

                        MainWorker.setLogInfo(map.toString(), exception);
                    }

                }

            }

        } finally {

            for (StatementEx se : hmStatements.values()) {
                se.close();
            }

            hmStatements.clear();
            stopBlinkText();

        }

    }

    /**
     * Добавляет Методы или скрипты в список команд , которые должны выполнятся
     * после выполнения всего запроса
     */
    private void addCommandGoEnd(CommandGet command) {

        String metods = (String) command.getProperty("c_formula");

        if (metods == null || metods.isEmpty()) {
            return;
        }

        ArrayList<String> alMetod = Work.getListByStartsWith(metods, GO_END, "//");

        if (alMetod != null && !alMetod.isEmpty()) {

            if (!alEnd.contains(command)) {
                alEnd.add(command);
            }
        }

    }


    // Для датчиков мощности
    class DmPower {
        Integer interval;

        int fulinter1;
        int fulinter2;

        Timestamp timeValues;

        HashMap<Integer, DateTime> hmDate;

        HashMap<Integer, ArrayList<ArrayList<Double>>> hmValue;
        DateTime t1;
        DateTime t2;


        public DmPower() {
            interval = 2;
            hmValue = new HashMap<>();
            hmDate = new HashMap<>();

        }


        public Timestamp getTimeValues() {
            return timeValues;
        }


        Timestamp createTimeValues() {

            DateTime time = new DateTime().millisOfSecond().setCopy(0).secondOfMinute().setCopy(0);

            time = time.minuteOfHour().setCopy(fulinter2 * interval);

            Timestamp result = new Timestamp(time.getMillis());
            return result;

        }


        public ArrayList<Double> write(Integer id, ArrayList<Double> value) {

            t2 = new DateTime();


            if (hmDate.containsKey(id)) {

                t1 = hmDate.get(id);
            } else {

                t1 = new DateTime();
                hmDate.put(id, t1);

            }


            int min1 = t1.getMinuteOfHour();
            int min2 = t2.getMinuteOfHour();

            // полных интервалов

            fulinter1 = min1 / interval;
            fulinter2 = min2 / interval;

            //Остаток  интервала

            int div = min2 % interval;


            ArrayList<Double> result = new ArrayList<>();

            int delta = min2 - min1;
            ArrayList<ArrayList<Double>> listval;

            if (hmValue.containsKey(id)) {

                listval = hmValue.get(id);


            } else {

                listval = new ArrayList<>();

            }
            if ((fulinter1 == fulinter2) && (div != 0)) {
                // в пределах интервала

                listval.add(value);


                //Пишем в память
                hmValue.put(id, listval);
                result = null;

            } else if (div == 0) {

                //кратно интервалу

                // пишем в память последнее значение

                listval.add(value);
                hmValue.put(id, listval);
                // остаток равен 0
                // усредняем  и Пишем в базу
                result = average(id);
                hmValue.remove(id);

                timeValues = createTimeValues();
                //обнуляем все

                hmValue.remove(id);
                hmDate.remove(id);


            } else if ((fulinter2 != fulinter1) && (div != 0)) {

                result = average(id);
                hmValue.remove(id);
                // усредняем и пишем в базу все предыдущие, новое значение в память.

                timeValues = createTimeValues();
                t1 = new DateTime();
                hmDate.put(id, t1);
                listval.add(value);
                hmValue.put(id, listval);

            }


            return result;
        }

        /**
         * усредням  все значения
         *
         * @return
         */
        ArrayList<Double> average(Integer id) {


            ArrayList<Double> result = new ArrayList<>();

            ArrayList<ArrayList<Double>> listVal;
            listVal = hmValue.get(id);

            int size = listVal.get(0).size();

            Double aResult[] = new Double[size];

            int aversize = listVal.size();


            for (int i = 0; i < size; i++) {
                aResult[i] = 0.0;
            }


            for (ArrayList<Double> list : listVal) {
                for (int i = 0; i < size; i++) {

                    Double dval = list.get(i);

                    aResult[i] = dval + aResult[i];
                }

            }

            for (int i = 0; i < size; i++) {

                aResult[i] = aResult[i] / aversize;
                result.add(aResult[i]);
            }


            return result;
        }


    }


    // Закончилось время ожидания ответа
    class ErrorCommandException extends Exception {

        private CommandGet commandGet;

        public ErrorCommandException(CommandGet commandGet) {

            this.commandGet = commandGet;
        }

        public String getErrorCmd() {

            if (commandGet != null) {
                return commandGet.errorCmd;

            } else {
                return "";
            }
        }

        public ErrorCommandException() {
        }

        public ErrorCommandException(String gripe) {

            super(gripe);
        }
    }

    public HashMap<String, Element> getHmCommansTree() {
        return hmCommansTree;
    }

    /**
     * Проверяем есть ли подчененные
     */
    private synchronized void questionChilds(CommandGet cmd, List<CommandGet> listCmd, int waitTime, int pauseTime, int countSql, Integer idPribor, Integer number) throws Exception {

        //  есть подчиненные  команды
        if (cmd.tmChilds != null && !cmd.tmChilds.isEmpty()) {

            // Ждем ответа на родительскую команду пока ответ не придет ждем
            while (cmd.result == null || ((Port) serialPort).isStop()) {

                Thread.sleep(200);

            }

            Object o = cmd.tmChilds.values().iterator().next();

            if (o instanceof CommandGet) {

                int size_curr = listCmd.size();

                int size_add = cmd.tmChilds.size();

                // с учетом подчиненных команд
                setMinMaxValue(0, size_curr + size_add);

                ArrayList<CommandGet> al = new ArrayList<CommandGet>(cmd.tmChilds.values());

                question(1, al, waitTime, pauseTime, countSql, idPribor, number);

                // Проверяем, есть ли сдвинутые команды
                al = getNoequalsCmds(al);

                if (!al.isEmpty()) {

                    if (B_LOG_TXD) {

                        setLoggerInfo("Профиль сдвинут !", null);

                    }

                    question(1, al, waitTime, pauseTime, countSql, idPribor, number);

                }

                // если все хорошо то удаляем родительскую как пройденую
                removeCommandInGroup(idPribor, cmd.name);

// Предварительно сохраняем данные...
                firstSaveValue(idPribor, cmd);

                if (cmd.bEmpty) {

                    // Для пустой команды ответ формируем после ответа подчиненных
                    createCommandExt(CMD_A, cmd);
                    createResult(cmd);

                    // Удаляем команду как пройденную
                    removeCommandInGroup(idPribor, cmd.name);
                    // Предварительно сохраняем данные...

                    firstSaveValue(idPribor, cmd);

                }

            }

        }

    }


    private void questionPack(int typOper, List<CommandGet> listCmd, int waitTime, int pauseTime, int countSql, Integer idPribor, Integer number) throws Exception {

        CommandGet cmd = null;


        //  CheckBuffer buffer = new CheckBuffer();
        // new Thread(buffer).start();


        //JsscSerialPort.SerialPortReader.CheckBuffer buffer=new

        ((Port) serialPort).startCheckBuffer(idPribor);

        //doSendPack(listCmd);

        for (int i = 0; i < listCmd.size(); i++) {

            cmd = listCmd.get(i);
            cmd.number = number;

            number++;

            cmd.errorCmd = null;

            if (((Port) serialPort).isStop()) {
                cmd.errorCmd = "Остановка запроса данных!";
                throw new ErrorCommandException(cmd);
            }

            createCommandExt(CMD_Q, cmd);

            //   serialPort.doSendPack(cmd, false);

            ((Port) serialPort).doSendPack(cmd, false);

        }


        // Port port = (Port) serialPort;

        //  ((Port) serialPort).checkTimeAnsver();

        // port.checkTimeAnsver();

        // timer=new Timer();
        // Ждем ответа
        // timer.schedule(new Port.WatchTask(),7000);


    }

    public void addPointInBlackList(Integer idPoint, String error) {

        List<Integer> objects = hmChannels.get(idPoint);

        for (Integer obj : objects) {

            addCmdInBlackList(obj, error, 1);

        }


    }

    /**
     * @param idPribor -id объекта учета
     * @param error    -название ошибки
     */
    public void addCmdInBlackList(Integer idPribor, String error, Integer count) {

        Object blackcount = 1;

        if (count == 1) {

            blackHash.put(idPribor, error);
            return;
        }


        if (blackHash.containsKey(idPribor)) {

            blackcount = blackHash.get(idPribor);


            if (blackcount instanceof Integer) {

                blackcount = (Integer) blackcount + 1;


                if ((Integer) blackcount >= count) {

                    blackcount = error;
                }
            }
        }
        blackHash.put(idPribor, blackcount);
    }


    public String isBlackList(Integer idPribor) {

        String result = null;


        if (blackHash.containsKey(idPribor)) {

            Object blackcount = blackHash.get(idPribor);

            if (blackcount instanceof String) {
                result = (String) blackcount;
            }

        }


        return result;

    }


    /**
     * Выполнение команд
     *
     * @param typOper   -тип операции (0 простая  1- пакетная
     * @param waitTime  -время ожидания ответа
     * @param pauseTime -пауза между командами
     * @param countSql  -количество попыток дозвона
     * @param number    -начальный номер команды
     * @return возвращает команду с ошибкой
     */
    private CommandGet question(int typOper, List<CommandGet> listCmd, int waitTime, int pauseTime, int countSql, Integer idPribor, Integer number) throws Exception {


        CommandGet result = null;

        int countError = 0; //счетчик подряд не ответивших команд

        if (bitSetFlags.get(ValuesByChannel.BSF_BUFER_PACK))
        //Пакетный режим

        {

            questionPack(typOper, listCmd, waitTime, pauseTime, countSql, idPribor, number);


            // Если пакетный режим, ждем отправки всех команд
            block.suspend();


            return result;
        }


        CommandGet cmd = null;

        errorString = null;

        for (int i = 0; i < listCmd.size(); i++) {

            cmd = listCmd.get(i);
            cmd.number = number;

            number++;
            // Наличие данных по групповому  запросу
            if (idPribor != null) {

                if (cmd.result != null) {
                    continue;
                }

            }

            cmd.errorCmd = null;

            if (((Port) serialPort).isStop()) {
                cmd.errorCmd = "Остановка запроса данных!";
                throw new ErrorCommandException(cmd);
            }

            cmd.waitTime = waitTime;
            cmd.sleepTime = pauseTime;
            cmd.countTry = countSql;

            // Выполняем какие либо функции до исполнения команды
            //   if (typOper == CMD_GET) {
            invokeMetod(cmd, GO_BEFORE);
            //  }

            cmd = listCmd.get(i);

            // Команда записи или Чтения
            //   setWorR(cmd, typOper);
            // Формируем запрос
            createCommandExt(CMD_Q, cmd);

            // Ищем команду в повторах
            String sRepeat = cmd.alSend.toString();
            String s = getAllName(cmd);

            // Если  не пакетная передача данных
            // if (!bitSetFlags.get(ValuesByChannel.BSF_BUFER_PACK) || cmd.onlyOne) {

            refreshBarValue(s);
            refreshBarValue(number);
            // }

            if (B_LOG_TXD) {

                setLoggerInfo("-------------------------------------------------", null);
                setLoggerInfo(s + " [W-" + cmd.waitTime + ", P-" + cmd.sleepTime + ", C-" + cmd.countTry + "]", null);
                setLoggerInfo("Флаги:" + bitSetFlags.toString(), null);
                setLoggerInfo("TxD [" + MathTrans.getNexStrByList(cmd.alSend, " ") + "]", null);

                Timestamp ts = (Timestamp) cmd.getProperty("value_date");

                if (ts != null) {

                    DateTime dt = new DateTime(ts.getTime());

                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

                    setLoggerInfo("Дата : " + dt.toString(dtf), null);

                }

            }

            if (hmRepeat != null && hmRepeat.containsKey(sRepeat)) {
                // Такой запрос уже был

                CommandGet repeatCmd = hmRepeat.get(sRepeat);

                cmd.alResult = repeatCmd.alResult;
                cmd.errorCmd = null;

            } else {
// Команда еще не выполнялась

                if (hmBefore != null && hmBefore.containsKey(cmd)) {
                    // Есть команда выполняемая перед текущей

                    CommandGet cg = hmBefore.get(cmd);

                    ArrayList<CommandGet> al = new ArrayList<CommandGet>();

                    al.add(cg);
                    question(typOper, al, waitTime, pauseTime, countSql, idPribor, 0);

                }
                cmd.sResult = null;
                cmd.result = null;

                // Для команд типа профиль;
                if (cmd.name.equals("SetProfilPower")) {

                    if (hmChilds.containsKey(idPribor)) {


                        cmd.tmChilds = hmChilds.get(idPribor);

                    }

                }

                // Дозвон по GSM (набор номера);
                if (cmd.name.equals("DialGsm")) {

                    bitSetFlags.set(BS_GSM_GO);
                    blinkText(s);

                }

                // Счетчик подряд не ответившых команд

                // если несколько команд подряд не отвечают, то выходим

                // try {

                if (!cmd.bEmpty) {

// Посылаем команду...если она не пустая


                    errorString = null;

                    if (!((Port) serialPort).doSend(cmd)) {


                        // Команда не ответила

                        countError = countError + 1;

                        invokeMetod(cmd, GO_ERROR);
                        cmdError = cmd;

                        errorString = cmd.errorCmd;

                        if (cmd.name.equals("DialGsm")) {
                            stopBlinkText();
                        }

                        // result=cmd;

                        if (cmd.criticalError) { //для команд типа <открыть счетчик>

                            // добавляем в черный список и выходим

                            addCmdInBlackList(idPribor, errorString, 7);

                            //   mapMessageProcess.setInfoProcess(idPribor, MapMessageProcess.DATA_BLACK);
                            // setNotifyObservers(mapMessageProcess);

                            result = cmd;
                            return result;
                            // throw new ErrorCommandException(cmd);


                        }
                    } else {

                        //команда ответила
                        countError = 0;


                    }

                }


                if (countError >= 3) {

                    // Команды не ответили подряд выходим
                    result = cmd;
                    return result;

                }


                if (errorString != null) {
                    continue;
                }


                if (cmd.name.equals("DialGsm")) {
                    stopBlinkText();
                }

//Если раньше не отвечал  а ответил
                blackHash.remove(idPribor);


// Добавляем команду  в пройденные  если есть флаг использовать повторяющиеся команды

                if (hmRepeat != null && bitSetFlags.get(BSF_REPEAT_ON)) {

                    hmRepeat.put(sRepeat, cmd);
                }

                //           if (B_LOG_TXD) {
                //             setLoggerInfo("RxD [" + getNextString(cmd.alResult) + "]", null);
                //       }
                //  } finally {

                //  if (cmd.name.equals("DialGsm")) {
                //    stopBlinkText();
                // }
                // }

            }

            if (((Port) serialPort).isStop()) {
                cmd.errorCmd = "Остановка запроса данных!";

                blinkText("Остановка запроса данных...");

                throw new ErrorCommandException(cmd);

            }

            // Если пакетная передача данных
            if (bitSetFlags.get(ValuesByChannel.BSF_BUFER_PACK) && !cmd.criticalError) {

                // для подчиненных если есть
                questionChilds(cmd, listCmd, waitTime, pauseTime, countSql, idPribor, number);

                continue;
            }

            //Формируем результат
            if (cmd.errorCmd == null) {

                //   createCommandExt(CMD_A, cmd);
                try {

                    if (!cmd.bEmpty) {


                        createResultAndRemove(idPribor, cmd);

                        //   createCommandExt(CMD_A, cmd);
                        //  createResult(cmd);
                    }
                    // if (cmd.tmChilds == null || cmd.tmChilds.isEmpty()) {
                    // Удаляем команду как пройденную
                    //   removeCommandInGroup(idPribor, cmd.name);
                    // Предварительно сохраняем данные...

                    // firstSaveValue(idPribor, cmd);

                    // }
                    // createResultOld(cmd);
                } catch (Exception ex) {

                    //  answerProcess(cmd.errorCmd, ProcLogs.MSG_ERROR);
                    // Если ошибка
                    invokeMetod(cmd, GO_ERROR);

                    errorString = cmd.errorCmd;

                    refreshBarValue(cmd.errorCmd);

                    if (ex instanceof ErrorCommandException) {

                        throw new ErrorCommandException(cmd);
                    } else {
                        throw new Exception(ex);
                    }

                }

                if (B_LOG_TXD) {

                    setLoggerInfo("RxD [" + MathTrans.getNexStrByList(cmd.alResult, " ") + "]", null);
                    setLoggerInfo("Результат:" + cmd.sResult, null);

                }

                if (typOper == CMD_GET) {
                    // метод после выполнения
                    invokeMetod(cmd, GO_AFTER);
                }

                //  есть подчиненные  команды
                if (cmd.tmChilds != null && !cmd.tmChilds.isEmpty()) {

                    Object o = cmd.tmChilds.values().iterator().next();

                    if (o instanceof CommandGet) {

                        int size_curr = listCmd.size();

                        int size_add = cmd.tmChilds.size();

                        // с учетом подчиненных команд
                        setMinMaxValue(0, size_curr + size_add);

                        ArrayList<CommandGet> al = new ArrayList<CommandGet>(cmd.tmChilds.values());

                        if (listFull != null) {
                            setByt17(idPribor, al);
                        }


                        result = question(typOper, al, waitTime, pauseTime, countSql, idPribor, number);

                        // Проверяем, есть ли сдвинутые команды
                        al = getNoequalsCmds(al);

                        if (!al.isEmpty()) {

                            if (B_LOG_TXD) {

                                setLoggerInfo("Профиль сдвинут !", null);

                            }

                            question(typOper, al, waitTime, pauseTime, countSql, idPribor, number);

                        }

                        // если все хорошо то удаляем родительскую как пройденую
                        removeCommandInGroup(idPribor, cmd.name);

// Предварительно сохраняем данные...
                        firstSaveValue(idPribor, cmd);

                        if (cmd.bEmpty) {

                            // Для пустой команды ответ формируем после ответа подчиненных
                            createCommandExt(CMD_A, cmd);
                            createResult(cmd);

                            // Удаляем команду как пройденную
                            removeCommandInGroup(idPribor, cmd.name);
                            // Предварительно сохраняем данные...

                            firstSaveValue(idPribor, cmd);

                        }

                    }

                }

                // Вывод на экран
                setNotifyObservers(cmd);

                if (typOper == CMD_SET) {

                    // если запись
                    Element e = hmCommansTree.get(cmd.name);

                    if (e != null) {
                        e.setAttribute("update", "");
                    }
                }
            } else {
                // есть ошибка

                //   setNotifyObservers(cmd);
            }
            //end for
        }
        return result;
    }

    /**
     * Формирует результат и удаляет из пройденных
     * если результат равен null то оставляем
     *
     * @param idPribor
     * @param cmd
     */
    public void createResultAndRemove(Integer idPribor, CommandGet cmd) throws Exception {


        createCommandExt(CMD_A, cmd);
        createResult(cmd);

        if (cmd.tmChilds == null || cmd.tmChilds.isEmpty()) {
            // Удаляем команду как пройденную
            removeCommandInGroup(idPribor, cmd.name);
            // Предварительно сохраняем данные...

            firstSaveValue(idPribor, cmd);

        }


    }

    /**
     * Устанавливает параметры порта rs485 перед запросом данных
     *
     * @return
     */
    private CommandGet setParamPort228(Integer idController) {

        if (setController.contains(idController) || bController) {
// если небыло уставок порта или опрашивается чисто контроллер

            return null;

        }

        CommandGet result = hmModem.get("SetParamPort228");

        if (result == null) {

            return result;
        }

        result.alSet = new ArrayList<>();

        // тип пакета
        result.alSet.add(1);

//    hmProperty
        int baud_rate = (int) hmProperty.get("ctr_baud_rate");
        int byte_size = (int) hmProperty.get("ctr_byte_size");
        int stop_bits = (int) hmProperty.get("ctr_stop_bits");
        //  int ctr_parity = (int) hmProperty.get("ctr_parity");
        int ctr_wait = (int) hmProperty.get("ctr_wait");
        int ctr_pause = (int) hmProperty.get("ctr_pause");

        int uart = MathTrans.getUART(baud_rate, byte_size, stop_bits);
        result.alSet.add(uart);

        int wait = MathTrans.getWAIT(ctr_wait);
        result.alSet.add(wait);

        result.alSet.add(ctr_pause);
        return result;
    }

    /**
     * Список команд по запросу
     * <p>
     * lWhere-Куда
     */
    public void getCommandByCroup(String sql,
                                  List<CommandGet> lWhere) {

        HashMap<String, Object> hm;
        CommandGet command;
        lWhere.clear();
        try {
            ResultSet rs = SqlTask.getResultSet(null, sql);

            try {
                while (rs.next()) {

                    int r = rs.getInt("c_id");
                    command = new CommandGet();
                    hm = new HashMap<String, Object>();
                    SqlTask.addParamToMap(rs, hm);
                    String nameCom = (String) hm.get("c_name");

                    if (nameCom == null) {
                        nameCom = "";
                    }

                    if (nameCom.isEmpty()) {
                        nameCom = "nameCom" + r;
                        hm.put("c_name", nameCom);
                    }

                    command.setHmPar(hm);
                    lWhere.add(command);
                }

            } finally {

                rs.getStatement().close();
            }

        } catch (SQLException ex) {
            setNotifyObservers(ex);
        }

    }

    /**
     * Список команд по группе счетчика hmFrom -Откуда lWhere-Куда
     */
    public static void getCommandByCroup(String NameGroup, String Value,
                                         Map<String, CommandGet> hmFrom, List<CommandGet> lWhere) {

        lWhere.clear();
        String gr = null;

        for (CommandGet cmd : hmFrom.values()) {
            gr = (String) cmd.getProperty(NameGroup);
            if (Value.equals(gr)) {
                lWhere.add(cmd);
            }

        }

    }

    public synchronized void sendSMS(String msg, String phone, String port) throws Exception {

        try {


            String namePort = null;


            if (port != null) {

                String[] namesPorts = jssc.SerialPortList.getPortNames();


                for (int i = 0; i < namesPorts.length; i++) {


                    if (namesPorts[i].contains(port)) {

                        namePort = namesPorts[i];


                        break;
                    }

                }


            }

            ParamPort paramPort = new ParamPort(hmPoint, HM_LOCALE, namePort);


            //  String namePort = getFreeGsmPort(paramPort);

            if (namePort == null) {
                return;
            }


            if (!serialPort.openPort(paramPort)) {
                return;
            }

            if (phone.startsWith("8")) {

                phone = phone.replaceFirst("8", "+7");

            }

            String ucs2msg = MathTrans.convertToUCS2(msg);

            String ucs2tlf = MathTrans.convertToUCS2(phone);

            //  String ucs2tlf = MathTrans.reversePhone(phone);


            alRun.clear();

            //CommandGet command = hmModem.get("GetCommandOff");
            //alRun.add(command);


            CommandGet command = hmModem.get("at_csms");
            alRun.add(command);

            command = hmModem.get("at_cmgf1");
            alRun.add(command);

            command = hmModem.get("at_csca_v");
            alRun.add(command);

            command = hmModem.get("at_csmp");
            alRun.add(command);

            command = hmModem.get("at_cscs");
            alRun.add(command);

            // телефонный номер отправителя
            command = hmModem.get("at_cmgs");

            command.putProperty("sms_phone", ucs2tlf);
            alRun.add(command);

            command = hmModem.get("at_send_sms");
            command.putProperty("sms_msg", ucs2msg);

            alRun.add(command);

            question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, 0);

        } finally {
            serialPort.closePort();
        }
    }

    /*
     * Показывает баланс модема
     */
    public void showBalance() {

        try {
            try {

                ParamPort paramPort = new ParamPort(hmPoint, HM_LOCALE, null);


                if (!serialPort.openPort(paramPort)) {
                    return;
                }
            } catch (Exception ex) {
                setLoggerInfo(sqlTable, ex);
            }

            //  createModemCommands();
            alRun.clear();

            CommandGet command = hmModem.get("CheckModem");
            alRun.add(command);

            // command = hmModem.get("GetCommandOff");
            // alRun.add(command);
            command = hmModem.get("BalanseGsm");

            alRun.add(command);
            try {
                question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, 0);
            } catch (Exception ex) {

                setLoggerInfo("Запрос", ex);
                return;

            }

            String balanse = command.sResult.toString();

//+CUSD: 2,"Balance:393,38r
            if (balanse != null) {

                String msg = "<html><h4><Баланс SIM  карты</h4><hr>"
                        + "<h3><FONT COLOR=#ff0000>" + balanse + "</FONT><h3></html>";

                JOptionPane.showMessageDialog(null, msg, "Баланс", 1, null);

            }

        } finally {
            try {
                serialPort.closePort();
            } catch (Exception ex) {
                setLoggerInfo("Закрытие порта", ex);
            }
        }

    }

    // Установка формата вывода данных на монитор для каждого байта ответа
    public void setFormatByByte(String format, CommandGet cmd) {

        Object[] ints = cmd.alAnsRes.toArray();
        cmd.sResult = String.format(format, ints);

    }

    // Установка формата вывода данных на монитор
    public void setFormat(String format, CommandGet cmd) {

        cmd.sResult = String.format(format, cmd.result);

    }

    private static CommandGet findCommandByName(String NameComm, List<CommandGet> lFrom, int TypFind) {

//command0l2
        if (TypFind == TF_ALL) {

            for (CommandGet cmd : lFrom) {
                if (cmd.name.equals(NameComm)) {
                    return cmd;
                }
            }
        }

        if (TypFind == TF_PART) {// Частично

            for (CommandGet cmd : lFrom) {
                if (cmd.name.indexOf(NameComm) != -1) {
                    return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Добавление команд в запрос перед командой command
     *
     * @param nameCmd - Имя добавляемой команды
     */
    public boolean addFirstCmd(String nameCmd, CommandGet command) {

        Boolean result = false;
        // CommandGet command1 = findCommandByName(nameCmd, alAddition, TF_ALL);
        CommandGet command1;

        command1 = findCommand(nameCmd);

        //   int idx = alRun.indexOf(command);
        if (command1 != null) {

            result = true;
            if (!alAddition.contains(command1.name)) {
                alAddition.add(command1.name);
            }
        }


        return result;
    }

    /**
     * Добавление команд в запрос перед командой command
     *
     * @param nameCmd - Имя добавляемой команды
     */
    public void addBeforeCmd(String nameCmd, CommandGet command) {

        // CommandGet command1 = findCommandByName(nameCmd, alAddition, TF_ALL);
        CommandGet command1;

        command1 = findCommand(nameCmd);

        if (command1 != null) {

            if (hmBefore == null) {

                hmBefore = new HashMap<CommandGet, CommandGet>();
            }

            hmBefore.put(command, command1);

        }

    }

    private Object[] getLastRecordProfilCET4TM(DateTime dt, CommandGet command) {

        // Старший байт
        int sh = command.alResult.get(6);
        // младший байт
        int sl = command.alResult.get(7);

        // адрес последней записи
        Integer iResult = sh * 256 + sl;

        //  iResult = iResult * 0x10;
        // Часы
        Integer ss = command.alResult.get(2);
        String sHour = Integer.toString(ss, 16);
        dt = dt.hourOfDay().setCopy(sHour);

        // минуты
        ss = command.alResult.get(1);

        if (ss > 0x59) {

            int s = 63;
            int s1 = ss;
            int s3 = s1 & s;
            ss = (int) s3;
        }

        String sMinute = Integer.toString(ss, 16);
        dt = dt.minuteOfHour().setCopy(sMinute);

        // Число
        ss = command.alResult.get(3);
        String sDay = Integer.toString(ss, 16);
        dt = dt.dayOfMonth().setCopy(sDay);

        // Месяц
        ss = command.alResult.get(4);
        String sMonth = Integer.toString(ss, 16);
        dt = dt.monthOfYear().setCopy(sMonth);

        // Год
        ss = command.alResult.get(5);
        String sYear = Integer.toString(ss, 16);
        dt = dt.yearOfCentury().setCopy(sYear);

        //Округляем минуты и секунды до получасовок
        int min = dt.getMinuteOfHour();
        if (min >= 30) {
            min = 30;
        } else {
            min = 0;
        }

        dt = dt.minuteOfHour().setCopy(min);
        dt = dt.secondOfMinute().setCopy(0);
        dt = dt.millisOfSecond().setCopy(0);

        DateTime dResult = dt;

        return new Object[]{iResult, dResult};
    }

    /**
     * Установка времени ожидания ответа команды
     *
     * @param WaitTime - Время ожидания в милисекундах
     * @param command  -текущая команда
     */
    public void setWaitTime(Integer WaitTime, CommandGet command) {

        command.waitTime = WaitTime;

    }

    /**
     * Устанавливаем параметры команды перед запросом профиля мощности
     *
     * @param command
     */
    public void setParamCmdPP(CommandGet command) {
    }

    /**
     * Добавляем Значения в дерево результатов (для одиночного опроса)
     */
    private void setValueInTree(Element element, Object valuesCmd) {

        String value;

        if (valuesCmd instanceof ArrayList) {

            ArrayList<Object> al = (ArrayList<Object>) valuesCmd;
            String sValues = "";

            String sVal = "";
            for (Object val : al) {

                if (val instanceof Number) {

                    sVal = String.format("%9.3f", (Number) val);

                } else {

                    sVal = val.toString();

                }

                sValues = sValues + sVal + ";";

            }

            sValues = sValues.substring(0, sValues.length() - 1);

            value = sValues;

        } else {

            value = (String) valuesCmd;

        }

        element.setAttribute("update", "");
        element.setAttribute("value", value);

    }

    /**
     * Устанавливаем значения из базы данных
     */
    private void setValueFromBase(int idPribor, CommandGet commandGet) {
        HashMap<String, Object> hmValues;
        String sDate;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        hmValues = hmBaseValue.get(idPribor);

        Element element;

        if (hmValues == null) {
            return;
        }

        String nameCmd = commandGet.name;
        dt = (DateTime) commandGet.getProperty("cmd_date");

        if (dt != null) {
            sDate = dt.toString(dtf);
            nameCmd = nameCmd + "//" + sDate;
        }

        if (hmValues.containsKey(nameCmd)) {

            if (commandGet.tmChilds != null && !commandGet.tmChilds.isEmpty()) {
                // Для групповых команд

                HashMap<DateTime, ArrayList<Object>> hmValCmd;
                hmValCmd = (HashMap<DateTime, ArrayList<Object>>) hmValues.get(nameCmd);

                LinkedHashMap<DateTime, CommandGet> hmComm;
                hmComm = commandGet.tmChilds;
                for (DateTime dateTime : hmComm.keySet()) {

                    Object object = hmValCmd.get(dateTime);

                    //   CommandGet cg = hmComm.get(dateTime);
                    if (object != null) {

                        CommandGet cg = hmComm.get(dateTime);

                        cg.result = object;
                        cg.bValueBase = true;

                    } else {
                        //     cg.bValueBase = false;
                        //   cg.result = null;
                    }

                }

            } else {
                Object object = hmValues.get(nameCmd);
                commandGet.result = object;
                commandGet.bValueBase = true;

                element = hmCommansTree.get(commandGet.name);

                if (element != null) {

                    element.setAttribute("base", "1");
                }

            }
        }

    }

    /**
     * Формируем профиль тока и напряжения за день
     *
     * @param command
     */
    public void createProfilVAndCDay(CommandGet command) {

        // Количество записей в текущем дне
        Integer countRec = (Integer) command.result;

        CommandGet commandGet = hmCommands.get("GetArxiveCutN");

        command.tmChilds = new LinkedHashMap();

        for (int i = 1; i <= countRec; i++) {

            CommandGet commandClon = (CommandGet) commandGet.clone();
            commandClon.alSet = new ArrayList<Integer>();
            commandClon.alSet.add(i);
            command.tmChilds.put(i, commandClon);

        }

        //  GetArxiveCutN
    }

    public String getNextString(List<Integer> al) {

        String result = "";

        if (al == null) {
            return result;
        }

        for (int s : al) {
            result = result + Integer.toHexString(s).toUpperCase() + " ";
        }

        return result;

    }

    /**
     * ID Объекта и Адрес счетчика из дерева запроса
     *
     * @return
     */
    private Integer getAddressCounter() {

        Integer result = -1;

        Element element = hmCommansTree.get("ListCountersCont");

        HashMap<String, String> hmValues = Work.getMapAttributes(element);

        String sName = element.getAttribute("value");
        String name_counters = element.getAttribute("name_counters");

        Map mapCounters = dbf.Work.getMapByJsonString(name_counters);

        // String value=parent.get("value");
        for (Object serial : mapCounters.keySet()) {

            String value = (String) mapCounters.get(serial);

            if (value.equals(sName)) {
                result = Integer.parseInt((String) serial);
                break;
            }

        }

        return result;

    }

    /**
     * Создаем профиль тока и напряжения
     *
     * @param command
     */
    public void createProfilVoltAndCur(CommandGet command) {

        // Адрес текущего счетчика
        int id = getAddressCounter();

        if (id == -1) {

            JOptionPane.showMessageDialog(null, "Не выбран текущий счетчик !");

            ((Port) serialPort).stop();
            return;
        }

        // Вытаскиваем дату профиля
        DateTime dateTime;
        DateTime dateTimeLast;
        DateTime dateTimeFirst;

        Element element = hmCommansTree.get(command.name);

        CommandGet command1;
        CommandGet commClon;

        String sValue = element.getAttribute("value");

        String[] dates = sValue.split("-");

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");

        dateTimeFirst = dtf.parseDateTime(dates[0]);
        dateTimeLast = dtf.parseDateTime(dates[1]);

        dateTimeLast = dateTimeLast.plusDays(1);

        DateTime dtCurrent = new DateTime();

        // Если текущая дата меньше чем запрашиваемая
        if (dtCurrent.isBefore(dateTimeLast)) {

            //Округляем минуты и секунды до получасовок
            int min = dtCurrent.getMinuteOfHour();
            if (min > 30) {
                min = 30;
            } else {
                min = 0;
            }

            dtCurrent = dtCurrent.minuteOfHour().setCopy(min);
            dtCurrent = dtCurrent.secondOfMinute().setCopy(0);
            dtCurrent = dtCurrent.millisOfSecond().setCopy(0);

            dateTimeLast = dtCurrent;

        }

        command.putProperty("dateTimeFirst", dateTimeFirst);
        command.putProperty("dateTimeLast", dateTimeLast);
        command.putProperty("nameTable", "profil_cur_volt");

        // Количество Дней
        Days days = Days.daysBetween(dateTimeFirst, dateTimeLast);

        int countRec = days.getDays();

        command.tmChilds = new LinkedHashMap();

        CommandGet commandGet = hmCommands.get("OpenArhiveSetDay");

        command.alSet = new ArrayList();
        command.alSet.add((int) id);

        int day = (int) dateTimeFirst.dayOfMonth().get();
        int month = (int) dateTimeFirst.monthOfYear().get();
        int yer = (int) dateTimeFirst.yearOfCentury().get();

        command.alSet.add(day);
        command.alSet.add(month);
        command.alSet.add(yer);

        for (int i = 0; i <= countRec; i++) {

            CommandGet comClon = (CommandGet) commandGet.clone();
            comClon.alSet = new ArrayList<Integer>();

            comClon.alSet.add((int) id);

            day = (int) dateTimeFirst.dayOfMonth().get();
            month = (int) dateTimeFirst.monthOfYear().get();
            yer = (int) dateTimeFirst.yearOfCentury().get();

            comClon.alSet.add(day);
            comClon.alSet.add(month);
            comClon.alSet.add(yer);
            command.tmChilds.put(dateTimeFirst, comClon);
            dateTimeFirst = dateTimeFirst.plusDays(1);

        }

    }

    /**
     * Добавление команды пари несовпадении даты полученного и запрошенного
     * значений
     *
     * @param listCmd - несовпадающая команды
     */
    private ArrayList<CommandGet> getNoequalsCmds(List<CommandGet> listCmd) {

        ArrayList<CommandGet> result = new ArrayList<>();

        for (CommandGet command : listCmd) {

            if (!command.hmPar.containsKey("noequals")) {
                continue;
            }

            CommandGet cmdParemt = (CommandGet) command.getProperty("ParentCmd");

            // время полученое
            DateTime dateTimeRes = (DateTime) command.getProperty("noequals");

            // время нужное
            Timestamp timestamp = (Timestamp) command.getProperty("value_date");

            DateTime dateTimeCmd = new DateTime(timestamp.getTime());

            // Количество получасовок
            Minutes minutes = Minutes.minutesBetween(dateTimeRes, dateTimeCmd);

            int countRec = minutes.getMinutes() / 30;

            DateTime dateTimeFirst = dateTimeCmd;

            CommandGet commClon;

            String name = command.name;

            for (int i = 0; i <= countRec; i++) {

                // commClon = (CommandGet) command.clone();
                if (!cmdParemt.tmChilds.containsKey(timestamp)) {
                    commClon = (CommandGet) command.clone();
                    result.add(commClon);
                    // cmdParemt.tmChilds.put(timestamp, commClon);
                    commClon.alSet = new ArrayList();
                    commClon.putProperty("value_date", timestamp);
                    //  commClon.bChild = true;
                    commClon.result = null;
                    commClon.name = name + "_add_" + i;
                    commClon.putProperty("ParentCmd", cmdParemt);

                }

                dateTimeFirst = dateTimeFirst.plusMinutes(30);
                timestamp = new Timestamp(dateTimeFirst.getMillis());

            }
        }
        return result;

    }

    /**
     * Создаем предварительно команды для профиля мощности
     *
     * @param command
     */
    public void createFirstCmdPP(CommandGet command, Integer idObject) {

        // Вытаскиваем дату профиля
        DateTime dateTime;
        DateTime dateTimeLast = null;
        DateTime dateTimeFirst = null;
        DateTime dtCurrent;
        JXDatePicker picker;

        CommandGet command1;
        CommandGet commClon;

        // По расписанию всегда за текущий месяц минус 2 дня
        if (typRegime == REJIM_GET_SCHEDULE) {

            dateTimeFirst = new DateTime().millisOfDay().setCopy(0);

            //количество прошедших дней месяца

            int montLast = dateTimeFirst.getDayOfMonth();


            dateTimeFirst = dateTimeFirst.minusDays(2 + montLast);

            dateTimeLast = new DateTime().millisOfDay().setCopy(0);
            dateTimeLast = dateTimeLast.minusDays(1);

        } else if (typRegime == REJIM_GET_SQL) {

            // По текущей таблице скопом
            picker = (JXDatePicker) getProperty("dataFirst");
            Date dateF = picker.getDate();
            dateTimeFirst = new DateTime(dateF.getTime());

            picker = (JXDatePicker) getProperty("dataLast");
            Date dateL = picker.getDate();
            dateTimeLast = new DateTime(dateL);

        } else {

            // Если одиночный запрос по дереву
            Element element = hmCommansTree.get(command.name);

            if (element != null) {
                String sValue = element.getAttribute("value");

                String[] dates = sValue.split("-");

                DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");

                dateTimeFirst = dtf.parseDateTime(dates[0]);
                dateTimeLast = dtf.parseDateTime(dates[1]);

            }
        }

        dateTimeLast = dateTimeLast.plusDays(1);
        dtCurrent = new DateTime();

        // Если текущая дата меньше чем запрашиваемая
        if (dtCurrent.isBefore(dateTimeLast)) {

            //Округляем минуты и секунды до получасовок
            int min = dtCurrent.getMinuteOfHour();
            if (min > 30) {
                min = 30;
            } else {
                min = 0;
            }

            dtCurrent = dtCurrent.minuteOfHour().setCopy(min);
            dtCurrent = dtCurrent.secondOfMinute().setCopy(0);
            dtCurrent = dtCurrent.millisOfSecond().setCopy(0);

            // Вычитаем последние 2 ч.(Бывает последние получасовки инициал. 0)
            dtCurrent = dtCurrent.minusHours(2);
            dateTimeLast = dtCurrent;

        }
        // Первая получасовка  00:30 (00-30)
        dateTimeFirst = dateTimeFirst.plusMinutes(30);
        command.putProperty("dateTimeFirst", dateTimeFirst);
        command.putProperty("dateTimeLast", dateTimeLast);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        LocalDateTime ldtFirst = LocalDateTime.fromDateFields(dateTimeFirst.toDate());
        LocalDateTime ldtLast = LocalDateTime.fromDateFields(dateTimeLast.toDate());

        if (B_LOG_TXD) {
            setLoggerInfo("Профиль с " + ldtFirst.toString(dtf) + " по " + ldtLast.toString(dtf), null);
        }

        // Количество получасовок
        Minutes minutes = Minutes.minutesBetween(dateTimeFirst, dateTimeLast);

        int countRec = minutes.getMinutes() / 30;

        CommandGet cmdProfil; //P+

        // Запрос на чтение информации по физ адресам
        cmdProfil = hmCommands.get("ProfilPower");

        String name = cmdProfil.name;

        command.result = null;

        if (hmChilds == null) {

            hmChilds = new HashMap<>();

        }

        LinkedHashMap<Timestamp, CommandGet> tmChilds = new LinkedHashMap();

        hmChilds.put(idObject, tmChilds);

        command.tmChilds = tmChilds;

        command.result = null;
        // command.bChild = true;
        // Создаем команды и устанавливаем дату
        //  добавляем P+ P- Q+ Q-
        // Object[] parCmd;

        String nameTable;

        //  parCmd = getParamByCmdName(cmdProfil.name);
        //  nameTable = (String) parCmd[0];
        // command.putProperty("nameTable", nameTable);
        // command.putProperty("paramChild", parCmd);
        command.putProperty("nameChild", "ProfilPower");

        String nameCol;

        //dtf = DateTimeFormat.forPattern("ddMMyyyyHHmm");
        Timestamp timestamp = new Timestamp(dateTimeFirst.getMillis());

        for (int i = 0; i <= countRec; i++) {

            //P+P-Q+Q-
            commClon = (CommandGet) cmdProfil.clone();
            //  parCmd = getParamByCmdName(commClon.name);
            tmChilds.put(timestamp, commClon);
            commClon.alSet = new ArrayList();
            commClon.putProperty("value_date", timestamp);
            //  commClon.bChild = true;
            commClon.result = null;
            commClon.name = name + "_" + i;
            commClon.putProperty("ParentCmd", command);
            dateTimeFirst = dateTimeFirst.plusMinutes(30);
            timestamp = new Timestamp(dateTimeFirst.getMillis());

        }

    }

    /**
     * Проверка видимости редактора или кнопки ячейки
     */
    public void checkVisibleCellTree() {

        if (hmCommansTree == null || hmCommansTree.isEmpty()) {
            return;
        }

        CommandGet command;

        Element element;

        // Журнал дискретных каналов
        command = hmCommands.get("JurnalDiscretEvends");
        element = hmCommansTree.get("JurnalDiscretEvends");

        if (command != null && element != null) {
            if (command.tmChilds != null && !command.tmChilds.isEmpty()) {

                element.setAttribute("visible1", "1");
                element.setAttribute("visible2", "1");

            } else {
                element.setAttribute("visible1", "0");
                element.setAttribute("visible2", "0");

            }
        }

        // Журнал превышений
        command = hmCommands.get("JurnalOwerUstav");
        element = hmCommansTree.get("JurnalOwerUstav");

        if (command != null && element != null) {
            if (command.tmChilds != null && !command.tmChilds.isEmpty()) {

                element.setAttribute("visible1", "1");
                element.setAttribute("visible2", "1");

            } else {
                element.setAttribute("visible1", "0");
                element.setAttribute("visible2", "0");

            }
        }

        // Таблица профиля
        command = hmCommands.get("SetProfilPower");
        element = hmCommansTree.get("SetProfilPower");

        if (command != null && element != null) {
            if (command.tmChilds != null && !command.tmChilds.isEmpty()) {

                element.setAttribute("visible2", "1");
            } else {
                element.setAttribute("visible2", "0");
            }
        }

        //   ListCounters
        // Список счетчиков
        command = hmCommands.get("ListCounters");
        element = hmCommansTree.get("ListCounters");

        if (command != null && element != null) {
            if (command.tmChilds != null && !command.tmChilds.isEmpty()) {

                element.setAttribute("visible1", "1");
                element.setAttribute("visible2", "1");
            } else {
                element.setAttribute("visible1", "0");
                element.setAttribute("visible2", "0");
            }
        }

        // Профиль тока и напряжения
        command = hmCommands.get("ProfVoltAndCurr");
        element = hmCommansTree.get("ProfVoltAndCurr");

        if (command != null && element != null) {
            if (command.tmChilds != null && !command.tmChilds.isEmpty()) {

                element.setAttribute("visible2", "1");
            } else {
                element.setAttribute("visible2", "0");
            }
        }

    }

    // Показать журнал превышений параметров
    public void viewJurnalOver() {

        StringBuilder builder = new StringBuilder();
        // DateTime dtCouter = (DateTime) command.get("DateProfile");
        CommandGet commandGet;

        DateTime dtFirst;
        DateTime dtLast;

        Double value;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        commandGet = hmCommands.get("JurnalOwerUstav");

        //  dtFirst = (DateTime) commandGet.get("dateTimeFirst");
        //  dtLast = (DateTime) commandGet.get("dateTimeLast");
        builder = new StringBuilder();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("ЖУРНАЛ ПРЕВЫШЕНИЙ");
        //    builder.append(" C ");
        //  builder.append(dtFirst.toString(dtf));
        //  builder.append(" ПО ");
        //  builder.append(dtLast.toString(dtf));

        builder.append("</H3>");
        builder.append("<TABLE BORDER=1 WIDTH=100%>");

// Заголовок
        builder.append("<TR>");

        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Адрес счетчика"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Дата и время"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Параметр"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Событие"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Значение"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Примечание"));

        builder.append("</TR>");

        String prim = "из базы";

        LinkedHashMap<Integer, CommandGet> tmChilds;

        tmChilds = commandGet.tmChilds;

        for (CommandGet cg : tmChilds.values()) {

            Object[] values = (Object[]) cg.result;

            builder.append("<TR>");

            builder.append("<TD>");
            builder.append(values[0].toString());
            builder.append("</TD>");

            DateTime time = (DateTime) values[1];
            String sDate = time.toString(dtf);

            builder.append("<TD>");
            builder.append(sDate);
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[2].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[3].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[4].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append("");
            builder.append("</TD>");

            builder.append("</TR>");

        }

        builder.append("</TABLE>");

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");

        String msg = builder.toString();

        ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }

    // Показать   дискретный журнал
    public void viewJurnalDiscret() {

        StringBuilder builder;
        // DateTime dtCouter = (DateTime) command.get("DateProfile");
        CommandGet commandGet;

        DateTime dtFirst;
        DateTime dtLast;

        Double value;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        commandGet = hmCommands.get("JurnalDiscretEvends");

        //  dtFirst = (DateTime) commandGet.get("dateTimeFirst");
        //  dtLast = (DateTime) commandGet.get("dateTimeLast");
        builder = new StringBuilder();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("ЖУРНАЛ ДИСКРЕТНЫХ КАНАЛОВ");
        //    builder.append(" C ");
        //  builder.append(dtFirst.toString(dtf));
        //  builder.append(" ПО ");
        //  builder.append(dtLast.toString(dtf));

        builder.append("</H3>");
        builder.append("<TABLE BORDER=1 WIDTH=100%>");

// Заголовок
        builder.append("<TR>");
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Дата и время"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Канал"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Событие"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Предыдущее состояние"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Текущее состояние"));
        builder.append(getHtmlCol("TH", null, Color.WHITE, colorBorder, "Примечание"));

        builder.append("</TR>");

        String prim = "из базы";

        LinkedHashMap<Integer, CommandGet> tmChilds;

        tmChilds = commandGet.tmChilds;

        for (CommandGet cg : tmChilds.values()) {

            Object[] values = (Object[]) cg.result;

            builder.append("<TR>");

            DateTime time = (DateTime) values[0];
            String sDate = time.toString(dtf);

            builder.append("<TD>");
            builder.append(sDate);
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[1].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[2].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[3].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(values[4].toString());
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append("");
            builder.append("</TD>");

            builder.append("</TR>");

        }

        builder.append("</TABLE>");

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");

        String msg = builder.toString();

        ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }

    // Показать   счетчики контроллера
    public void viewCounters() {

        StringBuilder builder;
        // DateTime dtCouter = (DateTime) command.get("DateProfile");
        CommandGet commandGet;

        DateTime dtFirst;
        DateTime dtLast;

        Double value;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        commandGet = hmCommands.get("ListCounters");

        //  dtFirst = (DateTime) commandGet.get("dateTimeFirst");
        //  dtLast = (DateTime) commandGet.get("dateTimeLast");
        builder = new StringBuilder();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("СПИСОК СЧЕТЧИКОВ");
        builder.append("</H3>");
        builder.append("<TABLE BORDER=1 WIDTH=100%>");

// Заголовок
        builder.append("<TR>");

        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Номер счетчика"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Примечание"));

        builder.append("</TR>");

        String prim = "";

        LinkedHashMap<Integer, String> tmChilds;

        tmChilds = commandGet.tmChilds;

        for (Integer icg : tmChilds.keySet()) {

            prim = tmChilds.get(icg);
            builder.append("<TR>");

            builder.append("<TD>");
            builder.append(icg);
            builder.append("</TD>");

            builder.append("<TD>");
            builder.append(prim);
            builder.append("</TD>");

            builder.append("</TR>");

        }

        builder.append("</TABLE>");

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");

        String msg = builder.toString();

        ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }

    /**
     * @param id_point -id присоединения
     * @return Список счетчиков под присоединением
     */
    public HashMap<Integer, String> getListCountersByPoint(Integer id_point) throws SQLException {

        HashMap<Integer, String> hmCounters = new HashMap<Integer, String>();

        String sql = "SELECT * FROM objects WHERE id_point=?";

        ResultSet rs = SqlTask.getResultSet(null, sql, new Object[]{id_point});
        try {

            HashMap<String, Object> hmProp;

            while (rs.next()) {

                hmProp = Work.getParametersObject(null, rs, true, false, true);
                Integer addresCount = (Integer) hmProp.get("counter_addres");
                String caption = (String) hmProp.get("caption");

                hmCounters.put(addresCount, caption);

            }
        } finally {
            rs.close();
        }
        return hmCounters;

    }

    //Создать список счетчиков
    public void createListCounters(CommandGet command) {

        CommandGet command2;
        CommandGet comClon;

        HashMap<String, Object> hmProp;

        // Количество счетчиков в контроллере
        HashMap<Integer, String> hmCountersCont = new HashMap<Integer, String>();

        // Количество счетчиков в базе
        HashMap<Integer, String> hmCountersDbf = new HashMap<Integer, String>();

        // Полный список счетчиков
        HashMap<Integer, String> hmAll = new HashMap<Integer, String>();

        Object idPoint = hmProperty.get("id_point");

        String sql = "SELECT id_object,id_point FROM objects WHERE id_point=?";
        try {
            Map<Object, Object> hmValues = SqlTask.getMapBySQL(null, sql, new Object[]{idPoint});

            for (Object id : hmValues.keySet()) {

                hmProp = Work.getParametersObject((Integer) id, null, true, false, true);
                Integer addresCount = (Integer) hmProp.get("counter_addres");

                if (addresCount == null) {

                    String msg = "Объект " + id + ". Не указан адрес счетчика !";

                    setLoggerInfo(msg, null);
                    continue;
                }

                String caption = (String) hmProp.get("caption");
                hmCountersDbf.put(addresCount, caption);

            }

        } catch (SQLException ex) {

            setLoggerInfo(sql, ex);
        }

        //   command1 = hmCommand.get("CountRecordsDiscret");
        command2 = hmCommands.get("ListCounters");

        String name = command2.name;

        List list = command.alResult;

        command.tmChilds = new LinkedHashMap();

        for (int i = 2; i < list.size() - 2; i++) {

            Integer s = (Integer) list.get(i);
            Integer is = s * 1;

            hmCountersCont.put(is, "-");
        }

        // Сравниваем
        String caption;

        //проверяем по контроллеру
        for (Integer id : hmCountersCont.keySet()) {

            if (hmCountersDbf.containsKey(id)) {
                caption = hmCountersDbf.get(id);
            } else {
                caption = "Нет в базе: Удалить...";

            }

            if (!hmAll.containsKey(id)) {
                hmAll.put(id, caption);
            }
        }

        //в контроллере меньше чем в базе
        for (Integer id : hmCountersDbf.keySet()) {
            if (hmCountersCont.containsKey(id)) {
                caption = hmCountersDbf.get(id);
            } else {
                caption = "Нет в контроллере: Добавить...";
            }

            if (!hmAll.containsKey(id)) {
                hmAll.put(id, caption);
            }

        }

        // Формируем список
        for (Integer id : hmAll.keySet()) {

            String cap = hmAll.get(id);
            command.tmChilds.put(id, cap);

        }

    }

    //Создать журнал дискретных каналов
    public void createJurnalDiscret(CommandGet command) {

        // CountRecordsDiscret
        CommandGet command2;
        CommandGet comClon;

        //   command1 = hmCommand.get("CountRecordsDiscret");
        command2 = hmCommands.get("OpenRecordDiscret");

        String name = command2.name;

        command.tmChilds = new LinkedHashMap();

        // Количество событий
        Integer count = (Integer) command.result;

        for (int i = 1; i < count; i++) {

            comClon = (CommandGet) command2.clone();
            //   comClon.name = "RecordOver";
            comClon.alSet = new ArrayList();
            comClon.alSet.add(i);
            //  alRun.add(comClon);
            command.tmChilds.put(i, comClon);
        }

    }

    // Установить значения превышений для счетчика
    public void setValueOver(CommandGet command) {

        String nameCmd = command.name;

        ArrayList<Object> alValues = (ArrayList<Object>) command.result;

        if (nameCmd.equals("UslEqualZero")) {

            // String[] formats=command.calcResult.split("//");
            // for(String items:formats){
            // String[] values=items.split("=");
            // }
            // Ток
            Object[] objects = (Object[]) alValues.get(0);

            Integer v = (Integer) objects[0];

            if (v == 1) {
                objects[1] = "Фиксировать";
            } else {
                objects[1] = "Не фиксировать";
            }

            // Напряжение
            objects = (Object[]) alValues.get(1);

            v = (Integer) objects[0];

            if (v == 1) {
                objects[1] = "Фиксировать";
            } else {
                objects[1] = "Не фиксировать";
            }

        } else {

            // Ток
            Object[] objects = (Object[]) alValues.get(0);

            Integer v = (Integer) objects[0];

            Double vd = v / 1000.0;

            objects[1] = String.format("%9.2f", vd);

            // Напряжение
            objects = (Object[]) alValues.get(1);

            v = (Integer) objects[0];

            vd = v / 100.0;

            objects[1] = String.format("%9.2f", vd);

        }

    }

    // создать журнал превышений параметров
    public void createJurnalOver(CommandGet command) {

        CommandGet command2;
        CommandGet comClon;

        Map map = command.tmChilds;

        List<Integer> alResult;

        for (Object cmd : map.values()) {

            comClon = (CommandGet) cmd;

            alResult = comClon.alResult;

        }

        command2 = hmCommands.get("OpenRecordOver");

        String name = command2.name;

        command.tmChilds = new LinkedHashMap();

        // Количество событий
        Integer count = (Integer) command.result;

        for (int i = 1; i < count; i++) {

            comClon = (CommandGet) command2.clone();
            comClon.alSet = new ArrayList();
            comClon.alSet.add(i);
            //    alRun.add(comClon);
            command.tmChilds.put(i, comClon);
        }
    }

    /**
     * Установка значений профиля
     *
     * @param command
     */
    public void showRowProfilPower(CommandGet command) {

        String name = command.name;

        CommandGet commandConst = hmCommands.get("ConsCouner");

        Integer constCount = (Integer) commandConst.result;

        DateTime dtCouter = (DateTime) command.getProperty("cmd_date");

        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

        // Часы
        Integer ss = command.alResult.get(2);
        String sHour = Integer.toString(ss, 16);
        dt = dt.hourOfDay().setCopy(sHour);

        // минуты
        ss = command.alResult.get(3);
        String sMinute = Integer.toString(ss, 16);
        dt = dt.minuteOfHour().setCopy(sMinute);

        // Число
        ss = command.alResult.get(4);
        String sDay = Integer.toString(ss, 16);
        dt = dt.dayOfMonth().setCopy(sDay);

        // Месяц
        ss = command.alResult.get(5);
        String sMonth = Integer.toString(ss, 16);
        dt = dt.monthOfYear().setCopy(sMonth);

        // Год
        ss = command.alResult.get(6);
        String sYear = Integer.toString(ss, 16);
        dt = dt.yearOfCentury().setCopy(sYear);

        //Округляем минуты и секунды до получасовок
        int min = dt.getMinuteOfHour();
        if (min >= 30) {
            min = 30;
        } else {
            min = 0;
        }

        dt = dt.minuteOfHour().setCopy(min);
        dt = dt.secondOfMinute().setCopy(0);
        dt = dt.millisOfSecond().setCopy(0);

        // DateTime time = (DateTime) command.getProperty("startDat");
        //  String sDate = time.toString(dtf);
        //  setLoggerInfo("Дата запроса : " + sDate, null);
        //  sDate = dt.toString(dtf);
        // setLoggerInfo("Дата значения : " + sDate, null);
// Дата из команды
        command.putProperty("cmd_date", dt);

        int sStatus = command.alResult.get(1);

        String prim = "";

        if (BitSetEx.isBitSet(sStatus, 1)) {

            prim = "Неполный срез";
        }
    }

    /**
     * Возвращает адрес записи
     *
     * @param dateTimeLast     -Дата последней записи
     * @param iAddressLast     -адрес после записи
     * @param dateTimeCurrent- Дата текущей записи
     * @param iStepTime        -шаг времени записи (мин.)
     * @param iMultiple        - кратность адреса
     * @return
     */
    private int getAddressRec(DateTime dateTimeLast, int iAddressLast, int iStepTime, int iMultiple, DateTime dateTimeCurrent) {
        int result = 0;

        // Количество получасовок
        Minutes minutes = Minutes.minutesBetween(dateTimeCurrent, dateTimeLast);

        int countRec = minutes.getMinutes() / iStepTime;

        // адрес   на дату текущей записи
        result = iAddressLast - (countRec * iMultiple);

        return result;
    }

    /**
     * Формируем профиль мощности
     */
    public void createProfilPower(CommandGet command) {

        // Вытаскиваем дату профиля
        Integer stepRec = 16; // Шаг записи
        Integer iLast = 0;
        DateTime dateTime = null;
        DateTime dateTimeLast;

        dateTimeLast = (DateTime) command.getProperty("dateTimeLast");

        ArrayList alRecord = null;
        // Последняя запись

        Object[] objects;

        if (modelPribor.contains("СЭТ-4ТМ")) {

            objects = getLastRecordProfilCET4TM(dateTimeLast, command);

            stepRec = 24;

        } else {

            alRecord = (ArrayList) command.result;
            iLast = (Integer) alRecord.get(0);
            dateTime = (DateTime) alRecord.get(1);
            stepRec = (Integer) alRecord.get(2);

        }
        // iLast = (Integer) objects[0];
        // dateTime = (DateTime) objects[1];

        if (iLast > 65536) {

            iLast = iLast - 65536;

        }

        // Старший байт
        int sh = 0;
        // младший байт
        int sl = 0;

        HashMap<String, CommandGet> hmCmd;

        LinkedHashMap<Timestamp, CommandGet> tmChilds;

        tmChilds = command.tmChilds;

        for (Timestamp tms : tmChilds.keySet()) {

            CommandGet cg = tmChilds.get(tms);

            DateTime dt = new DateTime(tms.getTime());

            cg.putProperty("parentResult", alRecord); // результат родительской команды

            cg.alSet.clear();

            cg.putProperty("startDat", dt);

            int iAddress = getAddressRec(dateTime, iLast, 30, stepRec, dt);

            if (iAddress < 0) {

                iAddress = getAddressRec(dateTime, iLast + 65536, 30, stepRec, dt);

            }

            sh = (int) (iAddress >> 8);
            sl = (int) (iAddress & 0xFF);

            cg.alSet.add(sh);
            cg.alSet.add(sl);

        }
    }

    /**
     * Установка команды дозвона
     */
    public boolean setCommandRing() {

        boolean result = false;
        alRun.clear();
        getCommandByCroup("c_sub_grup", "Дозвон", hmModem, alRun);
        try {
            question(CMD_GET, alRun, 3000, 10, 3, null, 0);

            // Режим дозвона по GSM
            bitSetFlags.set(ValuesByChannel.BSF_GPRS_GSM_DIAL);

            HashMap<String, Object> hmParam = null;
            hmParam = Work.getParametersFromConst("dialing");
            modelPribor = (String) hmParam.get("type_server");
            hmCommands = getCommandsByModel(modelPribor);

            return true;
        } catch (Exception ex) {

            setLoggerInfo("", ex);

        }

        return result;
    }

    public CommandGet findCommand(String name) {

        CommandGet result = null;

        for (String pribor : hmAllCommands.keySet()) {

            TreeMap<String, CommandGet> commands = hmAllCommands.get(pribor);

            result = commands.get(name);

            if (result != null) {
                return result;
            }

        }
        return result;
    }


    public String findScriptById(int id, String tscript) throws SQLException {

        String result = null;

        String sql = " SELECT  set_script, script_groovy,find_script,save_script FROM commands WHERE c_id=?";

        ResultSet rs = SqlTask.getResultSet(null, sql, new Object[]{id});

        try {

            if (rs.next()) {

                if (tscript == TS_GREATE) {
                    result = rs.getString("set_script");

                } else if (tscript == TS_RESULT) {

                    result = rs.getString("script_groovy");

                } else if (tscript == TS_FIND) {

                    result = rs.getString("find_script");

                } else if (tscript == TS_SAVE) {

                    result = rs.getString("save_script");
                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    /*
     * Отображение формы
     */
    public void showScript(String nameScript) throws Exception {

        ResultSet rs = getXmlDocById(nameScript);

        try {

            while (rs.next()) {

                winform.RecordWindow recordWindow = new RecordWindow(rs, this);

                recordWindow.setVisible(true);
            }

        } finally {

            rs.close();
        }

    }

    /**
     * Установка и удаление текущих флагов
     *
     * @param flag    -положительное значение - устанавливаем отрицательное -
     *                сбрасываем
     * @param command
     */
    public void setFlag(Integer flag, CommandGet command) {

        if (flag >= 0) {
            bitSetFlags.set(flag);
        } else {
            bitSetFlags.clear(flag * -1);
        }

    }

    /**
     * Установка статуса команды
     *
     * @param status
     * @param command
     */
    public void setSatus(Integer status, CommandGet command) {

        if (status == 0) {
            bModemYes = true; // Модем ответил

        }

        if (status == 1) {

            bTelephoneYes = true; // Дозвонились

        }

        if (status == 2) {
            bPriborYes = true; // Пробор ответил

        }

        //  command.curLog = "статус запроса " + status;
    }

    /*
     Выполнение команд перед или после выполненной команды

     */
    private Object invokeMetod(CommandGet command, String go) {

        Object result = null;
        ArrayList<String> alMetod = new ArrayList<String>();
        String metods = (String) command.getProperty("c_formula");

        if (metods == null || metods.isEmpty()) {
            return null;

        } // Единожды, перед выполнение команды(команд ) в пределах
        // текущей сессии
        //   если выполнялась то должна быть в списке выполненых команд

        if (go.equals(GO_BEGIN)) {

            alMetod = Work.getListByStartsWith(metods, GO_BEGIN, "//");
            // Проверяем выполнение

            for (String m : alMetod) {

                if (tsBegin.contains(m)) {
                    // Уже выполнялась
                    return null;

                } else {

                    // добавляем в выполненые
                    tsBegin.add(m);

                }
            }
        }

// До выполнения команды
        if (go.equals(GO_BEFORE)) {

            alMetod = Work.getListByStartsWith(metods, GO_BEFORE, "//");

        } // После выполнения команды
        if (go.equals(GO_AFTER)) {

            alMetod = Work.getListByStartsWith(metods, GO_AFTER, "//");

        } // После выполнения при ошибке
        if (go.equals(GO_ERROR)) {
            alMetod = Work.getListByStartsWith(metods, GO_ERROR, "//");

        } // После выполнения всех команд
        if (go.equals(GO_END)) {
            alMetod = Work.getListByStartsWith(metods, GO_END, "//");

        }

        ArrayList<Class> al = new ArrayList<Class>();
        ArrayList<Object> alObject = new ArrayList<Object>();

        for (String method : alMetod) {

            if (method.isEmpty()) {
                continue;

            }

            al.clear();
            alObject.clear();

            int idxl = method.indexOf("(");

            int idxr = method.indexOf(")");

            if (idxl < 0 || idxr < 0) {
                continue;

            }

            String nameMethod = method.substring(0, idxl);

            String parametrs = method.substring(idxl + 1, idxr);
            String[] par = parametrs.split(",");

            Class class1;

            for (int j = 0; j
                    < par.length; j++) {

                String namePar = par[j].trim();

                if (namePar.isEmpty()) {
                    continue;

                }

                if (namePar.indexOf("\"") != -1) {  // Строка

                    String np = namePar.substring(1, namePar.length() - 1);

                    class1 = String.class;
                    al.add(class1);
                    alObject.add(np);

                } else if (namePar.indexOf(".") != -1) {

                    Double d = Double.parseDouble(namePar);
                    class1 = Double.class;
                    al.add(class1);
                    alObject.add(d);

                } else {

                    Integer in = Integer.parseInt(namePar);
                    class1 = Integer.class;
                    al.add(class1);
                    alObject.add(in);
                }

            }

            al.add(CommandGet.class);
            alObject.add(command);

            Class[] classes = (new Class[alObject.size()]);

            Object[] objects = alObject.toArray();

            for (int x = 0;
                 x < alObject.size();
                 x++) {

                classes[x] = alObject.get(x).getClass();

            }

            try {
                Method method1 = this.getClass().getMethod(nameMethod, classes);

                try {
                    result = method1.invoke(this, objects);

                } catch (IllegalAccessException ex) {

                    setLoggerInfo(nameMethod, ex);
                } catch (IllegalArgumentException ex) {
                    setLoggerInfo(nameMethod, ex);

                } catch (InvocationTargetException ex) {
                    setLoggerInfo(nameMethod, ex);
                }

            } catch (NoSuchMethodException ex) {
                setLoggerInfo(nameMethod, ex);

            } catch (SecurityException ex) {
                setLoggerInfo(nameMethod, ex);
            }
        }

        return result;
    }

    /**
     * Добавляем команды по скрипту
     */
    private void addCmdbyScript(List<String> listName) {

        CommandGet cmd;
        for (String name : listName) {

            cmd = hmModem.get(name);

            if (cmd != null) {
                alGroup.add(cmd);
            }
        }
    }

    // команды для gsm дозвона
    private void addGsmCmd() {

        // createModemCommands();
        CommandGet cmd;
        cmd = hmModem.get("CheckModem");
        alGroup.add(cmd);

        // Скорость модема
        cmd = hmModem.get("at_ipr");
        alGroup.add(cmd);

        //cmd = hmModem.get("GsmDisconnection");
        //alGroup.add(cmd);
        cmd = hmModem.get("SetDeffParameters");
        alGroup.add(cmd);

        cmd = hmModem.get("GsmDownModule");
        alGroup.add(cmd);

        cmd = hmModem.get("GsmCheckPinKod");
        alGroup.add(cmd);

        cmd = hmModem.get("GsmCurrentSet");
        alGroup.add(cmd);

        cmd = hmModem.get("GsmDTRSet");
        alGroup.add(cmd);

        cmd = hmModem.get("GsmStringIni");
        alGroup.add(cmd);

        // Дозвон запрещен
        cmd = hmModem.get("NoRing");
        alGroup.add(cmd);

        cmd = hmModem.get("DialGsm");

        // Команду довона по телефону ставим  последней
        alGroup.add(cmd);

    }

    /**
     * Запрос по текущей таблицы или фильтру
     */
    private void getValueByTable() {

        try {

            ArrayList<Integer> alObject;// Объекты

            int count = 0;
            currOperation = CMD_GET;

            setNotifyObservers(
                    mapMessageProcess);
            setLoggerInfo(
                    "", null);
            setLoggerInfo(
                    "#################################################", null);
            setLoggerInfo(
                    "Запрос данных по текущей таблице...", null);
            setLoggerInfo(
                    "#################################################", null);
            tsBegin.clear();

            ((Port) serialPort).start();
            ///hsOkey.clear();

            // Устанавливаем команды для групп

            hmCmdSend.clear();

            for (Integer idPoint : hmChannels.keySet()) {

                if (((Port) serialPort).isStop()) {
                    return;
                }

                // Объекты с одинаковым каналом связи
                alObject = hmChannels.get(idPoint);

                setCommandsInGroup(alObject);

            }


            for (int i = 0; i < countIter; i++) {

                //   ((Port) serialPort).start();

                setNotifyObservers(esGoCommands);

                mapMessageProcess.clearErrorRows();

                setNotifyObservers(
                        mapMessageProcess);

                setLoggerInfo("Цикл N " + (i + 1), null);

                if (((Port) serialPort).isStop()) {
                    return;
                }


                if (hmCmdSend.isEmpty()) {
                    // Все опрошено
                    break;
                }


                for (Integer idPoint : hmChannels.keySet()) {


                    setNotifyObservers(esGoCommands);

                    if (((Port) serialPort).isStop()) {
                        return;
                    }

                    // Объекты с одинаковым каналом связи
                    alObject = hmChannels.get(idPoint);

                    // Очищаем флаги текущего присоединения
                    bitSetFlags.clear(9, 18);


                    // Убираем уже опрошенные


                    List<Integer> alClone = (List<Integer>) alObject.clone();

                    for (Integer obj : alClone) {

                        if (!hmCmdSend.containsKey(obj)) {

                            alObject.remove(obj);
                        }

                    }


                    //  if (hsOkey.contains(idPoint)) {
                    // Данные по присоединению собраны
                    //    continue;
                    // }

                    //  setCommandsInGroup(alObject);

                    // Удаляем из группы не опрашиваемые(черный список)

                    if (!blackHash.isEmpty()) {

                        alClone = (List<Integer>) alObject.clone();

                        for (Integer obj : alClone) {

                            String msg = isBlackList(obj);


                            if (msg != null) {
                                mapMessageProcess.setInfoProcess(obj, msg);

                                setNotifyObservers(mapMessageProcess);

                                //  alObject.remove(obj);
                                //   hmCmdSend.remove(obj);


                            }

                        }
                    }


                    if (hmCmdSend.isEmpty()) {
                        // Все опрошено
                        break;
                    }

                    if (!alObject.isEmpty()) {
                        getValueFromGroupEx(idPoint, alObject);
                    }

                }


            }


            answerProcess("Запрос данных закончен.", ProcLogs.MSG_OK);

            setLoggerInfo("#################################################", null);
            setLoggerInfo(
                    "Всего недоступных объектов:" + blackHash.size(), null);

            for (Integer id : blackHash.keySet()) {

                java.lang.Object serr = blackHash.get(id);

                if (serr instanceof String) {

                    String sss;
                    sss = (String) serr;
                    setLoggerInfo("Объект-" + id + ":" + sss, null);
                }
            }


            setLoggerInfo(
                    "Запрос данных закончен.", null);
            setLoggerInfo(
                    "#################################################", null);

        } finally {

            notifyObservers(esStopCommands);

        }

    }

    /**
     * Удаляем динамически добавленные кнопки
     */
    public void clearButtonsAndAnswer() throws Exception {

        if (hmAddButtons != null && !hmAddButtons.isEmpty()) {

            for (Element e : hmAddButtons.keySet()) {

                String attr = hmAddButtons.get(e);
                e.setAttribute(attr, "0");
            }
        }

        NodeList list = docTree.getElementsByTagName("answer");

        ArrayList<Node> al = new ArrayList<>();

        if (list == null) {

            return;
        }

        for (int i = 0; i < list.getLength(); i++) {

            Node n = list.item(i);
            al.add(n);

        }

        for (Node n : al) {
            n.getParentNode().removeChild(n);

        }

    }

    private Element getElementByName(Element element, String name) {

        Object parent;
        Element e = element;
        parent = element.getParentNode();

        while (parent != null) {

            if (parent instanceof Element) {

                e = (Element) parent;

                String group = e.getNodeName();

                if (group.equals(name)) {

                    return e;
                }

                parent = e.getParentNode();
            } else {
                return element;

            }

        }
        return e;

    }

    /**
     * Возвращает элемент дерева комманд по названию команды
     *
     * @param nameCmd название команды
     * @return Элемент дерева или null
     */
    public Element getElementByNameCommand(String nameCmd) {

        Element result = null;

        if (hmCommansTree != null && hmCommansTree.containsKey(nameCmd)) {

            result = hmCommansTree.get(nameCmd);

        }

        return result;
    }

    /**
     * Добавляет кнопку для выполнения скрипта
     *
     * @param poz        -позиция колонки(1,2) grup 3,4 -row 5,6 col
     * @param nameButton -название кнопки
     * @param command    -команда,где нах.скрипт
     */
    public void addButton(Integer poz, String nameButton, CommandGet command) {

        Element element = hmCommansTree.get(command.name);
        String vis = "";

        if (element != null) {

            if (poz == 1) {
                vis = "visible1";
                element = getElementByName(element, "grup");
                element.setAttribute("col_1", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");

            } else if (poz == 2) {
                vis = "visible2";
                element = getElementByName(element, "grup");
                element.setAttribute("col_2", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");

            } else if (poz == 3) {

                vis = "visible1";
                element = getElementByName(element, "row");
                element.setAttribute("col_1", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");

            } else if (poz == 4) {
                vis = "visible2";
                element = getElementByName(element, "row");
                element.setAttribute("col_2", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");
            } else if (poz == 5) {
                vis = "visible1";
                element.setAttribute("col_1", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");

            } else if (poz == 6) {
                vis = "visible2";
                element.setAttribute("col_2", command.name + "_%" + nameButton);
                element.setAttribute(vis, "1");

            }

            if (hmAddButtons == null) {
                hmAddButtons = new HashMap<>();
            }

            element.setAttribute("script", command.name);
            hmAddButtons.put(element, vis);

        }
    }

    /**
     * Установка количества попыток отправки команд
     *
     * @param countTry
     * @param cg
     */
    public void setCountTry(Integer countTry, CommandGet cg) {

        cg.countTry = countTry;
    }

    /**
     * Установка типа ответа
     *
     * @param sOk Строка ответа
     * @param cg  текущая команда
     */
    public void setOkString(String sOk, CommandGet cg) {

        cg.Ok = sOk;
    }

    /**
     * Установка паузы между посылкой команд
     *
     * @param iPau Пауза между командами в миллисекундах
     * @param cg   Текущая команда
     */
    public void setPause(Integer iPau, CommandGet cg) {

        cg.sleepTime = iPau;

    }

    /**
     * @param cg     - текущая команда
     * @param sError - строка ошибки
     */
    public void setErrorCmd(String sError, CommandGet cg) {

        cg.errorCmd = sError;

    }

    private void setErrorsPribor(int idPribor, String model, Object cmdError) {

        String caption = hmCaptions.get(idPribor);

        setLoggerInfo(
                "------------------------------------------------------------------------", null);

        String sName = "Объект: " + caption + "[" + idPribor + "]" + "[" + model + "]";

        if (cmdError instanceof String) {

            setLoggerInfo(sName + ":" + cmdError, null);

            return;

        }

        if (cmdError instanceof CommandGet) {

            CommandGet cg = (CommandGet) cmdError;

            String nameCmd = getAllName(cg);

            setLoggerInfo(
                    sName, null);

            if (!bPriborYes) {

                setLoggerInfo(nameCmd + " ОШИБКА: Прибор не ответил!", null);

            } else {

                setLoggerInfo(nameCmd + " ОШИБКА: " + cg.errorCmd, null);

            }

        }
    }

    // Команды после окончания запроса.
    protected void checkEnd() throws Exception {

        Boolean flag = ((Port) serialPort).isStop();

        //  if (flag) {
        //    return;
        // }


        try {

            setNotifyObservers(esBlockButton); //Временно блокируем...

            //serialPort.stop();
            runCommandByScript(typConnect, TS_SAVE);

            boolean rlsd = true;
            // serialPort.setDTR(false);

            // serialPort.closePort();


            if (!serialPort.isOpened()) {

                return;
            }


            if (typConnect.equals("GSM") && bitSetFlags.get(BSF_GSM_YES)) {

                try {

                    if (isSetStatus(esDTROn)) {

                        serialPort.setDTR(false);

                        serialPort.closePort();
                    } else {

                        CommandGet c;
                        alGroup.clear();
                        // Для  модемов не поддерживающих DTR
                        c = hmModem.get("SwitchByCom"); // Переключение из режима передачи данных
                        alGroup.add(c);
                        c = hmModem.get("GsmDisconnection");
                        alGroup.add(c);
                        alRun = new ArrayList(alGroup);

                        try {
                            question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, 0);

                            bTelephoneYes = false;

                        } catch (Exception ex) {

                            sendErrorMessge(null, null, ex);
                        }

                    }

                    //    if (bitSetFlags.get(BSF_DTR_YES)) {
                    //      return;
                    //  }
                    //  time = System.currentTimeMillis();
                    //  while (time + 6000 > System.currentTimeMillis()) {
                    //    rlsd = serialPort.isRLSD();
                    //  if (!rlsd) {
                    //    break;
                    // }
                    // Thread.sleep(500);
                    // }
                    // rlsd = serialPort.isRLSD();
                    // if (rlsd) {
                    //   bitSetFlags.set(BSF_DTR_NO);
                    // }
                } catch (Exception ex) {


                    //Разблокировка кнопки Start
                    setNotifyObservers(esStopCommands);
                    setLoggerInfo(ex.getMessage(), ex);
                    return;
                }

                //      if (bitSetFlags.get(BSF_DTR_NO) && rlsd) {
                //      CommandGet c;
                //        alGroup.clear();
                // Для  модемов не поддерживающих DTR
                //    c = hmModem.get("SwitchByCom"); // Переключение из режима передачи данных
                //  alGroup.add(c);
                //   c = hmModem.get("GsmDisconnection");
                //   alGroup.add(c);
                //   alRun = new ArrayList<CommandGet>(alGroup);
                // try {
                //   question(CMD_GET, alRun, time_aut, sys_time, count_sql, null, null, 0);
                // bTelephoneYes = false;
                // } catch (Exception ex) {
                //   sendErrorMessge(null, null, ex);
                // }
            }

        } finally {

            //  serialPort.start();
            stopBlinkText();
            setNotifyObservers(esStopCommands);
// Очищаем флаги текущего присоединения
            bitSetFlags.clear(9, 18);

            if (!alEnd.isEmpty()) {

                for (CommandGet cg : alEnd) {

                    invokeMetod(cg, GO_END);

                }

            }

        }

    }

    // Запрос данных в ручном режиме
    private void getValueHand() {

        //TODO Запрос данных в ручном режиме


        try {

            int idPoint;

            int idObject;

            // serialPort.stop();

            String idPnt = docTree.getDocumentElement().getAttribute("id_point");
            String idObj = docTree.getDocumentElement().getAttribute("id_object");

            try {

                idPoint = Integer.parseInt(idPnt);

                idObject = Integer.parseInt(idObj);

            } catch (NumberFormatException e) {

                setLoggerInfo("", e);

                return;

            }

            ArrayList<Integer> al = new ArrayList<Integer>();

            al.add(idObject);

            hmChannels = new HashMap<>();

            hmChannels.put(idPoint, al);


            tsBegin.clear();
            currentCommands = null;

            currOperation = CMD_GET;

            //  if (isOkeyPoint(idPoint, al)) {

            //    answerProcess("Успешно.", ProcLogs.MSG_OK);
            //  return;

//            }

            setCommandsInGroup(al);

            if (hmCmdSend.isEmpty()) {

                answerProcess("Не заданы параметры запроса.", ProcLogs.MSG_ERROR);

                return;
            }

            //    List list = hmCmdSend.values().iterator().next();

            //  int sizeStart = list.size();

            ((Port) serialPort).start();

            //serialPort.start();

            for (int i = 0; i < countIter; i++) {

                if (((Port) serialPort).isStop()) {

                    setNotifyObservers(esStopCommands);

                    answerProcess("Остановка запроса данных.", ProcLogs.MSG_LOG);

                    return;
                }

                if (hmCmdSend.isEmpty()) {

                    answerProcess("Успешно.", ProcLogs.MSG_OK);

                    setNotifyObservers(esStopCommands);


                    return;
                }


                setNotifyObservers(esGoCommands);

                // ручной режим
                getValueFromGroupEx(idPoint, al);

            }

            // int sizeEnd = hmCmdSend.values().iterator().next().size();

            // какие то проблемы...
            if (errorString == null || errorString.isEmpty()) {

                //   if (sizeEnd == sizeStart) {

                //     answerProcess("Не удалось сделать запрос.Проверте параметры запроса.", ProcLogs.MSG_ERROR);
                // } else {

                //   answerProcess("Не удалось считать все данные.", ProcLogs.MSG_WARNING);

                //}

            } else {

                answerProcess(errorString, ProcLogs.MSG_ERROR);

            }

        } finally {

            // setEnabledButtonGo(1);
            // processInterface.setImageStart();

//closePortEngidea();
            setNotifyObservers(esStopCommands);

        }

    }

    private String getHtmlCol(String sTyp, String spain, Color colorFont, Color colorBorder, Object value) {

        String result = null;

        StringBuilder builder = new StringBuilder();

        int ib = colorFont.getBlue();

        int ig = colorFont.getGreen();

        int ir = colorFont.getRed();
        String sb = Integer.toHexString(ib);
        String sg = Integer.toHexString(ig);
        String sr = Integer.toHexString(ir);

        String fColor = sr + sg + sb;

        ib = colorBorder.getBlue();
        ig = colorBorder.getGreen();
        ir = colorBorder.getRed();
        sb = Integer.toHexString(ib);
        sg = Integer.toHexString(ig);
        sr = Integer.toHexString(ir);

        String bColor = sr + sg + sb;

        builder.append("<");
        builder.append(sTyp);

        if (spain != null) {
            builder.append(" ");
            builder.append(spain);
            builder.append(" ");

        }
        builder.append(" ");
        builder.append("BGCOLOR=#");
        builder.append(bColor);
        builder.append(">");
        builder.append("<FONT COLOR=#");
        builder.append(fColor);
        builder.append(">");
        builder.append(value);
        builder.append("</FONT>");
        builder.append("</");
        builder.append(sTyp);
        builder.append(">");

        result = builder.toString();

        return result;

    }

    /**
     * Показать профиль тока и напряжения
     */
    private void viewTableCurrenAndVoltage() {

        StringBuilder builder = new StringBuilder();
        // DateTime dtCouter = (DateTime) command.get("DateProfile");
        CommandGet commandGet;

        DateTime dtFirst;
        DateTime dtLast;

        Double value;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        commandGet = hmCommands.get("ProfVoltAndCurr");

        dtFirst = (DateTime) commandGet.getProperty("dateTimeFirst");
        dtLast = (DateTime) commandGet.getProperty("dateTimeLast");

        builder = new StringBuilder();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("ПРОФИЛЬ ТОКА И НАПРЯЖЕНИЯ");
        builder.append(" C ");
        builder.append(dtFirst.toString(dtf));
        builder.append(" ПО ");
        builder.append(dtLast.toString(dtf));

        builder.append("</H3>");
        builder.append("<TABLE BORDER=1 WIDTH=100%>");

// Заголовок
        builder.append("<TR>");

        builder.append(getHtmlCol("TD", "ALIGN=CENTER ROWSPAN=2", Color.WHITE, colorBorder, "Дата и время"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER COLSPAN=3", Color.WHITE, colorBorder, "Ток,А"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER COLSPAN=3", Color.WHITE, colorBorder, "Напряжение,В"));

        builder.append("</TR>");

        builder.append("<TR>");

        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'A'"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'B'"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'C'"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'A'"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'B'"));
        builder.append(getHtmlCol("TD", "ALIGN=CENTER", Color.WHITE, colorBorder, "Фаза 'C'"));

        builder.append("</TR>");

        String prim = "из базы";

        LinkedHashMap<Integer, CommandGet> tmChilds;

        LinkedHashMap<Integer, CommandGet> tmSet;

        tmChilds = commandGet.tmChilds;

        for (CommandGet cg : tmChilds.values()) {

            tmSet = cg.tmChilds;

            for (CommandGet get : tmSet.values()) {

                Object[] values = (Object[]) get.result;

                if (values == null) {
                    continue;

                }

                builder.append("<TR>");

                DateTime time = (DateTime) values[7];
                String sDate = time.toString(dtf);

                builder.append("<TD>");
                builder.append(sDate);
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[1].toString());
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[2].toString());
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[3].toString());
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[4].toString());
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[5].toString());
                builder.append("</TD>");

                builder.append("<TD>");
                builder.append(values[6].toString());
                builder.append("</TD>");

                builder.append("</TR>");

            }

        }

        builder.append("</TABLE>");

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");

        String msg = builder.toString();

        ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }

    /**
     * Показать профиль мощности
     */
    private void showTableProfile() {

        StringBuilder builder = new StringBuilder();
        // DateTime dtCouter = (DateTime) command.get("DateProfile");
        CommandGet commandGet;

        DateTime dtFirst;
        DateTime dtLast;

        Double value;
        DateTime dt = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

        commandGet = hmCommands.get("SetProfilPower");

        dtFirst = (DateTime) commandGet.getProperty("dateTimeFirst");
        dtLast = (DateTime) commandGet.getProperty("dateTimeLast");

        builder = new StringBuilder();
        builder.append("<HTML>");
        builder.append("<H3 ALIGN=CENTER>");
        builder.append("ПРОФИЛЬ МОЩНОСТИ");
        builder.append(" C ");
        builder.append(dtFirst.toString(dtf));
        builder.append(" ПО ");
        builder.append(dtLast.toString(dtf));

        builder.append("</H3>");
        builder.append("<TABLE BORDER=1 WIDTH=100%>");

// Заголовок
        builder.append("<TR>");

        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Дата и время"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "P+"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "P-"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Q+"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Q-"));
        builder.append(getHtmlCol("TD", "ALIGN=center", colorFont, colorBorder, "Примечание"));
        builder.append("</TR>");

        String prim = "из базы";

        HashMap<String, CommandGet> hmCmd;

        LinkedHashMap<Timestamp, CommandGet> tmChilds;

        tmChilds = commandGet.tmChilds;

        for (Timestamp dateTime : tmChilds.keySet()) {

            CommandGet cg = tmChilds.get(dateTime);

            builder.append("<TR>");

            Timestamp ts = (Timestamp) cg.getProperty("value_date");

            DateTime time = new DateTime(ts.getTime());

            String sDate = time.toString(dtf);

            builder.append("<TD>");
            builder.append(sDate);
            builder.append("</TD>");

            if (cg.result == null) {

                continue;

            }

            ArrayList<Object> alValues = (ArrayList<Object>) cg.result;

            for (Object os : alValues) {

                String sv = os.toString();

                if (cg.bValueBase) {

                    prim = "ЕСТЬ В БАЗЕ";

                } else {
                    prim = "";

                }

                builder.append("<TD>");
                builder.append(sv);
                builder.append("</TD>");

            }

            builder.append("<TD>");
            builder.append(prim);
            builder.append("</TD>");

            builder.append("</TR>");

        }

        builder.append("</TABLE>");

        builder.append("<H3 ALIGN=CENTER>");
        builder.append("<FONT COLOR=000000>");
        builder.append("");
        builder.append("</FONT>");
        builder.append("</H3>");
        builder.append("</HTML>");

        String msg = builder.toString();

        ValueWindow valueWindow = new ValueWindow(null, false, msg);
        valueWindow.setVisible(true);

    }

    public HashMap<String, String> getMapInScript(String ts) {

        HashMap map = new HashMap();

        return map;
    }

    public String getScriptById(String nameScript) throws Exception {

        String result = "";

        String typ = String.valueOf(nameScript.charAt(1)).toUpperCase();

        String skey = nameScript.substring(2);

        Integer key = Integer.parseInt(skey.trim());
        String name = "script_groovy";

        if (typ.equals(TS_GREATE)) {
            name = "set_script";
        } else if (typ.equals(TS_RESULT)) {
            name = "script_groovy";
        } else if (typ.equals(TS_FIND)) {
            name = "find_script";
        } else if (typ.equals(TS_SAVE)) {
            name = "save_script";
        } else if (typ.equals(TS_CHECK)) {
            name = "check_script";
        }

        String sql = " SELECT " + name + " FROM commands WHERE c_id=" + key;

        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {
                result = rs.getString(name);
                //   hmScripts.put(nameScript, result);

            }

        } finally {
            rs.close();
        }

        return result;

    }

    public HashMap<String, Object> getMapById(String nameScript) throws Exception {

        HashMap<String, Object> result = null;

        ResultSet rs = getXmlDocById(nameScript);

        try {

            while (rs.next()) {

                String sDok = rs.getString(1);
                result = XmlTask.getMapValuesByXML(sDok, "name", "value", "cell");

            }

        } finally {

            rs.close();
        }

        return result;
    }

    public ResultSet getXmlDocById(String nameScript) throws Exception {

        ResultSet rs;

        String typ = String.valueOf(nameScript.charAt(0));

        String skey = nameScript.replace(typ, "");

        Integer key = Integer.parseInt(skey);
        String nameCol = "script_groovy";

        if (typ.equals("c")) {
            nameCol = "set_script";
        } else if (typ.equals("r")) {
            nameCol = "script_groovy";
        } else if (typ.equals("f")) {
            nameCol = "find_script";
        } else if (typ.equals("s")) {
            nameCol = "save_script";
        }

        String sql = " SELECT " + nameCol + " FROM commands WHERE c_id=" + key;

        rs = SqlTask.getResultSet(null, sql, ResultSet.CONCUR_UPDATABLE);

        return rs;

    }

    /**
     * результат выполнение команды по названию команды
     *
     * @param nameCmd
     * @return
     */
    public Object getValuesByNameCmd(String nameCmd) {

        Object result = null;

        CommandGet commandGet = hmCommands.get(nameCmd);

        if (commandGet != null) {

            result = commandGet.result;
        }

        return result;

    }

    /**
     * Имя команды по названию кнопки
     *
     * @param nameButton
     * @return
     */
    private CommandGet getCommandByButton(String nameButton) {

        String sql = "SELECT c_name FROM commands WHERE c_formula LIKE ?";

        CommandGet commandGet = null;

        String name;

        ResultSet rs;
        try {
            rs = SqlTask.getResultSet(null, sql, new Object[]{"%" + nameButton + "%"});

            try {
                while (rs.next()) {

                    name = rs.getString(1);

                    commandGet = hmCommands.get(name);

                }
            } finally {

                rs.close();

            }

        } catch (SQLException ex) {
            setLoggerInfo("Имя команды по названию кнопки", ex);
        }

        return commandGet;

    }

    /**
     * По имени команды параметры из XML дерева например(Для записи параметров в
     * прибор учета)
     *
     * @param nameCmd
     * @return
     */
    public HashMap<String, String> getValXmlByName(String nameCmd) {

        HashMap<String, String> hm = new HashMap<String, String>();

        Element element = hmCommansTree.get(nameCmd);

        if (element != null) {

            NamedNodeMap attributes = element.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {

                Node node = attributes.item(i);

                String name = node.getNodeName();
                String value = node.getNodeValue();

                hm.put(name, value);

            }

        }

        return hm;
    }


    /**
     * Выполняем команду при нажатии на кнопку в дереве команд прибора
     *
     * @param nameButton
     * @param element
     */
    public void runCommandByButton(String nameButton, Element element) {

        //TODO Выполнение команды по кнопке

        String nCom;
        CommandGet commandGet;

        Object object = null;

        bitSetFlags.set(BSF_BUTTON_ON);
        //hsOkey.clear();
        Integer iPoz;

        String nameCmd = element.getAttribute("name");

        if (nameCmd == null || nameCmd.isEmpty()) {

            // Для кнопок группы
            nameCmd = element.getAttribute("script");

        }

// String poz = element.getAttribute("poz");
        // При динамическом добавлении кнопки к дереву команд прибора
        commandGet = hmCommands.get(nameCmd);

        HashMap<String, String> hmAttr;
        HashMap<String, HashMap<String, String>> hmAttribute = new HashMap<>();

        hmAttr = Work.getMapAttributes(element);
        commandGet.putProperty("@parent_map", hmAttr);

        String poz_x = element.getAttribute("poz_x");
        String poz_y = element.getAttribute("poz_y");

        commandGet.putProperty("@poz_x", Integer.parseInt(poz_x));
        commandGet.putProperty("@poz_y", Integer.parseInt(poz_y));

        iPoz = (Integer) commandGet.getProperty(nameButton);

        if (iPoz == null) {

            for (String pos : hmAttr.values()) {

                if (pos.contains(nameButton)) {

                    if (pos.contains("1%")) {
                        iPoz = 1;
                    } else {

                        iPoz = 2;
                    }
                    break;
                }

            }

        }

        nCom = (String) commandGet.getProperty("c_measure");

        if (nCom == null || nCom.isEmpty()) {

            Work.ShowError("Нет выполняемого скрипта !");
            return;
        }

        String ns = nCom.toUpperCase();

        if (nCom.contains("/")) {

            String[] scr = nCom.split("/");

            // Позиция нажатой кнопки
            if (iPoz != null && iPoz == 1) {
                commandGet.putProperty("@posicion", 1);

                ns = scr[0];
            } else {
                commandGet.putProperty("@posicion", 2);

                ns = scr[1];
            }

        }

        try {

            NodeList nlNode = XmlTask.getNodeListByXpath(element, "descendant::*");

            for (int i = 0; i < nlNode.getLength(); i++) {

                Element elm = (Element) nlNode.item(i);

                NamedNodeMap map = elm.getAttributes();

                String nameParent;

                String update = elm.getAttribute("update").trim();

                boolean b_add = (update != null && !update.isEmpty());

                if (b_add) {

                    hmAttr = Work.getMapAttributes(elm);

                    nameParent = hmAttr.get("name");

                    hmAttribute.put(nameParent, hmAttr);

                }

            }

            //  NamedNodeMap map = element.getAttributes();
            //   String nameParent = element.getNodeName();
            //String update = element.getAttribute("update");
            //  boolean b_add = (update != null && !update.isEmpty());
            //  if (b_add) {
            //    for (int j = 0; j < map.getLength(); j++) {
            //      Node item = map.item(j);
            //    String name = item.getNodeName();
            //  String value = item.getNodeValue();
            //  hmAttr.put(name, value);
            // }
            // hmAttribute.put(nameParent, hmAttr);
            //  }
            //  NodeList list = element.getChildNodes();
            //for (int i = 0; i < list.getLength(); i++) {
            //  Element e = (Element) list.item(i);
            //nameParent = e.getAttribute("name");
            //  update = e.getAttribute("update");
            //  b_add = (update != null && !update.isEmpty());
            //  if (!b_add) {
            //    continue;
            // }
            //  map = e.getAttributes();
            //  hmAttr = new HashMap<>();
            //hmAttribute.put(nameParent, hmAttr);
            // for (int j = 0; j < map.getLength(); j++) {
            //   Node item = map.item(j);
            // String name = item.getNodeName();
            //  String value = item.getNodeValue();
            //  hmAttr.put(name, value);
            // }
            // hmAttr.put("element", e);
            // hmAttribute.put(nameParent, hmAttr);
            // }
            commandGet.putProperty("@attributes", hmAttribute);
            commandGet.putProperty("@cmdmodem", hmModem);

            object = evalScript(ns, commandGet);

        } catch (Exception ex) {
            Work.ShowError(ex.getMessage());
            setLoggerInfo("Поиск скрипта", ex);
        }

        if (object == null) {
            return;
        }

        if (object != null && object instanceof String) {
            String msg = (String) object;

            // Вывод информации на экран
            if (!msg.isEmpty()) {
                ValueWindow valueWindow = new ValueWindow(null, true, msg);
                valueWindow.setVisible(true);

            }
        } else if (object != null && object instanceof Map) {
            // Выполняем команды

            Map<String, String> hmCom = (Map<String, String>) object;

            currentCommands = hmCom.get(nameButton);
            typRegime = REJIM_GET_BUTTON;
            executeProcess();

        } else if (object != null && object instanceof ArrayList) {
            // Выполняем команду

            currentCommands = "";

            ArrayList<String> arrayList = (ArrayList<String>) object;

            for (String name : arrayList) {

                currentCommands = currentCommands + name + ",";

            }

            currentCommands = currentCommands.substring(0, currentCommands.length() - 1);

            typRegime = REJIM_GET_BUTTON;
            executeProcess();

        } else if (object != null && object instanceof Document) {
            //Выполням файл сценария
        } else {

            if (nameButton.equals("Таблица профиля")) {
                //Профиль мощности в табличном виде
                showTableProfile();

            } else if (nameButton.equals("Векторная диаграмма")) {

                setNotifyObservers(nameButton);

            } else if (nameButton.equals("Журнал превышений")) {

                viewJurnalOver();

            } else if (nameButton.equals("Журнал дискретных каналов")) {

                viewJurnalDiscret();

            } else if (nameButton.equals("Список счетчиков")) {

                viewCounters();

            } else if (nameButton.equals("Очистить журнал превышений уставок")) {

                clearJurnalOver();

            } else if (nameButton.equals("Очистить журнал дискретных каналов")) {

                clearJurnalDiscret();

            } else if (nameButton.equals("Скорректировать список счетчиков")) {

                correctListCounters();

            } else if (nameButton.equals("Таблица профиля тока и напряжения")) {

                viewTableCurrenAndVoltage();

            } else if (nameButton.equals("Записать пароль счетчика в контроллер")) {

                writePasswordCounter();

            } else {
                setValues();

            }

        }

    }

    /**
     * выполняет команды списка
     */
    private void beginCommands() {

        try {

            int idPoint;

            int idObject;

            String idPnt = docTree.getDocumentElement().getAttribute("id_point");
            String idObj = docTree.getDocumentElement().getAttribute("id_object");

            try {

                idPoint = Integer.parseInt(idPnt);

                idObject = Integer.parseInt(idObj);

            } catch (NumberFormatException e) {

                setLoggerInfo("", e);

                return;

            }

            ArrayList<Integer> al = new ArrayList<Integer>();

            al.add(idObject);

            tsBegin.clear();

            getValueFromGroupEx(idPoint, al);

        } finally {

            //setEnabledButtonGo(1);
            // processInterface.setImageStart();
// closePortEngidea();
            setNotifyObservers(esStopCommands);

        }

    }

    /**
     * Скорректировать список счетчиков
     */
    private void correctListCounters() {

        CommandGet cg = hmCommands.get("ListCounters");
        alSelect.clear();

        CommandGet cgAdd = hmCommands.get("AddCountInCont");
        CommandGet cgDel = hmCommands.get("DelCountInCont");

        CommandGet cgDelAdd;

        for (Object id : cg.tmChilds.keySet()) {

            String cap = (String) cg.tmChilds.get(id);

            if (cap.indexOf("Добавить") != -1) {

                cgDelAdd = (CommandGet) cgAdd.clone();
                cgDelAdd.alSet = new ArrayList();

                int i = (Integer) id;

                int s = (int) i;
                cgDelAdd.alSet.add(s);
                alMake.add(cgDelAdd);

            }

            if (cap.indexOf("Удалить") != -1) {

                cgDelAdd = (CommandGet) cgDel.clone();
                cgDelAdd.alSet = new ArrayList();

                int i = (Integer) id;

                int s = (int) i;
                cgDelAdd.alSet.add(s);
                alMake.add(cgDelAdd);

            }

        }

        cg.tmChilds = null;
        typRegime = REJIM_GET_BUTTON;
        executeProcess();

    }

    /**
     * Записать пароль счетчика в контроллер
     */
    private void writePasswordCounter() {

        //Все проверить
        int coAdd = getAddressCounter();

        HashMap<String, Object> hmPar = null;

        if (coAdd == -1) {

            JOptionPane.showMessageDialog(null, "Не выбран текущий объект !");
            ((Port) serialPort).stop();
            //serialPort.stop();

            return;

        }
        try {

            // hmPar = SqlTask.getValuesByKey(null, "object5", new Object[]{id});
            hmPar = Work.getParametersRow(coAdd, null, "objects", true, true);

        } catch (SQLException ex) {
            setLoggerInfo("", ex);

        }

        String password = (String) hmPar.get("password_1");

        hmProperty.put("password_1", password);

        hmProperty.put("counter_addres", coAdd);

        CommandGet cg = hmCommands.get("SetPasswordCounter");
        currentCommands = cg.name;

        typRegime = REJIM_GET_BUTTON;
        executeProcess();

    }

    /**
     * Очистить журнал дискретных каналов
     */
    private void clearJurnalDiscret() {

        CommandGet cg = hmCommands.get("ClearDisJurnal");
        currentCommands = cg.name;
        typRegime = REJIM_GET_BUTTON;
        executeProcess();

    }

    /**
     * Очистить журнал превышений уставок
     */
    private void clearJurnalOver() {

        CommandGet cg = hmCommands.get("ClearJurnalEvents");
        currentCommands = cg.name;
        typRegime = REJIM_GET_BUTTON;
        executeProcess();

    }

    /**
     * Очищаем все группы перед новым запросом
     */
    private void clearGrup() throws Exception {

        String sql = "descendant::*";

        NodeList nodeList = XmlTask.getNodeListByXpath(docTree.getDocumentElement(), sql);

        HashMap<String, Object> hm = new HashMap<String, Object>();

        for (int i = 0; i
                < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);

            String sClear = element.getAttribute("clear");

            if (sClear != null && sClear.equals("1")) {

                hm.put("closeNode", element);
                //   observableClass.setNotify(hm);

                // Удаляем подчиненные узлы
                while (element.hasChildNodes()) {

                    Node node = element.getFirstChild();

                    if (node != null) {
                        element.removeChild(node);

                    }
                }
            }
        }

        System.gc();

    }

    /**
     * Создаем карту связи между именем команды и элементом дерева
     */
    private void createContactCmdElm() throws Exception {

        String sql = "descendant::*";

        NodeList nodeList = XmlTask.getNodeListByXpath(docTree.getDocumentElement(), sql);

        for (int i = 0; i
                < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);

            String name = element.getAttribute("name");

            element.setAttribute("select", "0");

            String value = element.getAttribute("value");

            if (value != null && !value.isEmpty()) {
                element.setAttribute("value", "");
            }

            String base = element.getAttribute("base");

            if (base != null && !base.isEmpty()) {
                element.setAttribute("base", "");
            }

            String update = element.getAttribute("update");

            if (update != null && !update.isEmpty()) {
                element.setAttribute("update", "");
            }

            if (hmCommands.containsKey(name)) {
                hmCommansTree.put(name, element);

            }
        }

    }

    public ArrayList<String> getCommandsInDocument(Document document) throws Exception {

        if (document == null) {
            return null;

        }

        ArrayList<String> alResult = new ArrayList<String>();

        // Очищаем
        clearGrup();

        String sql = "descendant::*";

        NodeList nodeList = XmlTask.getNodeListByXpath(document.getDocumentElement(), sql);

        for (int i = 0; i
                < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);
            String sel = element.getAttribute("select");

            if (sel.equals("1")) {

                String name = element.getAttribute("name");

                if (!name.isEmpty()) {
                    alResult.add(name);

                }
            }
        }

        return alResult;

    }

    public Document getXMLDocByModel(int idPoint, int idObject, HashMap dopMap, String nameTable, JComponent panel) throws Exception {

        Document document;
// TODO Устанавливаем модель прибора
        // Устанавливаем  модель прибора
        String model;

        String mod_counters;
        String mod_controller;

        String all_typ;
        try {
            // Параметры текущего объекта

            bController = true;

            hmProperty = Work.getParametersRow(idObject, null, nameTable, true, true);

            if (hmProperty.containsKey("id_counter")) {
                bController = false;

            }

            if (hmdocTree == null) {

                hmdocTree = new HashMap<>();

            }

            // Добавляем параметры контроллера, если он есть
            hmController = Work.getMapController(idPoint);

            if (!hmController.isEmpty()) {

                hmProperty.putAll(hmController);

            }

            hmPoint = Work.getParametersRow(idPoint, null, "points", false, true);

            if (dopMap != null && !dopMap.isEmpty()) {

                hmProperty.putAll(dopMap);

            }

        } catch (SQLException ex) {

            setLoggerInfo("Параметры объекта", ex);

        }

        mod_counters = (String) hmProperty.get("model_counter");

        mod_controller = (String) hmProperty.get("typ_controller");

        model = getModelPribor(nameTable);

        if (mod_counters == null || mod_counters.isEmpty()) {

            mod_counters = "";
        }

        if (mod_controller == null || mod_controller.isEmpty()) {

            mod_controller = "";
        }

        all_typ = mod_counters + "_" + mod_controller;


        if (hmdocTree.containsKey(all_typ)) {


            document = hmdocTree.get(all_typ);

            document.getDocumentElement().setAttribute("id_point", "" + idPoint);
            document.getDocumentElement().setAttribute("id_object", "" + idObject);

            String objCapton = (String) hmProperty.get("caption");
            document.getDocumentElement().setAttribute("caption_object", objCapton);

            typConnect = (String) hmPoint.get("typ_connect");

            this.docTree = document;

            clearButtonsAndAnswer();

            hmCommansTree.clear();
            // Если текущая модель не совпадает с существующей
            if (modelPribor == null || !modelPribor.equals(model)) {

                modelPribor = model;
                hmCommands = getCommandsByModel(modelPribor);

            }

            // Создаем контакт между командой и деревом
            createContactCmdElm();
            this.nameTable = nameTable;
            // проверяем  видимость ячеек
            checkVisibleCellTree();

            Element element = (Element) XmlTask.getNodeByAttribute(document.getDocumentElement(), "counters", "ok", "grup");

            if (element != null) {

                String format = Work.getCountersByPoint(idPoint);
                element.setAttribute("format", format);
                String names = Work.getCountersByPointJson(idPoint);
                element.setAttribute("name_counters", names);
                hmCommansTree.put("ListCountersCont", element);

            }

            return document;

        }

        try {
            CursorToolkitOne.startWaitCursor(panel);

            // Создаем все команды по текущей модели
            HashMap<String, Element> hmRow = new HashMap();
            HashMap<String, Element> hmCol = new HashMap();

            this.nameTable = nameTable;

            ArrayList<String> alEditor = new ArrayList();

            String edit = null;
            String button = null;
            String check_script = null;

            Element eRow;
            Element eCol;
            Element eSubCol;

            Set<String> tsGroup = new TreeSet<>();

            String sql;

            hmCommansTree.clear();

            // Если текущая модель не совпадает с существующей
            if (modelPribor == null || !modelPribor.equals(model)) {

                modelPribor = model;
                hmCommands = getCommandsByModel(modelPribor);

            }

            document = XmlTask.getNewDocument();

            try {

                // Самое короткое название типа
                String global_name = (String) hmProperty.get("global_name");

                if (global_name != null && !global_name.isEmpty()) {

                    sql = "SELECT  c_sub_grup,check_script FROM commands WHERE c_sub_grup<>'Режим'  AND (c_instrument LIKE '%" + modelPribor + "%' "
                            + " OR c_instrument LIKE '%/" + global_name + "/%') ORDER BY c_sub_grup";

                } else {

                    sql = "SELECT  c_sub_grup,check_script FROM commands WHERE c_sub_grup<>'Режим'  AND c_instrument LIKE '%" + modelPribor + "%' ORDER BY c_sub_grup";
                }

                ResultSet rs = SqlTask.getResultSet(null, sql);
                String nameGrup;

                try {
                    Element eRoot = document.createElement("root");
                    document.appendChild(eRoot);

                    document.getDocumentElement().setAttribute("id_point", "" + idPoint);
                    document.getDocumentElement().setAttribute("id_object", "" + idObject);

                    String objCapton = (String) hmProperty.get("caption");
                    document.getDocumentElement().setAttribute("caption_object", objCapton);

                    eRoot.setAttribute("model", model);

                    while (rs.next()) {

                        nameGrup = rs.getString("c_sub_grup");

                        check_script = rs.getString("check_script");

                        if (check_script != null && !check_script.isEmpty()) {

                            Boolean b;

                            b = true;

                            if (b != null && b) {
                                tsGroup.add(nameGrup);
                            }
                        } else {
                            tsGroup.add(nameGrup);
                        }
                    }

                    for (String ng : tsGroup) {

                        nameGrup = ng;

                        Element eGrup = document.createElement("grup");
                        eGrup.setAttribute("caption", nameGrup);
                        eGrup.setAttribute("select", "0");

                        eRoot.appendChild(eGrup);

                        hmCol.clear();
                        hmRow.clear();

                        for (CommandGet cmd : hmCommands.values()) {

                            check_script = (String) cmd.getProperty("check_script");

                            if (check_script != null && !check_script.isEmpty()) {

                                Boolean b = (Boolean) evalScript(TS_CHECK, cmd);

                                if (b != null && !b) {
                                    continue;
                                }
                            }

                            cmd.clearCommand();

                            if (cmd.tmChilds != null) {
                                cmd.tmChilds.clear();

                            }

                            String format = "";

                            String grup = "";
                            String name = cmd.name;
                            String caption = "";
                            String measure = "";
                            String value = "";
                            String sRow = "";
                            String sCol = "";
                            String sSubCol = "";
                            String formula = "";

                            try {

                                // String instrNames = (String) c.get("c_instrument");
                                // Строка форматирования
                                format = (String) cmd.getProperty("c_format");

                                grup = (String) cmd.getProperty("c_sub_grup");
                                name = cmd.name;
                                caption = cmd.getProperty("c_name_row") + " " + cmd.getProperty("c_name_col") + " " + cmd.getProperty("c_name_subcol");
                                measure = (String) cmd.getProperty("c_measure");
                                value = "";
                                sRow = (String) cmd.getProperty("c_name_row");
                                sCol = (String) cmd.getProperty("c_name_col");
                                sSubCol = (String) cmd.getProperty("c_name_subcol");
                                formula = (String) cmd.getProperty("c_formula");

                                //  eGrup.setAttribute("name", name);
                            } catch (Exception e) {

                                setLoggerInfo("Создание XML Документа", e);
                            }

                            // Редактор параметра
                            // eGrup,eCol,eSubCol, eRow - Редакторы
                            // bGrup,bCol,bSubCol, bRow-Кнопки
                            // Совпадение с группой команды
                            if (nameGrup.equals(grup)) {

                                if (formula != null && !formula.isEmpty()) {

                                    alEditor = Work.getListByDelim(formula, "//");

                                } else {
                                    alEditor.clear();

                                }

                                for (String s : alEditor) {

                                    if (s.contains("%")) {

                                        String[] sas = s.split("%");

                                        String poz = sas[0];
                                        String nam = sas[1];

                                        if (poz.contains("1")) {

                                            cmd.putProperty(nam, 1);
                                        } else {
                                            cmd.putProperty(nam, 2);
                                        }

                                    }

                                }

                                // Устанавливаем скрипты для кнопок
                                // Проверяем, если редактор и (или) кнопка для группы или команда для листинга
                                for (String s : alEditor) {

                                    if (s.startsWith("grup_1")) {
                                        eGrup.setAttribute("col_1", name + "#" + s);

                                        // if (s.contains("%")) {
                                        //   eGrup.setAttribute("button1", name);
                                        // }
                                    }
                                    if (s.startsWith("grup_2")) {
                                        eGrup.setAttribute("col_2", name + "#" + s);

                                        //  if (s.contains("%")) {
                                        //    eGrup.setAttribute("button2", name);
                                        // }
                                    }

                                    // Кнопки для группы (у них нет имен)
                                    if (s.contains("grup") && s.contains("%")) {
                                        eGrup.setAttribute("script", name);
                                    }

                                    // Список счетчиков
                                    if (s.equals("grup_1&CellSelectBox")) {

                                        //       Integer idPoint = (Integer) hmProperty.get("@point");
                                        format = Work.getCountersByPoint(idPoint);
                                        eGrup.setAttribute("format", format);
                                        String names = Work.getCountersByPointJson(idPoint);
                                        eGrup.setAttribute("name_counters", names);
                                        eGrup.setAttribute("counters", "ok");
                                        hmCommansTree.put("ListCountersCont", eGrup);

                                    }
                                }
                                eCol = null;
                                eSubCol = null;

                                if (sCol != null && !sCol.trim().isEmpty()) {

                                    eCol = document.createElement("col");
                                    eCol.setAttribute("caption", sCol);
                                    eCol.setAttribute("name", name);
                                    eCol.setAttribute("value", value);
                                    eCol.setAttribute("select", "0");
                                    eCol.setAttribute("measure", measure);
                                    eCol.setAttribute("format", format);

                                    // Проверяем, если редактор или кнопка для столбца
                                    for (String s : alEditor) {

                                        if (s.startsWith("col_1")) {
                                            eCol.setAttribute("col_1", name + "#" + s);
                                            //  eCol.setAttribute("poz", "1");
                                        }
                                        if (s.startsWith("col_2")) {
                                            eCol.setAttribute("col_2", name + "#" + s);
                                            //   eCol.setAttribute("poz", "2");
                                        }
                                    }
                                }

                                if (sSubCol != null && !sSubCol.trim().isEmpty()) {

                                    eSubCol = document.createElement("subcol");
                                    eSubCol.setAttribute("caption", sSubCol);
                                    eSubCol.setAttribute("name", name);
                                    eSubCol.setAttribute("value", value);
                                    eSubCol.setAttribute("select", "0");
                                    eSubCol.setAttribute("measure", measure);
                                    eSubCol.setAttribute("format", format);

                                    // Проверяем, если редактор или кнопка для подстолбца
                                    for (String s : alEditor) {

                                        if (s.startsWith("subcol_1")) {
                                            eSubCol.setAttribute("col_1", name + "#" + s);
                                            //  eSubCol.setAttribute("poz", "1");
                                        }
                                        if (s.startsWith("subcol_2")) {
                                            eSubCol.setAttribute("col_2", name + "#" + s);
                                            //  eSubCol.setAttribute("poz", "2");
                                        }
                                    }
                                }

                                if (sRow != null && !hmRow.containsKey(sRow) && !sRow.isEmpty()) {
                                    // Такой строки нет

                                    hmCol.clear();

                                    eRow = document.createElement("row");

                                    eRow.setAttribute("caption", sRow);
                                    eRow.setAttribute("name", name);
                                    eRow.setAttribute("value", value);
                                    eRow.setAttribute("select", "0");
                                    eRow.setAttribute("measure", measure);
                                    eRow.setAttribute("format", format);
                                    // Проверяем, если редактор или кнопка для строки

                                    for (String s : alEditor) {

                                        // Кнопки для группы (у них нет имен)
                                        if (s.contains("row") && s.contains("%")) {
                                            eRow.setAttribute("script", name);
                                        }

                                        if (s.startsWith("row_1")) {
                                            eRow.setAttribute("col_1", name + "#" + s);
                                            // eRow.setAttribute("poz", "1");
                                        }
                                        if (s.startsWith("row_2")) {
                                            eRow.setAttribute("col_2", name + "#" + s);
                                            // eRow.setAttribute("poz", "2");
                                        }
                                    }

                                    eGrup.appendChild(eRow);
                                    hmRow.put(sRow, eRow);

                                    if (eCol != null && !hmCol.containsKey(sCol)) {

                                        eRow.appendChild(eCol);

                                        eRow.setAttribute("name", "");
                                        hmCol.put(sCol, eCol);

                                        if (eSubCol != null) {
                                            eRow.setAttribute("col_1", name + "#" + "row_1&CellExtLabel");

                                            eCol.setAttribute("col_1", name + "#" + "col_1&CellExtValues");

                                            for (String s : alEditor) {

                                                if (s.startsWith("subcol_1")) {
                                                    eCol.setAttribute("col_1", name + "#" + s);
                                                    // eCol.setAttribute("poz", "1");
                                                }
                                            }
                                            eRow.setAttribute("subcol", sSubCol);

                                        }

                                    } else if (eCol != null) {
                                        //   hmRow.get(sRow).appendChild(eCol);
                                    }
                                } else {

                                    eRow = hmRow.get(sRow);

                                    if (eCol != null && !hmCol.containsKey(sCol)) {

                                        eRow.appendChild(eCol);
                                        eRow.setAttribute("name", "");

                                        hmCol.put(sCol, eCol);

                                        if (eSubCol != null) {

                                            eCol.setAttribute("col_1", name + "#" + "col_1&CellExtValues");

                                            for (String s : alEditor) {

                                                if (s.startsWith("subcol_1")) {
                                                    eCol.setAttribute("col_1", name + "#" + s);

                                                }
                                            }
                                        }
                                    } else if (eSubCol != null) {

                                        eCol.setAttribute("col_1", name + "#" + "col_1&CellExtValues");

                                        for (String s : alEditor) {

                                            if (s.startsWith("subcol_1")) {
                                                eCol.setAttribute("col_1", name + "#" + s);

                                            }
                                        }
                                    }
                                }

                                // Если в группе нет подчиненных то
                                // прсваиваем имя
                                if (!eGrup.hasChildNodes()) {

                                    eGrup.setAttribute("name", name);

                                    //Устанавливаем признак очищения перед новым запросом
                                    eGrup.setAttribute("clear", "1");

                                }
                            }
                        }
                    }
                } finally {
                    rs.getStatement().close();

                }

            } catch (SQLException ex) {

                setLoggerInfo("Создание XML Документа", ex);

            } // Проверяем какая связь
            typConnect = (String) hmPoint.get("typ_connect");

            // Создаем контакт между командой и деревом
            this.docTree = document;

            createContactCmdElm();

            // проверяем  видимость ячеек
            checkVisibleCellTree();

            hmdocTree.put(all_typ, document);

        } finally {

            CursorToolkitOne.stopWaitCursor(panel);
        }

        return document;

    }

    // Очищаем значение в дереве при одиночном запросе;
    public void clearValuesTree() {

        for (Element element : hmCommansTree.values()) {

            element.setAttribute("value", "");
            element.setAttribute("base", "");

        }

    }

    /**
     * Выполнение процедур перед запросом текущей команды
     *
     * @param -имя команды
     */
    private void runBeforeCommands(String nameCmd) {

        // Выполняем  функции  исполнения команды которые  выполняются перед командой
        CommandGet commandGet;
        commandGet = findCommand(nameCmd);

        if (commandGet != null) {
            invokeMetod(commandGet, GO_BEGIN);

        }
    }

    /**
     * Добавить дополнительные процедуры которые должны выполнятся после запроса
     */
    private void addEndProcedures(List<String> list) {

        CommandGet commandGet;

        for (String nameCmd : list) {

            commandGet = getCommandByName(nameCmd);

            addCommandGoEnd(
                    commandGet);

        }

    }

    /**
     * Выполнение команд перед запросом и добавление команд которые должны
     * выполнятся после запроса
     *
     * @param list
     */
    private String runAndAddCmd(List<String> list) {

        // Выполняем  функции  исполнения команды которые  выполняются перед запросом
        CommandGet commandGet;

        String result = null;

        String nameCmd;

        for (int i = 0; i
                < list.size(); i++) {

            nameCmd = list.get(i);

            commandGet = getCommandByName(nameCmd);

            if (commandGet == null) {

                return nameCmd;
            }

            invokeMetod(commandGet, GO_BEGIN);


        }

        //  добавляем команды которые
        // выполняются после запроса
        for (String name : list) {

            commandGet = getCommandByName(name);

            addCommandGoEnd(
                    commandGet);

        }
        return result;

    }


    /**
     * Установка команд по модели
     *
     * @param model
     * @return
     */
    private boolean setCommandsByModel(String model) {

        boolean result = false;

        if (hmAllCommands.containsKey(model)) {

            hmCommands = hmAllCommands.get(model);

            // Очищаем
            clearCommandsModel();

            //    clearButtonsAndAnswer();
            result = true;

        }

        return result;

    }

    /**
     * Если есть контроллер то добавляем команды
     */

    private void setCommandsController(Integer idPoint) throws Exception {

        // Добавляем параметры контроллера, если он есть
        hmController = Work.getMapController(idPoint);

        if (!hmController.isEmpty()) {

            typContoller = (String) hmController.get("typ_controller");

            TreeMap<String, CommandGet> tmCont = getCommandsByModel(typContoller);

            hmAllCommands.put(typContoller, tmCont);


        }


    }

    /**
     * Список команд всех типов приборов запроса
     */
    private void setBeforeCommandsModels(ArrayList<Integer> alPribors) throws Exception {

        TreeMap<String, CommandGet> tmModCmd;

        hmCaptions.clear();

        for (Integer id : alPribors) {

            try {

                hmProperty = Work.getParametersObject(id, null, true, false, true);

            } catch (SQLException ex) {

                setLoggerInfo("", ex);

                continue;

            }

            String nameObject = (String) hmProperty.get("caption");

            if (!hmCaptions.containsKey(id)) {

                hmCaptions.put(id, nameObject);

            } // Устанавливаем  модель прибора
            String model = getModelPribor(nameTable);

            if (!hmAllCommands.containsKey(model)) {

                try {
                    tmModCmd = getCommandsByModel(model);
                } catch (Exception e) {

                    setLoggerInfo(model, e);
                    continue;
                }

                hmAllCommands.put(model, tmModCmd);

            }
        }

    }

    /**
     * Предварительно выбранные команды
     */
    private boolean setBeforeCommandsSelect() throws Exception {

        boolean result = true;

        if (alSelect != null) {

            alSelect.clear();

        } else {
            alSelect = new ArrayList();

        }

        if (currentCommands == null) {
            if (docTree != null) {

                alSelect = getCommandsInDocument(docTree);

                if (alSelect.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Не выбраны параметры запроса !", "Запрос данных", JOptionPane.ERROR_MESSAGE);
                    ((Port) serialPort).stop();

                    //  serialPort.stop();
                    setNotifyObservers(esStopCommands);
                    return false;

                }

            }

        } else {
            String[] cmds = currentCommands.split(",");

            alSelect = new ArrayList(Arrays.asList(cmds));

        }

        return result;
    }

    public void setCurrentCommands(String currentCommands) {
        this.currentCommands = currentCommands;

    }

    /**
     * Создание параметров для записи в базу из скрипта
     */
    private void putValueByScript(CommandGet cg, Map<Timestamp, Object> mapVal) throws Exception {

        if (cg.result == null) {
            return;
        }
        Timestamp ts = (Timestamp) cg.getProperty("value_date");

        if (ts == null) {

            return;
        }

        // Проверяем на сохранение  в  базе
        // if (!mapVal.containsKey(ts)) {
        //   return;
        // }
        // String save_script = (String) cg.getProperty("save_script");

        if (!cg.bSave) {

            return;
        }

        if (bitSetFlags.get(BSF_BUTTON_ON)) {

            return;
        }

        // if (save_script == null || save_script.isEmpty()) {

        // Данные в базе не хронятся
        //   return;

        // }
        try {

            Object value;

            //  Integer pcol = (Integer) cg.getProperty("idnCol");
            //  Integer prow = (Integer) cg.getProperty("idnRow");

            value = evalScript(TS_SAVE, cg);


            if (value != null) {
                mapVal.put(ts, value);
            }


        } catch (Exception ex) {
            setLoggerInfo(ex.getMessage(), ex);
        }

    }

    /**
     * Предварительная Запись запрошенных значений группы(например профиль) в
     * текущую карту значений;
     */
    private void putGroupValueInMap(Map<Timestamp, CommandGet> mapCmd, Map<Timestamp, Object> mapVal) throws Exception {

        for (Timestamp t : mapCmd.keySet()) {

            CommandGet cg = mapCmd.get(t);
            try {

                if (mapVal.containsKey(t)) {
                    putValueByScript(cg, mapVal);
                }

            } catch (Exception ex) {
                setLoggerInfo("", ex);
            }

        }
    }

    private int getCountCmd(List<CommandGet> list) {

        int result = 0;

        int childs = 0;

        for (CommandGet cg : list) {

            if (cg.tmChilds != null) {

                childs = cg.tmChilds.size();

                result = result + childs;

            } else {

                result++;

            }

        }

        return result;

    }

    private String getCountValues(Integer idPribor) {

        StringBuilder builder = new StringBuilder();

        String result = "";

        HashMap<String, Map<Timestamp, Object>> hmValues = null;
        // hmValues = hmCmdSendGoValues.get(idPribor);

        Map<Timestamp, Object> valueMap;

        if (hmValues != null) {

            for (String nameCmd : hmValues.keySet()) {
                valueMap = hmValues.get(nameCmd);

                if (valueMap != null) {
                    builder.append(nameCmd);
                    builder.append("[");
                    builder.append(valueMap.size());
                    builder.append("]");
                    builder.append(";");

                }
            }
        }

        result = builder.toString();

        return result;

    }

    private void sendErrorMessge(Integer idPoint, Integer idObject, Exception ex) {

        String msgError = "";

        if (ex instanceof ErrorCommandException) {

            ErrorCommandException exception = (ErrorCommandException) ex;
            msgError = exception.getErrorCmd();
            setLoggerInfo(exception.getErrorCmd(), null);
        } else {
            setLoggerInfo("Канал связи", ex);
            msgError = ex.getMessage();
        }

        if (idPoint != null) {
            setMessageByIdPoint(
                    idPoint, msgError, null);
        }

        if (idObject != null) {
            mapMessageProcess.setInfoProcess(idObject, msgError);
        }

        setNotifyObservers(mapMessageProcess);
        return;
    }

    private void setEnabledObj() {

    }

    /**
     * Запрос данных с приборов учета одного присоединения
     *
     * @param idPoint точка присоединение
     */
    private void getValueFromPribors(int idPoint) throws Exception {

        List<Integer> lObjects;
// По умолчанию    установлен влаг использовать повторяющиеся команды
        bitSetFlags.set(BSF_REPEAT_ON);

        // if (serialPort.isStop()) {
        //   return;

        //TODO  Запрос данных с приборов учета одного присоединения

        try {
// Начинаем...

            try {

                //Контроллер

                // Добавляем параметры контроллера, если он есть
                //  hmController = Work.getMapController(idPoint);


                lObjects = hmChannels.get(idPoint);

                // Set<Integer> list = hmCmdSend.keySet();
                // lObjects = new ArrayList(list);

                try {
                    // Установка параметров текущего присоединения

                    openChannel(idPoint);
                } finally {
                    //  setNotifyObservers(esGoCommands);


                    //Если ошибка или объкт не доступен
                    if (bitSetFlags.get(BSF_ERROR) || bitSetFlags.get(BSF_NO_CARRIER)) {

                        addPointInBlackList(idPoint, MapMessageProcess.DATA_BLACK);
                        return;

                    }

                    //Номер занят
                    if (bitSetFlags.get(BSF_BUSY)) {

                        return;

                    }


                }


            } catch (Exception ex) {
                sendErrorMessge(idPoint, null, ex);
                addPointInBlackList(idPoint, ex.getMessage());
                runCommandByScript(typConnect, TS_CHECK);
                setNotifyObservers(esStopCommands);
                return;
            }

// Если канал открыт то продолжаем
            if (((Port) serialPort).isStop()) {
                return;

            }

            //   sendSMS("Привет", "89172562616");
            if (bitSetFlags.get(ValuesByChannel.BSF_TRANS_PROTOKOL)) {
                // Уставки транспортного протокола

                Integer idController = (Integer) hmProperty.get("id_controller");

                CommandGet cg = setParamPort228(idController);

                String controller = typContoller;

                typContoller = null;
                // если есть команда уставок

                if (cg != null) {

                    alRun.clear();

                    alRun.add(cg);
                    try {
                        blinkText("Уставки транспортного протокола");

                        Thread.sleep(100);

                        question(currOperation, alRun, time_aut, sys_time, count_sql, null, 0);
                        setController.add(idController);


                    } finally {
                        stopBlinkText();
                    }

                }
                typContoller = controller;


                // Устанавливаем пакетный режим
                bitSetFlags.set(BSF_BUFER_PACK);

            }


            //   overpackWrapp=new OverpackWrapp(hmModem);
            for (Integer idObject : lObjects) {


                currIdObject = idObject;

                mapMessageProcess.setInfoProcess(idObject, MapMessageProcess.DATA_GO);
                setNotifyObservers(mapMessageProcess);


                if (((Port) serialPort).isStop()) {

                    return;

                }

                try {

                    hmProperty = Work.getParametersRow(idObject, null, nameTable, true, false);


                    if (!hmController.isEmpty()) {

                        hmProperty.putAll(hmController);

                    }


                    // Записываемые данные
                    if (!hmWrite.isEmpty()) {
                        hmProperty.putAll(hmWrite);

                    }

                } catch (SQLException ex) {

                    setLoggerInfo("Запрос параметров текущего объекта", ex);

                    continue;

                } // Устанавливаем  модель прибора
                String model = getModelPribor(nameTable);

                // Если текущая модель не совпадает с существующей
                if (modelPribor == null || !modelPribor.equals(model)) {

                    modelPribor = model;

                }

                alEnd.clear();

                // команды под текущую модель прибора
                // setCommandsByModel(model);
                setSelectCommandsByPribor(
                        idObject);

                /**
                 * Выполнение команд перед запросом и добавление команд которые
                 * должны выполнятся после запроса
                 */


                String ncmd = runAndAddCmd(alSelectPribor);


                if (ncmd != null) {

                    addCmdInBlackList(idObject, "Не существует команды-" + ncmd + "!", 1);

                    continue;
                }


                addEndProcedures(
                        alSelectPribor);

                addAdditionCommands(
                        alSelectPribor);

                setDeffCommand();

                clearYes();

                alRun.clear();

                addCommandInRun(
                        alAddition, false);
                addCommandInRun(
                        alSelectPribor, true);
                addMakeCommandInRun();

                //Если не ручной режим то команды очищаем
                if (typRegime != REJIM_GET_HAND) {
                    clearCommandsGet(alRun);

                }

                setLoggerInfo("==================================================", null);

                setLoggerInfo(
                        "Запрос данных... Объект: " + hmCaptions.get(idObject), null);

                if (B_LOG_TXD) {

                    setLoggerInfo("Команды добавленные: " + alAddition.toString(), null);
                    setLoggerInfo(
                            "Команды выбора: " + alSelectPribor.toString(), null);
                    setLoggerInfo(
                            "Данные: " + getCountValues(idObject), null);

                }
                setLoggerInfo("===================================================", null);

                setInquiryProcess(
                        hmCaptions.get(idObject));

                try {

                    if (bitSetFlags.get(BSF_BUFER_PACK)) {
                        // Пакетный режим

                        //Команда уставок порта
                        //   CommandGet cgPort = hmCommands.get("command5");
                        // cgPort.countTry = 3;
                        // cgPort.waitTime = time_aut;
                        // serialPort.setCommandPort(cgPort);
                        // questionOverPack(alRun, idObject);
                    }

                    hmRepeat = new HashMap();

                    // выполняем команды и флаги для текущего типа связи
                    // для каждого объекта (object)
                    runCommandByScript(typConnect, TS_RESULT);

                    int cmd_size = getCountCmd(alRun);

                    setMinMaxValue(0, cmd_size);

                    CommandGet result;


                    result = question(currOperation, alRun, time_aut, sys_time, count_sql, idObject, 0);

                    //  System.out.println("Все команды отправлены в порт.");

                    if (result != null) {

                        mapMessageProcess.setInfoProcess(idObject, result.errorCmd);

                        // sendErrorMessge(null, idObject, ex);

                    } else {
                        mapMessageProcess.setInfoProcess(idObject, MapMessageProcess.DATA_GET);
                        setNotifyObservers(mapMessageProcess);
                        setErrorsPribor(idObject, model, "ДАННЫЕ ПОЛУЧЕНЫ.");
                    }

                } catch (Exception ex) {

                    sendErrorMessge(null, idObject, ex);

                }

                if (((Port) serialPort).isStop()) {
                    return;

                }
            }

            if (bitSetFlags.get(BSF_BUFER_PACK)) {
                //   try {
                //     try {
                //blinkText("Пакетный запрос данных...");

                //  serialPort.doSendOverpack();
                //   } finally {
                // stopBlinkText();
                // }
                //Начинаем пакетный запрос
                //  } catch (InterruptedException ex) {
                //    setLoggerInfo("", ex);
                // }
            }

        } finally {
            //Закрываем канал связи
            checkEnd();
            //    closePort();
// Записываем информацию

            saveInfoObjects();

            try {
                mapMessageProcess.saveMessagesProcess();

            } catch (SQLException ex) {
                setLoggerInfo("Запрос данных", ex);

            }

        }

    }

    /**
     * Добавляем сообщения что данные есть в базе
     */
    private void addMsgValueBase(int idP, ArrayList<Integer> alObjOk) {

        //  if (hmChannels == null) {
        //    return;
        // }
        // ArrayList<Integer> alObjectPoint = hmChannels.get(idP);
        for (int idOb : alObjOk) {

            mapMessageProcess.setInfoProcess(idOb, MapMessageProcess.DATA_BASE);

        }

        // }
    }

    private boolean isOkeyPoint(Integer idPoint, List<Integer> objects) {

        boolean result = true;


        for (Integer i : objects) {
            mapMessageProcess.setInfoProcess(i, MapMessageProcess.DATA_BASE);
        }
        return result;

    }

    /**
     * Запрос данных по группе объектов c одного присоединения
     *
     * @param idPoint -Присоединение
     * @param alGroup группа объектов одного присоединения
     * @return
     */
    public void getValueFromGroupEx(Integer idPoint, ArrayList<Integer> alGroup) {
        hmCaptions.clear();
        hmFirst.clear();

        // TODO:  Запрос данных по группе объектов c одного присоединения


        if (hmParJSon != null) {
            hmParJSon.clear();
        }

        alAddition.clear();
        hmWrite.clear();
        typContoller = null;
        typPack = null;
//Устанавливаем режим чтения/записи данных
        //  bitSetFlags.set(BSF_RUN_COMMANDS);
        bitSetFlags.clear(BSF_GSM_YES);
// Устанавливаем комады для всех  типов приборов запроса
        try {


            // Устанавливаем комады для всех  типов приборов запроса
            setBeforeCommandsModels(
                    alGroup);


            // Устанавливаем текущие команды контроллера

            setCommandsController(idPoint);


// Предварительно выбраные команды
            //   setBeforeCommandsSelect();
            //setCommandsInGroup(alGroup);
        } catch (Exception ex) {
            setLoggerInfo("Предварительные команды", ex);
        }

        // if (currOperation == CMD_SET) {
        //   try {
        // Создаем команды для записи
        //     createWriteCmd();
        // } catch (Exception ex) {
        //   setLoggerInfo("Команды для записи", ex);
        // }

        // }

        ArrayList<Integer> alObjOk = null;
        //TODO: Проверяем данные по группе объектов


        // возвращаем объекты у которых  все собрано
        alObjOk = checkValueInBase(alGroup);

        //Удаляем присоединения у которых все собрано

        if (alObjOk.size() == alGroup.size()) {
            // все у всех собрано
            //рисуем зеленым если есть в базе
            addMsgValueBase(idPoint, alObjOk);

            setNotifyObservers(
                    mapMessageProcess);

        }


        if (alObjOk.size() != alGroup.size()) {

            try {

                try {
                    // не все есть в базе, Опрашиваем...
                    getValueFromPribors(idPoint);
                } finally {

                    runCommandByScript(typConnect, TS_FIND);

                    closePort();
                }

            } catch (Exception ex) {

                setLoggerInfo("", ex);
            }

        } else {
            // Все данные есть в базе

            setMessageByIdPoint(idPoint, MapMessageProcess.DATA_BASE, alObjOk);
            // setNotifyObservers(
            //       mapMessageProcess);

            try {
                mapMessageProcess.saveMessagesProcess();

            } catch (SQLException ex) {
                setLoggerInfo("Запись логов", ex);

            }
        }

        checkVisibleCellTree();

        try {
            // сохраняем полученные данные

            saveValues();

            // Сохраняем дополнительные данные
            saveJsonValues();
// saveValusInTable();

        } catch (SQLException ex) {

            setLoggerInfo("Запись данных в таблицу", ex);

        }

        //   runCommandByScript(typConnect, "write");
    }

    private void addMakeCommandInRun() {

        alRun.addAll(alMake);

    }

    public void addJsonParam(Integer id, String Name, Object value) {

        if (hmParJSon == null) {

            hmParJSon = new HashMap<>();

        }

        HashMap<String, Object> hmParam;

        if (hmParJSon.containsKey(id)) {

            hmParam = (HashMap<String, Object>) hmParJSon.get(id);
        } else {
            hmParam = new HashMap<>();
            hmParJSon.put(id, hmParam);

        }

        hmParam.put(Name, value);

    }

    private void addCommandInRun(LinkedList<String> al, boolean setOne) {

        CommandGet cg;

        int size = alRun.size();

        for (String name : al) {

            cg = getCommandByName(name);

            if (cg != null) {

                if (cg.criticalError && setOne) {
                    // признак одиночной команды
                    alRun.add(size, cg);

                } else {

                    alRun.add(cg);
                }
            } else {

                setLoggerInfo("Команда '" + name + "' не обнаружена ! ", null);

            }

        }

    }

    /**
     * проверяем команду на запуск
     *
     * @param nameCmd имя команды
     * @return
     */
    private boolean isRunCommand(Integer idPribor, String nameCmd) {

        //   String selCmd = hmSelects.get(idPribor);
        //   boolean result = selCmd.contains(nameCmd);
        return true;
        //return result;

    }

    // Устанавливаем выбраные команды для текущего опрашиваемого объекта
    public void setSelectCommandsByPribor(Integer idPribor) {

        alSelectPribor.clear();

        alSelectPribor.addAll(alSelect);

        //  for (String cgName : alSelect) {
        //    if (isRunCommand(idPribor, cgName)) {
        //      alSelectPribor.add(cgName);
        //}
        //}
    }


    /**
     * Открывает свободный сом порт для GSM связи по модему
     */
    public String getFreeGsmPort(ParamPort paramPort) throws Exception {


        String namePort = null;

        String[] namesPorts = jssc.SerialPortList.getPortNames();


        for (int i = 0; i < namesPorts.length; i++) {

            namePort = namesPorts[i];

            paramPort.setNamePort(namePort);

            if (serialPort.openPort(paramPort)) {

                //Проверяем, есть ли но нем GSM модем

                alRun.clear();

                CommandGet command = hmModem.get("CheckModem");
                alRun.add(command);

                // command = hmModem.get("GetCommandOff");
                // alRun.add(command);
                //  command = hmModem.get("BalanseGsm");

                //   alRun.add(command);
                try {

                    if (question(CMD_GET, alRun, 1000, 5, 3, null, 0) == null) {

                        return namePort;
                    } else {

                        namePort = null;
                    }


                } catch (Exception ex) {

                    setLoggerInfo("Запрос", ex);
                    return null;

                }
                serialPort.closePort();

            }


        }

        return namePort;

    }

    /**
     * Устанавливаем параметры связи и открываем порт выполняем скрипт "set" для
     * текущей связи и скрипт "create" для текущего контроллера
     *
     * @param id -точка присоединение (Point)
     * @throws Exception
     */
    private void openChannel(int id) throws Exception {

// TODO: Открываем канал связи
        boolean result = false;

        // Параметры текущего канала связи
        hmPoint = Work.getParametersRow(id, null, "points", false, true);

        hmPoint.put("module_work", this);

        hmProperty = hmPoint;

        if (hmPoint.isEmpty()) {

            throw new Exception("Точки подключения с номером " + id + " не существует.");

        }

        if (alGroup == null) {
            alGroup = new ArrayList();

        } else {
            alGroup.clear();

        }

        String nameChannel = (String) hmPoint.get("caption");

        // Проверяем какая связь
        typConnect = (String) hmPoint.get("typ_connect");

        setLoggerInfo(
                "=================================================", null);
        setLoggerInfo(
                "Соединение: " + nameChannel + "[" + id + "][" + typConnect + "]", null);

        setTypProcess(
                nameChannel);

        // if (overpack) {
        //   inspSerialPort.addObserver(this);
        // }
        // if (portGet != null) {

        //при опросе по расписанию
        //   hmPoint.put("comm_port", portGet);

        // }

        // typContoller = null;
        // typPack = null;
        ((Port) serialPort).setComPack(null);

        ((Port) serialPort).setChannel(this);


        try {

            //TODO  Установка параметров порта
            ParamPort paramPort = new ParamPort(hmPoint, HM_LOCALE, portSchesule);


            //  String namePort = getFreeGsmPort(paramPort);

//            paramPort.setNamePort(namePort);


            serialPort.openPort(paramPort);

        } catch (Exception e) {

            errorString = "Не удалось открыть порт " + serialPort.getPortName();
            throw new Exception(errorString, e);

        }

        hmPoint.put("inspSerialPort", serialPort);

        setLoggerInfo(
                "COM-Порт: " + serialPort.getPortName(), null);

        // Ожидание ответа
        time_aut = (Integer) hmPoint.get("time_aut");
        // Пауза между посылкой команд
        sys_time = (Integer) hmPoint.get("sys_time");
        // попыток дозвона
        count_sql = (Integer) hmPoint.get("count_sql");

        // выполняем команды и флаги для текущего типа связи
        // для каждого присоединения (point)
        runCommandByScript(typConnect, TS_GREATE);

        // bitSetFlags.set(BSF_GSM_YES);
        // Добавляем параметры контроллера, если он есть
        //  hmController = Work.getMapController(id);

        if (!hmController.isEmpty()) {

            hmPoint.putAll(hmController);
            // typContoller = (String) hmController.get("typ_controller");

            //  if(hmController.get("str1"))
            //  контроллер со своим протоколом упаковки данных
            CommandGet cmdPack = hmModem.get(typContoller);
            ((Port) serialPort).setComPack(cmdPack);

        }

        if (typContoller != null) {
            //Добавляем или выполняем команды и флаги для текущего типа контроллера
            runCommandByScript(typContoller, TS_GREATE);

        }

        // Выполняем скрипт после присоединения
        //    if (comScript != null) {
        //
        //}
    }

    private void setMessageByIdPoint(int idP, String msg, ArrayList<Integer> alObjErr) {

        if (hmChannels == null) {

            return;
        }

        ArrayList<Integer> alObject = hmChannels.get(idP);

        for (int idO : alObject) {

            if (alObjErr == null) {
                mapMessageProcess.setInfoProcess(idO, msg);

            } else if (!alObjErr.contains(idO)) {
                mapMessageProcess.setInfoProcess(idO, msg);

            }

        }

    }

    // Создаем карту Объектов по каналам
    private void createMapChannel() throws SQLException {

        hmChannels = new HashMap();

        mapMessageProcess = new MapMessageProcess();
        setNotifyObservers(
                mapMessageProcess);

        rsObjects.beforeFirst();

        ArrayList alObject;

        while (rsObjects.next()) {

            int row = rsObjects.getRow() - 1;

            // Канал
            Integer idPoint = rsObjects.getInt("id_point");

            //Обект
            Integer idObject = rsObjects.getInt("id_object");

            mapMessageProcess.setRow(idObject, row);

            if (idPoint == null || idPoint <= 0) {
                mapMessageProcess.setInfoProcess(idObject, "Нет точки подключения !");

                continue;

            }

            if (hmChannels.containsKey(idPoint)) {

                alObject = hmChannels.get(idPoint);

            } else {

                alObject = new ArrayList();

                hmChannels.put(idPoint, alObject);

            }
            alObject.add(idObject);
        }
        setNotifyObservers(mapMessageProcess);
    }
}

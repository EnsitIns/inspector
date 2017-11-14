/*
 * Класс выполнения задач
 * с визаулизацией потока.
 */
package servise;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static constatant_static.SettingActions.createIcon;
import static servise.ProcLogs.*;

/**
 *
 * @author 1
 */
public class MainWorker extends Observable implements Observer {

    private Worker worker;
    ExecutorService pool;
    private org.apache.log4j.Logger logger;  // Текущий логер
    public static ProcLogs LOGS;
    public static int ID_CUR_USER; // id текущего пользователя

    public static String jurnal = "channel.log";
    public static org.apache.log4j.Logger deffLoger = org.apache.log4j.Logger.getLogger("LogServer");
    private TreeMap<Object, Object> mapClient; //
    private boolean goProcess; // Проверка запуска процесса
    public String errorString; // Строка ошибки
    public static JButton buttonError; //Кнопка при ошибке
    public static HashMap<String, MainWorker> hmWorkers = new HashMap<>();
    public String currentTask;
    public Map mapProperties;
    public static boolean isStop; // остановка всех процесов
    public boolean stopProcess; // флаг остановки роцесса

    public static void setIsStop(boolean isStop) {
        MainWorker.isStop = isStop;
    
    
    }
    public MainWorker() {

        goProcess = false;
        mapClient = new TreeMap<Object, Object>();
        // по умолчанию
        logger = org.apache.log4j.Logger.getLogger("LogServer");

        isStop=false;
    }

    // Выполнить задачу
    public void executeTask(String currentTask, Map pMap) {
        this.mapProperties = pMap;
        this.currentTask = currentTask;
        executeProcess();
    }

//Вывод журнала с ошибками
    public void showLogGurnal() {

    }

    public void putProperty(Object key, Object value) {
        mapClient.put(key, value);
    }

    public Object getProperty(Object key) {
        return mapClient.get(key);
    }

    public void setGoProcess(boolean goProcess) {
        this.goProcess = goProcess;
    }

    public boolean isGoProcess() {
        return goProcess;
    }

    public static void setDeffLoger(Logger l) {

        deffLoger = l;
    }

    public static void setLogInfo(String msg, Throwable t) {

        if (t != null) {
            deffLoger.error(msg, t);

            if (buttonError != null) {

                ImageIcon icon = createIcon("error.png");

                buttonError.setIcon(icon);

            }

        } else {
            deffLoger.info(msg);
        }
    }

    public static void ShowError(String error) {

        JOptionPane.showMessageDialog(null, error, "Ошибка", JOptionPane.ERROR_MESSAGE);

    }

    public void setLoggerInfo(String msg, Throwable t) {

        if (t != null) {
            logger.error(msg, t);

            if (buttonError != null) {

                ImageIcon icon = createIcon("error.png");

                buttonError.setIcon(icon);

            }

            String nameLogger = logger.getName();

            if (nameLogger.equals("LogExcel")) {

                jurnal = "excel.log";

            } else if (nameLogger.equals("LogServer")) {

                jurnal = "srv.log";
            } else if (nameLogger.equals("LogSoap")) {

                jurnal = "soap.log";
            } else if (nameLogger.equals("LogPiramida")) {

                jurnal = "piramida.log";
            } else if (nameLogger.equals("LogChannel")) {

                jurnal = "channel.log";
            } else if (nameLogger.equals("LogChannel1")) {

                jurnal = "channel1.log";
            } else if (nameLogger.equals("LogChannel2")) {

                jurnal = "channel2.log";
            } else if (nameLogger.equals("LogChannel3")) {

                jurnal = "channel3.log";
            } else if (nameLogger.equals("LogGroovy")) {

                jurnal = "groovy.log";
            }

        } else {

            logger.info(msg);
        }

    }

    /**
     * Запуск не в отдельном потоке
     *
     * @throws Exception
     */
    public void doInBackground() throws Exception {
        worker = new Worker();
        worker.doInBackground();

    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Запуск процесса в отдельном потоке
     */
    public void executeProcess() {

        worker = new Worker();
        if (pool != null) {

            pool.submit(worker);
        } else {

            worker.execute();
        }

    }

    @Override
    public void update(Observable o, Object arg) {
    }

    public void sleepProcess(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            setLoggerInfo("", ex);
        }
    }

    public void doProcess() {

        
    }

    public void endProcess() {

        if (worker != null) {

            if (errorString != null && !errorString.isEmpty()) {

                setNotifyObservers(new Object[]{worker, MESSAGE_ANSWER, errorString, MSG_ERROR});
            } else {

                setNotifyObservers(new Object[]{worker, MESSAGE_ANSWER, "Успешно !", MSG_OK});
            }

        }

    }

    //Запрос
    public void setTypProcess(String msg) {

        if (worker != null) {
            setNotifyObservers(new Object[]{worker, MESSAGE_TYP, msg, null});
        }

    }

    //Запрос
    public void setInquiryProcess(String msg) {

        if (worker != null) {
            setNotifyObservers(new Object[]{worker, MESSAGE_SQL, msg, null});
        }

    }

    public void answerProcess(String msg, String msgTyp) {

        if (worker != null) {
            setNotifyObservers(new Object[]{worker, MESSAGE_ANSWER, msg, msgTyp});
        }

    }

    public void newProcess(String msg) {
        if (worker != null) {
            setNotifyObservers(new Object[]{worker, MESSAGE_PROCESS, msg, MSG_LOG});
        }
    }

    public void stopBlinkText() {
        if (worker != null) {
            setNotifyObservers(new Object[]{worker, false, new String()});
        }
    }

    public void blinkText(String msg) {
        if (worker != null) {
            setNotifyObservers(new Object[]{worker, true, msg});
        }
    }

    public void refreshBarValue(Object v) {

        if (worker != null) {
            worker.refreshBarValue(v);
        }
    }

    public void setMinMaxValue(Integer min, Integer max) {

        if (worker != null) {
            setNotifyObservers(new Object[]{worker, new Point(min, max)});
        }

    }

    // Передача всяких уведомлений
    public void setNotifyObservers(Object notyfy) {

        setChanged();
        notifyObservers(notyfy);

    }

    class Worker extends SwingWorker<Object, Object> {

        @Override
        protected void done() {

            endProcess();

        }

        @Override
        protected Object doInBackground() throws Exception {

            doProcess();

            return null;
        }

        public void refreshBarValue(Object v) {
            publish(v);
        }

        public void setMinMaxValue(Integer min, Integer max) {
            setNotifyObservers(new Object[]{this, new Point(min, max)});
        }

        @Override
        protected void process(List<Object> chunks) {

            for (Object obj : chunks) {
                setNotifyObservers(new Object[]{this, obj});
            }
        }
    }
}

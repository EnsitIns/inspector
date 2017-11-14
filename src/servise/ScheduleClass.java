package servise;

import connectdbf.SqlTask;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.TimerTask;

import static constatant_static.SettingActions.*;

/*
 *  Класс управления расписаниями
 * and open the template in the editor.
 */
/**
 *
 * @author 1
 */
public class ScheduleClass extends Observable implements Observer {

    public static boolean isCheck = false;
    private long period;  // Период проверки расписаний в минутах
    private int interval;  // Погрешность проверки расписаний в +-секундах
    private String nameSchedule;//Название расписания

    public ScheduleClass() {

        period = 1;
        interval = 30;

    }

    public String getNameSchedule() {
        return nameSchedule;
    }

    public static String getNameSchedule(int id) throws SQLException {
        String result = null;
        String sql = "SELECT * FROM schedules WHERE id_schedule=?";
        ResultSet rs;

        rs = SqlTask.getResultSet(null, sql, new Object[]{new Integer(id)});

        try {

            while (rs.next()) {

                result = rs.getString("ch_name");
                result = result + ":" + rs.getString("ch_date_go");

            }

        } finally {
            rs.close();
        }

        return result;
    }

    public void StartSchedule(int id, String name) {
        nameSchedule = name;
        goSchedule(id);

    }

    public void Start() {

        java.util.Timer timer = new java.util.Timer(true);

        DateTime dateTime = new DateTime();

        dateTime = dateTime.secondOfMinute().setCopy(0);
        dateTime = dateTime.millisOfSecond().setCopy(0);
        dateTime = dateTime.plusMinutes(1);

        timer.schedule(new onCheck(), dateTime.toDate(), 1000 * 60 * period);

        setNotifyObservers("Монитор расписаний запущен.");

    }

    // Создать  отчет в ручном режиме
    public void createReportInExcel(String sqlTable, Date dateFirst, Date dateLast, String nameReport, Integer idSelect) {

        setNotifyObservers(new Object[]{CM_EXCEL_GO_REPORT, sqlTable, dateFirst, dateLast, nameReport, idSelect});

    }

    // Экспорт данных в Excel
    public void expotrToExcel(String sqlTable) {

        setNotifyObservers(new Object[]{CM_EXCEL_EXPORT, sqlTable});

    }

    // Обнуление коммерческих данных
    public void RefreshCommercDate(DateTime dtFirst, DateTime dtLast) {

        setNotifyObservers(new Object[]{CM_PIRAMIDA_COMMERC, dtFirst, dtLast});

    }

    // передача по протоколу 80020
    public void Refresh80020Date(DateTime dtFirst, DateTime dtLast) {

        setNotifyObservers(new Object[]{CM_PIRAMIDA_80020, dtFirst, dtLast});

    }

// Передача всяких уведомлений
    public void setNotifyObservers(Object notyfy) {

        setChanged();
        notifyObservers(notyfy);

    }

    
    //Остановка всех расписаний
    public void stopAll() {
        setChanged();
        notifyObservers(true);

    }

    private void goSchedule(int id) {

        //  setNotifyObservers("Запуск расписания:'" + nameSchedule + "'");
        setChanged();
        notifyObservers(id);

    }

    private class onCheck extends TimerTask {

        @Override
        public void run() {

            //  System.out.println(Thread.currentThread().getName());
            checkSchedules();
        }
    }

    /**
     * Проверка активных расписаний если localhost то выполняется на текущей
     * машине
     */
    private void checkSchedules() {

        if (isCheck) {
            setNotifyObservers("Проверка активных расписаний...");
        }
        DateTime dateTime = new DateTime();
        int i = dateTime.getMinuteOfDay();

        String sql;

        ResultSet rs;

        Integer i1 = 0;

        Object[] objects = null;

        Integer idUser = MainWorker.ID_CUR_USER;

        sql = "SELECT * FROM schedules WHERE ch_noactive=? AND (id_user=? OR id_user=?)";

        objects = new Object[]{1, idUser, 0};

        if (isCheck) {
            setNotifyObservers("IP-Адрес:'" + "'");
        }

        try {

            rs = SqlTask.getResultSet(null, sql, objects);

            try {

                while (rs.next()) {

                    Integer id = rs.getInt("id_schedule");

                    nameSchedule = rs.getString("ch_name");

                    if (isCheck) {
                        setNotifyObservers("ID-" + id + " '" + nameSchedule + "'");
                    }

                    if (isGo(rs, dateTime)) {

                        goSchedule(id);

                    }
                }

            } finally {
                rs.close();
            }

        } catch (SQLException ex) {

            setNotifyObservers(ex);
        }
    }

    private boolean isGo(ResultSet rs, DateTime CurDate) throws SQLException {
        boolean result = false;

        // Убираем секунды и миллисекунды
        DateTime dtBtwS; // Даты интервала старта
        // DateTime dtBtbE;

        CurDate = CurDate.secondOfMinute().setCopy(0).millisOfSecond().setCopy(0);

        DateTime SchedDate;// дата расписания

        java.sql.Timestamp ts;

        // Дата начала расписания
        ts = rs.getTimestamp("ch_date_go");

        SchedDate = new DateTime(ts.getTime());

        // Убираем секунды и миллисекунды
        SchedDate = SchedDate.secondOfMinute().setCopy(0).millisOfSecond().setCopy(0);

        if (isCheck) {

            setNotifyObservers("Дата начала расписания(ts из базы) " + ts);
            setNotifyObservers("Дата начала расписания  (DateTime)" + SchedDate);

        }

        long sd = SchedDate.getMillis();
        long sds = CurDate.minusSeconds(interval).getMillis();
        long sde = CurDate.plusSeconds(interval).getMillis();

        dtBtwS = CurDate.minusSeconds(interval);
        //  dtBtbE=CurDate.plusSeconds(interval);

        if (sd < sds) {
            if (isCheck) {
                setNotifyObservers("Пока не началось");
            }

        }

// Старт расписания
        if (sd >= sds && sd <= sde) {
            //   return true;
        }

        // Период
        Integer per = rs.getInt("ch_period");
        // через сколько
        // 0-Каждый день
        // -1 -нет повторов
        // >0 через

        Integer ch_repeat_day;
        ch_repeat_day = rs.getInt("ch_repeat_day");

        // через сколько миллисекунд повторять
        Integer ch_repeat_time = null;
        ch_repeat_time = rs.getInt("ch_repeat_time");

        // Количество повторов за сутки
        Integer ch_repeat_count = 0;
        ch_repeat_count = rs.getInt("ch_repeat_count");

        if (per == null || per == 0) {
            // День

            int CurDayOfYear = CurDate.getDayOfYear();
            int SchedDayOfYear = SchedDate.getDayOfYear();

            if (ch_repeat_day != null && ch_repeat_day >= 0) {

                int ost = (CurDayOfYear - SchedDayOfYear) % (ch_repeat_day + 1);

                if (isCheck) {
                    setNotifyObservers("Остаток=" + ost);
                }

                // Совпал день
                if (ost == 0 || ch_repeat_day == 0) {

                    if (isCheck) {
                        setNotifyObservers("Совпал день");
                    }
                    if (isRepeat(CurDate, SchedDate, ch_repeat_time, ch_repeat_count)) {
                        return true;
                    }

                }

                // Разовое расписание
            } else if (ch_repeat_day != null && ch_repeat_day < 0) {

                if (isCheck) {
                    setNotifyObservers("Разовое расписание");
                }

                if (isRepeat(CurDate, SchedDate, ch_repeat_time, ch_repeat_count)) {
                    return true;
                }

            }

        } else if (per == 1) {

            // День месяца
            int DayOfMonth = CurDate.getDayOfMonth();

            // Совпали дни месяца
            if (DayOfMonth == ch_repeat_day) {

                if (isCheck) {
                    setNotifyObservers("Совпал день месяца");
                }
                if (isRepeat(CurDate, SchedDate, ch_repeat_time, ch_repeat_count)) {
                    return true;
                }

            }

        } else if (per == 2) {

            //Дни недели
            int DayOfWeek = CurDate.getDayOfWeek() - 1;

            BitSetEx bs = new BitSetEx(ch_repeat_day);

            boolean b = bs.get(DayOfWeek);

            // Совпал день недели
            if (b) {

                if (isCheck) {
                    setNotifyObservers("Совпал день недели");
                }
                if (isRepeat(CurDate, SchedDate, ch_repeat_time, ch_repeat_count)) {
                    return true;
                }
            }
        }

        if (isCheck) {
            setNotifyObservers("Результат:" + result);
        }

        return result;

    }

    private boolean isRepeat(DateTime CurDate, DateTime SchedDate, Integer rTime, Integer rCount) {
        boolean result = false;
        long sd = 0;
        long sds = 0;
        long sde = 0;
        DateTime dt;
        DateTime dts;
        DateTime dte;
        LocalTime lt;
        LocalTime lts;
        LocalTime lte;

        if (rCount == null || rCount == 0) {
            rCount = 1;
        }

        for (int i = 0; i < rCount; i++) {

            dt = SchedDate.plusMillis(i * rTime);

            // Проверяем переход на другой день
            if (SchedDate.getDayOfMonth() != dt.getDayOfMonth()) {
                break;
            }

            lt = dt.toLocalTime();
            sd = lt.getMillisOfDay();

            dts = CurDate.minusSeconds(interval);
            lts = dts.toLocalTime();
            sds = lts.getMillisOfDay();

            dte = CurDate.plusSeconds(interval);
            lte = dte.toLocalTime();
            sde = lte.getMillisOfDay();

            if (isCheck) {
                setNotifyObservers("sd " + sd + "[" + lt + "]");
                setNotifyObservers("sds " + sds + "[" + lts + "]");
                setNotifyObservers("sde " + sde + "[" + lte + "]");
            }

            if (sd >= sds && sd <= sde) {
                return true;
            }

        }

        return result;
    }

    public void GoEvents() {
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof sockets.ServerTcp) {
            try {
                Integer idshed = (Integer) arg;

                nameSchedule = getNameSchedule(idshed);

                StartSchedule(idshed, nameSchedule);
            } catch (SQLException ex) {
            }
        }

    }
}

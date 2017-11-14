/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

/**
 * Слушатель всех уведомлений ошибок и инфо
 *
 * @author 1
 */
public class ProcLogs implements Observer {

    private JTable tblMsg;
    private Object[] ColName;
    private HashMap<Object, TableRow> hmProcess;
    private LinkedList<TableRow> llRow = new LinkedList<TableRow>();
    private org.apache.log4j.Logger logger;  // Текущий логер
    public static final String MSG_WARNING = "Внимание";
    public static final String MSG_OK = "ОК";
    public static final String MSG_LOG = "Сообщение";
    public static final String MSG_SQL = "Запрос";
    public static final String MSG_ERROR = "Ошибка !";
    public static final int STR_NEW = 0;
    public static final int STR_SQL = 1;
    public static final int STR_ANSWER = 2;
    public static final int MESSAGE_SIMPLE = 0; // Простое сообщение
    public static final int MESSAGE_PROCESS = 1; // Сообщение с визуальном процессом
    public static final int MESSAGE_ANSWER = 2; // Ответное сообщение
    public static final int MESSAGE_LOG = 3; // Сообщение поверх предыдущего
    public static final int MESSAGE_SQL = 4; // Сообщение поля  запрос
    public static final int MESSAGE_TYP = 5; // Сообщение поля тип
    public static Color CLR_OK = new Color(0, 153, 0);
    public static Color CLR_WARNING = Color.BLUE;
    public static Color CLR_ERROR = Color.RED;
    public static Color CLR_LOG = new Color(102, 102, 102);
    public static Color CLR_SQL = new Color(206, 123, 0);

    public ProcLogs() {

        this.tblMsg = null;
        init();
    }

    public ProcLogs(JTable table) {

        this.tblMsg = table;

        if (tblMsg != null) {

            ColName = new String[]{"Дата/Время", "Тип", "Запрос/Сообщение", "Ответ"};
            llRow = new LinkedList<TableRow>();
            refrechTable();
            init();
        }

    }

    public void setLog(String msg) {

        logger.info(msg);

    }

    public void setTableLog(JTable table) {
        this.tblMsg = table;

        if (tblMsg != null) {
            ColName = new String[]{"Дата/Время", "Тип", "Запрос/Сообщение", "Ответ"};
            llRow = new LinkedList<TableRow>();

            refrechTable();
        }

    }

    private void init() {

        hmProcess = new HashMap<Object, TableRow>();
        logger = org.apache.log4j.Logger.getLogger("LogServer");

    }

    public synchronized TableRow sendMessage(String txtMsg, String typmsg, int TypStr) {

        TableRow row = null;

        if (tblMsg == null) {
            return null;
        }

        DateTime data = new DateTime();

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

        String fmt = data.toString(dtf);

        int hour = data.getHourOfDay();

        if (hour == 0) {
            // saveTable(data);
        }

        if (TypStr == STR_NEW) {
            row = new TableRow(fmt, typmsg, txtMsg, "");

            llRow.addFirst(row);

        } else if (TypStr == STR_SQL) {
            llRow.get(0).setSql(txtMsg);
            llRow.get(0).setTyp(typmsg);
        } else {
            llRow.get(0).setAnswer(txtMsg);
            llRow.get(0).setTyp(typmsg);

        }

        if (typmsg.equals(MSG_ERROR)) {
            //  splpMsg.setDividerLocation(0.8);
        }

        ((AbstractTableModel) tblMsg.getModel()).fireTableDataChanged();

        tblMsg.repaint();
        return row;

    }

    public synchronized TableRow sendMessageProcess(String txtMsg, String typmsg, int TypStr) {

        TableRow row = null;

        DateTime data = new DateTime();

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

        String fmt = data.toString(dtf);

        int hour = data.getHourOfDay();

        if (hour == 0) {
            // saveTable(data);
        }

        if (TypStr == STR_NEW) {
            row = new TableRow(fmt, typmsg, txtMsg, "");

            JProgressBar bar = new JProgressBar();

            bar.setStringPainted(true);
            bar.setString(txtMsg);
            row.setBar(bar);

            llRow.addFirst(row);

        } else if (TypStr == STR_SQL) {
            llRow.get(0).setSql(txtMsg);
            llRow.get(0).setTyp(typmsg);
        } else {
            llRow.get(0).setAnswer(txtMsg);
            llRow.get(0).setTyp(typmsg);

        }

        if (typmsg.equals(MSG_ERROR)) {
            // splpMsg.setDividerLocation(0.8);
        }

        ((AbstractTableModel) tblMsg.getModel()).fireTableDataChanged();

        tblMsg.repaint();
        return row;

    }

    public synchronized void sendMessageType(Object oAnswer, String txtMsg) {

        if (oAnswer != null) {

            int index = llRow.indexOf(oAnswer);

            if (index != -1) {

                llRow.get(index).setTyp(txtMsg);
                tblMsg.repaint();
            }
        }

    }

    public synchronized void sendMessageSql(Object oAnswer, String txtMsg) {

        if (oAnswer != null) {

            int index = llRow.indexOf(oAnswer);

            if (index != -1) {

                llRow.get(index).setSql(txtMsg);
                tblMsg.repaint();
            }
        }

    }

    public synchronized void sendMessageAnswer(Object oAnswer, String txtMsg, String typmsg) {

        if (oAnswer != null) {

            int index = llRow.indexOf(oAnswer);

            if (index != -1) {

                llRow.get(index).setAnswer(txtMsg);
                llRow.get(index).setTyp(typmsg);
                llRow.get(index).setBar(null);

                tblMsg.repaint();
            }
        }

    }

    private void refrechTable() {

        tblMsg.setModel(new MsgModel());

        Class cl = tblMsg.getColumnClass(1);

        tblMsg.setDefaultRenderer(cl, new ColorTableCellRenderer());

        TableColumn column = tblMsg.getColumnModel().getColumn(3);

        column.setCellRenderer(new ProcessCellRenderer());

    }

    static final class ColorTableCellRenderer extends JLabel implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (row < 0) {

                return this;
            }

            String s = (String) table.getValueAt(row, 1);

            if (s == null) {

                return this;
            }

            this.setFont(new Font(this.getFont().getName(), Font.PLAIN, this.getFont().getSize()));

            if (s.equals(MSG_ERROR)) {
                this.setForeground(CLR_ERROR);
            //    this.setFont(new Font(this.getFont().getName(), Font.BOLD, this.getFont().getSize()));

            } else if (s.equals(MSG_SQL)) {
                this.setForeground(CLR_SQL);
            } else if (s.equals(MSG_OK)) {
                //  this.setFont(new Font(this.getFont().getName(), Font.BOLD, this.getFont().getSize()));
                this.setForeground(CLR_OK);
            } else if (s.equals(MSG_WARNING)) {
                this.setForeground(CLR_WARNING);
            } else if (s.equals(MSG_LOG)) {
                this.setForeground(CLR_LOG);

            }

            this.setText((String) value);

            return this;

        }
    }

    class ProcessCellRenderer implements TableCellRenderer {

        private JLabel label;

        public ProcessCellRenderer() {
            label = new JLabel();

        }

        public void setProgressBar(int row) {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JProgressBar bar;

            bar = llRow.get(row).getBar();

            if (bar != null) {
                return bar;
            } else {

                String s = (String) table.getValueAt(row, 1);

                if (s == null) {

                    return label;
                }

                label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize()));

                if (s.equals(MSG_ERROR)) {
                    label.setForeground(CLR_ERROR);
                    //label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize()));

                } else if (s.equals(MSG_SQL)) {
                    label.setForeground(CLR_SQL);
                } else if (s.equals(MSG_OK)) {
                    //  label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize()));
                    label.setForeground(CLR_OK);
                } else if (s.equals(MSG_WARNING)) {
                    label.setForeground(CLR_WARNING);
                } else if (s.equals(MSG_LOG)) {
                    label.setForeground(CLR_LOG);

                }

                label.setText((String) value);
                return label;
            }

        }
    }

    private class MsgModel extends AbstractTableModel {

        public String getColumnName(int column) {
            return (String) ColName[column];
        }

        @Override
        public int getRowCount() {
            return llRow.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (llRow.size() < rowIndex + 1) {
                return null;
            }

            TableRow tr = llRow.get(rowIndex);
            if (columnIndex == 0) {
                return tr.getDate();
            } else if (columnIndex == 1) {
                return tr.getTyp();
            } else if (columnIndex == 2) {
                return tr.getSql();
            } else if (columnIndex == 3) {
                return tr.getAnswer();
            } else {
                return null;
            }
        }
    }

    public class TableRow {

        private JProgressBar bar;
        private String Typ;
        private String Date;
        private String Sql;
        private String Answer;
        private Timer timer;

        public TableRow(String date, String typ, String sql, String answer) {
            this.Date = date;
            this.Typ = typ;
            this.Sql = sql;
            this.Answer = answer;

        }

        public Timer getTimer() {
            return timer;
        }

        public void setTimer(Timer timer) {
            this.timer = timer;
        }

        public JProgressBar getBar() {
            return bar;
        }

        public void setBar(JProgressBar bar) {
            this.bar = bar;
        }

        public String getAnswer() {
            return Answer;
        }

        public void setAnswer(String Answer) {
            this.Answer = Answer;
        }

        public String getDate() {
            return Date;
        }

        public void setDate(String Date) {
            this.Date = Date;
        }

        public String getSql() {
            return Sql;
        }

        public void setSql(String Sql) {
            this.Sql = Sql;
        }

        public String getTyp() {
            return Typ;
        }

        public void setTyp(String Typ) {
            this.Typ = Typ;
        }
    }

    class onRefresch implements ActionListener {

        private JProgressBar bar;
        private String szText;
        private boolean bShow;

        public onRefresch(JProgressBar bar) {

            bShow = false;
            this.szText = bar.getString();
            this.bar = bar;

        }

        @Override
        public void actionPerformed(ActionEvent e) {

            bShow = !bShow;

            if (bShow) {

                bar.setString(szText);
            } else {

                bar.setString("");
            }

            bar.repaint();
            ((AbstractTableModel) tblMsg.getModel()).fireTableDataChanged();

        }
    }

    public synchronized void RefreshProcess(TableRow row, Object value) {

        if (row == null) {

            return;
        }

        Integer idx = llRow.indexOf(row);

        if (idx == -1) {
            return;
        }

        JProgressBar bar = row.getBar();

        if (bar != null) {

            if (value instanceof Integer) {

                bar.setValue((Integer) value);
            } else if (value instanceof String) {

                bar.setString((String) value);
            } else if (value instanceof Boolean) {

                Timer t;
                Boolean b = (Boolean) value;

                if (b) {

                    // Предыдущий процесс мигания
                    t = row.getTimer();

                    if (t != null) {
                        t.stop();
                    }

                    t = new javax.swing.Timer(777, new onRefresch(bar));
                    t.start();
                    row.setTimer(t);

                } else {

                    t = row.getTimer();

                    if (t != null) {
                        t.stop();
                    }
                }
            }

            ((AbstractTableModel) tblMsg.getModel()).fireTableDataChanged();

        }

    }

    // Суточная очистка и запись событий
    public void clearTable() {

        if (llRow != null && tblMsg != null) {

            llRow.clear();
            refrechTable();
            tblMsg.repaint();
        }
    }

    @Override
    public void update(Observable o, Object arg) {

        TableRow tableRow = null;

        if (arg instanceof Exception) {

            logger.error(o.getClass().getName(), (Throwable) arg);

        } else if (arg instanceof String) {

            logger.info(arg);

        } else if (arg instanceof Object[]) {
            //Для графической системы

            Object[] ses = (Object[]) arg;

            Object object = ses[0];

            if (hmProcess.containsKey(object)) {

                tableRow = hmProcess.get(object);
            }

            if (ses.length == 4) {

                Integer typ = (Integer) ses[1];

                String sMsg = (String) ses[2];

                String sLog = (String) ses[3];

                if (typ == MESSAGE_PROCESS) {
                    tableRow = (TableRow) sendMessageProcess(sMsg, sLog, STR_NEW);
                    hmProcess.put(object, tableRow);

                } else if (typ == MESSAGE_SIMPLE) {
                    tableRow = (TableRow) sendMessage(sMsg, sLog, STR_NEW);
                    hmProcess.put(object, tableRow);
                } else if (typ == MESSAGE_ANSWER) {

                    if (tableRow != null) {
                        sendMessageAnswer(tableRow, sMsg, sLog);
                    }
                } else if (typ == MESSAGE_SQL) {

                    if (tableRow != null) {
                        sendMessageSql(tableRow, sMsg);
                    }

                } else if (typ == MESSAGE_TYP) {

                    if (tableRow != null) {
                        sendMessageType(tableRow, sMsg);
                    }

                }

            } else if (ses.length == 2) {

                Object o1 = ses[1];

                if (tableRow == null) {
                    return;
                }

                if (o1 instanceof Point) {

                    Point point = (Point) o1;

                    JProgressBar bar = tableRow.getBar();

                    if (bar != null) {
                        bar.setMaximum(point.y);
                        bar.setMinimum(point.x);
                    }

                } else {
                    RefreshProcess(tableRow, o1);

                }

            } else if (ses.length == 3) {
                // Мигание текста

                if (tableRow == null) {
                    return;
                }

                Boolean o1 = (Boolean) ses[1];
                String msg = (String) ses[2];

                if (o1) {

                    JProgressBar bar = tableRow.getBar();

                    if (bar != null) {
                        bar.setString(msg);
                    }

                    RefreshProcess(tableRow, o1);
                } else {

                    RefreshProcess(tableRow, o1);
                }

            }
        }
    }
}

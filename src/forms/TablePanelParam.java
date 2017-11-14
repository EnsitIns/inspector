package forms;

import connectdbf.SqlTask;
import dbf.Work;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import servise.MainWorker;
import servise.MapMessageProcess;
import xmldom.XmlTask;

import javax.help.CSH;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static constatant_static.CursorToolkitOne.startWaitCursor;
import static constatant_static.CursorToolkitOne.stopWaitCursor;
import static constatant_static.SettingActions.*;
import static constatant_static.SettingActions.esStatus.*;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author 1
 */
public class TablePanelParam extends Observable implements Observer {

    private static final String PP_EDIT = "Редактировать";
    private static final String PP_DEL = "Удалить";
    public static final String CM_OBJECT_SELECT = "Выбрать объект из списка";
    public static final String CM_TABLE_EDIT_GROUP = "Групповое изменение параметра";
    public static final int ST_TABLE = 0;
    public static final int ST_SELECT = 1;
    private DateTableSelectColorRenderer cellRenderer;

    public static enum typShow {

        tsTable,
        tsSQL
    }
//    private ActionTable actionEdit;
    //  private ActionTable actionDelete;
    private ActionTable actionInsert;
    private ActionTable actionSave;
    //   private ActionTable actionLoad;
    private ActionTable actionSelectRow;
    private ActionTable actionSortZA;
    private ActionTable actionSortAZ;
    private ActionTable actionFilter;
    private ActionTable actionGrafics;
    private ActionTable actionGroupEdit;
    private JButton buttonSelect;
    private typShow showTyp;
    private HashMap<String, Object> hmRecord;
    private String nameTable; // имя загруженой таблицы
    private String nameView; // имя  представления загруженой таблицы
    private int fcol;// Столбец поиска
    private String Caption;// Название возвращаемого объекта
    private String NameFcol;// Название столбца поиска;
    private int selectRow;// Номер выделенной строки;
    private Object c_tree_id; // id выбранного объекта
    private JTable table;
    private HashMap<String, Object> hmKeys; // Список Primary Keys
    private JPanel panel;
    private JPanel mainPanel;
    private JComboBox cbSelectSql;
    private JLabel lblCaption;
    private JTextField txfFind;
    private JButton btnResaze;
    private ResultSet rsTable;
    private String sqlTable;  // Строка текущего запроса
    private boolean CurrentValueOnly;  // только значения текущей даты
    private ResultSetMetaData rsmd;
    private SimpleDateFormat formatter2;
    private JPopupMenu popup;
    private int statusTable;// статус таблицы / 0-рабочая таблица 1-выбор объекта
    private String checkTable = "";// Название текущей проверяемой таблицы
    // private Integer idTable; //  id загружаемой таблицы
    private Integer typTable; //  тип загруженой таблицы
    private String captionTable; // Назание таблицы
    private Integer selectCol; // Выбраный столбец
    private HashMap<String, String> hmColumns; // Названия столбцов
    private String[] namesCaption; // Имена полей названия объекта
    //номер строки id Объекта
    private HashMap<Integer, Integer> hmkeyRow;
    // Выбор SQL запросов

    @Override
    public void update(Observable o, Object arg) {
        String nameSql;

        if (arg instanceof MapMessageProcess) {

            MapMessageProcess messageProcess = (MapMessageProcess) arg;

            if (messageProcess.isEmpty()) {

                cellRenderer.clearSelectRows();

            } else {
                cellRenderer.setInfo(messageProcess);
            }
            ((AbstractTableModel) table.getModel()).fireTableDataChanged();

        }

        if (arg instanceof HashMap) {

            HashMap<String, Object> hm = (HashMap<String, Object>) arg;

            if (hm.containsKey("sql")) {

                // Запрос информации
                try {

                    // Выполняем запрос
                    Integer value = (Integer) hm.get("sql");
                    Integer idT = 0;

                    //DbfClass.getIdTableByObjectId(value);
                    String nameT = (String) Work.getParamTableById(idT, Work.TABLE_NAME);

                    String sql = "SELECT * FROM " + nameT + " WHERE c_tree_id=" + value;

                    ResultSet rs = SqlTask.getResultSet(null, sql);

                    try {
                        if (rs.next()) {
                            nameSql = rs.getString("name_report");
                            sqlTable = rs.getString("sql_string");
                            setResultSet(sqlTable, typShow.tsSQL, nameSql);
                        }
                    } finally {
                        rs.close();
                    }

                } catch (Exception ex) {

                    // DbfClass.setLog("Запрос", ex);
                }

            }

        }

        if (o instanceof MainWorker) {

            MainWorker worker = (MainWorker) o;

            String nTable = (String) worker.getProperty("refresh");
            String nCheckTable = (String) worker.getProperty("captionTable");

            if (nTable != null) {
                checkTable = "Поступление данных(" + nCheckTable + ")";
                setTableByName(nTable, false);
            }

        }

    }

    private String getView(Integer id, String sql) {
        String sqlName = null;

        try {

            TreeSet<String> tsNames = SqlTask.getNameTables(null);

            sqlName = "sql" + id;

            if (tsNames.contains(sqlName)) {

                return sqlName;
            }

            String sqlView = "CREATE  VIEW " + sqlName + " AS " + sql;

            SqlTask.executeSql(null, sqlView);

        } catch (SQLException ex) {
            MainWorker.deffLoger.error("Создание представления", ex);
        }
        return sqlName;
    }

    public void setStatusTable(int statusTable) {
        this.statusTable = statusTable;
    }

    public JComponent getComponent() {

        return mainPanel;

    }

    // Передача всяких уведомлений
    public void setNotifyObservers(Object notyfy) {

        setChanged();
        notifyObservers(notyfy);

    }

    private enum SortTab {

        stAZ,
        stZA
    }

    private enum DelRow {

        drAll,
        drSelect
    }

    public String getNameTable() {
        return nameView;
    }

    // Создает листинг запросов
    public DefaultComboBoxModel createListSQL() {

        String nameTab = "object6";

        DefaultComboBoxModel result = new DefaultComboBoxModel();

        String sql = "SELECT name_report FROM " + nameTab + " ORDER BY name_report";

        ResultSet rs;

        try {

            rs = SqlTask.getResultSet(null, sql);

            while (rs.next()) {
                String name = rs.getString(1);

                result.addElement(name);
            }

            rs.close();

            return result;

        } catch (SQLException ex) {

         //  DbfClass.setLog("Листинг запросов", ex);
            return result;

        }

    }

    private void saveTableInXml() throws Exception {

      //  Object o = DbfClass.SendMessage("Запись таблицы в XML...", MSG_LOG, STR_NEW);
        Document document = XmlTask.getNewDocument();

        Element root = document.createElement("root");

        document.appendChild(root);

        File file = XmlTask.openFile("xml", "", mainPanel.getParent());

        if (file == null) {
            return;
        }

        String path = file.getParent();
        String name = file.getName();

        if (!name.endsWith(".xml")) {

            name = name + ".xml";

            file = new File(path, name);

        }

        if (file == null) {
            //    DbfClass.SendMessageAnswer(o, "Отмена.", MSG_WARNING);
            return;

        }

        rsTable.first();

        ResultSetMetaData metaData = rsTable.getMetaData();

        String nameT = metaData.getTableName(1).toLowerCase();
        root.setAttribute("name_table", nameT);

        String sKey = SqlTask.getPrimaryKeyTable(null, nameT);
        root.setAttribute("name_key", sKey);

        HashMap<String, Object> hmParamCol = SqlTask.getMapNamesCol(null, nameTable, new Integer[]{6, 7});

        for (String nameCol : hmParamCol.keySet()) {

            Object[] oPar = (Object[]) hmParamCol.get(nameCol);

            String collTyp = "";
            String colSize = "";

            if (oPar[0] != null) {
                collTyp = oPar[0].toString().toUpperCase();
            }

            if (oPar[1] != null) {
                colSize = oPar[1].toString();
            }

            Element elType = document.createElement("type_col");

            elType.setAttribute("name", nameCol);
            elType.setAttribute("type", collTyp);
            elType.setAttribute("size", colSize);

            root.appendChild(elType);

        }

        String value;
        do {

            Element element = document.createElement("row");

            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                String nameCol = metaData.getColumnName(i).toLowerCase();

                int typCol = metaData.getColumnType(i);

                if (typCol == java.sql.Types.LONGVARBINARY) {

                    byte[] bs = rsTable.getBytes(nameCol);
                    value = javax.xml.bind.DatatypeConverter.printBase64Binary(bs);
                    root.setAttribute("Base64", nameCol);

                } else {
                    value = rsTable.getString(nameCol);
                }
                element.setAttribute(nameCol, value);
            }
            root.appendChild(element);
        } while (rsTable.next());

        XmlTask.saveXmlDocument(document, file);

       // DbfClass.SendMessageAnswer(o, "Успешно", MSG_OK);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void init() {

        hmkeyRow = new HashMap<Integer, Integer>();

        mainPanel = new JPanel(new BorderLayout(10, 0));

        // this.setLayout(new BorderLayout(10, 0));
        lblCaption = new JLabel("Параметры");

        lblCaption.setFont(new Font(lblCaption.getFont().getFontName(), Font.BOLD, lblCaption.getFont().getSize()));
        lblCaption.setForeground(new java.awt.Color(0, 0, 204));

        hmRecord = new HashMap<String, Object>(); // Текущая запись

        txfFind = new JTextField();
        fcol = 0;

        excludeStatus(esSelectRow);

        txfFind.addKeyListener(new onKey());
        //txfFind.setMaximumSize(new Dimension(50, 18));
        //   btnResaze = new JButton();
        table = new JTable();

        //DbfClass.createColumnsMap();
        ImageIcon img = null;

        panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();

        panel.setLayout(layout);

        // JPanel pnlcap = new JPanel();
        // pnlcap.add(lblCaption);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets.left = 10;
        gbc.insets.right = 10;
        panel.add(lblCaption, gbc);

        JPanel pnlfind = new JPanel(new BorderLayout(5, 5));
        pnlfind.add(txfFind, BorderLayout.CENTER);
        JLabel lbl = new JLabel();

        img = createIcon("find.png");
        lbl.setIcon(img);

        pnlfind.add(lbl, BorderLayout.WEST);
        pnlfind.setMinimumSize(new Dimension(100, 20));

        gbc = new GridBagConstraints();
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pnlfind, gbc);

        //  panel = new JPanel(new BorderLayout());
        DropShadowBorder dsb = new DropShadowBorder(true);

        JButton btn;

        JToolBar tbrbtn = new JToolBar();

        tbrbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //        tbrbtn.add(pnlfind);

        ImageIcon icon;
        ImageIcon iconOff;

        ActionTable action;

        icon = createIcon("filterl.png");
        actionFilter = new ActionTable(CM_SET_FILTER, icon, CM_SET_FILTER);
        btn = tbrbtn.add(actionFilter);

        icon = createIcon("filterl_off.png");
        btn.setDisabledIcon(icon);
        CSH.setHelpIDString(btn, "filter");

        icon = createIcon("editrow.png");
        iconOff = createIcon("editrow_off.png");

        //  actionEdit = new ActionTable(CM_EDIT_ROW_TABLE, icon, CM_EDIT_ROW_TABLE);
        //  btn = tbrbtn.add(actionEdit);
        //  btn.setDisabledIcon(iconOff);
        //  actionEdit.setEnabled(false);
        icon = createIcon("addrow.png");
        iconOff = createIcon("addrow_off.png");

        actionInsert = new ActionTable(CM_ADD_ROW_TABLE, icon, CM_ADD_ROW_TABLE);

        btn = tbrbtn.add(actionInsert);
        btn.setDisabledIcon(iconOff);

        icon = createIcon("delrow.png");
        iconOff = createIcon("delrow_off.png");

        // actionDelete = new ActionTable(CM_DELETE_ROW_TABLE, icon, CM_DELETE_ROW_TABLE);
        //    btn = tbrbtn.add(actionDelete);
        //  btn.setDisabledIcon(iconOff);
        //  actionDelete.setEnabled(false);
        // Экспорт в Excel
        //  icon_off=createImageIcon("byExceloff.png", null);
        icon = createIcon("byExcel.png");
        action = new ActionTable(CM_EXCEL_EXPORT, icon, CM_EXCEL_EXPORT);
        tbrbtn.add(action);

        icon = createIcon("sortaz.png");
        actionSortAZ = new ActionTable(CM_SORT_AZ, icon, CM_SORT_AZ);
        btn = tbrbtn.add(actionSortAZ);
        iconOff = createIcon("sortaz_off.png");
        btn.setDisabledIcon(iconOff);

        icon = createIcon("sortza.png");
        actionSortZA = new ActionTable(CM_SORT_ZA, icon, CM_SORT_ZA);
        btn = tbrbtn.add(actionSortZA);
        iconOff = createIcon("sortza_off.png");
        btn.setDisabledIcon(iconOff);

        icon = createIcon("resizetab.png");
        action = new ActionTable(CM_RESIZE_TABLE, icon, CM_RESIZE_TABLE);
        tbrbtn.add(action);

        icon = createIcon("save_table.png");

        actionSave = new ActionTable(CM_SAVE_TABLE, icon, CM_SAVE_TABLE);

        btn = tbrbtn.add(actionSave);
        actionSave.setEnabled(true);

        icon = createIcon("load.png");
        iconOff = createIcon("load_off.png");

        //  actionLoad = new ActionTable(CM_TABLE_LOAD, icon, CM_TABLE_LOAD);
        //  btn = tbrbtn.add(actionLoad);
        //  btn.setDisabledIcon(iconOff);
        //   actionLoad.setEnabled(true);
        icon = createIcon("group_edit.png");
        iconOff = createIcon("group_edit_off.png");

        actionGroupEdit = new ActionTable(CM_TABLE_EDIT_GROUP, icon, CM_TABLE_EDIT_GROUP);
        btn = tbrbtn.add(actionGroupEdit);
        btn.setDisabledIcon(iconOff);

        actionGroupEdit.setEnabled(true);

        // tbrbtn.add(pnlsql);
        icon = createIcon("select_obj.PNG");

        actionSelectRow = new ActionTable(CM_OBJECT_SELECT, icon, CM_OBJECT_SELECT);

        buttonSelect = new JButton();

        buttonSelect.setBorderPainted(true);
        buttonSelect.setContentAreaFilled(true);
        buttonSelect.setAction(actionSelectRow);

        //  btn = tbrbtn.add(actionSelectRow);
        // btn.setText(CM_DEL_CONTROLLER);
        //actionSave.setEnabled(true);
        //  tbrbtn.add(button);
        //if (IsSetStatus(esLevelAccess0)) {
        //  icon = createIcon("Ok.png");
        //action = new ActionTable(CM_CHECK_REFERENCE, icon, CM_CHECK_REFERENCE);
        // tbrbtn.add(action);
        // }
        gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        panel.add(tbrbtn, gbc);

        // String sql = "SELECT * FROM  object6  WHERE id_user=? OR id_user=-1 ORDER BY name_report";
        String sql = "SELECT * FROM  object6  WHERE id_user=? OR id_user=-1  AND  sql_string LIKE ?  ORDER BY name_report";

        String nTab = "";

        if (nameTable == null) {
            nTab = "%objects%";
        } else {

            nTab = "%" + nameTable + "%";
        }

//c_tree_id
     //   selectorObjects = new SelectorObjects(sql, "name_report", new String[]{"sql_string", "c_tree_id"}, new Object[]{0,0});
       // selectorObjects.addObserver(TablePanelParam.this);
        //    CellSelectObject cellSelectObject = null;
        //  try {
        //    cellSelectObject = new CellSelectObject(-1, nameTable, 9, false, false, true);
        // } catch (SQLException ex) {
        //   MainWorker.deffLoger.error("", ex);
        // }
        //  cellSelectObject.addObserver(this);
        //  JComponent component = cellSelectObject.getComponent(null);
        //component.setMinimumSize(new Dimension(150, 20));
        gbc = new GridBagConstraints();
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;

        //panel.add(selectorObjects.getPanel(), gbc);
        //  panel.add(component, gbc);
// tbrbtn.add(cellSelectObject.getComponent());
        // table = new JTable();
        table.addKeyListener(new onKeyTable());

        //panel.add(pnlfind, BorderLayout.CENTER);
        //    panel.add(pnlcap, BorderLayout.WEST);
        //  panel.add(tbrbtn, BorderLayout.CENTER);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getDefaultRenderer(String.class);
        renderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);

        //       panel.add(bar, BorderLayout.EAST);
        //  panel.add(buttonSelect, BorderLayout.EAST);
        // buttonSelect.setVisible(true);
        panel.setBorder(dsb);

        JScrollPane sp = new JScrollPane(table);

        sp.setBorder(dsb);

        mainPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(sp, BorderLayout.CENTER);

        if (statusTable == ST_SELECT) {
            actionFilter.setEnabled(false);
            actionGroupEdit.setEnabled(false);

        }

        //  this.add(panel, BorderLayout.NORTH);
        //  this.add(sp, BorderLayout.CENTER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new OnSelectRowTable());

        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new OnMouseHeaderClick());

        table.setRowHeight(18);
        table.addMouseListener(new OnMouseRowClick());

        // this.table.setRowHeight(18);
        // this.table.addMouseListener(new OnMouseRowClick());
        cellRenderer = new DateTableSelectColorRenderer();

        table.setDefaultRenderer(String.class, cellRenderer);

    }

    public TablePanelParam(String nameTable) {

        statusTable = ST_TABLE;
        init();

        CurrentValueOnly = false;
        this.nameTable = nameTable;
        setKey();

    }

    public TablePanelParam(int idSelectObject, String nameTable) {

        statusTable = ST_SELECT;

        this.selectRow = idSelectObject;
        this.nameTable = nameTable;
        init();

        CurrentValueOnly = false;
        this.nameTable = nameTable;
        setKey();

    }

    /**
     * Создаем названия столбцов
     */
    private void createNamesColumn() {
        try {

            hmColumns = (HashMap<String, String>) Work.getNamesColByResultSet(rsTable, sqlTable);

        } catch (Exception ex) {

            MainWorker.deffLoger.error("Названия столбцов", ex);
        }

    }

    private void setEnabled() {

        boolean isLevel = false;

        isLevel = (isSetStatus(esLevelAccess0) || isSetStatus(esLevelAccess1));

        if (IsdeleteRecord(nameTable)) {
            //    actionEdit.setEnabled(false);
            actionInsert.setEnabled(isLevel);
            //  actionDelete.setEnabled(false);

        } else {

            //  actionEdit.setEnabled(false);
            //  actionDelete.setEnabled(false);
            actionInsert.setEnabled(false);
        }

        if (isSetStatus(esLevelAccess0)) {
            actionGroupEdit.setEnabled(true);
        } else {

            actionGroupEdit.setEnabled(false);
        }

        if (isSetStatus(esLevelAccessAll)) {
            // actionLoad.setEnabled(true);
        } else {
            // actionLoad.setEnabled(false);
        }

        if (showTyp == typShow.tsSQL) {

            // actionEdit.setEnabled(false);
            // actionDelete.setEnabled(false);
            actionInsert.setEnabled(false);
            actionSelectRow.setEnabled(false);
            actionSortAZ.setEnabled(false);
            actionSortZA.setEnabled(false);

            txfFind.setEnabled(false);

        } else {

            actionSortAZ.setEnabled(true);
            actionSortZA.setEnabled(true);
            txfFind.setEnabled(true);

        }

        //   TreeMap<String, Integer> hashMap = DbfClass.createListReportExcel();
        // if (hashMap.containsKey(captionTable)) {
        //  actionGrafics.putValue(ActionTable.SHORT_DESCRIPTION, "Отчет '" + captionTable + "'");
        //  actionGrafics.setEnabled(true);
        // } else {
        //  actionGrafics.putValue(ActionTable.SHORT_DESCRIPTION, "");
        //  actionGrafics.setEnabled(false);
        // }
        // hashMap.clear();
    }

    private void setSelectRow() {

        AbstractTableModel atm = (AbstractTableModel) table.getModel();

        atm.fireTableDataChanged();

        //        table.setRowSelectionInterval(row, row);
    }

    public void updateTable() {
        try {
            rsTable.close();
            rsTable = SqlTask.getResultSetBySaveSql(null, sqlTable, ResultSet.CONCUR_READ_ONLY);
            ((AbstractTableModel) table.getModel()).fireTableDataChanged();

        } catch (SQLException ex) {
            MainWorker.deffLoger.error(sqlTable, ex);
        } catch (NullPointerException ex) {
            MainWorker.deffLoger.error(sqlTable, ex);
        }
    }

    private void RunAction(String nc) {

        if (nc.equals(CM_EXCEL_EXPORT)) {

          //  Main.SCHEDULE.expotrToExcel(sqlTable);
        }

        if (nc.equals(CM_TABLE_GRAFICS)) {
            //    TreeMap<String, Integer> hashMap = DbfClass.createListReportExcel();
            //  if (hashMap.containsKey(captionTable)) {
            //    Integer id = (Integer) hashMap.get(captionTable);
            //  Main.SCHEDULE.createReportInExcel(sqlTable, MainForm.dpFirst.getDate(), MainForm.dpLast.getDate(), captionTable);
            // }
            //   SVGView view = new SVGView(null, true);
            // view.createDocGrafic(rsTable);
            // view.setVisible(true);
        }

        if (nc.equals(CM_TABLE_EDIT_GROUP)) {
       //    try {
            //       DbfClass.editGroupProperti(sqlTable, this, hmColumns);
            // } catch (SQLException ex) {
            //   MainWorker.deffLoger.error(CM_TABLE_EDIT_GROUP, ex);
            //}

        }

        if (nc.equals(CM_SAVE_TABLE)) {

            try {

                saveTableInXml();

            } catch (Exception ex) {

                MainWorker.deffLoger.error("Запись формы в XML", ex);
            }

        }

        if (nc.equals(CM_SORT_AZ)) {

            sortCurrentTable(SortTab.stAZ);
        }

        if (nc.equals(CM_SORT_ZA)) {

            sortCurrentTable(SortTab.stZA);

        }

        if (nc.equals(CM_CHECK_REFERENCE)) {

//            DbfClass.checkReference(nameTable);
        }

        if (nc.equals(CM_SET_FILTER)) {

            setFilter();

        }

        if (nc.equals(CM_DELETE_ROW_TABLE)) {

            deleteRecord(DelRow.drSelect);
            updateTable();
        }

        if (nc.equals(CM_OBJECT_SELECT)) {

            setNotifyObservers(TablePanelParam.this);

        }

        if (nc.equals(CM_ADD_ROW_TABLE)) {

            if (nameTable.equals("schedules")) {

                // Расписания
                // ScheduleWindow schedule = new ScheduleWindow(new JFrame(), true);
                // schedule.setShedule(-1);
                // schedule.setVisible(true);
            } else {

               // RecordWindow form = new RecordWindow(null, -1, null, nameTable, null);
                // form.setVisible(true);
            }

            updateTable();

            try {
                rsTable.last();
                int row = rsTable.getRow();

                if (row > 0) {

                    table.setRowSelectionInterval(row - 1, row - 1);
                    table.scrollRectToVisible(table.getCellRect(row - 1, 1, true));
                }

            } catch (SQLException ex) {
                // DbfClass.setLog("Добавить строку", ex);
            }
        }

        if (nc.equals(CM_EDIT_ROW_TABLE)) {

            int selRow = table.getSelectedRow();

            if (nameTable.equals("schedules")) {

              //  ScheduleWindow schedule = new ScheduleWindow(new JFrame(), true);
                //  schedule.setShedule((Integer) c_tree_id);
                //  schedule.setVisible(true);
            } else {

                if (hmKeys != null && !hmKeys.isEmpty()) {
                //    RecordWindow form = new RecordWindow(null, hmKeys, null, nameTable);

                    // RecordWindow form = new RecordWindow(null, c_tree_id, null, idTable);
                    //  form.setVisible(true);
                } else {

                    JOptionPane.showMessageDialog(null, "Редактирование недоступно !", "Редактирование", JOptionPane.INFORMATION_MESSAGE);
                }

            }

            updateTable();

            if (selRow > 0) {
                table.setRowSelectionInterval(selRow, selRow);
                table.scrollRectToVisible(table.getCellRect(selRow, 1, true));
            }

        }

        if (nc.equals(CM_RESIZE_TABLE)) {

            setTableResize();

        }

    }

    class ActionTable extends AbstractAction {

        public ActionTable(String name, ImageIcon icon, String caption) {

            putValue(NAME, caption);
            putValue(SMALL_ICON, icon);
            putValue(AB_NAME_COMMAND, name);
            putValue(SHORT_DESCRIPTION, caption);

        }

        @Override
        public void actionPerformed(ActionEvent e) {

            String t = (String) this.getValue(AB_NAME_COMMAND);

            try {
                startWaitCursor(mainPanel.getRootPane());
                RunAction(t);
            } finally {
                stopWaitCursor(mainPanel.getRootPane());
            }
        }
    }

    public void FindRow(int id) {
        try {

            rsTable.first();

            do {

                int i = rsTable.getInt(hmKeys.keySet().iterator().next());

                if (i == id) {

                    int row = rsTable.getRow();
                    table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
                    table.scrollRectToVisible(table.getCellRect(row - 1, 1, true));
                    break;
                }

            } while (rsTable.next());

        } catch (SQLException ex) {
            //     Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private final class onKeyTable implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            // F4
            if (key == 115) {
                //    try {
                //  ExcelClass.ReportName reportName = ExcelClass.createReportName(rsTable.getMetaData());
                //reportName.execute();
                //  } catch (SQLException ex) {
                //    DbfClass.setLog("ReportName", ex);
                // }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == 109) {
            }

        }
    }

    private final class onKey implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {

            JTextField tf = (JTextField) e.getSource();
            String s = tf.getText();
            findRec(s);

        }
    }

    final class OnPopup implements PopupMenuListener {

        private DefaultComboBoxModel dcb = null;

        public OnPopup() {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

            JComboBox cb = (JComboBox) e.getSource();

            if (cb.getName().equals("cmbSql")) {
                //   dcb = MySql.createListSQL();
            } else {
                //   dcb = MySql.createListReport();
            }

            cb.setMaximumRowCount(20);
            cb.setModel(dcb);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    public boolean IsdeleteRecord(String nameTable) {
        boolean result = true;

        return result;
    }

    public void deleteRecord(DelRow dr) {

        String name = "";
        String[] s = {"Да", "Нет"};

        // Удаление всех объектов
        if (dr.equals(DelRow.drAll) && JOptionPane.showOptionDialog(null, "Подтвердите удаление объектов",
                "Удаление объектов", JOptionPane.WARNING_MESSAGE,
                JOptionPane.WARNING_MESSAGE, null, s, s[1]) == 0) {
        } else {
            // DbfClass.deleteObjectByID(mainPanel, c_tree_id, nameTable);

        }

        // setTable(idTable, false);
    }

    public void setFilter() {

       // FilterSQLWindows form = new FilterSQLWindows(null, this);
       // form.setAlwaysOnTop(true);
       // form.setVisible(true);
    }

    private void setTableResize() {

        if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

    }

    /**
     * Сортировка таблицы по возрастанию или убыванию
     */
    private void sortCurrentTable(SortTab sorts) {

        closeStatement();

        String sWhere = "";

        ArrayList<Object> alValues = new ArrayList<Object>();

        if (CurrentValueOnly) {

            Timestamp tsFirst = new Timestamp(777);
            DateTime dateTime = new DateTime();
            dateTime = dateTime.plusDays(1);
            Timestamp tsLast = new Timestamp(dateTime.getMillis());

            alValues.add(tsFirst);
            alValues.add(tsLast);

        }

        String sql;

        if (sorts.equals(SortTab.stAZ)) {

            sql = "SELECT * FROM " + nameView + sWhere + " ORDER BY " + NameFcol;

        } else {

            sql = "SELECT * FROM " + nameView + sWhere + " ORDER BY " + NameFcol + " DESC";

        }
        try {

            ArrayList<String> alNames = Work.getStatementList(sql, nameTable);

            sqlTable = SqlTask.getSaveSql(sql, alNames, alValues, nameView);

            rsTable = SqlTask.getResultSet(null, sqlTable);

        } catch (SQLException ex) {

            // Main.LOGGER.error("", ex);
        }

        setNotifyObservers(sqlTable);

        Refresh();

        // actionEdit.setEnabled(false);
        // actionDelete.setEnabled(false);
    }

    /**
     * Поиск записи по неполному соответствию
     *
     */
    public void findRec(String like) {

        String nameFind = null;
        String nt = null;
        String sql = null;

        TableColumn tc = table.getColumnModel().getColumn(0);
        TableCellRenderer tcr = tc.getHeaderRenderer();
        int sel_col = ((dateTableHeaderRenderer) tcr).getFcol();

        try {
            nameFind = rsTable.getMetaData().getColumnName(sel_col + 1).toLowerCase();

            if (like.isEmpty()) {

                setResultSet(nameView, typShow.tsTable, captionTable);

                return;
            }

            ArrayList<Object> alValues = new ArrayList<Object>();

            String sLike = "";

            // Если  поле цифровое
            HashMap<String, Integer> hmTypes = SqlTask.getNameAndTypeCol(null, nameView);

            // HashMap<String, Object> hmType = (HashMap<String, Object>) Work.getParamTableById(idTable, Work.TABLE_TYPE_COL);
            // String typCol = (String) hmType.get(nameFind);
            int iTypCol = hmTypes.get(nameFind);

            if (iTypCol == 4) {

                sLike = "=#";
                try {

                    Integer iFind = Integer.parseInt(like);

                    alValues.add(iFind);
                    // hmValues.put(nameFind, iFind);

                } catch (NumberFormatException nfe) {

                    alValues.add(-1);

                }

            } else {

                sLike = " LIKE #";

                alValues.add("%" + like + "%");

            }

            closeStatement();

            //  ArrayList<String> alNames = Work.getStatementList(sql, nameTable);
            ArrayList<String> alNames = Work.getStatementList(sql, nameView);

            if (sql.contains("LIKE")) {

                alNames.add(0, nameFind);
            }

            sqlTable = SqlTask.getSaveSql(sql, alNames, alValues, nameView);

            rsTable = SqlTask.getResultSet(null, sqlTable);

            setNotifyObservers(sqlTable);

            Refresh();

        } catch (SQLException ex) {

            //  DbfClass.setLog("Поиск записи", ex);
        }

    }

    private final class onPopup implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            JMenuItem mi = (JMenuItem) e.getSource();

            if (mi.getText().equals(PP_EDIT)) {
            }

            if (mi.getText().equals(PP_DEL)) {
            }

        }
    }

    public String getCaption() {
        return Caption;
    }

    public String getSqlTable() {
        return sqlTable;
    }

    public HashMap<String, Object> getHmRecord() {
        return hmRecord;
    }

    public Object getC_tree_id() {
        return c_tree_id;
    }

    public JTable getTable() {
        return table;
    }

    public ResultSet getRsTable() {
        return rsTable;
    }

    public void showButtonSelect(boolean b) {

        buttonSelect.setVisible(b);
    }

    public void showTable(String nameTable, boolean valueCurr) {

        setCurrentValueOnly(valueCurr);

        this.nameTable = nameTable;

        this.nameView = SqlTask.getViewByTable(nameTable);

        setResultSet(nameView, typShow.tsTable, "Сводная таблица");

    }

    public void setTableByName(String nameTable, boolean valueCurr) {

        cellRenderer.clearSelectRows();

        if (nameTable.contains("svodnaya")) {
            actionFilter.setEnabled(false);

        } else {

            actionFilter.setEnabled(true);
        }
        setCurrentValueOnly(valueCurr);
        this.nameTable = nameTable;

        if (nameTable.equals("objects")) {
          //  MainForm.iCurrObject = null;
            //  MainForm.lblCurrObject.setText("");
        }

        setKey();

    }

    public void setCurrentValueOnly(boolean bOnly) {

        CurrentValueOnly = bOnly;

    }

    private void setKey() {
        try {

            hmKeys = SqlTask.getPrimaryKeyMap(null, nameTable);
            //    alPrimaryKey = (ArrayList<String>) Work.getParamTableById(idTable, Work.TABLE_PRIMARY_KEY);
            //  typTable = (Integer) Work.getParamTableById(idTable, Work.TABLE_TYPE);
            //  nameTable = (String) Work.getParamTableById(idTable, Work.TABLE_NAME);
            captionTable = (String) Work.getParamTableByName(nameTable, Work.TABLE_CAPTION);
            fcol = 0;
            //   if (alPrimaryKey == null) {
            ///     ArrayList<String> alKey = (ArrayList<String>) Work.getParamTableById(idTable, Work.TABLE_INDEX);
            //  if (alKey != null && alKey.size() > 0) {
            //    alPrimaryKey = alKey;
            // }
            //}
            // if (typTable == 1) {
            //   MainForm.lblCurrObject.setText("");
            // MainForm.iCurrObject = null;
            // }

            // Названия столбцов
            // createNamesColumn();
            nameView = SqlTask.getViewByTable(nameTable);

            setSQLbyNameTable();

            setResultSet(nameView, typShow.tsTable, captionTable);
        } catch (Exception ex) {

            //Main.LOGGER.error("", ex);
        }

    }

    private void setSQLbyNameTable() {

        String sql = "SELECT * FROM  object6  WHERE id_user=? OR id_user=-1  AND  sql_string LIKE ?  ORDER BY name_report";

        String nTab = "";

        if (nameView == null) {
            nTab = "%objects%";
        } else {

            nTab = "%" + nameView + "%";
        }

//c_tree_id
      //  if (selectorObjects != null) {
        //   selectorObjects.updateBox(sql, new Object[]{Main.ID_CUR_USER, nTab});
        //  }
    }

    // Все данные только на текущую дату(Данные)
    public void setCurrValueTable(String sql) {

        // object_id
        ArrayList<Object> alValues = new ArrayList<Object>();

        String s = null;

        Timestamp tsFirst = new Timestamp(343434);
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusDays(1);
        Timestamp tsLast = new Timestamp(dateTime.getMillis());

        alValues.add(tsFirst);
        alValues.add(tsLast);

        int level = 0;
        String nameV = SqlTask.getViewByTable("objects");
        try {
            level = Work.getLevelTable(nameTable);
            typTable = SqlTask.getTypTableByNameTable(null, nameTable);

        } catch (SQLException ex) {
            //Main.LOGGER.error("", ex);
        }

//18-журналы
        try {

            if (typTable == 15) {
                s = "SELECT * FROM objects";

               // DbfClass.SendMessage("Не выбран текущий объект учета !", MSG_WARNING, STR_NEW);
            }

            ArrayList<String> alStat = Work.getStatementList(s, nameTable);
            sqlTable = SqlTask.getSaveSql(s, alStat, alValues, nameTable);
            rsTable = SqlTask.getResultSet(null, sqlTable, null, ResultSet.CONCUR_READ_ONLY);

        } catch (SQLException ex) {
            // Main.LOGGER.error("", ex);
        }

    }

    private void closeStatement() {

        if (rsTable != null) {
            try {
                rsTable.getStatement().close();
            } catch (SQLException ex) {

                // DbfClass.setLog("", ex);
            }

        }

    }

    // Из пользовательского фильтра
    public void setResultSet(String sql, ArrayList<Object> list, String nameTable, String caption) {
        try {
            this.nameTable = nameTable;

            ArrayList<String> alNames = SqlTask.getStatementList(null, sql, nameTable);

            sqlTable = SqlTask.getSaveSql(sql, alNames, list, nameTable);
            rsTable = SqlTask.getResultSet(null, sqlTable);

            setEnabled();
            lblCaption.setText(caption);
            captionTable = caption;

            createNamesColumn();
            setNotifyObservers(sqlTable);
            Refresh();
        } catch (SQLException ex) {
            //  Main.LOGGER.error("", ex);
        }

    }

    public void setResultSet(String sql, typShow ts, String caption) {

        closeStatement();
        table.setEnabled(true);

        String s = "";

        showTyp = ts;

        if (caption == null) {

            caption = "";
        }

        try {

            if (ts.equals(typShow.tsTable)) {

                // nameTable = sql;
                s = "SELECT * FROM " + sql;

                // Если только текущие данные
                if (CurrentValueOnly) {

                    setCurrValueTable(s);

                } else {

                    sqlTable = s;
                    rsTable = SqlTask.getResultSet(null, sqlTable);

                }

            }

            // SQL Запрос
            if (ts.equals(typShow.tsSQL)) {
                try {
                    rsTable = SqlTask.getResultSetBySaveSql(null, sqlTable, ResultSet.CONCUR_READ_ONLY);

                    if (sqlTable.contains("objects") || sqlTable.contains("svodnaya")) {

                        nameTable = "objects";

                    } else {
                        nameTable = "";
                    }

                } catch (Exception ex) {

                  //  DbfClass.SendMessage(ex.getMessage(), MSG_ERROR, STR_NEW);
                }

            }

            ResultSetMetaData rsm = rsTable.getMetaData();
            NameFcol = rsm.getColumnName(1).toLowerCase();

        } catch (SQLException ex) {

           // DbfClass.setLog("Запрос", ex);
        }

        captionTable = caption;
        lblCaption.setText(caption);

      //  if (nameTable.equals("objects") || nameTable.contains("svodnaya")) {
        //    includeStatus(esSelectObject);
        //  Main.CHANNEL_PROCESS_TABLE.getComponent().setVisible(true);
        // } else {
         //   excludeStatus(esSelectObject);
        // Main.CHANNEL_PROCESS_TABLE.getComponent().setVisible(false);
       // }
        createNamesColumn();
        setNotifyObservers(sqlTable);
        setEnabled();
        Refresh();

    }

    public final class Events extends Observable {

        public Object getSelectId() {

            return TablePanelParam.this.c_tree_id;

        }

        public Object getParent() {

            return TablePanelParam.this;

        }

        public void setNotify(Object o) {

            setChanged();
            notifyObservers(o);

        }
    }

    // Создаем карту для текущей строки
    private void CreateHashMapRec() {

        try {

            if (selectRow <= 0) {
                return;
            }

            hmRecord.clear();
            rsTable.absolute(selectRow);

            SqlTask.addParamToMap(rsTable, hmRecord);

        } catch (SQLException ex) {

            //  DbfClass.setLog("", ex);
        }

    }

    private void delRowTable() {

        deleteRecord(DelRow.drSelect);
        updateTable();

    }

    private void showRowTable() {

        if (nameTable.equals("schedules")) {

          //  ScheduleWindow schedule = new ScheduleWindow(new JFrame(), true);
            //   schedule.setShedule((Integer) c_tree_id);
            //  schedule.setVisible(true);
        } else {

            if (hmKeys != null && !hmKeys.isEmpty()) {
             //   RecordWindow form = new RecordWindow(null, hmKeys, null, nameTable);

                // RecordWindow form = new RecordWindow(null, c_tree_id, null, idTable);
                //  form.setVisible(true);
            } else {

                JOptionPane.showMessageDialog(null, "Редактирование недоступно !", "Редактирование", JOptionPane.INFORMATION_MESSAGE);
            }

        }

        updateTable();

        if (selectRow > 0) {
            table.setRowSelectionInterval(selectRow, selectRow);
            table.scrollRectToVisible(table.getCellRect(selectRow, 1, true));
        }
    }

    private void showPopupMenu(MouseEvent e) {

        JPopupMenu popupMenu = new JPopupMenu();
        ImageIcon icon = createIcon("editrow.png");

        JMenuItem itemEdit = new JMenuItem("Редактировать запись", icon);

        itemEdit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showRowTable();
            }
        });

        popupMenu.add(itemEdit);

        icon = createIcon("delrow.png");
        JMenuItem itemDeled = new JMenuItem("Удалить запись", icon);

        itemDeled.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                delRowTable();
            }
        });

        popupMenu.add(itemDeled);

        popupMenu.show(table, e.getX(), e.getY());

    }

    public final class OnMouseRowClick extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            Object object = e.getSource();

            if (object instanceof JTable) {

                selectCol = table.getSelectedColumn();
                selectRow = table.getSelectedRow();

            }

            if (selectCol == -1) {
                return;
            }

            if (selectRow == -1) {
                return;
            }

            if (e.getButton() == 3) {

                if (TablePanelParam.this.statusTable == ST_TABLE) {
                    showPopupMenu(e);
                }
            }

            try {

                nameTable = rsTable.getMetaData().getTableName(selectCol + 1).toLowerCase();
                nameTable = SqlTask.getNameTableByView(nameTable);
                hmKeys = SqlTask.getPrimaryKeyMap(null, nameTable);
                hmRecord.clear();
                rsTable.absolute(selectRow + 1);

                if (hmKeys != null && !hmKeys.isEmpty()) {

                    for (String key : hmKeys.keySet()) {

                        Object o = rsTable.getObject(key);
                        hmKeys.put(key, o);
                        c_tree_id = o;

                        if (c_tree_id instanceof Integer) {
                            //  TreeStructurePanel.currentId = (Integer) c_tree_id;
                        }
                    }

                }

                //   MainForm.lblCurrObject.setText("" + c_tree_id + "[" + selectRow + "][" + selectCol + "]");
                if (nameTable.equals("objects")) {

                    String captionRow = Work.getCaptionObjectByCol(rsTable, selectCol + 1);

                 //   MainForm.lblCurrObject.setText(captionRow);
                    // MainForm.iCurrObject = (Integer) c_tree_id;
                } else {
                 //   MainForm.iCurrObject = null;
                    // MainForm.lblCurrObject.setText("");
                }

            } catch (SQLException ex) {
                MainWorker.deffLoger.error("", ex);
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {

            if (e.getClickCount() >= 2) {

                if (statusTable == ST_SELECT) {
                    setNotifyObservers(TablePanelParam.this);
                } else {

                    showRowTable();
                }

            } else if (e.getClickCount() >= 3) {

                Toolkit.getDefaultToolkit().beep();
                selectCol = table.getSelectedColumn();
                selectRow = table.getSelectedRow();

                boolean isLevel = false;
                boolean isUser = false;

                c_tree_id = null;

                if (selectRow == -1) {
                    return;
                }

                selectRow = selectRow + 1;
                //   try {

                //   rsTable.absolute(selectRow);
                // hmRecord.clear();
                // SqlTask.addParamToMap(rsTable, hmRecord);
                if (hmKeys != null && !hmKeys.isEmpty()) {

                    for (String key : hmKeys.keySet()) {

                        if (hmRecord.containsKey(key)) {
                            Object o = hmRecord.get(key);
                            hmKeys.put(key, o);
                            c_tree_id = o;
                        }
                    }
                }

                //     } catch (SQLException ex) {
                //       DbfClass.setLog("Таблица", ex);
                // }
                isLevel = (isSetStatus(esLevelAccess0) || isSetStatus(esLevelAccess1));
                isUser = true;

                actionSelectRow.setEnabled(selectRow != -1 && c_tree_id != null);

                if (IsdeleteRecord(nameTable) && selectRow != -1) {
                    // actionDelete.setEnabled(isLevel);
                    // actionEdit.setEnabled(isLevel || isUser);
                } else if (nameTable.equals("schedules")) {
                    // actionDelete.setEnabled(isLevel);
                    // actionEdit.setEnabled(isLevel);
                } else {
                    //    actionDelete.setEnabled(false);
//                    actionEdit.setEnabled(false);
                }

            }

        }
    }

    public final class OnSelectRowTable implements ListSelectionListener {

        int poz = -1;

        @Override
        public void valueChanged(ListSelectionEvent e) {

            Integer rowFirst = e.getFirstIndex();

            Integer rowlast = e.getLastIndex();

            DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();

            Integer rowSelect = dlsm.getMaxSelectionIndex();
            if (rowSelect != -1 && rowSelect != poz) {

                Integer id = hmkeyRow.get(rowSelect);
                setNotifyObservers(id);
                poz = rowSelect;

            }

        }
    }

    private final class OnMouseHeaderClick extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            Object object = e.getSource();

            if (object instanceof JTableHeader) {
                try {
                    JTableHeader header = (JTableHeader) object;
                    int col = header.columnAtPoint(new Point(e.getX(), e.getY()));
                    TableColumn tc = header.getColumnModel().getColumn(col);
                    TableCellRenderer tcr = tc.getHeaderRenderer();
                    ((dateTableHeaderRenderer) tcr).setFcol(col);
                    fcol = col;
                    NameFcol = rsTable.getMetaData().getColumnName(fcol + 1).toLowerCase();
                } catch (SQLException ex) {

                    //     DbfClass.setLog("OnMouseHeaderClick", ex);
                }

            }

        }
    }

    /**
     *
     * @author 1
     */
    public class dateTableColorRenderer extends DefaultTableCellRenderer {

        Rectangle r1;
        Rectangle r2;
        Rectangle r3;
        String txt;

        public dateTableColorRenderer() {
        }

        @Override
        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);

            Graphics2D gd = (Graphics2D) g;

            Rectangle r = this.getBounds();
            Font f = g.getFont();
            FontRenderContext context = gd.getFontRenderContext();
            Rectangle2D rTxt;

            //       r1=new Rectangle(r.x, r.y, r.width/3, r.height);
            //      r2=new Rectangle(r.width/3, r.y, r.width/3, r.height);
            //    r3=new Rectangle(r.width/3*2, r.y, r.width/3, r.height);
            Rectangle rPoz;

            String[] ses = txt.split(";");

            if (ses.length != 3) {
                super.paintComponent(g);
                return;
            }

            for (int i = 0; i < ses.length; i++) {
                String s = ses[i];

                int prc = Integer.parseInt(s);

                rTxt = f.getStringBounds(s, context);
                rPoz = new Rectangle(r.width / 3 * i, 0, r.width / 3, r.height);

                int iPoz = r.width / 3 * i;

                // g.drawRect(iPoz, 0, r.width/3, 16);
//g.setColor(Color.BLACK);
// g.drawRect(rPoz.x, 0, r.width/3, 16);
                if (prc == 0) {
                    g.setColor(Color.RED);
                } else if (prc > 0 && prc < 100) {

                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.GREEN);
                }

                g.fillRect(rPoz.x + 1, rPoz.y + 1, rPoz.width - 1, rPoz.height - 1);

                g.setColor(Color.BLACK);

                //g.drawRect(rPoz.x, 0, r.width/3, 16);
                // g.fillRect(rPoz.x, rPoz.y, rPoz.width, rPoz.height);
                int x = (int) ((rPoz.width - rTxt.getWidth()) / 2);
                int y = (int) ((rPoz.height - rTxt.getHeight()) / 2);

                int ascent = (int) -rTxt.getY();

                int baseY = y + ascent;

                g.drawString(s + "% ", rPoz.x + x, baseY);

            }

            //    this.setBackground(Color.BLUE);
            // g.drawRect(r1);
            // this.setBackground(Color.GREEN);
            //  g.drawRect(r.width / 3, 0, r.width / 3, 16);
            //    g.drawString(r.toString(), 15, 0);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Rectangle r = this.getBounds();

            r1 = new Rectangle(r.x, r.y, r.width / 3, r.height);
            r2 = new Rectangle(r.width / 3, r.y, r.width / 3, r.height);
            r3 = new Rectangle(r.width / 3 * 2, r.y, r.width / 3, r.height);

            //this.setigetGraphics().drawRect(0, 0, 10, 16);
            // this.setBackground(Color.RED);
            txt = "0;0;0";

            if (value != null) {
                txt = (String) value;

                //   this.setIcon(icon0);
            }

            return this;
        }
    }

    /**
     *
     * @author 1
     */
    public class DateTableSelectColorRenderer extends DefaultTableCellRenderer {

        JLabel label;
        Rectangle r1;
        Rectangle r2;
        Rectangle r3;
        String txt;
        HashSet<Integer> hsErrors;
        HashSet<Integer> hsOk;
        HashSet<Integer> hsBase;
        Rectangle r;
        int pRow;
        int pCol;
        HashMap<Integer, String> hashMapInfo;
        Integer idColInfo;

        public DateTableSelectColorRenderer() {

            hsErrors = new HashSet<Integer>();
            hsOk = new HashSet<Integer>();
            hsBase = new HashSet<Integer>();
            label = new JLabel();
            label.setOpaque(true);
            //  label.setHorizontalAlignment(JLabel.RIGHT);

            //  hsErrors.add(5);
            //  hsErrors.add(9);
            //hsOk.add(10);
            //hsOk.add(11);
        }

        public void setInfo(MapMessageProcess messageError) {

            hashMapInfo = messageError.getInfoRows();

            idColInfo = messageError.getIdColInfo() - 1;

            for (Integer row : hashMapInfo.keySet()) {
                String msg = hashMapInfo.get(row);

                if (msg == null) {

                    hsOk.remove(row);
                    hsErrors.remove(row);
                    hsBase.remove(row);
                    continue;
                }

                if (msg.equals(MapMessageProcess.DATA_GET)) {

                    hsOk.add(row);
                } else if (msg.equals(MapMessageProcess.DATA_BASE)) {

                    hsBase.add(row);
                } else {

                    hsErrors.add(row);
                }

            }

        }

        public void setErrorRow(int iRow) {
            hsErrors.add(iRow);
        }

        public void setOkRow(int iRow) {
            hsOk.add(iRow);
        }

        public void clearSelectRows() {
            hsErrors.clear();
            hsOk.clear();
            hsBase.clear();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c;

            //  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (hsErrors.isEmpty() & hsOk.isEmpty() & hsBase.isEmpty()) {

                c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            } else {

                if (value != null) {
                    label.setText(value.toString());
                } else {
                    label.setText("-");
                }

                if (column == 0) {
                    label.setText(hashMapInfo.get(row));
                }

                if (hsErrors.contains(row)) {
                    c = label;
                    label.setBackground(new Color(0xff, 0xb8, 0xb8));
                } else if (hsOk.contains(row)) {
                    c = label;
                    label.setBackground(new Color(0xa6, 0xff, 0xa6));

                } else if (hsBase.contains(row)) {
                    c = label;
                    label.setBackground(new Color(0xbb, 0xf0, 0xff));

                } else {

                    c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                }

            }

            return c;
        }
    }

    /**
     *
     * @author 1
     */
    public class dateTableHeaderRenderer extends DefaultTableCellRenderer {

        String path = "/resources/images/find.png";
        int fcol = 0;

        public int getFcol() {
            return fcol;
        }

        public void setFcol(int fcol) {
            this.fcol = fcol;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            this.setBackground(new Color(0xd0, 0xdb, 0xf8));

            // this.setBackground(new Color(0xC0, 0xC0, 0xC0));
            this.setBorder(new BevelBorder(0));

            //((JLabel) this).sethIcon(img);
            this.setSize(new Dimension(this.getWidth(), 35));

            if (fcol == column) {

                ImageIcon img = new ImageIcon(getClass().getResource(path));

                ((JLabel) this).setIcon(img);

            } else {

                ((JLabel) this).setIcon(null);

            }

            return this;
        }
    }

    private void setHeaderColumn(JTable table, int select_col) {

        dateTableHeaderRenderer tcr = new dateTableHeaderRenderer();
        tcr.setFcol(select_col);

        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setHeaderRenderer(tcr);
        }

    }

    public void Refresh() {

        table.setModel(new clsDbfTableModel());
        setHeaderColumn(table, fcol);

        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
        excludeStatus(esSelectRow);

//table.getColumnModel().getColumn(0).setMinWidth(150);
    }

    /**
     *
     * @author 1
     */
    public class clsDbfTableModel extends AbstractTableModel {

        public clsDbfTableModel() {

            formatter2 = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

            try {
                rsmd = (ResultSetMetaData) rsTable.getMetaData();

            } catch (SQLException ex) {
               // DbfClass.setLog("", ex);

            }

        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {

            String className = null;
            try {
                className = rsmd.getColumnClassName(columnIndex + 1);
            } catch (SQLException ex) {
                MainWorker.deffLoger.error("", ex);
            }

            if (className.equals(Boolean.class.getName())) {

                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public int getRowCount() {
            try {

                if (rsTable == null) {
                    return 0;
                }

                rsTable.last();
                int row = rsTable.getRow();

                return row;

            } catch (SQLException ex) {

              //  DbfClass.setLog("", ex);
                return 0;
            }
        }

        @Override
        public int getColumnCount() {
            try {

                if (rsTable == null) {
                    return 0;
                }

                //   rsmd = (ResultSetMetaData) rs.getMetaData();
                return rsmd.getColumnCount();

            } catch (SQLException ex) {

              //  DbfClass.setLog("", ex);
                return 0;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {

                if (rowIndex == -1) {
                    return 0;
                }
                if (rsTable == null) {
                    return 0;
                }

                rsTable.absolute(rowIndex + 1);

                String NameCol = rsmd.getColumnName(columnIndex + 1).toLowerCase();

                if (NameCol.indexOf("password") != -1 && !isSetStatus(esLevelAccess0)) {

                    return "***";

                }

                Object obj = rsTable.getObject(columnIndex + 1);

                if (NameCol.contains("c_tree_id")) {

                    hmkeyRow.put(rowIndex, (Integer) obj);

                }

                if (obj instanceof Timestamp) {

                    if (obj != null) {
                        String fmt = formatter2.format(obj);
                        return fmt;
                    }

                }

                return obj;

            } catch (SQLException ex) {

            //    DbfClass.setLog("", ex);
                return "-";
            }

        }

        @Override
        public String getColumnName(int c) {

            String NameCol = "";
            try {
                NameCol = rsmd.getColumnName(c + 1).toLowerCase();

            } catch (SQLException ex) {

           //     DbfClass.setLog("", ex);
            }

            if (isSetStatus(esShowNameColumn)) {

                return NameCol;
            } else {
                return hmColumns.get(NameCol);
            }

        }
    }
}

package constatant_static;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import servise.MainWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author 1
 */
public class SettingActions {

    public static final int CLS_TREETABLE = 0;
    public static final int CLS_SXEMA = 1;
    //Общее
    public static final String CM_ABOUT = "О программе...";
    public static final String CM_CRC = "Контрольная сумма";

    public static final String CM_SET_CONFIG_OBJECT = "Загрузить конфигурацию объекта";
    public static final String CM_SET_UPDATE = "Загрузить обновления";
    public static final String CM_EXIT = "Выход";
    public static final String CM_WHAT = "Что это такое?";
    public static final String CM_HELP = "Справка";
    public static final String CM_SHOW_NAME_COLUMN = "Показать имена столбцов";
    public static final String CM_CHECK_REFERENCE = "Проверить целостность таблицы";
    public static final String CM_CHECK_REFERENCE_DATA = "Технологический анализ параметров программы";
    public static final String CM_CHECK_DATA = "Проверить время поступление данных";
    public static final String CM_CHECK_GPRS = "Загрузить поступившие  данные  GPRS";
    public static final String CM_SAVE_RECORD = "Сохранить изменения";
    public static final String CM_SAVE_AND_EXIT_RECORD = "Сохранить изменения и выйти";
    public static final String CM_CHECK_CONFIG_TABLES = "Сохранить изменения и выйти";
    public static final String CM_CEND_80020 = "Протокол 80020";
    public static final String CM_EXPORT = "Экспорт";
    public static final String CM_IMPORT_EXPORT = "Импорт Экспорт данных";

    public static final String CM_GET_SERVER = "Сервер данных";
    public static final String CM_SERVISE = "Сервисные команды";
    public static final String CM_TUNING = "Настройки";


    // Добавление и Удаление Превышений и расчетов
    public static final String CM_OBJ_CHECK_ADD = "Добавить объект контроля";
    public static final String CM_OBJ_CHECK_DEL = "Удалить объект контроля";
    // Выбор очищение Объекта
    public static final String CM_OBJECT_SELECT = "Выбрать оъект из списка";
    public static final String CM_OBJECT_CLEAR = "Очистить название объекта ";
    public static final String CM_SETTING_GLOBAL = "Серверные  настройки";
    public static final String CM_SETTING_LOCAL = "Локальные настройки";
    // EXCEL
    public static final String CM_EXCEL_GO_REPORT = "Создать отчет в Excel";
    public static final String CM_EXCEL_EXPORT = "Экспорт в Excel";
    public static final String CM_PIRAMIDA_SETTING = "Экспорт данных";
    public static final String CM_PIRAMIDA_COMMERC = "Повторно отправить данные('ПИРАМИДА')";
    public static final String CM_PIRAMIDA_80020 = "Повторно отправить данные(протокол 80020)";
    public static final String CM_SETTING_DIALING = "Параметры автодозвона по GSM";
    // Электронная  почта
    public static final String CM_SEND_MESSAGE = "Послать сообщение по почте";
    public static final String CM_SET_EXCHANGE_PARAM = "Установить параметры Exchange server";
    // Экспорт из Инспектора v 1.1
    public static final String CM_IMPORT_INSPECTOR = "Импорт дерева объектов учета из  'Excel'";
    public static final String CM_IMPORT_IN_DBF = "Импорт из другой базы";
    public static final String CM_IMPORT_IN_XML = "Импорт таблицы из XML";
    public static final String CM_UPDATE_PROGRAM = "Обновление программы из ZIP файла";
    // Таблица
    public static final String CM_PRINTTAB = "Печать";
    public static final String CM_SORT_AZ = "Сортировать по возрастанию";
    public static final String CM_SORT_ZA = "Сортировать по убыванию";
    public static final String CM_RESIZE_TABLE = "Таблица";
    public static final String CM_EDIT_ROW_TABLE = "Редактировать запись";
    public static final String CM_DELETE_ROW_TABLE = "Удалить запись";
    public static final String CM_ADD_ROW_TABLE = "Добавить запись";
    public static final String CM_SAVE_TABLE = "Сохранить таблицу в XML";
    public static final String CM_TABLE_LOAD = "Загрузить таблицу из XML";
    public static final String CM_TABLE_GRAFICS = "Создать график";
    //Расписание
    public static final String CM_SHEDULE_NEW = "Новое расписание";
    public static final String CM_SHEDULE_JURNAL = "Журнал расписаний";
    public static final String CM_SHEDULE_GO_NOW = "Запустить расписание";
    public static final String CM_SHEDULE_START = "Запуск расписания";
    public static final String CM_SHOW_POINT = "Соединения";
    public static final String CM_SHOW_COUNTERS = "Точки учета";
    public static final String CM_SHOW_CONTROLLERS = "Контроллеры";
    public static final String CM_SHOW_OBJECT = "Объекты";
    public static final String CM_SHOW_USERS = "Пользователи";
    public static final String CM_REFRESH_TREE = "Обновить структуру объектов";
    public static final String CM_REFRESH_SVODVEDOMOST = "Обновить сводную ведомость";
    public static final String CM_SHOW_JURNAL = "Показать журнал превышений";
    public static final String CM_SHOW_JURNAL_OVER = "Показать журнал превышений";
    public static final String CM_SHOW_JURNAL_DISCRET = "Показать журнал срабатывания контактов";
    public static final String CM_SHOW_JURNAL_CHANNEL = "Текущий журнал запроса данных";
    public static final String CM_SHOW_TABLE_USERS = "Таблица пользователей";
    public static final String CM_SHOW_TABLE_OBJECTS = "Таблица объектов учета";
    public static final String CM_SHOW_TABLE_SVODNAYA = "Сводная таблица объектов учета";
    public static final String CM_SHOW_TABLE_POINTS = "Таблица узлов учета";
    public static final String CM_SHOW_SHEDULE = "Показать расписание";
    public static final String CM_SHOW_ALL_VALUE = "Все данные";
    public static final String CM_SHOW_PROF_CURRENT = "Профиль тока";
    public static final String CM_SHOW_PROF_VOLTAGE = "Профиль напряжения";
    public static final String CM_SHOW_PROF_POWER = "Профиль мощности";
    public static final String CM_SHOW_ENERGY_DN = "Энергия";
    public static final String CM_SHOW_PROPERTIES = "Локальные установки";
    public static final String CM_OPEN_BASE = "Открыть базу";
    public static final String CM_ADD_POINT = "Добавить соединение";
    public static final String CM_ADD_COUNTER = "Добавить точку учета";
    public static final String CM_ADD_CONTROLLER = "Добавить  контроллер";
    public static final String CM_ADD_OBJECT = "Добавить  объект учета";
    public static final String CM_DEL_OBJECT = "Удалить объект учета";
    public static final String CM_DEL_ALL_OBJECT = "Удалить объекты";
    public static final String CM_EDIT_OBJECT = "Редактировать  объект";
    public static final String CM_DEL_POINT = "Удалить  соединение";
    public static final String CM_DEL_COUNTER = "Удалить точку учета";
    public static final String CM_DEL_CONTROLLER = "Удалить  контроллер";
    public static final String CM_EDIT_POINT = "Редактировать  соединение";
    public static final String CM_EDIT_COUNTER = "Редактировать точку учета";
    public static final String CM_EDIT_CONTROLLER = "Редактировать  контроллер";
    public static final String CM_SET_PARAMETERS = "Установка параметров программы";
    public static final String CM_GET_BY_SHEDULE = "Опросить по текущему расписанию";
    public static final String CM_SET_FILTER = "Отбор по фильтру";
    public static final String CM_EXPAND_ALL = "Развернуть дерево объектов";
    public static final String CM_COLLAPSE_ALL = "Свернуть дерево объектов";
    //СЧЕТЧИК
    public static final String CM_GET_PROFIL_COUNTER = "Прочитать профиль счетчика";
    public static final String CM_GET_VALUE_COUNTERS = "Прочитать из счетчика";
    public static final String CM_SET_VALUE_COUNTERS = "Записать в счетчик";
    public static final String CM_SET_VALUE = "Запись данных...";
    public static final String CM_SET_COMMANDS_XML = "Обновление команд из XML";
    public static final String CM_UPDATE_TABLE_XML = "Обновление  параметров данных";
    public static final String CM_SET_TYPES_XML = "Обновление  типов данных";
    public static final String CM_SET_NAME_FILE = "Название файла";
    public static final String CM_GET_VALUE = "Запрос данных...";
    public static final String CM_SHOW_VALUE_GSM = "Экспорт текущего запроса в 'Excel'";
    public static final String CM_STOP_SQL = "Остановить процесс";
    public static final String CM_GO_SQL = "Выполнить запрос";
    // МНЕМОСХЕМА
    public static final String CM_LOADSXEM = "Загрузить мнемосхему";
    public static final String CM_REFRESHSXEM = "Обновить мнемосхему";
    public static final String CM_SXEM_NEXT = "Следующая схема";
    public static final String CM_SXEM_PREVIOUS = "Предыдущая схема";
    // КОНТРОЛЛЕР
    public static final String CM_GET_CURR_STATE_CHNL = "Получить текущее состояние каналов";
    public static final String CM_GET_TUNING_CONDITION = "Получить настройки условий фиксации";
    public static final String CM_SET_TUNING_CONDITION = "Записать настройки условий фиксации";
    public static final String CM_CONTROLLER_ADD_SWITCH = "Добавить объект управления";
    public static final String CM_CONTROLLER_ADD_DEL_COUNTERS = "Скорректировать количество счетчиков";
    public static final String CM_CONTROLLER_ADD_REALAY = "Добавить канал дискретного ввода";
    // GSM
    public static final String CM_TREE_GSM_SAVE_SEND = "Сохранить текущий  запрос данных";
    public static final String CM_TREE_GSM_VECTOR_DIAG = "Векторная диаграмма";
    public static final String CM_TREE_GSM_PROFIL_DIAG = "Профиль мощности";
    // МОДЕМ
    public static final String CM_MODEM_TEST = "Тест модема";
    public static final String CM_MODEM_BALANSE = "Баланс SIM карты GSM модема";
    private static final HashMap<String, Action> hmActSet = new HashMap<String, Action>();
    // ВСПЛЫВАЮЩЕЕ МЕНЮ
    public static final String CM_CLEAR_LOG = "Очистить лог";
    public static final String AB_NAME_COMMAND = "TypCommand";
    public static final String AB_DOP_INFO = "DopInfo";

    public static Action addAction(String nameAction, Action action) {

        SettingActions.hmActSet.put(nameAction, action);

        return action;
    }

    public static ImageIcon createIcon(String filename) {
        String path = "/resources/images/" + filename;

        try {
            return new ImageIcon(SettingActions.class.getResource(path));
        } catch (Exception e) {
            MainWorker.ShowError(e.getMessage());
            return null;
        }

    }

    private static class ActionButton extends AbstractAction {

        public ActionButton(String Caption, Icon icon_On, Icon icon_Off, String help, String di, String command) {

            putValue(NAME, Caption);
            putValue(SMALL_ICON, icon_On);
            putValue("ICON_OFF", icon_Off);

            putValue(SHORT_DESCRIPTION, help);
            putValue(AB_NAME_COMMAND, command);
            putValue(AB_DOP_INFO, di);


        }

        public void actionPerformed(ActionEvent e) {


            putValue("EVENTS", e);

        }
    }

    public static enum esStatus {

        esOpen,
        esGsmModemOn, // Модем ответил
        esGsmPhoneGo, // Идет дозвон до прибора
        esGsmPhoneOn, // Дозвон состоялся
        esGsmConnectOn, // Прибор ответил
        esGoCommands,// Начало запроса данных
        esCheckEnd, // проверка после запроса данных
        esEndTime, // Закончилось время ожидания ответа
        esStopCommands, // Остановка запроса данных
        esBlockButton, //блокировка кнопки
        esEndGoPack,//Продолжить после Запроса в пакетном режиме
        esStatusOn,
        esSaveOn,// Функция разрешения записи в объект
        esUpdateRow,// Запись редактировалась
        esFilter,//Установлен фильтер
        esQuestion,// Идет запрос
        esRefreshSvodVed,// Обновляется сводная ведомость
        esDeleteObjOn,// Можно удалять текущий объект
        esQuestionByFilter, //Запрос по фильтру
        esSelectObject,//Выбрана таблица объектов
        esSelectRow,// Есть выбраная строка
        esSelectPoint,
        esSelectCounters,
        esSelectController,
        esGSMOn, // включена функция сбора данных по GSM
        esDTROn, // Класть трубку по DTR
        esGPRSOn, //  Идет сбор данных по GPRS
        esPiramida80020On,//Идет экспорт в пирамиду по протоколу 0020
        esPiramidaCommerc,//Идет экспорт в пирамиду коммерческих данных
        esCheckValue,// идет проверка превышений значений
        esAddArxive,// идет перевод данных в архив
        esServer, // не графический режим сервера
        esLocalDbf,//  локальная база(Derby)
        esSoundOn, // Проигрываем звуковой файл
        esScheduleOn, // Рарешить проверку расписаний
        esShowSxem, // Показывать графические схемы

        esStop, // Остановка любого процесса
        esSetPageTable, //Активна страница Таблицы
        esSetPageStrucrure, //Активна страница Структура
        esSetPageSxem, //Активна страница Схема

        esSxemNextOn, // есть следующая схема
        esSxemPreviosOn, // есть предыдущая схема

        esLevelAccess3,// "Гость"  пользователи с эти уровнем доступа могут только просматривать данные,
        //сохраненные в базе данных. Никакие изменения в базу данных они вносить не могут.
        esLevelAccess2, //"Гость" пользователи с эти уровнем доступа могут только просматривать данные,
        //сохраненные в базе данных. Никакие изменения в базу данных они вносить не могут.
        esLevelAccess1,  // доступ с правами на изменение. Пользователи с этим уровнем доступа имеют права создавать объекты
        //в базе данных, модифицировать существующие объекты и т.д.
        esLevelAccess0, // уровень доступа  Администратора(Полный доступ)
        //,  созлавать пользователей   выдавать права другим пользователям

        esLevelAccessAll, // уровень доступа  Программиста (Полный доступ)
        //, Полный доступ

        esChangeParent, // Изменился родитель
        esShowNameColumn, // Показать имена столбцов
        esShowError, // Показать ошибки
        esShowDiscret, // Показать дискретники
        esShowOver;  // Показать превышения

        /*
         *
         * Функция проверки видимости акции
         */
        private static void setVIsibleAction(String act, boolean visible) {

            Action a = hmActSet.get(act);


            Component cmp = (Component) a.getValue(act);


            if (cmp != null) {
                cmp.setVisible(visible);

            }

        }


        /*
         *
         * Функция проверки доступности акции
         */
        private static void setEnabledAction(String act, boolean enabled) {

            Action a = hmActSet.get(act);

            if (a != null) {

                a.setEnabled(enabled);
            }

        }

        private static void CheckAction() {


            if (hmActSet.size() == 0) {
                return;
            }

            boolean yes = false;

            yes = (isSetStatus(esLevelAccess0) || isSetStatus(esLevelAccess1) || isSetStatus(esLevelAccess2));

            boolean dispether = (isSetStatus(esLevelAccess0) || isSetStatus(esLevelAccess1));


            // Запись изменений

            setEnabledAction(CM_SAVE_RECORD, isSetStatus(esUpdateRow) && !isSetStatus(esLevelAccess3) && !isSetStatus(esLevelAccess2));

            setEnabledAction(CM_ADD_ROW_TABLE, !isSetStatus(esLevelAccess3) && !isSetStatus(esLevelAccess2));

            setEnabledAction(CM_DELETE_ROW_TABLE, isSetStatus(esSelectRow) && !isSetStatus(esLevelAccess3) && !isSetStatus(esLevelAccess2));

            setEnabledAction(CM_EDIT_ROW_TABLE, isSetStatus(esSelectRow));


            // Работа с деревом ообъектов
            setEnabledAction(CM_ADD_POINT, dispether);
            setEnabledAction(CM_ADD_CONTROLLER, isSetStatus(esSelectPoint) && dispether);
            setEnabledAction(CM_ADD_COUNTER, isSetStatus(esSelectPoint) && dispether);


            // Локальные установки  серверные установки
            setEnabledAction(CM_SETTING_LOCAL, isSetStatus(esLevelAccess0));
            setEnabledAction(CM_SETTING_GLOBAL, isSetStatus(esLevelAccess0));
            setEnabledAction(CM_SET_CONFIG_OBJECT, isSetStatus(esLevelAccess0));


            // Добавление удаление превышений

            setEnabledAction(CM_OBJ_CHECK_ADD, !isSetStatus(esLevelAccess3));
            setEnabledAction(CM_OBJ_CHECK_DEL, !isSetStatus(esLevelAccess3));


            // Выбор -удаление объектов

            //  setEnabledAction(CM_OBJECT_SELECT, !IsSet(esLevelAccess3) && !IsSet(esLevelAccess2));
            //  setEnabledAction(CM_OBJECT_CLEAR, !IsSet(esLevelAccess3) && !IsSet(esLevelAccess2));


            // Новое расписание
            setEnabledAction(CM_SHEDULE_NEW, !isSetStatus(esLevelAccess3) && !isSetStatus(esLevelAccess2));


            // Добавить новый объект
            setEnabledAction(CM_ADD_OBJECT, !isSetStatus(esLevelAccess3) && !isSetStatus(esLevelAccess2));


            setEnabledAction(CM_MODEM_TEST, isSetStatus(esGSMOn) && !isSetStatus(esSetPageTable));


            setEnabledAction(CM_SET_FILTER, !isSetStatus(esFilter));


            setEnabledAction(CM_REFRESH_SVODVEDOMOST, !isSetStatus(esRefreshSvodVed));
            setEnabledAction(CM_DEL_OBJECT, isSetStatus(esDeleteObjOn));

            //    setVIsibleAction(CM_SXEM_NEXT, IsSet(esSetPageSxem));
            //  setVIsibleAction(CM_SXEM_PREVIOUS, IsSet(esSetPageSxem));

            setEnabledAction(CM_SXEM_NEXT, isSetStatus(esSxemNextOn));
            setEnabledAction(CM_SXEM_PREVIOUS, isSetStatus(esSxemPreviosOn));


            // hmActSet.get(CM_ADD_CONTROLLER).setEnabled(IsSet(esSelectPoint) &&
            //       !IsSet(esSetPageTable)&&
            //     !IsSet(esQuestion));

            //  hmActSet.get(CM_ADD_COUNTER).setEnabled(IsSet(esSelectPoint) &&
            //        !IsSet(esSetPageTable)&&
            //      !IsSet(esQuestion));

            // Счетчик

            //    hmActSet.get(CM_GET_VALUE_COUNTERS).setEnabled(!IsSet(esQuestion));

            //  hmActSet.get(CM_SET_VALUE).setEnabled(IsSet(esSaveOn));


            //    hmActSet.get(CM_STOP_SQL).setEnabled(IsSet(esQuestion));


        }

        public synchronized static void Exclude(esStatus se[]) {


            for (esStatus e : se) {
                hs.remove(e);
            }


            CheckAction();

        }

        public synchronized static void excludeStatus(esStatus se) {
            hs.remove(se);
            CheckAction();

        }

        public synchronized static void includeStatus(esStatus se) {
            hs.add(se);
            CheckAction();
        }

        public static void clearAllStatus(esStatus se) {
            hs.clear();
            CheckAction();
        }

        public static boolean isSetAll(Collection<esStatus> col) {

            return hs.containsAll(col);
        }

        public static boolean isSetStatus(esStatus se) {

            return hs.contains(se);
        }

        private static HashSet<esStatus> hs = new HashSet();
    }

    ;

    public static Action getAction(String Name) {


        return hmActSet.get(Name);
    }

    public static void setAction(String Name, Action aAction) {
        hmActSet.put(Name, aAction);
    }
}

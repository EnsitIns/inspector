/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.util.*;

/**
 * команды запроса информации
 *
 * @author 1
 */
public class CommandGet implements Cloneable {

    public int waitTime; //Время на выполнение команды
    public int sleepTime; //Пауза между командами
    public int countTry; // Количество попыток
    public String calcResult; // Формула расчета результата
    public String name; // Название команды
    public Object result;//Результат
    public Object sResult;// Строковое представление результата
    public HashMap<String, Object> hmPar;// Настройки команды
    public List<Integer> alSend;    // Посылка (TxD)
    // public LinkedList<Short> alAnswer;    // Шаблон ответа
    public List<Integer> alHelp;  //Вспомогательный
    public List<Integer> alResult;  // Ответ прибора общий
    public List<Integer> alAnsRes;  // Ответ по конкретному параметру
    public List<Integer> alSet;  // Подставляемые значения
    public String errorCmd;  //Сообщение при ошибке
    public LinkedHashMap tmChilds;  // Подчиненные команды (например профиль)
    public boolean bValueBase;  //Значение получено из базы
    public boolean bChild; //Подчиненная команда (Профиль)
    public boolean bSave; //Данные хронятся в базе)
    public Boolean bEmpty; //Признак ждущей или пустой  команды
    public String query; //строка запроса
    public String answer; //строка ответа
    public String Ok;
    public int lenCrc; // количество пришедших символов,после которого проверяется контрольная сумма
    public int number; // Порядковый номер команды в запросе
    public int id; //Идентификатор команды

    // Критическая ошибка после которой дальше не опрашиваем
    public boolean criticalError;

    public CommandGet() {
        alSend = new LinkedList<Integer>();
        // alAnswer=new LinkedList<Short>();
        //alResult = new ArrayList<Integer>();
        // alHelp = new ArrayList<List<Integer>>();
        bEmpty = false;
        bValueBase = false;
        bChild = false;
    }

    public Object clone() {

        try {

            CommandGet cl = (CommandGet) super.clone();
            cl.hmPar = (HashMap<String, Object>) hmPar.clone();
            return cl;

        } catch (CloneNotSupportedException ex) {

            throw new InternalError(ex.toString());
        }
    }

    public void clearChildCmd() {

        if (tmChilds != null) {
            tmChilds.clear();
        }

    }

    public void addByteInSet(Integer ib) {

        if (alSet == null) {

            alSet = new ArrayList<>();

        }
        alSet.add(ib);

    }


    public void addChildCmd(String key, CommandGet cg) {

        if (tmChilds == null) {
            tmChilds = new LinkedHashMap();
        }

        if (key == null) {

            int idx = tmChilds.size();
            tmChilds.put(idx, cg);
        } else {

            tmChilds.put(key, cg);

        }
    }

    public Object removProperty(Object keyname) {
        return hmPar.remove(keyname);
    }

    public Object getProperty(String namePar) {
        return hmPar.get(namePar);
    }

    public void putProperty(String namePar, Object param) {
        hmPar.put(namePar, param);
    }

    public HashMap<String, Object> getHmPar() {
        return hmPar;
    }

    public void setHmPar(HashMap<String, Object> hmPar) {
        this.hmPar = hmPar;
    }

    public void clearCommand() {

        alSend.clear();
        errorCmd = null;
        sResult = "";
        result = null;
        bValueBase = false;
    }
}

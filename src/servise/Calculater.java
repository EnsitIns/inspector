package servise;

/*
 * clsCalculate.java
 *
 * Created on 17.12.2007, 17:39:46
 *
 * Расчет строковых выражений
 * 
 */
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *
 * @author 1
 */
public class  Calculater {

    private static String answer = "";
    private static ArrayList<TData> DataList = SetConst();
    private static HashMap<String, Integer> hmPriority = setProiority();
    // Массив значений и имен переменных и именованных констант
    // Переменные
    public static HashMap<String, Double> hmVar = new HashMap<String, Double>();
    // private static ArrayList<TData> DataList = ;
    // Массив сообщений об ошибках
    private static ArrayList<String> ErrorList = new ArrayList();
    // Стек, используемый при трансляции
    private static StringBuffer TrStack = new StringBuffer();
    // Массив числовых констант
    private static ArrayList<Double> ConstList = new ArrayList();
// Текущая позиция трансляции
    // Содержимое стека
    private static LinkedList<String> linkedStack = new LinkedList<String>();
    // Выходная строка
    private static LinkedList<Object> linkedlOutput = new LinkedList<Object>();
    private static int Position;
    //  {Переменная, содержащая характеристику прочитанной лексемы}
    //   private static TSynt SItem ;
    private static double ResultF; // Результат вычислений
    private static final int CALC_ALL = 0;// Рассчитать все
    private static final int CALC_ROW = 1; // Рассчитать строку

    static enum TType {

        None, Number, Divider, Ident, Func, Part, All
    }

    {
    } // Тип лексем
    // Множество символов, используемых при записи числа
    private static final String SetNum = "0123456789,";
    // Множество символов - разделителей
    private static final String SetDiv = ";()=+-/*^{}\r";
    //   {Множество символов, используемых при записи идентификаторов}
    private static final String SetChar = "@abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
    // Массив идентификаторов функций
    private static final String[] Functions = {"exp", "sin", "cos", "sqrt", "abs", "ln", "tg", "arctg", "arccos"};
    // Максимальное количество числовых констант
    private static final int Nconst = 100;
    // число предопределенных констант
    private static final int MConst = 2;
    // Массив польской записи
    private static ArrayList<Integer> PZ = new ArrayList<Integer>();
    // Окно ввода-вывода результатов
    private JTextArea taMsg = null;
    private JTextPane textPane = null;

    public Calculater(final JTextPane textPane) {

        this.textPane = textPane;




        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {


                if (e.getKeyCode() == 119) //F8
                {
                    GoCalc(CALC_ALL);
                }

                if (e.getKeyCode() == 120) //F9
                {
                    GoCalc(CALC_ROW);
                }




            }
        });


    }

    public Calculater(final JTextArea taHex) {

        this.taMsg = taHex;

        this.taMsg.addKeyListener(new KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {

                if (evt.getKeyCode() == 119) //F8
                {
                    GoCalc(CALC_ALL);
                }

                if (evt.getKeyCode() == 120) //F9
                {
                    GoCalc(CALC_ROW);
                }


            }
        });



    }

    // приоритет операций
    static HashMap<String, Integer> setProiority() {

        hmPriority = new HashMap<String, Integer>();
        hmPriority.put("(", 0);
        hmPriority.put(")", 1);
        hmPriority.put("+", 2);
        hmPriority.put("-", 2);
        hmPriority.put("*", 3);
        hmPriority.put("/", 3);
        hmPriority.put("^", 4);
        hmPriority.put("exp", 4);
        hmPriority.put("sin", 4);
        hmPriority.put("cos", 4);
        hmPriority.put("sqrt", 4);
        hmPriority.put("abs", 4);
        hmPriority.put("ln", 4);
        hmPriority.put("tg", 4);
        hmPriority.put("arctg", 4);
        hmPriority.put("arccos", 4);




        return hmPriority;
    }


    /*
     *
     */
    public static Double calcExpress(String express, Double thisValue, HashMap<String, Object> hmConst) {
        Double result = null;
        if (Calculater.CreatePZ(express, true)) {


            Calculater.hmVar.put("this", thisValue);


            for (String name : Calculater.hmVar.keySet()) {

                if (hmConst.containsKey(name)) {

                    Double kfc = null;

                    Object okfc = hmConst.get(name);

                    if (okfc instanceof Integer) {

                        Integer i = (Integer) okfc;

                        kfc = i * 1.0;

                    } else if (okfc instanceof String) {

                        String skfc = (String) okfc;

                        try {
                            kfc = Double.parseDouble(skfc);

                        } catch (NumberFormatException e) {
                            //   MessageUser.SendMessage("Ошибка конвертации " + skfc + " в число!", MessageUser.MSG_ERROR, MessageUser.STR_NEW);
                            return null;

                        }


                    } else {

                        kfc = (Double) okfc;
                    }

                    Calculater.hmVar.put(name, kfc);

                } else if (!name.equals("this")) {

                    // MessageUser.SendMessage("Параметр " + name + "не найден !", MessageUser.MSG_ERROR, MessageUser.STR_NEW);
                    return null;
                }
            }



            result = Calculater.evaluateExp();


        }




        return result;
    }

    static ArrayList SetConst() {

        DataList = new ArrayList();
        DataList.add(new TData("PI", Math.PI));
        DataList.add(new TData("E", 2.71828183));

        return DataList;
    }

    private void GoCalc(int iGo) {
        String snext = "";

        ClearPZ();
        String s = textPane.getText();

        s = "((5+6)/(5*3))^3";

        //   hmVar.put("value", (double) 23);
        //   hmVar.put("kt", (double) 10);
        //   hmVar.put("kn", (double) 5);





        s = "sqrt(value*kt*kn)";

        //  s = "sqrt(81)";

        if (iGo == CALC_ROW) {
            StringTokenizer tokenizer = new StringTokenizer(s);

            while (tokenizer.hasMoreTokens()) {
                snext = tokenizer.nextToken();

            }
        } else {
            snext = s;
        }

        //  snext = "{Соотношения для расчетов}" + '\n' +
        //        "a=Pi/4;" + '\n' +
        //      "B1=Sqrt(tg(a)+3);" + '\n' +
        //    "Y=B1^2*(sin(a)+cos(a));" + '\n' +
        //  "10*Y";

        StringBuilder builder;

        snext = snext.trim();

        hmVar.clear();
        if (CreatePZ(snext, true)) {

            hmVar.put("value", (double) 23);
            hmVar.put("kt", (double) 10);
            hmVar.put("kn", (double) 5);



            Double d = evaluateExp();



            if (d != null) {
                answer = String.valueOf(d);

                builder = new StringBuilder();

                builder.append("=" + answer + '\n');

                textPane.setText(textPane.getText() + builder.toString());


                if (iGo == CALC_ALL) {

                    builder = new StringBuilder();

                    for (TData dt : DataList) {

                        builder.append(dt.Name + "=" + Double.toString(dt.Data) + '\n');


                    }

                    textPane.setText(textPane.getText() + builder.toString());

                }




            }
        } else {

            builder = new StringBuilder();



            builder.append("Ошибки:" + '\n');

            for (String err : ErrorList) {
                builder.append(err + '\n');
            }

            textPane.setText(textPane.getText() + builder.toString());

        }



        //taHex.setFocusable(true);
        //taHex.setCaretPosition(taHex.getText().length());
    }

    // Индекс последнего
    private static int High(StringBuffer ARR) {
        return ARR.length() - 1;
    }

    private static int High(ArrayList ARR) {
        return ARR.size() - 1;
    }

    private static int High(double[] ARR) {
        return ARR.length - 1;
    }

    private static int High(int[] ARR) {

        return ARR.length - 1;
    }

    private static int calc(Double v1, Double v2, String eval, int poz) {

        int count = 0;

        Double result = null;

        if (eval.equals("+")) {

            result = v1 + v2;
            count = 2;

        } else if (eval.equals("-")) {
            result = v1 - v2;
            count = 2;

        } else if (eval.equals("*")) {
            result = v1 * v2;
            count = 2;

        } else if (eval.equals("/")) {

            result = v1 / v2;

            count = 2;

        } else if (eval.equals("^")) {

            result = Math.pow(v1, v2);

            count = 2;

        } else if (eval.equals("cos")) {

            result = Math.cos(v2);

            count = 1;

        } else if (eval.equals("sqrt")) {

            result = Math.sqrt(v2);

            count = 1;

        } else if (eval.equals("sin")) {

            result = Math.sin(v2);

            count = 1;

        } else if (eval.equals("tan")) {

            result = Math.tan(v2);

            count = 1;

        } else if (eval.equals("exp")) {

            result = Math.exp(v2);

            count = 1;

        } else if (eval.equals("abs")) {

            result = Math.abs(v2);

            count = 1;

        } else if (eval.equals("exp")) {

            result = Math.exp(v2);

            count = 1;

        } else {

            return 0;
        }

        linkedlOutput.set(poz, result);

        //   "exp", "sin", "cos", "sqrt", "abs", "ln", "tg", "arctg", "arccos"

        return count;
    }

    // Вычислить выражение
    public static Double evaluateExp() {


        Double value = null;
        String divid = null;

        Object o1 = null;
        Object o2;
        Double e1 = null;
        Double e2 = null;


        // Заменяем все переменные на значения

        for (int i = 0; i < linkedlOutput.size(); i++) {

            Object o = linkedlOutput.get(i);

            if (o instanceof String) {

                String name = (String) o;
                Double d;

                if (hmVar.containsKey(name)) {

                    d = hmVar.get(name);
                    linkedlOutput.set(i, d);

                }
            }
        }

        int sizeStr = linkedlOutput.size();


        int i = 0;
        while (true) {
            Object o = linkedlOutput.get(i);


            if (linkedlOutput.size() == 1) {
                // Конец расчета
                value = (Double) linkedlOutput.get(0);
                break;
            }


            // Если не выходим из цикла

            if (i > sizeStr) {
                return null;
            }

            // Ищем операцию




            if (o instanceof String) {

                // Операция
                divid = (String) o;

                if (i < 2) {

                    o2 = linkedlOutput.get(0);
                    e1 = null;
                    e2 = (Double) o2;


                } else {

                    o1 = linkedlOutput.get(i - 2);
                    o2 = linkedlOutput.get(i - 1);

                    e1 = (Double) o1;
                    e2 = (Double) o2;


                }

// Считаем
                int count = calc(e1, e2, divid, i);

                // Удаляем

                if (count == 2) {
                    linkedlOutput.remove(o1);
                    linkedlOutput.remove(o2);

                } else if (count == 1) {
                    linkedlOutput.remove(o2);
                } else {
                    return null;
                    //ОШИБКА !
                }


                // продолжаем

                i = 0;
                continue;

            }
            i++;
        }


        return value;

    }

    //procedure SyntItem(S: string; First: boolean = false; Pos: Integer = 1);
// Распознавание синтаксических элементов строки S
// First=true указывает на первое обращение
// Pos - позиция в строке при первом обращении
//private static
    private static void SyntItem(String S, boolean First, int Pos) {

        char a = ' ';
        if (S.equals("")) {
            SItem.mode = TType.All;
            return;
        }

// Если первое обращение,
// то Position = Pos (заданному значению или 1)
        if (First) {
            Position = Pos;
        }
        // Пропуск комментариев и пробельных символов
        do {
// Пропуск комментариев
            if ((Position < S.length()) && S.charAt(Position) == '{') {

                do {
                    Position++;
                    a = S.charAt(Position);

                } while ((Position < S.length()) && (a != '}'));

                Position++;
            }
// Пропуск пробелов и переводов строки
            if (Position < S.length()) {

                while ((S.charAt(Position) == ' ') || (S.charAt(Position) == '\r') || (S.charAt(Position) == '\n') || (S.charAt(Position) == '\t')) {
                    Position++;
                }
            }
        } while ((Position < S.length()) && S.charAt(Position) == '{');

        SItem.Error = false;
        SItem.Pos1 = Position;

        // Если Position > длины строки, то уход и mode = All
        if (Position >= S.length()) {
            SItem.mode = TType.All;
            return;
        }

        // Занесение текущего символа в Ident
        a = S.charAt(Position);
        SItem.Ident.setLength(0);
        SItem.Ident.append(a);



        if (SetChar.indexOf(Character.toString(a)) != -1) {
// Идентификатор
            SItem.mode = TType.Ident;
        } else if (SetNum.indexOf(Character.toString(a)) != -1) {
            // Число
            SItem.mode = TType.Number;
        } else if (SetDiv.indexOf(Character.toString(a)) != -1) {
            if (a != ';') {
                SItem.mode = TType.Divider;
            } else {
                SItem.mode = TType.Part;
            }
            // Переход к следующей позиции
            Position++;
            // Выход
            return;
        } else {
            // Неизвестный символ
            SItem.mode = TType.None;
            Position++;
            return;
        }

        // Продолжение ввода
        do {

            // Переход к следующей позиции
            Position++;



            if ((Position < S.length()) && (SItem.mode.equals(TType.Number)
                    && ((S.charAt(Position) == '-')
                    || (S.charAt(Position) == '+'))
                    && S.toUpperCase().charAt(Position - 1) == 'E')) {

                SItem.Ident.append(S.charAt(Position));


            } else if ((Position >= S.length())
                    || (SetDiv.indexOf(S.charAt(Position)) != -1)) {
                // Окончание идентификатора или числа
                if (SItem.mode == TType.Number) {


                    try {


                        SItem.Number = Double.parseDouble(SItem.Ident.toString());
                    } catch (NumberFormatException e1) {
                        // Перехват исключения при ошибочном числе
                        SItem.Error = true;
                    }

                }

                //    Проверка, соответствует ли идентификатор функции

                String sFunc = SItem.Ident.toString().toLowerCase();

                if (hmPriority.containsKey(sFunc)) {

                    // Значит функция

                    SItem.mode = TType.Func;
                    SItem.Number = 0;
                    break;
                }



                // Выход
                SItem.Pos2 = Position - 1;
                return;

            } // Продолжение формирования идентификатора или числа
            else {
                SItem.Ident.append(S.charAt(Position));
            }




            // Бесконечный цикл
        } while (true);
    }

// Очистка польской записи
    private static void ClearPZ() {
        linkedlOutput.clear();
        linkedStack.clear();
        hmVar.clear();
    }

    private static void SetLength(ArrayList al, int newLength) {



        if (al.size() < newLength) {
            while (al.size() < newLength) {
                al.add(null);
            }
        } else {
            while (al.size() > newLength) {
                al.remove(al.size() - 1);
            }
        }




    }

    public static boolean SetData(String Name, double Data) {

        boolean res = false;
        // Метод
        for (TData dt : DataList) {
            if (Name.equalsIgnoreCase(dt.Name)) {
                dt.Data = Data;
                res = true;
                break;
            }
        }

        return res;
    }

    public static double GetData(String Name) {

        double res = 0.0;

        // Метод 
        for (TData dt : DataList) {
            if (Name.equalsIgnoreCase(dt.Name)) {
                res = dt.Data;
                break;
            }
        }

        return res;
    }

    private static void push(String c) {



        ArrayList<String> alDel = new ArrayList<String>();

        if (linkedStack.isEmpty()) {
            /* Если стек пуст,
             *то операция из входной строки переписывается в стек
             */
            linkedStack.add(c);
            return;
        }



        if (c.equals("(")) {

            /* Если очередной символ из входной строки
             *  есть открывающаяся скобка, то она проталкивается в стек
             */



            linkedStack.addFirst(c);


        } else if (c.equals(")")) {

            /* Закрывающая круглая скобка выталкивает все операции из стека до
             *ближайшей открывающей скобки, сами скобки в выходную строку не переписываются, а уничтожают
             *друг друга
             */
            int p = 0;
            for (int i = 0; i < linkedStack.size(); i++) {

                String d = linkedStack.get(i);

                linkedlOutput.add(d);

                if (d.equals("(")) {
                    p = i;
                    linkedlOutput.removeLast();
                    break;
                }
            }
            // удаляем все элементы до ближайшей открывающей скобки


            while (true) {

                String s = linkedStack.getFirst();

                if (s.equals("(")) {
                    linkedStack.removeFirst();
                    break;
                } else {
                    linkedStack.removeFirst();
                }


            }




        } else {



            // Проверяем приоритет

            int prior = hmPriority.get(c);



            String s = linkedStack.get(0);

            int prst = hmPriority.get(s);

            if (prst < prior) {

                /* Если в стеке верхняя операция  имеет более
                 * низкий  приоритет , то рассматриваемая операция просто
                 * проталкивается в стек
                 */

                linkedStack.addFirst(c);

            } else {


                /* Если в стеке верхняя операция имеет более высокий
                 * приоритет, то из стека выталкиваются в выходную строку все
                 * элементы, до тех пор,пока не встретится операция с приоритетом
                 * ниже чем у рассматриваемой, после чего она проталкивается
                 * в стек
                 */

                for (String opr : linkedStack) {

                    prst = hmPriority.get(opr);


                    //верхняя операция имеет более высокий приоритет или равный
                    if (prior <= prst) {

                        alDel.add(opr);

                        linkedlOutput.add(opr);
                    }

                }
                //  ниже чем у рассматриваемой

                // удаляем из стека

                for (String sd : alDel) {

                    linkedStack.remove(sd);


                }


                linkedStack.addFirst(c);

            }

        }




        // return al;
    }

    public static boolean CreatePZ(String S, boolean listErrorOff) {
// Формирование польской записи
        //     {Флаг завершения операций при конце формирования записи выражения:}
        boolean lend = false;
        boolean Assign = false; // Допустимость операции =
        int Adress; // Адрес пересылки результата
        TType OldMode; // Характер предыдущей лексемы
        char OldS; // Первый символ предыдущей лексемы
        TrStack.setLength(1);
        TrStack.setCharAt(0, '0');
        OldMode = TType.None;
        OldS = ' ';//Предыдущий символ
        Assign = true;
        Adress = 0;

        boolean result = false; // возвращаемый результат



        ClearPZ();


        // Чтение первой лексемы
        SyntItem(S, true, 0);

        if (SItem.mode == TType.All) {



            ErrorList.add("Не введено никакого текста");
            return false;
        }

        do {
            char ss = SItem.Ident.charAt(0);

            if ((OldMode == TType.Func) && (ss != '(')) {


                if (listErrorOff) {
                    return false;
                }

                ErrorList.add("Пропущена скобка после функции в позиции " + SItem.Pos1);


            }


            if (SItem.mode == TType.Number) {

                if (OldMode == TType.Divider && OldMode == TType.None && OldMode == TType.Part) {

                    if (listErrorOff) {
                        return false;
                    }

                    ErrorList.add("Ошибка в позициях " + String.valueOf(SItem.Pos1) + " должен быть разделитель");
                }
                if (SItem.Error) {

                    if (listErrorOff) {
                        return false;
                    }

                    ErrorList.add("Ошибка в позициях " + String.valueOf(SItem.Pos1) + " - " + String.valueOf(SItem.Pos2));
                } else {


                    // Добавляем в рез.строку
                    linkedlOutput.add(SItem.Number);

                }
                Assign = false;


            } else if (SItem.mode == TType.Ident) {

                {
                    if (OldMode == TType.Divider
                            && OldMode == TType.None
                            && OldMode == TType.Part) {

                        if (listErrorOff) {
                            return false;
                        }

                        ErrorList.add("В позиции " + SItem.Pos1
                                + " должен быть разделитель");
                    }

                    // Добавляем идентификатор

                    hmVar.put(SItem.Ident.toString(), null);


                    // Добавляем в рез.строку
                    linkedlOutput.add(SItem.Ident.toString());




                }

            } else if (SItem.mode == TType.All || SItem.mode == TType.Part) {


                // Конец выражения


                if (!linkedStack.isEmpty()) {
                    String s = linkedStack.getFirst();
                    linkedlOutput.addLast(s);
                }

                //   evaluateExp();



                if (ErrorList.isEmpty()) {
                    result = true;
                } else {
                    result = false;
                }

                if (SItem.mode == TType.All) {
                    return result;
                } else {
                    Assign = true;
                    SItem.mode = TType.None;
                }


            } else if (SItem.mode == TType.Divider) {

                char sc = SItem.Ident.charAt(0);

                push(SItem.Ident.toString());


                if (((OldMode.equals(TType.Divider))
                        && (sc != '=')
                        && (sc != '(')
                        && (sc != ')'))
                        && ((OldS != '=')
                        && (OldS != '(')
                        && (OldS != ')'))) {

                    if (listErrorOff) {
                        return false;
                    }

                    ErrorList.add("Позиция два подряд символа операции");
                    break;
                }
                Assign = false;

            } else if (SItem.mode == TType.Func) {


                push(SItem.Ident.toString());


                Assign = false;


            } else if (SItem.mode == TType.None) {


                if (listErrorOff) {
                    return false;
                }

                ErrorList.add("Непонятный символ в позиции " + SItem.Pos1);
            }




            OldMode = SItem.mode;
            OldS = SItem.Ident.charAt(0);
            SyntItem(S, false, 0);
        } while (true);


        if (ErrorList.isEmpty()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private static class TData {

        private String Name;
        private double Data;

        public TData(String Name, double Data) {
            this.Name = Name;
            this.Data = Data;
        }
    }

    // Тип записи с характеристиками прочитанной лексемы
    private static class SItem {

        static TType mode; // Тип лексемы
        static double Number; // Число, если лексема -
        // числовая константа
        static StringBuffer Ident = new StringBuffer(); // Идентификатор или символ операции
        static boolean Error; // Обнаружена ли ошибка
        static int Pos1;
        static int Pos2; // Позиции начала и конца
        // лексемы в тексте
    }

    // Тип записи с характеристиками прочитанной лексемы
    private static class TSynt {

        static TType mode; // Тип лексемы
        static double Number; // Число, если лексема -
        // числовая константа
        static StringBuffer Ident = new StringBuffer(); // Идентификатор или символ операции
        static boolean Error; // Обнаружена ли ошибка
        static int Pos1;
        static int Pos2; // Позиции начала и конца
        // лексемы в тексте
    }

    public static double getResultF() {
        return ResultF;
    }

    private static void proc1() {

        char c = SItem.Ident.charAt(0);


        //  linkedStack.addFirst(c);
        TrStack.setLength(High(TrStack) + 2);
        TrStack.setCharAt(High(TrStack), (SItem.Ident.charAt(0)));


    }

    private static void code() {

        // Запись в стек кода арифметической операции

        char s = TrStack.charAt(High(TrStack));


        if (s == '+') {

            PZ.add(-1);
        }



        switch (s) {
            case '+':
                PZ.add(-1);
                break;
            case '-':
                PZ.add(-2);
                break;
            case '*':
                PZ.add(-3);
                break;
            case '/':
                PZ.add(-4);
                break;
            case '^':
                PZ.add(-5);
                break;
            case 'M':
                PZ.add(-6);
                break;
        }
    }

    private static void proc2() {
        code();
        TrStack.setCharAt(High(TrStack), (SItem.Ident.charAt(0)));
    }

    private static boolean proc3() {
        code();
        TrStack.setLength(High(TrStack));
        return false;
    }

    private static void proc4() {
        TrStack.setLength(High(TrStack));
    }

    private static void proc5() {
// Аналог proc1 для функций
        char s = ' ';
        int kod = (int) SItem.Number;
        s = (char) (127 + kod);

        TrStack.append(s);


//TrStack.setCharAt(High(TrStack),chr(127+SItem.Number));
    }

    private static void proc6() {
// Аналог proc3 для функций
        char s = TrStack.charAt(High(TrStack));
        PZ.add(-s + 27);
        TrStack.setLength(High(TrStack));
    }
}

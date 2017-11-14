package servise;

import java.sql.Timestamp;

/*
 * Сравнение двух велечин
 * 
 */
/**
 *
 * @author 1
 */
public class CompareClass {

    private Timestamp ts;// Дата события
    private String Name;// Имя
    private Double value;// расчитаное значение
    private String znak;  // Знак сравнения
    private Double valueOver;// Уставка
    private Boolean result;  // результат сравнения

    public CompareClass(String names) {

        int poz = -1;
        String[] s;
        String sd;

        poz = names.indexOf("<>");

        if (poz != -1) {

            znak = "<>";
            s = names.split("<>");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {
                valueOver = null;
            }
            return;
        }

        poz = names.indexOf("<=");

        if (poz != -1) {

            znak = "<=";
            s = names.split("<=");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {

                valueOver = null;
            }
            return;
        }

        poz = names.indexOf(">=");

        if (poz != -1) {

            znak = ">=";
            s = names.split(">=");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {
                valueOver = null;
            }
            return;
        }


        poz = names.indexOf("=");

        if (poz != -1) {

            znak = "=";
            s = names.split("=");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {
                valueOver = null;
            }
            return;
        }


        poz = names.indexOf("<");

        if (poz != -1) {

            znak = "<";
            s = names.split("<");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {
                valueOver = null;
            }
            return;
        }

        poz = names.indexOf(">");

        if (poz != -1) {

            znak = ">";
            s = names.split(">");
            Name = s[0];
            sd = s[1].trim();

            try {
                valueOver = Double.parseDouble(sd);
            } catch (NumberFormatException e) {
                valueOver = null;
            }
            return;
        }



    }

    public Boolean isCompare() {

        result = isCompare(value, znak, valueOver);

        return result;
    }

    public static Boolean isCompare(Double v1, String z, Double vset1) {

        boolean r = false;

        if (v1 == null && vset1 == null) {

            return null;
        }

        double v = v1;
        double vset = vset1;

        if (z.equals("<>")) {

            r = (v != vset ? true : false);
        } else if (z.equals("<=")) {
            r = (v <= vset ? true : false);

        } else if (z.equals(">=")) {
            r = (v >= vset ? true : false);

        } else if (z.equals(">")) {
            r = (v > vset ? true : false);

        } else if (z.equals("<")) {
            r = (v < vset ? true : false);

        } else if (z.equals("=")) {
            r = (v == vset ? true : false);

        }

        return r;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getName() {
        return Name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getValueOver() {
        return valueOver;
    }

    public void setValueOver(Double valueOver) {
        this.valueOver = valueOver;
    }

    public String getZnak() {
        return znak;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }

    public void setZnak(String znak) {
        this.znak = znak;
    }

    public boolean isResult() {
        return result;
    }
}

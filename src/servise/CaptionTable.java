/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import dbf.Work;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author 1
 */
public class CaptionTable {

    String[] names;

    public CaptionTable(String nameTable) throws Exception {
        names = (String[]) Work.getParamTableByName(nameTable, Work.TABLE_ORDER_NAME);
    }

    public String getCaption(ResultSet rs) throws SQLException {

        String caption = "";

        for (String s : names) {

            String name = rs.getString(s.trim());

            if (name != null && !name.isEmpty()) {
                caption = caption + name + ", ";
            }

        }

        if (caption.length() > 3) {

            caption = caption.substring(0, caption.length() - 2);
        }



        return caption;
    }
}

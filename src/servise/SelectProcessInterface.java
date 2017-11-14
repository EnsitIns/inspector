/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import javax.swing.*;

/**
 *
 * @author 1
 */
public interface SelectProcessInterface {

    public static final int EVENTS_STOP = 0;
    public static final int EVENTS_START = 1;

    public int getEvent();

    public String getValuesName();

    public void setComboBox(boolean flag);

    public void setVisible(boolean flag);

    public void setEnabled(boolean flag);

    public int getCountIter();

    public void setImageStart();

    public boolean isCheckValue();

    public void setRejim(Integer rejim);

    public JComponent getComponent();

    public Integer getRejim();

    public String getSqlTable();
}

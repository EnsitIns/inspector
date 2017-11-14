package cominterface;

import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.Observer;

/**
 *
 * @author 1
 */
public interface CellInterface {

    JComponent getComponent(Point point);

    Object getValue();

    boolean isChange();// Изменялось ли значение

    void setArguments(Object args[]);

    void setValue(Object o);
    
    void setVisible(boolean bVisible);

    void setSaveIcon(boolean bSave);

    String getName();

    Element getElement();

    void setName(String name);

    void setElement(Element element);

    void setNotify(Object o);

    String getValueToString();

    void addObserver(Observer observer);
}

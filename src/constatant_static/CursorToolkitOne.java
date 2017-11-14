package constatant_static;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 1
 */

import javax.swing.*;
import java.awt.*;

interface Cursors {

    Cursor WAIT_CURSOR =
            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    Cursor DEFAULT_CURSOR =
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
}

/**
 * Basic CursorToolkit that still allows mouseclicks
 */
public class CursorToolkitOne implements Cursors {

    private CursorToolkitOne() {
    }

    /**
     * Sets cursor for specified component to Wait cursor
     */
    public static void startWaitCursor(JComponent component) {

        if (component == null) {
            return;
        }
        RootPaneContainer root =
                (RootPaneContainer) component.getTopLevelAncestor();
        root.getGlassPane().setCursor(WAIT_CURSOR);
        root.getGlassPane().setVisible(true);
    }

    /**
     * Sets cursor for specified component to normal cursor
     */
    public static void stopWaitCursor(JComponent component) {

        if (component == null) {
            return;
        }

        RootPaneContainer root =
                (RootPaneContainer) component.getTopLevelAncestor();
        root.getGlassPane().setCursor(DEFAULT_CURSOR);
        root.getGlassPane().setVisible(false);
    }
}

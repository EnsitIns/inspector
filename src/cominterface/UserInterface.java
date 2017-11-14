/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cominterface;

import java.sql.SQLException;

/**
 *
 * @author 1
 */
public interface UserInterface {

     void getUserParameter() throws SQLException;
     void setUserParameter() throws SQLException;
}

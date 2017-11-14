/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import connectdbf.SqlTask;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import script_test.ScriptTest;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author 1
 */
public class Script {

    public static HashMap<Object, String> hmScripts;
    public static Map helpval;
    public Script() {

    }

    public Object evalScript(int idScript, Map<String, Object> hmPapam) throws Exception {

        Object result;
        String codScript = null;
     
        if (hmScripts == null) {
            hmScripts = new HashMap<>();
        }

        if (hmScripts.containsKey(idScript)) {

            codScript = hmScripts.get(idScript);
        } else {

            String sql = "SELECT *  FROM groovy_scripts  WHERE c_tree_id=?";

            ResultSet resultSet = SqlTask.getResultSet(null, sql, new Object[]{idScript});

            try {
                if (resultSet.next()) {
                    codScript = resultSet.getString("groovy_cod");
                    hmScripts.put(idScript, codScript);
                } else {

                    throw new Exception("Скрипт с кодом- " + idScript + " не существует !");

                }

            } finally {
                resultSet.close();
            }
        }

        if (codScript == null || codScript.isEmpty()) {

            return null;
        }

        if (codScript.startsWith("test")) {

      
            ScriptTest scriptTest=new ScriptTest();
            result=scriptTest.evalScript(hmPapam, helpval);
            
            
        } else {

            Binding binding = new Binding();
            binding.setVariable("values", hmPapam);
            binding.setVariable("helpval", helpval);

            GroovyShell shell;
            shell = new GroovyShell(binding);
            result = shell.evaluate(codScript);

        }

       // if (result instanceof Map) {
          //  Map map = (Map) result;
         // if (map.containsKey("helpval")) {
          //      helpval = map.get("helpval");
            //    map.remove("helpval");
           // }
        //}

        return result;

    }

}

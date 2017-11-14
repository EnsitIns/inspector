/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

/**
 *
 * @author 1
 */
 public class User {
    String password;
    String name;
    String acces;
    Integer id;
    String limit;
    Integer idx;  
    
    public User(Integer id, String name, String password, String acces, String limit) {

        this.id = id;
        this.acces = acces;
        this.name = name;
        this.password = password;
        this.limit = limit;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAcces(String acces) {
        this.acces = acces;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getAcces() {
        return acces;
    }

    public Integer getId() {
        return id;
    }

    public String getLimit() {
        return limit;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

   
}

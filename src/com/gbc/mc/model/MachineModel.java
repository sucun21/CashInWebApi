/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gbc.mc.model;

import com.gbc.mc.common.AppConst;
import com.gbc.mc.database.MySqlFactory;
import com.gbc.mc.data.Machine;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author tamvh
 */
public class MachineModel {
    private static MachineModel _instance = null;
    private static final Lock createLock_ = new ReentrantLock();
    protected static final Logger logger = Logger.getLogger(MachineModel.class);
    private static final Gson gson = new Gson();
    final String tableName = "tb_machine";

    public static MachineModel getInstance() throws IOException {
        if (_instance == null) {
            createLock_.lock();
            try {
                if (_instance == null) {
                    _instance = new MachineModel();
                }
            } finally {
                createLock_.unlock();
            }
        }
        return _instance;
    }
    
    public int getListMachine(List<Machine> listMac) {
        int ret =AppConst.ERROR_GENERIC;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String queryStr;
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            Machine mac;
            queryStr = String.format("SELECT `machine_name`, "
                    + " `address` "
                    + " ,`time_update` "
                    + " ,`action_type` "
                    + "FROM tb_machine"
                    + " ORDER BY `machine_name` DESC",
                    tableName);
            System.out.println("Query getListMachine: " + queryStr);
            stmt.execute(queryStr);
            rs = stmt.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                    mac=new Machine();
                    mac.setMachine_name(rs.getString("machine_name"));
                    mac.setAddress(rs.getString("address"));
                    mac.setTime_update(rs.getString("time_update"));
                    mac.setAction_type(rs.getString("action_type"));
                    listMac.add(mac);
                }
                ret=0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(MachineModel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    public int updateMachineInfo(String address,String action_type,String machine_name){
        int ret=AppConst.ERROR_GENERIC;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        String queryStr;
        try{
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            queryStr = String.format(" UPDATE %1$s " 
                                + " SET address= '%2$s' , " 
                                + "	action_type= '%3$s', " 
                                + "     time_update=sysdate() " 
                                + " WHERE machine_name= '%4$s' ",
                    tableName,address,action_type,machine_name);
           logger.info("Query updateMachineInfo: " + queryStr);
            int result = stmt.executeUpdate(queryStr);
            if (result > 0) {
                ret = 0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(MachineModel.class.getName()).log(Level.SEVERE, null, ex);
            ret = -1;
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    public int insertMachineInfo(Machine machine){
        int ret=AppConst.ERROR_GENERIC;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        String queryStr;
        try{
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            queryStr = String.format(" Insert Into %1$s " 
                                + " Values( " 
                                + "'%2$s', "
                                + "'%3$s', " 
                                + "sysdate(), " 
                                + "'%4$s' ) ",
                    tableName,machine.getMachine_name(),machine.getAddress(),machine.getAction_type());
           logger.info("Query updateMachineInfo: " + queryStr);
            int result = stmt.executeUpdate(queryStr);
            if (result > 0) {
                ret = 0;
            }
            if(ret==0){
                queryStr = String.format("SELECT `machine_name`, "
                    + " `address` "
                    + " ,`time_update` "
                    + " ,`action_type` "
                    + "FROM tb_machine "
                    + "WHERE `machine_name` = '%1$s' ORDER BY `machine_name` DESC LIMIT 0,1", machine.getMachine_name());
                if (stmt.execute(queryStr)) {
                    rs = stmt.getResultSet();
                    if (rs != null) {
                         if (rs.next()) {
                            machine.setMachine_name(rs.getString("machine_name"));
                            machine.setTime_update(rs.getString("time_update"));
                            machine.setAction_type(rs.getString("action_type"));
                            machine.setAddress(rs.getString("address"));
                            logger.info("get new machine success");
                         } 
                    }
                }
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(MachineModel.class.getName()).log(Level.SEVERE, null, ex);
            ret = -1;
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
     public int deleteMachineByListId(JsonArray arrItemIDDel) throws IOException {
        int ret = -1;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String queryStr;
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            queryStr = String.format("DELETE FROM %1$s WHERE `machine_name` IN (%2$s)",tableName, getWhereClauseDelete(arrItemIDDel));
            System.out.println("Query deleteCabinetById: " + queryStr);
            int result = stmt.executeUpdate(queryStr);
            if(result > 0) {
                ret = 0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(MachineModel.class.getName()).log(Level.SEVERE, null, ex);
            ret = -1;
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    public String getWhereClauseDelete(JsonArray a){
        StringBuilder result = new StringBuilder();
        String s = ",";
        for(int i = 0; i < a.size(); i++){
            if(i < (a.size() - 1)){
                result.append(String.format("'%s' %s", a.get(i).getAsString(), s));
            } else 
                result.append(String.format("'%s'",a.get(i).getAsString()));
        }        
        return result.toString();
    }
    
}

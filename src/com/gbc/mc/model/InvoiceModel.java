/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gbc.mc.model;

import com.gbc.mc.database.MySqlFactory;
import com.gbc.mc.data.Summary;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
public class InvoiceModel {
    private static InvoiceModel _instance = null;
    private static final Lock createLock_ = new ReentrantLock();
    protected static final Logger logger = Logger.getLogger(InvoiceModel.class);
    private static final Gson gson = new Gson();

    public static InvoiceModel getInstance() throws IOException {
        if (_instance == null) {
            createLock_.lock();
            try {
                if (_instance == null) {
                    _instance = new InvoiceModel();
                }
            } finally {
                createLock_.unlock();
            }
        }
        return _instance;
    }
    
    public int getListInvoice(List<JsonObject> list_invoice,int fromRecord,int total_item_per_page,String from_date,String to_date,String status,List<Integer> lengthList) {
        int ret = -1;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String queryStr;
            String condition;
            String tableName1 = "tb_invoice";
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            logger.info("from_date"+from_date);
            logger.info("to_date"+to_date);
            if(status.equals("-10")){
                status="%%";
                condition= "AND status like ";
            }else{
                condition= "AND status = ";
            }
            queryStr = String.format("SELECT "
                    + "(SELECT COUNT(*) AS `total` "
                    + " FROM %1$s "         
                    + " WHERE `date_order` BETWEEN '%2$s' AND '%3$s'"
                    +condition+"'%4$s' ) AS total ", tableName1 ,from_date, to_date,status);
            logger.info("Query getTotalInvoice: " + queryStr);
            stmt.execute(queryStr);
            rs = stmt.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                    lengthList.add(rs.getInt("total"));
                }
                MySqlFactory.safeClose(rs);
            }
            queryStr = String.format("SELECT `invoice_code`,"
            + "`invoice_index`,"
            + "`reciever`,"
            + "`transfer_type`,"
            + "`zptransid`,"
            + "`reciever` ,"
            + "`machine_name`,"
            + "`date_order`,"
            + "`amount`,"
            + " Case status"
                    + " when 1 then 'Thành công'"
                    + " when 10 then 'Đang xử lý'"
                    + " else 'Thất bại'"
            + " end as `status`"
            + " FROM %1$s iv "
            + " WHERE `date_order` BETWEEN '%2$s' AND '%3$s' "
            + condition+" '%4$s' "
            + " ORDER BY invoice_code DESC LIMIT %5$d,%6$d",
            tableName1,from_date,to_date,status,fromRecord, total_item_per_page);
            logger.info("Query getListInvoice: " + queryStr);
            stmt.execute(queryStr);
            rs = stmt.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                  JsonObject invoice=new JsonObject();
                  invoice.addProperty("invoice_code", rs.getString("invoice_code"));
                  invoice.addProperty("invoice_index", rs.getString("invoice_index"));
                  invoice.addProperty("zptransid", rs.getString("zptransid"));
                  invoice.addProperty("reciever", rs.getString("reciever"));
                  invoice.addProperty("machine_name", rs.getString("machine_name"));
                  invoice.addProperty("date_order", rs.getString("date_order"));
                  invoice.addProperty("amount", rs.getString("amount"));
                  invoice.addProperty("status", rs.getString("status"));
                  list_invoice.add(invoice);
                }
                ret=0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(InvoiceModel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    
    public int getChartInvoice(List<JsonObject> chartInvoices,String from_date,String to_date) {
        int ret = -1;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String queryStr;
            String tableName1 = "tb_invoice";
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            logger.info("from_date"+from_date);
            logger.info("to_date"+to_date);
            queryStr = String.format("SELECT `date_order`,Sum(amount) `total_amount`"
            + " FROM %1$s "
            + " WHERE `date_order` BETWEEN '%2$s' AND '%3$s' "
            + " AND status =1"
            + " GROUP BY date_order ",
            tableName1,from_date,to_date);
            logger.info("Query getListInvoice: " + queryStr);
            stmt.execute(queryStr);
            rs = stmt.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                  JsonObject invoice=new JsonObject();
                  invoice.addProperty("totalAmount", rs.getString("total_amount"));
                  invoice.addProperty("date", rs.getString("date_order"));
                  chartInvoices.add(invoice);
                }
                ret=0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(InvoiceModel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    
     public int getSummaryInvoice(String date,Summary summary ) {
        int ret = -1;
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        int total_payer=0;
        int total_amount=0;
        int total_invoice=0;
        try {
            String queryStr;
            String tableName1 = "tb_invoice";
            connection = MySqlFactory.getConnection();
            stmt = connection.createStatement();
            logger.info("date"+date);
            queryStr = String.format("SELECT sum(amount) as `amount`,"
                    + "count(*) `quantity` "
                    + " FROM %1$s " 
                    + "where date_order='%2$s'  "
                    + "and status=1 " 
                    +  "group by reciever",tableName1,date);
            logger.info("Query getSummaryInvoice: " + queryStr);
            stmt.execute(queryStr);
            rs = stmt.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                  total_payer++;
                  total_amount=total_amount + rs.getInt("amount");
                  total_invoice=total_invoice + rs.getInt("quantity");
                }
                summary.setTotal_amount(total_amount);
                summary.setTotal_invoice(total_invoice);
                summary.setTotal_payer(total_payer);
                ret=0;
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(InvoiceModel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySqlFactory.safeClose(rs);
            MySqlFactory.safeClose(stmt);
            MySqlFactory.safeClose(connection);
        }
        return ret;
    }
    
    
}

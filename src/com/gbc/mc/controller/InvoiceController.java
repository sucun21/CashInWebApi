/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gbc.mc.controller;

import com.gbc.mc.common.AppConst;
import com.gbc.mc.common.CommonModel;
import com.gbc.mc.common.JsonParserUtil;
import com.gbc.mc.data.Summary;
import com.gbc.mc.model.InvoiceModel;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author su
 */
public class InvoiceController extends HttpServlet{
    protected final Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp); //To change body of generated methods, choose Tools | Templates.
    }
    
     private void handle(HttpServletRequest req, HttpServletResponse resp) {
        try {
            processs(req, resp);
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".handle: " + ex.getMessage(), ex);
        }
    }

    private void processs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = (req.getPathInfo() != null) ? req.getPathInfo() : "";
        String cmd = req.getParameter("cm") != null ? req.getParameter("cm") : "";
        String data = req.getParameter("dt") != null ? req.getParameter("dt") : "";
        String content = "";
        logger.info("InvoiceController.processs, pathInfo:   " + pathInfo);
        logger.info("InvoiceController.processs, cmd:        " + cmd);
        logger.info("InvoiceController.processs, data:       " + data);
        CommonModel.prepareHeader(resp, CommonModel.HEADER_JS);
        switch (cmd) {            
            case "get_list_invoice":
                content = getListInvoice(req, data);
                break;
            case "get_summamy_invoice":
                content = getSummaryInvoice(req, data);
                break;
             case "get_chart_invoice":
                content = getChartInvoice(req, data);
                break;    
            
        }
        CommonModel.out(content, resp);
    }
    private String getListInvoice(HttpServletRequest req, String data) {
        String content;
        String fromDate = "";
        String toDate = "";
        String status="";
        int ret = AppConst.ERROR_GENERIC;
        List<Integer> lengthList = null;
        List<JsonObject> list_invoice=null;
        int fromRecord=0;
        int currentPage = 0;
        int total_item_per_page = 0;
        int length = 0;
        
        JsonObject jdata=null;
        
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {
                if(jsonObject.has("current_page")){
                    currentPage = jsonObject.get("current_page").getAsInt();
                }  
                if(jsonObject.has("total_item_per_page")){
                    total_item_per_page = jsonObject.get("total_item_per_page").getAsInt();
                } 
                if(jsonObject.has("from_date")){
                    fromDate = jsonObject.get("from_date").getAsString();
                } 
                if(jsonObject.has("to_date")){
                    toDate = jsonObject.get("to_date").getAsString();
                } 
                if(jsonObject.has("status")){
                    status = jsonObject.get("status").getAsString();
                }
                if(currentPage > 0 && total_item_per_page > 0 && !fromDate.isEmpty() && !toDate.isEmpty()){  
                    fromRecord = currentPage*total_item_per_page - total_item_per_page;   
                }
                list_invoice= new ArrayList<>();
                lengthList = new ArrayList<>(); 
                ret = InvoiceModel.getInstance().getListInvoice(list_invoice, fromRecord, total_item_per_page, fromDate, toDate,status, lengthList);
                length = lengthList.get(0);
            }    
            if (ret == 0) {
                jdata = new JsonObject();
                jdata.addProperty("length", length);
                jdata.addProperty("list_invoice", list_invoice.toString());
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox success", jdata);
            } else {
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox failed");
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".insertAccount: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }

        return content;
    }
    
    private String getChartInvoice(HttpServletRequest req, String data) {
        String content;
        String fromDate = "";
        String toDate = "";
        int ret = AppConst.ERROR_GENERIC;
        List<JsonObject> chartInvoices=null;
        
        JsonObject jdata=null;
        
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {
                if(jsonObject.has("from_date")){
                    fromDate = jsonObject.get("from_date").getAsString();
                } 
                if(jsonObject.has("to_date")){
                    toDate = jsonObject.get("to_date").getAsString();
                } 
                chartInvoices= new ArrayList<>();
                ret = InvoiceModel.getInstance().getChartInvoice(chartInvoices, fromDate, toDate);
            }    
            if (ret == 0) {
                jdata = new JsonObject();
                jdata.addProperty("chartInvoices", chartInvoices.toString());
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox success", jdata);
            } else {
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox failed");
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".getChartInvoice: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }

        return content;
    }
    
    public String getSummaryInvoice(HttpServletRequest req,String data){
        String content;
        String date = "";
        Summary summary=new Summary();
        
        int ret = AppConst.ERROR_GENERIC;
        
        JsonObject jdata=null;
        
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {
                if(jsonObject.has("date")){
                    date = jsonObject.get("date").getAsString();
                    logger.info(date);
                    
                }
                ret = InvoiceModel.getInstance().getSummaryInvoice(date,summary);
            }
            if (ret == 0) {
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox success", summary);
            } else {
                content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get history openbox failed");
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".insertAccount: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }

        return content;
    }
}

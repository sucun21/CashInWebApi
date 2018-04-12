/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gbc.mc.controller;

import com.gbc.mc.common.AppConst;
import com.gbc.mc.common.CommonModel;
import com.gbc.mc.common.JsonParserUtil;
import com.gbc.mc.data.Machine;
import com.gbc.mc.model.MachineModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
public class MachineController extends HttpServlet{
     protected final Logger logger = Logger.getLogger(this.getClass());
     private static final Gson _gson = new Gson();
     
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req,resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req,resp);
    }
    
    private void handle(HttpServletRequest req, HttpServletResponse resp){
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
        logger.info("MachineController.processs, pathInfo:   " + pathInfo);
        logger.info("MachineController.processs, cmd:        " + cmd);
        logger.info("MachineController.processs, data:       " + data);
        CommonModel.prepareHeader(resp, CommonModel.HEADER_JS);
        switch (cmd) {            
            case "get_list_machine":
                content = getListMachine(req, data);
                break;
            case "edit_machine":
                content=updateMachineInfo(req,data);
                break;
            case "delete_machine":
                content=deteteMachine(req,data);
                break;
            case "insert_machine":
                content=insertMachine(req,data);
                break;
        }
        CommonModel.out(content, resp);
    }
    
    private String getListMachine(HttpServletRequest req,String data){
        String content;
        int ret = AppConst.ERROR_GENERIC;
        try {
            List<Machine> listMac=new ArrayList<>();
            ret = MachineModel.getInstance().getListMachine(listMac);
            switch (ret) {  
                case 0:
                    content = CommonModel.FormatResponse(AppConst.NO_ERROR, "get list machine success",listMac);
                    break;
                default:
                    content = CommonModel.FormatResponse(AppConst.ERROR_GENERIC, "get list machine failed");
                    break;
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".getListMachine: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }
        return content;
    }

    private String updateMachineInfo(HttpServletRequest req,String data){
        String content;
        String address;
        String machine_name;
        String action_type;
        int ret = AppConst.ERROR_GENERIC;
        
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {
                JsonObject mc=jsonObject.get("machine").getAsJsonObject();
                action_type=mc.get("action_type").getAsString();
                machine_name=mc.get("machine_name").getAsString();
                address=mc.get("address").getAsString();
                if (machine_name==null) {
                    content = CommonModel.FormatResponse(ret, "Invalid parameter");
                } else {
                    ret = MachineModel.getInstance().updateMachineInfo(address,action_type,machine_name);
                    if (ret == 0) {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "edit machine info success", machine_name);
                    } else {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "edit machine info success failed");
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".updateMachineInfo: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }
        return content;
    }
    
   private String deteteMachine(HttpServletRequest req, String data) {
        String content;
        int ret = AppConst.ERROR_GENERIC;
        logger.info("delete");
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {                
                JsonArray arrItemIDDel = null;
                if(jsonObject.has("list_item_id_del")){
                    JsonElement ele = jsonObject.get("list_item_id_del");
                    if (ele.isJsonArray()) {
                        arrItemIDDel = ele.getAsJsonArray();                        
                    }                    
                }   
                if (arrItemIDDel.size()<= 0) {
                    content = CommonModel.FormatResponse(ret, "Invalid parameter");
                } else {
                    ret = MachineModel.getInstance().deleteMachineByListId(arrItemIDDel);
                    if(ret == 0) {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "delete cabinet success");
                    } else {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "delete cabinet failed");
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".deleteCabinet: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }
        return content;
    }
    
    private String insertMachine(HttpServletRequest req,String data){
        String content;
        int ret = AppConst.ERROR_GENERIC;
        Machine machine;
        try {
            JsonObject jsonObject = JsonParserUtil.parseJsonObject(data);
            if (jsonObject == null) {
                content = CommonModel.FormatResponse(ret, "Invalid parameter");
            } else {
                machine = _gson.fromJson(jsonObject.get("machine").getAsJsonObject(), Machine.class);
                if (machine==null) {
                    content = CommonModel.FormatResponse(ret, "Invalid parameter");
                } else {
                    ret = MachineModel.getInstance().insertMachineInfo(machine);
                    if (ret == 0) {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "edit machine info success", machine);
                    } else {
                        content = CommonModel.FormatResponse(AppConst.NO_ERROR, "edit machine info success failed");
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(getClass().getSimpleName() + ".updateMachineInfo: " + ex.getMessage(), ex);
            content = CommonModel.FormatResponse(ret, ex.getMessage());
        }
        return content;
    }
}

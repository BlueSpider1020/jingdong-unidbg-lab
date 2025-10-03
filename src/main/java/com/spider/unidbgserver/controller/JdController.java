package com.spider.unidbgserver.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.spider.unidbgserver.service.JdService;
import com.spider.unidbgserver.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.Console;
import java.io.IOException;

@RestController
@RequestMapping("/jd")
public class JdController{

    @Autowired
    JdService jdService;

    @RequestMapping("/sign")
    public R getSign(String functionId, String body){
        if (StrUtil.isEmpty(functionId) || StrUtil.isEmpty(body)){
            return R.fail(-1, "参数异常！");
        }
        try {
            JSONObject jsonObject = JSONUtil.parseObj(body);
        }catch (Exception e){
            System.out.println(e);
            return R.fail(-1, "body参数异常！");
        }
//        String uuid = StrUtil.uuid();
        String uuid = "d7d88e68f3599478";
        String sign = jdService.getSign(functionId, uuid, body);
        String res = "&clientVersion=15.1.55&build=100987&client=android&partner=baidu&sdkVersion=30&lang=zh_CN&networkType=wifi&&uuid=" + uuid + "&" + sign;
        return R.success("成功！", res);
    }

    @RequestMapping("/getLoginBody")
    public R getLoginBody(HttpServletRequest request){
        try {
            byte[] data = request.getInputStream().readAllBytes();

            if (data.length == 0) {
                return R.fail(-1, "Empty byte array");
            }

            String result = jdService.getLoginBody(data);

            return R.success("Byte array processed", result);

        } catch (IOException e) {
            e.printStackTrace();
            return R.fail(-1, "IO error: " + e.getMessage());
        }
    }

    @RequestMapping("/getJniDecryptMsg")
    public R getJniDecryptMsg(HttpServletRequest request){
        try {
            byte[] data = request.getInputStream().readAllBytes();

            if (data.length == 0) {
                return R.fail(-1, "Empty byte array");
            }

            String result = jdService.getJniDecryptMsg(data);

            return R.success("Byte array processed", result);

        } catch (IOException e) {
            e.printStackTrace();
            return R.fail(-1, "IO error: " + e.getMessage());
        }
    }

    @RequestMapping("/getJdsgParams")
    public R getJdsgParams(String uri, String param, String eid, String version, String unknown){
        if (StrUtil.isEmpty(uri) || StrUtil.isEmpty(param) || StrUtil.isEmpty(eid) || StrUtil.isEmpty(version) || StrUtil.isEmpty(unknown)){
            return R.fail(-1, "Parameter should not be empty！");
        }
        System.err.println("Uri:" + uri);
        System.err.println("Param:" + param);
        String jdgs = jdService.getJdsgParams(uri, param, eid, version, unknown);
        return R.success("Success！", jdgs);
    }
}

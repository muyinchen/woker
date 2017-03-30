package com.iot.learnssm.firstssm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * Created by brian on 2016/3/8.
 */
@Controller
public class LoginController {
    // 登陆
    @RequestMapping("/login")
    public String login(HttpSession session, String username, String password)
            throws Exception {

        // 调用service进行用户身份验证
        // ...

        // 在session中保存用户身份信息
        session.setAttribute("username", username);
        // 重定向到商品列表页面
        return "redirect:/items/queryItems.action";
    }

    // 退出
    @RequestMapping("/logout")
    public String logout(HttpSession session) throws Exception {

        // 清除session
        session.invalidate();

        // 重定向到商品列表页面
        return "redirect:/items/queryItems.action";
    }
}

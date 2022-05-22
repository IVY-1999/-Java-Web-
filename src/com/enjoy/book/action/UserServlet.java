package com.enjoy.book.action;

import com.enjoy.book.bean.User;
import com.enjoy.book.biz.UserBiz;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

//配置Servlet的虚拟地址
@WebServlet("/user.let")
public class UserServlet extends HttpServlet {
    //构建UserBiz的对象
    UserBiz userBiz = new UserBiz();
    //处理get类型的请求
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

//    处理post类型的请求

    /*
        /user.let?type = login  登录     ---- /:项目的根目录（web文件夹 --- login.html）
        /user.let?type = exit   安全退出
        /user.let?type = modifyPwd  修改密码
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //请求和响应的编码格式
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();

//         1.判读用户请求的类型为login
        String method = req.getParameter("type");
        switch (method){
//            用户登录
            case "login":
//          2.从login.html中获取用户名和密码,以及验证码
                String name = req.getParameter("name");
                String pwd = req.getParameter("pwd");
                String userCode = req.getParameter("valcode"); //用户填写的验证码

                // 2.1 提取session中的验证码
                String code = session.getAttribute("code").toString(); //与ValCodeServlet中地session一致
                // 2.2 进行判断(不区分大小写）
                if(!code.equalsIgnoreCase(userCode)){
                    out.println("<script>alert('验证码输入错误');location.href = 'login.html';</script>");
                    return;
                }

//         3.调用UserBiz的getUser方法，根据用户名和密码获取对应的用户对象
                User user = userBiz.getUser(name,pwd);
//         4.判断用户对象是否为null：
                if(user == null){
                    //4.1 如果是null，表示用户名或密码不正确，提示错误，回到登录界面
                    out.println("<script>alert('用户名或密码不存在');location.href = 'login.html';</script>");
                }else{
                    //4.2 非空，表示登陆成功，将用户对象保存到session中，提示登陆成功，并页面跳转到index.jsp
                    session.setAttribute("user",user);
                    out.println("<script>alert('登录成功');location.href='index.jsp';</script>");
                }
                break;

//                用户安全退出
            case "exit":
                // 验证用户是否登录
                if (session.getAttribute("user") == null){
                    out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
                    return;
                }
                // 1。清除session
                session.invalidate();
                // 2.跳转到login。html(框架中回去） top.jsp->parent->index.jsp
                out.println("<script>parent.window.location.href='login.html';</script>");
                break;

//                用户修改密码
            case "modifyPwd":
                // 验证用户是否登录
                if (session.getAttribute("user")==null){
                    out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
                    return;
                }
                // 1.获取用户输入的新密码
                String newPwd = req.getParameter("newpwd");
                // 2.获取用户的编号
               long id = ((User)session.getAttribute("user")).getId();
                // 3.调用biz层的方法
                int count = userBiz.modifyPwd(id,newPwd);
                // 4。响应客户端 --- 参考exit
                if(count > 0){
                    out.println("<script>alert('密码修改成功');parent.window.location.href='login.html';</script>");
                }else {
                    out.println("<script>alert('密码修改失败');</script>");
                }
                break;
        }

    }

}

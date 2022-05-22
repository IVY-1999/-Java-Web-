package com.enjoy.book.action;

import com.enjoy.book.bean.*;
import com.enjoy.book.biz.TypeBiz;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet("/type.let")
public class TypeServlet extends HttpServlet {
    TypeBiz typeBiz = new TypeBiz();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * /type.let?type=add ---- 类型的添加(表单）
     * /type.let?type=modifypre ---- 修改的预备（保存到req->转发->type_modify.jsp
     * /type.let?type=modify ---- 类型的修改（表单）
     * /type.let?type=remove&id=xx ---- 类型的删除（获取删除的类型编号）
     * /type_list.jsp ---- 类型的查询（所有的类型数据：项目运行时（监听器），数据读到application）

     * servlet的数据作用域：
     * request:同一个请求中传输信息
     * session:同一个会话（用户）
     * application:同一个运行项目
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 1.设置编码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");
        // 2.获取各种对象
        PrintWriter out = resp.getWriter();

        // 验证用户是否登录
        HttpSession session = req.getSession();
        if (session.getAttribute("user")==null){
            out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
            return;
        }

        ServletContext application = req.getServletContext();
        // 3.根据用户的需求完成业务
        String type = req.getParameter("type");
        switch (type){
            case "add":
                add(req,resp,out,application);
                break;
            case "modifypre":
                modifyPre(req,resp,out,application);
                break;
            case "modify":
                modify(req,resp,out,application);
                break;
            case "remove":
                remove(req,resp,out,application);
                break;
        }
    }

    /**
     * 用户进入到 type_add.jsp ---> type.let?type=add.jsp ---> ok ---> type_list.jsp
     *                                                    ---> no ---> type_add.jsp
     * @param out
     * @param application
     */
    private void add(HttpServletRequest req, HttpServletResponse resp,PrintWriter out, ServletContext application) {
        // 1.从表单中获取名字和父类型
        String typeName = req.getParameter("typeName");
        long parentId = Long.parseLong(req.getParameter("parentType"));
        // 2.调用biz的添加方法
        int count = typeBiz.add(typeName,parentId);
        // 3.更新application中的types
        // 4.提示结果
        if(count > 0){
            List<Type> types = typeBiz.getAll();
            application.setAttribute("types",types);
            out.println("<script>alert('添加成功');location.href='type_list.jsp';</script>");
        }else {
            out.println("<script>alert('添加失败');location.href='type_add.jsp';</script>");
        }
    }

    private void remove(HttpServletRequest req, HttpServletResponse resp,PrintWriter out, ServletContext application) {
        // 1.获取需要删除的id
        long id = Long.parseLong(req.getParameter("id"));
        // 2.调用方法biz
        // 3.更新application
        // 4.提示结果
        try {
            int count = typeBiz.remove(id);
            if(count >0){
                List<Type> types = typeBiz.getAll();
                application.setAttribute("types",types);
                out.println("<script>alert('删除成功');location.href='type_list.jsp';</script>");
            }else {
                out.println("<script>alert('删除失败');location.href='type_list.jsp';</script>");
            }
        } catch (Exception e) {
//            e.printStackTrace();
            out.println("<script>alert('"+e.getMessage()+"');location.href='type_list.jsp';</script>");
        }

    }

    private void modifyPre(HttpServletRequest req, HttpServletResponse resp,PrintWriter out, ServletContext application) throws ServletException, IOException {
         // 1.获取需要修改的type对象的id
        long id = Long.parseLong(req.getParameter("id"));
        // 2.根据id获取type对象
        Type type = typeBiz.getById(id);
        // 3.把type存到req对象中
        req.setAttribute("type",type);
        req.getRequestDispatcher("type_modify.jsp").forward(req,resp); // 页面的转发


    }

    /**
     * type_modify.jsp ---> type.let?type=modify ---> type_list.jsp
     * @param req
     * @param resp
     * @param out
     * @param application
     */
    private void modify(HttpServletRequest req, HttpServletResponse resp,PrintWriter out, ServletContext application) {
        // 1.获取表单中的数据（id:hidden,name,parentId)
        long id = Long.parseLong(req.getParameter("typeId"));
        String name = req.getParameter("typeName");
        long parentId = Long.parseLong(req.getParameter("parentType"));
        // 2.调用biz的修改方法
        int count = typeBiz.modify(id,name,parentId);
        // 3.更新application
        // 4.提示结果
        if(count > 0){
            List<Type> types = typeBiz.getAll();
            application.setAttribute("types",types);
            out.println("<script>alert('修改成功');location.href='type_list.jsp';</script>");
        }else {
            out.println("<script>alert('修改失败');location.href='type_list.jsp';</script>");
        }

    }




}

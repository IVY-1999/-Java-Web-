package com.enjoy.book.action;

import com.alibaba.fastjson.JSON;
import com.enjoy.book.util.DateHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import com.enjoy.book.bean.*;
import com.enjoy.book.biz.*;

@WebServlet("/book.let")
public class BookServlet extends HttpServlet {

    BookBiz bookBiz = new BookBiz();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * /book.let?type=add 添加图书
     * /book.let?type=modifypre&id=xx 修改前准备
     * /book.let?type=modify        修改
     * /book.let?type=remove&id=xx    删除
     * /book.let?type=query&pageIndex=1 :分页查询(request:转发)
     * /book.let?type=details&id=xx   展示书籍详细信息
     * /book.let?type=doajax&name=xx  使用ajax查询图书名对应的图书信息
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();

        // 验证用户是否登录
        HttpSession session = req.getSession();
        if (session.getAttribute("user")==null){
            out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
            return;
        }

        String type = req.getParameter("type");
        switch (type){
            case "add":
                try {
                    add(req,resp,out);
                } catch (FileUploadException e) {
                    e.printStackTrace();
                    resp.sendError(500,"文件上传失败");
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(500,e.getMessage());
                }
                break;
            case "modifypre":
                ///book.let?type=modifypre&id=xx
                long bookId = Long.parseLong(req.getParameter("id"));
                Book book = bookBiz.getById(bookId);
                req.setAttribute("book",book);
                req.getRequestDispatcher("book_modify.jsp").forward(req,resp);
                break;
            case "modify":
                try {
                    modify(req,resp,out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "remove":
                // 1.获取删除的书籍id
                long removeId = Long.parseLong(req.getParameter("id"));
                // 2.调用biz层的删除方法
                // 3.提示＋跳转 = out（跳转到查询的servlet)
                try {
                    int count = bookBiz.remove(removeId);
                    if (count>0){
                        out.println("<script>alert('图书删除成功');location.href='book.let?type=query&pageIndex=1';</script>");
                    }else {
                        out.println("<script>alert('图书删除失败');location.href='book.let?type=query&pageIndex=1';</script>");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("<script>alert('"+e.getMessage()+"');location.href='book.let?type=query&pageIndex=1';</script>");
                }
                break;
            case "query":
                query(req,resp,out);
                break;
            case "details":
                details(req,resp,out);
                break;
            case "doajax":
                String name = req.getParameter("name");
                Book book1 = bookBiz.getByName(name);
                if (book1 == null){
                    out.print("{}");  // 若书名不存在，则返回null json对象
                }else{
                    out.print(JSON.toJSONString(book1));
                }

                break;
            default:
                resp.sendError(404);
        }
    }

    /**
     * 修改图书信息
     * @param req
     * @param resp
     * @param out
     */
    private void modify(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        // 1.构建一个磁盘工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 2.设置磁盘工厂的大小
        factory.setSizeThreshold(1024 * 9);
        // 3.构建临时仓库
        File file = new File("d:\\temp");
        // 如果文件夹不存在，则创建一个
        if (!file.exists()) {
            file.mkdir();
        }
        factory.setRepository(file);

        // 4.文件上传 + 表单数据
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        // 5.将用户的请求解析成一个个FileItem(文件+表单元素）
        List<FileItem> fileItems = fileUpload.parseRequest(req);
        // 6.遍历FileItem
        Book book = new Book();
        for (FileItem item : fileItems) {
            if (item.isFormField()) {
                // 获取表单的元素名称和用户填写的值  name:文城
                String name = item.getFieldName();
                String value = item.getString("utf-8"); //防止乱码
                switch (name) {
                    case "id":
                        book.setId(Long.parseLong(value));
                        break;
                    case "pic":
                        book.setPic(value);
                        break;
                    case "typeId":
                        book.setTypeId(Long.parseLong(value));
                        break;
                    case "name":
                        book.setName(value);
                        break;
                    case "price":
                        book.setPrice(Double.parseDouble(value));
                        break;
                    case "desc":
                        book.setDesc(value);
                        break;
                    case "publish":
                        book.setPublish(value);
                        break;
                    case "author":
                        book.setAuthor(value);
                        break;
                    case "stock":
                        book.setStock(Long.parseLong(value));
                        break;
                    case "address":
                        book.setAddress(value);
                        break;
                }
            } else {
                // 文件：图片的文件名  文城.png 用户不选择图片时，fileName的数据是空串
                String fileName = item.getName();
                // 文件名相同，为了避免替换：当前的系统时间.png
                // 现获取后缀名 .png
                if (fileName.trim().length() > 0) {   //用户选择了图片，需要文件上传

                    String filterName = fileName.substring(fileName.lastIndexOf("."));
                    // 修改文件名 --- 20220303190355549.png
                    fileName = DateHelper.getImageName() + filterName;
                    // 保存到哪里 --- 虚拟路径 Images/cover/ 对应的实际路径中
                    String path = req.getServletContext().getRealPath("/Images/cover");
                    // 形成文件名 -- d:/xxx/xx/20220303190355549.png
                    String filePath = path + "/" + fileName;
                    // 数据库表的路径 --- Images/cover/101-1.png(相对项目根目录的位置）
                    String dbPath = "Images/cover/" + fileName;
                    book.setPic(dbPath);

                    // 保存文件
                    item.write(new File(filePath));
                }

            }
        }

        int count = bookBiz.modify(book);
        if (count>-0){
            out.println("<script>alert('修改书籍成功');location.href='book.let?type=query&pageIndex=1';</script>");
        }else {
            out.println("<script>alert('修改书籍失败');location.href='book.let?type=query&pageIndex=1';</script>");
        }
    }
    /**
     * 查看图书详情
     * @param req
     * @param resp
     * @param out
     */
    private void details(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws ServletException, IOException {
        // 1.获取图书的编号
        long bookId = Long.parseLong(req.getParameter("id"));
        // 2.根据图书编号获取图书对象
        Book book = bookBiz.getById(bookId);
        // 3.将对象保存到request中
        req.setAttribute("book",book);
        // 4.转发到jsp页面
            req.getRequestDispatcher("book_details.jsp").forward(req,resp);
    }

    /**
     * 查询
     * /book.let?type=query&pageIndex=1 :分页查询(request:转发)
     * 1.页数：Biz
     * 2.当前页码：pageIndex
     * 3.存到request当中
     * @param req
     * @param resp
     * @param out
     */
    private void query(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws ServletException, IOException {
        //1.获取信息：页数、页码、信息
        int pageSize = 3;
        int pageCount = bookBiz.getPageCount(pageSize);
        int pageIndex = Integer.parseInt(req.getParameter("pageIndex"));
        //解决上一页，下一页的问题
        if (pageIndex<1){
            pageIndex = 1;
        }
        if (pageIndex>pageCount){
            pageIndex = pageCount;
        }
        List<Book> books = bookBiz.getByPage(pageIndex,pageSize);

        //2.存
        req.setAttribute("pageCount",pageCount);
        req.setAttribute("books",books);

        //3.转发到jsp页面
        req.getRequestDispatcher("book_list.jsp?pageIndex" + pageIndex).forward(req,resp);
    }

    /**
     * 添加图书
     * 1.获取表单 ---- enctype="multipart/form-data"：和以前不同
     * 2.文件上传 ---- 图片文件从浏览器端保存到服务器（用第三方的jar包 FileUpload+io)
     * 3.文件路径 ---- 实际路径 vs 虚拟路径（服务器里存的路径）
     * @param req
     * @param resp
     * @param out
     * @throws Exception
     */
    private void add(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        // 1.构建一个磁盘工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 2.设置磁盘工厂的大小
        factory.setSizeThreshold(1024*9);
        // 3.构建临时仓库
        File file = new File("d:\\temp");
        // 如果文件夹不存在，则创建一个
        if(!file.exists()){
            file.mkdir();
        }
        factory.setRepository(file);

        // 4.文件上传 + 表单数据
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        // 5.将用户的请求解析成一个个FileItem(文件+表单元素）
        List<FileItem> fileItems = fileUpload.parseRequest(req);
        // 6.遍历FileItem
        Book book = new Book();
        for (FileItem item:fileItems){
            if (item.isFormField()){
                // 获取表单的元素名称和用户填写的值  name:文城
                String name = item.getFieldName();
                String value = item.getString("utf-8"); //防止乱码
                switch (name){
                    case "typeId":
                        book.setTypeId(Long.parseLong(value));
                        break;
                    case "name":
                        book.setName(value);
                        break;
                    case "price":
                        book.setPrice(Double.parseDouble(value));
                        break;
                    case "desc":
                        book.setDesc(value);
                        break;
                    case "publish":
                        book.setPublish(value);
                        break;
                    case "author":
                        book.setAuthor(value);
                        break;
                    case "stock":
                        book.setStock(Long.parseLong(value));
                        break;
                    case "address":
                        book.setAddress(value);
                        break;
                }
            }else {
                // 文件：图片的文件名  文城.png
                String fileName = item.getName();
                // 文件名相同，为了避免替换：当前的系统时间.png
                // 现获取后缀名 .png
                String filterName = fileName.substring(fileName.lastIndexOf("."));
                // 修改文件名 --- 20220303190355549.png
                fileName = DateHelper.getImageName()+filterName;
                // 保存到哪里 --- 虚拟路径 Images/cover/ 对应的实际路径中
                String path = req.getServletContext().getRealPath("/Images/cover");
                // 形成文件名 -- d:/xxx/xx/20220303190355549.png
                String filePath = path + "/" + fileName;
                // 数据库表的路径 --- Images/cover/101-1.png(相对项目根目录的位置）
                String dbPath = "Images/cover/" + fileName;
                book.setPic(dbPath);

                // 保存文件
                item.write(new File(filePath));


            }
        }

        // 7.将信息保存到数据库
        int count = bookBiz.add(book);
        if (count>-0){
            out.println("<script>alert('添加书籍成功');location.href='book.let?type=query&pageIndex=1';</script>");
        }else {
            out.println("<script>alert('添加书籍失败');location.href='book_add.jsp';</script>");
        }


    }
}

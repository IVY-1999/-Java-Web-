package com.enjoy.book.dao;

import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.enjoy.book.bean.Book;
import org.apache.commons.dbutils.handlers.ScalarHandler;

public class BookDao {
    QueryRunner runner = new QueryRunner();

    //根据类型查询对应的书籍信息
    public List<Book> getBooksByTypeId(long typeId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where typeId=?";
        List<Book> books = runner.query(conn,sql,new BeanListHandler<Book>(Book.class),typeId);
        DBHelper.close(conn);
        return books;
    }

    /**
     * 添加书籍
     * @return
     */
    public int add(long typeId,String name,double price,String desc,String pic,
                   String publish,String author,long stock,String address) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "insert into book(typeId,`name`,price,`desc`,pic,publish,author,stock,address) values(?,?,?,?,?,?,?,?,?)";
        int count = runner.update(conn,sql,typeId,name,price,desc,pic,publish,author,stock,address);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 修改书籍
     * @param id
     * @param typeId
     * @param name
     * @param price
     * @param desc
     * @param pic
     * @param publish
     * @param author
     * @param stock
     * @param address
     * @return
     */
    public int modify(long id,long typeId,String name,double price,String desc,String pic,
                      String publish,String author,long stock,String address) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update book set typeId = ?,`name` = ?,price = ?, `desc` = ?,pic = ?,publish = ?,author = ?,stock = ?,address = ? where id = ?";
        int count = runner.update(conn,sql,typeId,name,price,desc,pic,publish,author,stock,address,id);
        DBHelper.close(conn);
        return count;

    }

    /**
     * 删除书籍
     * @param id
     * @return
     */
    public int remove(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "delete from book where id = ?";
        int count = runner.update(conn,sql,id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 分页查询书籍（不考虑排序）
     * pageIndex --- 第几页，从1开始
     * pageSize --- 每一页多少行
     * @return --- 当前页的信息
     */
    public List<Book> getByPage(int pageIndex,int pageSize) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book limit ?,?";
        int offset = (pageIndex - 1) * pageSize;
        List<Book> books = runner.query(conn,sql, new BeanListHandler<Book>(Book.class),offset,pageSize);
        DBHelper.close(conn);
         return books;
    }

    /**
     * 用书籍的id属性查询一本书籍
     * @param id
     * @return
     */
    public Book getById(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where id = ?";
        Book book = runner.query(conn,sql,new BeanHandler<Book>(Book.class),id);
        DBHelper.close(conn);
        return book;

    }

    /**
     * 获取书籍的个数
     * @return
     */
    public int getCount() throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select count(id) from book";
        Number data = runner.query(conn,sql,new ScalarHandler<>()); // long类型
//        System.out.println(data.getClass());
        int count = data.intValue();
        DBHelper.close(conn);
        return count;
    }

    /**
     * 根据书的名字查询一本书
     * @param bookName
     * @return
     * @throws SQLException
     */
    public Book getByName(String bookName) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where name = ?";
        Book book = runner.query(conn,sql,new BeanHandler<Book>(Book.class),bookName);
        DBHelper.close(conn);
        return book;
    }


    /**
     * 修改书籍的数量
     * @param id
     * @param amount ---- 当amount为正数时，则+1；当amount为负数时，则-1
     * @return
     * @throws SQLException
     */
    public int modify(long id,int amount) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update book set stock = stock + ? where id = ?";
        int count = runner.update(conn,sql,amount,id);
        DBHelper.close(conn);
        return count;
    }

    public static void main(String[] args){
        BookDao bookDao = new BookDao();
        try {
            bookDao.modify(12,-1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

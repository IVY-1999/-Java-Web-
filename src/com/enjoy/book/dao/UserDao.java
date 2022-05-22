package com.enjoy.book.dao;

import com.enjoy.book.bean.User;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 用户表的数据操作对象
 */
public class UserDao {

    // 1.创建QueryRunner对象 ---- commons-dbutils-1.7.jar -- 开源框架，节省数据库的操作细节 --- DBUtils
    QueryRunner runner = new QueryRunner();

//    获取用户
    public User getUser(String name,String pwd) throws SQLException {

        // 2.调用DBhelper获取连接对象
        Connection conn = DBHelper.getConnection();
        // 3.准备执行的sql语句
        String sql = "select * from user where name=? and pwd=? and state = 1";
        // 4.调用查询方法，将查询的数据封装成User对象
        User user = runner.query(conn,sql,new BeanHandler<User>(User.class),name,pwd);
        // 5.关闭连接对象
        DBHelper.close(conn);
        // 6.返回User
        return user;
    }

//    修改密码的方法 --- 通过当前登录的用户编号来修改密码
//    返回的是修改的数据行 --- int类型
//    id是需要修改密码的用户编号
//    pwd是当前用户的新密码
    public int modifyPwd(long id, String pwd) throws SQLException {
        String sql = "update user set pwd=? where id=?";
        Connection conn = DBHelper.getConnection();
        int count = runner.update(conn,sql,pwd,id);
        DBHelper.close(conn);
        return count;
    }
}

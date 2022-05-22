package com.enjoy.book.dao;

import com.enjoy.book.bean.Member;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MemberDao {
    QueryRunner runner = new QueryRunner();

    /**
     * 添加会员 --- 会员开卡
     * @param name
     * @param pwd
     * @param typeId
     * @param balance
     * @param tel
     * @param idNumber
     * @return
     * @throws SQLException
     */
    public int add(String name,String pwd,long typeId,double balance,String tel,String idNumber) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "insert into member(`name`,pwd,typeId,balance,regdate,tel,idNumber) values(?,?,?,?,CURRENT_DATE,?,?)";
        int count = runner.update(conn,sql,name,pwd,typeId,balance,tel,idNumber);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 修改会员信息
     * @param id
     * @param name
     * @param pwd
     * @param typeId
     * @param balance
     * @param tel
     * @param idNumber
     * @return
     * @throws SQLException
     */
    public int modify(long id,String name,String pwd,long typeId,double balance,String tel,String idNumber) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update member set `name`=?,pwd=?,typeId=?,balance=?,tel=?,idNumber=? where id=?";
        int count = runner.update(conn,sql,name,pwd,typeId,balance,tel,idNumber,id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 删除会员
     * @param id
     * @return
     * @throws SQLException
     */
    public int remove(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "delete from member where id=?";
        int count = runner.update(conn,sql,id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 通过会员的身份证号码进行修改余额
     * @param idNumber
     * @param amount
     * @return
     * @throws SQLException
     */
    public int modifyBalance(String idNumber,double amount) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update member set balance = balance + ? where idNumber=?";
        int count = runner.update(conn,sql,amount,idNumber);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 通过会员编号（id）进行余额的修改(押金）
     * @param id
     * @param amount ---- 当amount为正数时，则是归还书，+押金；当amount为负数时，则是借书，-押金
     * @return
     * @throws SQLException
     */
    public int modifyBalance(long id ,double amount) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update member set balance = balance + ? where id=?";
        int count = runner.update(conn,sql,amount,id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 查询所有的会员
     * @return
     * @throws SQLException
     */
    public List<Member> getAll() throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select id,`name`,pwd,typeId,balance,regdate,tel,idNumber from member";
        List<Member> members = runner.query(conn,sql,new BeanListHandler<Member>(Member.class));
        DBHelper.close(conn);
        return members;
    }

    /**
     * 根据编号查询一个会员
     * @param id
     * @return
     */
    public Member getById(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select id,`name`,pwd,typeId,balance,regdate,tel,idNumber from member where id=?";
        Member member = runner.query(conn,sql,new BeanHandler<Member>(Member.class),id);
        DBHelper.close(conn);
        return member;
    }

    /**
     * 根据会员身份证号查询一个会员
     * @param idNumber
     * @return
     */
    public Member getByIdNumber(String idNumber) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select id,`name`,pwd,typeId,balance,regdate,tel,idNumber from member where idNumber=?";
        Member member = runner.query(conn,sql,new BeanHandler<Member>(Member.class),idNumber);
        DBHelper.close(conn);
        return member;
    }

    /**
     * 判断会员编号是否存在 record 表中（是否有外键）
     * @param id
     * @return
     */
    public boolean exists(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        //判断是否有外键
        String sql = "select count(*) from record where memberId=?";
        Number number = runner.query(conn,sql,new ScalarHandler<>(),id);
        DBHelper.close(conn);
        return number.intValue()>0?true:false;
    }
}

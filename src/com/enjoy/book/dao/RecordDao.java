package com.enjoy.book.dao;

import com.enjoy.book.bean.Record;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RecordDao {
    QueryRunner runner = new QueryRunner();

   public List<Record> getRecordByBookId(long bookId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where bookId = ?";
        List<Record> records = runner.query(conn,sql,new BeanListHandler<Record>(Record.class),bookId);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 根据用户的会员编号查询用户借阅信息
     * @param memberId
     * @return
     */
    public List<Record> getRecordsByMemberId(long memberId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where memberId = ? and backDate is null";
        List<Record> records = runner.query(conn,sql,new BeanListHandler<Record>(Record.class),memberId);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 根据用户的身份证号查询用户借阅信息
     * @param idNum
     * @return
     */
    public List<Record> getRecordsByIdNum(String idNum) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where memberId = (select id from member where idNumber = ?)";
        List<Record> records = runner.query(conn,sql,new BeanListHandler<Record>(Record.class),idNum);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 添加借书记录
     * @param memberId
     * @param bookId
     * @param deposit
     * @param userId
     * @return
     * @throws SQLException
     */
    public int add(long memberId,long bookId,double deposit,long userId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql="insert into record values(null,?,?,CURRENT_DATE,null,?,?,'978-7-302-12260-9')";
        int count = runner.update(conn,sql,memberId,bookId,deposit,userId);
        DBHelper.close(conn);
        return count;
    }

    /**
     *
     * @param deposit  --- 押金 --- 过期归还，则>0,按时归还，则清零
     * @param userId ---- 处理这个业务的管理员编号
     * @param id ---- 记录编号
     * @return
     */
    public int modify(double deposit,long userId,long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update record set backDate = CURRENT_DATE,deposit = ?,userId = ? where id = ?";
        int count = runner.update(conn,sql,deposit,userId,id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 根据编号查找数据
     * @param recordId
     * @return
     * @throws SQLException
     */
    public Record getById(long recordId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where id = ?";
        Record record = runner.query(conn,sql,new BeanHandler<Record>(Record.class),recordId);
        DBHelper.close(conn);
        return record;
    }

    /**
     * 修改时间（续借）
     * @param id
     * @return
     * @throws SQLException
     */
    public int modify(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update record set rentDate = CURRENT_DATE where id = ?";
        int count = runner.update(conn,sql,id);
        DBHelper.close(conn);
        return count;
    }


    /**
     * 查询全部：0
     * 已归还：1
     * 未归还：2
     * 最近一周需归还：3
     * 搜索：keyWord
     * @param typeId
     * @param keyWord
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> query(int typeId, String keyWord) throws SQLException {
        Connection conn = DBHelper.getConnection();
        StringBuilder sb = new StringBuilder("SELECT * FROM recordView where 1=1");
        switch (typeId){
            case 0:
                break;
            case 1:
                sb.append(" and backDate is not null");
                break;
            case 2:
                sb.append(" and backDate is null");
                break;
            case 3:
                sb.append(" and backDate is null and returnDate < date_add(CURRENT_DATE,interval 7 DAY)");
                break;
        }
        if (keyWord!=null){
            sb.append(" and bookName like '%"+keyWord+"%' or memberName like '%"+keyWord+"%' or concat(rentDate,'') like '%"+keyWord+"%'");
        }

        List<Map<String,Object>> data = runner.query(conn,sb.toString(),new MapListHandler());
        DBHelper.close(conn);
        return data;
    }

    public static void main(String[] args) {
        try {
            List<Map<String,Object>> lm = new RecordDao().query(0,null);
            for (Map<String,Object> row:lm){
                for (String key:row.keySet()){
                    System.out.print(key+":"+row.get(key)+"\t");
                }
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

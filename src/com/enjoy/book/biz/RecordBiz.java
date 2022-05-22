package com.enjoy.book.biz;

import com.enjoy.book.bean.Book;
import com.enjoy.book.bean.Member;
import com.enjoy.book.bean.Record;
import com.enjoy.book.dao.BookDao;
import com.enjoy.book.dao.MemberDao;
import com.enjoy.book.dao.RecordDao;
import com.enjoy.book.util.DBHelper;
import com.enjoy.book.util.DateHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RecordBiz {
    RecordDao recordDao = new RecordDao();
    BookDao bookDao = new BookDao();
    MemberDao memberDao = new MemberDao();
    MemberBiz memberBiz = new MemberBiz();

    public List<Record> getRecordsByMemberId(long memberId){
        List<Record> records = null;
        try {
            records = recordDao.getRecordsByMemberId(memberId);
            // 1.外键信息
              // 1.1获取会员对象
           // Member member =  memberDao.getById(memberId); // 拿不到外键对象
            Member member = memberBiz.getById(memberId);
            for(Record record:records){
                long bookId = record.getBookId();
                Book book = bookDao.getById(bookId);
                record.setBook(book);
                record.setMember(member);

                // 2.应还时间 --- 借阅时间+KeepDay
                long day = member.getType().getKeepDay();
                // 时间的计算(java)
                java.sql.Date rentDate = record.getRentDate();
                // 转成毫秒数
                java.sql.Date backDate = DateHelper.getNewDate(rentDate,day);
                record.setBackDate(backDate);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<Record> getRecordsByIdNum(String idNum){
        List<Record> records = null;
        try {
            records = recordDao.getRecordsByIdNum(idNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * 借阅：
     * 1.借一本书 --- record表添加一行信息（recordDao，insert）
     * 2.这本书的数量 -1 （bookDao，update）
     * 3.会员信息表中更改余额（memberDao，update）
     * 上述更改要么全部成功，要么全部失败(事务处理） --- 前提：使用同一个connection对象
     * @param memberId
     * @param bookIdList
     * @param userId
     * @return  ---- 0表示操作失败，1表示操作成功
     */
    public int add(long memberId,List<Long> bookIdList,long userId){
        // 启动事务
        try {
            DBHelper.beginTransaction();
            double total = 0;
            // 1.拿到借阅的书籍编号
            for (long bookId:bookIdList){
                // 书籍编号

                // 书籍对象
                Book book = bookDao.getById(bookId);
                // 调用价格
                double price = book.getPrice();
                // 算押金
                double regPrice = price*0.3f;
                total += regPrice;
                // 调用recordDao的insert
                recordDao.add(memberId,bookId,regPrice,userId);
                // 调用bookDao的update数量
                bookDao.modify(bookId,-1);
            }
             // 调用memberDao的update余额
            memberDao.modifyBalance(memberId,0-total);
            // 事务结束
            DBHelper.commitTransaction(); // 事务提交：成功
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                DBHelper.rollbackTransaction(); // 事务回滚：失败
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 1;
    }

    /**
     * 归还功能
     * 开启事务
     * 1.recordDao 修改：押金，归还日期，操作员的编号
     * 2.memberDao 修改：余额
     * 3.bookDao 修改：数量
     * 成功就提交，失败就回滚
     * @param memberId
     * @param recordIds
     * @param userId
     * @return
     */
    public int modify(long memberId, List<Long> recordIds, long userId) {
        // 1.开启事务
        try {
            DBHelper.beginTransaction();

            double total = 0;
            Member member = memberBiz.getById(memberId);
            for (long recordId:recordIds){
                // 2.1 通过recordId 获取记录对象：书
                Record record = recordDao.getById(recordId);
                // 2.2 先累加押金
                // 计算押金（逾期：超出一天扣1块）
                java.sql.Date backDate = DateHelper.getNewDate(record.getRentDate(),member.getType().getKeepDay());
                // 系统的当前时间
                java.util.Date currentDate = new java.util.Date();
                int day = 0;
                if (currentDate.after(backDate)){
                    // 计算押金
                    day = DateHelper.getSpan(currentDate,backDate);
                }
                total += record.getDeposit() - day;
                // 2.3 更改record
                recordDao.modify(day,userId,recordId);
                // 2.4 更改book 图书的数量+1
                bookDao.modify(record.getBookId(),1);
            }
            // 2.5 修改余额
            memberDao.modifyBalance(memberId,total);

            DBHelper.commitTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                DBHelper.rollbackTransaction();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
        }
        return 1;
    }

    public int modify(long id){
        int count = 0;
        try {
            count = recordDao.modify(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     * 查询
     * @param typeId
     * @param keyWord
     * @return
     */
    public List<Map<String,Object>> query(int typeId,String keyWord){
        List<Map<String,Object>> rows = null;
        try {
            rows = recordDao.query(typeId,keyWord);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}

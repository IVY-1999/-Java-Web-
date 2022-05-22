package com.enjoy.book.biz;

import com.enjoy.book.bean.Book;
import com.enjoy.book.bean.*;
import com.enjoy.book.dao.BookDao;
import com.enjoy.book.dao.RecordDao;
import com.enjoy.book.dao.TypeDao;

import java.sql.SQLException;
import java.util.List;

public class BookBiz {

    //创建BookDao对象
    BookDao bookDao = new BookDao();

    //根据编号获取书籍
    public List<Book> getBooksByTypeId(long typeId){
        try {
            return bookDao.getBooksByTypeId(typeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //添加
    public int add(long typeId,String name,double price,String desc,String pic,
                   String publish,String author,long stock,String address){
        int count = 0;
        try {
            count = bookDao.add(typeId,name,price,desc,pic,publish,author,stock,address);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    //当用户输入的是一个对象
    public int add(Book book){
        return add(book.getTypeId(),book.getName(),book.getPrice(),book.getDesc(),book.getPic(),book.getPublish(),book.getAuthor(),book.getStock(),book.getAddress());
    }

    public int modify(Book book){
        int count = 0;
        try {
            count = bookDao.modify(book.getId(),book.getTypeId(),book.getName(),book.getPrice(),book.getDesc(),book.getPic(),book.getPublish(),book.getAuthor(),book.getStock(),book.getAddress());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int remove(long id) throws Exception {

        RecordDao recordDao = new RecordDao();
        int count = 0;
        try {
            // 1.判断是否存在外键，存在则不能删除
            List<Record> records = recordDao.getRecordByBookId(id);
            if (records.size()>0){
                throw new Exception("删除的书籍有子信息，删除失败");
            }
            // 2.如果没有外键，则可以删除
            count = bookDao.remove(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public List<Book> getByPage(int pageIndex, int pageSize){
        TypeDao typeDao = new TypeDao();

        List<Book> books = null;
        try {
            books = bookDao.getByPage(pageIndex,pageSize);
            //处理type对象的数据问题
            for (Book book:books){
                long typeId = book.getTypeId();
                book.getType(); //null
                //根据typeId找到对应的type对象，再设置给book.setType()
                Type type = typeDao.getById(typeId);
                book.setType(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getById(long id) {
        Book book = null;
        TypeDao typeDao = new TypeDao();

        try {
              book = bookDao.getById(id);
              long typeId = book.getTypeId();
              Type type = typeDao.getById(typeId);
              book.setType(type);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return book;
    }

    /**
     * 由行数算页数
     * @return
     */
    public int getPageCount(int pageSize){
        int pageCount = 0;
        try {
            // 1.获取行数
            int rowCount = bookDao.getCount();
            // 2.根据行数得到页数，每页多少条
            pageCount = (rowCount - 1) / pageSize + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pageCount;
    }

    public Book getByName(String bookName){
        Book book = null;
        try {
            book = bookDao.getByName(bookName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }
}

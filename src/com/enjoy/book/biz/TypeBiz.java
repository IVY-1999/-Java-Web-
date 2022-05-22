package com.enjoy.book.biz;

import com.enjoy.book.bean.*;
import com.enjoy.book.dao.BookDao;
import com.enjoy.book.dao.TypeDao;

import java.sql.SQLException;
import java.util.List;

public class TypeBiz {
    TypeDao typeDao = new TypeDao();

    public List<Type> getAll(){
        try {
            return typeDao.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int add(String name,long parentId){
        int count = 0;
        try {
            count = typeDao.add(name,parentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int modify(long id,String name,long parentId){
        int count = 0;
        try {
              count = typeDao.modify(id, name, parentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 删除：
     * 表与表之间的关系：逻辑关系
     *type是book的外键
     * @param id
     * @return 0: 1.删除失败（books.size大于0）； 2.Exception --- 提示用户信息
     */
    public int remove(long id) throws Exception {
        //如果有子项，不能删除
        BookDao bookDao = new BookDao();
        int count = 0;
        try {
            List<Book> books = bookDao.getBooksByTypeId(id);
            if(books.size()>0){
                //不能删除
                throw new Exception("删除的类型有子信息，删除失败");
            }
            count = typeDao.remove(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public Type getById(long id){

        try {
            return typeDao.getById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

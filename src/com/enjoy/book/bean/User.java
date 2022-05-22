package com.enjoy.book.bean;


import java.io.Serializable;

/**
 * 保存用户的信息
 * 1.Serializable ----- 序列化接口，读写数据更加方便（用流去书写）
 * 2.私有的属性
 * 3.getter/setter
 * 4.默认的构造
 */

public class User implements Serializable {
  @Override
  public String toString() {   //方便显示和测试
    return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", pwd='" + pwd + '\'' +
            ", state=" + state +
            '}';
  }

  //与bookdb数据库中user表一一对应
  private long id;
  private String name;
  private String pwd;
  private long state;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getPwd() {
    return pwd;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
  }


  public long getState() {
    return state;
  }

  public void setState(long state) {
    this.state = state;
  }

}

package com.jikexueyuan.simplecontacts;

/**
 * Created by huyiqing on 16/11/15.
 */

public class Contact {
//    申明变量
    public String name;
    public String phone;

//    一个空的constructor
    public Contact(){}

//    带参数的constructor
    public Contact(String name, String phone){
        this.name = name;
        this.phone = phone;
    }
//setter
    public void setName(String name){
        this.name = name;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }
//getter
    public String getName(){
        return name;
    }

    public String getPhone(){
        return phone;
    }
//toString 其实并未用到 只是为了项目完整性而添加
    public String toString(){
        return "联系人: " + name + " 电话号: " + phone;
    }
}

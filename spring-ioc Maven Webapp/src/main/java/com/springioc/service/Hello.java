package com.springioc.service;

import com.springioc.dao.HelloDao;

public class Hello {
    private HelloDao helloDao;
    private String user;
    private String where;
    public void sayHello() {
        helloDao.sayHello(user);
    }
    public HelloDao getHelloDao() {
        return helloDao;
    }
    public void setHelloDao(HelloDao helloDao) {
        System.out.println("调用setHelloDao方法");
        this.helloDao = helloDao;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        System.out.println("调用setUser方法");
        this.user = user;
    }
}

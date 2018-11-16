package com.springioc.test;

import com.springioc.service.Hello;
import com.springioc.utils.MyClassPathXmlApplicationContext;

public class SpringTest {
    public static void main(String[] args) {
        MyClassPathXmlApplicationContext ctx = new MyClassPathXmlApplicationContext("beans.xml");
        Hello hello = (Hello)ctx.getBean("hello");
        hello.sayHello();
        asdf
            adsf
            adsf;
    }
}

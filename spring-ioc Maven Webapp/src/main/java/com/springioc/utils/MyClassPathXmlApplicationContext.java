package com.springioc.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 
 * .
 * @author hganghui
 * @since 2018年11月5日
 * @version Revision 1.0.0 2018年11月5日
 * @功能说明：类似于spring的classPathXmlApplicationContext
 *
 */
public class MyClassPathXmlApplicationContext {
    
    private List<BeanDefinition> beanDefines=new ArrayList<BeanDefinition>();//用来存储所有的beans
    private Map<String, Object> sigletons =new HashMap<String, Object>();//用来存储实例化后的bean
    /**
     * 构造方法，用来模拟spring的行为
     * @param fileName
     */
    public MyClassPathXmlApplicationContext(String fileName) {
        this.readXml(fileName);//读取XML配置文件
        this.instanceBeans();  //实例化bean 
//        this.injectObject();//3.实现对依赖对象的注入功能
        this.injectObejects();//3.实现对依赖对象的注入功能
    }
    
    /**
     * 模拟读取XML文件
     * 
     */
    private void readXml(String fileName) {
      //创建一个读取器
        SAXReader saxReader=new SAXReader();
        Document document=null;
        try {
            //获取要读取的配置文件的路径
            URL xmlPath=this.getClass().getClassLoader().getResource(fileName);
            //读取文件内容
            document=saxReader.read(xmlPath);
            //获取xml中的根元素
            Element rootElement=document.getRootElement();
            for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext();) {
                Element element = (Element) iterator.next();
                String id=element.attributeValue("id");//获取bean的id属性值
                String clazz=element.attributeValue("class");//获取bean的class属性值
                BeanDefinition beanDefinition=new BeanDefinition(id, clazz);
                //获取bean的Property属性
                for (Iterator subElementIterator = element.elementIterator(); subElementIterator.hasNext();) {
                    Element subElement = (Element) subElementIterator.next();
                    String propertyName=subElement.attributeValue("name");
                    String propertyRef= subElement.attributeValue("ref");
                    String propertyValue=subElement.attributeValue("value");
                    PropertyDefinition propertyDefinition=new PropertyDefinition(propertyName,propertyValue, propertyRef);
                    beanDefinition.getPropertyDefinitionList().add(propertyDefinition);
                }
                beanDefines.add(beanDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 完成实例化beans
     * 
     */
    private void instanceBeans() {
        if (beanDefines != null && beanDefines.size() >0) {
            //对每个bean进行实例化
            for (BeanDefinition beanDefinition : beanDefines) {
                try {
                    //bean的class属性存在的时候才进行实例化，否则不进行实例化
                    if (beanDefinition.getClassName() != null && !beanDefinition.getClassName().equals("")) {
                        //实例化的关键操作
                        Object object = Class.forName(beanDefinition.getClassName()).newInstance();
                        sigletons.put(beanDefinition.getId(), object);
                        System.out.println("id为："+beanDefinition.getId()+"的bean实例化成功");
//                        sigletons.put(beanDefinition.getId(),Class.forName(beanDefinition.getClassName()).newInstance());
//                        System.out.println("id为："+beanDefinition.getId()+"的bean实例化成功");
                    }
                } catch (Exception e) {
                    System.out.println("bean实例化失败");
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 完成java类的注入
     * 
     * @author hganghui
     * @since 2018年11月9日
     */
    private void injectObejects() {
        for (BeanDefinition beanDefinition : beanDefines) {
            Object object = sigletons.get(beanDefinition.getId());
            List<PropertyDefinition> beanPropertys = beanDefinition.getPropertyDefinitionList();
            try {
                
                BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass(), Object.class);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();//获取参数
                for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    String filedName = propertyDescriptor.getName();//获取属性名称
                    for(PropertyDefinition propertyDefinition : beanPropertys) {
                        if(filedName.equals(propertyDefinition.getName())) {
                            Object value = new Object();
                            Method method = propertyDescriptor.getWriteMethod();
                            if(propertyDefinition.getValue() != null && !"".equals(propertyDefinition.getValue())) {
                                value = propertyDefinition.getValue();
                            } else {
                                value = sigletons.get(propertyDefinition.getRef());
                            }
                            try {
                                method.invoke(object, value);
                            } catch (IllegalAccessException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IllegalArgumentException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            
                            break;
                        }
                        
                    }
                    
                    
                    
                    
                    
                }
                
                
            } catch (IntrospectionException e) {
                
                e.printStackTrace();
            }
            
        }
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * 为bean对象的属性注入值
     * 
     * Administer
     * 2013-8-18 下午7:59:03
     */
    private void injectObject() {
        //遍历配置文件中定义的所有的bean
        for (BeanDefinition beanDefinition : beanDefines) {
            //找到要注入的bean
            Object bean=sigletons.get(beanDefinition.getId());
            if (bean != null) {
                try {
                    BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors(); //获取类的属性
                    if(beanDefinition.getPropertyDefinitionList().size() > 0) {
                        for(PropertyDefinition propertyDefinition : beanDefinition.getPropertyDefinitionList()) {
                            String propertyName = propertyDefinition.getName();
                            for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                                if(propertyDescriptor.getName().equals(propertyName)) {
                                    Method setter = propertyDescriptor.getWriteMethod();
                                    if (setter != null) {
                                        Object value=null;//用来存储引用的值
                                        if(propertyDefinition.getRef() != null && !"".equals(propertyDefinition.getRef())) {
                                            value = sigletons.get(propertyDefinition.getRef());
                                        } else {
                                            value = ConvertUtils.convert(propertyDefinition.getValue(), propertyDescriptor.getPropertyType());
                                        }
                                        setter.setAccessible(true);
                                        try {
                                            setter.invoke(bean, value);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                }
                            }
                        
                        }
                        
                    } 
                
                
            } catch (IntrospectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                
                
                
                
                
                
                
            }
        }
    }
    
    
    
    
    
    
    
    
    
    /**
     * 
     * 通过beanName来获取类实例
     * 
     * @param beanName
     * @return
     * @author hganghui
     * @since 2018年11月5日
     */
    public Object getBean(String beanName){
        return sigletons.get(beanName);
    }
    
    
}

package com.nowcoder.community.entity;
//
//public class Page {
//    //当前页
//    private int current =1;
//    //每页记录数
//    private int limit=10;
//    //数据总数
//    private int rows;
//    // 查询路径(用于复用分页链接)
//    private String path;
//
//    // 获取当前页的起始行
//    public int getOffset(){
//        return (current-1)*limit;
//    }
//
//    //获取总页数
//    public int getTotal(){
//        if(rows%limit==0){
//            return rows/limit;
//        }else{
//            return rows/limit+1;
//        }
//    }
//
//    //获取起始页面
//    public int getFrom(){
//        int from = current-2;
//        return from<1?1:from;
//    }
//    //为什么这里写了getForm（）方法，但是没有form属性呢
//    //因为form属性是通过getForm（）方法计算出来的，所以不需要单独设置
//
//    //获取结束页码
//    public int getTo(){
//        int to = current+2;
//        int total  = getTotal();
//        return to>total?total:to;
//    }
//
//    public int getCurrent() {
//        return current;
//    }
//
//    public void setCurrent(int current) {
//        if(current>=1){
//            this.current = current;
//        }
//    }
//
//    public int getLimit() {
//        return limit;
//    }
//
//    public void setLimit(int limit) {
//        if(limit>=1&&limit<=50){
//            this.limit = limit;
//        }
//    }
//
//    public int getRows() {
//        return rows;
//    }
//
//    public void setRows(int rows) {
//        if(rows>0){
//            this.rows = rows;
//        }
//    }
//
//    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//}


public class Page{
    int current = 1;
    int limit = 10;

    int rows ;
    String path;

    //获取当前页的起始行
    public int getOffset(){
        return (current-1)*limit;
    }

    //获取总页数
    public int getTotal(){
        if(rows%limit==0){
            return rows%limit;
        }else{
            return rows%limit+1;
        }
    }

    //获取起始页码
    public int getFrom(){
        int from=current-2;
        return from<1?1:from;
    }
    //获取结束页码
    public int getTo(){
        int to = current+2;
        int total = getTotal();
        return to>total?total:to;
    }
    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=50){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
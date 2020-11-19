package com.iknow.community.bean;

public class Page {

    // 当前页
    private int current = 1;
    // 总页数
    private int rows;
    // 每页显示多少行
    private int limit = 10;
    // 路径
    private String path = "/index";
    // 起始页码
    private int from;
    // 结束页码
    private int to;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0)
            this.rows = rows;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }


    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    /**
     * 获取当前页的起始行
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     */
    public int getTotal() {

        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始结束页码
     */
    public int getPages(int i) {
        int total = getTotal();
        if (current < 4) {
            from = 1;
            to = from + 4 > total ? total : from + 4;
        } else {
            to = current + 2 > total ? total : current + 2;
            from = to - 4 < 1 ? 1 : to - 4;
        }
        if (i == 1)
            return from;
        else return to;
    }

    public int getFrom() {
//        if (current > 3) {
//            from = current - 2;
//        } else {
//            from = 1;
//        }
        return getPages(1);
    }

    public int getTo() {
//        return current + 2 > getTotal() ? getTotal() : current + 2;
        return getPages(2);
    }

}

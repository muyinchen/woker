<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>查询商品列表</title>
    <script type="text/javascript">
        function editItemsAllSubmit() {
            //提交form
            document.itemsForm.action = "${pageContext.request.contextPath }/items/editItemsAllSubmit.action";
            document.itemsForm.submit();
        }
        function queryItems() {
            //提交form
            document.itemsForm.action = "${pageContext.request.contextPath }/items/queryItems.action";
            document.itemsForm.submit();
        }
    </script>
</head>
<body>
<form name="itemsForm" action="${pageContext.request.contextPath }/items/queryItems.action" method="post">
    查询条件：
    <table width="100%" border=1>
        <tr>
            <td>商品名称：<input name="itemsCustom.name"/></td>
            <td>
                <input type="button" value="查询" onclick="queryItems()"/>
                <input type="button" value="批量修改" onclick="editItemsAllSubmit()"/>
            </td>
        </tr>
    </table>
    商品列表：
    <table width="100%" border=1>
        <tr>
            <td>商品名称</td>
            <td>商品价格</td>
            <td>生产日期</td>
            <td>商品描述</td>
            <td>操作</td>
        </tr>
        <c:forEach items="${itemsList }" var="item" varStatus="status">
            <tr>

                <td><input name="itemsList[${status.index }].name" value="${item.name }"/></td>
                <td><input name="itemsList[${status.index }].price" value="${item.price }"/></td>
                <td><input name="itemsList[${status.index }].createtime"
                           value="<fmt:formatDate value="${item.createtime}" pattern="yyyy-MM-dd HH:mm:ss"/>"/></td>
                <td><input name="itemsList[${status.index }].detail" value="${item.detail }"/></td>

            </tr>
        </c:forEach>

    </table>
</form>
</body>

</html>
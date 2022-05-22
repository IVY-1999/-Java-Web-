<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html >
<html >
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="keywords"  content = "图书 java jsp"/>
    <meta http-equiv="author" content="phenix"/>
    <link rel="stylesheet" type="text/css" href="./Style/skin.css" />
    <script src="Js/jquery-3.3.1.min.js"></script>
    <script>
        // 格式 2022-03-08
        // 获取系统的当前时间
        function getCurrentDate(){
            var dateObj = new Date();
            var year = dateObj.getFullYear();
            var month = dateObj.getMonth()+1; // 0-11
            var date = dateObj.getDate();
            var dateStr = year+"-"+(month>=10?month:"0"+month)+"-"+(date>=10?date:"0"+date);
            return dateStr;
        }

        /**
         * 获取归还时间
         * count --- 借阅时间
         * 2022-03--8 ； count=20 ---- 2022-03-30归还
         * 毫秒：1s = 1000ms   1m = 60s   1h = 60min   1d = 24h
         * @param count
         */
        function getBackDate(count){
            // 获取系统的当前时间
            var dateObj = new Date();
            // 转成毫秒数
            var mills = dateObj.getMilliseconds();
            // 计算
            mills += count*24*60*60*1000;
            // // 转换成日期
            dateObj.setMilliseconds(mills);
            // 时间转成字符串
            var year = dateObj.getFullYear();
            var month = dateObj.getMonth()+1; // 0-11
            var date = dateObj.getDate();
            var dateStr = year+"-"+(month>=10?month:"0"+month)+"-"+(date>=10?date:"0"+date);
            return dateStr;
        }

        $(function (){
            // 查询书籍的功能先关闭，不可用
            $("#btnQueryBook").prop("disabled","disabled");
            $("#btnSubmit").prop("disabled","disabled");
            var member = null;

            $("#btnQuery").click(function (){
                // 1.获取用户输入的内容（身份证号）
                var content = $("#memberId").val();
                // 判断是否为空或者为null
                if (!content){
                    alert("请输入用户号");
                    return;
                }
                // 2.调用ajax方法
                var url = "member.let?type=doajax&idn="+content;
                $.get(url,function (data,status){
                    // Json字符串
                    // 1.json字符串转成json对象
                    member = JSON.parse(data);
                    // 2.给组件赋值
                    $("#name").val(member.name);
                    $("#type").val(member.type.name);
                    $("#amount").val(member.type.amount);
                    $("#balance").val(member.balance);

                });
                // 查询用户的功能关闭
                $(this).prop("disabled","disabled");
                // 开启查询按钮的功能
                $("#btnQueryBook").removeAttr("disabled");
            });

            // 所有添加过的书名
            var bookNameList  = new Array();
            $("#btnQueryBook").click(function (){
               var name = $("#bookContent").val();
               var url = "book.let?type=doajax&name="+name;
               $.get(url,function (data,status){
                  // 返回 {} 或者一本书的 json 信息
                   // 书籍不存在
                   if (data==="{}"){
                       alert("当前书籍不存在，查找失败");
                       $("#bookContent").val("");
                       return;
                   }
                   // 判断这本书名是否已经存在（在列表中）
                   if(bookNameList.indexOf(name)>=0){
                       alert("当前书籍已添加，添加失败");
                       $("#bookContent").val("");
                       return;
                   }
                   // 添加书名到数组
                   bookNameList.push(name);
                   var book = JSON.parse(data);
                   // 书籍存在
                   // 1.创建行
                   var tr = $("<tr align=\"center\" class=\"d\">");
                   // 2.创建多个列
                   var tdCheck = $("<td><input type=\"checkbox\" value=\""+book.id+"\" class=\"ck\" checked></td>");
                   var tdName = $("<td>"+book.name+"</td>");
                   // 借书日期：系统当前时间
                   var tdRentDate = $("<td>"+getCurrentDate()+"</td>");
                   // 归还日期：系统当前时间 + 根据会员等级计算书可以在用户手中留多长时间
                   var tdBackDate = $("<td>"+getBackDate(member.type.keepDay)+"</td>");
                   var tdPublish = $("<td>"+book.publish+"</td>");
                   var tdAddress = $("<td>"+book.address+"</td>");
                   var tdPrice = $("<td>"+book.price+"</td>");
                   // 3.将列加到行里
                   tr.append(tdCheck);
                   tr.append(tdName);
                   tr.append(tdRentDate);
                   tr.append(tdBackDate);
                   tr.append(tdPublish);
                   tr.append(tdAddress);
                   tr.append(tdPrice);
                   // 4.将行加到表里
                   $("#tdBook").append(tr);
                   $("#bookContent").val("");
                   $("#btnSubmit").removeAttr("disabled");
               });
            });

            // 全选/全不选功能实现
            $("#ckAll").click(function (){
                $(".ck").prop("checked",$(this).prop("checked"));  //ck是添加书籍时设置的class
            });

            // 完成借阅的功能
            $("#btnSubmit").click(function (){
                // 获取用户选择的书籍编号（多本）
                var ids = new Array();
                var count = 0;
                $(".ck").each(function (){
                    if($(this).prop("checked")){
                        ids.push($(this).val());
                        count++;
                    }
                });
                if (count===0){
                    alert("请选择借阅书籍");
                    return;
                }
                if (count>member.type.amount){
                    alert("借阅数量超出范围");
                    return;
                }
                // 请求servlet
                location.href = "record.let?type=add&mid="+member.id+"&ids="+ids.join("_");
            });
        });
    </script>
</head>
    <body>
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <!-- 头部开始 -->
            <tr>
                <td width="17" valign="top" background="./Images/mail_left_bg.gif">
                    <img src="./Images/left_top_right.gif" width="17" height="29" />
                </td>
                <td valign="top" background="./Images/content_bg.gif">
                    
                </td>
                <td width="16" valign="top" background="./Images/mail_right_bg.gif"><img src="./Images/nav_right_bg.gif" width="16" height="29" /></td>
            </tr>
            <!-- 中间部分开始 -->
            <tr>
                <!--第一行左边框-->
                <td valign="middle" background="./Images/mail_left_bg.gif">&nbsp;</td>
                <!--第一行中间内容-->
                <td valign="top" bgcolor="#F7F8F9">
                    <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
                        <!-- 空白行-->
                        <tr><td colspan="2" valign="top">&nbsp;</td><td>&nbsp;</td><td valign="top">&nbsp;</td></tr>
                        <tr>
                            <td colspan="4">
                                <table>
                                    <tr>
                                        <td width="100" align="center"><img src="./Images/mime.gif" /></td>
                                        <td valign="bottom"><h3 style="letter-spacing:1px;">常用功能 > 图书借阅 </h3></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- 一条线 -->
                        <tr>
                            <td height="20" colspan="4">
                                <table width="100%" height="1" border="0" cellpadding="0" cellspacing="0" bgcolor="#CCCCCC">
                                    <tr><td></td></tr>
                                </table>
                            </td>
                        </tr>
                        <!-- 会员信息开始 -->
                        <tr>
                            <td width="2%">&nbsp;</td>
                            <td width="96%">
                                <fieldset>
                                    <legend>查询会员</legend>
                                    <table width="100%" border="0" class="cont"  >
                                        <tr>
                                            <td width="8%" class="run-right"> 会员编号</td>
                                            <td colspan="7"><input class="text" type="text" id="memberId" name="memberId"/> 
                                                 <input type="button" id="btnQuery" value="确定" style="width: 80px;"/></td>
                           
                                            </td>
                                         
                                        </tr>
                                        <tr>
                                            <td width="8%" class="run-right">会员名称</td>
                                            <td width="17%"><input class="text" type="text" id="name" disabled/></td>
                                            <td width="8%" class="run-right">会员类型:</td>
                                            <td width="17%"><input class="text" type="text" id="type" disabled/></td>
                                            <td width="8%" class="run-right">可借数量</td>
                                            <td width="17%"><input class="text" type="text" id="amount" disabled/></td>
                                            <td width="8%" class="run-right">账户余额</td>
                                            <td width="17%"><input class="text" type="text" id="balance" disabled/></td>
                                        </tr>
                                    </table>
                                </fieldset>
                            </td>
                            <td width="2%">&nbsp;</td>
                        </tr>
                      
                        <!--空行-->
                        <tr>
                            <td height="40" colspan="3">
                            </td>
                        </tr>
                        
                        <!--图书搜索条-->
                        <tr>
                            <td width="2%">&nbsp;</td>
                            <td width="96%">
                                <fieldset>
                                    <legend>查询图书</legend>
                                    <table width="100%" border="1" class="cont"  >
                                        <tr>
                                            <td colspan="8">
                                               
                                                请输入:&nbsp;&nbsp;<input class="text" type="text" id="bookContent" name="bookContent" placeholder="输入条形码/书名"/>
                                                <input type="button" id="btnQueryBook" value="确定" style="width: 80px;"/>
                                                <input type="button" id="btnSubmit" value="完成借阅" style="width: 80px;"/>
                                            </td>
                                         
                                        </tr>
                                       
                                    </table>
                                </fieldset>
                            </td>
                            <td width="2%">&nbsp;</td>
                        </tr>
                        <tr><td height="20" colspan="3"></td></tr>
                        <tr>
                            <td width="2%">&nbsp;</td>
                            <td width="96%">
                                <table width="100%">
                                    <tr>
                                        <td colspan="2">
                                            <form action="" method="">
                                                <table width="100%"  class="cont tr_color" id="tdBook">
                                                    <tr>
                                                        <th><input id="ckAll" type="checkbox" checked/>全选/全不选</th>
                                                        <th>书籍名</th>
                                                        <th>借阅时间</th>
                                                        <th>应还时间</th>
                                                        <th>出版社</th>
                                                        <th>书架</th>
                                                        <th>定价(元)</th>
                                                    </tr>
                                                </table>
                                            </form>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                            <td width="2%">&nbsp;</td>
                        </tr>
                        <!-- 产品列表结束 -->
                        <tr>
                            <td height="40" colspan="4">
                                <table width="100%" height="1" border="0" cellpadding="0" cellspacing="0" bgcolor="#CCCCCC">
                                    <tr><td></td></tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td width="2%">&nbsp;</td>
                            <td width="51%" class="left_txt">
                                <img src="./Images/icon_mail.gif" width="16" height="11"> 客户服务邮箱：2404926477@qq.com<br />
                            </td>
                            <td>&nbsp;</td><td>&nbsp;</td>
                        </tr>
                    </table>
                </td>
                <td background="./Images/mail_right_bg.gif">&nbsp;</td>
            </tr>
            <!-- 底部部分 -->
            <tr>
                <td valign="bottom" background="./Images/mail_left_bg.gif">
                    <img src="./Images/buttom_left.gif" width="17" height="17" />
                </td>
                <td background="./Images/buttom_bgs.gif">
                    <img src="./Images/buttom_bgs.gif" width="17" height="17">
                </td>
                <td valign="bottom" background="./Images/mail_right_bg.gif">
                    <img src="./Images/buttom_right.gif" width="16" height="17" />
                </td>           
            </tr>
        </table>
    </body>
</html>
<!DOCTYPE html>
<html>
<head>
    <title></title>
    <meta charset="utf-8"/>
    <script src="/teststatic/qrcode.js"></script>
    <style>
        #qrcode {
            /*text-align: center;*/
            /*display: table-cell;*/
            /*width: 96px;*/
            /*height: 96px;*/
            /*vertical-align:middle;*/
            /*position: relative;*/
        }
    </style>
</head>
<body>
<div id="qrcode">
</div>

<input type="text" id="getval"/>
<button id="send">点击更换验证码</button>
<script>
    window.onload =function(){
        var qrcode = new QRCode(document.getElementById("qrcode"), {
            width : 96,//设置宽高
            height : 96
        });
        qrcode.makeCode("http://www.baidu.com");
        document.getElementById("send").onclick =function(){
            qrcode.makeCode(document.getElementById("getval").value);
        }
    }



</script>
hello world
${msg}
this is test
</body>
</html>

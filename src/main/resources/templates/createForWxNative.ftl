<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Pay</title>
</head>
<body>
<div id="myQrcode"></div>
<div id="orderId">${orderId}</div>
<div id="returnUrl">${returnUrl}</div>

<script src="https://cdn.bootcdn.net/ajax/libs/jquery/1.5.1/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
<script>
    jQuery('#myQrcode').qrcode({
        text    :"${codeUrl}"
    });

    $(function (){
       //timer
       setInterval(function (){
           console.log('start to query payment status...')
           $.ajax({
               url:'/pay/queryByOrderId',
               data:{
                   'orderId':$('#orderId').text()
               },
               success:function (result){
                   console.log(result)
                   if(result.platformStatus != null
                      && result.platformStatus === 'SUCCESS'){
                       location.href = $('#returnUrl').text()
                   }
               },
               error:function (result){
                    alert(result)
               }
           })
       },2000)
    });
</script>
</body>
</html>
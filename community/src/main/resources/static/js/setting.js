/**
 * 上传头像到七牛云
 */

$(function () {
    $("#uploadForm").submit(upload);
});

function upload(){

    $.ajax({
        //七牛云的客户端上传地址
        url : "//upload-cn-east-2.qiniup.com",
        method: "post",
        //告诉jQuery不要去处理发送的数据,jquery默认会把数据转换成字符串，但是我们要传的是文件
        processData: false,
        //告诉jQuery不要去设置ContentType，浏览器会自动设置，
        //文件是二进制文件，和别的数据混在一起的时候，边界怎么确定浏览器会加一个随机的边界字符串好去拆分
        //但是如果设置了这个属性，jquery就会去走动区设置这个类型，但是边界字符串jquery设置不了，会导致传文件出问题。
        contentType:false,
        //FormData是原生的js对象，
        // $("#uploadForm")[0]是原生的dom对象，jquery对象转换成原生dom对象，jquery对象是dom对象的数组
        data: new FormData($("#uploadForm")[0]),
        success:function (data){
            if(data && data.code == 0){
                //更新头像访问路径
                $.post(
                    CONTEXT_PATH+"/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function (data){
                        data = $.parseJSON(data);
                        if(data.code==0){
                            window.location.reload();
                        }else{
                            alert(data.msg);
                        }
                    }
                )
            }else {
                alert("上传失败！")
            }
        }
    });
    //阻止表单默认提交。
    return false;
}
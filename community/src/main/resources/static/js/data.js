//处理数据统计页面的js
$(function(){
    $("#uvData").click(getUVData);
    $("#dauData").click(getDAUData);
});
function getUVData() {
    //如果没有输入开始或者结束日期，就不发送请求
    if($("#uvStart").val() == "" || $("#uvEnd").val() == "") {
        alert("请输入日期");
        return;
    }
    $.post(
         CONTEXT_PATH+"/data/uv",
        //$("#uvStart").val()获取的是字符串，因为在html中设置了input的type="date"，所以获取的是yyyy-MM-dd格式的字符串
        {
            "start": $("#uvStart").val(),
            "end": $("#uvEnd").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#uvResult").text(data.uvResult);

                // 将data.uvStart格式化成yyyy-MM-dd
                var uvStartDate = data.uvStart;
                var uvStartFormatted = formatDate(uvStartDate);
                $("#uvStart").val(uvStartFormatted);

                // 将data.uvEnd格式化成yyyy-MM-dd
                var uvEndDate = data.uvEnd;
                var uvEndFormatted = formatDate(uvEndDate);
                $("#uvEnd").val(uvEndFormatted);
            } else {
                alert(data.msg);
            }
        }
    );
}

function getDAUData() {
    //如果没有输入开始或者结束日期，就不发送请求
    if($("#dauStart").val() == "" || $("#dauEnd").val() == "") {
        alert("请输入日期");
        return;
    }
    $.post(
        CONTEXT_PATH+"/data/dau",
        {
            "start": $("#dauStart").val(),
            "end": $("#dauEnd").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#dauResult").text(data.dauResult);

                // 将data.dauStart格式化成yyyy-MM-dd
                var dauStartDate = data.dauStart;
                var dauStartFormatted = formatDate(dauStartDate);
                $("#dauStart").val(dauStartFormatted);

                // 将data.dauEnd格式化成yyyy-MM-dd
                var dauEndDate = data.dauEnd;
                var dauEndFormatted = formatDate(dauEndDate);
                $("#dauEnd").val(dauEndFormatted);
            } else {
                alert(data.msg);
            }
        }
    );
}

// 日期格式化函数
function formatDate(date) {
    date = new Date(date);
    // getFullYear() 方法只能用于new Date()格式的时间，
    // 所以要先将date转换成new Date()格式,虽然不转换也能用，但是会报错
    var year = date.getFullYear();
    // 月份从0开始，所以要加1
    var month = date.getMonth() + 1;
    var day = date.getDate();
    return year + '-' + (month < 10 ? '0' + month : month) + '-' + (day < 10 ? '0' + day : day);
}
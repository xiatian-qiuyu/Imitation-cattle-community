$(function(){
	$("#publishBtn").click(publish);
});

/**
 * 发布帖子按钮事件
 */
function publish() {
	//隐藏发布框
	$("#publishModal").modal("hide");

	// 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
//    var token = $("meta[name='_csrf']").attr("content");
//    var header = $("meta[name='_csrf_header']").attr("content");
//    $(document).ajaxSend(function(e, xhr, options){
//        xhr.setRequestHeader(header, token);
//    });

	//获取发布信息
	let title=$("#recipient-name").val()
	let content=$("#message-text").val()
	$.post(
		CONTEXT_PATH+"/disscussPost/add",
		{"title":title,"content":content},
		function (data){
			//将data转换为json对象
			data = $.parseJSON(data);
			//给提示框返回提示信息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			// 2秒后,自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 500);
		}
	)
}
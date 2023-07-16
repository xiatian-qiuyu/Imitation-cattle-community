$(function(){
	$("#sendBtn").click(send_letter);
	//不能通过Id来获取，因为Id是唯一的，而前端是一个遍历的li，所以要通过class来获取
	$(".closeMsg").click(withdraw_Message);

});

function send_letter() {
	$("#sendModal").modal("hide");
	//获取发送的详情
	let toName = $("#recipient-name").val();
	let content = $("#message-text").val();
	$.post(
		CONTEXT_PATH+"/message/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			data = $.parseJSON(data);
			if(data.code==0){
				$("#hintBody").text("发送成功");
			}else{
				$("#hintBody").text("发送失败")
			}
			//.modal()是bootstrap的方法，用于显示模态框
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").hide();
				//$("#hintModal").hide()和$("#hintModal").modal("hide")的区别是什么
				//a:$("#hintModal").hide()是直接隐藏，而$("#hintModal").modal("hide")是调用bootstrap的方法隐藏
				$("#hintModal").modal("hide");
				if(data.code===0){
					window.location.reload();
				}
			}, 1000);
		}
	)
}

function withdraw_Message() {
	// 撤回消息
	var btn = this;
	//$(btn).prev().val();是获取当前元素的前一个元素的值
	var messageId = $(btn).prev().val();
	//不能使用$("#message-id").val();来获取，因为这样获取的是第一个元素的值,id是唯一的,而前端是遍历的li
	// var messageId = $("#message-id").val();
	$.post(
		CONTEXT_PATH + "/message/letter/withdraw",
		{"messageId":messageId},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				//将当前元素的父元素删除，即删除整个li,li的class为media
				$(btn).parents(".media").remove();
			} else {
				alert(data.msg);
			}
		}
	);
}
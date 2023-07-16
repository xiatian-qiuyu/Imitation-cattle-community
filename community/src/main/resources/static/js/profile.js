$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    var btn = this;
    // 关注,取消关注
    $.post(
        CONTEXT_PATH + "/follow",
        {"entityType": 3, "entityId": $(btn).prev().val()},
        function (data) {
            data = $.parseJSON(data);
			if (data.code == 0) {
				if(data.followStatus){
					$(btn).text("已关注").addClass("btn-secondary").removeClass("btn-info");
				}else{
					$(btn).text("关注").addClass("btn-info").removeClass("btn-secondary");
				}
				$("#followerCount").text(data.followerCount);
			} else {
				alert(data.msg);
			}
        }
    )


}
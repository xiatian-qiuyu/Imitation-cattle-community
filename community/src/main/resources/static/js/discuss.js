$(function(){
    $("#toTop").click(setTop);
    $("#refinement").click(refinement);
    $("#delete").click(deletePost);
});

function setTop() {
    $.post(
        CONTEXT_PATH+"/disscussPost/toTop",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code==0) {
                if (data.type == 1) {
                    $("#toTop").text("取消置顶").addClass("btn-success").removeClass("btn-danger");
                } else {
                    $("#toTop").text("置顶").addClass("btn-danger").removeClass("btn-success");
                }
            }else{
                alert(data.msg);
            }
        }
    );
}

function refinement() {
    $.post(
        CONTEXT_PATH+"/disscussPost/refinement",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code==0) {
                if (data.status == 1) {
                    $("#refinement").text("取消加精").addClass("btn-success").removeClass("btn-danger");
                } else {
                    $("#refinement").text("加精").addClass("btn-danger").removeClass("btn-success");
                }
            }else{
                alert(data.msg);
            }
        }
    );
}
function deletePost() {
    $.post(
        CONTEXT_PATH+"/disscussPost/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code==0) {
                location.href = CONTEXT_PATH+"/index";
            }else{
                alert(data.msg);
            }
        }
    );
}

function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
            data = $.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            }else{
                alert(data.msg);
            }
        }
    )
}

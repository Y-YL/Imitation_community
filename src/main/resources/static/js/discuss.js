$(
    function () {
        $("#btnTop").click(setTop);
        $("#btnWonder").click(setWonderful);
        $("#btnDelete").click(setDelete);
    }
)


function like(btnLink,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH + "/like",
        {
        "entityType":entityType,
            "entityId":entityId,
            "entityUserId":entityUserId,
            "postId":postId
    },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btnLink).children("i").text(data.likeCount);
                $(btnLink).children("b").text(data.likeStatus==1? '已赞':'赞');
            }else{
                alert(data.msg)
            }
        }
    )
}

function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {
            "discussPostId":$("#postId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("btnTop").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        }
    )
}

function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {
            "discussPostId":$("#postId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("btnWonder").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        }
    )
}

function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {
            "discussPostId":$("#postId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                location.href=CONTEXT_PATH + "/index";
            }else{
                alert(data.msg);
            }
        }
    )
}
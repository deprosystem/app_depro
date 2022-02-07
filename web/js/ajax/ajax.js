
function doServer(metod, url, callBack, data, paramCallBack, progress, cbError){
    var req = new XMLHttpRequest();
    let divProgress;
    req.open(metod, url, true);
    req.setRequestHeader('schemDB', schema);
//    req.setRequestHeader('Content-type', "text/html; charset=utf-8");
    req.onreadystatechange = function () {
        if (divProgress != null) {
            document.body.removeChild(divProgress);
            divProgress = null;
        }
        if (req.readyState == 4) {
            if (req.status == 200) {
                callBack(req.response, paramCallBack);

            } else {
console.log("AJAX="+req.response);
                var mes = JSON.parse(req.response).message;
                dialogError("Server error", "status=" + req.status + " " + mes);
                if (cbError != null) {
                    cbError(req.response);
                }
//                alert("doServer status=" + req.status + " " + mes);
            }
        }
    };
    data=data||null;
    if (progress != null) {
        divProgress = windProgr(progress);
        document.body.append(divProgress);
    }
    req.send(data);
}

function windProgr(progress) {
    let xy = getCoordsEl(progress);
    let x = xy.left;
    let y = xy.top;
    let h = xy.height;
    let w = xy.width;
    let pr;
    let dv = document.createElement('div');
    dv.style.cssText = "position:absolute;width:" + w + "px;height:" + h + "px;background:#fffc;outline:1px solid #1dace9;border-radius:8px;left:" + x + "px;top:" + y + "px;z-index:100";
    pr = document.createElement('img');
    pr.className = "progress_center";
    dv.appendChild(pr);
    pr.src = "img/progress.png";
    return dv;
}

function insertHtml() {
    this.get = function (elId, url, func) {
        this.elId = elId;
        this.url = url;
        this.func = func;
        var a = new XMLHttpRequest();
        a.open("GET", this.url);
        a.onreadystatechange = function(){
            if (a.readyState===4){
                document.getElementById(elId).innerHTML = a.responseText;
                if (func != null) {
                    func();
                }
            }
        }
        a.send(null);
    }
}

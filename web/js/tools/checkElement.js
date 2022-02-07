
function editCheckbox(title, val, cb) {
    valCh = "check-act";
    if (val != null && val) {
        valCh = "check-sel_1";
    }
    let cb_1 = "";
    let cb_2 = "";
    if (cb != undefined && cb != null) {
        cb_1 = cb + '(';
         cb_2 = ")";
    }
    let changFirst = '<div style="float:left;margin-top:5px;">'
                +'<div style="font-size:10px;color:#2228">' + title + '</div>'
                +'<img class="_check" onclick="' + cb_1 + 'checkEditCheckbox(this)' + cb_2 + ';" style="cursor:pointer;margin-top:5px;margin-left:14px" width="16" height="16" src="img/' 
                + valCh + '.png">'
            +'</div>';
    return newDOMelement(changFirst);
}

function checkEditCheckbox(el) {
    let check = el.src.indexOf("check-sel") == -1;
    if (check) {
        el.src = "img/check-sel_1.png";
    } else {
        el.src = "img/check-act.png";
    }    
    return check;
}

function checkValue(el) {
    let check = el.querySelector('img');
    return check.src.indexOf("check-sel") != -1;
}
var windTooltip;

// біля елемента target показується повідомлення message на 2 секунди в рамці

function tooltipMessage(target, message) {
    let dv = tooltipMessageOver(target, message);
    setTimeout(function(){ document.body.removeChild(dv);},2000);
}

function tooltipMessageOver(target, message) {
    let maxW = 400;
    let xy = getCoordsEl(target);
    let x = xy.left + 5;
    let y = xy.top;
    let dv = document.createElement('div');
    if (y > 30) {
        y -= 30;
    } else {
        y += 20;
    }
    let wD = document.documentElement.clientWidth;
    if ((wD - x) < maxW) {
        x = wD - maxW - 20;
    }
    dv.style.cssText = "position:absolute;max-width:" + maxW + "px;padding:5px;background:var(--c_content);border:1px solid #ffc700;border-radius:8px;left:" + x + "px;top:" + y + "px;z-index:100";
    dv.innerHTML = message;
    document.body.append(dv);
    windTooltip = dv;
    return dv;
}

function tooltipErrorScreen(target, message) {
    let maxW = 550;
    let xy = getCoordsEl(target);
    let x = xy.left + 5;
    let y = xy.top;
    let dv = document.createElement('div');
    if (y > 30) {
        y -= 30;
    } else {
        y += 20;
    }
    let wD = document.documentElement.clientWidth;
    if ((wD - x) < maxW) {
        x = wD - maxW - 20;
    }
    dv.style.cssText = "position:absolute;max-width:" + maxW + "px;padding:5px;background:var(--c_yelow_lite);border:1px solid #ffc700;border-radius:8px;left:" + x + "px;top:" + y + "px;z-index:100";
    dv.innerHTML = "<pre>" + message + "</pre>";
    document.body.append(dv);
    windTooltip = dv;
    return dv;
}

function tooltipMessageOut(el) {
    if (windTooltip != null) {
        document.body.removeChild(windTooltip);
    }
    windTooltip = null;
}

function tooltipHelpOver(target, message) {
    let maxW = 250;
    let xy = getCoordsEl(target);
    let x = xy.left;
    let y = xy.top;
    let dv = document.createElement('div');
    if (y > 30) {
        y -= 30;
    } else {
        y += 20;
    }
    let wD = document.documentElement.clientWidth;
    if ((wD - x) < maxW) {
        x = wD - maxW - 20;
    }
    dv.style.cssText = "position:absolute;max-width:" + maxW + "px;padding:5px;background:#d5f0ff;border:1px solid #1dace9;border-radius:8px;left:" + x + "px;top:" + y + "px;z-index:100";
    dv.innerHTML = message;
    document.body.append(dv);
    setTimeout(function(){ document.body.removeChild(dv);},2000);
}

function getCoordsEl(elem) { 
    var box = elem.getBoundingClientRect();
    return {
      top: box.top + pageYOffset,
      left: box.left + pageXOffset,
      height: box.height,
      width: box.width
    };
}
// формує перелік безпосередніх дітей елемента el з className = name
function getChildrenByClassName(el, name) {
    let c = el.children;
    let res = [];
    if (c != null && c.length > 0) {
        let ik = c.length;
        for (i = 0; i < ik; i++) {
            let cp = c[i];
            if (cp.className == name) {
                res.push(cp);
            }
        }
        if (res.length > 0) {
            return res;
        } else {
            return null;
        }
    } else {
        return null;
    }
}

function newDOMelement(st) {
    var container = document.createElement('div');
    container.innerHTML = st;
    return container.firstChild
}

function colorStrToRGB(st) {
    let res = {};
    let r1 = charToInt(st.charCodeAt(1));
    let r2 = charToInt(st.charCodeAt(2));
    res.r = r1 * 16 + r2;
    
    let g1 = charToInt(st.charCodeAt(3));
    let g2 = charToInt(st.charCodeAt(4));
    res.g = g1 * 16 + g2;
    
    let b1 = charToInt(st.charCodeAt(5));
    let b2 = charToInt(st.charCodeAt(6));
    res.b = b1 * 16 + b2;
    return res;
}

function charToInt(c) {
    if (c < 58) {
        return c - 48;
    }
    if (c < 71) {
        return c - 55;
    }
    if (c < 103) {
        return c - 87;
    }
    return 0;
}

function checkElement(el) {
    let check = el.src.indexOf("check-sel") == -1;
    if (check) {
        el.src = "img/check-sel_1.png";
    } else {
        el.src = "img/check-act.png";
    }
    return check;
}

function closeWind(el) {
    el.parentElement.parentElement.style.display = "none";
}

function createContour() {
    var container = document.createElement('div');
    container.innerHTML = '<div id="contour" class="contourEl" onmousedown="moveElement(event)"><div class="contourRT" onmousedown="resizeContour(event)"></div>\n\
        <div class="contourLT" onmousedown="resizeContour(event)"></div><div class="contourLB" onmousedown="resizeContour(event)"></div>\n\
        <div class="contourRB" onmousedown="resizeContour(event)"></div></div>';
    return container.firstChild;
}

function daysInMonth(m, y) {//m is 0-based, feb = 1
   return 31 - (m ^ 1? m % 7 & 1:  y & 3? 3: y % 25? 2: y & 15? 3: 2);
}

function readFile(ext, cbResult) {
    let windMenu = formWind(450, 200, 40, 250, "Choose a file");
    let extF = "*.*";
    if (ext != null && ext != "") {
        extF = ext;
    }
    let inputFile = newDOMelement('<input type="file" accept=' + extF + ' style="display: none"/>');
    windMenu.appendChild(inputFile);
    let contInp = newDOMelement('<div style="margin-top:20px;margin-left:16px;"></div');
    windMenu.appendChild(contInp);
    let inputFileView = newDOMelement('<input class="input_style" type="text" readonly style="width: 200px;float:left;height:28px"/>');
    contInp.appendChild(inputFileView);
    let buttonFileView = newDOMelement('<div class="button_blue">'
                +'<div style="text-align:center;margin-top:7px;color:#fff">Choose file</div>'+'</div>');
    contInp.appendChild(buttonFileView);
    buttonFileView.addEventListener("click", function(){inputFile.click();}, true);
    inputFile.addEventListener("change", function(){inputFileView.value = inputFile.files[0].name;}, true);
    
    let txtCheck = '<div style="float:left;clear:both;margin-top:8px;">'
                +'<img class="check_first_line" onclick="checkElement(this);" style="float:left;cursor:pointer;margin-left:14px" width="16" height="16" src="img/check-sel_1.png">'
                +'<div style="float:left;margin-top:3px;margin-left:10px;">The first line of the file contains the names of the table columns<\div>'
            +'</div>'
//    let check_1_line = newDOMelement('<div style="float:left;margin-top:10px">The first line of the file contains the names of the table columns<\div>');
    let check_first = newDOMelement(txtCheck);
    windMenu.append(check_first);
    let el_first = check_first.querySelector(".check_first_line");
    let footer = createFooter(56);
    windMenu.appendChild(footer);
    let buttonSend = createButtonBlue('Import', 70);
    footer.appendChild(buttonSend);
    let buttonCancel = createButtonWeite('Cancel', 70);
    footer.appendChild(buttonCancel);
    buttonSend.addEventListener("click", function(){
        let file = inputFile.files [0];
        let reader = new FileReader()
        reader.onload = function() {
            closeWindow(buttonCancel);
            cbResult.csvParse(reader.result, el_first);
        }
        reader.readAsText(file);
    }, true);
    buttonCancel.addEventListener("click", function(){closeWindow(buttonCancel);}, true);
}

function add0(st) {
    if (st.length == 1) {
        return "0" + st;
    } 
    return st
}



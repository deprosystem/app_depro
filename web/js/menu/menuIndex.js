var listMenu = [];
var stringExportRes;

listMenu[0] = {nameM : 'Images', dependProject : true};
listMenu[0].children = new Array(
        {nameI : 'Upload image', func : uploadImage, dependProject : true},
//        {nameI : 'add field descr_query', func : addDescr, dependProject : true},
//        {nameI : 'add field fields_result', func : addFieldsR, dependProject : true}
                
//        {nameI : 'add field orderBy', func : addOrder, dependProject : true}
    );

        
function formMenuEl() {
    var ik = listMenu.length;
    var menuUL = newUl();
    menuMain.innerHTML = "";
    menuMain.appendChild(menuUL);
    for (var i = 0; i < ik; i++) {
        var menuI = listMenu[i];
        var menuLi = newLi();
        menuUL.appendChild(menuLi);
        var elM = newElM(menuI.nameM);
        menuI.domElement = elM;
        menuLi.appendChild(elM);
        var menuUlSub = newUl();
        menuLi.appendChild(menuUlSub);
        var jk = listMenu[i].children.length;
        if (jk > 0) {
            for (var j = 0; j < jk; j++) {
                var menuIJ = menuI.children[j];
                var menuSubPunkt = newSub(menuIJ.nameI, "subMainMenu");
/*
                if (menuIJ.dependProject && projectId == 0 || menuIJ.dependScreen && screenId == 0) {
                    menuSubPunkt = newSub(menuIJ.nameI, "subMainMenuNo");
                } else {
                    menuSubPunkt = newSub(menuIJ.nameI, "subMainMenu");
                }
*/
                menuIJ.domElement = menuSubPunkt.firstChild;
                menuUlSub.appendChild(menuSubPunkt);
            }
        }
    }
}

function uploadImage() {
    sendImageZip();
}

function addOrder() {
    doServer("POST", "db/addField", cbAddOrder, "");
}

function addDescr() {
    doServer("POST", "db/addDescr", cbAddOrder, "");
}

function addFieldsR() {
    doServer("POST", "db/addFieldsResult", cbAddOrder, "");
}

function cbAddOrder(res) {
    
}

function newElM(name) {
    var container = document.createElement('div')
    container.innerHTML = '<div class="mainMenu">' + name + '</div>'
    return container.firstChild
}

function newSub(name, classPunkt) {
    var container = document.createElement('div')
    container.innerHTML = '<li><div class="' + classPunkt + '" onclick="punct(this)">' + name + '</div></li>';
    return container.firstChild
}

function newUl() {
    var container = document.createElement('div')
    container.innerHTML = '<ul></ul>';
    return container.firstChild
}

function newLi() {
    var container = document.createElement('div')
    container.innerHTML = '<li></li>';
    return container.firstChild
}

function hintShow() {
    if (menuMain.style.display == 'none') {
        hintMenuMain.style.display = 'block';
    }
}

function hintHide() {
    hintMenuMain.style.display = 'none';
}

function punct(el) {
    mainMenuShow();
    var txt = el.innerHTML;
    var ik = listMenu.length;
    var func = null;
    for (var i = 0; i < ik; i++) {
        var jk = listMenu[i].children.length;
        if (jk > 0) {
            for (var j = 0; j < jk; j++) {
                if (txt == listMenu[i].children[j].nameI && listMenu[i].children[j].domElement.className == 'subMainMenu') {
                    func = listMenu[i].children[j].func;
                    break;
                }
            }
        }
        if (func != null) break;
    }
    if (func != null) {
        func();
    }
}

function all() {
    alert('ALL');
}
/*
function changeDevice(v) {
    var i = parseInt(v);
    screenW = sizeDeviceArray[i][0];
    screenH = sizeDeviceArray[i][1];
    setRoot();
}
*/
function scaleMinus() {
    if (currentScale > 50) {
        currentScale -= 10;
        scaleValue.innerHTML = currentScale + '%';
        SCALE = currentScale / 100;
        MEASURE = DENSITY * SCALE;
        setDp();
        setScreenView();
//        setRoot();
//        changeRoot()
        content_src.scroll_y.resize(content_src);
    }
}

function scalePlus() {
    if (currentScale < 200) {
        currentScale += 10;
        scaleValue.innerHTML = currentScale + '%';
        SCALE = currentScale / 100;
        MEASURE = DENSITY * SCALE;
        setDp();
        setScreenView();
//        setRoot();
//        changeRoot()
        content_src.scroll_y.resize(content_src);
    }
}
/*
function changeRoot() {
    var child = root.childNodes;
    var ik = child.length;
    for (var i = 0; i < ik; i++) {
        var el = child[i];
        if (el.android != null) {
            viewComponElem(el);
        }
    }
}
*/

function menuhide(el) {
    var currentmenu = el.getElementsByClassName("navbody")[0];
    currentmenu.style.visibility = 'hidden';
}

function menushow(el) {
    var currentmenu = el.getElementsByClassName("navbody")[0];
    currentmenu.style.visibility = 'visible';
}

function mainMenuShow() {
    if (hamburger1.className == "hamburger1") {
        hamburger1.className = 'hamburger1_open';
        hamburger2.style.display = 'none';
        hamburger3.className = 'hamburger3_open';
        menuMain.style.display = "block";
    } else {
        hamburger1.className = 'hamburger1';
        hamburger2.style.display = 'inline-block';
        hamburger3.className = 'hamburger3';
        menuMain.style.display = "none";
    }
}
var backPathImg, //     ikon
        stringPathImg, stringFolder, viewPathImg;
var viewDataImg, viewDataGal, listFileImg, preViewImage, preViewGal, pathFileImg;
var listEditGal, positionEditGal;
var selectFileIMG, selectFilePosition;
var timeoutDBLclickIMG;
var viewElementImg;
var localURL= "";

function sendImageFile(nameTable, nameField, el) {
    let windMenu = formWind(350, 220, 40, 250, "Choose a file with an image");
    let hidesFooter, hidesInput, inputLink;
    let inputFile = newDOMelement('<input type="file" accept="image/*" name="imgFile" multiple style="display: none"/>');
    windMenu.appendChild(inputFile);
    let contInp = newDOMelement('<div style="margin-top:20px;margin-left:15px;"></div');
    windMenu.appendChild(contInp);
    let inputFileView = newElementFromString('<input class="input_style" type="text" readonly style="width: 200px;float:left;height:28px"/>');
    contInp.appendChild(inputFileView);
    let buttonFileView = newElementFromString('<div class="button_blue">'
                +'<div style="text-align:center;margin-top:7px;color:#fff">My computer</div>'+'</div>');
    contInp.appendChild(buttonFileView);
    buttonFileView.addEventListener("click", function(){hidesFooter.style.display = "none"; hidesInput.style.display = "none"; inputFile.click();}, true);
    inputFile.addEventListener("change", function(){inputFileView.value = inputFile.files[0].name;}, true);
    
    let changeNameFile = editCheckbox("Change file name", false);
    changeNameFile.style.clear = "both";
    changeNameFile.style.marginLeft = "15px";
    windMenu.appendChild(changeNameFile);
    
    let footer = createFooter(56);
    windMenu.appendChild(footer);
    
    let buttonSend = createButtonBlue('Send', 70);
    footer.appendChild(buttonSend);
    let buttonCancel = createButtonWeite('Cancel', 70);
    footer.appendChild(buttonCancel);
    hidesFooter = newDOMelement('<div style="position:absolute;left:0;right:0;bottom:5px;height:100px;background-color:#fff"></div>');
    hidesInput = newDOMelement('<div style="position:absolute;left:0;top:0;height:100px;width:230px;background-color:#fff"></div>');
    let buttonServer = createButtonBlue('Server', 100);
    buttonServer.style.marginTop = "20px";
    hidesInput.appendChild(buttonServer);
    buttonServer.addEventListener("click", function(){closeWindow(buttonCancel); getImageServer(el);}, true);
    windMenu.appendChild(hidesFooter);
    windMenu.appendChild(hidesInput);
    
    inputLink = newDOMelement('<div style="position:absolute;left:0;right:0;top:70px;height:50px;border-top:1px solid #1dace9";background-color:#fee"></div>');
    inputLink.append(newDOMelement('<div style="margin-left:10px;margin-top:5px;color: #8199A5;font-size: 10px;">Link</div>'));
    let inpEl = newDOMelement('<input class="input_style" type="text" style="width: 235px;float:left;clear:both;height:28px;margin-top:3px;margin-left:10px"/>');
    inputLink.append(inpEl);
    let buttonLink = createButtonBlue('Save link', 70);
    buttonLink.style.marginTop = "4px";
    buttonLink.style.marginLeft = "10px";
    buttonLink.addEventListener("click", function(){setLink(inpEl, el);}, true);
    inputLink.append(buttonLink);
//    buttonLink.style.
    windMenu.append(inputLink);
    
    let paramForCB = {wind:windMenu, elImg:el};
    buttonSend.addEventListener("click", function(){
        let formData = new FormData ();
        formData.append ("nameTable", nameTable);
        formData.append ("nameField", nameField);
        formData.append ("changeName", checkValue(changeNameFile));
        let listFiles = inputFile.files;
        let ik = listFiles.length;
        for (let i = 0; i < ik; i++) {
            formData.append ("file_" + i, inputFile.files [i]);
        }
        doServer("POST", "images/save", cbSendImageFile, formData, paramForCB, windMenu);
    }, true);
    buttonCancel.addEventListener("click", function(){closeWindow(buttonCancel);}, true);
}
// Привязка названия файла изображения к типу и названию компонента ????
function setLink(inp, el) {
    if (inp.value != null && inp.value != "") {
        setImageInEl(el, inp.value);
        closeWindow(inp);
    }
}

function editGalleryFile(nameTable, nameField, el) {
    let hFooter = 56;
    let wImgs = 510;
    let windMenu = formWind(700, 350, 40, 250, "Editing images in the gallery");
    backPathImg = newDOMelement('<IMG SRC="img/arrow_left.png" style="margin-left:10px;float:left;width:14px;height:14px;margin-top:5px;cursor:pointer">');
    backPathImg.addEventListener("click", function(){backPath();}, true);
    let wind = newDOMelement('<div style="position:absolute;top:0px;left:0;bottom:' + (hFooter +1) + 'px;right:0"></div>');
    windMenu.appendChild(wind);
    let listImg = newDOMelement('<div style="position:absolute;top:0;left:0;bottom:0;width:' + wImgs + 'px"></div>');
    wind.appendChild(listImg);
    preViewGal = newDOMelement('<IMG style="position:absolute;top:20px;width:180px;right:5px;height:100px;object-fit:contain">');
    wind.appendChild(preViewGal);
    let scrollImg = formViewScrolY(listImg);
    viewDataGal = scrollImg.getElementsByClassName("viewData")[0];
    
    let footer = createFooter(hFooter);
    windMenu.appendChild(footer);
    let buttonSend = createButtonBlue('Save', 70);
    footer.appendChild(buttonSend);
    let buttonCancel = createButtonWeite('Cancel', 70);
    footer.appendChild(buttonCancel);
    let buttonDelete = createButtonWeite('Delete', 70);
    footer.appendChild(buttonDelete);
    buttonSend.addEventListener("click", function(){saveGal(el);closeWindow(buttonSend);}, true);
    buttonDelete.addEventListener("click", function(){deleteGal()}, true);
    buttonCancel.addEventListener("click", function(){closeWindow(buttonCancel);}, true);
    listEditGal = el.adrImg.toString().split(",");
    if (isLocalHost) {
        localURL = "https://deb-apps.dp-ide.com/";
    }
    setListGal();
    positionEditGal = 0;
    preViewGal.src = localURL + listEditGal[0];
}

function setListGal() {
    let ik = listEditGal.length;
    if (listEditGal == null || listEditGal.length == 0) {
        viewDataImg.innerHTML = '<div style="font-size:20px;margin-left:20px">No images</div>';
        return;
    }
    viewDataGal.innerHTML = "";
    for (let i = 0; i < ik; i++) {
        let line = newDOMelement('<div style="height:24px;float:left;width:100%">' + listEditGal[i] + '</div>');
        line.addEventListener("click", function(){selectLineGal(i)}, true);
        viewDataGal.append(line);
    }
}

function saveGal(el) {
    el.adrImg = listEditGal.toString().split(",");
    let cell = el.closest('.col');
    cell.isEdit = true;
    let row = cell.closest('.row');
    row.isEdit = true;
    if ( ! row.newRecord) {
        setStatus(row.numRow, 1);
    }
}

function deleteGal() {
    listEditGal.splice(positionEditGal, 1);
    setListGal();
    
//    viewDataGal.children[positionEditGal].remove();
    let ik = listEditGal.length - 1;
    if (ik > -1) {
        if (positionEditGal > ik) {
            positionEditGal = ik;
        }
        viewDataGal.children[positionEditGal].style.backgroundColor = "#DAF0FA";
        preViewGal.src = localURL + listEditGal[positionEditGal];
    }
}

function selectLineGal(i) {
    viewDataGal.children[positionEditGal].style.backgroundColor = "";
    positionEditGal = i;
    viewDataGal.children[positionEditGal].style.backgroundColor = "#DAF0FA";
    preViewGal.src = localURL + listEditGal[i];
}

function sendImageZip() {
    let windMenu = formWind(350, 300, 40, 250, "Choose a file with an image");
    windMenu.typeProgr = "upload";
    let inputFile = newDOMelement('<input type="file" accept=".zip,image/*" name="imgFile" multiple style="display: none"/>');
    windMenu.appendChild(inputFile);
    let contInp = newDOMelement('<div style="margin-top:20px;margin-left:15px;"></div');
    windMenu.appendChild(contInp);
    let inputFileView = newElementFromString('<input class="input_style" type="text" readonly style="width: 200px;float:left;height:28px"/>');
    contInp.appendChild(inputFileView);
    let buttonFileView = newElementFromString('<div class="button_blue">'
                +'<div style="text-align:center;margin-top:7px;color:#fff">Choose file</div>'+'</div>');
    contInp.appendChild(buttonFileView);
    buttonFileView.addEventListener("click", function(){inputFile.click();}, true);
    inputFile.addEventListener("change", function(){inputFileView.value = inputFile.files[0].name;}, true);
    
    let footer = createFooter(56);
    windMenu.appendChild(footer);
    let buttonSend = createButtonBlue('Send', 70);
    footer.appendChild(buttonSend);
    let buttonCancel = createButtonWeite('Cancel', 70);
    footer.appendChild(buttonCancel);
    let paramForCB = {wind:windMenu};
    buttonSend.addEventListener("click", function(){
        let formData = new FormData();
        formData.append ("changeName", "false");
        let listFiles = inputFile.files;
        let ik = listFiles.length;
        for (let i = 0; i < ik; i++) {
            formData.append ("file_" + i, inputFile.files [i]);
        }
        doServer("POST", "images/save", cbSendImageZip, formData, paramForCB, windMenu);
    }, true);
    buttonCancel.addEventListener("click", function(){closeWindow(buttonCancel);}, true);
}

function cbSendImageZip(res, par) {
    closeWindow(par.wind);
}

function cbSendImageFile(res, par) {
    closeWindow(par.wind);
    listImage = null;
    if (par.elImg != null) {
        let el = par.elImg;
        let adr = JSON.parse(res);
        setImageInEl(el, adr.img);
    }
}

function setImageInEl(el, adr) {
    if (el.typeEl == "gallery") {
        el.src = adr;
        if (el.adrImg == null) {
            el.adrImg = [];
        }
        el.adrImg.push(adr);
    } else {
        el.src = adr;
        el.adrImg = adr;
    }
    let cell = el.closest('.col');
    cell.isEdit = true;
    let row = cell.closest('.row');
    row.isEdit = true;
    if ( ! row.newRecord) {
        setStatus(row.numRow, 1);
    }
}

function setStatus(j, stat) {
    let numJ = dataNumb.children[j];
    let statV = numJ.querySelector('.status');
    statV.style.backgroundColor = colorsStatus[stat];
}

function getImageServer(el) {
    viewElementImg = el;
    stringPathImg = "";
    let hFooter = 56;
    let wImgs = 510;
    let hControl = 24;
    let windMenu = formWind(700, 350, 40, 250, "Select an image on the server");
    let control = newDOMelement('<div style="position:absolute;top:0;left:0;right:0;height:' + hControl + 'px;border-bottom:1px solid #1dace9"></div>');
    backPathImg = newDOMelement('<IMG SRC="img/arrow_left.png" style="margin-left:10px;float:left;width:14px;height:14px;margin-top:5px;cursor:pointer">');
    backPathImg.addEventListener("click", function(){backPath();}, true);
    control.appendChild(backPathImg);
    viewPathImg = newDOMelement('<div style="margin-left:10px;float:left;margin-top:3px;font-size:15px"></div>');
    control.appendChild(viewPathImg);
    windMenu.appendChild(control);
    let wind = newDOMelement('<div style="position:absolute;top:' + (hControl + 1) + 'px;left:0;bottom:' + (hFooter +1) + 'px;right:0"></div>');
    windMenu.appendChild(wind);
    let listImg = newDOMelement('<div style="position:absolute;top:0;left:0;bottom:0;width:' + wImgs + 'px"></div>');
    wind.appendChild(listImg);
    preViewImage = newDOMelement('<IMG style="position:absolute;top:20px;width:180px;right:5px;height:100px;object-fit:contain">');
    wind.appendChild(preViewImage);
    let scrollImg = formViewScrolY(listImg);
    viewDataImg = scrollImg.getElementsByClassName("viewData")[0];
    
    let footer = createFooter(hFooter);
    windMenu.appendChild(footer);
    let buttonSend = createButtonBlue('Choose', 70);
    footer.appendChild(buttonSend);
    let buttonCancel = createButtonWeite('Cancel', 70);
    footer.appendChild(buttonCancel);
    buttonSend.addEventListener("click", function(){selectFileImage(el, selectFilePosition)}, true);
    buttonCancel.addEventListener("click", function(){closeWindow(buttonCancel);}, true);
    getListImg();
}

function getListImg() {
    doServer("GET", "images/list", cbListImg);
}

function cbListImg(res) {
    res1 = JSON.parse(res);
    pathFileImg = res1.dir;
    listFileImg = res1.list;
    if (listFileImg == null || listFileImg.length == 0) {
        viewDataImg.innerHTML = '<div style="font-size:20px;margin-left:20px">No images</div>';
        return;
    }
    viewDataImg.innerHTML = "";
    let listener = [{id:"view_0",event:"click",func:clickViewImg},{id:"view_0",event:"dblclick",func:selectFileImage},{
            id:"view_1",event:"dblclick",func:selectFoldeImage}];
    showList(listFileImg, createItem, viewDataImg, listener, "type");
    let scr = viewDataImg.closest('.viewport');
    scr.scroll_y.resize();
    setFirstFile();
}

function selectFoldeImage(el, i) {
    let item = listFileImg[i];
    stringFolder = item.name + "/";
    doServer("GET", "images/list?dir=" + stringPathImg + stringFolder, cbListFolderImg);
}

function cbListFolderImg(res) {
    stringPathImg += stringFolder;
    viewPathImg.innerHTML = stringPathImg;
    cbListImg(res);
}

function setFirstFile() {
    let ik = listFileImg.length;
    selectFilePosition = -1;
    selectFileIMG = null;
    for (let i = 0; i < ik; i++) {
        if (listFileImg[i].type == 0) {
            let ch = viewDataImg.children;
            fileViewImg(ch[i], i);
            break;
        }
    }
}

function clickViewImg(el, i) {
    if (! timeoutDBLclickIMG) {
        timeoutDBLclickIMG = setTimeout(function(){ fileViewImg(el, i); }, 200);
    }
}

function fileViewImg(el, i) {
    timeoutDBLclickIMG = null;
    let item = listFileImg[i];
    if (selectFileIMG != null) {
        selectFileIMG.style.backgroundColor = "";
    }
    selectFileIMG = el;
    selectFilePosition = i;
    selectFileIMG.style.backgroundColor = "#DAF0FA";
    preViewImage.src = "img_app/" + schema + "/" + stringPathImg + item.name;
}

function selectFileImage(el, i) {
    clearTimeout(timeoutDBLclickIMG);
    timeoutDBLclickIMG = null;
    let item = listFileImg[i];
    setImageInEl(viewElementImg, "img_app/" + schema + "/" + stringPathImg + item.name);
    closeWindow(viewDataImg);
}

function createItem(i) {
    let view_0 = '<div id="view_0" class="field" style="height:24px;width:100%;"><div id="name" class="field" style="float:left;margin-top:5px;margin-left:20px;width:340px;overflow: hidden"></div>'
        +'<div id="date" class="field date" style="float:left;margin-top:5px;margin-left:2px;"></div>'
        +'<div id="size" class="field" style="float:right;margin-top:5px;margin-right:2px;overflow: hidden"></div>'
        +'</div>';
    let view_1 = '<div id="view_1" style="height:24px;width:100%;">'
        +'<img src="img/folder.png" height="14" width="14" style="float: left;margin-left:5px;margin-top:5px"/>'
        +'<div id="name" class="field" style="float:left;margin-top:5px;margin-left:3px;width:340px;overflow: hidden"></div>'
        +'</div>';
    let vv;
    if (i == 0) {
        vv = newDOMelement(view_0);
        let dd = vv.querySelector('.date');
        dd.format = "dd.MM.yy hh:mm";
    } else {
        vv = newDOMelement(view_1);
    }
    return vv;
}

function backPath() {
    if (stringPathImg != "") {
        let i = stringPathImg.lastIndexOf("/", stringPathImg.length - 2);
        stringFolder = "";
        if (i == -1) {
            stringPathImg = "";
            viewPathImg.innerHTML = "";
            doServer("GET", "images/list", cbListFolderImg);
        } else {
            stringPathImg = stringPathImg.substring(0, i + 1);
            viewPathImg.innerHTML = stringPathImg;
            doServer("GET", "images/list?dir=" + stringPathImg, cbListFolderImg);
        }
    }
}

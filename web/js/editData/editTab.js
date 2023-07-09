function EditTable(meta, data, domEl, dataTitle, dataNumb, footer, obrSave) {
    let edData, edDomEl;
    let edObrSave = obrSave;
    let widthTabl;
    let lineChangeWidthCell;
    let addControl, delControl, saveControl;
    let hTool = 50;
    let countRows;
    let imgSetValue;
    let tabViewport;
    let scrollVert;
    let widthTable;
    let hCells = heightCells;
    let selectRows = [];
    let mouseX, offsetDivider;
    let widthSeparator = 3;
    let primaryKayName;
    let primaryKayNumber;
    let funcMove, funcUp;   // что бы удалить обработчики с анонимными функциями
    let  colorSelect = "#f3f8ff", colorNew = "#f5f9ff", 
            colorDel, colorErrorTr, colorErrorTh;
    if (meta == null) {
        return null;
    };
    let offset = new Date().getTimezoneOffset() * 60000;
    let self = this;
    self.edMeta = meta.description;
    let ikM = self.edMeta.length;
    if (domEl == null) {
        return null;
    }
    edDomEl = domEl;
    if (data == null) {
        return null;
    }
    let wind = edDomEl.parentElement.parentElement.parentElement;
    edData = data;
    
    let ikD;
    
    addControl = newDOMelement('<IMG SRC="img/add_blue.png" style="margin-left:20px;float:left;width:20px;margin-top:5px;cursor:pointer">');
    addControl.addEventListener("click", function(event){addItem(event)}, true);
    let csvImport = newDOMelement('<IMG SRC="img/import_csv.png" style="margin-left:20px;float:left;width:20px;margin-top:5px;cursor:pointer">');
    csvImport.addEventListener("click", function(event){addCsv(event)}, true);
    let csvEksport = newDOMelement('<IMG SRC="img/eksport_csv.png" style="margin-left:20px;float:left;width:20px;margin-top:5px;cursor:pointer">');
    csvEksport.addEventListener("click", function(event){exportCsv(event)}, true);
    delControl = newDOMelement('<IMG SRC="img/del_blue.png" style="margin-left:20px;float:left;width:20px;margin-top:5px;cursor:pointer">');
    delControl.addEventListener("click", function(event){delRow(event)}, true);
    saveControl = newDOMelement('<IMG SRC="img/save_db.png" style="margin-left:20px;float:left;width:20px;margin-top:5px;cursor:pointer">');
    saveControl.addEventListener("click", function(event){saveData(event)}, true);
    footer.appendChild(addControl);
    footer.appendChild(delControl);
    footer.appendChild(csvImport);
    footer.appendChild(csvEksport);
    footer.appendChild(saveControl);
    
    function formEditTab() {
        ikD = edData.length;
        let row;
        let col;

        lineChangeWidthCell = newDOMelement('<div style="width:1px;height:100%;position:absolute;display:none;background-color:#222"></div>');
        wind.appendChild(lineChangeWidthCell);
        widthTable = 0;
        let divTitle = newDOMelement('<div style="float:left;height:' + (heightTitle - 1) + 'px;border-bottom:1px solid #C5DCFA;"></div>');
        dataTitle.appendChild(divTitle);
        primaryKayName = "";
        for (let i = 0; i < ikM; i++) {
            dv = document.createElement('div');
            let item = self.edMeta[i];
            dv.innerHTML = item.title;
            let len = item.length;
            switch (item.type) {
                case "Select":
                case "Text":
                    if (len != null && len.length > 0) {
                        item.lenTab = len * 5;
                    } else {
                        item.lenTab = 170;
                    }
                    break;
                case "Bigserial":
                case "Serial":
                    if (item.key && primaryKayName.length == 0) {
                        primaryKayName = item.name;
                        primaryKayNumber = i;
                    }
                    item.lenTab = 40;
                    break;
                case "Int":
                    item.lenTab = 40;
                    break;
                case "Long":
                    item.lenTab = 50;
                    break;
                case "Timestamp":
                    if (item.name.indexOf("__") == 0) {
                        item.lenTab = 125;
                    } else {
                        item.lenTab = 180;
                    }
                    break;
                case "Time":
                    if (item.name.indexOf("__") == 0) {
                        item.lenTab = 100;
                    } else {
                        item.lenTab = 70;
                    }
                    break;
                case "Date":
                    if (item.name.indexOf("__") == 0) {
                        item.lenTab = 85;
                    } else {
                        item.lenTab = 120;
                    }
                    break;
                case "Float":
                    item.lenTab = 50;
                    break;
                case "Double":
                    item.lenTab = 60;
                    break;
                case "Gallery":
                case "Img":
                    item.lenTab = 60;
                    hCells = 40;
                    break;
                case "Check":
                case "Switch":
                case "Boolean":
                    item.lenTab = 60;
                    break;
            }
            dv.style.cssText = "float:left;margin-top:7px;text-align: center;font-weight:600;width:" + item.lenTab + "px;overflow:hidden;";
            widthTable += item.lenTab + widthSeparator + 1;
            divTitle.appendChild(dv);
            let divider = newDOMelement('<div style="float:left;width:' + widthSeparator + 'px;height:' 
                    + heightTitle + 'px;border-right:1px solid #C5DCFA;cursor:col-resize"></div>');
            divTitle.appendChild(divider);
            divider.addEventListener("mousedown", function(event){chWidthCell(event)}, true);
        }
        if (ikD > 0) {
            let firstElem;
            for (let j = 0; j < ikD; j++) {
                let item = edData[j];
                row = document.createElement('div');
                row.className = "row";
                row.numRow = j;
                row.style.cssText = "float:left;clear:both;position:relative;height:" + hCells + "px;border-bottom:1px solid #C5DCFA;";
                row.addEventListener("mouseover", function(event){mouseoverTr(event)}, true);
                row.addEventListener("mouseout", function(event){mouseoutTr(event)}, true);
                createCellNum(j);
                for (let i = 0; i < ikM; i++) {
                    let met = self.edMeta[i];
                    col = createCol(i, met, item[met.name]);
//                    col = createCol(i, item[met.name]);
                    if (firstElem == null) {
                        let ff = col.getElementsByTagName("input");
                        if ( ff != null) {
                            firstElem = ff[0];
                        }
                    }
                    if (col.primaryK != null) {
                        row.primaryK = col.primaryK;
                    }
                    row.appendChild(col);
                    let divider = newDOMelement('<div style="float:left;width:3px;height:' 
                            + hCells + 'px;border-right:1px solid #C5DCFA;"></div>');
                    row.appendChild(divider);
                }
                edDomEl.appendChild(row);
                if (firstElem != null) {
                    firstElem.focus();
                }
            }
        } else {
            createNewRow();
        }
        
        edDomEl.style.width = (widthTable + 5) + 'px';
        dataTitle.style.width = (widthTable + 5) + 'px';
    }
    
    this.setScrolls = function (scrollY, scrollX) {
        scrollVert = scrollY;
    }

    this.getWidthW = function() {
        return widthTable;
    };
    
    function chWidthCell(e) {
        el = e.target;
        mouseX = e.clientX;
        offsetDivider = el.offsetLeft + widthNum + 3;
        lineChangeWidthCell.style.left = offsetDivider + "px";
        lineChangeWidthCell.style.display = "block";
        
        wind.addEventListener("mousemove", funcMove = function(event){chMoveDivider(event)}, true);
        wind.addEventListener("mouseup", funcUp = function(event){chMouseUp(event)}, true);
    }
    
    function chMoveDivider(e) {
        lineChangeWidthCell.style.left = (offsetDivider - mouseX + e.clientX) + "px";
    }
    
    function chMouseUp(e) {
        lineChangeWidthCell.style.display = "none";
        wind.removeEventListener("mousemove", funcMove, true);
        wind.removeEventListener("mouseup", funcUp, true);
    }
    
    function mouseoverTr(event) {
        hoverTr(event, "#f3f7ff");
    }
    
    function mouseoutTr(event) {
        hoverTr(event, "");
    }
    
    function addCsv(e) {
        readFile(".csv", self);
//        readFile(".csv", csvParse);
    }
    
    function exportCsv(e) {
        let windMenu = formWind(250, 300, 40, 250, "Formation of csv");
        let fileCreate = document.createElement("div");
        fileCreate.style.cssText = "text-align:center; margin-top:20px;";
        fileCreate.innerHTML = "formation of csv";
        windMenu.appendChild(fileCreate);
        let buttSave = createButtonBlue("Save", 80);
        buttSave.style.position = "relative";
        buttSave.style.marginTop = "25px";
        buttSave.className = "save-csv";
        windMenu.appendChild(buttSave);
        doServer("GET", "tables/export?name_table=" + meta.name_table, cbExportCsv, null, windMenu, windMenu);
    }

    function cbExportCsv(res, wind) {
        let save = wind.getElementsByClassName("save-csv")[0];
        if (save != null) {
            save.appendChild(newDOMelement('<a href="' + res + 
                    '" download style="text-decoration: none;color:#fff0;display:inline-block;width:100%;height:100%;position:absolute;top:0;left:0"> </a>'));
        }
        save.addEventListener("click", function(){closeWindow(save);}, true);
    }
    
    self.csvParse = function (strCSV, elFirst) {
        let first = false;
        let col;
        if (elFirst != null && elFirst.src.indexOf("check-sel") > -1) {
            first = true;
        }
        let listFieldTab = [];
        let listFieldCSV;
        let arRow = strCSV.split(/\r?\n|\r/);
        let ik = arRow.length - 1;
        let listNoField = "";
        let beginCsv;
        if (first) {
            beginCsv = 1;
            listFieldCSV = arRow[0].split(";");
            let jk = listFieldCSV.length;
            let countCSV = 0;;
            for (let i = 0; i < ikM; i++) {
                let item = self.edMeta[i];
                listFieldTab.push(item.name);
                let nn = item.name;
                let num = -1;
                for (let j = 0; j < jk; j++) {
                    if (listFieldCSV[j] == nn) {
                        num = j;
                        countCSV ++;
                        break;
                    }
                }
                listFieldTab[i] = num;
            }
        } else {
            beginCsv = 0;
            for (let i = 0; i < ikM; i++) {
                listFieldTab[i] = i;
            }
        }
        let lastRow;
        let ch = edDomEl.children;
        if (ch != null && ch.length > 0) {
            lastRow = ch[ch.length - 1];
        }
        if (lastRow != null) {
            if (lastRow.newRecord && ! lastRow.isEdit) {
                lastRow.remove();
                let chNumb = dataNumb.children;
                let lastNumb = chNumb[chNumb.length - 1];
                lastNumb.remove();
            }
        }
        ik = arRow.length - 1;
        let num = 0;
        if (edDomEl.children != null) {
            num = edDomEl.children.length;
        }
        for (let i = beginCsv; i < ik; i++) {
            let item = formItemCSV(arRow[i]);
            let itemLen = item.length;
            let row = document.createElement('div');
            row.className = "row";
            row.numRow = i + num - beginCsv;
            row.newRecord = true;
            row.isEdit = true;
            row.style.cssText = "float:left;clear:both;position:relative;height:" + hCells + "px;border-bottom:1px solid #C5DCFA;";
            row.addEventListener("mouseover", function(event){mouseoverTr(event)}, true);
            row.addEventListener("mouseout", function(event){mouseoutTr(event)}, true);
            createCellNum(i + num - beginCsv);
            setStatus(i + num - beginCsv, 2);
            let edM = meta.description;
            for (let k = 0; k < ikM; k++) {
                let j = listFieldTab[k];
                let met = edM[k];
                let vv = null;
                if (j < itemLen) {
                    vv = item[j];
                    if (met.type == "Gallery") {
                        if ( ! vv.startsWith("[")) {
                            let arVV = vv.split(",");
                            vv = JSON.stringify(arVV);
                        }
                    }
                }
                col = createCol(j, met, vv);
                col.isEdit = true;
                if (col.primaryK != null) {
                    row.primaryK = col.primaryK;
                }
                row.appendChild(col);
                let divider = newDOMelement('<div style="float:left;width:3px;height:' 
                        + hCells + 'px;border-right:1px solid #C5DCFA;"></div>');
                row.appendChild(divider);
            }
            edDomEl.appendChild(row);
        }
        scrollVert.resize();
    }
    
    function formItemCSV(oneRow) {
        let item = [];
        let lenRow = oneRow.length;
        let val;
        let j = 0;
        let k;
        let sepStr = '";';
        while (j < lenRow) {
            if (oneRow[j] == '"') {     // field is String
                noEnd = true;
                let jc = j + 1;
                while (noEnd) {
                    k = oneRow.indexOf(sepStr, jc);
                    if (k > -1) {
                        if ( oneRow[k - 1] != '"') {
                            val = oneRow.substring(j + 1, k);
                        } else {
                            jc = k + 2;
                            continue;
                        }
                    } else {
                        val = oneRow.substring(j + 1, lenRow - 1);
                    }
                    noEnd = false;
                    let z, x;
                    let isQu = true;
                    let res = "";
                    let lenVal = val.length;

                    while (isQu) {
                        x = val.indexOf('""', z);
                        if (x >-1) {
                            res += val.substring(z, x + 1);
                            z = x + 2;
                        } else {
                            res += val.substring(z, lenVal);
                            isQu = false;
                        }
                    }
                    val = res;
                    if (k != -1) {
                        j = k + 2;
                    } else {
                        j = lenRow;
                    }
                    item.push(val);
                };
            } else {            // field is Number
                k = oneRow.indexOf(';', j);
                if (k > -1) {
                    val = oneRow.substring(j, k);
                } else {
                    val = oneRow.substring(j, lenRow);
                }
                val = val.replace('""', '"');
                if (k != -1) {
                    j = k + 1;
                } else {
                    j = lenRow;
                }
                item.push(val);
            }
        }
        return item;
    }
     
    function delRow(e) {
        if (selectRows.length > 0) {
            let row = edDomEl.children[selectRows[0]];
            let del = row.getElementsByClassName("del");
            if (del != null && del.length > 0) {
                row.isDel = false;
                del[0].remove();
            } else {
                row.isDel = true;
                let delEl = newDOMelement('<div style="left:0;right:0;height:2px;background-color:#f99;top:10px;position:absolute"></div>');
                delEl.className = "del";
                row.appendChild(delEl);
            }
        }
    }
    
    function hoverTr(event, color) {
        let row = event.currentTarget;
        let cells = row.getElementsByClassName("col");
        let ik = cells.length;
        for (let i = 0; i < ik; i++) {
            cells[i].style.backgroundColor = color;
        }
    }
    
    function createCellNum(j) {
        let dN = document.createElement('div');
        dN.numRow = j;
        dN.className = "NNN_" + j;
        dN.addEventListener("click", function(event){selectRow(event)}, true);
        dN.style.cssText = "width:" + (widthNum - 1) + "px;height:" + hCells + "px;float:left;clear:both;border-bottom:1px solid #C5DCFA;border-right:1px solid #C5DCFA;position:relative";
        let dvStatus = newDOMelement('<div class="status" style="width:3px;position:absolute;top:0;bottom:0;left:0"></div>');
        dN.appendChild(dvStatus);
        let dv1 = document.createElement('div');
        dv1.style.cssText = "text-align:right;margin-top:3px;margin-right:3px;";
        dv1.innerHTML = j;
        dN.appendChild(dv1);
        dataNumb.appendChild(dN);
    }
    
    function setStatus(j, stat) {
        let numJ = dataNumb.children[j];
        let statV = numJ.querySelector('.status');
        statV.style.backgroundColor = colorsStatus[stat];
    }
    
    function selectRow(e) {
        let cellNum = e.currentTarget;
        if (selectRows.length > 0) {
            let oldSel = dataNumb.children[selectRows[0]];
            oldSel.style.backgroundColor = "";
            selectRows.length = 0;
        } 
        selectRows[0] = cellNum.numRow;
        cellNum.style.backgroundColor = "#bbf";
    }
    
    function  createCol(i, met, vv) {
        let td = document.createElement('div');
        td.className = "col";
        td.numField = i;
        td.style.cssText = "float:left;width:" + met.lenTab + "px;height:" + hCells + "px;";
        if (met.type == null) {
            met.type = Text;
        }
        let inp;
        let img;
        if (met.name.indexOf("__") == 0) {
            inp = document.createElement('div');
            inp.type = "user";
            if (vv != null) {
                let vv_1 = Number(vv);
                switch (met.type) {
                    case "Timestamp":
                        vv_1 = new Date(vv_1);
                        vv_1 = vv_1.toLocaleString();
                        break;
                    case "Date":
                        vv_1 = new Date(vv_1);
                        vv_1 = vv_1.toLocaleDateString();
                        break;
                    case "Time":
                        vv_1 = new Date(vv_1);
                        vv_1 = vv_1.toLocaleTimeString();
                        break;
                }
                inp.innerHTML = vv_1;
            } else {
                inp.innerHTML = "";
            }
            inp.name = met.name;
            inp.style.cssText = "margin-left:3px;width:" + (met.lenTab - 6) + "px;height:" + hCells + "px;text-align:right";
            td.appendChild(inp);
        } else {
            switch (met.type) {
                case "Boolean":
                    inp = document.createElement('input');
                    inp.type = "checkbox";
                    inp.name = met.name;
                    if (vv != null) {
                        inp.checked = vv;
                    } else {
                        inp.checked = false;
                    }
                    if (met.marg != null) {
                        inp.style.marginLeft = met.marg + "px";
                        inp.style.marginRight = met.marg + "px";
                    }
                    inp.style.backgroundColor = "#0000";
                    inp.addEventListener('change', function(event){setFlagEdit(event)}, false);
                    td.appendChild(inp);
                    break;
                case "Bigserial":
                case "Serial":
                    inp = document.createElement('div');
                    inp.type = "serial";
                    if (vv != null) {
                        inp.innerHTML = vv;
                        td.primaryK = vv;
                    } else {
                        inp.innerHTML = "";
                        td.primaryK = "";
                    }
                    inp.name = met.name;
                    inp.style.cssText = "margin-left:3px;width:" + (met.lenTab - 6) + "px;height:" + hCells + "px;text-align:right";
                    td.appendChild(inp);
                    break;
                case "Select":
                    if (met.select != null && met.select.length > 0) {
                        td.appendChild(setSelect(met, vv));
                    }
                    break;
                case "Gallery":
                    img = newDOMelement('<img style="width:100%;height:100%;cursor:pointer">');
                    if (vv != null && vv != "") {
                        let vv_1 = JSON.parse(vv);
                        img.adrImg = vv_1;
                        img.src = vv_1[vv_1.length - 1];
                    }
                    img.typeEl = "gallery";
                    img.addEventListener('click', function(event){selecktImgFile(img)}, false);
                    if (vv != null && vv != "") {
                        img.addEventListener("contextmenu", function(){event.preventDefault();event.stopPropagation();editGallery(img);return false;}, false);
                    }
                    td.appendChild(img);
                    break;
                case "Img":
                    img = newDOMelement('<img style="width:100%;height:100%;cursor:pointer">');
                    if (vv != null && vv != "") {
                        img.src = vv;
                        img.srcElem = vv;
                        img.adrImg = vv;
                    }
                    img.typeEl = "img";
                    img.addEventListener('click', function(event){selecktImgFile(img)}, false);
    //                img.addEventListener('contextmenu', function(event){selecktImgServer(event)}, false);
                    td.appendChild(img);
                    break;
                case "Timestamp":
                    inp = newDOMelement('<input type="datetime-local" style="margin-left:3px;border:none;width:' + (met.lenTab - 6) + 'px;"/>');
                    if (vv != null && vv != 0) {
                        inp.valueAsNumber = vv;
                    } else {
                        inp.value = "";
                    }
                    inp.addEventListener('change', function(event){setFlagEdit(event)}, false);
                    td.appendChild(inp);
                    break;
                case "Date":
                    inp = newDOMelement('<input type="date" style="margin-left:3px;border:none;width:' + (met.lenTab - 6) + 'px;"/>');
                    let vv_N = 0;
                    if (vv != null) {
                        if (typeof vv === "number") {
                            vv_N = vv;
                        } else {
                            if (vv.length != 0) {
                                vv_N = parseInt(vv);
                            }
                        }
                    }
                    if (vv_N != 0) {
                        let newDat = vv_N + offset;
                        inp.valueAsNumber = newDat;
                    } else {
                        if (met.def == "CURRENT_DATE") {
                            inp.valueAsNumber = new Date();
                        } else {
                            inp.value = "";
                        }
                    }
                    inp.addEventListener('change', function(event){setFlagEdit(event)}, false);
                    td.appendChild(inp);
                    break;
                case "Time":
                    inp = newDOMelement('<input type="time" style="margin-left:3px;border:none;width:' + (met.lenTab - 6) + 'px;"/>');
                    if (vv != null && vv != 0) {
                        inp.valueAsNumber = vv;
                    } else {
                        inp.value = "";
                    }
                    inp.addEventListener('change', function(event){setFlagEdit(event)}, false);
                    td.appendChild(inp);
                    break;
                default:
                    inp = document.createElement('input');
                    inp.type = "text";
                    inp.name = met.name;
                    inp.style.cssText = "margin-left:3px;border:none;width:" + (met.lenTab - 6) + "px;";
                    if (vv != null) {
                        inp.value = vv;
                    } else {
                        inp.value = "";
                    }
                    switch (met.type) {
                        case "Long":
                        case "Int":
                            inp.addEventListener('keydown', function(event){clickNumbet(event)}, false);
                            break;
                        case "Double":
                        case "Float":
                            inp.addEventListener('keydown', function(event){editFloat(event)}, false);
                            break;
                        case "Text":
                        default:
                            inp.addEventListener('keydown', function(event){clickText(event, met.valid)}, false);
                    }
                    inp.addEventListener('focus', function(event){focusInput(event)}, false);
                    inp.addEventListener('blur', function(event){blurInput(event)}, false);
                    inp.style.backgroundColor = "#0000";
                    td.appendChild(inp);
            }
        }
        return td;
    }
    
    function selecktImgFile(img) {
        cell = img.parentElement;
        sendImageFile(meta.name_table, self.edMeta[cell.numField].name, img);
    }
    
    function editGallery(img) {
        cell = img.parentElement;
        editGalleryFile(meta.name_table, self.edMeta[cell.numField].name, img);
    }

    function setImgEditData(i, par) {
        let nn = listImage[i];
        par.src = nn;
        par.srcElem = nn;
    }

    function setSelect(met, vv) {
        let selSel = formSelectForEditData(met.select, vv);
        selSel.style.width = met.len + "px";
        selSel.style.border = "none";
        selSel.style.backgroundColor = "#0000";
        return selSel;
    }
    
    function focusInput(e) {
        e.currentTarget.style.background = "#cdf";
    }
    
    function blurInput(e) {
        e.currentTarget.style.background = "#0000";
    }
    
    function tooltip(target, message) {
        let maxW = 250;
        let xy = getCoords(target);
        let x = xy.left;
        let y = xy.top;
        let dv = document.createElement('div');
        if (y > 40) {
            y -= 40;
        } else {
            y += 20;
        }
        let wD = document.documentElement.clientWidth;
        if ((wD - x) < maxW) {
            x = wD - maxW - 20;
        }
        dv.style.cssText = "position:absolute;max-width:" + maxW + "px;padding:5px;background:var(--c_yelow_lite);border:1px solid #ffc700;border-radius:8px;left:" 
                + x + "px;top:" + y + "px;z-index:100";
        dv.innerHTML = message;
        document.body.append(dv);
        setTimeout(function(){ document.body.removeChild(dv);},2000);
    }
    
    function getCoords(elem) { 
        var box = elem.getBoundingClientRect();
        return {
          top: box.top + pageYOffset,
          left: box.left + pageXOffset
        };
    }
    
    clickUpInput = function(event) {
        let k = event.key;
        if (k == "ArrowDown" || k == "ArrowUp") {
            let cell = event.target.closest('.col');
            if (cell != null) {
                let cR = cell.closest('.row').numRow;
                let cC = cell.numField;
                switch (k) {
                    case "ArrowDown":
                        if (cR < (edDomEl.children.length - 1)) {
                            upDown(cC, cR + 1);
                            return true;
                        }
                        break;
                    case "ArrowUp":
                        if (cR > 0) {
                            upDown(cC, cR - 1);
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }
    
    function clickText(event, valid) {
        let k = event.key;
        if (k == "Backspace" || k == "Delete") {
            setFlagEdit(event);
        }
        if (k == "ArrowUp" || k == "ArrowDown") {
            return clickUpInput(event);
        } else { 
            if (k == "ArrowRight" || k == "ArrowLeft" || k == "Tab" 
                || k == "Home" || k == "End" || k == "Backspace" || k == "Delete") {
                return true;
            }
        }
        if (valid != null) {
            if (valid.latin != null && valid.latin) {
                let kUp = event.key.toUpperCase();
                if ( ! ((kUp >= "A" && kUp <= "Z") || kUp == "_" || (kUp >= "0" && kUp <= "9")))  {
                    event.preventDefault();
                    tooltip(event.target, "Только английские буквы, _ и цифры");
                }
                setFlagEdit(event);
            } else {
                if (valid.name_low != null && valid.name_low) {
                    let targ = event.target;
//                        let k = event.key;
                    if ( ! ((k >= "a" && k <= "z") || k == "_" || (k >= "0" && k <= "9")))  {
                        event.preventDefault();
                        tooltip(targ, "Только английские буквы, _ и цифры");
                    } else {
                        if (targ.value.length == 0 && k >= "0" && k <= "9") {
                            event.preventDefault();
                            tooltip(targ, "The first character cannot be a digit");
                        }
                    }
                }
                setFlagEdit(event);
            }
        } else {
            setFlagEdit(event);
        }
    }
    
    function setFlagEdit(event) {
        let cell = event.target.closest('.col');
        cell.isEdit = true;
        let row = cell.closest('.row');
        if ( ! row.newRecord) {
            setStatus(row.numRow, 1);
        }
        row.isEdit = true;
    }
    
    function clickNumbet(event) {
        let k = event.keyCode;
        if (k < 47) {
            return clickUpInput(event);
        } else {
            if ( ! ((k > 47 && k < 58) || k == 173)) {
                event.preventDefault();
                tooltip(event.target, "Only numbers");
            } else {
                if (k == 173) {
                    if (event.target.selectionStart > 0) {
                        event.preventDefault();
                        tooltip(event.target, "Minus not at the beginning");
                    }
                }
            }
            setFlagEdit(event);
        }
    }

    function editFloat(event) {
        let k = event.keyCode;
        let z = event.key;
        if (k < 47) {
            return true;
        }
        if ((k > 47 && k < 58) || k == 173 || z == ".") {
            if (k == 173) {
                if (event.target.selectionStart > 0) {
                    event.preventDefault();
                    tooltipMessage(event.target, "Минус не в начале");
                    return false;
                }
            } else if (z == ".") {
                let vv = event.target.value;
                if (vv.indexOf(".") > -1) {
                    event.preventDefault();
                    tooltipMessage(event.target, "Точка уже есть");
                    return false;
                }
            }
            setFlagEdit(event);
            return true;
        } else {
            event.preventDefault();
            tooltipMessage(event.target, "Только цифры");
            return false;
        }
    }

    function upDown(cC, cR) {
        let row = edDomEl.children[cR];
        let cellSel = row.getElementsByClassName("col")[cC];
        let newInput = cellSel.getElementsByTagName("input")[0];
        newInput.focus();
    }

    function addItem(e) {
        createNewRow();
        scrollVert.resize(e);
        dataNumb.scrollTop = dataNumb.scrollHeight;
    }
    
    function createNewRow() {
        let firstElem;
        let num = 0;
        if (edDomEl.children != null) {
            num = edDomEl.children.length;
        }
        let row = document.createElement('div');
        row.className = "row";
        row.numRow = num;
        row.newRecord = true;
        row.style.cssText = "float:left;clear:both;position:relative;height:" + hCells + "px;border-bottom:1px solid #C5DCFA;";
        createCellNum(num);
        setStatus(num, 2);
        for (let i = 0; i < ikM; i++) {
            let met = self.edMeta[i];
            let col = createCol(i, met, null);
            if (firstElem == null) {
                let ff = col.getElementsByTagName("input");
                if ( ff != null) {
                    firstElem = ff[0];
                }
            }
            row.appendChild(col);
            let divider = newDOMelement('<div style="float:left;width:3px;height:' 
                    + hCells + 'px;border-right:1px solid #C5DCFA;"></div>');
            row.appendChild(divider);
        }
        row.addEventListener("mouseover", function(event){mouseoverTr(event)}, true);
        row.addEventListener("mouseout", function(event){mouseoutTr(event)}, true);
        edDomEl.appendChild(row);
        if (firstElem != null) {
            firstElem.focus();
        }
    }
    
    function saveData(e) {
        let listRow = edDomEl.children;
        let jk = listRow.length;
        if (jk == 0) return;
        let datNew = [];
        let dataEdit = [];
        let dataDel = [];
        for (j = 0; j < jk; j++) {
            let item = {};
            let row = listRow[j];
            let dd = row.isDel;
            if (dd == null) {
                dd = false;
            }
            if (dd) {
                if ( row.newRecord == null || (! row.newRecord) ) {
                    dataDel.push(row.primaryK);                    
                } 
            } else {
                let nn = row.newRecord;
                if (nn == null) {
                    nn = false;
                }
                let ed = row.isEdit;
                if (ed == null) {
                    ed = false;
                }
                if (nn) {       // new
                    if (ed) {
                        if (datNew.length == 0) {
                            datNew.push(newListFieldsName());
                        }
                        datNew.push(listFieldsValue(j));
                    }  
                } else {
                    if (ed) {
                        dataEdit.push(newUpdateSet(j));
                    }   
                }
            }
        }
        if (datNew.length > 1 || dataEdit.length > 0 || dataDel.length > 0) {
            queryDat = {name_table: meta.name_table, name_primary: primaryKayName, datNew: datNew, dataEdit: dataEdit, dataDel: dataDel};
            doServer('POST', 'tables/save', function(res){
                edData = JSON.parse(res);
                edDomEl.innerHTML = "";
                dataTitle.innerHTML = "";
                dataNumb.innerHTML = "";
                formEditTab();
            }, JSON.stringify(queryDat));

        }
    }

    function newListFieldsName() {
        let res = "(";
        let sep = "";
        for (let i = 0; i < ikM; i++) {
            let item = self.edMeta[i];
            let nam = item.name;
            if (item.type.indexOf("erial") == -1 && nam.indexOf("__") != 0) {     //  not    Serial or Bigserial
                res += sep + nam;
                sep = ", ";
            }
        }
//console.log("newListFieldsName res="+res + ")<<");
        return res + ")";
    }
    
    function listFieldsValue(j) {
        let dat = edDomEl.children[j];
        let res = "(";
        let sep = "";
        for (let i = 0; i < ikM; i++) {
            let item = self.edMeta[i];
            let val;
            let inp;
            if (item.type.indexOf("erial") == -1 && item.name.indexOf("__") != 0) {     //  not    Serial or Bigserial
                let dat_i = dat.getElementsByClassName("col")[i];
                if (dat_i.isEdit) {
                    switch (item.type) {
                        case "Timestamp":
                        case "Date":
                        case "Time":
                        case "Text":
                            inp = dat_i.querySelector('input');
                            if (inp.value == null || inp.value.length == 0) {
                                val = "DEFAULT";
                            } else {
                                val = "'" + inp.value.replaceAll("'", "''") + "'";
                            }
                            break;
                        case "Gallery":
                            inp = dat_i.querySelector('img');
                            val_1 = JSON.stringify(inp.adrImg);
                            val = "'" + val_1 + "'";
                            break;
                        case "Img":
                            inp = dat_i.querySelector('img');
                            val = "'" + inp.adrImg + "'";
                            break;
                        case "Long":
                        case "Float":
                        case "Double":
                        case "Int":
                            inp = dat_i.querySelector('input');
                            val = inp.value;
                            break;
                        case "Boolean":
                            inp = dat_i.querySelector('input');
                            if (inp.checked) {
                                val = "TRUE";
                            } else {
                                val = "FALSE";
                            }
//                            val = inp.value;
                            break;
                    }
                } else {
                    val = 'DEFAULT';
                }
                res += sep + val;
                sep = ", ";
            }
        }
//console.log("   listFieldsValue res="+res + ")<<");
        return res + ")";
    }
    
    function newUpdateSet(j) {
        let dat = edDomEl.children[j];
        let res = "";
        let sep = "";
        let primaryN;
        let primaryV;
        for (let i = 0; i < ikM; i++) {
            let item = self.edMeta[i];
            let val;
            let inp;
            let dat_i = dat.getElementsByClassName("col")[i];
            let nam = item.name;
            if (item.type.indexOf("erial") == -1 && nam.indexOf("__") != 0) {     //  not    Serial or Bigserial
                if (dat_i.isEdit) {
                    switch (item.type) {
                        case "Timestamp":
                            inp = dat_i.querySelector('input');
                            val = "'" + inp.value.replaceAll("'", "''") + "'";
                            break;
                        case "Date":
                        case "Time":
                            inp = dat_i.querySelector('input');
                            let dd = new Date(inp.value);
//console.log("newUpdateSet VAL="+inp.value+"<< ddd="+dd.getTime());
                            val = dd.getTime();
                        case "Text":
                            inp = dat_i.querySelector('input');
                            vv = inp.value;
                            vv.replaceAll("'", "''");
                            val = "'" + vv + "'";
                            break;
                        case "Gallery":
                            inp = dat_i.querySelector('img');
                            val_1 = JSON.stringify(inp.adrImg);
                            val = "'" + val_1 + "'";
                            break;
                        case "Img":
                            inp = dat_i.querySelector('img');
                            val = "'" + inp.adrImg + "'";
                            break;
                        case "Int":
                        case "Long":
                        case "Float":
                        case "Double":
                            inp = dat_i.querySelector('input');
                            val = inp.value;
                            break;
                        case "Boolean":
                            inp = dat_i.querySelector('input');
                            if (inp.checked) {
                                val = "TRUE";
                            } else {
                                val = "FALSE";
                            }
//                            val = inp.value;
                            break;
                    }
                    res += sep + item.name + " = " + val;
                    sep = ", ";
                }
            } else {
                if (item.key) {
                    primaryN = item.name;
                    primaryV = dat_i.primaryK;
                }
            }
        }
        return res + " WHERE " + primaryN + " = " + primaryV;
    }
    
    formEditTab();
}

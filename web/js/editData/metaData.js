var widthNum = 28;
var heightCells = 20;
var heightTitle = 24;
var heightFooter = 30;
var dataNumb;
var colorsStatus = ["", "#ffc700", "#0a0"];

function editDataWind(meta, data, obrSave, w, h, l) {
    let ww = 400, hh = 350, ll = 550;
    if (w != null) {
        ww = w;
    }
    if (h != null) {
        hh = h;
    }
    if (l != null) {
        ll = l;
    }
    let titleW = meta.titleForm;
    if (titleW == null || titleW.length == 0) {
        titleW = meta.name_table;
    }
    let windOverall = formWind(ww, hh, 40, ll, titleW);
    windOverall.onselectstart = function() { return false; }
    windOverall.className = "windOverall";
    
    let footer = newDOMelement('<div style="position:absolute;width:100%;height:' + heightFooter + 'px;bottom:0;border-top:1px solid #C5DCFA;"></div>');
    windOverall.appendChild(footer);

    let windMenu = newDOMelement('<div style="position:absolute;width:100%;top:0;bottom:' + (heightFooter + 1) + 'px"></div>');
    windOverall.appendChild(windMenu);
    
    let viewport = document.createElement('div');
    viewport.className = "viewport";
    viewport.style.left = widthNum + "px";
    viewport.style.top = heightTitle + "px";
    windMenu.appendChild(viewport);
    
    let content = document.createElement('div');
    content.className = "content";
    viewport.appendChild(content);
    
    let dataTable = document.createElement('div');
    content.appendChild(dataTable);
    
    let contTitle = newDOMelement('<div style="position: absolute;top: 0;right: 6px;overflow: hidden"></div>');
    contTitle.style.height = heightTitle + "px";
    contTitle.style.left = widthNum + "px";
    windMenu.appendChild(contTitle);
    
    let scrollTitle = newDOMelement('<div style="position: absolute;left: 0;top: 0;bottom: -17px;right: 0;overflow-x: scroll;overflow-y: hidden"></div>');
    contTitle.appendChild(scrollTitle);
    
    let dataTitle = document.createElement('div');
    scrollTitle.appendChild(dataTitle);
    
    windMenu.appendChild(newDOMelement('<div style="position: absolute;left: 0;top: 0;width:' + (widthNum - 1) + 'px;height:' 
            + heightTitle + 'px;border-right:1px solid #C5DCFA;"></div>'));
    
    let contNumb = newDOMelement('<div style="position: absolute;left: 0;bottom: 6px;overflow: hidden;border-top:1px solid #C5DCFA;"></div>');
    contNumb.style.width = widthNum + "px";
    contNumb.style.top = (heightTitle - 1) + "px";
    windMenu.appendChild(contNumb);
    
    let scrollNumb = newDOMelement('<div style="position: absolute;left: 0;top: 0;bottom:0;right:-17px;overflow-y: scroll;overflow-x: hidden"></div>');
    contNumb.appendChild(scrollNumb);
    
    dataNumb = document.createElement('div');
    scrollNumb.appendChild(dataNumb);
    let editTab = new EditTable(meta, data, dataTable, dataTitle, dataNumb, footer, obrSave);
    let wWindTable = editTab.getWidthW() + widthNum + 25;
    let wDocClient = document.documentElement.clientWidth * 0.8;
    if (wWindTable > wDocClient) {
        wWindTable = wDocClient;
    }
    windOverall.parentElement.style.width = wWindTable + "px";
    
    let scrollVert = new ScrollY(viewport, true, dataNumb);
    editTab.setScrolls(scrollVert);
    scrollVert.setScrollHide(true);
    scrollVert.init();
    
    let scrollHoris = new ScrollX(viewport, true, dataTitle);
//    editTab.setScrolls(scrollHoris);
    scrollHoris.setScrollHide(true);
    scrollHoris.init();
}

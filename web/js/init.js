var schema;
var listTablesView;
//var listTables = null;
var scrollTables;
var debagStatus;
var isLocalHost;

function initiale() {
//console.log("window.location.hostname="+window.location.hostname+"<<");
    debagStatus = window.location.hostname.startsWith("deb-") || window.location.hostname == "localhost";
    isLocalHost = window.location.hostname == "localhost";
    formMenuEl();
    if (debagStatus) {
        statusDebagView.style.display = "block";
    }
    let st = new String(window.location);
    let arSt = st.split("?");
    schema = "";
    if (arSt.length == 2) {
        let par = arSt[1].split("&");
        let ik = par.length;
        for (let i = 0; i < ik; i++) {
            let stPar = par[i].split("=");
            if (stPar[0] == "schema") {
                schema = stPar[1];
                break;
            }
        }
    }
console.log("schema="+schema+"<<");

    if (schema != "") {
        let viewport = document.createElement('div');
        viewport.className = "viewport";
        let content = document.createElement('div');
        content.className = "content";
        listTablesView = newDOMelement('<div style="margin-right:12px"></div>');
        content.appendChild(listTablesView);
        viewport.appendChild(content);
        table_list.appendChild(viewport);
        
        scrollTables = new ScrollY(viewport);
        scrollTables.setScrollHide(true);
        scrollTables.init();
        doServer("GET", 'tables/list', cbGetTables);
    }
}


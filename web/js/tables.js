var indSelectTable;

function cbGetTables(res) {
    listTables = JSON.parse(res);
    let ik = listTables.length;
    if (ik > 0) {
        for (let i = 0; i < ik; i++) {
            listTablesView.append(oneTableView(i));
        }
    }
    scrollTables.resize();
}

function oneTableView(i) {
    let item = listTables[i];
    return newDOMelement('<div onclick="callEditTable(' + i + ')" style="float:left;width:100%;height:30px;cursor:pointer;border-bottom:1px solid #aaf;clear:both">' 
            + '<div style="font-size:16px;color:#000;margin-top:5px;float:left;margin-left:5px">' + item.name_table + '</div>'
            + '<div style="font-size:12px;color:#555;margin-top:8px;float:left;margin-left:10px">' + item.title_table + '</div>'
            +'</div>');
}

function callEditTable(z) {
    indSelectTable = z;
    let item = listTables[indSelectTable];
    doServer("GET", "tables/listdata?name_table=" + item.name_table, cbListData);
}

function cbListData(res) {
//console.log("APP_DePro RES="+res);
    let item = listTables[indSelectTable];
    let fields = JSON.parse(item.fields_table);
//    fields.unshift({id_field:0, name:"id_" + item.name_table, type:"Bigserial", title:"", key:true});
    let listField = [];
    let ik = fields.length;
    for (let i = 0; i < ik; i++) {
        let ff = fields[i];
        let len;
        let nn;
        if (ff.title == null || ff.title.length == 0) {
            len = 10;
            nn = ff.name;
        } else {
            len = ff.title.length;
            nn = ff.title;
        }
        let itemF = {name:ff.name,title:nn,type:ff.type,len:len,key:ff.key,def:ff.def};
        listField.push(itemF);
    }
    let metaData = {titleForm:item.title_table, name_table:item.name_table, description:listField};
//console.log("RES="+res);
    let dat = JSON.parse(res);
    editDataWind(metaData, dat, cbEditTable, 200, 450, 300);
}

function cbEditTable(dat) {
    
}

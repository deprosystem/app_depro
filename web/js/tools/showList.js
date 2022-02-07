function showList(list, view, container, listener, typeNum) {
    let ik = list.length;
    for (let i = 0; i < ik; i++) {
        let item = list[i];
        let type = 0;
        if (typeNum != null) {
            type = item[typeNum];
        }
        let vv = view(type);
        let fields = vv.getElementsByClassName("field");
        if (fields != null && fields.length > 0) {
            let jk = fields.length;
            for (let j = 0; j < jk; j++) {
                let el = fields[j];
                let id = el.id;
                let val = item[id];
                if (el.format != null) {
                    if (typeof val === "number") {
                        el.innerHTML = dateFormat(val, el.format);
                    } else {
                        el.innerHTML = val;
                    }
                } else {
                    el.innerHTML = val;
                }
                if (listener != null) {
                    setFuncListener(el, listener, i);
                }
            }
        }

        if (listener != null) {
            setFuncListener(vv, listener, i);
        }

        container.appendChild(vv);
    }
}

function dateFormat(val, format) {
    let dd = new Date(val);
    return dd.toString(format);
}

function setFuncListener(el, listener, pos) {
    let id = el.id;
    if (id == null) {
        id = "";
    }
    let ik = listener.length;
    for (let i = 0; i < ik; i++) {
        let item = listener[i];
        if (id == item.id) {
            let event = item.event;
            if (event == null || event.length == 0) {
                event = "click";
            }
            if (item.func != null) {
                el.addEventListener(event, function(){item.func(el, pos);}, false);
            }
        }
    }
}

function dialogError(title, message, cancel) {
    let windMenu = formWindCenter(400, 300, title);
    let divErr = document.createElement('div');
    divErr.style.cssText = "text-align:center;margin-top:16px";
    let imgErr = document.createElement('img');
    imgErr.src = "img/danger.png";
    divErr.appendChild(imgErr);
    windMenu.appendChild(divErr);
    let mesErr = document.createElement('div');
    mesErr.innerHTML = message;
    mesErr.style.cssText = 'font-size:16px;text-align:center;margin-top:16px;';
    windMenu.appendChild(mesErr);
    let futErr = document.createElement('div');
    futErr.className = 'futer_wind';
    windMenu.appendChild(futErr);
    let okErr = document.createElement('div');
    okErr.className = 'button_blue';
    okErr.style.marginTop = "12px";
    futErr.appendChild(okErr);
    
    let okTxt = document.createElement('div');
    okTxt.innerHTML = "oK";
    okTxt.style.cssText = "text-align: center;margin-top:7px;color:#fff";
    okErr.appendChild(okTxt);
    okErr.addEventListener("click", function(event){closeWindError(event)}, true);
}

function closeWindError(e) {
    let el = e.currentTarget;
    let el1 = parentWind(el);
    el1.parentNode.removeChild(el1);
}
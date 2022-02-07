/*
    Вертикальный скролинг
    Структура блоков:
            <div id="bbbb" class="viewport">
                <div class="content">
                    <div class="inf_block">
                        
                    </div>
                </div>
            </div>
    В блоке с классом inf_block содержится информация которая будет скролиться.
    Элемент bbbb с классом viewport передается в качестве параметра в ScrollY.
    Вызов
        let scrollVert = new ScrollY(bbbb, localScroll, addEl);
        scrollVert.setScrollColor(color);       // Можно не вызывать вызывается если цвет скролла должен быть не такой как в стилях. Здесь color - новый цвет
        scrollVert.setScrollHide(true);       // Можно не вызывать вызывается если нужно не показывать скролл в случае если все умещается
        scrollVert.init();

    Параметр localScroll если он задан b = true, то скрол бар буде размещен в родителе viewport. В этом случае предусмтрите его видимость в css
    Параметр addEl указывает дополнительный элемент, который будет скролиться вместе с блоком content
    Дополнительный элемент может быть в любом месте но в такой обертке:
            <div class="analog_viewport">
                <div id="dop" class="analog_content">
                    <div>
                        Здесь дополнительная инфа которая скролится.
                    </div>
                </div>
            </div>
        В качестве параметра addEl будет передаваться блок dop

    Если изменились размеры содержимого блока inf_block нужно вызвать  bbbb.scroll_y.resize();
    
    
    Все названия классов произвольное (может и отсутствовать. Только content должен быть ОБЯЗАТЕЛЬНО
    Блок viewport должен обязательно иметь установленные любым способом размеры у него position: absolute и overflow: hidden
        Если не нужно что бы элементы в viewport выделялись при скроле, то задать свойства -webkit-user-select: none; user-select: none; и т.д
    Стили должны быть следующие
    ОБЯЗАТЕЛЬНО все свойства для content
    .content {position: absolute;overflow-x: hidden; left: 0; top: 0; right:-17px;bottom: 0; overflow-y: scroll;}
    Стили для скролБара:
        .scrollbar_y, .scroller_y {width: 5px; position: absolute; top: 0;}
        .scrollbar_y {right: 0px; bottom: 0;}
        .scroller_y {left: 0; border-radius: 2px; background: var(--c_scroll); cursor: pointer;}
        .scroll_line_y {width: 1px; position: absolute; top: 0;right: 1.5px; bottom: 0;background:var(--c_scroll);}

    Аналогично для горизонтального скролла

*/

function ScrollY(container, localScroll, addEl) {
    this.nameEvent = "scroll";
    this.viewport = container;
    this.addEl = addEl;
    this.isHide = false;
    this.localScroll = localScroll;
    if (this.localScroll == null) {
        this.localScroll = false;
    }
    this.scrollColor = "";
    this.parentViewport = this.viewport.parentElement;
    container.scroll_y = this;
    this.content = this.viewport.querySelector('.content');
    this.viewportHeight = this.viewport.offsetHeight;
    this.contentHeight = this.content.scrollHeight;
    this.max = this.viewport.clientHeight - this.contentHeight;
    this.ratio = this.viewportHeight / this.contentHeight;
    this.scrollerHeightMin = 15;
    this.step = 20;
    this.pressed = false;
    this.baseHeight = parseInt(this.ratio * this.viewportHeight);
    
    const fn = ScrollY.prototype;

    fn.init = function() {
        if (this.viewportHeight > this.contentHeight) return;
        this.createScrollbar();
        this.registerEventsHandler();
    };

    fn.createScrollbar = function() {
        let scrollbar = document.createElement('div'),
                scroller = document.createElement('div');
        let scrollLine = document.createElement('div');
        scrollbar.className = 'scrollbar_y';
        scroller.className = 'scroller_y';
        scrollLine.className = 'scroll_line_y';
        scrollbar.appendChild(scrollLine);
        scrollbar.appendChild(scroller);
        if (this.localScroll) {
            this.parentViewport.appendChild(scrollbar);
            let rectP = this.parentViewport.getBoundingClientRect();
            let rectV = this.viewport.getBoundingClientRect();
            scrollbar.style.top = (rectV.top - rectP.top) + "px";
            scrollbar.style.bottom = (rectP.bottom - rectV.bottom) + "px";
        } else {
            this.viewport.appendChild(scrollbar);
        }
        if (this.scrollColor != "") {
            scroller.style.backgroundColor = this.scrollColor;
            scrollLine.style.backgroundColor = this.scrollColor;
        }

        this.scroller = scroller;
        this.scrollbar = scrollbar;
        this.scrollerHeight = parseInt(this.ratio * this.viewportHeight);
        this.scrollerHeight = (this.scrollerHeight < this.scrollerHeightMin) ? this.scrollerHeightMin : this.scrollerHeight;
        if (this.isHide) {
            if (this.ratio >=1) {
                this.scrollbar.style.display = "none";
            } else {
                this.scrollbar.style.display = "block";
            }
        }
        this.scroller.style.height = this.scrollerHeight + 'px';
        this.scrollerMaxOffset = this.viewportHeight - this.scroller.offsetHeight;
    };
    
    fn.setScrollColor = function (color) {
        this.scrollColor = color;
    }

    fn.registerEventsHandler = function(e) {
            this.content.addEventListener('scroll', () => {
                    this.scroller.style.top = (this.content.scrollTop * this.ratio) + 'px';
                    if (this.addEl != null) {
//                        this.addEl.parentElement.scrollTo(this.addEl.scrollLeft, this.content.scrollTop);
                        this.addEl.parentElement.scrollTo(this.addEl.scrollTop, this.content.scrollTop);
                    }
            });
            this.scroller.addEventListener('mousedown', e => {
                    // координата по оси Y нажатия левой кнопки мыши
                    this.start = e.clientY;
                    // устанавливаем флаг, информирующий о нажатии левой кнопки мыши
                    this.pressed = true;
            });
            document.addEventListener('mousemove', this.drop.bind(this));
            document.addEventListener('mouseup', () => this.pressed = false);
    };
    
    fn.setScrollHide = function(hide) {
        this.isHide = hide;
    }

    fn.resize = function(e) {
        this.viewportHeight = this.viewport.offsetHeight;
        this.contentHeight = this.content.scrollHeight;
        this.max = this.viewport.clientHeight - this.contentHeight;
        this.ratio = this.viewportHeight / this.contentHeight;

        this.scrollerHeight = parseInt(this.ratio * this.viewportHeight);
        this.scrollerHeight = (this.scrollerHeight < this.scrollerHeightMin) ? this.scrollerHeightMin : this.scrollerHeight;
        this.scroller.style.height = this.scrollerHeight + 'px';
        if (this.isHide) {
//            if (this.baseHeight == this.scrollerHeight) {
            if (this.ratio >= 1) {
                this.scrollbar.style.display = "none";
            } else {
                this.scrollbar.style.display = "block";
            }
        }
        this.scrollerMaxOffset = this.viewportHeight - this.scroller.offsetHeight;
    }

    fn.scroll = function(e) {
            e.preventDefault();
            let dir = -Math.sign(e.deltaY);
            let	step = (Math.abs(e.deltaY) >= 3) ? this.step * dir : 0;
            this.content.style.top = (this.content.offsetTop + step) + 'px';
            if (this.content.offsetTop > 0) this.content.style.top = '0px';
            if (this.content.offsetTop < this.max) this.content.style.top = this.max + 'px';
            this.scroller.style.top = (-this.content.offsetTop * this.ratio) + 'px';
    };

    fn.drop = function(e) {
            e.preventDefault();
            if (this.pressed === false) return;
            let shiftScroller = this.start - e.clientY;
            this.scroller.style.top = (this.scroller.offsetTop - shiftScroller) + 'px';
            if (this.scroller.offsetTop <= 0) this.scroller.style.top = '0px';
            let	totalHeight = this.scroller.offsetHeight + this.scroller.offsetTop;
            if (totalHeight >= this.viewportHeight) this.scroller.style.top = this.scrollerMaxOffset + 'px';

            let	shiftContent = this.scroller.offsetTop / this.ratio;
            
            if (this.addEl != null) {
                this.addEl.parentElement.scrollTo(this.addEl.scrollLeft, shiftContent);
            }
            this.content.scrollTo(this.content.scrollLeft, shiftContent);
            this.start = e.clientY;
    };
}
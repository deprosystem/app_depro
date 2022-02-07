// Инструкция в ScrollY
function ScrollX(container, localScroll, addEl) {
    this.nameEvent = "scroll";
    this.viewport = container;
    this.addEl = addEl;
    this.localScroll = localScroll;
    if (this.localScroll == null) {
        this.localScroll = false;
    }
    this.scrollColor = "";
    this.parentViewport = this.viewport.parentElement;
    container.scroll_x = this;
    this.content = this.viewport.querySelector('.content');
    this.viewportWidth = this.viewport.offsetWidth;
    this.contentWidth = this.content.scrollWidth;
    this.max = this.viewport.clientWidth - this.contentWidth;
    this.ratio = this.viewportWidth / this.contentWidth;
    this.scrollerWidthMin = 25;
    this.step = 20;
    this.pressed = false;
    
    const fn = ScrollX.prototype;

    fn.init = function() {
            if (this.viewportWidth > this.contentWidth) return;
            this.createScrollbar();
            this.registerEventsHandler();
    };

    fn.createScrollbar = function() {
            let scrollbar = document.createElement('div'),
                    scroller = document.createElement('div');
            let scrollLine = document.createElement('div');
            scrollbar.className = 'scrollbar_x';
            scroller.className = 'scroller_x';
            scrollLine.className = 'scroll_line_x';
            scrollbar.appendChild(scrollLine);
            scrollbar.appendChild(scroller);
            if (this.localScroll) {
                this.parentViewport.appendChild(scrollbar);
                let rectP = this.parentViewport.getBoundingClientRect();
                let rectV = this.viewport.getBoundingClientRect();
                scrollbar.style.left = (rectV.left - rectP.left) + "px";
                scrollbar.style.right = (rectP.right-rectV.right) + "px";
            } else {
                this.viewport.appendChild(scrollbar);
            }
            if (this.scrollColor != "") {
                scroller.style.backgroundColor = this.scrollColor;
                scrollLine.style.backgroundColor = this.scrollColor;
            }
            this.scroller = scroller;
            this.scrollbar = scrollbar;
            this.scrollerWidth = parseInt(this.ratio * this.viewportWidth);
            this.scrollerWidth = (this.scrollerWidth < this.scrollerWidthMin) ? this.scrollerWidthMin : this.scrollerWidth;
            if (this.isHide) {
                if (this.ratio >=1) {
                    this.scrollbar.style.display = "none";
                } else {
                    this.scrollbar.style.display = "block";
                }
            }
            this.scroller.style.width = this.scrollerWidth + 'px';
            this.scrollerMaxOffset = this.viewportWidth - this.scroller.offsetWidth;
    };

    fn.registerEventsHandler = function(e) {
            this.content.addEventListener('scroll', () => {
                    this.scroller.style.left = (this.content.scrollLeft * this.ratio) + 'px';
                    if (this.addEl != null) {
                        this.addEl.parentElement.scrollTo(this.content.scrollLeft, this.addEl.scrollTop);
                    }
            });
            
            this.scroller.addEventListener('mousedown', e => {
                    // координата по оси X нажатия левой кнопки мыши
                    this.start = e.clientX;
                    // устанавливаем флаг, информирующий о нажатии левой кнопки мыши
                    this.pressed = true;
            });
            document.addEventListener('mousemove', this.drop.bind(this));
            document.addEventListener('mouseup', () => this.pressed = false);
    };
    
    fn.setScrollHide = function(hide) {
        this.isHide = hide;
    }
    
    fn.setScrollColor = function (color) {
        this.scrollColor = color;
    }

    fn.resize = function(e) {
        this.viewportWidth = this.viewport.offsetWidth;
        this.contentWidth = this.content.scrollWidth;
        this.max = this.viewport.clientWidth - this.contentWidth;
        this.ratio = this.viewportWidth / this.contentWidth;

        this.scrollerWidth = parseInt(this.ratio * this.viewportWidth);
        this.scrollerWidth = (this.scrollerWidth < this.scrollerWidthMin) ? this.scrollerWidthMin : this.scrollerWidth;
        this.scroller.style.width = this.scrollerWidth + 'px';
        if (this.isHide) {
            if (this.ratio >= 1) {
                this.scrollbar.style.display = "none";
            } else {
                this.scrollbar.style.display = "block";
            }
        }
        this.scrollerMaxOffset = this.viewportWidth - this.scroller.offsetWidth;
    }

    fn.scroll = function(e) {
            e.preventDefault();
            let dir = -Math.sign(e.deltaX);
            let	step = (Math.abs(e.deltaX) >= 3) ? this.step * dir : 0;
            this.content.style.left = (this.content.offsetLeft + step) + 'px';
            if (this.content.offsetLeft > 0) this.content.style.left = '0px';
            if (this.content.offsetLeft < this.max) this.content.style.Left = this.max + 'px';
            this.scroller.style.left = (-this.content.offsetLeft * this.ratio) + 'px';
    };

    fn.drop = function(e) {
            e.preventDefault();
            if (this.pressed === false) return;
            let shiftScroller = this.start - e.clientX;
            this.scroller.style.left = (this.scroller.offsetLeft - shiftScroller) + 'px';
            if (this.scroller.offsetLeft <= 0) this.scroller.style.left = '0px';
            let	totalWidth = this.scroller.offsetWidth + this.scroller.offsetLeft;
            if (totalWidth >= this.viewportWidth) this.scroller.style.left = this.scrollerMaxOffset + 'px';

            let	shiftContent = this.scroller.offsetLeft / this.ratio;
            
            if (this.addEl != null) {
                this.addEl.scrollTo(shiftContent, this.addEl.scrollTop);
            }
            this.content.scrollTo(shiftContent, this.content.scrollTop);

            this.start = e.clientX;
    };
}


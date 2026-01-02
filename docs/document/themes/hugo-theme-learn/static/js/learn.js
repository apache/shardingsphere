// Scrollbar Width function
function getScrollBarWidth() {
    var inner = document.createElement('p');
    inner.style.width = "100%";
    inner.style.height = "200px";

    var outer = document.createElement('div');
    outer.style.position = "absolute";
    outer.style.top = "0px";
    outer.style.left = "0px";
    outer.style.visibility = "hidden";
    outer.style.width = "200px";
    outer.style.height = "150px";
    outer.style.overflow = "hidden";
    outer.appendChild(inner);

    document.body.appendChild(outer);
    var w1 = inner.offsetWidth;
    outer.style.overflow = 'scroll';
    var w2 = inner.offsetWidth;
    if (w1 == w2) w2 = outer.clientWidth;

    document.body.removeChild(outer);

    return (w1 - w2);
};

function setMenuHeight() {
    $('#sidebar .leftMenu').height($(window).innerHeight() - $('#header-wrapper').height() - 240);
    $('#sidebar .leftMenu').perfectScrollbar('update');
}

function fallbackMessage(action) {
    var actionMsg = '';
    var actionKey = (action === 'cut' ? 'X' : 'C');

    if (/iPhone|iPad/i.test(navigator.userAgent)) {
        actionMsg = 'No support :(';
    }
    else if (/Mac/i.test(navigator.userAgent)) {
        actionMsg = 'Press ⌘-' + actionKey + ' to ' + action;
    }
    else {
        actionMsg = 'Press Ctrl-' + actionKey + ' to ' + action;
    }

    return actionMsg;
}

function switchTab(tabGroup, tabId) {
    allTabItems = jQuery("[data-tab-group='"+tabGroup+"']");
    targetTabItems = jQuery("[data-tab-group='"+tabGroup+"'][data-tab-item='"+tabId+"']");

    // if event is undefined then switchTab was called from restoreTabSelection
    // so it's not a button event and we don't need to safe the selction or
    // prevent page jump
    var isButtonEvent = event != undefined;

    if(isButtonEvent){
      // save button position relative to viewport
      var yposButton = event.target.getBoundingClientRect().top;
    }

    allTabItems.removeClass("active");
    targetTabItems.addClass("active");

    if(isButtonEvent){
      // reset screen to the same position relative to clicked button to prevent page jump
      var yposButtonDiff = event.target.getBoundingClientRect().top - yposButton;
      window.scrollTo(window.scrollX, window.scrollY+yposButtonDiff);

      // Store the selection to make it persistent
      if(window.localStorage){
          var selectionsJSON = window.localStorage.getItem("tabSelections");
          if(selectionsJSON){
            var tabSelections = JSON.parse(selectionsJSON);
          }else{
            var tabSelections = {};
          }
          tabSelections[tabGroup] = tabId;
          window.localStorage.setItem("tabSelections", JSON.stringify(tabSelections));
      }
    }
}

function restoreTabSelections() {
    if(window.localStorage){
        var selectionsJSON = window.localStorage.getItem("tabSelections");
        // if(selectionsJSON){
        //   var tabSelections = JSON.parse(selectionsJSON);
        // }else{
          var tabSelections = {};
        // }
        Object.keys(tabSelections).forEach(function(tabGroup) {
          var tabItem = tabSelections[tabGroup];
          switchTab(tabGroup, tabItem);
        });
    }
}

// for the window resize
$(window).resize(function() {
    setMenuHeight();
});

// debouncing function from John Hann
// http://unscriptable.com/index.php/2009/03/20/debouncing-javascript-methods/
(function($, sr) {

    var debounce = function(func, threshold, execAsap) {
        var timeout;

        return function debounced() {
            var obj = this, args = arguments;

            function delayed() {
                if (!execAsap)
                    func.apply(obj, args);
                timeout = null;
            };

            if (timeout)
                clearTimeout(timeout);
            else if (execAsap)
                func.apply(obj, args);

            timeout = setTimeout(delayed, threshold || 100);
        };
    }
    // smartresize
    jQuery.fn[sr] = function(fn) { return fn ? this.bind('resize', debounce(fn)) : this.trigger(sr); };

})(jQuery, 'smartresize');


jQuery(document).ready(function() {
    restoreTabSelections();

    jQuery('#sidebar .category-icon').on('click', function() {
        $( this ).toggleClass("fa-angle-down fa-angle-right") ;
        $( this ).parent().parent().children('ul').toggle() ;
        return false;
    });

    var sidebarStatus = searchStatus = 'open';
    $('#sidebar .leftMenu').perfectScrollbar();
    setMenuHeight();

    jQuery('#overlay').on('click', function() {
        jQuery(document.body).toggleClass('sidebar-hidden');
        sidebarStatus = (jQuery(document.body).hasClass('sidebar-hidden') ? 'closed' : 'open');

        return false;
    });

    jQuery('[data-sidebar-toggle]').on('click', function() {
        jQuery(document.body).toggleClass('sidebar-hidden');
        sidebarStatus = (jQuery(document.body).hasClass('sidebar-hidden') ? 'closed' : 'open');

        return false;
    });
    jQuery('[data-clear-history-toggle]').on('click', function() {
        sessionStorage.clear();
        location.reload();
        return false;
    });
    jQuery('[data-search-toggle]').on('click', function() {
        if (sidebarStatus == 'closed') {
            jQuery('[data-sidebar-toggle]').trigger('click');
            jQuery(document.body).removeClass('searchbox-hidden');
            searchStatus = 'open';

            return false;
        }

        jQuery(document.body).toggleClass('searchbox-hidden');
        searchStatus = (jQuery(document.body).hasClass('searchbox-hidden') ? 'closed' : 'open');

        return false;
    });

    var ajax;
    jQuery('[data-search-input]').on('input', function() {
        var input = jQuery(this),
            value = input.val(),
            items = jQuery('[data-nav-id]');
        items.removeClass('search-match');
        if (!value.length) {
            $('ul.topics').removeClass('searched');
            items.css('display', 'block');
            sessionStorage.removeItem('search-value');
            $(".highlightable").unhighlight({ element: 'mark' })
            return;
        }

        sessionStorage.setItem('search-value', value);
        $(".highlightable").unhighlight({ element: 'mark' }).highlight(value, { element: 'mark' });

        if (ajax && ajax.abort) ajax.abort();

        jQuery('[data-search-clear]').on('click', function() {
            jQuery('[data-search-input]').val('').trigger('input');
            sessionStorage.removeItem('search-input');
            $(".highlightable").unhighlight({ element: 'mark' })
        });
    });

    $.expr[":"].contains = $.expr.createPseudo(function(arg) {
        return function( elem ) {
            return $(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
        };
    });

    if (sessionStorage.getItem('search-value')) {
        var searchValue = sessionStorage.getItem('search-value')
        $(document.body).removeClass('searchbox-hidden');
        $('[data-search-input]').val(searchValue);
        $('[data-search-input]').trigger('input');
        var searchedElem = $('#body-inner').find(':contains(' + searchValue + ')').get(0);
        if (searchedElem) {
            searchedElem.scrollIntoView(true);
            var scrolledY = window.scrollY;
            if(scrolledY){
                window.scroll(0, scrolledY - 125);
            }
        }
    }

    // clipboard
    var clipInit = false;
    $('code').each(function() {
        var code = $(this),
            text = code.text();

        if (text.length > 5) {
            if (!clipInit) {
                var text, clip = new ClipboardJS('.copy-to-clipboard', {
                    text: function(trigger) {
                        text = $(trigger).prev('code').text();
                        return text.replace(/^\$\s/gm, '');
                    }
                });

                var inPre;
                clip.on('success', function(e) {console.log(text)
                    e.clearSelection();
                    inPre = $(e.trigger).parent().prop('tagName') == 'PRE';
                    $(e.trigger).attr('aria-label', 'Copied to clipboard!').addClass('tooltipped tooltipped-' + (inPre ? 'w' : 's'));
                });

                clip.on('error', function(e) {
                    inPre = $(e.trigger).parent().prop('tagName') == 'PRE';
                    $(e.trigger).attr('aria-label', fallbackMessage(e.action)).addClass('tooltipped tooltipped-' + (inPre ? 'w' : 's'));
                    $(document).one('copy', function(){
                        $(e.trigger).attr('aria-label', 'Copied to clipboard!').addClass('tooltipped tooltipped-' + (inPre ? 'w' : 's'));
                    });
                });

                clipInit = true;
            }

            code.after('<span class="copy-to-clipboard" title="Copy to clipboard" />');
            code.next('.copy-to-clipboard').on('mouseleave', function() {
                $(this).attr('aria-label', null).removeClass('tooltipped tooltipped-s tooltipped-w');
            });
        }
    });

    // allow keyboard control for prev/next links
    jQuery(function() {
        jQuery('.nav-prev').click(function(){
            location.href = jQuery(this).attr('href');
        });
        jQuery('.nav-next').click(function() {
            location.href = jQuery(this).attr('href');
        });
    });

    jQuery('input, textarea').keydown(function (e) {
         //  left and right arrow keys
         if (e.which == '37' || e.which == '39') {
             e.stopPropagation();
         }
     });

    jQuery(document).keydown(function(e) {
      // prev links - left arrow key
      if(e.which == '37') {
        jQuery('.nav.nav-prev').click();
      }

      // next links - right arrow key
      if(e.which == '39') {
        jQuery('.nav.nav-next').click();
      }
    });

    $('#top-bar a:not(:has(img)):not(.btn)').addClass('highlight');
    $('#body-inner a:not(:has(img)):not(.btn):not(a[rel="footnote"])').addClass('highlight');

    var touchsupport = ('ontouchstart' in window) || (navigator.maxTouchPoints > 0) || (navigator.msMaxTouchPoints > 0)
    if (!touchsupport){ // browser doesn't support touch
        $('#toc-menu').hover(function() {
            $('.progress').stop(true, false, true).fadeToggle(100);
        });

        $('.progress').hover(function() {
            $('.progress').stop(true, false, true).fadeToggle(100);
        });
    }
    if (touchsupport){ // browser does support touch
        $('#toc-menu').click(function() {
            $('.progress').stop(true, false, true).fadeToggle(100);
        });
        $('.progress').click(function() {
            $('.progress').stop(true, false, true).fadeToggle(100);
        });
    }

    /**
    * Fix anchor scrolling that hides behind top nav bar
    * Courtesy of https://stackoverflow.com/a/13067009/28106
    *
    * We could use pure css for this if only heading anchors were
    * involved, but this works for any anchor, including footnotes
    **/
    (function (document, history, location) {
        var HISTORY_SUPPORT = !!(history && history.pushState);

        var anchorScrolls = {
            ANCHOR_REGEX: /^#[^ ]+$/,
            OFFSET_HEIGHT_PX: 50,

            /**
             * Establish events, and fix initial scroll position if a hash is provided.
             */
            init: function () {
                this.scrollToCurrent();
                $(window).on('hashchange', $.proxy(this, 'scrollToCurrent'));
                $('body').on('click', 'a', $.proxy(this, 'delegateAnchors'));
            },

            /**
             * Return the offset amount to deduct from the normal scroll position.
             * Modify as appropriate to allow for dynamic calculations
             */
            getFixedOffset: function () {
                return this.OFFSET_HEIGHT_PX;
            },

            /**
             * If the provided href is an anchor which resolves to an element on the
             * page, scroll to it.
             * @param  {String} href
             * @return {Boolean} - Was the href an anchor.
             */
            scrollIfAnchor: function (href, pushToHistory) {
                var match, anchorOffset;

                if (!this.ANCHOR_REGEX.test(href)) {
                    return false;
                }

                match = document.getElementById(href.slice(1));

                if (match) {
                    anchorOffset = $(match).offset().top - this.getFixedOffset();
                    $('html, body').animate({ scrollTop: anchorOffset });

                    // Add the state to history as-per normal anchor links
                    if (HISTORY_SUPPORT && pushToHistory) {
                        history.pushState({}, document.title, location.pathname + href);
                    }
                }

                return !!match;
            },

            /**
             * Attempt to scroll to the current location's hash.
             */
            scrollToCurrent: function (e) {
                if (this.scrollIfAnchor(window.location.hash) && e) {
                    e.preventDefault();
                }
            },

            /**
             * If the click event's target was an anchor, fix the scroll position.
             */
            delegateAnchors: function (e) {
                var elem = e.target;

                if (this.scrollIfAnchor(elem.getAttribute('href'), true)) {
                    e.preventDefault();
                }
            }
        };

        $(document).ready($.proxy(anchorScrolls, 'init'));
    })(window.document, window.history, window.location);


    //railroad diagram
    if ($('.tab-panel').length) {
        var codeBlock = $('.tab-panel code').first();
        var diagram = $('#diagram');
        var grammarText = codeBlock.text() || '';

        if (grammarText && diagram.length) {
            var loadingId = 'rr-loading';
            diagram.before('<p id="' + loadingId + '">Loading ...</p>');

            function baseRailroadPath() {
                var parts = window.location.pathname.split('/');
                var docIdx = parts.indexOf('document');
                if (docIdx === -1) {
                    return '/railroad/';
                }
                // e.g. /document/current/... -> /document/current/railroad/
                return '/' + parts.slice(1, docIdx + 2).join('/') + '/railroad/';
            }

            function toHex(buffer) {
                var bytes = new Uint8Array(buffer);
                var hex = '';
                for (var i = 0; i < bytes.length; i++) {
                    hex += ('00' + bytes[i].toString(16)).slice(-2);
                }
                return hex;
            }

            function sha256(text) {
                if (!window.crypto || !window.crypto.subtle || !window.TextEncoder) {
                    return Promise.reject(new Error('SHA-256 unsupported'));
                }
                var encoder = new TextEncoder();
                return window.crypto.subtle.digest('SHA-256', encoder.encode(text)).then(toHex);
            }

            sha256(grammarText).then(function (hash) {
                var paths = [baseRailroadPath(), '/railroad/'];
                var idx = 0;

                function loadNext() {
                    if (idx >= paths.length) {
                        $('#' + loadingId).text('Railroad diagram unavailable.');
                        return;
                    }
                    var src = paths[idx] + hash + '.html';
                    diagram.attr('src', src);
                }

                diagram.on('load', function () {
                    $('#' + loadingId).remove();
                    var codeHeight = codeBlock.height();
                    diagram.height(codeHeight > 500 ? codeHeight + 'px' : '500px');
                }).on('error', function () {
                    idx += 1;
                    loadNext();
                });

                loadNext();
            }).catch(function () {
                $('#' + loadingId).text('Railroad diagram unavailable.');
            });
        }
    }

});



jQuery(window).on('load', function() {
    function adjustForScrollbar() {
        if ((parseInt(jQuery('#body-inner').height()) + 83) >= jQuery('#body').height()) {
            jQuery('.nav.nav-next').css({ 'margin-right': getScrollBarWidth() });
        } else {
            jQuery('.nav.nav-next').css({ 'margin-right': 0 });
        }
    }

    // adjust sidebar for scrollbar
    adjustForScrollbar();

    jQuery(window).smartresize(function() {
        adjustForScrollbar();
    });

    // store this page in session
    sessionStorage.setItem(jQuery('body').data('url'), 1);

    // loop through the sessionStorage and see if something should be marked as visited
    for (var url in sessionStorage) {
        if (sessionStorage.getItem(url) == 1) jQuery('[data-nav-id="' + url + '"]').addClass('visited');
    }


    $(".highlightable").highlight(sessionStorage.getItem('search-value'), { element: 'mark' });
});

$(function() {
    $('a[rel="lightbox"]').featherlight({
        root: 'section#body'
    });

    var logo = $('header img')
    var logosrc = logo.attr('src')
    var loadTheme = localStorage.getItem('ss-theme')
    if(loadTheme){
        $('body').attr("class", loadTheme+'-theme');
        $('.change-theme span').removeClass('active')
        $('.change-theme span[data-item='+loadTheme+']').addClass('active')
        $('.change-theme span[data-item='+loadTheme+']').addClass(loadTheme)
    }  
    if(/dark|deep/.test(loadTheme)){
        logo.attr('src', logosrc.replace('logo_v3','logo_v2'))
    }else{
        logo.attr('src', logosrc.replace('logo_v2','logo_v3'))
    }

    $('.change-theme span').click(function(){
        var _this = $(this),
        theme =  _this.data('item')
        if(theme){
            $('body').attr("class", theme+'-theme');
            localStorage.setItem('ss-theme', theme)
        }else{
            $('body').attr("class", '');
            localStorage.setItem('ss-theme', '')
        }
        if(/dark|deep/.test(theme)){
            logo.attr('src', logosrc.replace('logo_v3','logo_v2'))
        }else{
            logo.attr('src', logosrc.replace('logo_v2','logo_v3'))
        }
        _this.addClass('active')
        _this.addClass(theme)
        _this.siblings().attr('class','')
    })
});

window.onload = function(){
    var markdown = document.querySelector('#body'),
    h2s = markdown.querySelectorAll('h2'),
    bookToc = document.querySelector('#TableOfContents');
    if(bookToc){
      var bocs = bookToc.querySelectorAll('a'),
      h2Info = [];
      h2s.forEach(item=>{
        h2Info.push({
          top: item.offsetTop,
          id: item.id
        })
      })
      
      function ScollPostion() {
        var t, l, w, h;
        if (document.documentElement && document.documentElement.scrollTop) {
            t = document.documentElement.scrollTop;
            l = document.documentElement.scrollLeft;
            w = document.documentElement.scrollWidth;
            h = document.documentElement.scrollHeight;
        } else if (document.body) {
            t = document.body.scrollTop;
            l = document.body.scrollLeft;
            w = document.body.scrollWidth;
            h = document.body.scrollHeight;
        }
        return {
            top: t,
            left: l,
            width: w,
            height: h
        };
    }

      function deal(str){
        bocs.forEach(function(item){
          if(item.getAttribute('href').split('#')[1] == str){
            // console.log(item, str)
            item.classList='active'
          }else{
            item.classList=''
          }
        })
      }

      document.body.onscroll = function(e){
        var scrollTop = ScollPostion().top
        h2Info.map(function(item){
          if(Math.abs(scrollTop - item.top)<20){
            deal(item.id)
          }
        })
      }
    }
    
  }

  window.onload = function(){
    var markdown = document.querySelector('#body'),
    h2s = markdown.querySelectorAll('h2'),
    bookToc = document.querySelector('#TableOfContents');
    if(bookToc){
      var bocs = bookToc.querySelectorAll('a'),
      h2Info = [];
      h2s.forEach(item=>{
        h2Info.push({
          top: item.offsetTop,
          id: item.id
        })
      })

      function ScollPostion() {
        var t, l, w, h;
        if (document.documentElement && document.documentElement.scrollTop) {
            t = document.documentElement.scrollTop;
            l = document.documentElement.scrollLeft;
            w = document.documentElement.scrollWidth;
            h = document.documentElement.scrollHeight;
        } else if (document.body) {
            t = document.body.scrollTop;
            l = document.body.scrollLeft;
            w = document.body.scrollWidth;
            h = document.body.scrollHeight;
        }
        return {
            top: t,
            left: l,
            width: w,
            height: h
        };
    }

      function deal(str){
        bocs.forEach(function(item){
          if(item.getAttribute('href').split('#')[1] == str){
            item.classList='active'
          }else{
            item.classList=''
          }
        })
      }

      document.body.onscroll = function(e){
        var scrollTop = ScollPostion().top
        h2Info.map(function(item){
          if(Math.abs(scrollTop - item.top)<20){
            deal(item.id)
          }
        })
      }
    }

  }

jQuery.extend({
    highlight: function(node, re, nodeName, className) {
        if (node.nodeType === 3) {
            var match = node.data.match(re);
            if (match) {
                var highlight = document.createElement(nodeName || 'span');
                highlight.className = className || 'highlight';
                var wordNode = node.splitText(match.index);
                wordNode.splitText(match[0].length);
                var wordClone = wordNode.cloneNode(true);
                highlight.appendChild(wordClone);
                wordNode.parentNode.replaceChild(highlight, wordNode);
                return 1; //skip added node in parent
            }
        } else if ((node.nodeType === 1 && node.childNodes) && // only element nodes that have children
            !/(script|style)/i.test(node.tagName) && // ignore script and style nodes
            !(node.tagName === nodeName.toUpperCase() && node.className === className)) { // skip if already highlighted
            for (var i = 0; i < node.childNodes.length; i++) {
                i += jQuery.highlight(node.childNodes[i], re, nodeName, className);
            }
        }
        return 0;
    }
});

jQuery.fn.unhighlight = function(options) {
    var settings = {
        className: 'highlight',
        element: 'span'
    };
    jQuery.extend(settings, options);

    return this.find(settings.element + "." + settings.className).each(function() {
        var parent = this.parentNode;
        parent.replaceChild(this.firstChild, this);
        parent.normalize();
    }).end();
};

jQuery.fn.highlight = function(words, options) {
    var settings = {
        className: 'highlight',
        element: 'span',
        caseSensitive: false,
        wordsOnly: false
    };
    jQuery.extend(settings, options);

    if (!words) { return; }

    if (words.constructor === String) {
        words = [words];
    }
    words = jQuery.grep(words, function(word, i) {
        return word != '';
    });
    words = jQuery.map(words, function(word, i) {
        return word.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
    });
    if (words.length == 0) { return this; }
    ;

    var flag = settings.caseSensitive ? "" : "i";
    var pattern = "(" + words.join("|") + ")";
    if (settings.wordsOnly) {
        pattern = "\\b" + pattern + "\\b";
    }
    var re = new RegExp(pattern, flag);

    return this.each(function() {
        jQuery.highlight(this, re, settings.element, settings.className);
    });
};

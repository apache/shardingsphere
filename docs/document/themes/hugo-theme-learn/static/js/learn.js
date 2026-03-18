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


    // railroad diagram (generate in-browser to avoid cross-domain CSP blocks)
    (function () {
        if (!$('.tab-panel').length) {
            return;
        }
        var codeBlock = $('.tab-panel code').first();
        var grammarText = (codeBlock.text() || '').trim();
        var diagramFrame = $('#diagram');
        if (!grammarText || !diagramFrame.length) {
            return;
        }

        var loadingId = 'rr-loading';
        diagramFrame.before('<p id="' + loadingId + '">Loading ...</p>');

        function resolveStaticPath(relPath) {
            var parts = window.location.pathname.split('/');
            var docIdx = parts.indexOf('document');
            if (docIdx === -1) {
                return '/' + relPath;
            }
            return '/' + parts.slice(1, docIdx + 2).join('/') + '/' + relPath;
        }

        var railroadAssetsPromise;
        function ensureRailroadAssets() {
            if (railroadAssetsPromise) {
                return railroadAssetsPromise;
            }
            railroadAssetsPromise = new Promise(function (resolve, reject) {
                function onReady() {
                    resolve();
                }

                // load CSS once
                if (!document.getElementById('railroad-diagrams-css')) {
                    var link = document.createElement('link');
                    link.id = 'railroad-diagrams-css';
                    link.rel = 'stylesheet';
                    link.href = resolveStaticPath('css/railroad-diagrams.css');
                    document.head.appendChild(link);
                }

                if (window.Diagram && window.NonTerminal && window.Terminal) {
                    resolve();
                    return;
                }

                var script = document.createElement('script');
                script.src = resolveStaticPath('js/railroad-diagrams.js');
                script.onload = onReady;
                script.onerror = function () {
                    reject(new Error('Railroad script load failed'));
                };
                document.head.appendChild(script);
            });
            return railroadAssetsPromise;
        }

        function tokenize(text) {
            var regex = /::=|\?|\*|\+|\||\(|\)|\[|\]|'[^']*'|[A-Za-z_][A-Za-z0-9_-]*|,/g;
            var tokens = text.match(regex);
            return tokens ? tokens : [];
        }

        function parseExpression(tokens, indexRef) {
            var terms = [parseTerm(tokens, indexRef)];
            while (tokens[indexRef.idx] === '|') {
                indexRef.idx += 1;
                terms.push(parseTerm(tokens, indexRef));
            }
            if (terms.length === 1) {
                return terms[0];
            }
            return { type: 'choice', options: terms };
        }

        function parseTerm(tokens, indexRef) {
            var items = [];
            while (indexRef.idx < tokens.length) {
                var tok = tokens[indexRef.idx];
                if (tok === '|' || tok === ')' || tok === ']') {
                    break;
                }
                items.push(parseFactor(tokens, indexRef));
            }
            if (items.length === 1) {
                return items[0];
            }
            return { type: 'sequence', items: items };
        }

        function parseFactor(tokens, indexRef) {
            var node = parsePrimary(tokens, indexRef);
            var tok = tokens[indexRef.idx];
            if (tok === '?' || tok === '*' || tok === '+') {
                indexRef.idx += 1;
                if (tok === '?') {
                    node = { type: 'optional', item: node };
                } else if (tok === '*') {
                    node = { type: 'zeroOrMore', item: node };
                } else if (tok === '+') {
                    node = { type: 'oneOrMore', item: node };
                }
            }
            return node;
        }

        function parsePrimary(tokens, indexRef) {
            var tok = tokens[indexRef.idx];
            indexRef.idx += 1;
            if (!tok) {
                return { type: 'terminal', value: '' };
            }
            if (tok === '(') {
                var expr = parseExpression(tokens, indexRef);
                indexRef.idx += 1; // skip ')'
                return expr;
            }
            if (tok === '[') {
                var optExpr = parseExpression(tokens, indexRef);
                indexRef.idx += 1; // skip ']'
                return { type: 'optional', item: optExpr };
            }
            if (tok[0] === "'" && tok.length >= 2) {
                return { type: 'terminal', value: tok.slice(1, -1) };
            }
            return { type: 'nonterminal', value: tok };
        }

        function astToRailroad(node) {
            switch (node.type) {
                case 'terminal':
                    return new Terminal(node.value);
                case 'nonterminal':
                    return new NonTerminal(node.value);
                case 'sequence':
                    return Sequence.apply(null, node.items.map(astToRailroad));
                case 'choice':
                    var opts = node.options.map(astToRailroad);
                    return Choice.apply(null, [0].concat(opts));
                case 'optional':
                    return new Optional(astToRailroad(node.item), 'skip');
                case 'zeroOrMore':
                    return new ZeroOrMore(astToRailroad(node.item));
                case 'oneOrMore':
                    return new OneOrMore(astToRailroad(node.item));
                default:
                    return new Terminal('');
            }
        }

        function parseDefinitions(text) {
            var blocks = text.split(/\n\s*\n/);
            var defs = [];
            for (var i = 0; i < blocks.length; i++) {
                var block = blocks[i].trim();
                if (!block) {
                    continue;
                }
                var parts = block.split('::=');
                if (parts.length < 2) {
                    continue;
                }
                var name = parts[0].trim().split(/\s+/)[0];
                var rhs = parts.slice(1).join('::=').trim();
                defs.push({ name: name, rhs: rhs });
            }
            return defs;
        }

        function renderRailroad(grammar) {
            var defs = parseDefinitions(grammar);
            var htmlParts = ['<style>svg.railroad-diagram{background:transparent;} .rr-title{font:bold 14px Verdana, sans-serif;margin:10px 0 4px;}</style>'];
            for (var i = 0; i < defs.length; i++) {
                var def = defs[i];
                try {
                    var tokens = tokenize(def.rhs);
                    var ast = parseExpression(tokens, { idx: 0 });
                    // Diagram takes items as varargs; call as a factory to wrap arguments correctly
                    var diagram = Diagram(astToRailroad(ast));
                    htmlParts.push('<p class=\"rr-title\">' + def.name + ':</p>');
                    htmlParts.push('<div class=\"rr-wrapper\">' + diagram.toString() + '</div>');
                } catch (e) {
                    htmlParts.push('<p class=\"rr-title\">' + def.name + ':</p><p>Railroad diagram unavailable.</p>');
                }
            }
            return htmlParts.join('\n');
        }

        ensureRailroadAssets().then(function () {
            try {
                var container = $('<div class=\"railroad-diagrams\"></div>');
                container.html(renderRailroad(grammarText));
                diagramFrame.replaceWith(container);
            } catch (e) {
                $('#' + loadingId).text('Railroad diagram unavailable.');
            } finally {
                $('#' + loadingId).remove();
            }
        }).catch(function () {
            $('#' + loadingId).text('Railroad diagram unavailable.');
        });
    })();

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

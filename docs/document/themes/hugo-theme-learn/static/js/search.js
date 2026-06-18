var lunrIndex, pagesIndex;

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

// Initialize lunrjs using our generated index file
function initLunr() {
    if (!endsWith(baseurl,"/")){
        baseurl = baseurl+'/'
    };

    // First retrieve the index file
    $.getJSON(baseurl +"index.json")
        .done(function(index) {
            pagesIndex = index;
            // Set up lunrjs by declaring the fields we use
            // Also provide their boost level for the ranking
            lunrIndex = lunr(function() {
                this.ref("uri");
                this.field('title', {
		    boost: 15
                });
                this.field('tags', {
		    boost: 10
                });
                this.field("content", {
		    boost: 5
                });
				
                this.pipeline.remove(lunr.stemmer);
                this.searchPipeline.remove(lunr.stemmer);
				
                // Feed lunr with each file and let lunr actually index them
                pagesIndex.forEach(function(page) {
		    this.add(page);
                }, this);
            })
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.error("Error getting Hugo index file:", err);
        });
}

/**
 * Trigger a search in lunr and transform the result
 *
 * @param  {String} query
 * @return {Array}  results
 */
function search(queryTerm) {
    // Find the item in our index corresponding to the lunr one to have more info
    return lunrIndex.search(queryTerm+"^100"+" "+queryTerm+"*^10"+" "+"*"+queryTerm+"^10"+" "+queryTerm+"~2^1").map(function(result) {
            return pagesIndex.filter(function(page) {
                return page.uri === result.ref;
            })[0];
        });
}

// Let's get started
initLunr();
$( document ).ready(function() {
    var searchList = new autoComplete({
        /* selector for the search box element */
        selector: $("#search-by").get(0),
        /* source is the callback to perform the search */
        source: function(term, response) {
            response(search(term));
        },
        /* renderItem displays individual search results */
        renderItem: function(item, term) {
            var numContextWords = 2;
            var text = item.content.match(
                "(?:\\s?(?:[\\w]+)\\s?){0,"+numContextWords+"}" +
                    term+"(?:\\s?(?:[\\w]+)\\s?){0,"+numContextWords+"}");
            item.context = text;
            var divcontext = document.createElement("div");
            divcontext.className = "context";
            divcontext.innerText = (item.context || '');
            var divsuggestion = document.createElement("div");
            divsuggestion.className = "autocomplete-suggestion";
            divsuggestion.setAttribute("data-term", term);
            divsuggestion.setAttribute("data-title", item.title);
            divsuggestion.setAttribute("data-uri", item.uri);
            divsuggestion.setAttribute("data-context", item.context);
            divsuggestion.innerText = '» ' + item.title;
            divsuggestion.appendChild(divcontext);
            return divsuggestion.outerHTML;
        },
        /* onSelect callback fires when a search suggestion is chosen */
        onSelect: function(e, term, item) {
            location.href = item.getAttribute('data-uri');
        }
    });
});

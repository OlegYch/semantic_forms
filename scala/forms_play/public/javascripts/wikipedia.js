// https://github.com/dbpedia/lookup
// wget "http://lookup.dbpedia.org/api/search.asmx/PrefixSearch?QueryClass=&MaxHits=10&QueryString=berl" --header='Accept: application/json'

/* pulldown menu in <input> does show on Chrome; Opera, Android ????
see
https://jqueryui.com/autocomplete/
https://github.com/RubenVerborgh/dbpedia-lookup-page
alternate implementation, abandoned: http://blog.teamtreehouse.com/creating-autocomplete-dropdowns-datalist-element
TODO: translate in Scala:
https://github.com/scala-js/scala-js-jquery
https://www.google.fr/search?q=ajax+example+scala.js
 */

var resultsCount = 15;
var urlReqPrefix = "http://lookup.dbpedia.org/api/search.asmx/PrefixSearch?QueryClass=&MaxHits=" +
      resultsCount + "&QueryString=" ;
// var urlReqPrefix = "/lookup?q=";

// function XHRCompletion (url) {
//     return new Promise (function(resolve, reject) {
//         $.when($.ajax({
//             url: "http://lookup.dbpedia.org/api/search/PrefixSearch",
//             data: { MaxHits: resultsCount, QueryString: request.term },
//             dataType: "json",
//             timeout: 5000
//         }))
//          .then(function(response) { resolve(response) })
//          .catch(function(error) { reject(error) })
//     })
// }

$(document).ready(function() {
    var topics = [];
    $(".sf-standard-form").on('focus', '.hasLookup', function(event) {
        $(this).autocomplete({
            autoFocus: true,
            minlength: 3,
            search: function() {
                $(this).addClass('sf-suggestion-search')
            },
            open: function() {
                $(this).removeClass('sf-suggestion-search')
            },
            select: function( event, ui ) {
                console.log( "Topic chosen label event ");
                console.log($(this));
                console.log( "Topic chosen label ui");
                console.log(ui);
                $emptyFields = $(this).siblings().filter(function(index) { return $(this).val() == ''}).length;
                console.log('Champs vides : '+ $emptyFields);
                if ($emptyFields === 0) {
                    addedWidget = cloneWidget($(this))
                }
            },
            source: function(request, callback) {
                console.log("Déclenche l'événement :")

		// TODO add QueryClass
		// compare results: QueryClass=person , and ?QueryClass=place
		// view-source:http://lookup.dbpedia.org/api/search/PrefixSearch?QueryClass=Person&QueryString=berlin
		// view-source:http://lookup.dbpedia.org/api/search/PrefixSearch?QueryClass=Place&QueryString=berlin

		// QueryClass comes from attribute data-rdf-type in <input> tag , but data-rdf-type is a full URI !

                var typeName
                var $el = $(event.target);
                if ($el) {
                var type = $el.attr('data-rdf-type').split('/');
                    if (type) {
                      typeName = type[type.length - 1];
                    }
                }

                $.ajax({
                    url: "http://lookup.dbpedia.org/api/search/PrefixSearch",
                    data: { MaxHits: resultsCount, QueryClass: typeName, QueryString: request.term },
                    dataType: "json",
                    timeout: 5000
                }).done(function (response) {
                    console.log(response)
                    callback(response.results.map(function (m) {
                        // topics[m.label] = m.uri;
                        return { "label": m.label + " - " +
                        cutStringAfterCharacter(m.description, '.'), "value": m.uri }
                    }));
                }).fail(function (error){
                    $.ajax({
                        url: "/lookup",
                        data: { MaxHits: resultsCount, QueryClass: typeName, QueryString: request.term + "*" },
                        dataType: "json",
                        timeout: 5000
                    }).done(function(response) {
                        console.log('Done');
                        var topics = [];
                        callback(response.results.map(function (m) {
                            // topics[m.label] = m.uri;
                            return { "label": m.label /* + " - " +
                            cutStringAfterCharacter(m.description, '.') */, "value": m.uri }
                        }))
                    });
                })
            }
        })
    });
});

function cutStringAfterCharacter(s, c) {
    if (!(s === null)) {
        var n = s.indexOf(c);
        return s.substring(0, n != -1 ? n : s.length);
    } else {
        return s;
    }
};

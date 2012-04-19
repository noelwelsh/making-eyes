function toList(elt, f) {
    return $(elt).map(function (idx, elt) {
      return '<li>'+f(elt)+'</li>';
    }).get().join('');
}

function toLink(elt) {
  var $elt = $(elt);
  var link = '<a href="#' + $elt.attr('id') + '">' + $elt.text() + '</a>';
  return link;
}

$(function() {
  var headings = toList($('h2'), function(h2) {
    return toLink(h2) + "<ul>" + toList($(h2).nextUntil('h2').filter('h3'), function(elt) {
      return toLink(elt);
    }) + "</ul>";
  });

  $('#navbar').append('<ul class="nav nav-list"><li class="nav-header">On This Page</li>' + headings + '</ul>')
});

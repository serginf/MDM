$(document).ready(function(){
// your code
    autoResize();
});

function autoResize(){
    var height = $(window).height();
    //console.log(height);
    document.getElementById("ViewiframeGraph").height = height-200;
}

function resize() {
    window.parent.autoResize();
}

$(window).on('resize', resize);

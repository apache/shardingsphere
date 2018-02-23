$(document).ready(function() { 
	var outTimer;
	$('div [data-index]').hover(function() {
		var $this = $(this);
		clearTimeout(outTimer);
        hoverTimer = setTimeout(function(){
			$this.addClass("qc-unit-active").siblings().removeClass("qc-unit-active");
		}, 150);
	});
}); 

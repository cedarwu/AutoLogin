
$.get("http://counter.cedar.tk");
$.get( "http://counter.cedar.tk/query/autologindownload", function( data ) {
	console.log(data);
  	$( "#downcount" ).html( "下载次数：" + data );
});

$( "#link1" ).click(function() {
  $.get("http://counter.cedar.tk/autologindownload");
});
$( "#link2" ).click(function() {
  $.get("http://counter.cedar.tk/autologindownload");
});
$(".historyapk").click(function() {
  $.get("http://counter.cedar.tk/autologindownload");
});
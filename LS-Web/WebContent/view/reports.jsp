<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<!-- JQuery -->
<script src="../static/jquery/jquery-1.10.2.min.js" type="text/javascript"></script>
<script src="../static/jquery/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<link href="../static/jquery/jquery-ui-1.10.3.custom.min.css" rel="stylesheet" type="text/css" />

<!-- Bootstrap core CSS -->
<link href="../static/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../static/bootstrap/css/docs.css" rel="stylesheet">

<!-- Bootstrap javaScript -->
<script src="../static/bootstrap/js/bootstrap.js" type="text/javascript"></script>

<!-- Am charts -->
<script src="../static/amcharts/amcharts.js" type="text/javascript"></script>
<script src="../static/amcharts/serial.js" type="text/javascript"></script>

<!-- Custom styles for this template -->
<link href="../static/css/fancylog.css" rel="stylesheet">

<link rel="shortcut icon" href="http://cdn.sstatic.net/stackoverflow/img/favicon.ico">

<style type="text/css" id="holderjs-style"></style>

<title>Fancy Log Reports</title>
 <script type="text/javascript">

    			var chartData=[];
    			'<c:forEach var="report" items="${data}">';
    			chartData.push({
					date: new Date(${report.date}),
					y35: ${report.y35},
					doubleSeat: ${report.doubleSeat},
					eurExtraLegRoomL: ${report.eurExtraLegRoomL},
					icaExtraLegRoomS: ${report.icaExtraLegRoomS},
					eurExtraLegRoomS: ${report.eurExtraLegRoomS},
					icaExtraLegRoomL: ${report.icaExtraLegRoomL},
					dlEconomyComfort: ${report.dlEconomyComfort},
					seatChoiceAF: ${report.seatChoiceAF},
					seatChoiceCategoryAF: ${report.seatChoiceCategoryAF},
					seatDuo: ${report.seatDuo},
					seatFront: ${report.seatFront},
					seatUperDeck: ${report.seatUperDeck},
					paidUpgradeKL: ${report.paidUpgradeKL},
					paidUpgradeAFEBMediumhaul: ${report.paidUpgradeAFEBMediumhaul},
					paidUpgradeAFEB: ${report.paidUpgradeAFEB},
					paidUpgradeAFEP: ${report.paidUpgradeAFEP},
					paidUpgradeAFPB: ${report.paidUpgradeAFPB},
					wwPeice: ${report.wwPeice},
					afPeiceOnline: ${report.afPeiceOnline},
					afPeice: ${report.afPeice},
					co2: ${report.co2},
					specialMeal: ${report.specialMeal},
					standradMeal: ${report.standradMeal},
					alaCarte: ${report.alaCarte},
					afPU:${report.afPU},
					afABA:${report.afABA},
					paidSeats:${report.paidSeats}
				});
				'</c:forEach>';
    			
            	var chart = AmCharts.makeChart("chartdiv", {
                    type: "serial",
                    pathToImages: "../static/amcharts/images/",
                    dataProvider: chartData,
                    categoryField: "date",
                    titles: [{
                        "text": "Anachillary Offers",
                        "size": 15
                    }],                    
                    categoryAxis: {
                        parseDates: true,
                        gridAlpha: 0.15,
                        minorGridEnabled: true,
                        axisColor: "#DADADA",
                        minPeriod:"hh",
                    },
                    graphs: [{
                    	type: "line",
                        title: "Paid Seats",
                        valueField: "paidSeats",
                        bullet: "round",
                        bulletBorderColor: "#66FF00",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#66FF00",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                    	hidden: true,
                        title: "Economy Comfort",
                        valueField: "y35",
                        bullet: "round",
                        bulletBorderColor: "#66FF33",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#66FF33",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Double Seat",
                        hidden: true,
                        valueField: "doubleSeat",
                        bullet: "round",
                        bulletBorderColor: "#3399FF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#3399FF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "EUR Extra Legroom L",
                        hidden: true,
                        valueField: "eurExtraLegRoomL",
                        bullet: "round",
                        bulletBorderColor: "#FF9933",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#FF9933",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "ICA Extra Legroom S",
                        hidden: true,
                        valueField: "icaExtraLegRoomS",
                        bullet: "round",
                        bulletBorderColor: "#FF0066",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#666666",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "EUR Extra Legroom S",
                        hidden: true,
                        valueField: "eurExtraLegRoomS",
                        bullet: "round",
                        bulletBorderColor: "#944D70",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#944D70",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "ICA Extra Legroom L",
                        hidden: true,
                        valueField: "icaExtraLegRoomL",
                        bullet: "round",
                        bulletBorderColor: "#990033",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#990033",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Dleta Economy Comfort",
                        hidden: true,
                        valueField: "dlEconomyComfort",
                        bullet: "round",
                        bulletBorderColor: "#990099",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#990099",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Seat Choice AF",
                        valueField: "seatChoiceAF",
                        bullet: "round",
                        bulletBorderColor: "#FF4D4D",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#FF4D4D",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                    	hidden: true,
                        title: "Seat Choice Category AF",
                        valueField: "seatChoiceCategoryAF",
                        bullet: "round",
                        bulletBorderColor: "#666666",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#666666",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Seat Duo",
                        hidden: true,
                        valueField: "seatDuo",
                        bullet: "round",
                        bulletBorderColor: "#FF4D4D",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#FF4D4D",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Seat Front",
                        valueField: "seatFront",
                        hidden: true,
                        bullet: "round",
                        bulletBorderColor: "#CC6600",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#CC6600",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Seat Upper Deck",
                        valueField: "seatUperDeck",
                        hidden: true,
                        bullet: "round",
                        bulletBorderColor: "#99CCFF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#99CCFF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade - KL",
                        valueField: "paidUpgradeKL",
                        bullet: "round",
                        bulletBorderColor: "#00FFFF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#00FFFF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade - AF",
                        valueField: "afPU",
                        bullet: "round",
                        bulletBorderColor: "#336600",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#336600",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade-AF E-B Medium Haul",
                        hidden: true,
                        valueField: "paidUpgradeAFEBMediumhaul",
                        bullet: "round",
                        bulletBorderColor: "#3366FF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#3366FF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade-AF E-B",
                        hidden: true,
                        valueField: "paidUpgradeAFEB",
                        bullet: "round",
                        bulletBorderColor: "#3333CC",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#3333CC",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade-AF E-P",
                        hidden: true,
                        valueField: "paidUpgradeAFEP",
                        bullet: "round",
                        bulletBorderColor: "#666699",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#666699",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Paid Upgrade-AF P-B",
                        hidden: true,
                        valueField: "paidUpgradeAFPB",
                        bullet: "round",
                        bulletBorderColor: "#6600CC",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#6600CC",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "KL ABA",
                        valueField: "wwPeice",
                        bullet: "round",
                        bulletBorderColor: "#33CCCC",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#33CCCC",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "AF ABA",
                        valueField: "afABA",
                        bullet: "round",
                        bulletBorderColor: "#33CC00",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#33CC00",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "AF Peice Online",
                        hidden: true,
                        valueField: "afPeiceOnline",
                        bullet: "round",
                        bulletBorderColor: "#3333FF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#3333FF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "AF Peice",
                        valueField: "afPeice",
                        hidden: true,
                        bullet: "round",
                        bulletBorderColor: "#9999FF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#9999FF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "CO2",
                        valueField: "co2",
                        hidden: true,
                        bullet: "round",
                        bulletBorderColor: "#99EBFF",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#99EBFF",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Special Meal",
                        valueField: "specialMeal",
                        bullet: "round",
                        hidden: true,
                        bulletBorderColor: "#CCCC00",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#CCCC00",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "Standrad Meal",
                        valueField: "standradMeal",
                        bullet: "round",
                        bulletBorderColor: "#666600",
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        hidden: true,
                        lineColor: "#666600",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    },{
                    	type: "line",
                        title: "A la Carte",
                        valueField: "alaCarte",
                        bullet: "round",
                        bulletBorderColor: "#B88A00",
                        hidden: true,
                        bulletBorderAlpha: 1,
                        lineThickness: 2,
                        lineColor: "#B88A00",
                        balloonText: "[[category]]<br><b><span style='font-size:14px;'>value: [[value]]</span></b>"
                    }],
                    chartCursor: {
                        fullWidth:true,
                        cursorPosition:"mouse",
                        categoryBalloonDateFormat:"JJ:NN, DD MMMM",
                        cursorAlpha:0.1,
                        pan: true
                    },
                    chartScrollbar: {
                        scrollbarHeight: 40,
                        color: "#FFFFFF",
                        autoGridCount: true,
                        graph: "g1"
                    },

                    mouseWheelZoomEnabled:true,
                    legend: {
                    	position:"right",
                        valueText: "[[value]]",
                        valueWidth: 40,
                        valueAlign: "center",
                        markerType: "bubble"
                    },
                });
            	 chart.addListener("dataUpdated", zoomChart);
                 chart.write("chartdiv");
                 
                 // this method is called when chart is first inited as we listen for "dataUpdated" event
                 function zoomChart() {
                     // different zoom methods can be used - zoomToIndexes, zoomToDates, zoomToCategoryValues
                     chart.zoomToIndexes(chartData.length - 40, chartData.length - 1);
                 }

        </script>
</head>
<body>

<%@include file="jspf/header.jspf" %>

<div class="bs-example page-fancylog">
<div id="chartdiv" style="width:100%; height:800px;"></div>
</div>

</body>
</html>
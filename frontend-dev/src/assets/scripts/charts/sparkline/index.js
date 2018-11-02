import * as $ from 'jquery';
import 'jquery-sparkline';
import {debounce} from 'lodash';
import {COLORS} from '../../constants/colors';

export default (function () {
    // ------------------------------------------------------
    // @Dashboard Sparklines
    // ------------------------------------------------------

    const drawSparklines = () => {
        let sparkLineObject = $('#weeklyWorkload');
        if (sparkLineObject.length > 0) {
            let weeklyWorkloadSparkLines = [5, 8, 9, 3.5, 2.25, 0, 0];
            let workloadHoursSum = weeklyWorkloadSparkLines.reduce((a, b) => a + b, 0);

            sparkLineObject.sparkline(weeklyWorkloadSparkLines, {
                type: 'bar',
                height: '20',
                barWidth: '3',
                resize: true,
                barSpacing: '3',
                barColor: '#4caf50',

                tooltipFormat: '{{offset:offset}}: {{value}} hours',
                tooltipValueLookups: {
                    'offset': {
                        0: 'Monday',
                        1: 'Tuesday',
                        2: 'Wednesday',
                        3: 'Thursday',
                        4: 'Friday',
                        5: 'Saturday',
                        6: 'Sunday'
                    }
                }
            });
            //set the text for weekly workload ~~ if result had to be round
            if (workloadHoursSum % 1 > 0) {
                workloadHoursSum = '~' + Math.round(workloadHoursSum);
            }
            $('#weeklyWorkloadHours').text(workloadHoursSum);
        }

        sparkLineObject = $('#sparklinedash2');
        if (sparkLineObject.length > 0) {
            sparkLineObject.sparkline([0, 5, 6, 10, 9, 12, 4, 9], {
                type: 'bar',
                height: '20',
                barWidth: '3',
                resize: true,
                barSpacing: '3',
                barColor: '#9675ce',
            });
        }

        sparkLineObject = $('#sparklinedash3');
        if (sparkLineObject.length > 0) {
            sparkLineObject.sparkline([0, 5, 6, 10, 9, 12, 4, 9], {
                type: 'bar',
                height: '20',
                barWidth: '3',
                resize: true,
                barSpacing: '3',
                barColor: '#03a9f3',
            });
        }

        sparkLineObject = $('#overlappingMeetings');
        if (sparkLineObject.length > 0) {
            let weeklyMeetingsCount = 20;
            let overlappingMeetingsSparkLines = [1, 0, 3, 0, 0, 0, 0];
            let overlappingMeetingsCount = overlappingMeetingsSparkLines.reduce((a, b,) => a + b, 0);
            let percentage = 100 / weeklyMeetingsCount * overlappingMeetingsCount;

            sparkLineObject.sparkline(overlappingMeetingsSparkLines, {
                type: 'bar',
                height: '20',
                barWidth: '3',
                resize: true,
                barSpacing: '3',
                barColor: '#f96262',

                tooltipFormat: '{{offset:offset}}: {{value}} meetings',
                tooltipValueLookups: {
                    'offset': {
                        0: 'Monday',
                        1: 'Tuesday',
                        2: 'Wednesday',
                        3: 'Thursday',
                        4: 'Friday',
                        5: 'Saturday',
                        6: 'Sunday'
                    }
                }
            });

            $('#overlappingMeetingsPercentageValue').text(percentage);
        }
    };

    drawSparklines();

    // Redraw sparkLines on resize
    $(window).resize(debounce(drawSparklines, 150));

    // ------------------------------------------------------
    // @Other SparkLines
    // ------------------------------------------------------

    $('#sparkline').sparkline(
        [5, 6, 7, 9, 9, 5, 3, 2, 2, 4, 6, 7],
        {
            type: 'line',
            resize: true,
            height: '20',
        }
    );

    $('#compositebar').sparkline(
        'html',
        {
            type: 'bar',
            resize: true,
            barColor: '#aaf',
            height: '20',
        }
    );

    $('#compositebar').sparkline(
        [4, 1, 5, 7, 9, 9, 8, 7, 6, 6, 4, 7, 8, 4, 3, 2, 2, 5, 6, 7],
        {
            composite: true,
            fillColor: false,
            lineColor: 'red',
            resize: true,
            height: '20',
        }
    );

    $('#normalline').sparkline(
        'html',
        {
            fillColor: false,
            normalRangeMin: -1,
            resize: true,
            normalRangeMax: 8,
            height: '20',
        }
    );

    $('.sparktristate').sparkline(
        'html',
        {
            type: 'tristate',
            resize: true,
            height: '20',
        }
    );

    $('.sparktristatecols').sparkline(
        'html',
        {
            type: 'tristate',
            colorMap: {
                '-2': '#fa7',
                resize: true,
                '2': '#44f',
                height: '20',
            },
        }
    );

    const values = [5, 4, 5, -2, 0, 3, -5, 6, 7, 9, 9, 5, -3, -2, 2, -4];
    const valuesAlt = [1, 1, 0, 1, -1, -1, 1, -1, 0, 0, 1, 1];

    $('.sparkline').sparkline(values, {
        type: 'line',
        barWidth: 4,
        barSpacing: 5,
        fillColor: '',
        lineColor: COLORS['red-500'],
        lineWidth: 2,
        spotRadius: 3,
        spotColor: COLORS['red-500'],
        maxSpotColor: COLORS['red-500'],
        minSpotColor: COLORS['red-500'],
        highlightSpotColor: COLORS['red-500'],
        highlightLineColor: '',
        tooltipSuffix: ' Bzzt',
        tooltipPrefix: 'Hello ',
        width: 100,
        height: undefined,
        barColor: '9f0',
        negBarColor: 'ff0',
        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });

    $('.sparkbar').sparkline(values, {
        type: 'bar',
        barWidth: 4,
        barSpacing: 1,
        fillColor: '',
        lineColor: COLORS['deep-purple-500'],
        tooltipSuffix: 'Celsius',
        width: 100,
        barColor: '39f',
        negBarColor: COLORS['deep-purple-500'],
        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });

    $('.sparktri').sparkline(valuesAlt, {
        type: 'tristate',
        barWidth: 4,
        barSpacing: 1,
        fillColor: '',
        lineColor: COLORS['light-blue-500'],
        tooltipSuffix: 'Celsius',
        width: 100,
        barColor: COLORS['light-blue-500'],
        posBarColor: COLORS['light-blue-500'],
        negBarColor: 'f90',
        zeroBarColor: '000',
        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });

    $('.sparkdisc').sparkline(values, {
        type: 'discrete',
        barWidth: 4,
        barSpacing: 5,
        fillColor: '',
        lineColor: '9f0',
        tooltipSuffix: 'Celsius',
        width: 100,
        barColor: '9f0',

        negBarColor: 'f90',

        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });

    $('.sparkbull').sparkline(values, {
        type: 'bullet',
        barWidth: 4,
        barSpacing: 5,
        fillColor: '',
        lineColor: COLORS['amber-500'],
        tooltipSuffix: 'Celsius',
        height: 'auto',
        width: 'auto',
        targetWidth: 'auto',
        barColor: COLORS['amber-500'],
        negBarColor: 'ff0',
        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });

    $('.sparkbox').sparkline(values, {
        type: 'box',
        barWidth: 4,
        barSpacing: 5,
        fillColor: '',
        lineColor: '9f0',
        tooltipSuffix: 'Celsius',
        width: 100,
        barColor: '9f0',
        negBarColor: 'ff0',
        stackedBarColor: ['ff0', '9f0', '999', 'f60'],
        sliceColors: ['ff0', '9f0', '000', 'f60'],
        offset: '30',
        borderWidth: 1,
        borderColor: '000',
    });
}())

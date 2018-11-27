import * as $ from 'jquery';
import 'clockpicker/dist/bootstrap-clockpicker.min'
import 'clockpicker/dist/bootstrap-clockpicker.min.css'

export default (function () {
    $('.clockpicker').clockpicker({
        autoclose: true,
    });
}())
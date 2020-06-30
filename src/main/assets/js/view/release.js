import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { releaseBaseMethods } from "../baseController";

export function initRelease() {
    return new Vue({
        el: '#releaseVue',
        mixins: [releaseBaseMethods],
        data: {
            mode: false
        },
        methods: {
            sendForm: function () {
                $("#release-form").submit();
                $("#confirm-modal").modal('hide');
            }
        }
    });
}
import Vue from 'vue/dist/vue'

import { baseMethods } from "../baseController";

export function initPrVue(baseSort, baseFilter, baseFilterField) {
    return new Vue({
        el: '#prVue',
        mixins: [baseMethods],
        data: {
            path: 'pr'
        },
        mounted: function () {
            this.checkCache(baseSort, baseFilter, baseFilterField);
        },
        methods: {}
    });
}
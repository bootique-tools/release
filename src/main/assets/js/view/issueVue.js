import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { baseMethods } from "../baseController";

export function initIssueVue(baseSort, baseFilters, baseField) {
    return new Vue({
        el: '#issueVue',
        mixins: [baseMethods],
        data: {
            path: 'issue'
        },
        mounted: function () {
            this.checkCache(baseSort, baseFilters, baseField);
        },
        methods: {}
    });
}
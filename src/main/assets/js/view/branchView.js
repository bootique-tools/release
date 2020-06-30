import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { defaultBaseMethods } from "../baseController";

export function initBranchView() {
    return new Vue({
        el: '#branchVue',
        mixins: [defaultBaseMethods],
        data: {
            branchTitle: '',
            path: 'branches'
        },
        beforeMount() {
            let currApp = this;
            if (sessionStorage.showProcess === 'initBranchView') {
                currApp.checkStatus();
            }
        },
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            branchTitle: function (val) {
                this.disableStartButton();
            },
        },
        methods: {
            startTask: function (task) {
                let currApp = this;
                sessionStorage.showProcess = 'initBranchView';
                currApp.progress = 0;
                axios.get(`/ui/branches/${String(task)}?branchTitle=${this.branchTitle}&selectedModules=${JSON.stringify(currApp.selectedModules)}`)
                .then(function (response) {
                    currApp.checkStatus();
                })
                .catch(function () {
                 console.log("Error in " + task);
                 window.sessionStorage.removeItem('showProcess');
             })
            },
        }
    });
}
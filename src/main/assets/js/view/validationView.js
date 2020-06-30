import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { releaseBaseMethods } from "../baseController";

export function initValidationView() {
    return new Vue({
        el: '#validationVue',
        mixins: [releaseBaseMethods],
        beforeMount(){
            let currApp = this;
            if (sessionStorage.showProcess === 'initValidationView') {
                currApp.checkStatus();
            }
        },
        methods: {
            validate: function () {
                let currApp = this;
                sessionStorage.showProcess = 'initValidationView';
                this.$showProcessGlobal = true;
                axios.get(`/ui/validation/validate?releaseVersion=${currApp.releaseVersion}&nextDevVersion=${currApp.nextDevVersion}&selectedModules=${JSON.stringify(currApp.selectedModules)}`)
                    .then(function () {
                    currApp.checkStatus();
                })
                .catch(function () {
                 console.log("Error while validation.");
                 window.sessionStorage.removeItem('showProcess');
             })
            },
        },
    });
}
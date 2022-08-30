import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'


import {baseMethods, defaultBaseMethods} from "../baseController";


export function initMavenVue() {
    return new Vue({
        el: '#mavenVue',
        mixins: [baseMethods,defaultBaseMethods],
        data: {
            showProcess: false,
            progress: 0,
            statusArr: null,
            verifyButton: true,
            path: 'release'
        },
        beforeMount() {
            if (sessionStorage.showProcess === 'initMavenVue') {
                this.verify();
            }
        },
        mounted: function () {
            this.checkCache(null, null, null);
        },
        methods: {
            additionalMethod: function (app) {
                if (sessionStorage.showProcess != null) {
                    this.verifyButton = true;
                } else {
                    app.verifyButton = sessionStorage.showProcess != null;
                }
            },
            verify: function () {
                let currApp = this;
                sessionStorage.showProcess = 'initMavenVue';
                currApp.showProcess = true;
                currApp.progress = 0;
                this.$showProcessGlobal = true;
                axios.post(`/ui/maven/verify`)
                    .then(function () {
                        currApp.connectJobStatusWebsocket();
                    })
                    .catch(function () {
                        console.log("Error start mvn verify.");
                        window.sessionStorage.removeItem('showProcess');
                    })
            },
        }
    });
}
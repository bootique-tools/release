import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import {baseMethods} from "../baseController";

export function initMavenVue() {
    return new Vue({
        el: '#mavenVue',
        mixins: [baseMethods],
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
                currApp.showProcess = true;
                sessionStorage.showProcess = 'initMavenVue';
                this.$showProcessGlobal = true;
                axios.post(`/ui/maven/verify`)
                    .then(function (response) {
                        currApp.checkStatus();
                    })
                    .catch(function () {
                        console.log("Error start mvn verify.");
                        window.sessionStorage.removeItem('showProcess');
                        window.sessionStorage.removeItem('showProcess');
                    })
            },
            checkStatus: function () {
                let currApp = this;
                let intervalCheck = setInterval(function () {
                    axios.get(`/ui/release/process/status`)
                        .then(function (response) {
                            currApp.progress = response.data.percent.percent;
                            currApp.statusArr = response.data.results;
                            currApp.stageName = response.data.name;
                            if (response.data.percent.percent === 100) {
                                clearInterval(intervalCheck);
                            }
                        })
                        .catch(function () {
                            console.log("Error in checking status.");
                            window.sessionStorage.removeItem('showProcess');
                        })
                }, 1200);
            },
        }
    });
}
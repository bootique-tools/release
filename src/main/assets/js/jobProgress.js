import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

export function jobProgress() {
    return new Vue({
        el: '#jobProgress',
        data: {
            percentProgress: null,
            showLink: false,
            lastJobLink: null,
            webSocket: null,
            currentJobStatus: null
        },
        beforeMount() {
            if (sessionStorage.showProcess != null) {
                this.connectWS();
            }
        },
        watch: {
            $showProcessGlobal() {
                if (this.$showProcessGlobal) {
                    this.connectWS();
                }
            }
        },
        methods: {
            disableLink: function () {
                let currApp = this;
                if (currApp.percentProgress === '100%') {
                    currApp.showLink = false;
                }
            },
            connectWS: function () {
                let currApp = this;
                // open the connection if one does not exist
                if (currApp.webSocket !== null && currApp.webSocket.readyState !== WebSocket.CLOSED) {
                    return;
                }
                // Create a websocket
                currApp.webSocket = new WebSocket("ws://localhost:9999/job-progress");
                currApp.webSocket.onopen = function (event) {
                    currApp.showLink = true;
                };
                currApp.webSocket.onclose = function(event) {
                    window.sessionStorage.removeItem('showProcess');
                }
                currApp.webSocket.onmessage = function (event) {
                    currApp.currentJobStatus = JSON.parse(event.data);
                    currApp.percentProgress = currApp.currentJobStatus.percent.percent.toFixed(2) + '%';
                    currApp.lastJobLink = currApp.currentJobStatus.name;
                    if (currApp.percentProgress === '100%') {
                        window.sessionStorage.removeItem('showProcess');
                    }
                };
            }
        }
    });
}
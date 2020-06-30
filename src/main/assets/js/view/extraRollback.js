import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { baseMethods } from "../baseController";

export function initExtraRollback() {
    return new Vue({
        el: '#extraRollback',
        mixins: [baseMethods],
        data: {
            devVersion: '',
            releaseVersion: '',
            prevVersion: '',
            selectedModules: [],
            startRollback: true,
            path: 'release'
        },
        mounted: function () {
            this.checkCache(null, null, null);
        },
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            devVersion: function (val) {
                this.disableStartButton();
            },
            releaseVersion: function (val) {
                this.disableStartButton();
            },
            prevVersion: function (val) {
                this.disableStartButton();
            }
        },
        methods: {
            disableStartButton: function () {
                this.startRollback = !(this.selectedModules.length !== 0
                    && this.devVersion
                    && this.releaseVersion
                    && this.prevVersion
                );
            },
            sendForm: function () {
                $("#rollbackForm").submit();
                $("#confirm-modal").modal('hide');
            }
        }
    });
}
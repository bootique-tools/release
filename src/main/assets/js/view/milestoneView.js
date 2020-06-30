import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { defaultBaseMethods } from "../baseController";

export function initMilestoneView() {
    return new Vue({
        el: '#milestonesVue',
        mixins: [defaultBaseMethods],
        data: {
            milestoneTitle: '',
            milestoneNewTitle: '',
            milestones: null,
            currentAction: '',
            showModalButton: true,
            disableSelection: true,
            path: 'milestone'
        },
        beforeMount() {
            let currApp = this;
            if (sessionStorage.showProcess === 'initMilestoneView') {
                currApp.checkStatus();
            }
        },
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            milestoneTitle: function (val) {
                this.disableActionButton();
            },
            milestoneNewTitle: function (val) {
                this.disableActionButton();
            },
        },
        methods: {
            disableActionButton: function () {
                if (this.currentAction === 'Create') {
                    this.showModalButton = !this.milestoneNewTitle;
                } else if (this.currentAction === 'Close') {
                    this.showModalButton = !this.milestoneTitle;
                } else {
                    this.showModalButton = !(this.milestoneNewTitle && this.milestoneTitle);
                }
            },
            getMilestones: function (actionType) {
                let currApp = this;
                this.disableSelection = true;
                this.currentAction = actionType;
                this.milestoneTitle = '';
                this.milestoneNewTitle = '';
                this.controlUI(actionType);
                axios.get(`/ui/milestone/getMilestones?selectedModules=${JSON.stringify(currApp.selectedModules)}`)
                    .then(function (response) {
                        currApp.milestones = response.data;
                        currApp.disableSelection = false;
                    })
                    .catch(function () {
                        console.log("Error in getting milestones.");
                    })
                $("#milestone-modal").modal('show');
            },
            controlUI: function (actionType) {
                if (actionType === 'Create') {
                    $(".milestone-new-title").css('display', 'block');
                    $(".milestone-combo-box").css('display', 'none');
                } else if (actionType === 'Close') {
                    $(".milestone-new-title").css('display', 'none');
                    $(".milestone-combo-box").css('display', 'block');
                } else {
                    $(".milestone-new-title").css('display', 'block');
                    $(".milestone-combo-box").css('display', 'block');
                }
            },
            start: function (val) {
                let currApp = this;
                sessionStorage.showProcess = 'initMilestoneView';
                axios.get(`/ui/milestone/${String(val).toLowerCase()}?milestoneTitle=${this.milestoneTitle}&selectedModules=${JSON.stringify(currApp.selectedModules)}&milestoneNewTitle=${this.milestoneNewTitle}`)
                .then(function (response) {
                    currApp.checkStatus();
                    $("#milestone-modal").modal('hide');
                })
                .catch(function () {
                 console.log("Error in creating milestones.");
                 window.sessionStorage.removeItem('showProcess');
             })
            },
        }
    });
}
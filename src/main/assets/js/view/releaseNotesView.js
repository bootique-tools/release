import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

export function initReleaseNotesView() {
    return new Vue({
        el: '#releaseNotesVue',
        delimiters: ['[[', ']]'],
        data: {
            milestoneTitle: '',
            releaseNotes: '',
            disableButton: true
        },
        watch: {
            milestoneTitle: function () {
                this.disableGenerateButton();
            },
        },
        methods: {
            disableGenerateButton: function () {
                this.disableButton = !this.milestoneTitle;
            },
            generate: function (todo) {
                let currApp = this;
                $('#bar').fadeIn();
                axios.get(`/ui/release-notes/generate?milestoneTitle=${currApp.milestoneTitle}&todo=${todo}`)
                    .then(function (response) {
                        currApp.releaseNotes = response.data;
                        $('#bar').fadeOut();
                    })
                    .catch(function () {
                        console.log("Error in generating release notes");
                    })
            }
        },
    });
}
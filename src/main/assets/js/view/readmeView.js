import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

export function initReadmeView() {
    return new Vue({
        el: '#readmeVue',
        delimiters: ['[[', ']]'],
        data: {
            milestoneTitle: '',
            readme: '',
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
            generate: function () {
                let currApp = this;
                $('#bar').fadeIn();
                axios.get(`/ui/readme/generate?milestoneTitle=${currApp.milestoneTitle}`)
                    .then(function (response) {
                        currApp.readme = response.data;
                        $('#bar').fadeOut();
                    })
                    .catch(function () {
                        console.log("Error in generating readme");
                    })
            }
        },
    });
}
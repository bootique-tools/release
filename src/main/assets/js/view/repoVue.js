import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

import { baseMethods } from "../baseController";

export function initRepoVue() {
    return new Vue({
        el: '#repoVue',
        mixins: [baseMethods],
        data: {
            path: 'repo'
        },
        mounted: function () {
            this.checkCache(null, null, null);
        },
        methods: {
            repoView: function (repo, type) {
                $.get(`/ui/git/open?repo=${repo}&type=${type}`, () => console.log('Show repo'));
            },
            repoClone: function (repo) {
                const btn = document.getElementById(repo);
                btn.disabled = true;
                $.post(`/ui/git/clone?repo=${repo}`, () => location.reload());
            },
            repoUpdate: function (repo) {
                const btn = document.getElementById(repo);
                btn.disabled = true;
                $.post(`/ui/git/update?repo=${repo}`, () => btn.disabled = false);
            }
        }
    });
}
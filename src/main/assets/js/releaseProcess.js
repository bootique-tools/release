import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

export function initReleaseProcess() {

    return new Vue({
        el: '#release',
        delimiters: ['[[', ']]'],
        data: {
            repositoryList: [],
            stages: [],
            logs: ``,

            releaseRunning: false,
        },
        computed: {
            isReleaseRunning: function () {
                return this.releaseRunning;
            },
        },
        mounted: function () {
            let currApp = this;
            this.$nextTick(function () {
                currApp.connectWebsocket();
                currApp.getStages();
                currApp.getRepositories();
            })
        },
        methods: {
            connectWebsocket: function () {
                this.wsUri = `ws://` + document.location.host + document.location.pathname + `socket`;
                this.webSocket = new WebSocket(this.wsUri);

                let currApp = this;
                this.webSocket.onmessage = function (message) {
                    currApp.getRepositories()
                }
                this.webSocket.onopen = function () {
                    axios.post(`/ui/release/start-execute`)
                }
            },
            getStages: function () {
                let currApp = this
                axios.get(`/ui/release/stage`).then(function (response) {
                    currApp.stages = response.data
                })
            },
            getRepositories: function () {
                let currApp = this
                axios.get(`/ui/release/repository`)
                    .then(function (response) {
                        currApp.repositoryList = response.data
                        currApp.setUIAccess();
                    })
            },
            skipStage: function (name, stage) {

                const params = new URLSearchParams();
                params.append('repository', name);
                params.append('stage', stage);

                axios.post(`/ui/release/skip-stage`, params, {'Content-Type': 'application/x-www-form-urlencoded'});
            },
            restartStage: function (name, stage) {

                const params = new URLSearchParams();
                params.append('repository', name);
                params.append('stage', stage);

                axios.post(`/ui/release/restart-stage`, params, {'Content-Type': 'application/x-www-form-urlencoded'});
            },
            skipRollback: function (name, stage) {

                const params = new URLSearchParams();
                params.append('repository', name);
                params.append('stage', stage);

                axios.post(`/ui/release/skip-rollback`, params, {'Content-Type': 'application/x-www-form-urlencoded'});
            },
            rollbackRepository: function (name, stage) {
                const params = new URLSearchParams();
                params.append('repository', name);
                params.append('stage', stage);

                axios.post(`/ui/release/rollback-repository`, params, {'Content-Type': 'application/x-www-form-urlencoded'});
            },
            rollbackRelease: function () {
                axios.post(`/ui/release/rollback-release`);
                $('#rollbackRelease').modal('hide');
            },
            showStageLogs: function (name, stage) {
                let currApp = this;
                axios.get(`/ui/release/releaseLog?repository=${name}&stage=${stage}`).then(function (response) {
                    currApp.logs = response.data;
                })
            },
            showRollbackLogs: function (name, stage) {
                let currApp = this;
                axios.get(`/ui/release/rollbackLog?repository=${name}&stage=${stage}`).then(function (response) {
                    currApp.logs = response.data;
                })
            },
            setUIAccess: function () {
                let currApp = this

                currApp.releaseRunning = false
                currApp.repositoryList.forEach(repository => {
                    currApp.releaseRunning |= Object.values(repository[`stageStatusMap`]).indexOf(`In_Progress`) !== -1;
                });
                currApp.releaseRunning = Boolean(currApp.releaseRunning)
            },

            rollbackIsEnable: function (repository, stage) {

                let performStatus = repository[`stageStatusMap`][`RELEASE_PERFORM`];
                let rollbackIsEnable = true;
                if (this.repositoryList.filter(repository => repository[`stageStatusMap`]["RELEASE_SYNC"] !== `Not_Start`)
                    .length !== 0) {
                    rollbackIsEnable &= false;
                }
                rollbackIsEnable &= (stage === `RELEASE_PREPARE` && (performStatus === `Not_Start` ^ performStatus === `Rollback`)) ^ stage === `RELEASE_PERFORM`;
                return Boolean(rollbackIsEnable);
            },
            isRollbackCanStart: function (repository, stage) {
                let rIndex = Object.values(repository[`stageStatusMap`]).indexOf(`Rollback`);
                let fIndex = Object.values(repository[`stageStatusMap`]).indexOf(`Fail_Rollback`);
                let index = rIndex === -1 && fIndex === -1 ? -1 : rIndex === -1 ? fIndex : fIndex === -1? rIndex : Math.min(rIndex, fIndex)
                return index !== -1 && Object.keys(repository[`stageStatusMap`])[index] === stage;
            },
            isFullRollbackFail: function (repository) {
                return repository[`stageStatusMap`][`RELEASE_PULL`] === `Fail_Rollback`;
            },
            syncStageIsEnable: function () {
                return this.repositoryList.every(repository => {
                    let performStatus = repository[`stageStatusMap`][`RELEASE_PERFORM`];
                    return performStatus === `Skip` ^ performStatus === `Success`
                }) && !this.syncStageStart()

            },
            syncStageStart: function () {
                return this.repositoryList
                    .some(repository => repository[`stageStatusMap`][`RELEASE_SYNC`] !== `Not_Start`)
            },
            releaseIsFinished: function () {
                return this.repositoryList.every(repository => {
                    let syncStatus = repository[`stageStatusMap`][`RELEASE_SYNC`]
                    return syncStatus === `Skip` ^ syncStatus === `Success`
                })
            },
            canStartFullRollback: function () {
                return this.repositoryList.every(repository => {
                    return (repository[`stageStatusMap`][`RELEASE_PREPARE`] !== `Not_Start` ||
                        repository[`stageStatusMap`][`RELEASE_PERFORM`] !== `Not_Start`) &&
                        repository[`stageStatusMap`][`RELEASE_SYNC`] === `Not_Start`;
                })
            },
        }

    })
}
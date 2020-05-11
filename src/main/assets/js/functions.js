import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

function incrementLast(v) {
    return v.replace(/[0-9]+(?!.*[0-9])/, function (match) {
        return parseInt(match, 10) + 1;
    });
}

const baseMethods = {
    delimiters: ['[[', ']]'],
    data: {
        allItems: null,
        errorMessage: '',
    },
    methods: {
        checkCache: function (filter, sort) {
            let currApp = this;
            let intervalCheck = setInterval(function () {
                axios.get(`/ui/checkCache`)
                    .then(function (response) {
                        if (response.data) {
                            $('#bar').fadeOut();
                            clearInterval(intervalCheck);
                            currApp.getAllProjects(filter, sort);
                        }
                    })
                    .catch(function () {
                        console.log("Error in loading projects.");
                    })
            }, 100);
        },
        sortAndFilter: function(filter, sort) {
            this.getAllProjects(filter, sort);
        },
        getAllProjects: function (filter, sort) {
            let currApp = this;
            axios.get(`/ui/${currApp.path}/show-all?filter=${filter}&sort=${sort}`)
            .then(function (response) {
                currApp.allItems = response.data;
                currApp.additionalMethod(currApp);
                if(currApp.allItems.length === 0) {
                    currApp.errorMessage = "Please clone all repositories to your local repositories!";
                }
            })
            .catch(function () {
             console.log("Error in loading projects.");
            })
        },
        additionalMethod: function (data) {
        },
    }
}

const defaultBaseMethods = {
    delimiters: ['[[', ']]'],
    mixins: [baseMethods],
    data: {
        selectedModules: [],
        statusMap: null,
        errorMap: null,
        showButton: true,
        progress: 0,
    },
    mounted: function () {
        this.checkCache(null, null);
        this.statusMap = new Map();
        this.errorMap = new Map();
    },
    watch: {
        checked: function (val) {
            let currApp = this;
            currApp.selectedModules = [];
            if (val === true) {
                for (var i = 0; i < currApp.allItems.length; i++) {
                    currApp.selectedModules.push(currApp.allItems[i].repository.name);
                }
            }
        },
    },
    methods: {
        disableStartButton: function () {
            this.showButton = !(this.selectedModules.length !== 0 && sessionStorage.showProcess == null);
        },
        checkStatus: function () {
            let currApp = this;
            let intervalCheck = setInterval(function () {
                axios.get(`/ui/release/process/status`)
                    .then(function (response) {
                        currApp.progress = response.data.percent.percent;
                        if(currApp.allItems != null){
                for(let i = 0 ; i < currApp.allItems.length; i++) {
                    for(let j = 0; j < response.data.results.length; j++) {
                        if(currApp.allItems[i].repository.name === response.data.results[j].data.repository.name) {
                            currApp.allItems[i] = response.data.results[j].data;
                            currApp.statusMap.set(currApp.allItems[i], response.data.results[j].status);
                            currApp.errorMap.set(currApp.allItems[i], response.data.results[j].result);
                        }
                    }
                }
              }
              if(response.data.percent.percent === 100) {
                clearInterval(intervalCheck);
                window.sessionStorage.removeItem('showProcess');
              }
            })
            .catch(function (){
              console.log("Error in checking status.");
              window.sessionStorage.removeItem('showProcess');
            })
          }, 100);
        },
    }
}

const releaseBaseMethods = {
    delimiters: ['[[', ']]'],
    mixins: [defaultBaseMethods],
    data: {
        currentVersion: '',
        releaseVersion: '',
        nextDevVersion: '',
        versions: null,
        startRelease: true,
        path: 'release'
    },
    watch: {
        releaseVersion: function (val) {
            let parsing = val.split("-");
            this.nextDevVersion = incrementLast(parsing[0]) + '-SNAPSHOT';
        }
    },
    methods: {
        additionalMethod: function (currApp) {
            let versionSet = new Set();
            for (let i = 0; i < currApp.allItems.length; i++) {
                versionSet.add(currApp.allItems[i].rootModule.version);
            }
            currApp.versions = Array.from(versionSet);
        },
        versionSelector: function () {
            let currApp = this;
            const vSelector = document.getElementById('vSelector');
            this.currentVersion = vSelector.options[vSelector.selectedIndex].text;
            this.releaseVersion = this.currentVersion.split("-")[0];
            axios.get(`/ui/release/show-projects?version=${this.currentVersion}`)
                .then(function (response) {
                    currApp.selectedModules = [];
                    currApp.allItems = response.data;
                    if (currApp.allItems.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to your local repositories!";
                    } else {
                        for (let i = 0; i < currApp.allItems.length; i++) {
                            if (currApp.allItems[i].disable === false) {
                                currApp.selectedModules.push(currApp.allItems[i].repository.name);
                            }
                        }
                        currApp.startRelease = currApp.selectedModules.length === 0;
                    }
                })
                .catch(function () {
                    console.log("Show projects for version error. Can't show projects for this version.");
                })
        },
        moduleSelect: function (project) {
            let currApp = this;
            let state = false;
            for (let i = 0; i < currApp.selectedModules.length; i++) {
                if (project === currApp.selectedModules[i]) {
                    state = true;
                }
            }
            axios.get(`/ui/release/select-projects?version=${this.currentVersion}&projects=${JSON.stringify(currApp.selectedModules)}&selectedProject=${project}&state=${state}`)
                .then(function (response) {
                    currApp.selectedModules = [];
                    const currList = response.data;
                    for (let i = 0; i < currList.length; i++) {
                        currApp.selectedModules.push(currList[i].repository.name);
                    }

                    currApp.startRelease = currApp.selectedModules.length === 0
                        && sessionStorage.showProcess != null;
                })
                .catch(function () {
                    console.log("Selection error. Can't display selected projects.");
                })
        },
    }
}

export function initRelease() {
    return new Vue({
        el: '#releaseVue',
        mixins: [releaseBaseMethods],
        data: {
            mode: false
        },
        methods: {
            sendForm: function () {
                $("#release-form").submit();
                $("#confirm-modal").modal('hide');
            }
        }
    });
}


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
            this.checkCache(null, null);
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

const showProcessGlobal = Vue.observable({showProcessGlobal: false})

Object.defineProperty(Vue.prototype, '$showProcessGlobal', {
    configurable: true,
    get() {
        return showProcessGlobal.showProcessGlobal
    },
    set(value) {
        showProcessGlobal.showProcessGlobal = value
    }
})


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
        beforeMount(){
           if (sessionStorage.showProcess === 'initMavenVue') {
               this.verify();
           }
        },
        mounted: function(){
           this.checkCache(null, null);
        },
        methods: {
        additionalMethod: function(app) {
        if (sessionStorage.showProcess != null) {
            this.verifyButton = true;
        } else {
            app.verifyButton = sessionStorage.showProcess != null;
            }
            },
        verify: function() {
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
              if(response.data.percent.percent === 100) {
                clearInterval(intervalCheck);
              }
            })
            .catch(function (){
              console.log("Error in checking status.");
              window.sessionStorage.removeItem('showProcess');
            })
          }, 1200);
        },
        }
    });
}

export function initRepoVue() {
    return new Vue({
        el: '#repoVue',
        mixins: [baseMethods],
        data: {
            path: 'repo'
        },
        mounted: function () {
            this.checkCache(null, null);
        },
        methods: {
            repoView: function (repo, type) {
                $.get(`/ui/git/open?repo=${repo}&type=${type}`, () => console.log('Show repo'));
            },
            repoClone: function (repo) {
                const btn = document.getElementById(repo);
                btn.disabled = true;
                $.get(`/ui/git/clone?repo=${repo}`, () => location.reload());
            },
            repoUpdate: function (repo) {
                const btn = document.getElementById(repo);
                btn.disabled = true;
                $.get(`/ui/git/update?repo=${repo}`, () => btn.disabled = false);
            }
        }
    });
}

export function initPrVue(baseFilter, baseSort) {
    return new Vue({
        el: '#prVue',
        mixins: [baseMethods],
        data: {
            path: 'pr'
        },
        mounted: function () {
            this.checkCache(baseFilter, baseSort);
        },
        methods: {}
    });
}

export function initIssueVue(baseFilters, baseSort) {
    return new Vue({
        el: '#issueVue',
        mixins: [baseMethods],
        data: {
            path: 'issue'
        },
        mounted: function () {
            this.checkCache(baseFilters, baseSort);
        },
        methods: {}
    });
}

export function initMilestoneView() {
    return new Vue({
        el: '#milestonesVue',
        mixins: [defaultBaseMethods],
        data: {
            milestoneTitle: '',
            milestoneNewTitle: '',
            checked: false,
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

export function initBranchView() {
    return new Vue({
        el: '#branchVue',
        mixins: [defaultBaseMethods],
        data: {
            checked: false,
            branchTitle: '',
            path: 'branches'
        },
        beforeMount() {
            let currApp = this;
            if (sessionStorage.showProcess === 'initBranchView') {
                currApp.checkStatus();
            }
        },
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            branchTitle: function (val) {
                this.disableStartButton();
            },
        },
        methods: {
            startTask: function (task) {
                let currApp = this;
                sessionStorage.showProcess = 'initBranchView';
                currApp.progress = 0;
                axios.get(`/ui/branches/${String(task)}?branchTitle=${this.branchTitle}&selectedModules=${JSON.stringify(currApp.selectedModules)}`)
                .then(function (response) {
                    currApp.checkStatus();
                })
                .catch(function () {
                 console.log("Error in " + task);
                 window.sessionStorage.removeItem('showProcess');
             })
            },
        }
    });
}

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

import Vue from 'vue/dist/vue'
import axios from 'axios/dist/axios'

function incrementLast(v) {
    return v.replace(/[0-9]+(?!.*[0-9])/, function (match) {
        return parseInt(match, 10) + 1;
    });
}

export const baseMethods = {
    delimiters: ['[[', ']]'],
    data: {
        allItems: null,
        errorMessage: '',
        isASC: true,
        sortDir: null,
    },
    methods: {
        checkCache: function (sort, filter, baseFilterField) {
            let currApp = this;
            let intervalCheck = setInterval(function () {
                axios.get(`/ui/checkCache`)
                    .then(function (response) {
                        if (response.data) {
                            clearInterval(intervalCheck);
                            if (sort != null && sort !== "" || filter != null && filter !== "") {
                                currApp.sortAndFilter(sort, filter, baseFilterField);
                            } else {
                                let uri = "";
                                currApp.getAllProjects(uri);
                            }
                        }
                    })
                    .catch(function () {
                        console.log("Error in loading projects.");
                    })
            }, 100);
        },
        sortAndFilter: function (sort, filter, baseFilterField) {
            let uri = "";
            if (sort !== null && sort !== "") {
                if (this.isASC) {
                    this.sortDir = "ASC";
                } else {
                    this.sortDir = "DESC";
                }
                uri += "sort=" + sort + "&dir=" + this.sortDir;
                this.isASC = !this.isASC;
            }
            if (filter !== null) {
                if (uri !== "") {
                    uri = "&" + uri;
                }
                uri += "cayenneExp=[\"" + filter + " like $b\",\"" + baseFilterField + "\"]";
            }
            this.getAllProjects(uri);
        },
        getAllProjects: function (uri) {
            let currApp = this;
            if (currApp.path === "repo") {
                uri = "include=[\"milestones\",\"issues\",\"pullRequests\",\"parent\"]" + "&" + uri;
            } else if (currApp.path === "issue") {
                uri = "include=[\"milestone\",\"labels\",\"repository\",\"author\",\"repository.parent\"]" + "&" + uri;
            } else if (currApp.path === "milestone") {
                uri = "include=[\"issues\",\"milestones\",\"milestones.openIssues\"]" + "&" + uri;
            } else if (currApp.path === "pr") {
                uri = "include=[\"labels\",\"repository\",\"author\",\"repository.parent\"]" + "&" + uri;
            }

            if (uri !== "") {
                uri = "?" + uri;
            }

            axios.get(`/ui/${currApp.path}/show-all${uri}`)
                .then(function (response) {
                    currApp.allItems = response.data;
                    if (currApp.allItems.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to your local repositories!";
                    } else {
                        if (currApp.additionalMethod !== undefined) {
                            currApp.additionalMethod(currApp);
                        }
                    }
                    $('#bar').fadeOut();
                })
                .catch(function () {
                    currApp.errorMessage = "Error in loading projects.";
                    console.log("Error in loading projects.");
                })
        },
    }
}

export const defaultBaseMethods = {
    delimiters: ['[[', ']]'],
    mixins: [baseMethods],
    data: {
        selectedModules: [],
        statusMap: null,
        errorMap: null,
        connection: null,
        showButton: true,
        progress: 0,
    },
    mounted: function () {
        this.checkCache(null, null, null);
        this.statusMap = new Map();
        this.errorMap = new Map();
    },
    computed: {
        selectAll: {
            get: function () {
                if (this.errorMessage === "") {
                    return this.allItems
                        ? this.selectedModules.length === this.allItems.data.length
                        : false;
                }
            },
            set: function (value) {
                let selectedModules = [];
                if (value) {
                    this.allItems.data.forEach(function (module) {
                        selectedModules.push(module.repository.name);
                    });
                }
                this.selectedModules = selectedModules;
            }
        }
    },
    methods: {

        connectJobStatusWebsocket: function () {
            this.wsUri = `ws://` + document.location.host + '/job/status';
            this.connection = new WebSocket(this.wsUri);
            this.connection.onopen = function (event) {
                console.log("Successfully connected to the websocket server")
            }

            let currApp = this;
            this.connection.addEventListener('message', function (event) {
                currApp.checkJobStatus()
            })
        },

        disableStartButton: function () {
            this.showButton = !(this.selectedModules.length !== 0 && sessionStorage.showProcess == null);
        },

        checkJobStatus: function () {
            let currApp = this;
            axios.get(`/ui/job/status`)
                .then(function (response) {
                    console.log('checkJobStatus ' + response.data)
                    currApp.progress = response.data;
                    if (currApp.progress === 100) {
                        currApp.connection.close();
                        console.log("Job done, websocket connection closed");
                        window.sessionStorage.removeItem('showProcess');
                    }
                })
                .catch(function () {
                    console.log("Error in checking status.");
                    window.sessionStorage.removeItem('showProcess');
                })
            currApp.checkCache();
        },
    }
}

export const releaseBaseMethods = {
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
            for (let i = 0; i < currApp.allItems.data.length; i++) {
                versionSet.add(currApp.allItems.data[i].version);
                currApp.allItems.data[i].disable = true;
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
                    if (currApp.allItems.data.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to your local repositories!";
                    } else {
                        for (let i = 0; i < currApp.allItems.data.length; i++) {
                            if (currApp.allItems.data[i].disable === false) {
                                currApp.selectedModules.push(currApp.allItems.data[i].repository.name);
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
            axios.get(`/ui/release/project?version=${this.currentVersion}&projects=${JSON.stringify(currApp.selectedModules)}&selectedProject=${project}&state=${state}`)
                .then(function (response) {
                    currApp.selectedModules = [];
                    const currList = response.data;
                    for (let i = 0; i < currList.data.length; i++) {
                        currApp.selectedModules.push(currList.data[i].repository.name);
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

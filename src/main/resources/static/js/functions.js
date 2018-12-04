function incrementLast(v) {
    return v.replace(/[0-9]+(?!.*[0-9])/, function(match) {
        return parseInt(match, 10)+1;
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
        let intervalCheck = setInterval(function() {
            const param = new Date().getTime();
            axios.get(`/ui/checkCache`)
            .then(function (response) {
                if(response.data) {
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
        let currApp = this;
        currApp.getAllProjects(filter, sort);
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
    additionalMethod: function(data) {},
}
}

export function initRelease() {
    return new Vue({
        el: '#releaseVue',
        mixins: [baseMethods],
        data: {
            currentVersion: '',
            releaseVersion: '',
            nextDevVersion: '',
            versions: null,
            selectedModules: [],
            startRelease: true,
            mode: false,
            path: 'release'
        },
        mounted: function(){
            this.checkCache(null, null);
        },
        watch: {
            releaseVersion: function (val) {
                let parsing = val.split("-");
                this.nextDevVersion = incrementLast(parsing[0]) + '-SNAPSHOT';
            }
        },
        methods: {
            additionalMethod: function(currApp) {
                let versionSet = new Set();
                for(let i = 0; i < currApp.allItems.length; i++) {
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
                    if(currApp.allItems.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to your local repositories!";
                    } else {
                        for(let i = 0; i < currApp.allItems.length; i++) {
                            if(currApp.allItems[i].disable === false){
                                currApp.selectedModules.push(currApp.allItems[i].repository.name);
                            }
                        }
                        if(currApp.selectedModules.length === 0) {
                            currApp.startRelease = true;
                        } else {
                            currApp.startRelease = false;
                        }
                    }
                })
                .catch(function () {
                   console.log("Show projects for version error. Can't show projects for this version.");
               })
            },
            moduleSelect: function (project) {
                let currApp = this;
                let state = false;
                for(let i = 0; i < currApp.selectedModules.length; i++){
                    if(project === currApp.selectedModules[i]){
                        state = true;
                    }
                }
                axios.get(`/ui/release/select-projects?version=${this.currentVersion}&projects=${JSON.stringify(currApp.selectedModules)}&selectedProject=${project}&state=${state}`)
                .then(function (response) {
                    currApp.selectedModules = [];
                    const currList = response.data;
                    for(let i = 0; i < currList.length; i++){
                        currApp.selectedModules.push(currList[i].repository.name);
                    }

                    if(currApp.selectedModules.length === 0) {
                        currApp.startRelease = true;
                    } else {
                        currApp.startRelease = false;
                    }
                })
                .catch(function () {
                   console.log("Selection error. Can't display selected projects.");
               })
            },
            sendForm: function() {
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
        mounted: function(){
            this.checkCache(null, null);
        },
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            devVersion: function(val) {
                this.disableStartButton();
            },
            releaseVersion: function(val) {
                this.disableStartButton();
            },
            prevVersion: function(val) {
                this.disableStartButton();
            }
        },
        methods: {
            disableStartButton: function() {
                let currApp = this;
                if(currApp.selectedModules.length !== 0 && currApp.devVersion && currApp.releaseVersion && currApp.prevVersion) {
                    currApp.startRollback = false;
                } else {
                    currApp.startRollback = true;
                }
            },
            sendForm: function() {
                $("#rollbackForm").submit();
                $("#confirm-modal").modal('hide');
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
        mounted: function(){
         this.checkCache(null, null);
     },
     methods: {
        additionalMethod: function(app) {
            app.verifyButton = false;
        },
        verify: function() {
            let currApp = this;
            currApp.showProcess = true;
            axios.get(`/ui/maven/verify`)
            .then(function (response) {
                currApp.checkStatus();
            })
            .catch(function () {
                console.log("Error start mvn verify.");
            })
        },
        checkStatus: function() {
          let currApp = this;
          let intervalCheck = setInterval(function() {
            const param = new Date().getTime();
            axios.get(`/ui/release/process/status?time=${param}`)
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
        mounted: function(){
         this.checkCache(null, null);
     },
     methods: {       
        repoView: function(repo, type) {
            $.get(`/ui/git/open?repo=${repo}&type=${type}`, () => console.log('Show repo'));
        },
        repoClone: function(repo) {
            const btn = document.getElementById(repo);
            btn.disabled = true;
            $.get(`/ui/git/clone?repo=${repo}`, () => location.reload());
        },
        repoUpdate: function(repo) {
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
        mounted: function(){
         this.checkCache(baseFilter, baseSort);
     },
     methods: {
     }
 });
}

export function initIssueVue(baseFilters, baseSort) {
    return new Vue({
        el: '#issueVue',
        mixins: [baseMethods],
        data: {
            path: 'issue'
        },
        mounted: function(){
         this.checkCache(baseFilters, baseSort);
     },
     methods: {
     }
 });
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
mounted: function(){
 this.checkCache(null, null);
 this.statusMap = new Map();
 this.errorMap = new Map();
},
watch: {
    checked: function(val) {
        let currApp = this;
        currApp.selectedModules = [];
        if(val === true) {
            for(var i = 0; i < currApp.allItems.length; i++) {
                currApp.selectedModules.push(currApp.allItems[i].repository.name);
            }
        }
    },
},
methods: {
    disableStartButton: function() {
        let currApp = this;
        if(currApp.selectedModules.length !== 0) {
            currApp.showButton = false;
        } else {
            currApp.showButton = true;
        }
    },
    checkStatus: function() {
      let currApp = this;
      let intervalCheck = setInterval(function() {
        const param = new Date().getTime();
        axios.get(`/ui/release/process/status?time=${param}`)
        .then(function (response) {
          currApp.progress = response.data.percent.percent;
          for(let i = 0 ; i < currApp.allItems.length; i++) {
            for(let j = 0; j < response.data.results.length; j++) {
                if(currApp.allItems[i].repository.name === response.data.results[j].data.repository.name) {
                    currApp.allItems[i] = response.data.results[j].data;
                    currApp.statusMap.set(currApp.allItems[i], response.data.results[j].status);
                    currApp.errorMap.set(currApp.allItems[i], response.data.results[j].result);
                }
            }
        }

        if(response.data.percent.percent === 100) {
            clearInterval(intervalCheck);
        }
    })
        .catch(function (){
          console.log("Error in checking status.");
      })
    }, 100);
  },
}
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
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            milestoneTitle: function(val) {
                this.disableActionButton();
            },
            milestoneNewTitle: function(val) {
                this.disableActionButton();
            },
        },
        methods: {
            disableActionButton: function() {
                let currApp = this;
                if(currApp.currentAction === 'Create') {
                    if(currApp.milestoneNewTitle) {
                        currApp.showModalButton = false;
                    } else {
                        currApp.showModalButton = true;
                    }
                } else if(currApp.currentAction === 'Close') {
                    if(currApp.milestoneTitle) {
                        currApp.showModalButton = false;
                    } else {
                        currApp.showModalButton = true;
                    }
                } else {
                    if(currApp.milestoneNewTitle && currApp.milestoneTitle) {
                        currApp.showModalButton = false;
                    } else {
                        currApp.showModalButton = true;
                    }
                }
            },
            getMilestones: function(val) {
                let currApp = this;
                currApp.disableSelection = true;
                currApp.currentAction = val;
                currApp.milestoneTitle = '';
                currApp.milestoneNewTitle = '';
                currApp.controlUI(val);
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
            controlUI: function(val) {
                if(val === 'Create') {
                    $(".milestone-new-title").css('display', 'block');
                    $(".milestone-combo-box").css('display', 'none');
                } else if(val === 'Close') {
                    $(".milestone-new-title").css('display', 'none');
                    $(".milestone-combo-box").css('display', 'block');
                } else {
                    $(".milestone-new-title").css('display', 'block');
                    $(".milestone-combo-box").css('display', 'block');
                }
            },
            start: function(val) {
                let currApp = this;
                axios.get(`/ui/milestone/${String(val).toLowerCase()}?milestoneTitle=${this.milestoneTitle}&selectedModules=${JSON.stringify(currApp.selectedModules)}&milestoneNewTitle=${this.milestoneNewTitle}`)
                .then(function (response) {
                    currApp.checkStatus();
                    $("#milestone-modal").modal('hide');
                })
                .catch(function () {
                   console.log("Error in creating milestones.");
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
        watch: {
            selectedModules: function (val) {
                this.disableStartButton();
            },
            branchTitle: function (val) {
                this.disableStartButton();
            },
        },
        methods: {
            startTask: function(task) {
                let currApp = this;
                currApp.progress = 0;
                axios.get(`/ui/branches/${String(task)}?branchTitle=${this.branchTitle}&selectedModules=${JSON.stringify(currApp.selectedModules)}`)
                .then(function (response) {
                    currApp.checkStatus();
                })
                .catch(function () {
                   console.log("Error in " + task);
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
            milestoneTitle: function (val) {
                this.disableGenerateButton();
            },
        },
        methods: {
            disableGenerateButton: function(val) {
                let currApp = this;
                if(currApp.milestoneTitle) {
                    currApp.disableButton = false;
                } else {
                    currApp.disableButton = true;
                }
            },
            generate: function() {
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

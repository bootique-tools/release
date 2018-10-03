function incrementLast(v) {
    return v.replace(/[0-9]+(?!.*[0-9])/, function(match) {
        return parseInt(match, 10)+1;
    });
}
export function initRelease() {
    return new Vue({
        el: '#releaseVue',
        delimiters: ['[[', ']]'],
        data: {
            currentVersion: '',
            releaseVersion: '',
            nextDevVersion: '',
            allModules: null,
            versions: null,
            selectedModules: [],
            startRelease: true,
            errorMessage: '',
            mode: false
        },
        mounted: function(){
            this.getAllProjects();
        },
        watch: {
            releaseVersion: function (val) {
                let parsing = val.split("-");
                this.nextDevVersion = incrementLast(parsing[0]) + '-SNAPSHOT';
            }
        },
        methods: {
            getAllProjects: function () {
                let currApp = this;
                axios.get(`/ui/release/show-all`)
                .then(function (response) {
                    $('#bar').fadeOut();
                    currApp.allModules = response.data;
                    let versionSet = new Set();
                    for(let i = 0; i < currApp.allModules.length; i++) {
                        versionSet.add(currApp.allModules[i].rootModule.version);
                    }
                    currApp.versions = Array.from(versionSet);
                    if(currApp.allModules.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to you local repositories!";
                    }
                })
                .catch(function () {
                   console.log("Error in loading projects.");
               })
            },
            versionSelector: function () {
                let currApp = this;
                const vSelector = document.getElementById('vSelector');
                this.currentVersion = vSelector.options[vSelector.selectedIndex].text;
                this.releaseVersion = this.currentVersion.split("-")[0];
                axios.get(`/ui/release/show-projects?version=${this.currentVersion}`)
                .then(function (response) {
                    currApp.selectedModules = [];
                    currApp.allModules = response.data;
                    if(currApp.allModules.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to you local repositories!";
                    } else {
                        for(let i = 0; i < currApp.allModules.length; i++) {
                            if(currApp.allModules[i].disable === false){
                                currApp.selectedModules.push(currApp.allModules[i].repository.name);
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
        delimiters: ['[[', ']]'],
        data: {
            devVersion: '',
            releaseVersion: '',
            prevVersion: '',
            allModules: null,
            selectedModules: [],
            startRollback: true,
            errorMessage: ''
        },
        mounted: function(){
            this.getAllProjects();
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
            getAllProjects: function () {
                let currApp = this;
                axios.get(`/ui/release/show-all`)
                .then(function (response) {
                    $('#bar').fadeOut();
                    currApp.allModules = response.data;
                    if(currApp.allModules.length === 0) {
                        currApp.errorMessage = "Please clone all repositories to you local repositories!";
                    }
                })
                .catch(function () {
                   console.log("Error in loading projects.");
               })
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
        delimiters: ['[[', ']]'],
        data: {
            allModules: null,
            showProcess: false,
            progress: 0,
            statusArr: null,
            errorMessage: '',
            verifyButton: true
        },
        mounted: function(){
         this.checkCache();
     },
     methods: {
        checkCache: function () {
            let currApp = this;
            let intervalCheck = setInterval(function() {
                const param = new Date().getTime();
                axios.get(`/ui/checkCache`)
                .then(function (response) {
                    if(response.data) {
                        $('#bar').fadeOut();
                        clearInterval(intervalCheck); 
                        currApp.getAllProjects();
                    }
                })
                .catch(function () {
                    console.log("Error in loading projects.");
                })
            }, 100);
        },
        getAllProjects: function () {
            let currApp = this;
            axios.get(`/ui/release/show-all`)
            .then(function (response) {
                currApp.allModules = response.data;
                $('#bar').fadeOut();
                currApp.verifyButton = false;
                if(currApp.allModules.length === 0) {
                    currApp.errorMessage = "Please clone all repositories to you local repositories!";
                }
            })
            .catch(function () {
               console.log("Error in loading projects.");
           })
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


export function initMilestoneView() {
    return new Vue({
        el: '#milestonesVue',
        delimiters: ['[[', ']]'],
        data: {
            milestoneTitle: '',
            milestoneNewTitle: '',
            selectedModules: [],
            allModules: [],
            errorMessage: '',
            showButton: true,
            checked: false,
            progress: 0,
            statusMap: null,
            errorMap: null,
            milestones: null,
            currentAction: '',
            showModalButton: true,
            disableSelection: true
        },
        mounted: function(){
         this.checkCache();
         this.statusMap = new Map();
         this.errorMap = new Map();
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
        checked: function(val) {
            let currApp = this;
            currApp.selectedModules = [];
            if(val === true) {
                console.log(currApp.allModules.length);
                for(var i = 0; i < currApp.allModules.length; i++) {
                    currApp.selectedModules.push(currApp.allModules[i].repository.name);
                }
            }
        }
    },
    methods: {
        checkCache: function () {
            let currApp = this;
            let intervalCheck = setInterval(function() {
                const param = new Date().getTime();
                axios.get(`/ui/checkCache`)
                .then(function (response) {
                    if(response.data) {
                        $('#bar').fadeOut();
                        clearInterval(intervalCheck); 
                        currApp.getAllProjects();
                    }
                })
                .catch(function () {
                    console.log("Error in loading projects.");
                })
            }, 100);
        },
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
        disableStartButton: function() {
            let currApp = this;
            if(currApp.selectedModules.length !== 0) {
                currApp.showButton = false;
            } else {
                currApp.showButton = true;
            }
        },
        getAllProjects: function () {
            let currApp = this;
            axios.get(`/ui/milestone/show-all`)
            .then(function (response) {
             currApp.allModules = response.data;
             $('#bar').fadeOut();
             if(currApp.allModules.length === 0) {
                currApp.errorMessage = "Please clone all repositories to you local repositories!";
            }
        })
            .catch(function () {
               console.log("Error in loading projects.");
           })
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
        checkStatus: function() {
          let currApp = this;
          let intervalCheck = setInterval(function() {
            const param = new Date().getTime();
            axios.get(`/ui/release/process/status?time=${param}`)
            .then(function (response) {
              currApp.progress = response.data.percent.percent;
              for(let i = 0 ; i < currApp.allModules.length; i++) {
                for(let j = 0; j < response.data.results.length; j++) {
                    if(currApp.allModules[i].repository.name === response.data.results[j].data.repository.name) {
                        currApp.allModules[i] = response.data.results[j].data;
                        currApp.statusMap.set(currApp.allModules[i], response.data.results[j].status);
                        currApp.errorMap.set(currApp.allModules[i], response.data.results[j].result);
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
});
}

export function initRepoVue() {
    return new Vue({
        el: '#repoVue',
        delimiters: ['[[', ']]'],
        data: {
            allRepos: null,
            errorMessage: ''
        },
        mounted: function(){
         this.checkCache();
     },
     methods: {
        checkCache: function () {
            let currApp = this;
            let intervalCheck = setInterval(function() {
                const param = new Date().getTime();
                axios.get(`/ui/checkCache`)
                .then(function (response) {
                    if(response.data) {
                        $('#bar').fadeOut();
                        clearInterval(intervalCheck); 
                        currApp.getAllProjects();
                    }
                })
                .catch(function () {
                    console.log("Error in loading projects.");
                })
            }, 100);
        },        
        sort: function(val) {
            let currApp = this;
            currApp.getAllProjects(val);
        },
        getAllProjects: function (val) {
            let currApp = this;
            axios.get(`/ui/show-all?sort=${val}`)
            .then(function (response) {
                currApp.allRepos = response.data;
                if(currApp.allRepos.length === 0) {
                    currApp.errorMessage = "Please clone all repositories to you local repositories!";
                }
            })
            .catch(function () {
               console.log("Error in loading projects.");
           })
        },
        repoView: function(repo, type) {
            const btn = $(this);
            btn.attr('disabled', true);
            $.get(`/ui/git/open?repo=${repo}&type=${type}`, () => btn.attr('disabled', false));
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
        delimiters: ['[[', ']]'],
        data: {
            allPrs: null,
            errorMessage: ''
        },
        mounted: function(){
         this.checkCache();
     },
     methods: {
        checkCache: function () {
            let currApp = this;
            let intervalCheck = setInterval(function() {
                const param = new Date().getTime();
                axios.get(`/ui/checkCache`)
                .then(function (response) {
                    if(response.data) {
                        $('#bar').fadeOut();
                        clearInterval(intervalCheck); 
                        currApp.getAllProjects(baseFilter, baseSort);
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
            axios.get(`/ui/pr/show-all?filter=${filter}&sort=${sort}`)
            .then(function (response) {
                currApp.allPrs = response.data;
            })
            .catch(function () {
               console.log("Error in loading projects.");
           })
        },
    }
});
}
export function initIssueVue(baseFilters, baseSort) {
    return new Vue({
        el: '#issueVue',
        delimiters: ['[[', ']]'],
        data: {
            allIssues: null,
            errorMessage: '',
        },
        mounted: function(){
         this.checkCache();
     },
     methods: {
        checkCache: function () {
            let currApp = this;
            let intervalCheck = setInterval(function() {
                const param = new Date().getTime();
                axios.get(`/ui/checkCache`)
                .then(function (response) {
                    if(response.data) {
                        $('#bar').fadeOut();
                        clearInterval(intervalCheck); 
                        currApp.getAllProjects(baseFilters, baseSort);
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
            axios.get(`/ui/issue/show-all?filter=${filter}&sort=${sort}`)
            .then(function (response) {
                currApp.allIssues = response.data;
            })
            .catch(function () {
               console.log("Error in loading projects.");
           })
        },
    }
});
}
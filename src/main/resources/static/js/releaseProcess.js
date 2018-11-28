  export function initReleaseProcess(id) {
    return new Vue({
      el: '#' + id,
      delimiters: ['[[', ']]'],
      data: {
        jobId: 0,
        progress: 0,
        statusArr: null,
        showButton: true,
        statusBar: null,
        stageName: '',
        mode: false
      },
      mounted: function () {
        this.$nextTick(function () {
          this.checkDescriptor();
        })
      },
      methods: {
        checkDescriptor: function() {
          let currApp = this;
          currApp.getStatusBar();
          axios.get('/ui/release/process/has-descriptor')
          .then(function (response) {
            if(response.data) {
              currApp.mode = response.data.mode;
              currApp.checkStatus();
              currApp.needToClose();
            }
          })
          .catch(function (){
            console.log("Error in checking descriptor");
          })
        },
        needToClose: function() {
          let currApp = this;
          let intervalCheck = setInterval(function() {
            const param = new Date().getTime();
            axios.get(`/ui/release/process/need-to-close?time=${param}`)
            .then(function (response) {
              if(response.data === true) {
                clearInterval(intervalCheck);
              }
            })
            .catch(function (){
              console.log("Bad response");
            })
          }, 1200);
        },
        checkStatus: function() {
          let currApp = this;
          currApp.progress = 0;
          let intervalCheck = setInterval(function() {
            const param = new Date().getTime();
            axios.get(`/ui/release/process/status?time=${param}`)
            .then(function (response) {
              currApp.progress = response.data.percent.percent;
              currApp.statusArr = response.data.results;
              currApp.stageName = response.data.name; 
              currApp.getStatusBar();
              if(currApp.mode) {
                currApp.showButton = true;
              }
              if(response.data.percent.percent === 100 && !currApp.mode) {
                clearInterval(intervalCheck); 
                currApp.showButton = false;
              }
            })
            .catch(function (){
              console.log("Error in checking status.");
            })
          }, 1200);
        },
        getStatusBar: function() {
          let currApp = this;
          const param = new Date().getTime();
          axios.get(`/ui/release/process/get-status-bar?time=${param}`)
          .then(function (response) {
            currApp.statusBar = response.data;
          })
          .catch(function () {
           console.log("Error in check statusBar.");
         })
        }
      }
    });
  }
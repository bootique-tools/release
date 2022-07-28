// styles and 3rd-party libs
import '../scss/main.scss';
import 'bootstrap/dist/js/bootstrap';
import '@fortawesome/fontawesome-free/js/all';

// export everything used directly from HTML in the global scope
import { initMilestoneView } from "./view/milestoneView";
import { initBranchView } from "./view/branchView";
import { initValidationView } from "./view/validationView";
import { initReleaseNotesView } from "./view/releaseNotesView";
import { initIssueVue } from "./view/issueVue";
import { initRepoVue } from "./view/repoVue";
import { initPrVue } from "./view/prVue";
import { initExtraRollback } from "./view/extraRollback";
import { initMavenVue } from "./view/mavenVue";
import { initRelease } from "./view/release";
import { initReleaseProcess } from "./releaseProcess";

window.initRepoVue = initRepoVue;
window.initIssueVue = initIssueVue;
window.initBranchView = initBranchView;
window.initExtraRollback = initExtraRollback;
window.initMavenVue = initMavenVue;
window.initMilestoneView = initMilestoneView;
window.initPrVue = initPrVue;
window.initReleaseNotesView = initReleaseNotesView;
window.initValidationView = initValidationView;
window.initRelease = initRelease;
window.initReleaseProcess = initReleaseProcess;
//window.initReleaseTableHead = initReleaseTableHead

$(document).ready(() => init());

function initMilestonesButtons() {
    $('#btn-add-milestone').click(function () {
        $.get('/ui/milestone/add', () => {
        });
    });
}

function initConfirmModal() {
    $("#btn-confirm").on("click", function () {
        $("#confirm-modal").modal('show');
    });

    $("#modal-btn-yes").on("click", function () {
        $("#confirm-modal").modal('hide');
    });
}

function initValidatePom() {
    $("#validate-pom").on("click", function () {
        $.get("/ui/validation/pom")
            .done(function (data) {
                $("#validation-content > p").text(data);
                $("#validation-modal").modal('show');
            });
    });
}

function init() {
    initMilestonesButtons();
    initConfirmModal();
    initValidatePom();
}
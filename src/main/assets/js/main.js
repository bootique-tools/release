// styles and 3rd-party libs
import '../scss/main.scss';
import 'bootstrap/dist/js/bootstrap';
import '@fortawesome/fontawesome-free/js/all';

// export everything used directly from HTML in the global scope
import {
    jobProgress, initRepoVue, initIssueVue, initBranchView, initExtraRollback, initMavenVue,
    initMilestoneView, initPrVue, initReadmeView, initValidationView, initRelease
} from "./functions";
import {initReleaseProcess} from "./releaseProcess";

window.jobProgress = jobProgress;
window.initRepoVue = initRepoVue;
window.initIssueVue = initIssueVue;
window.initBranchView = initBranchView;
window.initExtraRollback = initExtraRollback;
window.initMavenVue = initMavenVue;
window.initMilestoneView = initMilestoneView;
window.initPrVue = initPrVue;
window.initReadmeView = initReadmeView;
window.initValidationView = initValidationView;
window.initRelease = initRelease;
window.initReleaseProcess = initReleaseProcess;

$(document).ready(() => init());

function initFolderSetup() {
    $('#btn-folder-setup').click(function () {
        const btn = $(this);
        btn.attr('disabled', true);
        $.get('/ui/git/select_path', (data) => {
            if (data) {
                location.reload();
            } else {
                btn.attr('disabled', false);
            }
        });
    });
}

function initRepoButtons() {
    $('#btn-update-all').click(function () {
        const btn = $(this);
        btn.attr('disabled', true);
        $('.repo-update').attr('disabled', true);
        $.get('/ui/git/update_all', () => {
            btn.attr('disabled', false);
            $('.repo-update').attr('disabled', false);
        });
    });

    $('#btn-clone-all').click(function () {
        const btn = $(this);
        btn.attr('disabled', true);
        $('.repo-clone').attr('disabled', true);
        $.get('/ui/git/clone_all', () => {
            btn.attr('disabled', false);
            $('.repo-clone').attr('disabled', false);
            location.reload();
        });
    });
}

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
    initFolderSetup();
    initRepoButtons();
    initMilestonesButtons();
    initConfirmModal();
    initValidatePom();
}
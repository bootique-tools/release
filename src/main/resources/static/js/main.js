
    $(document).ready(() => init());

    function initFolderSetup() {
        $('#btn-folder-setup').click(function() {
            const btn = $(this);
            btn.attr('disabled', true);
            $.get('/ui/git/select_path', (data) => {
                if(data) {
                    location.reload();
                } else {
                    btn.attr('disabled', false);
                }
            });
        });
    }

    function initRepoButtons() {
        $('#btn-update-all').click(function() {
            const btn = $(this);
            btn.attr('disabled', true);
            $('.repo-update').attr('disabled', true);
            $.get('/ui/git/update_all', () => {
                btn.attr('disabled', false);
                $('.repo-update').attr('disabled', false);
            });
        });

        $('#btn-clone-all').click(function() {
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
        $('#btn-add-milestone').click(function() {
            $.get('/ui/milestone/add', () => {});
        });
    }

    function initConfirmModal() {
        $("#btn-confirm").on("click", function(){
            $("#confirm-modal").modal('show');
        });

        $("#modal-btn-yes").on("click", function(){
            $("#confirm-modal").modal('hide');
        });
    }

    function init() {
        initFolderSetup();
        initRepoButtons();
        initMilestonesButtons();
        initConfirmModal();
    }

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
        $('.repo-clone').click(function() {
            const repo = $(this).attr('data-target');
            $(this).attr('disabled', true);
            $.get('/ui/git/clone?repo=' + repo, () => location.reload());
        });

        $('.repo-update').click(function() {
            const repo = $(this).attr('data-target');
            const btn = $(this);
            btn.attr('disabled', true);
            $.get('/ui/git/update?repo=' + repo, () => btn.attr('disabled', false));
        });

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

        $('.repo-view').click(function() {
            const btn = $(this);
            const repo = btn.attr('data-target');
            let type = btn.attr('data-type');
            if(!type) {
                type = 'viewer';
            }
            btn.attr('disabled', true);
            $.get(`/ui/git/open?repo=${repo}&type=${type}`, () => btn.attr('disabled', false));
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
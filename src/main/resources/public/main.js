checkSize(); // run asap (ideally before table load)

jQuery(document).ready(function($) {
    $(window).resize(checkSize);
    $(window).on('resize', checkSize());

    $(".link-row").click(function() {
        window.location = $(this).data("href");
    });
});

function checkSize() {
    var status = window.getComputedStyle(document.getElementsByClassName('sampleClass')[0]).float;
    if (status == "left" ) {
        $('td:nth-child(3)').hide();
        $('td:nth-child(5)').hide();
        $('td:nth-child(6)').hide();
    } else if (status == 'none') {
        $('td:nth-child(3)').show();
        $('td:nth-child(5)').show();
        $('td:nth-child(6)').show();
    }
}
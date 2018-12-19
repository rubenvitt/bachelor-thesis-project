function setChangeListener(checkbox, changedListener) {
    $(checkbox).change(function () {
        setCheckboxVisibility(this);
        changedListener(this.checked, $(this).attr('data-content'));
    });
}

function setCheckboxVisibility(checkbox) {
    const parent = $(checkbox).parent();
    parent.find('i').css('visibility', checkbox.checked ? 'visible' : 'hidden');
    if (checkbox.checked) {
        parent.addClass('active');
    } else {
        parent.removeClass('active');
    }
}

export function initCheckboxList(checkboxes, changedListener) {
    checkboxes.each(function () {
        setCheckboxVisibility(this);
        setChangeListener(this, changedListener);
    });
}
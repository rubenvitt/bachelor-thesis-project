const checkboxes = $('label').find('input[type=checkbox]');
setAllCheckboxVisibility();
setAllCheckboxesChanged();

function setAllCheckboxesChanged() {
    checkboxes.change(function () {
        console.log("CHANGED");
        setCheckboxVisibility(this);
    });
}

function setChangeListener(checkbox, changedListener) {
    $(checkbox).change(function () {
        setCheckboxVisibility(this);
        changedListener(this.checked, $(this).attr('data-content'));
    });
}

function setAllCheckboxVisibility() {
    console.log("Setting all checkbox-visibilities");
    checkboxes.each(function () {
        setCheckboxVisibility(this);
    })
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
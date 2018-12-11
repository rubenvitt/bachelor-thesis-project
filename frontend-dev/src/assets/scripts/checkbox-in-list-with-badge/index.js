const checkboxes = $('label').find('input[type=checkbox]');
setAllCheckboxVisibility();
setAllCheckboxesChanged();

function setAllCheckboxesChanged() {
    checkboxes.change(function () {
        console.log("CHANGED");
        setCheckboxVisibility(this);
    });
}

function setChangeListener(checkbox) {
    $(checkbox).change(function () {
        setCheckboxVisibility(this);
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

export function initCheckboxList(checkboxes) {
    checkboxes.each(function () {
        setCheckboxVisibility(this);
        setChangeListener(this);
    });
}
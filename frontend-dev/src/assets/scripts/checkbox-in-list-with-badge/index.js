function setChangeListener(checkbox, changedListener, radioList) {
    $(checkbox).change(function () {
        if (radioList)
            removeActiveStateFromCheckboxes(this);
        setCheckboxVisibility(this);
        changedListener(this.checked, $(this).attr('data-content'));
    });
}

function removeActiveStateFromCheckboxes(checkbox) {
    const parent = $(checkbox).parent().parent();
    parent.find('label').find('i').css('visibility', 'hidden');
    parent.find('label').removeClass('active');
    parent.find('label').find('input').checked = false;
    checkbox.checked = true;
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
        setChangeListener(this, changedListener, false);
    });
}

export function initRadioList(checkboxes, changedListener) {
    checkboxes.each(function () {
        setCheckboxVisibility(this);
        setChangeListener(this, changedListener, true);
    })
}
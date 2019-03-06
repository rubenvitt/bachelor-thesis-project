function setChangeListener(checkbox, changedListener, radioList) {
    $(checkbox).change(function () {
        if (radioList) {
            removeActiveStateFromCheckboxes(checkbox);
            setRadiobuttonVisibility(checkbox)
        } else {
            setCheckboxVisibility(this);
        }
        console.log("lala");
        changedListener(this.checked, $(this).attr('data-content'));
    });
}

function setOnButtonClickListener(checkbox, onButtonClickListener) {
    $(checkbox).parent().find('button').click(function () {
        onButtonClickListener($(checkbox).attr('data-content'));
        $(checkbox).attr('default', 'true');
        $(checkbox).parent().parent().find('input[default="true"]').parent().find('.input-group-append').removeClass('d-none');
        $(checkbox).parent().parent().find('input[default="true"]').parent().find('i.bg-white.badge').addClass('d-none');
        $(checkbox).parent().find('.input-group-append').addClass('d-none');
        $(checkbox).parent().find('i.bg-white.badge').removeClass('d-none');
    });
}

function removeActiveStateFromCheckboxes(checkbox) {
    const parent = $(checkbox).parent().parent();
    parent.find('label').find('i').css('visibility', 'hidden');
    parent.find('label').removeClass('active');
    parent.find('label').find('input').attr('checked', false);
    $(checkbox).attr('checked', true);
}

function setCheckboxVisibility(checkbox) {
    const parent = $(checkbox).parent();
    parent.find('.input-group-append').toggleClass('d-none', !checkbox.checked || checkbox.getAttribute('default') === 'true');
    if (checkbox.checked) {
        parent.addClass('active');
    } else {
        parent.removeClass('active');
    }
}

function setRadiobuttonVisibility(checkbox) {
    const parent = $(checkbox).parent();
    parent.children('i').css('visibility', $(checkbox).attr('checked') ? 'visible' : 'hidden');
    if ($(checkbox).attr('checked')) {
        parent.addClass('active');
    } else {
        parent.removeClass('active');
    }
}

export function initCheckboxList(checkboxes, changedListener, onButtonClickListener) {
    checkboxes.each(function () {
        setCheckboxVisibility(this);
        setChangeListener(this, changedListener, false);
        setOnButtonClickListener(this, onButtonClickListener);
    });
}

export function initRadioList(checkboxes, changedListener) {
    checkboxes.each(function () {
        setRadiobuttonVisibility(this);
        setChangeListener(this, changedListener, true);
    })
}
